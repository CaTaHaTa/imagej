package imagej.imglib.process;

import imagej.DataType;
import imagej.Utils;
import imagej.function.unary.CopyUnaryFunction;
import imagej.imglib.process.operation.BinaryAssignOperation;
import imagej.imglib.process.operation.GetPlaneOperation;
import imagej.process.Index;

import mpicbg.imglib.container.Container;
import mpicbg.imglib.container.ContainerFactory;
import mpicbg.imglib.container.basictypecontainer.PlanarAccess;
import mpicbg.imglib.container.basictypecontainer.array.ArrayDataAccess;
import mpicbg.imglib.container.basictypecontainer.array.ByteArray;
import mpicbg.imglib.container.basictypecontainer.array.CharArray;
import mpicbg.imglib.container.basictypecontainer.array.DoubleArray;
import mpicbg.imglib.container.basictypecontainer.array.FloatArray;
import mpicbg.imglib.container.basictypecontainer.array.IntArray;
import mpicbg.imglib.container.basictypecontainer.array.LongArray;
import mpicbg.imglib.container.basictypecontainer.array.ShortArray;
import mpicbg.imglib.cursor.Cursor;
import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.image.ImageFactory;
import mpicbg.imglib.type.logic.BitType;
import mpicbg.imglib.type.numeric.RealType;
import mpicbg.imglib.type.numeric.integer.ByteType;
import mpicbg.imglib.type.numeric.integer.IntType;
import mpicbg.imglib.type.numeric.integer.LongType;
import mpicbg.imglib.type.numeric.integer.ShortType;
import mpicbg.imglib.type.numeric.integer.Unsigned12BitType;
import mpicbg.imglib.type.numeric.integer.UnsignedByteType;
import mpicbg.imglib.type.numeric.integer.UnsignedIntType;
import mpicbg.imglib.type.numeric.integer.UnsignedShortType;
import mpicbg.imglib.type.numeric.real.DoubleType;
import mpicbg.imglib.type.numeric.real.FloatType;

/** this class designed to hold functionality that could be migrated to imglib */
public class ImageUtils
{
	public static final String X = "X";
	public static final String Y = "Y";
	public static final String Z = "Z";
	public static final String TIME = "Time";
	public static final String CHANNEL = "Channel";

	// ***************** public methods  **************************************************

	/** returns the total number of samples in an imglib image. Notice its of long type unlike
		imglib's image.getNumPixels()
	*/
	public static long getTotalSamples(Image<?> image)
	{
		return Utils.getTotalSamples(image.getDimensions());
	}

	/** gets the imglib type of an imglib image */
	public static RealType<?> getType(Image<?> image)
	{
		Cursor<?> cursor = image.createCursor();
		RealType<?> type = (RealType<?>) cursor.getType();
		cursor.close();
		return type;
	}

	/** copies a plane of data of specified size from an imglib image to an output array of doubles
	 *
	 * @param image - the image we want to pull the data from
	 * @param w - the desired width
	 * @param h - the desire height
	 * @param planePos - the position of the plane within the imglib image
	 * @return the sample data as an array of doubles
	 */
	@SuppressWarnings({"rawtypes"})
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

	/** Obtains planar access instance backing the given image, if any. */
	@SuppressWarnings("unchecked")
	public static PlanarAccess<ArrayDataAccess<?>> getPlanarAccess(Image<?> im) {
		PlanarAccess<ArrayDataAccess<?>> planarAccess = null;
		final Container<?> container = im.getContainer();
		if (container instanceof PlanarAccess<?>) {
			planarAccess = (PlanarAccess<ArrayDataAccess<?>>) container;
		}
		return planarAccess;
	}

  /**
	 * Gets the plane at the given position from the specified image,
	 * by reference if possible.
	 *
	 * @param <T> Type of image.
	 * @param im Image from which to extract the plane.
	 * @param planePos Dimension position of the plane in question.
	 */
	public static <T extends RealType<T>> Object getPlane(Image<T> im, int[] planePos) {
		// obtain dimensional lengths
		final int[] dims = im.getDimensions();
		if (dims.length < 2) {
			throw new IllegalArgumentException("Too few dimensions: " + dims.length);
		}

		final PlanarAccess<ArrayDataAccess<?>> planarAccess = ImageUtils.getPlanarAccess(im);
		if (planarAccess == null) {
			return getPlaneCopy(im, planePos);
		}

		// TODO: Add utility method for this to Index class.
		final int[] lengths = new int[dims.length - 2];
		for (int i=2; i<dims.length; i++) lengths[i - 2] = dims[i];
		final int no = Index.positionToRaster(lengths, planePos);
		return planarAccess.getPlane(no).getCurrentStorageArray();
	}

	/** Wraps raw primitive array in imglib Array object. */
	public static ArrayDataAccess<?> makeArray(Object array) {
		final ArrayDataAccess<?> access;
		if (array instanceof byte[]) {
			access = new ByteArray((byte[]) array);
		}
		else if (array instanceof char[]) {
			access = new CharArray((char[]) array);
		}
		else if (array instanceof double[]) {
			access = new DoubleArray((double[]) array);
		}
		else if (array instanceof int[]) {
			access = new IntArray((int[]) array);
		}
		else if (array instanceof float[]) {
			access = new FloatArray((float[]) array);
		}
		else if (array instanceof short[]) {
			access = new ShortArray((short[]) array);
		}
		else if (array instanceof long[]) {
			access = new LongArray((long[]) array);
		}
		else
			access = null;
		return access;
	}

