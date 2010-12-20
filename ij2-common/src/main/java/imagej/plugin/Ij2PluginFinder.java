package imagej.plugin;

import java.util.ArrayList;
import java.util.List;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PluginFinder.class)
public class Ij2PluginFinder implements PluginFinder {

	@Override
	public void findPlugins(List<PluginEntry> plugins) {
		for (final IndexItem<Plugin, Runnable> item :
			Index.load(Plugin.class, Runnable.class))
		{
			final String pluginClass = item.className();
			final List<String> menuPath = new ArrayList<String>();

			// parse menu path from annotations
			final Menu[] menu = item.annotation().menu();
			if (menu != null) {
				for (Menu m : menu) menuPath.add(m.label());
			}
			else {
				// parse menuPath attribute
				final String[] menuPathTokens = item.annotation().menuPath().split(">");
				for (String token : menuPathTokens) menuPath.add(token);
			}
			
			// TEMP - use last menu element for label
			final int lastIndex = menuPath.size() - 1;
			final String label = menuPath.get(lastIndex);
			menuPath.remove(lastIndex);

			final String arg = "";
			final PluginEntry pluginEntry =
				new PluginEntry(pluginClass, menuPath, label, arg);
			plugins.add(pluginEntry);
		}
	}

}
