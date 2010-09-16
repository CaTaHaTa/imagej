package imagej.process.operation;

import ij.process.ImgLibProcessor;
import imagej.process.TypeManager;
import mpicbg.imglib.type.numeric.RealType;

public class FindEdgesFilterOperation<K extends RealType<K>> extends Filter3x3Operation<K>
{
	private boolean dataIsIntegral;
	private RealType<?> dataType;
	
	public FindEdgesFilterOperation(ImgLibProcessor<K> ip, int[] origin, int[] span)
	{
		super(ip, origin, span);
		this.dataIsIntegral = TypeManager.isIntegralType(ip.getType());
		this.dataType = ip.getType();
	}

	protected double calcSampleValue(final double[] neighborhood)
	{
		double sum1 = neighborhood[0] + 2*neighborhood[1] + neighborhood[2]
		               - neighborhood[6] - 2*neighborhood[7] - neighborhood[8]
		                                                                    ;
		double sum2 = neighborhood[0] + 2*neighborhood[3] + neighborhood[6]
		               - neighborhood[2] - 2*neighborhood[5] - neighborhood[8];
		
		double value = Math.sqrt(sum1*sum1 + sum2*sum2);
		
		if (this.dataIsIntegral)
		{
			value = Math.floor(value);
			value = TypeManager.boundValueToType(this.dataType, value);
		}

		return value;
	}
}
