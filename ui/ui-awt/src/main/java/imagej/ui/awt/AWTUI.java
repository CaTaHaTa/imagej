//
// AWTUI.java
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

package imagej.ui.awt;

import imagej.ImageJ;
import imagej.display.Display;
import imagej.event.Events;
import imagej.platform.event.AppMenusCreatedEvent;
import imagej.plugin.PluginEntry;
import imagej.plugin.PluginManager;
import imagej.plugin.ui.ShadowMenu;
import imagej.plugin.ui.awt.MenuBarCreator;
import imagej.ui.UI;
import imagej.ui.UserInterface;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.MenuBar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * AWT-based user interface for ImageJ.
 *
 * @author Curtis Rueden
 */
@UI
public class AWTUI implements UserInterface {

	private Frame frame;
	private AWTToolBar toolBar;
	private AWTStatusBar statusBar;
	private Display activeDisplay;

	// -- UserInterface methods --

	@Override
	public void initialize() {
		frame = new Frame("ImageJ");
		toolBar = new AWTToolBar();
		statusBar = new AWTStatusBar();
		createMenuBar();

		frame.setLayout(new BorderLayout());
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});

		frame.add(toolBar, BorderLayout.NORTH);
		frame.add(statusBar, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void processArgs(final String[] args) {
		// TODO
	}

	@Override
	public AWTToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public AWTStatusBar getStatusBar() {
		return statusBar;
	}

	// -- Helper methods --

	private void createMenuBar() {
		final PluginManager pluginManager = ImageJ.get(PluginManager.class);
		final List<PluginEntry<?>> entries = pluginManager.getPlugins();
		final ShadowMenu rootMenu = new ShadowMenu(entries);
		final MenuBar menuBar = new MenuBar();
		new MenuBarCreator().createMenus(rootMenu, menuBar);
		frame.setMenuBar(menuBar);
		Events.publish(new AppMenusCreatedEvent(menuBar));
	}

}
