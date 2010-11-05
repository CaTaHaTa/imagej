package imagej.process.function.binary;

public class AverageFloatBinaryFunction implements BinaryFunction {

	public AverageFloatBinaryFunction()
	{
	}

	public double compute(double input1, double input2)
	{
		return (input1 + input2) / 2.0 ;
	}

}
