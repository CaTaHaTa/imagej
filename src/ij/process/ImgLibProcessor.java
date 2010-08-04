package ij.process;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.File;

import javax.swing.JFileChooser;

import loci.common.DataTools;
import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.container.imageplus.ImagePlusContainer;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.exception.ImgLibException;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.display.imagej.ImageJFunctions;
import mpicbg.imglib.io.LOCI;
import mpicbg.imglib.type.Type;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.GenericByteType;
import mpicbg.imglib.type.numeric.integer.GenericShortType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

// NOTES
// Image may change to Img to avoid name conflict with java.awt.Image.
//
// TODO
// 1. Add a new container that uses array-backed data of the proper primitive
//    type, plane by plane.
// 2. Then we can return the data with getPixels by reference in that case
//    (and use the current cursor approach in other cases).
//
// For create8BitImage, we can call imageData.getDisplay().get8Bit* to extract
// displayable image data as bytes [was LocalizableByPlaneCursor relevant here
// as well? can't remember].
//
// For getPlane* methods, we can use a LocalizablePlaneCursor (see
// ImageJVirtualStack.extractSliceFloat for an example) to grab the data
// plane-by-plane; this way the container knows the optimal way to traverse.

public class ImgLibProcessor<T extends RealType<T>> extends ImageProcessor implements java.lang.Cloneable {

	static enum PixelType {BYTE,SHORT,INT,FLOAT,DOUBLE};
	
	//****************** Instance variables *******************************************************
	
	private final Image<T> imageData;


	// TODO: How can we use generics here without breaking javac?
	@SuppressWarnings("rawtypes")
	private final RealType type;

	private byte[] pixels8;
	private Snapshot<T> snapshot;
	private ImageProperties<T> imageProperties;
	

	//****************** Helper methods *******************************************************

