package imagej.process.function;

import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class InvertUnaryFunction implements UnaryFunction
{
	private double min, max;
	private boolean dataIsIntegral;
	
	public InvertUnaryFunction(RealType<?> targetType, double min, double max)
	{
		this.min = min;
		this.max = max;
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
	}
	
	public void compute(RealType<?> result, RealType<?> input)
	{
		double value = this.max - (input.getRealDouble() - this.min);
		
		if (this.dataIsIntegral)
			value = TypeManager.boundValueToType(result, value);
		
		result.setReal( value );
	}
}
