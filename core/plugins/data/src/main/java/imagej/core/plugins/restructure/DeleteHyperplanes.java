//
// DeleteHyperplanes.java
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
import imagej.data.display.DisplayService;
import imagej.data.display.ImageDisplay;
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
 * Deletes hyperplanes of data from an input Dataset along a user specified
 * axis.
 * 
 * @author Barry DeZonia
 */
@Plugin(menu = { @Menu(label = "Image", mnemonic = 'i'),
	@Menu(label = "Stacks", mnemonic = 's'), @Menu(label = "Delete Data...") })
public class DeleteHyperplanes extends DynamicPlugin {

	private static final String NAME_KEY = "Axis to modify";
	private static final String POSITION_KEY = "Deletion position";
	private static final String QUANTITY_KEY = "Deletion quantity";

	private Dataset dataset;
	private String axisToModify;
	private long oneBasedDelPos;
	private long numDeleting;

	private long deletePosition;

	public DeleteHyperplanes() {
		final DisplayService displayService = ImageJ.get(DisplayService.class);
		final ImageDisplay display = displayService.getActiveImageDisplay();
		if (display == null) return;
		dataset = ImageJ.get(DisplayService.class).getActiveDataset(display);

		final DefaultModuleItem<String> name =
			new DefaultModuleItem<String>(this, NAME_KEY, String.class);
		final List<Axis> datasetAxes = Arrays.asList(dataset.getAxes());
		final ArrayList<String> choices = new ArrayList<String>();
		for (final Axis candidateAxis : Axes.values()) {
			if (datasetAxes.contains(candidateAxis)) choices.add(candidateAxis
				.getLabel());
		}
		name.setChoices(choices);
		addInput(name);

		final DefaultModuleItem<Long> pos =
			new DefaultModuleItem<Long>(this, POSITION_KEY, Long.class);
		pos.setMinimumValue(1L);
		// TODO - figure some way to set max val based on chosen Dataset's curr
		// values
		addInput(pos);

		final DefaultModuleItem<Long> quantity =
			new DefaultModuleItem<Long>(this, QUANTITY_KEY, Long.class);
		quantity.setMinimumValue(1L);
		// TODO - figure some way to set max val based on chosen Dataset's curr
		// values
		addInput(quantity);
	}

	/**
	 * Creates new ImgPlus data copying pixel values as needed from an input
	 * Dataset. Assigns the ImgPlus to the input Dataset.
	 */
	@Override
	public void run() {
		final Map<String, Object> inputs = getInputs();
		axisToModify = (String) inputs.get(NAME_KEY);
		oneBasedDelPos = (Long) inputs.get(POSITION_KEY);
		numDeleting = (Long) inputs.get(QUANTITY_KEY);

		deletePosition = oneBasedDelPos - 1;
		final Axis axis = Axes.get(axisToModify);
		if (inputBad(axis)) return;
		final Axis[] axes = dataset.getAxes();
		final long[] newDimensions =
			RestructureUtils.getDimensions(dataset, axis, -numDeleting);
		final ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(dataset, newDimensions, axes);
		int compositeChannelCount =
			compositeStatus(dataset.getCompositeChannelCount(), dstImgPlus, axis);
		fillNewImgPlus(dataset.getImgPlus(), dstImgPlus, axis);
		// TODO - colorTables, metadata, etc.?
		dstImgPlus.setCompositeChannelCount(compositeChannelCount);
		dataset.setImgPlus(dstImgPlus);
	}

	/**
	 * Detects if user specified data is invalid
	 */
	private boolean inputBad(final Axis axis) {
		// axis not determined by dialog
		if (axis == null) return true;

		// setup some working variables
		final int axisIndex = dataset.getAxisIndex(axis);
		final long axisSize = dataset.getImgPlus().dimension(axisIndex);

		// axis not present in Dataset
		if (axisIndex < 0) return true;

		// bad value for startPosition
		if ((deletePosition < 0) || (deletePosition >= axisSize)) return true;

		// bad value for numDeleting
		if (numDeleting <= 0) return true;

		// trying to delete all hyperplanes along axis
		if (numDeleting >= axisSize) return true;

		// if here everything is okay
		return false;
	}

	/**
	 * Fills the newly created ImgPlus with data values from a larger source
	 * image. Copies data from those hyperplanes not being cut.
	 */
	private void fillNewImgPlus(final ImgPlus<? extends RealType<?>> srcImgPlus,
		final ImgPlus<? extends RealType<?>> dstImgPlus, final Axis modifiedAxis)
	{
		final long[] dimensions = dataset.getDims();
		final int axisIndex = dataset.getAxisIndex(modifiedAxis);
		final long axisSize = dimensions[axisIndex];
		final long numBeforeCut = deletePosition;
		long numInCut = numDeleting;
		if (numBeforeCut + numInCut > axisSize) numInCut = axisSize - numBeforeCut;
		final long numAfterCut = axisSize - (numBeforeCut + numInCut);

		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis, 0, 0,
			numBeforeCut);
		RestructureUtils.copyData(srcImgPlus, dstImgPlus, modifiedAxis,
			numBeforeCut + numInCut, numBeforeCut, numAfterCut);
	}

	private int compositeStatus(int compositeCount, ImgPlus<?> output, Axis axis) {
		if (axis == Axes.CHANNEL) {
			int axisIndex = output.getAxisIndex(Axes.CHANNEL);
			long numChannels = output.dimension(axisIndex);
			if (numChannels < compositeCount)
				return (int) numChannels;
		}
		return compositeCount;
	}
}
