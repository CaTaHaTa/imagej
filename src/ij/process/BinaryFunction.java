package ij.process;

import mpicbg.imglib.type.numeric.RealType;

public interface BinaryFunction {
	void compute(RealType<?> result, RealType<?> input1, RealType<?> input2);
}
