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

package imagej.core.plugins.assign;

import imagej.data.Dataset;
import imagej.data.Position;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayService;
import imagej.data.overlay.Overlay;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.PreviewPlugin;
import net.imglib2.RandomAccess;
import net.imglib2.ops.PointSetIterator;
import net.imglib2.ops.function.real.PrimitiveDoubleArray;
import net.imglib2.ops.operation.unary.complex.ComplexUnaryOperation;
import net.imglib2.ops.pointset.HyperVolumePointSet;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;

/**
 * Base class for previewable math plugins.
 * 
 * @author Barry DeZonia
 */
public abstract class AbstractAssignPlugin<I extends ComplexType<I>, O extends ComplexType<O>>
	implements ImageJPlugin, PreviewPlugin
{

	// -- instance variables that are Parameters --

	@Parameter(persist = false)
	protected ImageDisplayService displayService;

	@Parameter(persist = false)
	protected OverlayService overlayService;

	@Parameter(persist = false)
	protected ImageDisplay display;

	@Parameter(label = "Preview")
	protected boolean preview;

	@Parameter(label = "Apply to all planes")
	protected boolean allPlanes;

	// -- instance variables --

	private O outType;
	private PrimitiveDoubleArray dataBackup;
	//private InplaceUnaryTransform<I,O> previewTransform;
	//private InplaceUnaryTransform<I,O> finalTransform;
	private PointSetIterator iter;
	private RandomAccess<? extends RealType<?>> accessor;
	private Dataset dataset;
	private Overlay overlay;
	private Position planePos;
	
	// -- public interface --

	public AbstractAssignPlugin(O outType) {
		this.outType = outType;
	}
	
	@Override
	public void run() {
		if (dataset == null) {
			initialize();
		}
		else if (preview) {
			restorePreviewRegion();
		}
		transformFullRegion();
	}

	@Override
	public void preview() {
		if (dataset == null) {
			initialize();
			savePreviewRegion();
		}
		else restorePreviewRegion();
		if (preview) transformPreviewRegion();
	}

	@Override
	public void cancel() {
		if (preview) restorePreviewRegion();
	}

	public ImageDisplay getDisplay() {
		return display;
	}

	public void setDisplay(final ImageDisplay display) {
		this.display = display;
	}

	public boolean getPreview() {
		return preview;
	}

	public void setPreview(final boolean preview) {
		this.preview = preview;
	}

	public abstract ComplexUnaryOperation<O,O> getOperation();

	// -- private helpers --

	private void initialize() {
		dataset = displayService.getActiveDataset(display);
		overlay = overlayService.getActiveOverlay(display);
		DatasetView view = displayService.getActiveDatasetView(display);
		planePos = view.getPlanePosition();

		InplaceUnaryTransform<?,?> xform =
				getPreviewTransform(dataset, overlay);
		accessor = dataset.getImgPlus().randomAccess();
		iter = initIterator(xform.getRegionOrigin(), xform.getRegionSpan());
		dataBackup = new PrimitiveDoubleArray();

		// check dimensions of Dataset
		final long w = xform.getRegionSpan()[0];
		final long h = xform.getRegionSpan()[1];
		if (w * h > Integer.MAX_VALUE)
			throw new IllegalArgumentException(
				"preview region too large to copy into memory");
	}

	private InplaceUnaryTransform<I,O> getPreviewTransform(
				Dataset ds, Overlay ov)
	{
		return new InplaceUnaryTransform<I,O>(
					getOperation(), outType, ds, ov, planePos);
	}
	
	private InplaceUnaryTransform<I,O> getFinalTransform(
			Dataset ds, Overlay ov)
	{
		if (allPlanes)
			return new InplaceUnaryTransform<I,O>(
					getOperation(), outType, ds, ov);
		return getPreviewTransform(ds, ov);
	}

	private PointSetIterator initIterator(long[] planeOrigin, long[] planeSpan) {
		// copy data to a double[]
		final long[] planeOffsets = planeSpan.clone();
		for (int i = 0; i < planeOffsets.length; i++)
			planeOffsets[i]--;
		return new HyperVolumePointSet(
						planeOrigin, new long[planeOrigin.length],
						planeOffsets).createIterator();
	}
	
	// NB
	// We are backing up preview region to doubles. This can cause precision
	// loss for long backed datasets with large values. But using dataset's
	// getPlane()/setPlane() code takes more ram/time than needed. And it has
	// various container limitations.

	private void savePreviewRegion() {
		iter.reset();
		while (iter.hasNext()) {
			accessor.setPosition(iter.next());
			dataBackup.add(accessor.get().getRealDouble());
		}
	}

	private void restorePreviewRegion() {
		int index = 0;
		iter.reset();
		while (iter.hasNext()) {
			accessor.setPosition(iter.next());
			accessor.get().setReal(dataBackup.get(index++));
		}
		dataset.update();
	}

	private void transformFullRegion() {
		getFinalTransform(dataset,overlay).run();
	}

	private void transformPreviewRegion() {
		getPreviewTransform(dataset,overlay).run();
	}

}