package imagej.core.plugins;

import mpicbg.imglib.algorithm.OutputAlgorithm;
import imagej.model.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Parameter;

/**
 * TODO
 *
 * @author Barry DeZonia
 */
public class ImglibOutputAlgorithmPlugin implements ImageJPlugin
{
	// ********** instance variables ****************************************************************
	
	@Parameter(output=true)
	protected Dataset output;

	private OutputAlgorithm algorithm;
	
	// ********** public interface ****************************************************************
	
	/** constructor that allows algorithm to be set later via setAlgorithm() */
	public ImglibOutputAlgorithmPlugin()
	{
	}
	
	/** preferred constructor */
	public ImglibOutputAlgorithmPlugin(final OutputAlgorithm algorithm)
	{
		this.algorithm = algorithm;
	}

	/** assign the algorithm this plugin should use. sometimes an algorithm cannot be assigned until subclass plugins have harvested their input variables. */
	public void setAlgorithm(final OutputAlgorithm algorithm)
	{
		this.algorithm = algorithm;
	}

	/** run the plugin and assign output */
	@Override
	public void run()
	{
		if (this.algorithm == null)
			throw new IllegalStateException("algorithm reference is null: algorithm must be set via constructor or setAlgorithm() before calling ImglibOutputAlgorithmPlugin::run()");
		
		if (!algorithm.checkInput() || !algorithm.process())
			throw new IllegalStateException(algorithm.getErrorMessage());
		
		output = new Dataset(algorithm.getResult());
	}
}
