//
//
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

package imagej.legacy.translate;

import java.util.HashMap;
import java.util.Map;

import net.imglib2.img.Axes;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import ij.ImagePlus;
import ij.ImageStack;
import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.legacy.translate.LegacyUtils;


/**
 * Provides methods for synchronizing data between an ImageDisplay and an
 * ImagePlus.
 * 
 * @author Barry DeZonia
 * 
 */
public class Harmonizer {

	// -- instance variables --

	private ImageTranslator imageTranslator;
	private final OverlayTranslator overlayTranslator = new OverlayTranslator();
	private final Map<ImagePlus, Integer> bitDepthMap = new HashMap<ImagePlus, Integer>();
	
	private GrayPixelHarmonizer grayPixelHarmonizer = new GrayPixelHarmonizer();
	private ColorPixelHarmonizer colorPixelHarmonizer = new ColorPixelHarmonizer();
	private ColorTableHarmonizer colorTableHarmonizer = new ColorTableHarmonizer();
	private MetadataHarmonizer metadataHarmonizer = new MetadataHarmonizer();
	private CompositeHarmonizer compositeHarmonizer = new CompositeHarmonizer();
	private PlaneHarmonizer planeHarmonizer = new PlaneHarmonizer();
	
	// -- constructor --

	public Harmonizer(ImageTranslator trans) {
		imageTranslator = trans; 
	}
	
	// -- public interface --

	/**
	 * Changes the data within an {@link ImagePlus} to match data in a
	 * {@link ImageDisplay}. Assumes Dataset has planar primitive access in an IJ1
	 * compatible format.
	 */
	public void
		updateLegacyImage(final ImageDisplay display, final ImagePlus imp)
	{
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final Dataset ds = imageDisplayService.getActiveDataset(display);
		if (!imagePlusIsNearestType(ds, imp)) {
			rebuildImagePlusData(display, imp);
		}
		else {
			if ((dimensionsIncompatible(ds, imp)) || (imp.getStack().getSize() == 0))
			{ // NB - in IJ1 stack size can be zero for single slice image!
				rebuildImagePlusData(display, imp);
			}
			else if (imp.getType() == ImagePlus.COLOR_RGB) {
				colorPixelHarmonizer.updateLegacyImage(ds, imp);
			}
			else if (LegacyUtils.datasetIsIJ1Compatible(ds)) {
				planeHarmonizer.updateLegacyImage(ds, imp);
			}
			else grayPixelHarmonizer.updateLegacyImage(ds, imp);
		}
		metadataHarmonizer.updateLegacyImage(ds, imp);
		overlayTranslator.setImagePlusOverlays(display, imp);
		colorTableHarmonizer.updateLegacyImage(display, imp);
	}

