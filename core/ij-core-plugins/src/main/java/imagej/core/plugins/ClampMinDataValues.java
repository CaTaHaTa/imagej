package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Min;

/**
 * Fills an output Dataset by clamping an input Dataset such that no values are less than a user defined constant value.
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "PureIJ2>Process>Math>Min"
)
public class ClampMinDataValues implements ImageJPlugin
{
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	Dataset input;
	
	@Parameter(output=true)
	Dataset output;
	
	@Parameter(label="Enter minimum clamp value")
	private double constant;
	
	// ***************  constructor ***************************************************************

	public ClampMinDataValues()
	{
	}
	
	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new Min(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		NAryOperation operation = new NAryOperation(input, func);
		operation.setOutput(output);
		output = operation.run();
	}
}
