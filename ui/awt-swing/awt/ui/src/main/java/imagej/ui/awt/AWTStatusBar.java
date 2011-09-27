//
// AWTStatusBar.java
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

package imagej.ui.awt;

import imagej.ImageJ;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.StatusEvent;
import imagej.ui.StatusBar;

import java.awt.Graphics;
import java.awt.Label;

/**
 * AWT implementation of {@link StatusBar}.
 *
 * @author Curtis Rueden
 */
public class AWTStatusBar extends Label
	implements StatusBar, EventSubscriber<StatusEvent>
{

	private int value;
	private int maximum;

	public AWTStatusBar() {
		ImageJ.get(EventService.class).subscribe(StatusEvent.class, this);
	}

	// -- Component methods --

	@Override
	public void paint(Graphics g) {
		final int width = getWidth();
		final int height = getHeight();
		final int pix = maximum > 0 ? value * width / maximum : 0;
		g.setColor(getForeground());
		g.fillRect(0, 0, pix, height);
		g.setColor(getBackground());
		g.fillRect(pix, 0, width, height);
		super.paint(g);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		setText(message);
	}

	@Override
	public void setProgress(final int val, final int max) {
		value = val;
		maximum = max;
		repaint();
	}

	// -- EventSubscriber methods --

	@Override
	public void onEvent(final StatusEvent event) {
		final String message = event.getStatusMessage();
		final int val = event.getProgressValue();
		final int max = event.getProgressMaximum();
		setStatus(message);
		setProgress(val, max);
	}

}
