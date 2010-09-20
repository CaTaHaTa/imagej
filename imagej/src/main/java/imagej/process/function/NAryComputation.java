package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;

public class NAryComputation
{
	private NAryFunction function;
	private double[] inputDoubles;
	
	public NAryComputation(int numSamples, NAryFunction function)
	{
		this.function = function;
		this.inputDoubles = new double[numSamples];
	}
	
	public void compute(RealType<?> result, RealType<?>[] inputs)
	{
		for (int i = 0; i < inputDoubles.length; i++)
			inputDoubles[i] = inputs[i].getRealDouble();
		
		double resultValue = function.compute(inputDoubles);
		
		result.setReal(resultValue);
	}
}
