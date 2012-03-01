package imagej.util.eclipse;

import java.util.Collection;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class CheckSezpozFactory implements AnnotationProcessorFactory {

	@Override
	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> arg0,
			AnnotationProcessorEnvironment arg1) {
System.err.println("getProcessorFor");
		return null;
	}

	@Override
	public Collection<String> supportedAnnotationTypes() {
System.err.println("supportedAnnotationTypes");
		return null;
	}

	@Override
	public Collection<String> supportedOptions() {
System.err.println("supportedOptions");
		return null;
	}

}
