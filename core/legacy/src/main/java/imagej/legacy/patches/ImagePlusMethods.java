//
// ImagePlusMethods.java
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

package imagej.legacy.patches;

import ij.ImagePlus;
import imagej.ImageJ;
import imagej.legacy.LegacyOutputTracker;
import imagej.legacy.LegacyService;
import imagej.util.Log;

/**
 * Overrides {@link ImagePlus} methods.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public final class ImagePlusMethods {

	private ImagePlusMethods() {
		// prevent instantiation of utility class
	}

	/** Appends {@link ImagePlus#updateAndDraw()}. */
	public static void updateAndDraw(final ImagePlus obj) {
		Log.debug("ImagePlus.updateAndDraw(): " + obj);
		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		legacyService.legacyImageChanged(obj);
	}

	/** Appends {@link ImagePlus#repaintWindow()}. */
	public static void repaintWindow(final ImagePlus obj) {
		Log.debug("ImagePlus.repaintWindow(): " + obj);
		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		legacyService.legacyImageChanged(obj);
	}

	/** Appends {@link ImagePlus#show(String message)}. */
	public static void show(final ImagePlus obj, @SuppressWarnings("unused")
	final String message)
	{
		Log.debug("ImagePlus.show(): " + obj);
		final LegacyService legacyService = ImageJ.get(LegacyService.class);
		legacyService.legacyImageChanged(obj);
	}

	/** Appends {@link ImagePlus#hide()}. */
	public static void hide(final ImagePlus obj) {
		Log.debug("ImagePlus.hide(): " + obj);
		LegacyOutputTracker.getOutputImps().remove(obj);
		LegacyOutputTracker.getClosedImps().add(obj);
	}

	/** Appends {@link ImagePlus#close()}. */
	public static void close(final ImagePlus obj) {
		if ((obj != null) && (!LegacyOutputTracker.isBeingClosedbyIJ2(obj))) LegacyOutputTracker
			.getClosedImps().add(obj);
	}
}
