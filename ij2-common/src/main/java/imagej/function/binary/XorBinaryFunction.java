package imagej.function.binary;

import imagej.function.BinaryFunction;

public class XorBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		return ((long)input1) ^ ((long)input2);
	}

}