	private static int[] createExtraDimensions(int[] dims)
	{
		if (dims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");

		int[] extraDimensions = new int[dims.length - 2];
		
		for (int i = 0; i < extraDimensions.length; i++)
		{
			extraDimensions[i] = dims[i+2];
		}
		
		return extraDimensions;
	}
	
	/**
	 * Returns plane preserving multidimensional array.
	 * First two dimensions are zeroized by new call. (E.g.
	 * { x, y, c, z, t...} where x & y will be zero.
 	 * @param coords
	 * @return
	 */
	private static int[] makePosArray(int[] coords) 
	{
		int[] pos = new int[2 + coords.length];
		for (int i=0; i<coords.length; i++) pos[i + 2] = coords[i];
		return pos;
	}

	private static int[] onePlaneExtent(int width, int height, int totalDims)
	{
		int[] spans = new int[totalDims];
		
		spans[0] = width;
		spans[1] = height;
		for (int i = 2; i < spans.length; i++)
			spans[i] = 1;
		
		return spans;
	}
	
	/*
	 * Throws an exception if the LUT length is wrong for the pixel layout type
	 */
	private void testLUTLength( int[] lut )
	{
		if ( type instanceof GenericByteType< ? > )
		{
			if (lut.length!=256)
				throw new IllegalArgumentException("lut.length != expected length for type " + type );
		} else if( type instanceof GenericShortType< ? > )
		{
			if (lut.length!=65536)
				throw new IllegalArgumentException("lut.length != expected length for type " + type );
		} else {
			throw new IllegalArgumentException("LUT NA for type " + type ); 
		}
	}
	
//	@SuppressWarnings("unchecked")
//	private ArrayDataAccess<?> getAccess(mpicbg.imglib.image.Image<T> img) {
//		final Container<?> container = img.getContainer();
//		if (!(container instanceof Array<?, ?>)) return null;
//		final Array<T, ?> array = (Array<T, ?>) img.getContainer();
//		ArrayDataAccess<?> access = (ArrayDataAccess<?>) array.update(null);
//		return access;
//	}

	// TODO is there a better way? ask.

//	private boolean isSignedType(T t) {
//		return !(
//			(t instanceof UnsignedByteType) ||
//			(t instanceof UnsignedIntType) ||
//			(t instanceof UnsignedShortType)
//		);
//	}

	private int[] getMultiDimensionalPositionArray( int x, int y )
	{
		//get the dimensions
		int[] imageDimensions = imageData.getDimensions( ).clone( );
		
		//copy in x and y
		imageDimensions[0] = x;
		imageDimensions[1] = y;
		
		//assign the dimensions
		for(int i = 2; i < (2 + imageProperties.getExtraDimensions().length); i++)
		{
			imageDimensions[i]=imageProperties.getExtraDimensions()[i-2];
		}
		
		return imageDimensions;
	}

	private Object getCopyOfPixelsFromImage(Image<T> image, RealType type, int[] extraDims)
	{
		int w = image.getDimension(0);
		int h = image.getDimension(1);
		
		if (type instanceof ByteType) {
			Image<ByteType> im = (Image) image;
			return getPlaneBytes(im, w, h, extraDims);
		}
		if (type instanceof UnsignedByteType) {
			Image<UnsignedByteType> im = (Image) image;
			return getPlaneUnsignedBytes(im, w, h, extraDims);
		}
		if (type instanceof ShortType) {
			Image<ShortType> im = (Image) image;
			return getPlaneShorts(im, w, h, extraDims );
		}
		if (type instanceof UnsignedShortType) {
			Image<UnsignedShortType> im = (Image) image;
			return getPlaneUnsignedShorts(im, w, h, extraDims);
		}
		if (type instanceof IntType) {
			Image<IntType> im = (Image) image;
			return getPlaneInts(im, w, h, extraDims);
		}
		if (type instanceof UnsignedIntType) {
			Image<UnsignedIntType> im = (Image) image;
			return getPlaneUnsignedInts(im, w, h, extraDims);
		}
		if (type instanceof LongType) {
			Image<LongType> im = (Image) image;
			return getPlaneLongs(im, w, h, extraDims);
		}
		if (type instanceof FloatType) {
			Image<FloatType> im = (Image) image;
			return getPlaneFloats(im, w, h, extraDims);
		}
		if (type instanceof DoubleType) {
			Image<DoubleType> im = (Image) image;
			return getPlaneDoubles(im, w, h, extraDims);
		}
		return getPlaneData(image, w, h, extraDims);
	}
	
	private double getPixValue(Object pixels, PixelType type, int pixNum)
	{
		switch (type) {
			case BYTE:
				return ((byte[])pixels)[pixNum];
			case SHORT:
				return ((short[])pixels)[pixNum];
			case INT:
				return ((int[])pixels)[pixNum];
			case FLOAT:
				return ((float[])pixels)[pixNum];
			case DOUBLE:
				return ((double[])pixels)[pixNum];
			default:
				throw new IllegalArgumentException("unknown pixel type");
		}
	}
	
	private void setSnapshotPlane(Object pixels, PixelType type, int numPixels)
	{
		Image<T> data = snapshot.getStorage();

		long totalSamples = 1;
		for (int i = 0; i < data.getNumDimensions(); i++)
			totalSamples *= data.getDimension(i);
		
		if (numPixels != totalSamples)
			throw new IllegalArgumentException("snapshot size does not match number of pixels passed in");
		
		int[] extraDims = createExtraDimensions(data.getDimensions());
		
		int[] origin = makePosArray(extraDims);
		
		int[] extents = onePlaneExtent(width,height,data.getNumDimensions());
		
		final LocalizableByDimCursor<T> snapCursor = data.createLocalizableByDimCursor( );
		
        RegionOfInterestCursor<T> snapRoiCursor = new RegionOfInterestCursor< T >( snapCursor, origin, extents );
		
        int i = 0;
		for (final T pixel:snapRoiCursor)
		{
			pixel.setReal( getPixValue(pixels,type,i) );
		}
		
		//close the cursors
		snapRoiCursor.close();
		snapCursor.close();
	}
	
	//****************** public interface *******************************************************

	public ImgLibProcessor(Image<T> img, T type ) {

		final int[] dims = img.getDimensions();
		
		if (dims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");

		this.imageData = img;
		this.type = type;
		
		//assign the properties object for the image
		imageProperties = new ImageProperties< T >( );
		
		int[] extraDimensions = createExtraDimensions(dims);
		
		imageProperties.setExtraDimensions( extraDimensions );

		this.width = dims[0]; // TODO: Dimensional labels are safer way to find X
		this.height = dims[1]; // TODO: Dimensional labels are safer way to find Y
	}
	
	protected ImageProperties<T> getImageProperties() {
		return imageProperties;
	}

	@Override
	public void applyTable(int[] lut) 
	{
		//test "lut" length
		testLUTLength(lut);
		
		//Fill the image with data - first get a cursor
		final LocalizableByDimCursor<T> imageCursor = this.imageData.createLocalizableByDimCursor( );
        RegionOfInterestCursor<T> imageRegionOfInterestCursor = new RegionOfInterestCursor< T >( imageCursor, new int[] {roiX, roiY}, new int[] { roiWidth, roiHeight} );
		
		for (final T pixel:imageRegionOfInterestCursor)
		{
			pixel.setReal( lut[ (int) pixel.getRealDouble() ] );
		}
		
		//close the cursor
		imageCursor.close( );
	}

	@Override
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void convolve3x3(int[] kernel) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
    throw new RuntimeException("Unimplemented");

	}

	//TODO ask about changing name of Image to avoid conflict with java.awt
	@Override
	public java.awt.Image createImage() {
		boolean firstTime = pixels8==null;
		if (firstTime || !lutAnimation)
			create8BitImage();
		if (cm==null)
			makeDefaultColorModel();
		if (source==null) {
			source = new MemoryImageSource(width, height, cm, pixels8, 0, width);
			source.setAnimated(true);
			source.setFullBufferUpdates(true);
			img = Toolkit.getDefaultToolkit().createImage(source);
		} else if (newPixels) {
			source.newPixels(pixels8, cm, 0, width);
			newPixels = false;
		} else
			source.newPixels();

		lutAnimation = false;
	  return img;
	}

	@Override
	public ImageProcessor createProcessor(int width, int height) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public ImageProcessor crop() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public void dilate() {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void drawPixel(int x, int y) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public ImageProcessor duplicate() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public void erode() {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void fill(ImageProcessor mask) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void filter(int type) {
    throw new RuntimeException("Unimplemented");

	}

	/** swap the rows of an image about its central row */
	@Override
	public void flipVertical()
	{
		// create suitable cursor
		final LocalizableByDimCursor<T> cursor1 = this.imageData.createLocalizableByDimCursor( );
		final LocalizableByDimCursor<T> cursor2 = this.imageData.createLocalizableByDimCursor( );
		
		// allocate arrays that will hold position variables
		final int[] position1 = makePosArray( this.imageProperties.getExtraDimensions() );
		final int[] position2 = makePosArray( this.imageProperties.getExtraDimensions() );
		
		// calc some useful variables in regards to our region of interest.
		final int minX = this.roiX;
		final int minY = this.roiY;
		final int maxX = minX + this.roiWidth - 1;
		final int maxY = minY + this.roiHeight - 1;
		
		// calc half height - we will only need to swap the top half of the rows with the bottom half
		final int halfRoiHeight = this.roiHeight / 2;
		
		// the half the rows
		for (int yoff = 0; yoff < halfRoiHeight; yoff++) {
			
			// calc locations of the two rows to be swapped
			final int y1 = minY + yoff;
			final int y2 = maxY - yoff;
			
			// for each col in this row
			for (int x=minX; x<=maxX; x++) {
				
				// setup position index for cursor 1
				position1[0] = x;
				position1[1] = y1;

				// setup position index for cursor 2
				position2[0] = x;
				position2[1] = y2;

				// move to position1 and save the current value
				cursor1.setPosition(position1);
				final double pixVal1 = cursor1.getType().getRealDouble();
				
				// move to position2 and save the current value
				cursor2.setPosition(position2);
				final double pixVal2 = cursor2.getType().getRealDouble();
		
				// write the values back in swapped order
				cursor2.getType().setReal(pixVal1);
				cursor1.getType().setReal(pixVal2);
			}
		}
		
		// close the cursors when done with them
		cursor1.close();
		cursor2.close();
	}

	@Override
	public int get(int x, int y) 
	{	
		int value;
		
		final LocalizableByDimCursor<T> cursor = imageData.createLocalizableByDimCursor();
		cursor.setPosition( getMultiDimensionalPositionArray( x, y ) );
		
		value = (int)( cursor.getType().getRealDouble() );
		
		cursor.close( );
		
		return value;
	}

	@Override
	public int get(int index) {
		//imageData
		int x = index/width;
		int y = index%width;
		return get( x, y) ;
	}

	@Override
	public double getBackgroundValue() {
		return imageProperties.getBackgroundValue();
	}

	@Override
	public int[] getHistogram() {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getInterpolatedPixel(double x, double y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public double getMax() 
	{
		//get the current image data
		int[] imageDimensionsOffset = makePosArray( imageProperties.getExtraDimensions() );
		int[] imageDimensionsSize = makePosArray( imageProperties.getExtraDimensions() );
		
		//set the size
		imageDimensionsSize[0] = imageData.getDimensions()[0];
		imageDimensionsSize[1] = imageData.getDimensions()[1];
		
		//Get a cursor
		final LocalizableByDimCursor<T> imageCursor = imageData.createLocalizableByDimCursor( );
		final RegionOfInterestCursor<T> imageROICursor = new RegionOfInterestCursor< T >( imageCursor, imageDimensionsOffset, imageDimensionsSize );
				
		//assign the return value
		double max = imageCursor.getType().getMinValue();
 
		//iterate over all the pixels, of the selected image plane
		for(T pixel:imageROICursor)
		{
			if( pixel.getRealDouble() > max )
				max = pixel.getRealDouble();
		}
		
		//close the cursor
		imageROICursor.close( );
		imageCursor.close( );
		
		return max;
	}

	@Override
	public double getMin() 
	{
		//get the current image data
		int[] imageDimensionsOffset = makePosArray( imageProperties.getExtraDimensions() );
		int[] imageDimensionsSize = makePosArray( imageProperties.getExtraDimensions() );
		
		//set the size
		imageDimensionsSize[0] = imageData.getDimensions()[0];
		imageDimensionsSize[1] = imageData.getDimensions()[1];
		
		//Get a cursor
		final LocalizableByDimCursor<T> imageCursor = imageData.createLocalizableByDimCursor( );
		final RegionOfInterestCursor<T> imageROICursor = new RegionOfInterestCursor< T >( imageCursor, imageDimensionsOffset, imageDimensionsSize );

		//assign the return value
		double min = imageCursor.getType().getMaxValue();
 
		//iterate over all the pixels, of the selected image plane
		for(T pixel:imageROICursor)
		{
			if( pixel.getRealDouble() < min )
				min = pixel.getRealDouble();
		}
		
		//close the cursor
		imageCursor.close( );
		
		return min;
	}

	@Override
	public int getPixel(int x, int y) {
		return get(x,y);
	}

	@Override
	public int getPixelInterpolated(double x, double y) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public float getPixelValue(int x, int y) {
		return getf(x,y);
	}

	@Override
	public Object getPixels() {
		// TODO: could add a special case for single-image 8-bit array-backed data
		// TODO: special case for new container
		return getPixelsArray();
	}

	@Override
	public Object getPixelsCopy() {
		
		if (snapshot!=null && snapshotCopyMode)
		{
			snapshotCopyMode = false;
			int[] extraDims = createExtraDimensions(snapshot.getStorage().getDimensions());
			return getCopyOfPixelsFromImage(snapshot.getStorage(),type,extraDims);
		}
		else
		{
			return getPixelsArray();
		}
	}

	@Override
	public Object getSnapshotPixels() {
		Image<T> image = this.snapshot.getStorage();
		int[] extraDims = createExtraDimensions(image.getDimensions());
		return getCopyOfPixelsFromImage(image, type, extraDims);
	}

	@Override
	public float getf(int x, int y) 
	{
		float value;
		
		final LocalizableByDimCursor<T> cursor = imageData.createLocalizableByDimCursor();
				
		cursor.setPosition( getMultiDimensionalPositionArray(x, y) );
		
		value =  ( float ) cursor.getType().getRealDouble();
		
		cursor.close( );
		
		return value;
	}

	@Override
	public float getf(int index) {
		int x = index/width;
		int y = index%width;
		return getf( x, y) ;
	}

	@Override
	public void medianFilter() {
	    throw new RuntimeException("Unimplemented");
	}

	@Override
	public void noise(double range) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void putPixel(int x, int y, int value) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void putPixelValue(int x, int y, double value) {
    throw new RuntimeException("Unimplemented");

	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void reset() {
		
		if (this.snapshot!=null)
		{
			this.snapshot.pasteIntoImage(this.imageData);
		}
		// TODO - ShortProcessor kept track of max and min here. Might need to do so also. But imglib or Rick may do too.
	}

	@Override
	public void reset(ImageProcessor mask) {
		
		if (mask==null || snapshot==null)
			return;
		
		if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
			throw new IllegalArgumentException(maskSizeError(mask));

		Image<T> snapData = snapshot.getStorage();
		
		LocalizableByDimCursor<T> imageCursor = imageData.createLocalizableByDimCursor();
		LocalizableByDimCursor<T> snapshotCursor = snapData.createLocalizableByDimCursor();

		int[] originInImage = makePosArray(imageProperties.getExtraDimensions());
		originInImage[0] = roiX;
		originInImage[1] = roiY;
		int[] originInSnapshot = new int[snapData.getNumDimensions()];

		int[] spanInImage = onePlaneExtent(roiWidth,roiHeight,imageData.getNumDimensions());
		int[] spanInSnapshot = onePlaneExtent(roiWidth,roiHeight,snapData.getNumDimensions());
		
		RegionOfInterestCursor<T> imageRoiCursor = new RegionOfInterestCursor<T>(imageCursor, originInImage, spanInImage);
		RegionOfInterestCursor<T> snapRoiCursor = new RegionOfInterestCursor<T>(snapshotCursor, originInSnapshot, spanInSnapshot);
		
		byte[] maskPixels = (byte[])mask.getPixels();
		
		int i = 0;
		while (imageRoiCursor.hasNext() && snapRoiCursor.hasNext())
		{
			imageRoiCursor.fwd();
			snapRoiCursor.fwd();
			
			if (maskPixels[i++] == 0)
			{
				double pix = snapRoiCursor.getType().getRealDouble();
				imageRoiCursor.getType().setReal(pix);
			}
		}
		
		snapRoiCursor.close();
		imageRoiCursor.close();
		snapshotCursor.close();
		imageCursor.close();
	}

	@Override
	public ImageProcessor resize(int dstWidth, int dstHeight) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public void rotate(double angle) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void scale(double xScale, double yScale) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void set(int x, int y, int value) 
	{
		final LocalizableByDimCursor<T> cursor = imageData.createLocalizableByDimCursor();
		
		cursor.setPosition( getMultiDimensionalPositionArray( x, y ) );
		cursor.getType().setReal( value );
		
		cursor.close();
	}

	@Override
	public void set(int index, int value) 
	{
		int x = index/width;
		int y = index%width;
		set( x, y, value) ;
	}

	@Override
	public void setBackgroundValue(double value) 
	{
		imageProperties.getBackgroundValue();
	}

	@Override
	public void setColor(Color color) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setMinAndMax(double min, double max) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setPixels(Object pixels) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setPixels(int channelNumber, FloatProcessor fp) {
    throw new RuntimeException("Unimplemented");

	}

	@Override
	public void setSnapshotPixels(Object pixels)
	{
		if (pixels instanceof byte[])
			setSnapshotPlane(pixels,PixelType.BYTE,((byte[])pixels).length);
		else if (pixels instanceof short[])
			setSnapshotPlane(pixels,PixelType.SHORT,((short[])pixels).length);
		else if (pixels instanceof int[])
			setSnapshotPlane(pixels,PixelType.INT,((int[])pixels).length);
		else if (pixels instanceof float[])
			setSnapshotPlane(pixels,PixelType.FLOAT,((float[])pixels).length);
		else if (pixels instanceof double[])
			setSnapshotPlane(pixels,PixelType.DOUBLE,((double[])pixels).length);
		else
			throw new IllegalArgumentException();
	}

	@Override
	public void setValue(double value) 
	{
		fgColor = (int) value;
		
		fgColor = boundIntValueToType(fgColor);
	}

	@Override
	public void setf(int x, int y, float value) {
		final LocalizableByDimCursor<T> cursor = imageData.createLocalizableByDimCursor();
		
		cursor.setPosition( getMultiDimensionalPositionArray( x, y ) );
		cursor.getType().setReal( value );
		
		cursor.close();
	}

	@Override
	public void setf(int index, float value) {
		int x = index/width;
		int y = index%width;
		setf( x, y, value);
	}

	@Override
	public void snapshot() 
	{
		int[] origins = makePosArray( imageProperties.getExtraDimensions() );

		origins[0] = 0;
		origins[1] = 0;
		
		int[] spans = onePlaneExtent(width, height, imageData.getNumDimensions());
		
		this.snapshot.copyFromImage(imageData, origins, spans);
		
		// TODO - ShortProcessor kept track of max and min here. Might need to do so also. But imglib or Rick may do too.
	}

	@Override
	public void threshold(int thresholdLevel) 
	{
		//ensure level is OK for underlying type & convert to double
		double thresholdLevelAsDouble = boundIntValueToType( thresholdLevel );
		
		//Get a cursor
		final LocalizableByDimCursor<T> imageCursor = imageData.createLocalizableByDimCursor( );
        
		for (final T pixel : imageCursor)
		{
			pixel.setReal( pixel.getRealDouble() <= thresholdLevelAsDouble ? pixel.getMinValue( ) : pixel.getMaxValue() );
		}
		
		//close the cursor
		imageCursor.close( );
	}

	@Override
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp) {
    throw new RuntimeException("Unimplemented");
	}

	@Override
	public byte[] create8BitImage()
	{
		// TODO: use imageData.getDisplay().get8Bit* methods
		Object pixels = getPixels();

		if (pixels instanceof byte[])
		{
			pixels8 = (byte[]) pixels;
		}
		else if (pixels instanceof short[])
		{
			short[] pix = (short[]) pixels;
			pixels8 = DataTools.shortsToBytes(pix, false);
		}
		else if (pixels instanceof int[])
		{
			int[] pix = (int[]) pixels;
			pixels8 = DataTools.intsToBytes(pix, false);
		}
		else if (pixels instanceof float[])
		{
			float[] pix = (float[]) pixels;
			pixels8 = DataTools.floatsToBytes(pix, false);
		}
		else if (pixels instanceof double[])
		{
			double[] pix = (double[]) pixels;
			pixels8 = DataTools.doublesToBytes(pix, false);
		}
		else if (pixels instanceof long[])
		{
			long[] pix = (long[]) pixels;
			pixels8 = DataTools.longsToBytes(pix, false);
		}

		return pixels8;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Object getPixelsArray() {
		return getCopyOfPixelsFromImage(this.imageData, this.type, this.imageProperties.getExtraDimensions());
	}

	public double[] getPlaneData()
	{
		return getPlaneData(imageData,width,height,imageProperties.getExtraDimensions());
	}
	
	public double[] getPlaneData(Image<T> image, int w, int h, int[] extraDims) {
		  // TODO - use LocalizablePlaneCursor
			// example in ImageJVirtualStack.extractSliceFloat
			final double[] data = new double[w * h];
			final LocalizableByDimCursor<T> cursor = image.createLocalizableByDimCursor();
			final int[] pos = makePosArray(extraDims);
			int index = 0;
			for (int y=0; y<h; y++) {
				pos[1] = y;
				for (int x=0; x<w; x++) {
					pos[0] = x;
					cursor.setPosition(pos);
					// TODO: better handling of complex types
					data[index++] = cursor.getType().getRealDouble();
				}
			}
			return data;
		}
		
	// TODO: Can we extract these arrays without case logic? Seems difficult...

	public static byte[] getPlaneBytes(Image<ByteType> im, int w, int h, int[] coords)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<ByteType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static byte[] getPlaneUnsignedBytes(Image<UnsignedByteType> im, int w, int h, int[] coords)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<UnsignedByteType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (byte) cursor.getType().get();
			}
		}
		return data;
	}

	public static short[] getPlaneShorts(Image<ShortType> im, int w, int h, int[] coords)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<ShortType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static short[] getPlaneUnsignedShorts(Image<UnsignedShortType> im, int w, int h, int[] coords)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<UnsignedShortType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (short) cursor.getType().get();
			}
		}
		return data;
	}