	/**
	 * Sets the plane at the given position for the specified image,
	 * by reference if possible.
	 *
	 * @param <T> Type of image.
	 * @param im Image from which to extract the plane.
	 * @param planePos Dimension position of the plane in question.
	 * @param plane The plane data to assign.
	 * @throws ClassCastException if the plane is incompatible with the image.
	 * @throws RuntimeException if the plane cannot be set by reference.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void setPlane(Image<?> im, int[] planePos, Object plane) {
		// obtain dimensional lengths
		final int[] dims = im.getDimensions();
		if (dims.length < 2) {
			throw new IllegalArgumentException("Too few dimensions: " + dims.length);
		}

		final PlanarAccess planarAccess = ImageUtils.getPlanarAccess(im);
		if (planarAccess == null) {
			// TODO
			throw new RuntimeException("Unimplemented");
		}

		// TODO: Add utility method for this to Index class.
		final int[] lengths = new int[dims.length - 2];
		for (int i=2; i<dims.length; i++) lengths[i - 2] = dims[i];
		final int no = Index.positionToRaster(lengths, planePos);
		// TODO: move ImageOpener.makeArray somewhere more suitable.
		planarAccess.setPlane(no, makeArray(plane));
	}

	/** copies data from one image to another given origins and dimensional spans */
	public static <K extends RealType<K>>
		void copyFromImageToImage(Image<K> srcImage, int[] srcOrigin, int[] srcSpan,
									Image<K> dstImage, int[] dstOrigin, int[] dstSpan)
	{
		CopyUnaryFunction copyFunc = new CopyUnaryFunction();

		BinaryAssignOperation<K> copier =
			new BinaryAssignOperation<K>(dstImage, dstOrigin, dstSpan, srcImage, srcOrigin, srcSpan, copyFunc);

		copier.execute();
	}

	@SuppressWarnings({"unchecked"})
	public static <K extends RealType<K>> Image<K> createImage(RealType<K> type, ContainerFactory cFact, int[] dimensions)
	{
		ImageFactory<K> factory = new ImageFactory<K>((K)type, cFact);
		return factory.createImage(dimensions);
	}

	public static int getWidth(final Image<?> img) {
		return getDimSize(img, X, 0);
	}

	public static int getHeight(final Image<?> img) {
		return getDimSize(img, Y, 1);
	}

	public static int getNChannels(final Image<?> img) {
		return getDimSize(img, CHANNEL, 2);
	}

	public static int getNSlices(final Image<?> img) {
		return getDimSize(img, Z, 3);
	}

	public static int getNFrames(final Image<?> img) {
		return getDimSize(img, TIME, 4);
	}

	public static int getDimSize(final Image<?> img, final String dimType) {
		return getDimSize(img, dimType, -1);
	}

	// ***************** private methods  **************************************************

	/**
	 * get a plane of data from an imglib image
	 * @param im - the imglib image we will be pulling plane from
	 * @param planePos - the position of the plane within the image
	 * @return an array of primitive type matching the imglib image's type
	 */
	@SuppressWarnings({"unchecked"})
	private static Object getPlaneCopy(Image<? extends RealType<?>> im, int[] planePos)
	{
		RealType<?> type = getType(im);

		if (type instanceof BitType)
			return GetPlaneOperation.getPlaneAs((Image<ByteType>)im, planePos, DataType.BIT);

		if (type instanceof ByteType)
			return GetPlaneOperation.getPlaneAs((Image<ByteType>)im, planePos, DataType.BYTE);

		if (type instanceof UnsignedByteType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedByteType>)im, planePos, DataType.UBYTE);

		if (type instanceof Unsigned12BitType)
			return GetPlaneOperation.getPlaneAs((Image<Unsigned12BitType>)im, planePos, DataType.UINT12);

		if (type instanceof ShortType)
			return GetPlaneOperation.getPlaneAs((Image<ShortType>)im, planePos, DataType.SHORT);

		if (type instanceof UnsignedShortType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedShortType>)im, planePos, DataType.USHORT);

		if (type instanceof IntType)
			return GetPlaneOperation.getPlaneAs((Image<IntType>)im, planePos, DataType.INT);

		if (type instanceof UnsignedIntType)
			return GetPlaneOperation.getPlaneAs((Image<UnsignedIntType>)im, planePos, DataType.UINT);

		if (type instanceof LongType)
			return GetPlaneOperation.getPlaneAs((Image<LongType>)im, planePos, DataType.LONG);

		if (type instanceof FloatType)
			return GetPlaneOperation.getPlaneAs((Image<FloatType>)im, planePos, DataType.FLOAT);

		if (type instanceof DoubleType)
			return GetPlaneOperation.getPlaneAs((Image<DoubleType>)im, planePos, DataType.DOUBLE);

		throw new IllegalArgumentException("getPlaneCopy(): unsupported type - "+type.getClass());
	}

	/** Converts the given image name back to a list of dimensional axis types. */
	private static String[] decodeTypes(String name) {
		final int lBracket = name.lastIndexOf(" [");
		if (lBracket < 0) return new String[0];
		final int rBracket = name.lastIndexOf("]");
		if (rBracket < lBracket) return new String[0];
		return name.substring(lBracket + 2, rBracket).split(" ");
	}

	private static int getDimSize(final Image<?> img, final String dimType, final int defaultIndex)
	{
		final String imgName = img.getName();
		final int[] dimensions = img.getDimensions();
		final String[] dimTypes = decodeTypes(imgName);
		int size = 1;
		if (dimTypes.length == dimensions.length) {
			for (int i = 0; i < dimTypes.length; i++) {
				if (dimType.equals(dimTypes[i])) size *= dimensions[i];
			}
		}
		else {
			// assume default ordering
			if (defaultIndex >= 0 && defaultIndex < dimensions.length) {
				size = dimensions[defaultIndex];
			}
		}
		return size;
	}

}
