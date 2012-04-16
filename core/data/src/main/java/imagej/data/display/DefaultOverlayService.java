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

package imagej.data.display;

import imagej.ImageJ;
import imagej.data.ChannelCollection;
import imagej.data.Data;
import imagej.data.Dataset;
import imagej.data.DrawingTool;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.overlay.Overlay;
import imagej.data.overlay.OverlaySettings;
import imagej.ext.display.Display;
import imagej.ext.display.DisplayService;
import imagej.object.ObjectService;
import imagej.service.AbstractService;
import imagej.service.Service;
import imagej.util.RealRect;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RealRandomAccess;
import net.imglib2.ops.PointSetIterator;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.roi.RegionOfInterest;
import net.imglib2.type.logic.BitType;

/**
 * Default service for working with {@link Overlay}s.
 * 
 * @author Curtis Rueden
 */
@Service
public final class DefaultOverlayService extends AbstractService implements
	OverlayService
{

	private final ObjectService objectService;
	private final DisplayService displayService;
	private final ImageDisplayService imageDisplayService;

	private OverlaySettings defaultSettings;

	// -- Constructors --

	public DefaultOverlayService() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public DefaultOverlayService(final ImageJ context,
		final ObjectService objectService, final DisplayService displayService,
		final ImageDisplayService imageDisplayService)
	{
		super(context);
		this.objectService = objectService;
		this.displayService = displayService;
		this.imageDisplayService = imageDisplayService;
	}

	// -- OverlayService methods --

	@Override
	public ObjectService getObjectService() {
		return objectService;
	}

	/**
	 * Gets a list of all {@link Overlay}s. This method is a shortcut that
	 * delegates to {@link ObjectService}.
	 */
	@Override
	public List<Overlay> getOverlays() {
		return objectService.getObjects(Overlay.class);
	}

	/**
	 * Gets a list of {@link Overlay}s linked to the given {@link ImageDisplay}.
	 */
	@Override
	public List<Overlay> getOverlays(final ImageDisplay display) {
		final ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		if (display != null) {
			for (final DataView view : display) {
				final Data data = view.getData();
				if (!(data instanceof Overlay)) continue;
				final Overlay overlay = (Overlay) data;
				overlays.add(overlay);
			}
		}
		return overlays;
	}

	/** Adds the list of {@link Overlay}s to the given {@link ImageDisplay}. */
	@Override
	public void addOverlays(final ImageDisplay display,
		final List<Overlay> overlays)
	{
		for (final Overlay overlay : overlays) {
			display.display(overlay);
		}
	}

	/**
	 * Removes an {@link Overlay} from the given {@link ImageDisplay}.
	 * 
	 * @param display the {@link ImageDisplay} from which the overlay should be
	 *          removed
	 * @param overlay the {@link Overlay} to remove
	 */
	@Override
	public void removeOverlay(final ImageDisplay display, final Overlay overlay)
	{
		final ArrayList<DataView> overlayViews = new ArrayList<DataView>();
		final List<DataView> views = display;
		for (final DataView view : views) {
			final Data data = view.getData();
			if (data == overlay) overlayViews.add(view);
		}
		for (final DataView view : overlayViews) {
			display.remove(view);
			view.dispose();
		}
		display.update();
	}

	@Override
	public void removeOverlay(final Overlay overlay) {
		List<ImageDisplay> imgDisps = objectService.getObjects(ImageDisplay.class);
		for (ImageDisplay disp : imgDisps)
			removeOverlay(disp, overlay);
	}
	
	/**
	 * Gets the bounding box for the selected overlays in the given
	 * {@link ImageDisplay}.
	 * 
	 * @param display the {@link ImageDisplay} from which the bounding box should
	 *          be computed
	 * @return the smallest bounding box encompassing all selected overlays
	 */
	@Override
	public RealRect getSelectionBounds(final ImageDisplay display) {
		// get total XY extents of the display by checking all datasets
		double width = 0, height = 0;
		for (final DataView view : display) {
			final Data data = view.getData();
			if (!(data instanceof Dataset)) continue;
			final Dataset dataset = (Dataset) data;
			final Extents extents = dataset.getExtents();
			final double w = extents.dimension(0);
			final double h = extents.dimension(1);
			if (w > width) width = w;
			if (h > height) height = h;
		}

		// TODO - Compute bounds over N dimensions, not just two.
		// TODO - Update this method when ticket #660 is done.
		// For example, why don't all Data objects have Extents?

		// determine XY bounding box by checking all overlays
		double xMin = Double.POSITIVE_INFINITY;
		double xMax = Double.NEGATIVE_INFINITY;
		double yMin = Double.POSITIVE_INFINITY;
		double yMax = Double.NEGATIVE_INFINITY;
		for (final DataView view : display) {
			if (!view.isSelected()) continue; // ignore non-selected objects
			final Data data = view.getData();
			if (!(data instanceof Overlay)) continue; // ignore non-overlays

			final Overlay overlay = (Overlay) data;
			final RegionOfInterest roi = overlay.getRegionOfInterest();
			final double min0 = roi.realMin(0);
			final double max0 = roi.realMax(0);
			final double min1 = roi.realMin(1);
			final double max1 = roi.realMax(1);
			if (min0 < xMin) xMin = min0;
			if (max0 > xMax) xMax = max0;
			if (min1 < yMin) yMin = min1;
			if (max1 > yMax) yMax = max1;
		}

		// use entire XY extents if values are out of bounds
		if (xMin < 0 || xMin > width) xMin = 0;
		if (xMax < 0 || xMax > width) xMax = width;
		if (yMin < 0 || yMin > height) yMin = 0;
		if (yMax < 0 || yMax > height) yMax = height;

		// swap reversed bounds
		if (xMin > xMax) {
			final double temp = xMin;
			xMin = xMax;
			xMax = temp;
		}
		if (yMin > yMax) {
			final double temp = yMin;
			yMin = yMax;
			yMax = temp;
		}

		return new RealRect(xMin, yMin, xMax - xMin, yMax - yMin);
	}

	@Override
	public OverlaySettings getDefaultSettings() {
		if (defaultSettings == null) defaultSettings = new OverlaySettings();
		return defaultSettings;
	}

	@Override
	public void drawOverlay(Overlay o, ChannelCollection channelData) {
		outlineOrFill(o, DrawMode.OUTLINE, channelData);
	}

	@Override
	public void fillOverlay(Overlay o, ChannelCollection channelData) {
		outlineOrFill(o, DrawMode.FILL, channelData);
	}

	// -- helpers --

	private enum DrawMode {OUTLINE, FILL}
	

	private void outlineOrFill(Overlay o, DrawMode mode, ChannelCollection chans)
	{
		final ImageDisplay display = getFirstDisplay(o);
		if (display == null) return;
		final Dataset ds = getDataset(display);
		if (ds == null) return;
		final Position position = display.getActiveView().getPlanePosition();
		long[] pp = new long[position.numDimensions()];
		position.localize(pp);
		long[] fullPos = new long[pp.length + 2];
		for (int i = 2; i < fullPos.length; i++)
			fullPos[i] = pp[i-2];
		final DrawingTool tool = new DrawingTool(ds);
		tool.setPosition(fullPos);
		tool.setChannels(chans);
		if (mode == DrawMode.FILL)
			fillOverlay(o, tool);
		else if (mode == DrawMode.OUTLINE)
			outlineOverlay(o, tool);
		else
			throw new IllegalArgumentException("unknown draw mode "+mode);
		ds.update();
	}
	
	private void fillOverlay(Overlay o, DrawingTool tool) {
		RegionOfInterest reg = o.getRegionOfInterest();
		int numDims = reg.numDimensions();
		double[] minD = new double[numDims];
		double[] maxD = new double[numDims];
		reg.realMin(minD);
		reg.realMax(maxD);
		long[] minL = new long[numDims];
		long[] maxL = new long[numDims];
		for (int i = 0; i < numDims; i++) {
			minL[i] = (long) Math.floor(minD[i]);
			maxL[i] = (long) Math.ceil(maxD[i]);
		}
		HyperVolumePointSet pointSet = new HyperVolumePointSet(minL, maxL);
		RealRandomAccess<BitType> accessor = reg.realRandomAccess();
		PointSetIterator iter = pointSet.createIterator();
		long[] pos;
		while (iter.hasNext()) {
			pos = iter.next();
			accessor.setPosition(pos);
			if (accessor.get().get())
				tool.drawPixel(pos[0], pos[1]);
		}
		
	}

	private void outlineOverlay(Overlay o, DrawingTool tool) {
		RegionOfInterest reg = o.getRegionOfInterest();
		int numDims = reg.numDimensions();
		double[] minD = new double[numDims];
		double[] maxD = new double[numDims];
		reg.realMin(minD);
		reg.realMax(maxD);
		long[] minL = new long[numDims];
		long[] maxL = new long[numDims];
		for (int i = 0; i < numDims; i++) {
			minL[i] = (long) Math.floor(minD[i]);
			maxL[i] = (long) Math.ceil(maxD[i]);
		}
		HyperVolumePointSet pointSet = new HyperVolumePointSet(minL, maxL);
		RealRandomAccess<BitType> accessor = reg.realRandomAccess();
		PointSetIterator iter = pointSet.createIterator();
		long[] pos;
		while (iter.hasNext()) {
			pos = iter.next();
			accessor.setPosition(pos);
			if (accessor.get().get())
				if (isBorderPixel(accessor, pos, maxL[0], maxL[1]))
					tool.drawPixel(pos[0], pos[1]);
		}
	}
	
	private boolean isBorderPixel(RealRandomAccess<BitType> accessor, long[] pos,
		long maxX, long maxY)
	{
		if (pos[0] == 0) return true;
		if (pos[0] == maxX) return true;
		if (pos[1] == 0) return true;
		if (pos[1] == maxY) return true;
		accessor.setPosition(pos[0]-1,0);
		if (!accessor.get().get()) return true;
		accessor.setPosition(pos[0]+1,0);
		if (!accessor.get().get()) return true;
		accessor.setPosition(pos[0],0);
		accessor.setPosition(pos[1]-1,1);
		if (!accessor.get().get()) return true;
		accessor.setPosition(pos[1]+1,1);
		if (!accessor.get().get()) return true;
		return false;
	}
	

	private ImageDisplay getFirstDisplay(Overlay o) {
		List<Display<?>> displays = displayService.getDisplays();
		for (Display<?> display : displays) {
			if (display instanceof ImageDisplay) {
				List<Overlay> displayOverlays = getOverlays((ImageDisplay)display);
				if (displayOverlays.contains(o))
					return (ImageDisplay) display;
			}
		}
		return null;
	}
	
	private Dataset getDataset(ImageDisplay display) {
		return imageDisplayService.getActiveDataset(display);
	}
}
