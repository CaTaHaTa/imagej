package imagej.process.function;

import mpicbg.imglib.type.numeric.RealType;
import imagej.process.TypeManager;

public class MultiplyBinaryFunction implements BinaryFunction {

	private boolean dataIsIntegral;
	private double max;
	
	public MultiplyBinaryFunction(RealType<?> targetType)
	{
		this.dataIsIntegral = TypeManager.isIntegralType(targetType);
		this.max = targetType.getMaxValue();
	}

	public double compute(double input1, double input2)
	{
		double value = input1 * input2;
		if ((this.dataIsIntegral) && (value > this.max))
			value = this.max;
		return value;
	}

}
