package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.Constant;

@Plugin(
	menuPath = "Process>Set"
)
@SuppressWarnings("rawtypes")
public class SetDataValues extends NAryOperation
{
	@Parameter(label="Enter value to set each data value to")
	private double constant;
	
	public SetDataValues()
	{
		UnaryOperator op = new Constant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
