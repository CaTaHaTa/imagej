package imagej.gui.swing;

import imagej.Log;
import imagej.gui.menus.MenuCreator;
import imagej.gui.menus.ShadowMenu;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class JMenuBarCreator extends SwingMenuCreator
	implements MenuCreator<JMenuBar>
{

	@Override
	public void createMenus(final ShadowMenu root, final JMenuBar menuBar) {
		// create menu items and add to menu bar
		final List<JMenuItem> childMenuItems = createChildMenuItems(root);
		for (final JMenuItem childMenuItem : childMenuItems) {
			if (childMenuItem instanceof JMenu) {
				final JMenu childMenu = (JMenu) childMenuItem;
				menuBar.add(childMenu);
			}
			else {
				Log.warn("Ignoring unexpected leaf menu item: " + childMenuItem);
			}
		}
	}

}
