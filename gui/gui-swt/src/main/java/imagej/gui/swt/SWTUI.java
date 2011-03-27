//
// SWTUI.java
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

package imagej.gui.swt;

import imagej.plugin.api.PluginEntry;
import imagej.plugin.api.PluginUtils;
import imagej.plugin.gui.ShadowMenu;
import imagej.plugin.gui.swt.MenuCreator;
import imagej.tool.ToolManager;
import imagej.ui.UserInterface;
import imagej.ui.UI;

import java.util.List;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * SWT-based user interface for ImageJ.
 *
 * @author Curtis Rueden
 */
@UI
public class SWTUI implements UserInterface, Runnable {

	private Display display;
	private Shell shell;
	private SWTStatusBar statusBar;

	// -- UserInterface methods --

	@Override
	public void initialize() {
		display = new Display();

		shell = new Shell(display, 0);
		shell.setLayout(new MigLayout("wrap 1"));
		shell.setText("ImageJ");
		new SWTToolBar(display, shell, new ToolManager());
		statusBar = new SWTStatusBar(shell);
		createMenuBar();

		shell.pack();
		shell.open();

		new Thread(this, "SWT-Dispatch").start();
	}

	// -- Runnable methods --

	@Override
	public void run() {
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();
	}

	// -- Helper methods --

	private void createMenuBar() {
		final List<PluginEntry<?>> entries = PluginUtils.findPlugins();
		statusBar.setStatus("Discovered " + entries.size() + " plugins");
		final ShadowMenu rootMenu = new ShadowMenu(entries);
		final Menu menuBar = new Menu(shell);
		new MenuCreator().createMenus(rootMenu, menuBar);
		shell.setMenuBar(menuBar); // TODO - is this necessary?
	}

}
