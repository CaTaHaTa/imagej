//
// AbstractAWTMenuCreator.java
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

package imagej.ext.ui.awt;

import imagej.ImageJ;
import imagej.ext.Accelerator;
import imagej.ext.menu.AbstractMenuCreator;
import imagej.ext.menu.ShadowMenu;
import imagej.ext.module.ModuleInfo;
import imagej.ext.plugin.PluginService;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Populates an AWT menu structure with menu items from a {@link ShadowMenu}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public abstract class AbstractAWTMenuCreator<T> extends
	AbstractMenuCreator<T, Menu>
{

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final MenuItem menuItem = createLeaf(shadow);
		target.add(menuItem);
	}

	@Override
	protected Menu addNonLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final Menu menu = createNonLeaf(shadow);
		target.add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToMenu(final Menu target) {
		target.addSeparator();
	}

	protected MenuItem createLeaf(final ShadowMenu shadow) {
		final MenuItem menuItem = new MenuItem(shadow.getMenuEntry().getName());
		assignProperties(menuItem, shadow);
		linkAction(shadow.getModuleInfo(), menuItem);
		return menuItem;
	}

	protected Menu createNonLeaf(final ShadowMenu shadow) {
		final Menu menu = new Menu(shadow.getMenuEntry().getName());
		assignProperties(menu, shadow);
		return menu;
	}

	// -- Helper methods --

	private void
		assignProperties(final MenuItem menuItem, final ShadowMenu shadow)
	{
		final Accelerator acc = shadow.getMenuEntry().getAccelerator();
		if (acc != null) {
			final int code = acc.getKeyCode().getCode();
			final boolean shift = acc.getModifiers().isShiftDown();
			final MenuShortcut shortcut = new MenuShortcut(code, shift);
			menuItem.setShortcut(shortcut);
		}
	}

	private void linkAction(final ModuleInfo info, final MenuItem menuItem) {
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				ImageJ.get(PluginService.class).run(info);
			}
		});
	}

}
