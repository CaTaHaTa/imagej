 //
// CropImageRoi.java
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
package imagej.core.plugins.imglib;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.roi.Overlay;
import imagej.data.roi.RectangleOverlay;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.OverlayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import java.util.List;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.roi.RectangleRegionOfInterest;
import net.imglib2.type.numeric.RealType;

// TODO - minX, minY, maxX, maxY are treated as harvested variables (for simple testing). Should make them passed
//        in parameters to constructor
// TODO - the IJ1 crop plugin can do a lot more than this can. Investigate its abilities and replicate them as needed
/**
 * Creates an output Dataset by cropping an input Dataset in X & Y. Works on
 * images of any dimensionality. X & Y are assumed to be the first two
 * dimensions.
 * 
 * @author Barry DeZonia
 * @author Grant Harris
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "CropRoi", accelerator = "shift control Q")})  // TODO - add correct weight
@SuppressWarnings({"rawtypes", "synthetic-access", "unchecked"})
public class CropImageRoi implements ImageJPlugin {

	// -- instance variables that are Parameters --
	@Parameter
	Dataset input;
	private int minX;
	private int minY;
	private int maxX;
	private int maxY;
	Dataset currDataset;
		
	/**
	 * Runs the crop process and returns the output as a Dataset */
	@Override
	public void run() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		final Display display = displayService.getActiveDisplay();
		if (display == null) {
			return; // headless UI or no open images
		}
		currDataset = (Dataset) display.getActiveView().getDataObject();
		final OverlayService overlayService = ImageJ.get(OverlayService.class);
		List<Overlay> overlays = overlayService.getOverlays(display);

		// overlayService.getOverlays(display);
		final double [] origin = new double[currDataset.getImgPlus().numDimensions()];
		final double [] extent= new double[currDataset.getImgPlus().numDimensions()];
		for (final Overlay overlay : overlays) {
			overlay.getName();
			if (overlay instanceof RectangleOverlay) {
				((RectangleRegionOfInterest) overlay.getRegionOfInterest()).getExtent(extent);
				((RectangleRegionOfInterest) overlay.getRegionOfInterest()).getOrigin(origin);
				minX = (int)origin[0];
				minY = (int)origin[1];
				maxX = (int)extent[0];
				maxY = (int)extent[1];
				break;
			}
		}
		OutputAlgorithm algorithm = new CropAlgorithm();
		ImglibDataTransform runner =new ImglibDataTransform(currDataset, algorithm);
		runner.run();
	}

	// -- private interface --
	/**
	 * CropAlgorithm is responsible for creating the cropped image from the input
	 * Dataset. It is an Imglib OutputAlgorithm.
	 */
	private class CropAlgorithm implements OutputAlgorithm {

		private String errMessage = "No error";
		private Img inputImage;  // TODO - had to make this raw to avoid compiler errors
		private Img<? extends RealType<?>> outputImage;

		/**
		 * Returns false if there is any problem with the input data. returns true
		 * otherwise.
		 */
		@Override
		public boolean checkInput() {
			inputImage = input.getImgPlus();

			long[] newDimensions = new long[inputImage.numDimensions()];

			inputImage.dimensions(newDimensions);
			newDimensions[0] = maxX - minX + 1;
			newDimensions[1] = maxY - minY + 1;

			// TODO - in inputImage not a raw type this won't compile
			outputImage = inputImage.factory().create(newDimensions, inputImage.firstElement());

			return true;
		}

		/**
		 * Returns the current error message */
		@Override
		public String getErrorMessage() {
			return errMessage;
		}

		/**
		 * Runs the cropping process */
		@Override
		public boolean process() {
			RandomAccess<? extends RealType<?>> inputAccessor =
					inputImage.randomAccess();

			Cursor<? extends RealType<?>> outputCursor = outputImage.localizingCursor();

			long[] tmpPosition = new long[outputImage.numDimensions()];

			while (outputCursor.hasNext()) {
				outputCursor.next();

				outputCursor.localize(tmpPosition);

				tmpPosition[0] += minX;
				tmpPosition[1] += minY;

				inputAccessor.setPosition(tmpPosition);

				double value = inputAccessor.get().getRealDouble();

				outputCursor.get().setReal(value);
			}

			return true;
		}

		/**
		 * Returns the resulting output image. not valid before checkInput() and
		 * process() have been called.
		 */
		@Override
		public Img<? extends RealType<?>> getResult() {
			return outputImage;
		}

	}
}