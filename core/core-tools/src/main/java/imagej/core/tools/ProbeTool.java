//
// ProbeTool.java
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

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import imagej.data.Dataset;
import imagej.display.MouseCursor;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

/**
 * TODO
 * 
 * @author Rick Lentz
 * @author Grant Harris
 * @author Barry DeZonia
 */
@Tool(name = "Probe", iconPath = "/tools/probe.png",
	description = "Probe Pixel Tool", priority = ProbeTool.PRIORITY)
public class ProbeTool extends BaseTool {

	public static final int PRIORITY = 204;

	// -- ITool methods --

	// TODO - this hatches cursors over and over - very expensive!
	//   Dataset should have thread local cursors for pulling values
	//   out of a Dataset.
	@Override
	public void onMouseMove(final MsMovedEvent evt) {
		final Dataset dataset = evt.getDisplay().getDataset();
		final Image<? extends RealType<?>> image =
			(Image<? extends RealType<?>>) dataset.getImage();
		LocalizableByDimCursor<? extends RealType<?>> cursor =
			(LocalizableByDimCursor<? extends RealType<?>>)
				image.createLocalizableByDimCursor();
		final int x = evt.getX();
		final int y = evt.getY();
		final IntCoords mousePos = new IntCoords(x, y);
		final RealCoords coords =
			evt.getDisplay().getImageCanvas().panelToImageCoords(mousePos);
		if ( ! evt.getDisplay().getImageCanvas().isInImage(mousePos) )
			Events.publish(new StatusEvent(""));
		else {
			final int cx = coords.getIntX();
			final int cy = coords.getIntY();
			// TODO - another performance bottleneck - many array allocations
			final int[] position = cursor.createPositionArray();
			final int[] currPlanePos = evt.getDisplay().getCurrentPlanePosition();
			// FIXME - assumes x & y axes are first two
			position[0] = x;
			position[1] = y;
			for (int i = 2; i < position.length; i++)
				position[i] = currPlanePos[i-2];
			cursor.setPosition(position);
			double doubleValue = cursor.getType().getRealDouble();
			String s = "";
			if (dataset.isFloat())
				s = "" + doubleValue;
			else
				s = "" + ((long) doubleValue);
			Events.publish(
				new StatusEvent("x=" + cx + ", y=" + cy + ", value=" + s));
		}
		cursor.close();
	}

	@Override
	public MouseCursor getCursor() {
		return MouseCursor.CROSSHAIR;
	}

}
