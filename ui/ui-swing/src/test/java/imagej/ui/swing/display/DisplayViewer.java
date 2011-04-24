//
// DisplayViewer.java
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

package imagej.ui.swing.display;

import imagej.display.view.DatasetView;
import imagej.display.view.DimensionSliderPanel;
import imagej.display.view.DisplayViewBuilder;
import imagej.display.view.ImageDisplayPanel;
import imagej.display.view.ImgDisplayController;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import loci.common.StatusEvent;
import loci.common.StatusListener;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgIOException;
import net.imglib2.io.ImgIOUtils;
import net.imglib2.io.ImgOpener;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * TODO
 *
 * @author Grant Harris
 */
public class DisplayViewer <T extends RealType<T> & NativeType<T>> {

		private static void center(final Window win) {
		final Dimension size = win.getSize();
		final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		final int w = (screen.width - size.width) / 2;
		final int h = (screen.height - size.height) / 2;
		win.setLocation(w, h);
	}

	public void buildDisplay() {
		final String[] urls = {
			//"file:///C:/TestImages/TestImages/MyoblastCells.tif"
			"http://loci.wisc.edu/files/software/data/mitosis-test.zip"
			//"http://loci.wisc.edu/files/software/ome-tiff/z-series.zip"
		};
		final JFrame frame = new JFrame("ImgPanel Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImgDisplayController ctrl = new ImgDisplayController();
		final ImageDisplayPanel imgPanel = new ImageDisplayPanel(ctrl);
		int positionX = 0;
		int positionY = 0;
		//
		for (String url : urls) {
			final ImgPlus<T> img = loadImage(url);
			//img.getAxes(Metadata
			// is it multichannel ??
			DatasetView view = null;
			if (img.getAxisIndex(Axes.CHANNEL) > 0) {
				if (img.dimension(img.getAxisIndex(Axes.CHANNEL)) == 3) {
					// If 3 channels, probably an RGB image
					view = DisplayViewBuilder.createCompositeRGBView(url, img);

				} else {
					view = DisplayViewBuilder.createView(url, img);
					// more than 3 channels
					// default luts up to 7
					// > 7, gray
				}
			} else {
				view = DisplayViewBuilder.createView(url, img);
			}
			if (view != null) {
				view.setPositionX(positionX);
				view.setPositionY(positionY);
				view.setImgCanvas(imgPanel);
				ctrl.addView(view);
				imgPanel.setMaxDimension(view);
				DimensionSliderPanel dsp = new DimensionSliderPanel(view);
				//imgPanel.addToControlPanel(dsp);
				imgPanel.add(dsp);
				positionX += 32;
				positionY += 32;
			}
		}
		imgPanel.resetSize();

		frame.setContentPane(imgPanel);
		frame.pack();
		center(frame);
		frame.setVisible(true);
		
	}

	public static final void main(final String[] args) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@SuppressWarnings("rawtypes")
				@Override
				public void run() {
					new DisplayViewer().buildDisplay();
				}

			});
		}
	}

	/*
	 *  Image loading using BioFormats...
	 * 	- to load a file (on Windows): "file:///C:/TestImages/TestImages/MyoblastCells.tif"
	 * 	- to load from a URL: "http://loci.wisc.edu/files/software/data/mitosis-test.zip"
	 */

		public  static <T extends RealType<T> & NativeType<T>> ImgPlus<T> loadImage(final String url) {
		try {
			System.out.println("Downloading " + url);
			final String id = ImgIOUtils.cacheId(url);
			System.out.println("Opening " + id);
			final ImgOpener imgOpener = new ImgOpener();
			imgOpener.addStatusListener(new StatusListener() {
				@Override
				public void statusUpdated(StatusEvent e) {
					System.out.println(e.getStatusMessage());
				}
			});
			return imgOpener.openImg(id);
		} catch (final IncompatibleTypeException e) {
			e.printStackTrace();
		} catch (final ImgIOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
