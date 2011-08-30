//
// InvertDataValues.java
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

package imagej.core.plugins.assign;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.DisplayService;
import imagej.display.ImageDisplay;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import net.imglib2.Cursor;
import net.imglib2.ops.operation.unary.real.RealInvert;
import net.imglib2.ops.Real;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 * Fills an output Dataset by applying an inversion to an input Dataset's data
 * values. The inversion is relative to the minimum and maximum data values
 * present in the input Dataset. Note that in IJ1 inversion for 8-bit data is
 * relative to 0 & 255 which is not mirrored here.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Edit", mnemonic = 'e'),
	@Menu(label = "Invert", weight = 30, accelerator = "shift control I") })
public class InvertDataValues implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private ImageDisplay display;

	// -- instance variables --

	private Dataset dataset;
	private double min, max;

	// -- public interface --

	/**
	 * Fills the output image from the input image */
	@Override
	public void run() {
		dataset = ImageJ.get(DisplayService.class).getActiveDataset(display);
		// this is similar to IJ1
		if (dataset.isInteger() && !dataset.isSigned() &&
				dataset.getType().getBitsPerPixel() == 8) {
			min = 0;
			max = 255;
		}
		else
			calcValueRange();
		UnaryOperation<Real,Real> op = new RealInvert(min, max);
		InplaceUnaryTransform transform = new InplaceUnaryTransform(display, op);
		transform.run();
	}

	// -- private interface --

	/**
	 * Finds the smallest and largest data values actually present in the input
	 * image
	 */
	private void calcValueRange() {
		min = Double.MAX_VALUE;
		max = -Double.MAX_VALUE;

		Cursor<? extends RealType<?>> cursor = dataset.getImgPlus().cursor();

		while (cursor.hasNext()) {
			double value = cursor.next().getRealDouble();

			if (value < min) min = value;
			if (value > max) max = value;
		}
	}

}
