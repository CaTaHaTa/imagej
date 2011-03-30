//
// Neighborhood3x3Operation.java
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

package imagej.core.plugins.neigh;

import imagej.core.plugins.imglib.ImglibOutputAlgorithmRunner;
import imagej.data.Dataset;
import imagej.util.Rect;
import mpicbg.imglib.algorithm.OutputAlgorithm;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyFactory;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyMirrorFactory;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;

/**
 * Neighborhood3x3Operation - a helper class for 3x3 neighborhood operation
 * plugins such as SmoothDataValues, SharpenDataValues, and FindEdges. Does
 * the work of communicating with a Neighborhood3x3Watcher.
 * 
 * @author Barry DeZonia
 */
public class Neighborhood3x3Operation {

	// -- instance variables --

	private Dataset input;

	private String errMessage = "No error";

	private Neighborhood3x3Watcher watcher;

	// -- constructor --

	public Neighborhood3x3Operation(Dataset input, Neighborhood3x3Watcher watcher)
	{
		this.input = input;
		this.watcher = watcher;

		if (watcher == null) throw new IllegalArgumentException(
			"neighborhood watcher cannot be null!");
	}

	// -- public interface --

	public Dataset run() {
		OutputAlgorithm algorithm = new Neighborhood3x3Algorithm(input);

		ImglibOutputAlgorithmRunner runner =
			new ImglibOutputAlgorithmRunner(algorithm);

		return runner.run();
	}

	// -- private interface --

	/**
	 * implements any Neighborhood3x3Watcher algorithm using imglib's
	 * OutputAlgorithm. creates an output image as a result.
	 */
	private class Neighborhood3x3Algorithm implements OutputAlgorithm {

		private Image<?> inputImage;
		private Image<?> outputImage;
		private Rect selection;

		/** constructor - an algorithm will reference an input Dataset */
		public Neighborhood3x3Algorithm(Dataset input) {
			inputImage = input.getImage();
			outputImage = null;  // initialize later
			selection = input.getSelection();
		}

		/**
		 * make sure we have an input image and that it's dimensionality is correct
		 */
		@Override
		public boolean checkInput() {
			if (inputImage == null) {
				errMessage = "input image is null";
				return false;
			}

			if (inputImage.getNumDimensions() != 2) {
				errMessage =
					"input image is not 2d but has " + inputImage.getNumDimensions() +
						" dimensions";
				return false;
			}

			outputImage = inputImage.createNewImage();

			return true;
		}

		/**
		 * returns the current value of the error message. Only useful if
		 * checkInput() returns false.
		 */
		@Override
		public String getErrorMessage() {
			return errMessage;
		}

		/**
		 * iterates over the input image updating the Neighborhood3x3Watcher as it
		 * goes. sets the values in the output image to the resulting data values
		 * calculated by the Neighborhood3x3Watcher.
		 */
		@Override
		public boolean process() {
			LocalizableByDimCursor<? extends RealType<?>> outputCursor =
				(LocalizableByDimCursor<? extends RealType<?>>) outputImage
					.createLocalizableByDimCursor();

			int[] origin = outputImage.createPositionArray();
			origin[0] = selection.x;
			origin[1] = selection.y;
			
			int[] span = outputImage.getDimensions();
			if (selection.width > 0)
				span[0] = selection.width;
			if (selection.height > 0)
				span[1] = selection.height;
			
			RegionOfInterestCursor<? extends RealType<?>> neighCursor =
				new RegionOfInterestCursor(outputCursor, origin, span);
			
			OutOfBoundsStrategyFactory factory =
				new OutOfBoundsStrategyMirrorFactory();
			
			LocalizableByDimCursor<? extends RealType<?>> inputCursor =
				(LocalizableByDimCursor<? extends RealType<?>>) inputImage
					.createLocalizableByDimCursor(factory);

			int[] inputPosition = new int[inputCursor.getNumDimensions()];
			int[] localInputPosition = new int[inputCursor.getNumDimensions()];

			// initialize the watcher
			watcher.setup();

			// walk the neighborhood of the output image
			
			while (neighCursor.hasNext()) {
				
				// locate cursor on next location
				RealType<?> outputValue = neighCursor.next();

				// remember the location so that the input image cursor can use
				// it
				neighCursor.getPosition(inputPosition);
				
				// NOTE - inputPosition is in relative coordinates of neigh cursor.
				//  Must translate to input coord space.
				for (int i = 0; i < inputPosition.length; i++)
					inputPosition[i] += origin[i];
				
				// let watcher know we are visiting a new neighborhood
				watcher.initializeNeighborhood(inputPosition);

				// iterate over the 3x3 neighborhood
				for (int dy = -1; dy <= 1; dy++) {

					localInputPosition[1] = inputPosition[1] + dy;
					
					for (int dx = -1; dx <= 1; dx++) {

						localInputPosition[0] = inputPosition[0] + dx;

						// move the input cursor there
						inputCursor.setPosition(localInputPosition);

						// update watcher about the position and value of the
						// subneighborhood location
						double inputValue = inputCursor.getType().getRealDouble();
						
						watcher.visitLocation(dx, dy, inputValue);
					}
				}

				// assign output
				outputValue.setReal(watcher.calcOutputValue());
			}

			inputCursor.close();
			neighCursor.close();
			outputCursor.close();

			return true;
		}

		/** returns the resulting output image of this algorithm */
		@Override
		public Image<?> getResult() {
			return outputImage;
		}
	}
}
