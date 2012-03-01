package imagej.util.eclipse;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
public class CheckSezpozProcessor extends AbstractProcessor {

	public static final String annotationPath = "META-INF/annotations/imagej.util.eclipse.CheckSezPoz";

	@Override
	public void init(ProcessingEnvironment environment) {
log("Called helper ");
Map<String, String> options = environment.getOptions();
for (final String key : options.keySet()) {
	log("\t" + key + " = " + options.get(key));
}
		super.init(environment);
		try {
			FileObject file = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", annotationPath);
			file.openOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> arg0,
			RoundEnvironment arg1) {
log("process");
		return false;
	}

	public void log(String message) {
		processingEnv.getMessager().printMessage(Kind.NOTE, message);
	}
}
