//
// RectangleAdapter.java
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

package imagej.ui.swing.tools.roi;

import imagej.data.display.DisplayView;
import imagej.data.roi.Overlay;
import imagej.data.roi.RectangleOverlay;
import imagej.tool.Tool;
import imagej.ui.swing.roi.JHotDrawOverlayAdapter;
import imagej.ui.swing.roi.SelectionTool;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import net.imglib2.roi.RectangleRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.RectangleFigure;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 */
@Tool(name = "Rectangle", description = "Rectangular overlays",
	iconPath = "/icons/tools/rectangle.png",
	priority = RectangleAdapter.PRIORITY, enabled = true)
@JHotDrawOverlayAdapter(priority = RectangleAdapter.PRIORITY)
public class RectangleAdapter extends
	AbstractJHotDrawOverlayAdapter<RectangleOverlay>
{

	public static final int PRIORITY = SelectionTool.PRIORITY + 1;

	protected static RectangleOverlay downcastOverlay(final Overlay roi) {
		assert (roi instanceof RectangleOverlay);
		return (RectangleOverlay) roi;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if ((figure != null) && (!(figure instanceof RectangleFigure))) return false;
		if (overlay instanceof RectangleOverlay) return true;
		return false;
	}

	@Override
	public Overlay createNewOverlay() {
		return new RectangleOverlay();
	}

	@Override
	public Figure createDefaultFigure() {
		final RectangleFigure figure = new RectangleFigure();
		figure.set(AttributeKeys.FILL_COLOR, new Color(255, 255, 255, 0));
		figure.set(AttributeKeys.STROKE_COLOR, defaultStrokeColor);
		return figure;
	}

	@Override
	public void updateFigure(final Overlay overlay, final Figure f,
		final DisplayView view)
	{
		super.updateFigure(overlay, f, view);
		final RectangleOverlay rectangleOverlay = downcastOverlay(overlay);
		final RectangleRegionOfInterest roi =
			rectangleOverlay.getRegionOfInterest();
		final double x0 = roi.getOrigin(0);
		final double w = roi.getExtent(0);
		final double y0 = roi.getOrigin(1);
		final double h = roi.getExtent(1);
		final Point2D.Double anchor = new Point2D.Double(x0, y0);
		final Point2D.Double lead = new Point2D.Double(x0 + w, y0 + h);
		f.setBounds(anchor, lead);
	}

	@Override
	public void updateOverlay(final Figure figure, final Overlay overlay) {
		super.updateOverlay(figure, overlay);
		final RectangleOverlay rOverlay = downcastOverlay(overlay);
		final RectangleRegionOfInterest roi = rOverlay.getRegionOfInterest();
		final Rectangle2D.Double bounds = figure.getBounds();
		roi.setOrigin(bounds.getMinX(), 0);
		roi.setOrigin(bounds.getMinY(), 1);
		roi.setExtent(bounds.getWidth(), 0);
		roi.setExtent(bounds.getHeight(), 1);
	}

}