	public static int[] getPlaneInts(Image<IntType> im, int w, int h, int[] coords)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<IntType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static int[] getPlaneUnsignedInts(Image<UnsignedIntType> im, int w, int h, int[] coords)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<UnsignedIntType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = (int) cursor.getType().get();
			}
		}
		return data;
	}

	public static long[] getPlaneLongs(Image<LongType> im, int w, int h, int[] coords)
	{
		final long[] data = new long[w * h];
		final LocalizableByDimCursor<LongType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static float[] getPlaneFloats(Image<FloatType> im, int w, int h, int[] coords)
	{
		final float[] data = new float[w * h];
		final LocalizableByDimCursor<FloatType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static double[] getPlaneDoubles(Image<DoubleType> im, int w, int h, int[] coords)
	{
		final double[] data = new double[w * h];
		final LocalizableByDimCursor<DoubleType> cursor =
			im.createLocalizableByDimCursor();
		final int[] pos = makePosArray(coords);
		int index = 0;
		for (int y=0; y<h; y++) {
			pos[1] = y;
			for (int x=0; x<w; x++) {
				pos[0] = x;
				cursor.setPosition(pos);
				data[index++] = cursor.getType().get();
			}
		}
		return data;
	}

	public static <T extends RealType<T>> void display(Image<T> img,
			    String title)
			  {
			    ImagePlus imp = null;
			    Container<T> c = img.getContainer();
			    if (c instanceof ImagePlusContainer<?, ?>) {
			      ImagePlusContainer<T, ?> ipc = (ImagePlusContainer<T, ?>) c;
			      try {
			        imp = ipc.getImagePlus();
			      }
			      catch (ImgLibException exc) {
			        IJ.log("Warning: " + exc.getMessage());
			      }
			    }
			    if (imp == null) {
			      imp = ImageJFunctions.copyToImagePlus(img);
			    }
			    if (title != null) imp.setTitle(title);
			    imp.show();
			  }

	//public <T extends Type<T>> ImgLibProcessor(final Image<T> img)
	//{
	//	
	//}
/*	
	public static <T extends RealType<T>> ImagePlus createImagePlus(final Image<T> img)
	{
		ImageProcessor processor = new ImgLibProcessor<T>(img, img.createCursor().getType() );
		ImagePlus imp = new ImagePlus(img.getName(),processor);
		return imp;
	}
*/
	
	public static ImagePlus createImagePlus(final Image<?> img)
	{
		ImageProcessor processor = null;
		
		Type runtimeT = img.createCursor().getType();
		
		if (runtimeT instanceof UnsignedByteType)
		{
			processor = new ImgLibProcessor<UnsignedByteType>((Image<UnsignedByteType>)img, new UnsignedByteType());
		}
			
		if (processor == null)
			throw new IllegalArgumentException("no processor type was matched");
		
		return new ImagePlus(img.getName(),processor);
	}
	
	//TODO add to utility class...
	/**
	 * Limits and returns the range of the input value
	 * to the corresponding max and min values respective to the
	 * underlying type.
	 */
	public int boundIntValueToType(int inputValue)
	{
		if (type instanceof ByteType ||
			type instanceof UnsignedByteType ||
			type instanceof ShortType ||
			type instanceof UnsignedShortType ) 
		{
			if (inputValue < type.getMinValue() ) inputValue = ( int ) type.getMinValue();
			if (inputValue > type.getMaxValue() ) inputValue = (int) type.getMaxValue();
		}

		return inputValue;
	}
	
	public static void main(String[] args) {
		final JFileChooser chooser = new JFileChooser();
		int rval = chooser.showOpenDialog(null);
		if (rval != JFileChooser.APPROVE_OPTION) return;
		final File file = chooser.getSelectedFile();
		final String fileName = file.getAbsolutePath();
		final ContainerFactory containerFactory = new ArrayContainerFactory();
		//Image<?> image = LOCI.openLOCI( fileName, containerFactory );
		final Image<UnsignedByteType> image = LOCI.openLOCIUnsignedByteType(fileName, containerFactory);
		
		// make our image plus from inglib image
		final ImagePlus imp = createImagePlus(image);
		
		// methods to test to make sure they work
		
		// invert() : works
		//
		// imp.getProcessor().invert();
		
		// applyTable() : works
		//
		int[] lut = new int[256];
		for (int i = 0; i < 256; i++)
		lut[i] = 255-i;
		imp.getProcessor().applyTable(lut);
		
		// TODO
		//convolve(float[] kernel, int kernelWidth, int kernelHeight)
		//convolve3x3(int[] kernel)
		//copyBits(ImageProcessor ip, int xloc, int yloc, int mode)
		// etc.

		// get(x,y)
		//imp.getProcessor().set( 20, 20,175 );
		//System.out.println( imp.getProcessor().get(20,20) );
		
		//Start the timer
		long a = System.currentTimeMillis( );
		for(int x = 0;x<1000;x++)
			imp.getProcessor().flipVertical();
		
		//stop the timer
		long b = System.currentTimeMillis( );
		
		System.out.println("Took imglib " + (b-a) + " milliseconds or " + ((b-a)/1000) + " seconds to do an image flip.");

		new ImageJ();
		imp.show();
		
		
		/*
		final int[] dims = image.getDimensions();
		final int width = dims[0];
		final int height = dims[1];
		final int sizeZ = 1;
		final int[] coords = new int[1];
		final ImageStack imageStack = new ImageStack(width, height);
		for (int i =0; i<sizeZ; i++) {
			coords[0] = i;
			ImageProcessor ip = new ImgLibProcessor<UnsignedByteType>(image,
				new UnsignedByteType(), coords);
			imageStack.addSlice("" + (i + 1), ip);
		}
		*/

	}
}
