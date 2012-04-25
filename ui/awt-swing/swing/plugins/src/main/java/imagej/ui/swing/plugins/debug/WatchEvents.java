/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.ui.swing.plugins.debug;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.event.ImageJEvent;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Listens for events, displaying results in a text window.
 * 
 * @author Curtis Rueden
 */
@Plugin(menuPath = "Plugins>Debug>Watch Events")
public class WatchEvents implements ImageJPlugin {

	// -- Parameters --

	@Parameter(persist = false)
	private EventService eventService;

	// -- Fields --

	private WatchEventsFrame watchEventsFrame;

	private List<EventSubscriber<?>> subscribers;

	// -- Runnable methods --

	@Override
	public void run() {
		watchEventsFrame = new WatchEventsFrame();
		subscribers = eventService.subscribe(this);
		watchEventsFrame.setVisible(true);
		watchEventsFrame.addWindowListener(new WindowAdapter() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void windowClosing(WindowEvent e) {
				eventService.unsubscribe(subscribers);
			}
		});
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final ImageJEvent evt) {
		watchEventsFrame.registerEventClass(evt.getClass());
		showEvent(evt);
	}

	// -- Helper methods --

	private void showEvent(final ImageJEvent evt) {
		final String eventClass = evt.getClass().getSimpleName();
		emitMessage("[" + timeStamp() + "] " + eventClass + evt);
	}

	private String timeStamp() {
		final SimpleDateFormat formatter =
			new SimpleDateFormat("hh:mm:ss.SS", Locale.getDefault());
		final Date currentDate = new Date();
		final String dateStr = formatter.format(currentDate);
		return dateStr;
	}

	private void emitMessage(final String msg) {
		if (watchEventsFrame == null) return;
		watchEventsFrame.append(msg + "\n");
	}

}