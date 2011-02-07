package imagej.ij1bridge.patches;

import ij.gui.ImageWindow;
import imagej.Log;
import imagej.ij1bridge.LegacyManager;

/** Overrides {@link ImageWindow} methods. */
public final class ImageWindowMethods {

	private ImageWindowMethods() {
		// prevent instantiation of utility class
	}

	/** Replaces {@link ImageWindow#setVisible(boolean). */
	public static void setVisible(ImageWindow obj, boolean visible) {
		Log.debug("ImageWindow.setVisible(" + visible + "): " + obj);
		if (!visible) return;
		LegacyManager.getImageMap().registerLegacyImage(obj.getImagePlus());
	}

	/** Replaces {@link ImageWindow#show(). */
	public static void show(ImageWindow obj) {
		setVisible(obj, true);
	}

}
