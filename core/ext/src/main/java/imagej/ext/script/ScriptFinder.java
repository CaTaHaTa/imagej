//
// ScriptFinder.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.ext.script;

import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Discovers scripts.
 * <p>
 * To accomplish this, we must crawl the plugins/ directory.
 * </p>
 * 
 * @author Johannes Schindelin
 */
public class ScriptFinder {

	private static final String SCRIPT_PLUGIN_CLASS = ScriptPlugin.class
		.getName();
	private static final String SCRIPT_PLUGIN_ICON = "/icons/script_code.png";
	private static final String SPECIAL_SUBDIRECTORY = "Scripts";

	private final ScriptService scriptService;

	public ScriptFinder(final ScriptService scriptService) {
		this.scriptService = scriptService;
	}

	private int scriptCount;

	/**
	 * Discover the scripts
	 * 
	 * @param plugins The collection to which the discovered scripts are added
	 */
	public void findPlugins(final List<PluginInfo<?>> plugins) {
		final String path = System.getProperty("plugins.dir");
		if (path == null) return;

		File directory = new File(path);
		if (!path.endsWith("plugins")) {
			final File pluginsDir = new File(directory, "plugins");
			if (pluginsDir.isDirectory()) directory = pluginsDir;
		}
		scriptCount = 0;
		discoverScripts(plugins, directory, null);
		Log.info("Found " + scriptCount + " scripts");
	}

	/**
	 * Look through a directory, discovering and adding scripts
	 * 
	 * @param plugins The collection to which the discovered scripts are added
	 * @param directory The directory in which to look for scripts recursively
	 * @param menuPath The menuPath. If <i>null</i>, it defaults to Plugins>,
	 *          except for the subdirectory <i>Scripts/</i> whose entries will be
	 *          pulled into the top-level menu structure
	 */
	private void discoverScripts(final List<PluginInfo<?>> plugins,
		final File directory, MenuPath menuPath)
	{
		final File[] fileList = directory.listFiles();
		if (fileList == null) return; // directory does not exist

		// TODO: sort?
		final boolean isTopLevel = menuPath == null;
		if (isTopLevel) menuPath = new MenuPath("Plugins");
		for (final File file : fileList)
			if (file.isDirectory()) {
				if (isTopLevel && file.getName().equals(SPECIAL_SUBDIRECTORY)) discoverScripts(
					plugins, file, new MenuPath());
				else discoverScripts(plugins, file, subMenuPath(menuPath, file
					.getName()));
			}
			else if (scriptService.isFileHandled(file)) {
				plugins.add(createEntry(file, subMenuPath(menuPath, file.getName())));
				scriptCount++;
			}
	}

	private MenuPath
		subMenuPath(final MenuPath menuPath, final String subMenuName)
	{
		final MenuPath result = new MenuPath(menuPath);
		result.add(new MenuEntry(subMenuName));
		return result;
	}

	private PluginInfo<ImageJPlugin> createEntry(final File scriptPath,
		final MenuPath menuPath)
	{
		final Map<String, Object> presets = new HashMap<String, Object>();
		presets.put("file", scriptPath);
		final PluginModuleInfo<ImageJPlugin> pe =
			new PluginModuleInfo<ImageJPlugin>(SCRIPT_PLUGIN_CLASS,
				ImageJPlugin.class);
		pe.setMenuPath(menuPath);
		pe.setPresets(presets);

		// flag script with special icon
		menuPath.getLeaf().setIconPath(SCRIPT_PLUGIN_ICON);

		return pe;
	}

}
