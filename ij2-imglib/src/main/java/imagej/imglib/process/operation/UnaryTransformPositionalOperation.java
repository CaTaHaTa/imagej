package imagej.imglib.process.operation;

import imagej.function.UnaryFunction;
import imagej.imglib.computation.UnaryComputation;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

/** UnaryTransformPositionalOperation transforms the values in a dataset using the result of applying a UnaryFunction
 *  computation that uses the values in the dataset. The computation takes a sample value from the dataset
 *  and returns a value as defined by the given UnaryFunction. The Negate() function would be an example
 *  of a UnaryFunction that returns the result of multiplying -1 * its input value. This operation is Positional
 *  so that you can use positional information in any attached SelectionFunction. See base class for more
 *  information about SelectionFunctions.
 * */
public class UnaryTransformPositionalOperation<T extends RealType<T>> extends PositionalSingleCursorRoiOperation<T>
{
	private UnaryComputation computer;
	
	public UnaryTransformPositionalOperation(Image<T> image, int[] origin, int[] span, UnaryFunction func) 
	{
		super(image, origin, span);
		this.computer = new UnaryComputation(func);
	}
	
	@Override
	protected void beforeIteration(RealType<T> type)
	{
	}

	@Override
	protected void insideIteration(int[] position, RealType<T> sample)
	{
		this.computer.compute(sample, sample);
	}

	@Override
	protected void afterIteration()
	{
	}

}
