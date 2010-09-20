package imagej.process.function;

public class DifferenceBinaryFunction implements BinaryFunction {

	public double compute(double input1, double input2)
	{
		return ( Math.abs(input2 - input1) );
	}

}
