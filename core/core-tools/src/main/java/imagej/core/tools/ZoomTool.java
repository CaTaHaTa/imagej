//
// ZoomTool.java
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

import imagej.display.Display;
import imagej.display.NavigableImageCanvas;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.mouse.MsButtonEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsWheelEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;
import imagej.util.IntCoords;

/**
 * TODO
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Tool(name = "Zoom", description = "Image Zoom Tool",
	iconPath = "/tools/zoom.png")
public class ZoomTool extends BaseTool {

	// -- ITool methods --

	@Override
	public void onMouseDown(final MsPressedEvent evt) {
		final Display display = evt.getDisplay();
		final IntCoords center = new IntCoords(evt.getX(), evt.getY());
		if (evt.getButton() == MsButtonEvent.LEFT_BUTTON) zoomIn(display, center);
		else zoomOut(display, center);
	}

	@Override
	public void onKeyDown(final KyPressedEvent evt) {
		final Display display = evt.getDisplay();
		final char c = evt.getCharacter();
		if (c == '=' || c == '+') zoomIn(display, null);
		else if (c == '-') zoomOut(display, null);
	}

	@Override
	public void onMouseWheel(MsWheelEvent evt) {
		final Display display = evt.getDisplay();
		final IntCoords center = new IntCoords(evt.getX(), evt.getY());
		if (evt.getWheelRotation() > 0) zoomIn(display, center);
		else zoomOut(display, center);
	}

	// -- Helper methods --

	private void zoomIn(final Display display, final IntCoords zoomCenter) {
		final NavigableImageCanvas canvas = display.getImageCanvas();
		final double currentZoom = canvas.getZoom();
		final double newZoom = currentZoom * canvas.getZoomIncrement();
		if (zoomCenter == null) canvas.setZoom(newZoom);
		else canvas.setZoom(newZoom, zoomCenter);
	}

	private void zoomOut(final Display display, final IntCoords zoomCenter) {
		final NavigableImageCanvas canvas = display.getImageCanvas();
		final double currentZoom = canvas.getZoom();
		final double newZoom = currentZoom / canvas.getZoomIncrement();
		if (zoomCenter == null) canvas.setZoom(newZoom);
		else canvas.setZoom(newZoom, zoomCenter);
	}

}
