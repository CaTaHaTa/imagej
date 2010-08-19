package ij.process;

import ij.ImagePlus;
import ij.ImageStack;
import mpicbg.imglib.container.array.ArrayContainerFactory;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.Type;
import mpicbg.imglib.type.numeric.ComplexType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

// TODO
//   Notice that copyFromImageToImage() could create a CopyOperation and apply it. This requires breaking Operations out of
//     ImgLibProcessor first.
//   createImagePlus() calls imp.setDimensions(z,c,t). But we may have other dims too. Change when we can call ImagePlus::setDimensions(int[] dims)

public class ImageUtils {
	
	public static int[] getDimsBeyondXY(int[] fullDims)
	{
		if (fullDims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");
		
		int[] extraDims = new int[fullDims.length-2];
		
		for (int i = 0; i < extraDims.length; i++)
			extraDims[i] = fullDims[i+2];
		
		return extraDims;
	}
	
	public static long getTotalSamples(int[] dimensions)
	{
		int numDims = dimensions.length;
		
		if (numDims == 0)
			return 0;
		
		long totalSamples = 1;
		
		for (int i = 0; i < numDims; i++)
			totalSamples *= dimensions[i];
		
		return totalSamples;
	}
	
	public static long getTotalPlanes(int[] dimensions)
	{
		int numDims = dimensions.length;
		
		if (numDims < 2)
			return 0;
	
		if (numDims == 2)
			return 1;
		
		// else numDims > 2
		
		int[] sampleSpace = getDimsBeyondXY(dimensions);
		
		return getTotalSamples(sampleSpace);
	}

	/** return an n-dimensional position array populated from a sample number */
	public static int[] getPosition(int[] dimensions, long sampleNumber)
	{
		int numDims = dimensions.length;
		
		if (numDims == 0)
			throw new IllegalArgumentException("getPosition() passed an empty dimensions array");
		
		long totalSamples = getTotalSamples(dimensions);
		
		if ((sampleNumber < 0) || (sampleNumber >= totalSamples))
			throw new IllegalArgumentException("getPosition() passed a sample number out of range");
		
		int[] position = new int[numDims];
		
		for (int dim = 0; dim < numDims; dim++)
		{
			long multiplier = 1;
			for (int j = dim+1; j < numDims; j++)
				multiplier *= dimensions[j];
			
			int thisDim = 0;
			while (sampleNumber >= multiplier)
			{
				sampleNumber -= multiplier;
				thisDim++;
			}
			position[dim] = thisDim;
		}
		
		return position;
	}

	public static int[] getPlanePosition(int[] dimensions, long planeNumber)
	{
		int numDims = dimensions.length;
		
		if (numDims < 2)
			throw new IllegalArgumentException("getPlanePosition() requires at least a 2-D image");
		
		if (numDims == 2)
		{
			if (planeNumber != 0)
				throw new IllegalArgumentException("getPlanePosition() 2-D image can only have 1 plane");
			
			return new int[]{};  // TODO - this is a little scary to do. might need to throw exception and have other places fix the fact
								//    that we have a rows x cols x 1 image
		}
			
		int[] subDimensions = new int[numDims - 2];
		
		for (int i = 0; i < subDimensions.length; i++)
			subDimensions[i] = dimensions[i+2];
		
		return getPosition(subDimensions,planeNumber);
	}

	public static double[] getPlaneData(Image<? extends RealType> image, int w, int h, int[] planePos) {
		  // TODO - use LocalizablePlaneCursor
			// example in ImageJVirtualStack.extractSliceFloat
			final double[] data = new double[w * h];
			final LocalizableByDimCursor<? extends RealType> cursor = image.createLocalizableByDimCursor();
			final int[] pos = Index.create(0,0,planePos);
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

	public static byte[] getPlaneBytes(Image<ByteType> im, int w, int h, int[] planePos)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<ByteType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static byte[] getPlaneUnsignedBytes(Image<UnsignedByteType> im, int w, int h, int[] planePos)
	{
		final byte[] data = new byte[w * h];
		final LocalizableByDimCursor<UnsignedByteType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static short[] getPlaneShorts(Image<ShortType> im, int w, int h, int[] planePos)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<ShortType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static short[] getPlaneUnsignedShorts(Image<UnsignedShortType> im, int w, int h, int[] planePos)
	{
		final short[] data = new short[w * h];
		final LocalizableByDimCursor<UnsignedShortType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static int[] getPlaneInts(Image<IntType> im, int w, int h, int[] planePos)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<IntType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static int[] getPlaneUnsignedInts(Image<UnsignedIntType> im, int w, int h, int[] planePos)
	{
		final int[] data = new int[w * h];
		final LocalizableByDimCursor<UnsignedIntType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static long[] getPlaneLongs(Image<LongType> im, int w, int h, int[] planePos)
	{
		final long[] data = new long[w * h];
		final LocalizableByDimCursor<LongType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static float[] getPlaneFloats(Image<FloatType> im, int w, int h, int[] planePos)
	{
		final float[] data = new float[w * h];
		final LocalizableByDimCursor<FloatType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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

	public static double[] getPlaneDoubles(Image<DoubleType> im, int w, int h, int[] planePos)
	{
		final double[] data = new double[w * h];
		final LocalizableByDimCursor<DoubleType> cursor = im.createLocalizableByDimCursor();
		final int[] pos = Index.create(0,0,planePos);
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
	
	public static Object getPlane(Image<? extends RealType<?>> im, int w, int h, int[] planePos)
	{
		Cursor<? extends RealType<?>> cursor = im.createCursor();

		RealType<?> type = cursor.getType();

		cursor.close();

		if (type instanceof ByteType)
			return getPlaneBytes((Image<ByteType>)im,w,h,planePos);

		if (type instanceof UnsignedByteType)
			return getPlaneUnsignedBytes((Image<UnsignedByteType>)im,w,h,planePos);

		if (type instanceof ShortType)
			return getPlaneShorts((Image<ShortType>)im,w,h,planePos);

		if (type instanceof UnsignedShortType)
			return getPlaneUnsignedShorts((Image<UnsignedShortType>)im,w,h,planePos);

		if (type instanceof IntType)
			return getPlaneInts((Image<IntType>)im,w,h,planePos);

		if (type instanceof UnsignedIntType)
			return getPlaneUnsignedInts((Image<UnsignedIntType>)im,w,h,planePos);

		if (type instanceof LongType)
			return getPlaneLongs((Image<LongType>)im,w,h,planePos);

		if (type instanceof FloatType)
			return getPlaneFloats((Image<FloatType>)im,w,h,planePos);

		if (type instanceof DoubleType)
			return getPlaneDoubles((Image<DoubleType>)im,w,h,planePos);

		// TODO - longs and complex types

		throw new IllegalArgumentException("getPlane(): unsupported type - "+type.getClass());
	}
	
	/** copies data from one image to another given origins and dimensional spans */
	public static <K extends ComplexType<K>>
		void copyFromImageToImage(Image<K> srcImage, int[] srcOrigin, Image<K> dstImage, int[] dstOrigin, int[] span)
	{
		// COPY DATA FROM SOURCE IMAGE TO DEST IMAGE:
		
		// create cursors
		final LocalizableByDimCursor<K> srcCursor = srcImage.createLocalizableByDimCursor();
		final LocalizableByDimCursor<K> dstCursor = dstImage.createLocalizableByDimCursor();
		final RegionOfInterestCursor<K> srcROICursor = new RegionOfInterestCursor<K>( srcCursor, srcOrigin, span );
		final RegionOfInterestCursor<K> dstROICursor = new RegionOfInterestCursor<K>( dstCursor, dstOrigin, span );
			
		//iterate over the target data...
		while( srcROICursor.hasNext() && dstROICursor.hasNext() )
		{
			//point cursors to current value
			srcROICursor.fwd();
			dstROICursor.fwd();
			
			//get the source value
			double real = srcROICursor.getType().getPowerDouble();
			double image = srcROICursor.getType().getPhaseDouble();
			
			//System.out.println("Source values are " + real + " and " + complex );
		
			//set the destination value
			dstROICursor.getType().setComplexNumber(real, image);
		}
		
		//close the open cursors
		srcROICursor.close( );
		dstROICursor.close( );    	
		srcCursor.close( );
		dstCursor.close( );
	}
	
	public static ImgLibProcessor<?> createProcessor(int width, int height, Object pixels, boolean unsigned)
	{
		ImgLibProcessor<?> proc = null;
		
		int[] dimensions = new int[]{width, height, 1};
		
		if (pixels instanceof byte[])
		{
			if (unsigned)
			{
				ImageFactory<UnsignedByteType> factory = new ImageFactory<UnsignedByteType>(new UnsignedByteType(),new ArrayContainerFactory());
				Image<UnsignedByteType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<UnsignedByteType>(hatchedImage, new UnsignedByteType(), 0);
			}
			else
			{
				ImageFactory<ByteType> factory = new ImageFactory<ByteType>(new ByteType(),new ArrayContainerFactory());
				Image<ByteType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<ByteType>(hatchedImage, new ByteType(), 0);
			}
		}
		else if (pixels instanceof short[])
		{
			if (unsigned)
			{
				ImageFactory<UnsignedShortType> factory = new ImageFactory<UnsignedShortType>(new UnsignedShortType(),new ArrayContainerFactory());
				Image<UnsignedShortType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<UnsignedShortType>(hatchedImage, new UnsignedShortType(), 0);
			}
			else
			{
				ImageFactory<ShortType> factory = new ImageFactory<ShortType>(new ShortType(),new ArrayContainerFactory());
				Image<ShortType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<ShortType>(hatchedImage, new ShortType(), 0);
			}
		}
		else if (pixels instanceof int[])
		{
			if (unsigned)
			{
				ImageFactory<UnsignedIntType> factory = new ImageFactory<UnsignedIntType>(new UnsignedIntType(),new ArrayContainerFactory());
				Image<UnsignedIntType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<UnsignedIntType>(hatchedImage, new UnsignedIntType(), 0);
			}
			else
			{
				ImageFactory<IntType> factory = new ImageFactory<IntType>(new IntType(),new ArrayContainerFactory());
				Image<IntType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<IntType>(hatchedImage, new IntType(), 0);
			}
		}
		else if (pixels instanceof long[])
		{
			if (unsigned)
			{
				throw new IllegalArgumentException("createProcessor(): unsigned long is not a supported pixel type");
			}
			else
			{
				ImageFactory<LongType> factory = new ImageFactory<LongType>(new LongType(),new ArrayContainerFactory());
				Image<LongType> hatchedImage = factory.createImage(dimensions);
				proc = new ImgLibProcessor<LongType>(hatchedImage, new LongType(), 0);
			}
		}
		else if (pixels instanceof float[])
		{
			ImageFactory<FloatType> factory = new ImageFactory<FloatType>(new FloatType(),new ArrayContainerFactory());
			Image<FloatType> hatchedImage = factory.createImage(dimensions);
			proc = new ImgLibProcessor<FloatType>(hatchedImage, new FloatType(), 0);
		}
		else if (pixels instanceof double[])
		{
			ImageFactory<DoubleType> factory = new ImageFactory<DoubleType>(new DoubleType(),new ArrayContainerFactory());
			Image<DoubleType> hatchedImage = factory.createImage(dimensions);
			proc = new ImgLibProcessor<DoubleType>(hatchedImage, new DoubleType(), 0);
		}
		else
			throw new IllegalArgumentException("createProcessor(): passed unknown type of pixels - "+pixels.getClass());
		
		proc.setPixels(pixels);
		
		return proc;
	}
	

	public static ImagePlus createImagePlus(final Image<?> img)
	{
		Cursor<?> cursor = img.createCursor();
		
		Type<?> runtimeT = cursor.getType();
		
		cursor.close();
		
		int[] dimensions = img.getDimensions();
		
		long numPlanes = ImageUtils.getTotalPlanes(dimensions);

		ImageStack stack = new ImageStack(img.getDimension(0), img.getDimension(1));
		
		for (long plane = 0; plane < numPlanes; plane++)
		{
			ImageProcessor processor = null;
			
			if (runtimeT instanceof UnsignedByteType)
			{
				processor = new ImgLibProcessor<UnsignedByteType>((Image<UnsignedByteType>)img, new UnsignedByteType(), plane);
			}
				
			if (runtimeT instanceof ByteType)
			{
				processor = new ImgLibProcessor<ByteType>((Image<ByteType>)img, new ByteType(), plane);
			}
				
			if (runtimeT instanceof UnsignedShortType)
			{
				processor = new ImgLibProcessor<UnsignedShortType>((Image<UnsignedShortType>)img, new UnsignedShortType(), plane);
			}
				
			if (runtimeT instanceof ShortType)
			{
				processor = new ImgLibProcessor<ShortType>((Image<ShortType>)img, new ShortType(), plane);
			}
				
			if (runtimeT instanceof UnsignedIntType)
			{
				processor = new ImgLibProcessor<UnsignedIntType>((Image<UnsignedIntType>)img, new UnsignedIntType(), plane);
			}
				
			if (runtimeT instanceof IntType)
			{
				processor = new ImgLibProcessor<IntType>((Image<IntType>)img, new IntType(), plane);
			}
				
			if (runtimeT instanceof LongType)
			{
				processor = new ImgLibProcessor<LongType>((Image<LongType>)img, new LongType(), plane);
			}
				
			if (runtimeT instanceof FloatType)
			{
				processor = new ImgLibProcessor<FloatType>((Image<FloatType>)img, new FloatType(), plane);
			}
				
			if (runtimeT instanceof DoubleType)
			{
				processor = new ImgLibProcessor<DoubleType>((Image<DoubleType>)img, new DoubleType(), plane);
			}
				
			if (processor == null)
				throw new IllegalArgumentException("createImagePlus(): unknown processor type requested - "+runtimeT.getClass());
			
			stack.addSlice(""+plane, processor);
		}
		
		ImagePlus imp = new ImagePlus(img.getName(), stack);
		
		// let ImageJ know what dimension we have
		
		// TODO - next calc only works for images with 5 or fewer dimensions and requires default ordering of xyzct
		//          Need to be able to say imp.setDimensions(int[] dims);
		
		int slices = 1;
		if (dimensions.length > 2)
			slices = dimensions[2];
		
		int channels = 1;
		if (dimensions.length > 3)
			channels = dimensions[3];

		int frames = 1;
		if (dimensions.length > 4)
			frames = dimensions[4];

		imp.setDimensions(channels, slices, frames);
		
		return imp;
	}
}
