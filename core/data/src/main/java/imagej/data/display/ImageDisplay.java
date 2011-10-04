//
// ImageDisplay.java
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

package imagej.data.display;

import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.LabeledSpace;
import imagej.data.roi.Overlay;
import imagej.ext.display.Display;

import java.util.List;

import net.imglib2.img.Axis;

/**
 * A image display is a special kind of {@link Display} for visualizing
 * {@link Data} objects.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public interface ImageDisplay extends Display<DataView>, LabeledSpace {

	/** Gets the view currently designated as active. */
	DataView getActiveView();

	/** Gets the axis currently designated as active. */
	Axis getActiveAxis();

	/** Sets the axis currently designated as active. */
	void setActiveAxis(Axis axis);

	/** Gets the image canvas upon which this display's output is painted. */
	ImageCanvas getImageCanvas();

	/** Tests whether this display contains the given data object (via a view). */
	boolean containsData(Data data);
	
	// CTR TODO - move getAxes method into LabeledSpace.
	
	List<Axis> getAxes();
	
	// CTR TODO - eliminate the methods below.
	
	@Deprecated
	void display(Dataset dataset);

	@Deprecated
	void display(Overlay overlay);

	/** Removes a view from this display. */
	@Deprecated
	void removeView(DataView view);

	/** Forces the display window to redo its layout. */
	void redoWindowLayout();

}
