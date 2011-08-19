//
// AddAxis.java
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

package imagej.core.plugins.restructure;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.ImageDisplay;
import imagej.display.DisplayService;
import imagej.ext.module.DefaultModuleItem;
import imagej.ext.plugin.DynamicPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.imglib2.img.Axes;
import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;

/**
 * Adds a new axis to an input Dataset.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Add Axis...") })
public class AddAxis extends DynamicPlugin {

	private static final String NAME_KEY = "Axis to add";
	private static final String SIZE_KEY = "Axis size";

	private Dataset dataset;
	private String axisName;
	private long axisSize;

	public AddAxis() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		final ImageDisplay display = displayService.getActiveImageDisplay();
		if (display == null) return;
		dataset = ImageJ.get(DisplayService.class).getActiveDataset(display);

		final DefaultModuleItem<String> name =
			new DefaultModuleItem<String>(this, NAME_KEY, String.class);
		final List<Axis> datasetAxes = Arrays.asList(dataset.getAxes());
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis candidateAxis : Axes.values()) {
			if (!datasetAxes.contains(candidateAxis)) choices.add(candidateAxis
				.getLabel());
		}
		name.setChoices(choices);
		addInput(name);

		final DefaultModuleItem<Long> size =
			new DefaultModuleItem<Long>(this, SIZE_KEY, Long.class);
		size.setMinimumValue(2L);
		addInput(size);
	}

	// private long axisSize;

	/**
	 * Creates new ImgPlus data with an additonal axis. sets pixels of 1st
	 * hyperplane of new imgPlus to original imgPlus data. Assigns the ImgPlus to
	 * the input Dataset.
	 */
	@Override
	public void run() {
		final Map<String, Object> inputs = getInputs();
		axisName = (String) inputs.get(NAME_KEY);
		axisSize = (Long) inputs.get(SIZE_KEY);
		final Axis axis = Axes.get(axisName);
		if (inputBad(axis)) return;
		final Axis[] newAxes = getNewAxes(dataset, axis);
		final long[] newDimensions = getNewDimensions(dataset, axisSize);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, newAxes);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus);
		dstImgPlus.setCompositeChannelCount(dataset.getCompositeChannelCount());
		// TODO - colorTables, metadata, etc.?
		dataset.setImgPlus(dstImgPlus);
	}

	/**
	 * Detects if user specified data is invalid
	 */
	private boolean inputBad(final Axis axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// axis already present in Dataset
		final int axisIndex = dataset.getAxisIndex(axis);
		if (axisIndex >= 0) return true;

		// axis size invalid
		if (axisSize <= 0) return true;

		return false;
	}

	/**
	 * Creates an Axis[] that consists of all the axes from a Dataset and an
	 * additional axis appended.
	 */
	private Axis[] getNewAxes(final Dataset ds, final Axis axis) {
		final Axis[] origAxes = ds.getAxes();
		final Axis[] newAxes = new Axis[origAxes.length + 1];
		for (int i = 0; i < origAxes.length; i++)
			newAxes[i] = origAxes[i];
		newAxes[newAxes.length - 1] = axis;
		return newAxes;
	}

	/**
	 * Creates a long[] that consists of all the dimensions from a Dataset and an
	 * additional value appended.
	 */
	private long[] getNewDimensions(final Dataset ds,
		final long lastDimensionSize)
	{
		final long[] origDims = ds.getDims();
		final long[] newDims = new long[origDims.length + 1];
		for (int i = 0; i < origDims.length; i++)
			newDims[i] = origDims[i];
		newDims[newDims.length - 1] = lastDimensionSize;
		return newDims;
	}

	/**
	 * Fills the 1st hyperplane in the new Dataset with the entire contents of the
	 * original image
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus)
	{
		final long[] srcOrigin = new long[srcImgPlus.numDimensions()];
		final long[] dstOrigin = new long[dstImgPlus.numDimensions()];

		final long[] srcSpan = new long[srcOrigin.length];
		final long[] dstSpan = new long[dstOrigin.length];

		srcImgPlus.dimensions(srcSpan);
		dstImgPlus.dimensions(dstSpan);
		dstSpan[dstSpan.length - 1] = 1;

		RestructureUtils.copyHyperVolume(srcImgPlus, srcOrigin, srcSpan,
			dstImgPlus, dstOrigin, dstSpan);
	}

}
