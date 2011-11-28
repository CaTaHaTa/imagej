//
// FGTool.java
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

package imagej.core.tools;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DataView;
import imagej.data.display.ImageCanvas;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.ext.display.Display;
import imagej.ext.display.event.input.MsClickedEvent;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.util.ColorRGB;
import imagej.util.IntCoords;
import imagej.util.RealCoords;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;

// TODO - this code adapted from PixelProbe. Update both this and that to
// share some code.

/**
 * Sets foreground value when tool is active and mouse clicked over an image.
 * 
 * @author Barry DeZonia
 */
@Tool(name = "FGTool",
	description = "Drawing value tool (sets foreground color/value)",
	iconPath = "/icons/tools/fgtool.png",
	priority = FGTool.PRIORITY)
public class FGTool extends AbstractTool {

	// -- constants --
	
	public static final int PRIORITY = 299;

	// -- instance variables --
	
	private ColorRGB fgColor = new ColorRGB(0,0,0);
	private double fgValue = 0;

	// -- ValueTool methods --
	
	public ColorRGB getFgColor() {
		return new ColorRGB(
			fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
	}

	public double getFgValue() {
		return fgValue;
	}

	// -- ITool methods --

	@Override
	public void onMouseClick(final MsClickedEvent evt) {

    final ImageJ context = evt.getContext();
    final ImageDisplayService imageDisplayService =
    		context.getService(ImageDisplayService.class);
    final EventService eventService = context.getService(EventService.class);

		final Display<?> display = evt.getDisplay();
		if (!(display instanceof ImageDisplay)) return;
		final ImageDisplay imageDisplay = (ImageDisplay) display;

		final ImageCanvas canvas = imageDisplay.getCanvas();
		final IntCoords mousePos = new IntCoords(evt.getX(), evt.getY());
		if (!canvas.isInImage(mousePos)) {
			eventService.publish(new StatusEvent(null));
			return;
		}

		// mouse is over image

		// TODO - update tool to probe more than just the active view
		final DataView activeView = imageDisplay.getActiveView();
		final Dataset dataset = imageDisplayService.getActiveDataset(imageDisplay);

		final RealCoords coords = canvas.panelToImageCoords(mousePos);
		final int cx = coords.getIntX();
		final int cy = coords.getIntY();

		final Position planePos = activeView.getPlanePosition();

		final Img<? extends RealType<?>> image = dataset.getImgPlus();
		final RandomAccess<? extends RealType<?>> randomAccess =
			image.randomAccess();
		final int xAxis = dataset.getAxisIndex(Axes.X);
		final int yAxis = dataset.getAxisIndex(Axes.Y);

		setPosition(randomAccess, cx, cy, planePos, xAxis, yAxis);

		// color dataset?
		if (dataset.isRGBMerged()) {
			// NB - don't set fgValue in any way
			fgColor = getColor(dataset, randomAccess);
		}
		else {  // gray dataset
			fgValue = randomAccess.get().getRealDouble();
			final double min = randomAccess.get().getMinValue();
			final double max = randomAccess.get().getMaxValue();
			final double percent = (fgValue - min) / (max - min);
			final int byteVal = (int) Math.round(255*percent);
			fgColor = new ColorRGB(byteVal, byteVal, byteVal);
		}
		String message = String.format("(%d,%d,%d)",
			fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue());
		eventService.publish(new StatusEvent(message));
	}

	// -- private interface --

	private void setPosition(
		final RandomAccess<? extends RealType<?>> randomAccess, final int cx,
		final int cy, final Position planePos, final int xAxis, final int yAxis)
	{
		int i = 0;
		for (int d = 0; d < randomAccess.numDimensions(); d++) {
			if (d == xAxis) randomAccess.setPosition(cx, xAxis);
			else if (d == yAxis) randomAccess.setPosition(cy, yAxis);
			else randomAccess.setPosition(planePos.getLongPosition(i++), d);
		}
	}

	private ColorRGB getColor(Dataset dataset,
		RandomAccess<? extends RealType<?>> access)
	{
		int channelAxis = dataset.getAxisIndex(Axes.CHANNEL);
		access.setPosition(0, channelAxis);
		int r = (int) access.get().getRealDouble();
		access.setPosition(1, channelAxis);
		int g = (int) access.get().getRealDouble();
		access.setPosition(2, channelAxis);
		int b = (int) access.get().getRealDouble();
		return new ColorRGB(r,g,b);
	}
}