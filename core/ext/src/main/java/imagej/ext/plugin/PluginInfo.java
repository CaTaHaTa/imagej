//
// PluginInfo.java
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

package imagej.ext.plugin;

import imagej.ext.IndexItemInfo;
import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.ext.UIDetails;

/**
 * A collection of metadata about a particular plugin. For performance reasons,
 * the metadata is populated without actually loading the plugin class, by
 * reading from an efficient binary cache (see {@link PluginService} for
 * details). As such, ImageJ can very quickly build a complex menu structure
 * containing all available plugins without waiting for the Java class loader.
 * 
 * @author Curtis Rueden
 * @see ImageJPlugin
 * @see Plugin
 * @see PluginService
 */
public class PluginInfo<P extends IPlugin> extends IndexItemInfo<P> {

	/** Type of this entry's plugin; e.g., {@link ImageJPlugin}. */
	private Class<P> pluginType;

	/** Annotation describing the plugin. */
	protected Plugin plugin;

	public PluginInfo(final String className, final Class<P> pluginType) {
		setClassName(className);
		setPluginType(pluginType);
		setMenuPath(null);
	}

	public PluginInfo(final String className, final Class<P> pluginType,
		final Plugin plugin)
	{
		this(className, pluginType);
		this.plugin = plugin;

		populate(this, plugin);
	}

	// -- PluginInfo methods --

	public void setPluginType(final Class<P> pluginType) {
		this.pluginType = pluginType;
	}

	public Class<P> getPluginType() {
		return pluginType;
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
		if (pluginType != null) {
			appendParam(sb, "pluginType", pluginType);
		}
		return sb.toString();
	}

	// -- Utility methods --

	/**
	 * Populates the given {@link UIDetails} object with the specified
	 * 
	 * @{@link Plugin} annotation's values.
	 */
	public static void populate(final UIDetails info, final Plugin plugin) {
		info.setName(plugin.name());
		info.setLabel(plugin.label());
		info.setDescription(plugin.description());

		final MenuPath menuPath = new MenuPath();
		final Menu[] menu = plugin.menu();
		if (menu.length > 0) {
			parseMenuPath(menuPath, menu);
		}
		else {
			// parse menuPath attribute
			final String path = plugin.menuPath();
			if (!path.isEmpty()) parseMenuPath(menuPath, path);
		}
		info.setMenuPath(menuPath);

		final String iconPath = plugin.iconPath();
		info.setIconPath(iconPath);
		info.setPriority(plugin.priority());
		info.setEnabled(plugin.enabled());
		info.setSelectable(plugin.selectable());
		info.setSelectionGroup(plugin.selectionGroup());

		// add default icon if none attached to leaf
		final MenuEntry menuLeaf = menuPath.getLeaf();
		if (menuLeaf != null && !iconPath.isEmpty()) {
			final String menuIconPath = menuLeaf.getIconPath();
			if (menuIconPath == null || menuIconPath.isEmpty()) {
				menuLeaf.setIconPath(iconPath);
			}
		}
	}

	// -- Helper methods --

	private static void
		parseMenuPath(final MenuPath menuPath, final Menu[] menu)
	{
		for (int i = 0; i < menu.length; i++) {
			final String name = menu[i].label();
			final double weight = menu[i].weight();
			final char mnemonic = menu[i].mnemonic();
			final String accel = menu[i].accelerator();
			final String iconPath = menu[i].iconPath();
			menuPath.add(new MenuEntry(name, weight, mnemonic, accel, iconPath));
		}
	}

	private static void
		parseMenuPath(final MenuPath menuPath, final String path)
	{
		final String[] menuPathTokens = path.split(">");
		for (final String token : menuPathTokens) {
			menuPath.add(new MenuEntry(token.trim()));
		}
	}

}
