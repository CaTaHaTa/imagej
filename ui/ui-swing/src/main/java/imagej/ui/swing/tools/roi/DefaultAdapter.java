//
// DefaultAdapter.java
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

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import javax.imageio.ImageTypeSpecifier;

import imagej.data.roi.Overlay;
import imagej.tool.Tool;
import imagej.util.ColorRGB;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.ImageFigure;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.roi.IterableRegionOfInterest;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.sampler.special.ConstantRandomAccessible;
import net.imglib2.type.logic.BitType;

/**
 * @author Lee Kamentsky
 *
 *The default adapter handles any kind of overlay. It uses the fill color
 *and alpha of the overlay to draw the mask and leaves the rest of the figure transparent. 
 */
@JHotDrawOverlayAdapter(priority = DefaultAdapter.PRIORITY)
public class DefaultAdapter extends AbstractJHotDrawOverlayAdapter<Overlay> {
	static public final int PRIORITY = Integer.MAX_VALUE;
	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		return ((figure == null) || (figure instanceof ImageFigure));
	}

	@Override
	public Overlay createNewOverlay() {
		return null;
	}

	@Override
	public Figure createDefaultFigure() {
		ImageFigure figure = new ImageFigure();
		figure.setTransformable(false);
		figure.set(AttributeKeys.FILL_COLOR, new Color(0,0,0,0));
		return figure;
	}

	/* (non-Javadoc)
	 * @see imagej.ui.swing.tools.roi.AbstractJHotDrawOverlayAdapter#updateFigure(imagej.data.roi.Overlay, org.jhotdraw.draw.Figure)
	 */
	@Override
	public void updateFigure(Overlay overlay, Figure figure) {
		assert figure instanceof ImageFigure;
		ImageFigure imgf = (ImageFigure)figure;
		RegionOfInterest roi = overlay.getRegionOfInterest();
		if ((roi != null) && (roi instanceof IterableRegionOfInterest)) {
			IterableRegionOfInterest iroi = (IterableRegionOfInterest)roi;
			BitType t = new BitType();
			t.set(true);
			IterableInterval<BitType> ii = iroi.getIterableIntervalOverROI(new ConstantRandomAccessible<BitType>(t, roi.numDimensions()));
			Cursor<BitType> c = ii.localizingCursor();
			// TODO At some point, the BinaryMaskOverlay and display have to communicate the plane or transform
			//       that should be applied to make an N-d ROI into a 2-d one.
			ColorRGB color = overlay.getFillColor();
			IndexColorModel cm = new IndexColorModel(1, 2, 
					new byte[] { 0, (byte)color.getRed()},
					new byte[] { 0, (byte)color.getGreen()},
					new byte[] { 0, (byte)color.getBlue() },
					new byte[] { 0, (byte)overlay.getAlpha() });
			int icolor = color.getARGB() | (int)0xFF000000;
			BufferedImage img = new BufferedImage((int)ii.dimension(0), (int)ii.dimension(1), BufferedImage.TYPE_BYTE_INDEXED, cm);
			while(c.hasNext()) {
				c.next();
				img.setRGB(c.getIntPosition(0), c.getIntPosition(1), icolor);
			}
			imgf.setBounds(new Rectangle2D.Double(0,0,ii.dimension(0), ii.dimension(1)));
			imgf.setBufferedImage(img);
		}
	}

}
