//
// AbstractLineROIAdapter.java
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
package imagej.ui.swing.tools.roi;

import imagej.data.roi.AbstractLineOverlay;

import org.jhotdraw.draw.AbstractAttributedFigure;
import org.jhotdraw.draw.AttributeKeys;

/**
 * @author leek
 * The AbstractLineROIAdapter adds mechanisms to populate the line attributes
 * of an AbstractLineROI from an AbstractAttributedFigure and vice-versa
 */
public abstract class AbstractLineOverlayAdapter<F extends AbstractAttributedFigure> extends AbstractJHotDrawOverlayAdapter {

	protected void setFigureLineProperties(AbstractLineOverlay roi, F figure) {
		figure.set(AttributeKeys.STROKE_COLOR, roi.getLineColor());
		figure.set(AttributeKeys.STROKE_WIDTH, roi.getLineWidth());
	}
	
	protected void setROILineProperties(F figure, AbstractLineOverlay roi) {
		roi.setLineColor(figure.get(AttributeKeys.STROKE_COLOR));
		roi.setLineWidth(figure.get(AttributeKeys.STROKE_WIDTH));
	}
}