package imagej;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imagej.SampleInfo.ValueType;
import imagej.process.ImgLibProcessor;
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

/** SampleManager manages the information related to all supported types in ImageJ */
public class SampleManager {

	/** the internal list of imagej type sample data info */
	static SampleInfo[] sampleInfoArray;

	@SuppressWarnings("rawtypes")
	/** the internal list of imglib type data info */
	static RealType[] realTypeArray;
	
	/** initialize the type lists */
	static
	{
		sampleInfoArray = new SampleInfo[ValueType.values().length];
		
		sampleInfoArray[ValueType.BYTE.ordinal()] = new Sample8BitSigned();
		sampleInfoArray[ValueType.UBYTE.ordinal()] = new Sample8BitUnsigned();
		sampleInfoArray[ValueType.SHORT.ordinal()] = new Sample16BitSigned();
		sampleInfoArray[ValueType.USHORT.ordinal()] = new Sample16BitUnsigned();
		sampleInfoArray[ValueType.INT.ordinal()] = new Sample32BitSigned();
		sampleInfoArray[ValueType.UINT.ordinal()] = new Sample32BitUnsigned();
		sampleInfoArray[ValueType.FLOAT.ordinal()] = new Sample32BitFloat();
		sampleInfoArray[ValueType.LONG.ordinal()] = new Sample64BitSigned();
		sampleInfoArray[ValueType.DOUBLE.ordinal()] = new Sample64BitFloat();
		
		realTypeArray = new RealType[ValueType.values().length];

		realTypeArray[ValueType.BYTE.ordinal()] = new ByteType();
		realTypeArray[ValueType.UBYTE.ordinal()] = new UnsignedByteType();
		realTypeArray[ValueType.SHORT.ordinal()] = new ShortType();
		realTypeArray[ValueType.USHORT.ordinal()] = new UnsignedShortType();
		realTypeArray[ValueType.INT.ordinal()] = new IntType();
		realTypeArray[ValueType.UINT.ordinal()] = new UnsignedIntType();
		realTypeArray[ValueType.FLOAT.ordinal()] = new FloatType();
		realTypeArray[ValueType.LONG.ordinal()] = new LongType();
		realTypeArray[ValueType.DOUBLE.ordinal()] = new DoubleType();
	}
	
	/** make this class noninstantiable */
	private SampleManager() {}
	

	/** get the ValueType (BYTE,SHOURT,UINT,etc.) associated with an ImgLibType. Right now there is a one to one correspondance
	 *  between imglib and IJ. If this changes in the future this method is defined to return the subsample ValueType.
	 */
	public static ValueType getValueType(RealType<?> imglib)
	{
		ValueType valueType;
		
		if (imglib instanceof ByteType)
			valueType = ValueType.BYTE; 
		else if (imglib instanceof UnsignedByteType)
			valueType = ValueType.UBYTE; 
		else if (imglib instanceof ShortType)
			valueType = ValueType.SHORT; 
		else if (imglib instanceof UnsignedShortType)
			valueType = ValueType.USHORT; 
		else if (imglib instanceof IntType)
			valueType = ValueType.INT; 
		else if (imglib instanceof UnsignedIntType)
			valueType = ValueType.UINT; 
		else if (imglib instanceof LongType)
			valueType = ValueType.LONG; 
		else if (imglib instanceof FloatType)
			valueType = ValueType.FLOAT; 
		else if (imglib instanceof DoubleType)
			valueType = ValueType.DOUBLE; 
		else
			throw new IllegalArgumentException("unknown Imglib type : "+imglib);
		
		return valueType;
	}
	
	/** get an imglib type from a IJ ValueType */
	public static RealType<?> getRealType(ValueType type)
	{
		return realTypeArray[type.ordinal()];
	}
	
	/** get a SampleInfo associated with a ValueType */
	public static SampleInfo getSampleInfo(ValueType type)
	{
		return sampleInfoArray[type.ordinal()];
	}

	/** get the ValueType associated with an ImagePlus. Calls ImagePlus::getProcessor(). */
	public static ValueType getValueType(ImagePlus imp)
	{
		return getValueType(imp.getProcessor());
	}
	
	/** get the ValueType associated with an ImageProcessor */
	public static ValueType getValueType(ImageProcessor proc)
	{
		if (proc instanceof ImgLibProcessor<?>)
			return getValueType(((ImgLibProcessor<?>)proc).getType());

		if (proc instanceof ByteProcessor)
			return ValueType.UBYTE;

		if (proc instanceof ShortProcessor)
			return ValueType.USHORT;

		if (proc instanceof FloatProcessor)
			return ValueType.FLOAT;
		
		if (proc instanceof ColorProcessor)
			return ValueType.UINT;
		
		throw new IllegalArgumentException("unknown processor type");
	}
	
	/** get the SampleInfo assocaited with a SampleInfo name. Returns null if not found. */
	public static SampleInfo findSampleInfo(String name)
	{
		if (name == null)
			return null;
		
		for (SampleInfo s : sampleInfoArray)
			if (name.equals(s.getName()))
				return s;
		
		return null;
	}
	
	// *************** Private helpers follow **********************************************************
	
	/** helper function - return the total number of bits in a sample described by a SampleInfo object */
	private static int calcNumBits(SampleInfo s)
	{
		return s.getNumValues() * s.getNumBitsPerValue();
	}

	/** SampleInfo that describes IJ's 8 bit signed type */
	private static class Sample8BitSigned implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.BYTE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 8; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return true; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return true; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "8-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 8 bit unsigned type */
	private static class Sample8BitUnsigned implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.UBYTE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 8; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return false; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return true; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "8-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 16 bit signed type */
	private static class Sample16BitSigned implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.SHORT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 16; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return true; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return true; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "16-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 16 bit unsigned type */
	private static class Sample16BitUnsigned implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.USHORT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 16; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return false; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return true; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "16-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 32 bit signed type */
	private static class Sample32BitSigned implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.INT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return true; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return true; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "32-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 32 bit unsigned type */
	private static class Sample32BitUnsigned implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.UINT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return false; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return true; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "32-bit unsigned"; }
	}
	
	/** SampleInfo that describes IJ's 32 bit float type */
	private static class Sample32BitFloat implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.FLOAT; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 32; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return true; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return false; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "32-bit float"; }
	}
	
	/** SampleInfo that describes IJ's 64 bit signed type */
	private static class Sample64BitSigned implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.LONG; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 64; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return true; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return true; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "64-bit signed"; }
	}
	
	/** SampleInfo that describes IJ's 64 bit float type */
	private static class Sample64BitFloat implements SampleInfo
	{
		public ValueType getValueType() { return ValueType.DOUBLE; }

		public int getNumValues() { return 1; }

		public int getNumBitsPerValue() { return 64; }

		public int getNumBits() { return calcNumBits(this); }

		public boolean isSigned() { return true; }
		
		public boolean isUnsigned() { return !isSigned(); }
		
		public boolean isIntegral() { return false; }
		
		public boolean isFloat() { return !isIntegral(); }

		public String getName() { return "64-bit float"; }
	}
}
