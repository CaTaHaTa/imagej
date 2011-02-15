package imagej.gui;

import imagej.Log;
import imagej.dataset.Dataset;
import imagej.plugin.Plugin;
import imagej.plugin.PluginModule;
import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginException;
import imagej.plugin.api.PluginIndex;
import imagej.plugin.process.PluginPostprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Plugin(type = PluginPostprocessor.class)
public class DisplayPostprocessor implements PluginPostprocessor {

	@Override
	public void process(PluginModule<?> module) {
		final Map<String, Object> outputs = module.getOutputs();
		handleOutput(outputs.values());
	}

	/** Displays output datasets. */
	public void handleOutput(Object value) {
		if (value instanceof Collection) {
			final Collection<?> collection = (Collection<?>) value;
			for (final Object item : collection) handleOutput(item);
		}
		else if (value instanceof Dataset) {
			final Dataset dataset = (Dataset) value;

			// get available display plugins from the plugin index
			final ArrayList<PluginEntry<DisplayPlugin>> plugins =
				PluginIndex.getIndex().getPlugins(DisplayPlugin.class);
			for (final PluginEntry<DisplayPlugin> pe : plugins) {
				try {
					final DisplayPlugin displayPlugin = pe.createInstance();
					// display dataset using the first compatible DisplayPlugin
					// TODO: prompt user with dialog box if multiple matches
					if (displayPlugin.canDisplay(dataset)) {
						displayPlugin.display(dataset);
						break;
					}
				}
				catch (PluginException e) {
					Log.error(e);
				}
			}
		}
		else {
			// ignore non-Dataset output
		}
	}

}