	/**
	 * Changes the data within a {@link ImageDisplay} to match data in an
	 * {@link ImagePlus}. Assumes the given ImagePlus is not a degenerate
	 * set of data (an empty stack).
	 */
	public void updateDisplay(final ImageDisplay display, final ImagePlus imp) {
		
		// NB - if ImagePlus is degenerate the following code can fail. This is
		// because imglib cannot represent an empty data container. So we catch
		// the issue here:
		
		if (imp.getStack().getSize() == 0)
			throw new IllegalArgumentException(
				"cannot update a display with an ImagePlus that has an empty stack");
			
		final ImageDisplayService imageDisplayService =
			ImageJ.get(ImageDisplayService.class);
		final Dataset ds = imageDisplayService.getActiveDataset(display);

		// did type of ImagePlus change?
		if (imp.getBitDepth() != bitDepthMap.get(imp)) {
			final ImageDisplay tmp = imageTranslator.createDisplay(imp, ds.getAxes());
			final Dataset dsTmp = imageDisplayService.getActiveDataset(tmp);
			ds.setImgPlus(dsTmp.getImgPlus());
			ds.setRGBMerged(dsTmp.isRGBMerged());
		}
		else { // ImagePlus type unchanged
			if (dimensionsIncompatible(ds, imp)) {
				reshapeDataset(ds, imp);
			}
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				colorPixelHarmonizer.updateDataset(ds, imp);
			}
			else if (LegacyUtils.datasetIsIJ1Compatible(ds)) {
				planeHarmonizer.updateDataset(ds, imp);
			}
			else grayPixelHarmonizer.updateDataset(ds, imp);
		}
		metadataHarmonizer.updateDataset(ds, imp);
		compositeHarmonizer.updateDataset(ds, imp);
		overlayTranslator.setDisplayOverlays(display, imp);
		colorTableHarmonizer.updateDisplay(display, imp);
		// NB - make it the lower level methods' job to call ds.update()
	}

	/**
	 * Remembers the type of an {@link ImagePlus}. This type can be checked after
	 * a call to a plugin to see if the ImagePlus underwent a type change.
	 */
	public void registerType(final ImagePlus imp) {
		bitDepthMap.put(imp, imp.getBitDepth());
	}

	/**
	 * Forgets the types of all {@link ImagePlus}es. Called before a plugin is run
	 * to reset the tracking of types.
	 */
	public void resetTypeTracking() {
		bitDepthMap.clear();
	}

	// -- private interface --

	/**
	 * Returns true if an {@link ImagePlus}' type is the best fit for a given
	 * {@link Dataset}. Best fit means the IJ1 type that is the best at preserving
	 * data.
	 */
	private boolean imagePlusIsNearestType(final Dataset ds, final ImagePlus imp) {
		final int impType = imp.getType();

		if (impType == ImagePlus.COLOR_RGB) {
			if (LegacyUtils.isColorCompatible(ds)) return true;
		}

		final RealType<?> dsType = ds.getType();
		final boolean isSigned = ds.isSigned();
		final boolean isInteger = ds.isInteger();
		final int bitsPerPix = dsType.getBitsPerPixel();

		if (!isSigned && isInteger && bitsPerPix <= 8) {
			return impType == ImagePlus.GRAY8 || impType == ImagePlus.COLOR_256;
		}

		if (!isSigned && isInteger && bitsPerPix <= 16) {
			return impType == ImagePlus.GRAY16;
		}

		// isSigned && !isInteger
		return impType == ImagePlus.GRAY32;
	}
	
	/**
	 * Changes the shape of an existing {@link Dataset} to match that of an
	 * {@link ImagePlus}. Assumes that the Dataset type is correct. Does not set
	 * the data values or change the metadata.
	 */
	// assumes the data type of the given Dataset is fine as is
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void reshapeDataset(final Dataset ds, final ImagePlus imp) {
		final long[] newDims = ds.getDims();
		final double[] cal = new double[newDims.length];
		ds.calibration(cal);
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);
		if (xIndex >= 0) newDims[xIndex] = imp.getWidth();
		if (yIndex >= 0) newDims[yIndex] = imp.getHeight();
		if (cIndex >= 0) {
			if (imp.getType() == ImagePlus.COLOR_RGB) {
				newDims[cIndex] = 3 * imp.getNChannels();
			}
			else newDims[cIndex] = imp.getNChannels();
		}
		if (zIndex >= 0) newDims[zIndex] = imp.getNSlices();
		if (tIndex >= 0) newDims[tIndex] = imp.getNFrames();
		final ImgFactory factory = ds.getImgPlus().factory();
		final Img<?> img = factory.create(newDims, ds.getType());
		final ImgPlus<?> imgPlus =
			new ImgPlus(img, ds.getName(), ds.getAxes(), cal);
		ds.setImgPlus((ImgPlus<? extends RealType<?>>) imgPlus);
	}

	/**
	 * Determines whether a {@link Dataset} and an {@link ImagePlus} have
	 * incompatible dimensionality.
	 */
	private boolean dimensionsIncompatible(final Dataset ds, final ImagePlus imp)
	{
		final int xIndex = ds.getAxisIndex(Axes.X);
		final int yIndex = ds.getAxisIndex(Axes.Y);
		final int zIndex = ds.getAxisIndex(Axes.Z);
		final int tIndex = ds.getAxisIndex(Axes.TIME);

		final long[] dimensions = ds.getDims();

		final long x = (xIndex < 0) ? 1 : dimensions[xIndex];
		final long y = (yIndex < 0) ? 1 : dimensions[yIndex];
		final long z = (zIndex < 0) ? 1 : dimensions[zIndex];
		final long t = (tIndex < 0) ? 1 : dimensions[tIndex];

		if (x != imp.getWidth()) return true;
		if (y != imp.getHeight()) return true;
		if (z != imp.getNSlices()) return true;
		if (t != imp.getNFrames()) return true;
		// channel case a little different
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			int cIndex = ds.getAxisIndex(Axes.CHANNEL);
			if (cIndex < 0) return true;
			long c = dimensions[cIndex];
			if (c != imp.getNChannels() * 3) return true;
		}
		else { // not color data
			long c = LegacyUtils.ij1ChannelCount(dimensions, ds.getAxes());
			if (c != imp.getNChannels()) return true;
		}

		return false;
	}

	/**
	 * Creates a new {@link ImageStack} of data from a {@link ImageDisplay} and
	 * assigns it to given {@link ImagePlus}
	 * 
	 * @param display
	 * @param imp
	 */
	private void rebuildImagePlusData(final ImageDisplay display,
		final ImagePlus imp)
	{
		final ImagePlus newImp = imageTranslator.createLegacyImage(display);
		imp.setStack(newImp.getStack());
		final int c = newImp.getNChannels();
		final int z = newImp.getNSlices();
		final int t = newImp.getNFrames();
		imp.setDimensions(c, z, t);
		LegacyUtils.deleteImagePlus(newImp);
	}

}