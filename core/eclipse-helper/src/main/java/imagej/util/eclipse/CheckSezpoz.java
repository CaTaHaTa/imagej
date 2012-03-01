package imagej.util.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CheckSezpoz {

	public static boolean check() throws IOException {
		boolean upToDate = true;
		for (final String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
			if (!check(new File(path))) upToDate = false;
		}
		return upToDate;
	}

	public static boolean check(final File file) throws IOException {
		if (!file.exists()) return true;
		if (file.isDirectory()) return checkDirectory(file);
		else if (file.isFile() && file.getName().endsWith(".jar")) checkJar(file);
		else System.err.println("Skipping sezpoz check of " + file);
		return true;
	}

	public static boolean checkDirectory(final File directory) throws IOException {
		final File annotation = new File(directory, CheckSezpozProcessor.annotationPath);
		if (annotation.exists() && checkDirectory(directory,annotation.lastModified())) {
			return true;
		}

		fix(directory);
		return false;
	}

	public static boolean checkDirectory(final File directory, final long olderThan) throws IOException {
		if (directory.getName().equals("META-INF")) return true;

		final File[] list = directory.listFiles();
		if (list == null) return true;
		for (final File file : list) {
			if (file.isDirectory()) {
				if (!checkDirectory(file, olderThan)) return false;
			}
			else if (file.isFile() && file.lastModified() > olderThan) {
System.err.println(file.getPath() + " last modified: " + new java.util.Date(file.lastModified()));
System.err.println("older than: " + new java.util.Date(olderThan));
//throw new IOException("Annotations for " + file + " are out-of-date!");
				return false;
			}
		}
		return true;
	}

	public static void checkJar(final File file) throws IOException {
		final JarFile jar = new JarFile(file);
		final JarEntry annotation = jar.getJarEntry(CheckSezpozProcessor.annotationPath);
		if (annotation == null) {
			// Eclipse cannot generate .jar files (except in manual mode). Assume everything is alright
			return;
		}

		final long mtime = annotation.getTime();
		if (mtime < 0) {
			// Eclipse cannot generate .jar files (except in manual mode). Assume everything is alright
			return;
		}
		for (final JarEntry entry : iterate(jar.entries())) {
			if (entry.getTime() > mtime) {
				throw new IOException("Annotations for " + entry + " in " + file + " are out-of-date!");
			}
		}
	}

	public static boolean fix(final File directory) {
System.err.println("Running sezpoz annotation on " + directory);
		final Method aptProcess;
		try {
			Class<?> aptClass = CheckSezpoz.class.getClassLoader().loadClass("com.sun.tools.apt.Main");
			aptProcess = aptClass.getMethod("process", new Class[] { String[].class });
		} catch (Exception e) {
			System.err.println("Could not fix " + directory + ": apt not found");
			e.printStackTrace();
			return false;
		}
		if (!directory.getPath().endsWith("target/classes")) {
			System.err.println("Ignoring non-Maven build directory: " + directory.getPath());
			return false;
		}
		final File baseDirectory = directory.getParentFile().getParentFile();
		if (baseDirectory == null) return false;
		final File srcDirectory = new File(baseDirectory, "src/main/java");
		if (!srcDirectory.exists()) {
			System.err.println("Sources are not in the expected place: " + srcDirectory);
			return false;
		}

		// before running, remove possibly outdated annotations
		final File[] obsoleteAnnotations = new File(directory, "META-INF/annotations").listFiles();
		if (obsoleteAnnotations != null) {
			for (final File annotation : obsoleteAnnotations) annotation.delete();
		}

		List<String> aptArgs = new ArrayList<String>();
		//aptArgs.add("-nocompile");
		aptArgs.add("-factory");
		aptArgs.add("net.java.sezpoz.impl.IndexerFactory");
		aptArgs.add("-d");
		aptArgs.add(directory.getPath());
		addJavaPathsRecursively(aptArgs, srcDirectory);
		final String[] args = aptArgs.toArray(new String[aptArgs.size()]);
		try {
			aptProcess.invoke(null, new Object[] { args });
		} catch (Exception e) {
			System.err.println("Could not fix " + directory + ": apt failed");
			e.printStackTrace();
			return false;
		}

		// pretend that the CheckSezpoz annotator also ran
		final File annotation = new File(directory, CheckSezpozProcessor.annotationPath);
		try {
			touch(annotation);
		} catch (IOException e) {
			System.err.println("Could not touch " + annotation);
		}
		return true;
	}

	protected static void addJavaPathsRecursively(final List<String> list,
		final File directory)
	{
		final File[] files = directory.listFiles();
		if (files == null) return;
		for (final File file : files) {
			if (file.isDirectory()) addJavaPathsRecursively(list, file);
			else if (file.isFile() && file.getName().endsWith(".java")) list.add(file.getPath());
		}
	}

	protected static void touch(File file) throws IOException {
		new FileOutputStream(file, true).close();
	}

	public static <T> Iterable<T> iterate(final Enumeration<T> en) {
		final Iterator<T> iterator = new Iterator<T>() {
			public boolean hasNext() {
				return en.hasMoreElements();
			}

			public T next() {
				return en.nextElement();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};

		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}

	public static void main(String[] args) throws IOException {
		check();
	}
}
