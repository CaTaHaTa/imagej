//
// RecentFilesService.java
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

package imagej.script.editor;

import imagej.ext.MenuEntry;
import imagej.ext.MenuPath;
import imagej.ext.menu.MenuConstants;
import imagej.ext.menu.ShadowMenu;
import imagej.ext.plugin.PluginModuleInfo;
import imagej.script.editor.plugins.RecentFilesMenuItem;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class RecentFilesService {
	protected EditorFrame editorFrame;
	protected ShadowMenu rootMenu;
	protected Map<File, PluginModuleInfo<ScriptEditorPlugin>> recentFiles;

	public RecentFilesService(final EditorFrame editorFrame, final ShadowMenu rootMenu) {
		this.editorFrame = editorFrame;
		this.rootMenu = rootMenu;
		recentFiles = new LinkedHashMap<File, PluginModuleInfo<ScriptEditorPlugin>>();
	}

	public void clear() {
		final Set<PluginModuleInfo<?>> set = new HashSet<PluginModuleInfo<?>>();
		for (final File file : recentFiles.keySet()) set.add(recentFiles.get(file));
		rootMenu.removeAll(set);
		recentFiles.clear();
	}

	public void add(final File file) {
		final PluginModuleInfo<ScriptEditorPlugin> info =
				new PluginModuleInfo<ScriptEditorPlugin>("imagej.script.editor.plugins.RecentFilesMenuItem",
						ScriptEditorPlugin.class);

		// hard code path to open as a preset
		final HashMap<String, Object> presets = new HashMap<String, Object>();
		presets.put("editorFrame", editorFrame);
		presets.put("file", file);
		info.setPresets(presets);

		if (recentFiles.containsKey(info)) remove(file);

		setMenuPath(info, file, recentFiles.size() + 1);
		recentFiles.put(file, info);
		rootMenu.add(info);
	}

	public void remove(final File file) {
		remove(recentFiles.get(file));
	}

	public void remove(final PluginModuleInfo<?> info) {
		recentFiles.remove(info);
		renumberItems();
		rootMenu.remove(info);
	}

	private void renumberItems() {
		int count = 1;
		for (final File file : recentFiles.keySet()) {
			setMenuPath(recentFiles.get(file), file, count++);
		}
	}

	private void setMenuPath(PluginModuleInfo<ScriptEditorPlugin> info,
		File file, int index)
	{
		char mnemonic = (char)(index < 10 ? '0' + index : 0);
		info.setDescription(file.getPath());
		final MenuPath menuPath = new MenuPath();
		menuPath.add(new MenuEntry(MenuConstants.FILE_LABEL));
		menuPath.add(new MenuEntry(RecentFilesMenuItem.MENU_NAME));
		menuPath.add(new MenuEntry(file.getName(), index, mnemonic, null, null));
		info.setMenuPath(menuPath);
	}

}
