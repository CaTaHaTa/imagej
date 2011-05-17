//
// LegacyImageMap.java
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

package imagej.legacy;

import ij.ImagePlus;
import ij.WindowManager;

import imagej.data.Dataset;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * TODO
 *
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyImageMap {

	// -- instance variables --
	
	private Map<ImagePlus, Dataset> imageTable;
	private ImageTranslator imageTranslator;

	// -- public interface --

	public LegacyImageMap() {
		imageTable = new WeakHashMap<ImagePlus, Dataset>();
		imageTranslator = new DefaultImageTranslator();
	}

	public Dataset findDataset(ImagePlus imp) {
		return imageTable.get(imp);
	}

	/**
	 * Ensures that the given legacy image has a corresponding dataset.
	 *
	 * @return the {@link Dataset} object shadowing this legacy image.
	 */
	public Dataset registerLegacyImage(ImagePlus imp) {
		synchronized (imageTable) {
			Dataset dataset = imageTable.get(imp);
			if (dataset == null) {
				// mirror image window to dataset
				dataset = imageTranslator.createDataset(imp);
				imageTable.put(imp, dataset);
			}
			else {  // dataset was already existing
				// do nothing
			}
			return dataset;
		}
	}

	/**
	 * Ensures that the given dataset has a corresponding legacy image.
	 *
	 * @return the {@link ImagePlus} object shadowing this dataset.
	 */
	public ImagePlus registerDataset(Dataset dataset) {
		// find image window
		ImagePlus imp = null;
		synchronized (imageTable) {
			for (final ImagePlus key : imageTable.keySet()) {
				final Dataset value = imageTable.get(key);
				if (dataset == value) {
					imp = key;
					break;
				}
			}
			if (imp == null) {
				// mirror dataset to image window
				imp = imageTranslator.createLegacyImage(dataset);
				imageTable.put(imp, dataset);
			}
		}
		WindowManager.setTempCurrentImage(imp);
		return imp;
	}
	
	public ImageTranslator getTranslator() {
		return imageTranslator;
	}
}
