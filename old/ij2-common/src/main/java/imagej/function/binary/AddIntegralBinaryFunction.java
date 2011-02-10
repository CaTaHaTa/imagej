package imagej.function.binary;

import imagej.function.BinaryFunction;

public class AddIntegralBinaryFunction implements BinaryFunction {

	private double max;
	
	public AddIntegralBinaryFunction(double maxValue)
	{
		this.max = maxValue;
	}
	
	public double compute(double input1, double input2)
	{
		double value = input1 + input2;
		if (value > this.max)
			value = this.max;
		return value;
	}

}
