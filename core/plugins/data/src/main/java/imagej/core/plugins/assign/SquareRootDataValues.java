//
// SquareRootDataValues.java
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

import imagej.data.display.ImageDisplay;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import net.imglib2.ops.Real;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.operation.unary.real.RealSqrt;

/**
 * Fills an output Dataset by taking the square root of the data values of an
 * input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Math", mnemonic = 'm'),
	@Menu(label = "Square Root", weight = 16) })
public class SquareRootDataValues implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter(required = true, persist = false)
	private ImageDisplay display;

	// -- public interface --

	@Override
	public void run() {
		UnaryOperation<Real,Real> op = new RealSqrt();
		InplaceUnaryTransform transform = new InplaceUnaryTransform(display, op);
		transform.run();
	}
	
	public ImageDisplay getDisplay() {
		return display;
	}
	
	public void setDisplay(ImageDisplay display) {
		this.display = display;
	}

}
