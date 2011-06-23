//
// ShadowsEast.java
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

package imagej.core.plugins.convolve;

import java.awt.event.KeyEvent;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayManager;
import imagej.display.event.DisplayDeletedEvent;
import imagej.display.event.key.KyPressedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.event.StatusEvent;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/**
 * Implements IJ1's Shadows East plugin functionality
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = {
	@Menu(label = "Process", mnemonic = 'p'),
	@Menu(label = "Shadows", mnemonic = 's'),
	@Menu(label = "Shadows Demo", weight=200)})
public class ShadowsDemo implements ImageJPlugin {

	// -- instance variables that are Parameters --

	@Parameter
	private Dataset input;
	
	// -- private instance variables --

	private boolean userHasQuit = false;
	private double[][] kernels = new double[][] {
		new double[] { 1, 2, 1, 0, 1, 0, -1, -2, -1 }, // north
		new double[] { 0, 1, 2, -1, 1, 1, -2, -1, 0 }, // northeast
		new double[] { -1, 0, 1, -2, 1, 2, -1, 0, 1 }, // east
		new double[] { -2, -1, 0, -1, 1, 1, 0, 1, 2 }, // southeast
		new double[] { -1, -2, -1, 0, 1, 0, 1, 2, 1 }, // south
		new double[] { 0, -1, -2, 1, 1, -1, 2, 1, 0 }, // southwest
		new double[] { 1, 0, -1, 2, 1, -2, 1, 0, -1 }, // west
		new double[] { 2, 1, 0, 1, 1, -1, 0, -1, -2 }  // northwest
	};
	private Display currDisplay;
	private EventSubscriber<KyPressedEvent> kyPressSubscriber;
	private EventSubscriber<DisplayDeletedEvent> displaySubscriber;
	
	// -- public interface --

	@Override
	public void run() {
		subscribeToEvents();
		Events.publish(new StatusEvent("Press ESC to terminate"));
		currDisplay = ImageJ.get(DisplayManager.class).getActiveDisplay();
		Dataset originalData = input.duplicate();
		userHasQuit = false;
		while (!userHasQuit) {
			for (int i = 0; i < kernels.length; i++) {
				Convolve3x3Operation operation =
					new Convolve3x3Operation(input, kernels[i]);
				operation.run();
				try {	Thread.sleep(100); }
				catch (Exception e) {
					// do nothing
				}
				originalData.copyInto(input);
				if (userHasQuit) break;
			}
		}
		Events.publish(new StatusEvent("Shadows demo terminated"));
		unsubscribeFromEvents();
	}
	
	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		kyPressSubscriber = new EventSubscriber<KyPressedEvent>() {
			@Override
			public void onEvent(KyPressedEvent event) {
				if (event.getCode() == KeyEvent.VK_ESCAPE)
					userHasQuit = true;
			}
		};
		Events.subscribe(KyPressedEvent.class, kyPressSubscriber);
		
		displaySubscriber = new EventSubscriber<DisplayDeletedEvent>() {
			@Override
			public void onEvent(DisplayDeletedEvent event) {
				if (event.getObject() == currDisplay)
					userHasQuit = true;
			}
		};
		Events.subscribe(DisplayDeletedEvent.class, displaySubscriber);
	}

	private void unsubscribeFromEvents() {
		Events.unsubscribe(KyPressedEvent.class, kyPressSubscriber);
		Events.unsubscribe(DisplayDeletedEvent.class, displaySubscriber);
	}
}
