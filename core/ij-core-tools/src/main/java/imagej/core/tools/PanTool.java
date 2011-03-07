//
// PanTool.java
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

package imagej.core.tools;

import imagej.display.Display;
import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.tool.BaseTool;
import imagej.tool.Tool;

import java.awt.event.KeyEvent;

/**
 * TODO
 * 
 * @author Rick Lentz
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Tool(
	name = "Pan",
	description = "Pans the display",
	iconPath = "/tools/pan.png"
)
public class PanTool extends BaseTool {

	private static final float PAN_AMOUNT = 10;

	// @todo: Add customization to set pan amount

	private int lastX, lastY;

	@Override
	public void onKeyDown(KyPressedEvent evt) {
		final Display display = evt.getDisplay();
		switch (evt.getCode()) {
			case KeyEvent.VK_UP:
				display.pan(0, -PAN_AMOUNT);
				break;
			case KeyEvent.VK_DOWN:
				display.pan(0, -PAN_AMOUNT);
				break;
			case KeyEvent.VK_LEFT:
				display.pan(-PAN_AMOUNT, 0);
				break;
			case KeyEvent.VK_RIGHT:
				display.pan(PAN_AMOUNT, 0);
				break;
		}
	}

	@Override
	public void onMouseDown(MsPressedEvent evt) {
		lastX = evt.getX();
		lastY = evt.getY();
	}

	@Override
	public void onMouseDrag(MsDraggedEvent evt)  {
		final Display display = evt.getDisplay();
		display.pan(evt.getX() - lastX, evt.getY() - lastY);
		lastX = evt.getX();
		lastY = evt.getY();
	}

}
