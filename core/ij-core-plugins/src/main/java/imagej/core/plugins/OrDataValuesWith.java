package imagej.core.plugins;

import imagej.plugin.Plugin;
import imagej.plugin.Parameter;
import imglib.ops.function.p1.UnaryOperatorFunction;
import imglib.ops.operator.UnaryOperator;
import imglib.ops.operator.unary.OrConstant;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
@Plugin(
	menuPath = "Process>Math2>OR"
)
public class OrDataValuesWith extends NAryOperation
{
	@Parameter(label="Enter value to OR with each data value")
	private long constant;
	
	public OrDataValuesWith()
	{
	}
	
	@Override
	public void run()
	{
		UnaryOperator op = new OrConstant(constant);
		UnaryOperatorFunction func = new UnaryOperatorFunction(op);
		setFunction(func);
		super.run();
	}
}
