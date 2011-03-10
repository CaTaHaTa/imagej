package imagej.core.plugins.options;

import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Runs the Edit::Options::Colors... dialog
 * 
 * @author Barry DeZonia
 */

@Plugin(menu = { 
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Colors...", weight = 8) })
public class OptionsColors implements ImageJPlugin{

	@Parameter(label = "Foreground", persist=true, choices = {"red","green","blue","magenta", "cyan", "yellow", "orange", "black", "white"})
	private String fgColor;
	
	@Parameter(label = "Background", persist=true, choices = {"red","green","blue","magenta", "cyan", "yellow", "orange", "black", "white"})
	private String bgColor;
	
	@Parameter(label = "Selection", persist=true, choices = {"red","green","blue","magenta", "cyan", "yellow", "orange", "black", "white"})
	private String selColor;
	
	@Override
	public void run() {
		// DO NOTHING - all functionality contained in annotations
	}

}
