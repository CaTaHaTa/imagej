//
// SaltAndPepper.java
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
import imagej.data.Extents;
import imagej.data.Position;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.OverlayService;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.util.RealRect;

import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Implements the same functionality as IJ1's Salt and Pepper plugin. Assigns
 * random pixels to 255 or 0. 0 and 255 assignments are each evenly balanced at
 * 2.5% of the image. Currently only works on 2d images.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Noise", mnemonic = 'n'),
	@Menu(label = "Salt and Pepper", weight = 3) })
public class SaltAndPepper implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Display display;

	// -- other instance variables --

	private Dataset input;
	private RealRect selection;
	private Img<? extends RealType<?>> inputImage;
	private RandomAccess<? extends RealType<?>> accessor;
	private long[] position;
	
	// -- public interface --

	@Override
	public void run() {
		initializeMembers();
		checkInput();
		setupWorkingData();
		assignPixels();
		cleanup();
		input.update();
	}

	// -- private interface --

	private void initializeMembers() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		final OverlayService overlayService = ImageJ.get(OverlayService.class);
		input = displayService.getActiveDataset(display);
		selection = overlayService.getSelectionBounds(display);
	}
	
	private void checkInput() {
		input = ImageJ.get(DisplayService.class).getActiveDataset(display);
		if (input == null)
			throw new IllegalArgumentException("input Dataset is null");
		
		if (input.getImgPlus() == null)
			throw new IllegalArgumentException("input Image is null");
	}

	private void setupWorkingData() {
		inputImage = input.getImgPlus();
		position = new long[inputImage.numDimensions()];
		accessor = inputImage.randomAccess();
	}
	
	private void assignPixels() {
		Random rng = new Random();

		rng.setSeed(System.currentTimeMillis());

		long[] planeDims = new long[inputImage.numDimensions() - 2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = inputImage.dimension(i+2);
		Extents extents = new Extents(planeDims);
		Position planePos = extents.createPosition();
		if (planeDims.length == 0) { // 2d only
			assignPixelsInXYPlane(planePos, rng);
		}
		else { // 3 or more dimsensions
			while (planePos.hasNext()) {
				planePos.fwd();
				assignPixelsInXYPlane(planePos, rng);
			}
		}
	}

	private void cleanup() {
		// nothing to do
	}

	private void assignPixelsInXYPlane(Position planePos, Random rng) {
		
		// set non-XY coordinate values once
		for (int i = 2; i < position.length; i++)
			position[i] = planePos.getLongPosition(i-2);

		long ox = (long) selection.x;
		long oy = (long) selection.y;
		long w = (long) selection.width;
		long h = (long) selection.height;
		
		if (w <= 0) w = inputImage.dimension(0);
		if (h <= 0) h = inputImage.dimension(1);

		double percentToChange = 0.05;
		long numPixels = (long) (percentToChange * w * h);

		for (long p = 0; p < numPixels / 2; p++) {
			long randomX, randomY;

			randomX = ox + nextLong(rng,w);
			randomY = oy + nextLong(rng,h);
			setPixel(randomX, randomY, 255);

			randomX = ox + nextLong(rng,w);
			randomY = oy + nextLong(rng,h);
			setPixel(randomX, randomY, 0);
		}
	}
	
	private long nextLong(Random rng, long bound) {
		double val = rng.nextDouble();
		return (long) (val * bound);
	}
	
	/**
	 * Sets a value at a specific (x,y) location in the image to a given value
	 */
	private void setPixel(long x, long y, double value) {
		position[0] = x;
		position[1] = y;

		accessor.setPosition(position);

		accessor.get().setReal(value);
	}
}
