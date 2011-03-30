
package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.util.SettingsKeys;

/**
 * Runs the Edit::Options::Colors... dialog
 * 
 * @author Barry DeZonia
 */

@Plugin(menu = { @Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Colors...", weight = 8) })
public class OptionsColors implements ImageJPlugin {

	@Parameter(label = "Foreground", choices = { "red", "green", "blue",
		"magenta", "cyan", "yellow", "orange", "black", "white" },
		persistKey = SettingsKeys.OPTIONS_COLORS_FOREGROUND)
	private String fgColor;

	@Parameter(label = "Background", choices = { "red", "green", "blue",
		"magenta", "cyan", "yellow", "orange", "black", "white" },
		persistKey = SettingsKeys.OPTIONS_COLORS_BACKGROUND)
	private String bgColor;

	@Parameter(label = "Selection", choices = { "red", "green", "blue",
		"magenta", "cyan", "yellow", "orange", "black", "white" },
		persistKey = SettingsKeys.OPTIONS_COLORS_SELECTION)
	private String selColor;

	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
