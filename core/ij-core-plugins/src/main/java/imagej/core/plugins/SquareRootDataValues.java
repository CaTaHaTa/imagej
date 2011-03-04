package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Sqrt;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Square Root"
)
public class SquareRootDataValues implements ImageJPlugin
{
	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;
	
	public SquareRootDataValues()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new Sqrt();
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		NAryOperation operation = new NAryOperation(input, func);
		operation.setOutput(output);
		output = operation.run();
	}
}
