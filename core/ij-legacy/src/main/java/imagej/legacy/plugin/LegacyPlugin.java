package imagej.legacy.plugin;

import ij.IJ;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes an IJ1 plugin.
 *
 * @author Curtis Rueden
 */
public class LegacyPlugin implements ImageJPlugin {

	@Parameter
	private String className;

	@Parameter
	private String arg;

	@Parameter(output=true)
	private List<Dataset> outputs;

	@Override
	public void run() {
		final Set<Dataset> outputSet = LegacyPlugin.getOutputSet();
		outputSet.clear();
		IJ.runPlugIn(className, arg);
		outputs = new ArrayList<Dataset>(outputSet);
		outputSet.clear();
	}

	public List<Dataset> getOutputs() {
		return outputs;
	}

	/** Used to provide one list of datasets per calling thread. */
	private static ThreadLocal<Set<Dataset>> outputDatasets =
		new ThreadLocal<Set<Dataset>>()
	{
		@Override
		protected synchronized Set<Dataset> initialValue() {
			return new HashSet<Dataset>();
		}
	};

	/**
	 * Gets a list for storing output parameter values.
	 * This method is thread-safe, because it uses a separate map per thread.
	 */
	public static Set<Dataset> getOutputSet() {
		return outputDatasets.get();
	}

}
