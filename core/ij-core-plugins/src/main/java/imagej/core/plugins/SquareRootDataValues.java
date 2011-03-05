package imagej.core.plugins;

import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
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
	// ***************  instance variables that are Parameters ***************************************************************

	@Parameter
	private Dataset input;
	
	@Parameter(output=true)
	private Dataset output;

	// ***************  public interface ***************************************************************

	@Override
	public void run()
	{
		UnaryOperator op = new Sqrt();
		output = new UnaryTransformation(input, output, op).run();
	}
}
