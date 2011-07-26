//
// OverlayView.java
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

package imagej.display;

import imagej.ImageJ;
import imagej.data.roi.Overlay;
import net.imglib2.img.Axis;

/**
 * A view into an {@link Overlay}, for use with a {@link Display}.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractOverlayView extends AbstractDisplayView {

	private final Overlay overlay;

	public AbstractOverlayView(final Display display, final Overlay overlay) {
		super(display, overlay);
		this.overlay = overlay;
		long[] dims = ImageJ.get(DisplayService.class).getActiveDataset(display).getDims();
		setDimensions(dims);
	}
	
	// -- DisplayView methods --

	/* (non-Javadoc)
	 * @see imagej.display.AbstractDisplayView#getDataObject()
	 */
	@Override
	public Overlay getDataObject() {
		return overlay;
	}
	
	/* (non-Javadoc)
	 * @see imagej.display.AbstractDisplayView#isVisible()
	 */
	@Override
	public boolean isVisible() {
		for (int i=2; i<overlay.numDimensions(); i++) {
			Axis axis = overlay.axis(i);
			final Long pos = overlay.getPosition(axis);
			if ((pos != null) && ! pos.equals(getPlanePosition().getLongPosition(i-2))) {
				return false;
			}
		}
		return true;
	}

}
