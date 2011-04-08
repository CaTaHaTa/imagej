//
// ChangeToFLOAT32.java
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

package imagej.core.plugins.zoom;

import mpicbg.imglib.image.Image;

import imagej.data.Dataset;
import imagej.display.ActiveDisplay;
import imagej.display.Display;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;


// TODO - general zoom issues
//  there is a slight pan error after zooming 100%
//  it is possible to zoom in/out wheel (and click?), resize, and then the
//    following zooms all keep the same zoom ceneter locked no matter what
//  can shrink window, view 100%, have it zoom with image panned offscreen,
//    and can no longer find image

/** zooms the currently displayed image to 100% resolution 
 *  
 * @author bdezonia
 *
 */
@Plugin(menu = {
	@Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Zoom", mnemonic = 'z'),
	@Menu(label = "View 100%", accelerator = "control 5") })
public class Zoom100Percent implements ImageJPlugin {

	@Override
	public void run() {

		Display display = ActiveDisplay.get();
		
		if (display == null)  // headless UI or no open images
			return;
		
		Dataset ds = display.getDataset();
		Image<?> image = ds.getImage();
		int w = image.getDimension(0);
		int h = image.getDimension(1);
		
		display.setZoom(1.0f, w/2.0f, h/2.0f);
	}

}
