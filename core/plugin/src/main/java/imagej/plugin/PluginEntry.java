//
// PluginEntry.java
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

package imagej.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of metadata about a particular plugin. For performance reasons,
 * the metadata is populated without actually loading the plugin class, by
 * reading from an efficient binary cache (see {@link PluginManager} for
 * details). As such, ImageJ can very quickly build a complex menu structure
 * containing all available plugins without waiting for the Java class loader.
 * 
 * @author Curtis Rueden
 * @see ImageJPlugin
 * @see Plugin
 * @see PluginManager
 */
public class PluginEntry<T extends BasePlugin> extends BaseEntry<T> {

	/** Type of this entry's plugin; e.g., {@link ImageJPlugin}. */
	private Class<T> pluginType;

	/** Linked boolean parameter; see {@link Plugin#toggleParameter}. */
	private String toggleParameter;

	/** Toggle plugin's group; see {@link Plugin#toggleGroup}. */
	private String toggleGroup;

	/** Path to this plugin's suggested position in the menu structure. */
	private List<MenuEntry> menuPath;

	/** List of inputs with fixed, preset values. */
	private Map<String, Object> presets;

	/** Factory used to create a module associated with this entry's plugin. */
	private PluginModuleFactory<T> factory;

	public PluginEntry(final String className, final Class<T> pluginType) {
		setClassName(className);
		setPluginType(pluginType);
		setMenuPath(null);
		setPresets(null);
		setPluginModuleFactory(null);
	}

	public void setPluginType(final Class<T> pluginType) {
		this.pluginType = pluginType;
	}

	public Class<T> getPluginType() {
		return pluginType;
	}

	public void setToggleParameter(final String toggleParameter) {
		this.toggleParameter = toggleParameter;
	}

	public String getToggleParameter() {
		return toggleParameter;
	}

	public void setToggleGroup(final String toggleGroup) {
		this.toggleGroup = toggleGroup;
	}

	public String getToggleGroup() {
		return toggleGroup;
	}

	public void setMenuPath(final List<MenuEntry> menuPath) {
		if (menuPath == null) {
			this.menuPath = new ArrayList<MenuEntry>();
		}
		else {
			this.menuPath = menuPath;
		}
	}

	public List<MenuEntry> getMenuPath() {
		return menuPath;
	}

	public void setPresets(final Map<String, Object> presets) {
		if (presets == null) {
			this.presets = new HashMap<String, Object>();
		}
		else {
			this.presets = presets;
		}
	}

	public Map<String, Object> getPresets() {
		return presets;
	}

	public void setPluginModuleFactory(final PluginModuleFactory<T> factory) {
		if (factory == null) {
			this.factory = new DefaultPluginModuleFactory<T>();
		}
		else {
			this.factory = factory;
		}
	}

	public PluginModuleFactory<T> getPluginModuleFactory() {
		return factory;
	}

	/**
	 * Creates a module to work with this entry, using the entry's associated
	 * {@link PluginModuleFactory}.
	 */
	public PluginModule<T> createModule() throws PluginException {
		return factory.createModule(this);
	}

	// -- Object methods --

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		final String className = getClassName();
		sb.append(className);
		boolean firstField = true;

		final String name = getName();
		if (name != null && !name.isEmpty()) {
			firstField = handleFirst(firstField, sb);
			sb.append("name = " + name);
		}

		final String label = getLabel();
		if (label != null && !label.isEmpty()) {
			firstField = handleFirst(firstField, sb);
			sb.append("label = " + label);
		}

		final String description = getDescription();
		if (description != null && !description.isEmpty()) {
			firstField = handleFirst(firstField, sb);
			sb.append("description = " + description);
		}

		final String tParam = getToggleParameter();
		if (tParam != null && !tParam.isEmpty()) {
			firstField = handleFirst(firstField, sb);
			sb.append("toggleParameter = " + tParam);
		}

		final String tGroup = getToggleGroup();
		if (tGroup != null && !tGroup.isEmpty()) {
			firstField = handleFirst(firstField, sb);
			sb.append("toggleGroup = " + tGroup);
		}

		if (!menuPath.isEmpty()) {
			firstField = handleFirst(firstField, sb);
			sb.append("menu = " + getMenuString(menuPath));
		}

		final String iconPath = getIconPath();
		if (iconPath != null && !iconPath.isEmpty()) {
			firstField = handleFirst(firstField, sb);
			sb.append("iconPath = " + iconPath);
		}

		final int priority = getPriority();
		if (priority < Integer.MAX_VALUE) {
			firstField = handleFirst(firstField, sb);
			sb.append("priority = " + priority);
		}

		for (final String key : presets.keySet()) {
			final Object value = presets.get(key);
			firstField = handleFirst(firstField, sb);
			sb.append(key + " = '" + value + "'");
		}

		if (!firstField) sb.append("]");

		return sb.toString();
	}

	// -- Utility methods --

	public static String getMenuString(final List<MenuEntry> menuPath) {
		final StringBuilder sb = new StringBuilder();
		boolean firstMenu = true;
		for (final MenuEntry menu : menuPath) {
			if (firstMenu) firstMenu = false;
			else sb.append(" > ");
			sb.append(menu);
		}
		return sb.toString();
	}

	// -- Helper methods --

	private boolean handleFirst(final boolean first, final StringBuilder sb) {
		sb.append(first ? " [" : "; ");
		return false;
	}

}
