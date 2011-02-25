package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.XorConstant;

@Plugin(
	menuPath = "Process>XOR"
)
@SuppressWarnings("rawtypes")
public class XorDataValuesWith extends NAryOperation
{
	@Parameter(label="Enter value to XOR with each data value")
	private long constant;
	
	public XorDataValuesWith()
	{
		UnaryOperator op = new XorConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
	}
}
