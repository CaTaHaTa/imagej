//
// ToolManager.java
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

package imagej.tool;

import imagej.display.event.key.KyPressedEvent;
import imagej.display.event.key.KyReleasedEvent;
import imagej.display.event.mouse.MsClickedEvent;
import imagej.display.event.mouse.MsDraggedEvent;
import imagej.display.event.mouse.MsMovedEvent;
import imagej.display.event.mouse.MsPressedEvent;
import imagej.display.event.mouse.MsReleasedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.tool.event.ToolActivatedEvent;
import imagej.tool.event.ToolDeactivatedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of available tools, including which tool is active,
 * and delegates UI events to the active tool.
 *
 * @author Grant Harris
 * @author Curtis Rueden
 */
public class ToolManager {

	private List<ToolEntry> toolEntries;

	/** Maintain list of subscribers, to avoid garbage collection. */
	private List<EventSubscriber<?>> subscribers;

	private ITool activeTool;

	public ToolManager() {
		toolEntries = ToolUtils.findTools();
		subscribeToEvents();
	}

	public List<ToolEntry> getToolEntries() {
		return toolEntries;
	}

	public ITool getActiveTool() {
		return activeTool;
	}

	public void setActiveTool(final ITool activeTool) {
		if (this.activeTool == activeTool) return; // nothing to do
		if (this.activeTool != null) {
			// deactivate old tool
			this.activeTool.deactivate();
			Events.publish(new ToolDeactivatedEvent(this.activeTool));
		}
		this.activeTool = activeTool;
		// activate new tool
		activeTool.activate();
		Events.publish(new ToolActivatedEvent(activeTool));
	}

	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<KyPressedEvent> kyPressedSubscriber =
			new EventSubscriber<KyPressedEvent>()
		{
			@Override
			public void onEvent(final KyPressedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onKeyDown(event);
			}
		};
		subscribers.add(kyPressedSubscriber);
		Events.subscribe(KyPressedEvent.class, kyPressedSubscriber);

		final EventSubscriber<KyReleasedEvent> kyReleasedSubscriber =
			new EventSubscriber<KyReleasedEvent>()
			{
			@Override
			public void onEvent(final KyReleasedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onKeyUp(event);
			}
		};
		subscribers.add(kyReleasedSubscriber);
		Events.subscribe(KyReleasedEvent.class, kyReleasedSubscriber);

		final EventSubscriber<MsPressedEvent> msPressedSubscriber =
			new EventSubscriber<MsPressedEvent>()
		{
			@Override
			public void onEvent(final MsPressedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onMouseDown(event);
			}
		};
		subscribers.add(msPressedSubscriber);
		Events.subscribe(MsPressedEvent.class, msPressedSubscriber);

		final EventSubscriber<MsReleasedEvent> msReleasedSubscriber =
			new EventSubscriber<MsReleasedEvent>()
		{
			@Override
			public void onEvent(final MsReleasedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onMouseUp(event);
			}
		};
		subscribers.add(msReleasedSubscriber);
		Events.subscribe(MsReleasedEvent.class, msReleasedSubscriber);

		final EventSubscriber<MsClickedEvent> msClickedSubscriber =
			new EventSubscriber<MsClickedEvent>()
		{
			@Override
			public void onEvent(final MsClickedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onMouseClick(event);
			}
		};
		subscribers.add(msClickedSubscriber);
		Events.subscribe(MsClickedEvent.class, msClickedSubscriber);

		final EventSubscriber<MsMovedEvent> msMovedSubscriber =
			new EventSubscriber<MsMovedEvent>()
		{
			@Override
			public void onEvent(final MsMovedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onMouseMove(event);
			}
		};
		subscribers.add(msMovedSubscriber);
		Events.subscribe(MsMovedEvent.class, msMovedSubscriber);

		final EventSubscriber<MsDraggedEvent> msDraggedSubscriber =
			new EventSubscriber<MsDraggedEvent>()
		{
			@Override
			public void onEvent(final MsDraggedEvent event) {
				if(getActiveTool()!=null) getActiveTool().onMouseDrag(event);
			}
		};
		subscribers.add(msDraggedSubscriber);
		Events.subscribe(MsDraggedEvent.class, msDraggedSubscriber);
	}

}
