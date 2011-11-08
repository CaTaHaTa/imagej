//
// ImgLibDataTransform.java
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

import imagej.data.Dataset;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Runs an ImgLib {@link OutputAlgorithm}, assigning the created image to the
 * input Dataset.
 * 
 * @author Barry DeZonia
 */
public class ImgLibDataTransform implements Runnable {

	// -- instance variables --

	private final Dataset dataset;
	private final OutputAlgorithm<Img<? extends RealType<?>>> algorithm;

	// -- constructor --

	public ImgLibDataTransform(final Dataset dataset,
		final OutputAlgorithm<Img<? extends RealType<?>>> algorithm)
	{
		this.dataset = dataset;
		this.algorithm = algorithm;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		if (!algorithm.checkInput() || !algorithm.process()) {
			throw new IllegalStateException(algorithm.getErrorMessage());
		}

		final ImgPlus<? extends RealType<?>> imgPlus =
			ImgPlus.wrap(algorithm.getResult(), dataset);

		dataset.setImgPlus(imgPlus);
	}

}
