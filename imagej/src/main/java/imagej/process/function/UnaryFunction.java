package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public interface UnaryFunction {
	void compute(RealType<?> result, RealType<?> input);
}
