package imagej.plugin.spi;

import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;

public interface PluginRunner {

	/**
	 * Executes the given plugin.
	 *
	 * @return instance of the executed plugin, when applicable.
	 * @throws PluginException if the plugin cannot be executed. 
	 */
	Object runPlugin(PluginEntry plugin) throws PluginException;

}
