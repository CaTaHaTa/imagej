//
// AddNoiseToDataValues.java
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
import imagej.data.display.DisplayService;
import imagej.data.display.ImageDisplay;
import net.imglib2.Cursor;
import net.imglib2.ops.Real;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.operation.unary.real.RealAddNoise;
import net.imglib2.type.numeric.RealType;

/**
 * Fills an output Dataset by applying random noise to an input Dataset. This
 * class is used by AddDefaultNoiseToDataValues and
 * AddSpecificNoiseToDataValues. They each manipulate setStdDev(). This class
 * can be used to implement simple (1 pixel neighborhood) gaussian noise
 * addition without requiring a plugin.
 * 
 * @author Barry DeZonia
 */
public class AddNoiseToDataValues {

	// -- instance variables --

	private ImageDisplay display;
	
	/**
	 * The stand deviation of the gaussian random value used to create perturbed
	 * values
	 */
	private double rangeStdDev;

	/**
	 * Maximum allowable values - varies by underlying data type. For instance
	 * (0,255) for 8 bit and (0,65535) for 16 bit. Used to make sure that
	 * returned values do not leave the allowable range for the underlying data
	 * type.
	 */
	private double rangeMin, rangeMax;

	// -- constructor --

	/**
	 * Constructor - takes an input Dataset as the baseline data to compute
	 * perturbed values from.
	 */
	public AddNoiseToDataValues(ImageDisplay display) {
		this.display = display;
	}

	// -- public interface --

	/**
	 * Specify the standard deviation of the gaussian range desired. affects the
	 * distance of perturbation of each data value.
	 */
	protected void setStdDev(double stdDev) {
		this.rangeStdDev = stdDev;
	}

	/**
	 * Runs the operation and returns the Dataset that contains the output data */
	public void run() {
		calcTypeMinAndMax();

		UnaryOperation<Real,Real> op = new RealAddNoise(rangeMin, rangeMax, rangeStdDev);

		InplaceUnaryTransform transform = new InplaceUnaryTransform(display, op);

		transform.run();
	}

	// -- private interface --

	/**
	 * Calculates the min and max allowable data range for the image : depends
	 * upon its underlying data type
	 */
	private void calcTypeMinAndMax() {
		Dataset input = ImageJ.get(DisplayService.class).getActiveDataset(display);
		Cursor<? extends RealType<?>> cursor = input.getImgPlus().cursor();
		rangeMin = cursor.get().getMinValue();
		rangeMax = cursor.get().getMaxValue();
	}

}
