//
// AbstractDatasetView.java
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

package imagej.display;

import imagej.data.Dataset;
import imagej.data.event.DatasetRGBChangedEvent;
import imagej.data.event.DatasetTypeChangedEvent;
import imagej.data.event.DatasetUpdatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;
import imagej.util.Dimensions;
import imagej.util.Index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.CompositeXYProjector;
import net.imglib2.display.RealLUTConverter;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * A view into a {@link Dataset}, for use with a {@link Display}.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
public abstract class AbstractDatasetView extends AbstractDisplayView
	implements DatasetView
{

	private final Dataset dataset;

	/** The dimensional index representing channels, for compositing. */
	private int channelDimIndex;

	/**
	 * Default color tables, one per channel, used when the {@link Dataset} 
	 * doesn't have one for a particular plane.
	 */
	private ArrayList<ColorTable8> defaultLUTs;

	private ARGBScreenImage screenImage;
	private CompositeXYProjector<? extends RealType<?>, ARGBType> projector;
	private final ArrayList<RealLUTConverter<? extends RealType<?>>> converters =
		new ArrayList<RealLUTConverter<? extends RealType<?>>>();
	private ArrayList<EventSubscriber<?>> subscribers;
	private int offsetX, offsetY;

	public AbstractDatasetView(final Display display, final Dataset dataset) {
		super(display, dataset);
		this.dataset = dataset;
		subscribeToEvents();
	}

	// -- DatasetView methods --

	@Override
	public ARGBScreenImage getScreenImage() {
		return screenImage;
	}

	@Override
	public int getCompositeDimIndex() {
		return channelDimIndex;
	}

	@Override
	public int getOffsetX() {
		return offsetX;
	}

	@Override
	public void setOffsetX(final int offsetX) {
		this.offsetX = offsetX;
	}

	@Override
	public int getOffsetY() {
		return offsetY;
	}

	@Override
	public void setOffsetY(final int offsetY) {
		this.offsetY = offsetY;
	}

	@Override
	public ImgPlus<? extends RealType<?>> getImgPlus() {
		return dataset.getImgPlus();
	}

	@Override
	public CompositeXYProjector<? extends RealType<?>, ARGBType> getProjector() {
		return projector;
	}

	@Override
	public List<RealLUTConverter<? extends RealType<?>>> getConverters() {
		return Collections.unmodifiableList(converters);
	}

	@Override
	public void setComposite(final boolean composite) {
		projector.setComposite(composite);
	}

	@Override
	public List<ColorTable8> getColorTables() {
		return Collections.unmodifiableList(defaultLUTs);
	}

	@Override
	public void setColorTable(final ColorTable8 colorTable, final int channel) {
		defaultLUTs.set(channel, colorTable);
		updateLUTs();
	}

	@Override
	public void resetColorTables(final boolean grayscale) {
		final int channelCount = (int) getChannelCount();
		defaultLUTs.clear();
		defaultLUTs.ensureCapacity(channelCount);
		if (grayscale || channelCount == 1) {
			for (int i = 0; i < channelCount; i++) {
				defaultLUTs.add(ColorTables.GRAYS);
			}
		}
		else {
			// use RGBCMY
			for (int i = 0; i < channelCount; i++) {
				final ColorTable8 lut;
				switch (i) {
					case 0:
						lut = ColorTables.RED;
						break;
					case 1:
						lut = ColorTables.GREEN;
						break;
					case 2:
						lut = ColorTables.BLUE;
						break;
					case 3:
						lut = ColorTables.CYAN;
						break;
					case 4:
						lut = ColorTables.MAGENTA;
						break;
					case 5:
						lut = ColorTables.YELLOW;
						break;
					default:
						lut = ColorTables.GRAYS;
				}
				defaultLUTs.add(lut);
			}
		}
		updateLUTs();
	}

	// -- DisplayView methods --

	@Override
	public Dataset getDataObject() {
		return dataset;
	}

	@Override
	public void setPosition(final int value, final int dim) {
		final int currentValue = projector.getIntPosition(dim);
		if (value == currentValue) return; // no change
		projector.setPosition(value, dim);

		// update color tables
		if (dim != channelDimIndex) updateLUTs();

		projector.map();

		super.setPosition(value, dim);
	}

	@Override
	public void rebuild() {
		channelDimIndex = getChannelDimIndex(dataset);

		final ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();

		dims = new long[img.numDimensions()];
		img.dimensions(dims);
		planeDims = Dimensions.getDims3AndGreater(dims);
		position = new long[dims.length];
		planePos = new long[planeDims.length];

		if (defaultLUTs == null || defaultLUTs.size() != dims[channelDimIndex]) {
			defaultLUTs = new ArrayList<ColorTable8>();
			resetColorTables(false);
		}

		final int width = (int) img.dimension(0);
		final int height = (int) img.dimension(1);
		screenImage = new ARGBScreenImage(width, height);

		final int min = 0, max = 255;
		final boolean composite = isComposite(dataset);
		projector = createProjector(min, max, composite);
		projector.map();
	}

	// -- Helper methods --

	private static boolean isComposite(final Dataset dataset) {
		return dataset.getCompositeChannelCount() > 1 || dataset.isRGBMerged();
	}

	private static int getChannelDimIndex(final Dataset dataset) {
		return dataset.getAxisIndex(Axes.CHANNEL);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CompositeXYProjector<? extends RealType<?>, ARGBType>
		createProjector(final int min, final int max, final boolean composite)
	{
		converters.clear();
		final long channelCount = getChannelCount();
		for (int c = 0; c < channelCount; c++) {
			converters.add(new RealLUTConverter(min, max, null));
		}
		final CompositeXYProjector proj =
			new CompositeXYProjector(dataset.getImgPlus(), screenImage, converters,
				channelDimIndex);
		proj.setComposite(composite);
		updateLUTs();
		return proj;
	}

	private void updateLUTs() {
		if (converters.size() == 0) return; // converters not yet initialized
		final long channelCount = getChannelCount();
		for (int c = 0; c < channelCount; c++) {
			final ColorTable8 lut = getCurrentLUT(c);
			converters.get(c).setLUT(lut);
		}
	}

	private ColorTable8 getCurrentLUT(final int cPos) {
		if (channelDimIndex >= 0) planePos[channelDimIndex - 2] = cPos;
		final int no = (int) Index.indexNDto1D(planeDims, planePos);
		final ColorTable8 lut = dataset.getColorTable8(no);
		if (lut != null) return lut; // return dataset-specific LUT
		return defaultLUTs.get(cPos); // return default channel LUT
	}

	private long getChannelCount() {
		return channelDimIndex < 0 ? 1 : dims[channelDimIndex];
	}

	@SuppressWarnings("synthetic-access")
	private void subscribeToEvents() {
		subscribers = new ArrayList<EventSubscriber<?>>();

		final EventSubscriber<DatasetTypeChangedEvent> typeChangeSubscriber =
			new EventSubscriber<DatasetTypeChangedEvent>()
		{
			@Override
			public void onEvent(final DatasetTypeChangedEvent event) {
				if (dataset == event.getObject()) rebuild();
			}
		};
		subscribers.add(typeChangeSubscriber);
		Events.subscribe(DatasetTypeChangedEvent.class, typeChangeSubscriber);

		final EventSubscriber<DatasetRGBChangedEvent> rgbChangeSubscriber =
			new EventSubscriber<DatasetRGBChangedEvent>()
		{
			@Override
			public void onEvent(final DatasetRGBChangedEvent event) {
				if (dataset == event.getObject()) rebuild();
			}
		};
		subscribers.add(rgbChangeSubscriber);
		Events.subscribe(DatasetRGBChangedEvent.class, rgbChangeSubscriber);

		final EventSubscriber<DatasetUpdatedEvent> updateSubscriber =
			new EventSubscriber<DatasetUpdatedEvent>()
		{
			@Override
			public void onEvent(final DatasetUpdatedEvent event) {
				if (event instanceof DatasetTypeChangedEvent) return;
				if (event instanceof DatasetRGBChangedEvent) return;
				if (dataset == event.getObject()) projector.map();
			}
		};
		subscribers.add(updateSubscriber);
		Events.subscribe(DatasetUpdatedEvent.class, updateSubscriber);
	}

}
