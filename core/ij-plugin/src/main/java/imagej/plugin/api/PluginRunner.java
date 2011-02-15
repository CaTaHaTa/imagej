package imagej.plugin.api;

import imagej.Log;
import imagej.plugin.PluginModule;
import imagej.plugin.RunnablePlugin;
import imagej.plugin.process.PluginPostprocessor;
import imagej.plugin.process.PluginPreprocessor;

/** Executes a runnable plugin. */
public class PluginRunner<T extends RunnablePlugin> {

	private PluginEntry<T> entry;

	public PluginRunner(final PluginEntry<T> entry) {
		this.entry = entry;
	}

	public T run() {
		final PluginModule<T> module;
		try {
			module = entry.createModule();
		}
		catch (final PluginException e) {
			Log.error(e);
			return null;
		}
		final T plugin = module.getPlugin();

		// execute plugin
		preProcess(module);
		plugin.run();
		postProcess(module);

		return plugin;
	}

	public void preProcess(final PluginModule<T> module) {
		for (final PluginEntry<PluginPreprocessor> p :
			PluginIndex.getIndex().getPlugins(PluginPreprocessor.class))
		{
			Log.debug("Preprocessing: " + p);//TEMP
			try {
				final PluginPreprocessor processor = p.createInstance();
				processor.process(module);
			}
			catch (final PluginException e) {
				Log.error(e);
			}
		}
	}

	public void postProcess(final PluginModule<T> module) {
		for (final PluginEntry<PluginPostprocessor> p :
			PluginIndex.getIndex().getPlugins(PluginPostprocessor.class))
		{
			try {
				final PluginPostprocessor processor = p.createInstance();
				processor.process(module);			
			}
			catch (final PluginException e) {
				Log.error(e);
			}
		}
	}

}
