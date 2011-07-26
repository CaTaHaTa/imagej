//
// ClampMinDataValues.java
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

import imagej.data.Dataset;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PreviewPlugin;
import net.imglib2.ops.operator.UnaryOperator;
import net.imglib2.ops.operator.unary.Min;

/**
 * Fills an output Dataset by clamping an input Dataset such that no values are
 * less than a user defined constant value.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Math", mnemonic = 'm'),
	@Menu(label = "Min...", weight = 8) })
public class ClampMinDataValues implements ImageJPlugin, PreviewPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	Dataset input;

	@Parameter(label = "Value")
	private double constant;

	@Parameter(label = "Preview")
	private boolean preview;

	private Dataset dataBackup = null;
	
	// -- public interface --

	@Override
	public void run() {
		if (dataBackup != null)
			restoreOriginalData();
		UnaryOperator op = new Min(constant);
		InplaceUnaryTransform transform = new InplaceUnaryTransform(input, op);
		transform.run();
	}

	@Override
	public void preview() {
		if (dataBackup == null)
			saveOriginalData();
		if (!preview) {
			restoreOriginalData();
			return;
		}
		run();
	}

	// -- private helpers --
	
	private void saveOriginalData() {
		dataBackup = input.duplicate();
	}
	
	private void restoreOriginalData() {
		input.copyDataFrom(dataBackup);
	}
}
