package ij.process;

import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.real.*;
import mpicbg.imglib.container.array.*;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.image.*;

/** an N-dimensional copy of a subset of an Image's data with ability to capture and restore values */
public class Snapshot<T extends RealType<T>>
{
	// ************** Instance variables ********************************************************************
	
	/** The start index in the referenced image that the snapshot copies. */
	private int[] dimensionOrigins;
	
	/** The width of each dimension */
	private int[] dimensionSpans;

	/** Internal multidimensional storage reference. Has same number of dimensions as referenced data set but actual sizes may differ. */
	private Image<T> storage;
	
	
	// ************** Private helper methods ********************************************************************

	/** throws an exception if the combination of origins and spans is outside an image's dimensions */
	private void testDimensionBoundaries(int[] imageDimensions, int[] origins, int[] spans)
	{
		// span dims should match origin dims
		if (origins.length != spans.length)
			throw new IllegalArgumentException("testDimensionBoundaries() : origins and spans arrays are of differing sizes");

		// origin/span dimensions should match image dimensions
		if (origins.length != imageDimensions.length)
			throw new IllegalArgumentException("testDimensionBoundaries() : origins/spans different size than input image");
		
		// make sure origin in a valid range : within bounds of source image
		for (int i = 0; i < origins.length; i++)
			if ((origins[i] < 0) || (origins[i] >= imageDimensions[i]))
				throw new IllegalArgumentException("testDimensionBoundaries() : origins outside bounds of input image at index " + i);

		// make sure span in a valid range : >= 1
		for (int i = 0; i < spans.length; i++)
			if (spans[i] < 1)
				throw new IllegalArgumentException("testDimensionBoundaries() : span size < 1 at index " + i);

		// make sure origin + span within the bounds of the input image
		for (int i = 0; i < spans.length; i++)
			if ( (origins[i] + spans[i]) > imageDimensions[i] )
				throw new IllegalArgumentException("testDimensionBoundaries() : span range (origin+span) beyond input image boundaries at index " + i);
	}
	
	/** used by toString override */
	private String arrayToStr(int[] arr)
	{
		StringBuffer str = new StringBuffer();
		str.append("[");
		for (int i = 0; i < arr.length; i++)
		{
			if (i > 0)
				str.append(":");
			str.append(arr[i] );
		}
		str.append("]");
		
		return str.toString();
	}
	
	// ************** Public methods ********************************************************************
	
	/** a Snapshot is taken from an image starting at an origin and spanning each dimension */
	public Snapshot(Image<T> image, int[] origins, int[] spans)
	{
		copyFromImage(image,origins,spans);
	}
	
	/** allow access to snapshot data */ 
	public Image<T> getStorage()
	{
		return this.storage;
	}
	
	/*
	public void setStorage(Image<T> newStorage)
	{
		int[] currDimensions = this.storage.getDimensions();
		int[] newDimensions  = newStorage.getDimensions();
		
		if (currDimensions.length != newDimensions.length)
			throw new IllegalArgumentException("Snapshot::setStorage() passed an Image whose dimensions do not match current storage layout.");
		
		for (int i = 0; i < currDimensions.length; i++)
			if (currDimensions[i] != newDimensions[i])
				throw new IllegalArgumentException("Snapshot::setStorage() passed an Image whose dimensions do not match current storage layout.");
		
		this.storage = newStorage;
	}
	*/
	
	/** take a snapshot of an Image's data from given origin and across each span dimension */
	public void copyFromImage(Image<T> image, int[] origins, int[] spans)
	{
		// verify input
		testDimensionBoundaries(image.getDimensions(),origins,spans);
		
		// remember dimensions
		this.dimensionOrigins = origins.clone();
		this.dimensionSpans = spans.clone();
		
		// get factory to create a data repository
		ImageFactory<T> factory = new ImageFactory<T>(image.createType(),image.getContainerFactory());

		// create the data repository
		this.storage = factory.createImage(this.dimensionSpans);
			
		// copy the data
		ImageUtils.copyFromImageToImage( image, this.storage, this.dimensionOrigins, new int[image.getDimensions().length], this.dimensionSpans );
	}

	/** paste snapshot data into an image */
	public void pasteIntoImage(Image<T> image)
	{
		// copy from the snapshot to the image
		ImageUtils.copyFromImageToImage(this.storage, image, new int[image.getDimensions().length], this.dimensionOrigins, this.dimensionSpans );
	}
	
	/** encode a snapshot as a String */
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.append( "snapshot(" );
		str.append( arrayToStr(this.storage.getDimensions()) );
		str.append(",");
		str.append( arrayToStr(this.dimensionOrigins) );
		str.append(",");
		str.append( arrayToStr(this.dimensionSpans) );
		str.append( ")" );
		
		return str.toString();
	}
}
