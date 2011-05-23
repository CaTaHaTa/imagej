//
// OverlayProperties.java
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

package imagej.core.plugins.roi;

import imagej.ImageJ;
import imagej.data.DataObject;
import imagej.data.roi.Overlay;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.DisplayView;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;
import imagej.plugin.PreviewPlugin;
import imagej.util.ColorRGB;

import java.util.ArrayList;
import java.util.List;

/**
 * A plugin to change the properties (e.g., line color, line width) of the
 * selected overlays.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Overlay", mnemonic = 'o'),
	@Menu(label = "Properties...", mnemonic = 'p') })
public class OverlayProperties implements ImageJPlugin, PreviewPlugin {

	@Parameter(label = "Line color", persist = false)
	private ColorRGB lineColor;

	@Parameter(label = "Line width", persist = false, min = "0.1")
	private double lineWidth;

	@Parameter(label = "Fill color", persist = false)
	private ColorRGB fillColor;

	@Parameter(label = "Alpha", persist = false, min = "0", max = "255",
		description = "The opacity or alpha of the interior of the overlay "
			+ "(0=transparent, 255=opaque)")
	private int alpha;

	public OverlayProperties() {
		// set default values to match the first selected overlay
		final List<Overlay> selected = getSelectedOverlays();
		if (selected.size() > 0) {
			final Overlay overlay = selected.get(0);
			lineColor = overlay.getLineColor();
			lineWidth = overlay.getLineWidth();
			fillColor = overlay.getFillColor();
			alpha = overlay.getAlpha();
		}
	}

	@Override
	public void run() {
		// change properties of all selected overlays
		final List<Overlay> selected = getSelectedOverlays();
		for (final Overlay overlay : selected) {
			overlay.setLineColor(lineColor);
			overlay.setLineWidth(lineWidth);
			overlay.setFillColor(fillColor);
			overlay.setAlpha(alpha);
			overlay.update();
		}
	}

	@Override
	public void preview() {
		run();
	}

	public ColorRGB getLineColor() {
		return lineColor;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public ColorRGB getFillColor() {
		return fillColor;
	}

	public int getAlpha() {
		return alpha;
	}

	private List<Overlay> getSelectedOverlays() {
		final ArrayList<Overlay> result = new ArrayList<Overlay>();

		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);
		final Display display = displayManager.getActiveDisplay();
		for (final DisplayView view : display.getViews()) {
			if (!view.isSelected()) continue;
			final DataObject dataObject = view.getDataObject();
			if (!(dataObject instanceof Overlay)) continue;
			final Overlay overlay = (Overlay) dataObject;
			result.add(overlay);
		}
		return result;
	}

}
