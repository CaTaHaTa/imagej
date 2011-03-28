//
// LegacyManager.java
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

package imagej.legacy;

import ij.ImageJ;
import ij.ImagePlus;
import imagej.legacy.plugin.LegacyPlugin;
import imagej.manager.Manager;
import imagej.manager.ManagerComponent;
import imagej.model.Dataset;

/**
 * Manager component for working with legacy ImageJ 1.x.
 * <p>
 * The legacy manager overrides the behavior of various IJ1 methods,
 * inserting seams so that (e.g.) the modern GUI is aware of IJ1 events
 * as they occur.
 * </p>
 * <p>
 * It also maintains an image map between IJ1 {@link ImagePlus} objects
 * and IJ2 {@link Dataset}s.
 * </p>
 * <p>
 * In this fashion, when a legacy plugin is executed on a {@link Dataset},
 * the manager transparently translates it into an {@link ImagePlus}, and
 * vice versa, enabling backward compatibility with legacy plugins.
 * </p>
 *
 * @author Curtis Rueden
 */
@Manager(priority = LegacyManager.PRIORITY)
public final class LegacyManager implements ManagerComponent {

	public static final int PRIORITY = 0;

	static {
		// NB: Override class behavior before class loading gets too far along.
		final CodeHacker hacker = new CodeHacker();

		// override behavior of ij.IJ
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(double progress)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showProgress(int currentIndex, int finalIndex)");
		hacker.insertAfterMethod("ij.IJ",
			"public static void showStatus(java.lang.String s)");
		hacker.loadClass("ij.IJ");

		// override behavior of ij.ImagePlus
		hacker.insertAfterMethod("ij.ImagePlus", "public void updateAndDraw()");
		hacker.loadClass("ij.ImagePlus");

		// override behavior of ij.gui.ImageWindow
		hacker.insertMethod("ij.gui.ImageWindow",
			"public void setVisible(boolean vis)");
		hacker.insertMethod("ij.gui.ImageWindow", "public void show()");
		hacker.loadClass("ij.gui.ImageWindow");

		// override behavior of MacAdapter
		hacker.replaceMethod("MacAdapter",
			"public void run(java.lang.String arg)", ";");
		hacker.loadClass("MacAdapter");
	}

	/** Mapping between datasets and legacy image objects. */
	private LegacyImageMap imageMap;

	public LegacyImageMap getImageMap() {
		return imageMap;
	}

	public void legacyImageChanged(final ImagePlus imp) {
		// register image with legacy manager
		final Dataset dataset = imageMap.registerLegacyImage(imp);

		// record resultant dataset as a legacy plugin output
		LegacyPlugin.getOutputSet().add(dataset);
	}

	// -- ManagerComponent methods --

	@Override
	public void initialize() {
		imageMap = new LegacyImageMap();

		// initialize legacy ImageJ application
		new ImageJ(ImageJ.NO_SHOW);
	}

}
