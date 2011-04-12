//
// SWTImageDisplay.java
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

package imagej.ui.swt.display;

import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.ImageDisplayWindow;
import imagej.display.NavigableImageCanvas;
import imagej.plugin.Plugin;
import imagej.util.Rect;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
@Plugin(type = Display.class)
public class SWTImageDisplay implements Display {

	private SWTImageFrame imageFrame;

	@Override
	public boolean canDisplay(Dataset dataset) {
		return true;
	}

	@Override
	public void display(final Dataset dataset) {
		imageFrame = new SWTImageFrame();

		// TODO - listen for user input

		// TODO - use DisplayView instead of Dataset directly
		imageFrame.setDataset(dataset);
		imageFrame.open();
	}

	@Override
	public void pan(double x, double y) {
		// TODO
	}

	@Override
	public void panReset() {
		// TODO
	}

	@Override
	public double getPanX() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public double getPanY() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void setZoom(double factor, double centerX, double centerY) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void zoomIn(double centerX, double centerY) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void zoomOut(double centerX, double centerY) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void zoomToFit(Rect rect) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public double getZoomFactor() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public double getZoomCtrX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getZoomCtrY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Dataset getDataset() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void update() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public ImageDisplayWindow getImageDisplayWindow() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public NavigableImageCanvas getImageCanvas() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Object getCurrentPlane() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int[] getCurrentPlanePosition() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void setZoom(double factor) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void zoomIn() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void zoomOut() {
		throw new UnsupportedOperationException("TODO");
	}
}
