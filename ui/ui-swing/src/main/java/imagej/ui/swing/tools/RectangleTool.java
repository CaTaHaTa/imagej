//
// RectangleTool.java
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

package imagej.ui.swing.tools;

import imagej.ImageJ;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.tool.BaseTool;
import imagej.tool.Tool;
import imagej.ui.swing.display.JHotDrawImageCanvas;
import imagej.ui.swing.display.SwingImageDisplay;

import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.draw.tool.CreationTool;

/**
 * TODO
 * 
 * @author Curtis Rueden
 */
@Tool(name = "Rectangle", iconPath = "/tools/rectangle.png",
	priority = RectangleTool.PRIORITY, enabled = true)
public class RectangleTool extends BaseTool {

	public static final int PRIORITY = 100;

	@Override
	public void activate() {
		final DisplayManager displayManager = ImageJ.get(DisplayManager.class);		
		final Display display = displayManager.getActiveDisplay();
		final SwingImageDisplay swingDisplay = (SwingImageDisplay) display; // TEMP -- horrible
		// FIXME - Change architecture to call tool.activate() every time a display is set to active.
		final JHotDrawImageCanvas canvas = swingDisplay.getImageCanvas();
		canvas.getDrawingEditor().setTool(new CreationTool(new RectangleFigure()));
	}

	@Override
	public void deactivate() {
//		canvas.getDrawingEditor().setTool(dummyTool);
	}

}
