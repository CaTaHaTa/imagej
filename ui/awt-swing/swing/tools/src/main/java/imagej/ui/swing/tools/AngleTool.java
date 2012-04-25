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

package imagej.ui.swing.tools;

import imagej.ImageJ;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.AngleOverlay;
import imagej.data.overlay.Overlay;
import imagej.ext.display.event.input.MsButtonEvent;
import imagej.ext.display.event.input.MsPressedEvent;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.AbstractTool;
import imagej.ext.tool.Tool;
import imagej.ui.swing.tools.overlay.LineAdapter;
import imagej.util.IntCoords;
import imagej.util.RealCoords;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.imglib2.RealPoint;

/**
 * TODO
 * 
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Angle", description = "Angle tool",
	iconPath = "/icons/tools/angle.png", priority = AngleTool.PRIORITY,
	enabled=false)
public class AngleTool extends AbstractTool {

	public static final int PRIORITY = LineAdapter.PRIORITY - 1;
	
	private List<RealPoint> coords = new LinkedList<RealPoint>();

	@Override
	public void onMouseDown(MsPressedEvent evt) {
		if (evt.getDisplay() == null) return;
		if (evt.getButton() != MsButtonEvent.LEFT_BUTTON) return;
		final ImageDisplayService imgService = evt.getContext().getService(ImageDisplayService.class);
		ImageDisplay imgDisp = imgService.getActiveImageDisplay();
		IntCoords panelPoint = new IntCoords(evt.getX(), evt.getY());
		RealCoords modelPoint = imgDisp.getCanvas().panelToImageCoords(panelPoint);
		RealPoint realPoint = new RealPoint(modelPoint.x, modelPoint.y);
		handlePoint(evt.getContext(), imgDisp, realPoint);
		evt.consume();
	}

	private void handlePoint(ImageJ context, ImageDisplay display, RealPoint point) {
		OverlayService os = context.getService(OverlayService.class);
		coords.add(point);
		while (coords.size()/3 >= 1) {
			AngleOverlay angleOverlay = new AngleOverlay(context);
			angleOverlay.setEndPoint1(coords.remove(0)); 
			angleOverlay.setCenterPoint(coords.remove(0)); 
			angleOverlay.setEndPoint2(coords.remove(0));
			// TMP HACK
			Overlay overlay = angleOverlay;
			os.addOverlays(display, Arrays.asList(overlay));
			System.out.println("Angle overlay added");
			// END TMP HACK
		}
	}
}
