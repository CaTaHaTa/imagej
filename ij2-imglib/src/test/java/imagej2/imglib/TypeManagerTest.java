package imagej2.imglib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import imagej2.SampleInfo;
import imagej2.SampleInfo.ValueType;
import imagej2.imglib.TypeManager;
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

import org.junit.Test;


public class TypeManagerTest {

	@Test
	public void testGetRealType()
	{
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.BIT) instanceof BitType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.BYTE) instanceof ByteType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.UBYTE) instanceof UnsignedByteType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.UINT12) instanceof Unsigned12BitType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.SHORT) instanceof ShortType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.USHORT) instanceof UnsignedShortType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.INT) instanceof IntType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.UINT) instanceof UnsignedIntType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.FLOAT) instanceof FloatType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.DOUBLE) instanceof DoubleType);
		assertTrue(TypeManager.getRealType(SampleInfo.ValueType.LONG) instanceof LongType);
	}

	@Test
	public void testGetValueTypeRealType()
	{
		assertEquals(ValueType.BIT, TypeManager.getValueType(new BitType()));
		assertEquals(ValueType.BYTE, TypeManager.getValueType(new ByteType()));
		assertEquals(ValueType.UBYTE, TypeManager.getValueType(new UnsignedByteType()));
		assertEquals(ValueType.UINT12, TypeManager.getValueType(new Unsigned12BitType()));
		assertEquals(ValueType.SHORT, TypeManager.getValueType(new ShortType()));
		assertEquals(ValueType.USHORT, TypeManager.getValueType(new UnsignedShortType()));
		assertEquals(ValueType.INT, TypeManager.getValueType(new IntType()));
		assertEquals(ValueType.UINT, TypeManager.getValueType(new UnsignedIntType()));
		assertEquals(ValueType.FLOAT, TypeManager.getValueType(new FloatType()));
		assertEquals(ValueType.DOUBLE, TypeManager.getValueType(new DoubleType()));
		assertEquals(ValueType.LONG, TypeManager.getValueType(new LongType()));
	}
	@Test
	public void testIsUnsignedType() {
		
		assertFalse(TypeManager.isUnsignedType(new ByteType()));
		assertFalse(TypeManager.isUnsignedType(new ShortType()));
		assertFalse(TypeManager.isUnsignedType(new IntType()));
		assertFalse(TypeManager.isUnsignedType(new LongType()));
		assertFalse(TypeManager.isUnsignedType(new FloatType()));
		assertFalse(TypeManager.isUnsignedType(new DoubleType()));
		
		assertTrue(TypeManager.isUnsignedType(new UnsignedByteType()));
		assertTrue(TypeManager.isUnsignedType(new UnsignedShortType()));
		assertTrue(TypeManager.isUnsignedType(new UnsignedIntType()));
		assertTrue(TypeManager.isUnsignedType(new Unsigned12BitType()));
	}

	@Test
	public void testIsIntegralType() {
		assertTrue(TypeManager.isIntegralType(new ByteType()));
		assertTrue(TypeManager.isIntegralType(new ShortType()));
		assertTrue(TypeManager.isIntegralType(new IntType()));
		assertTrue(TypeManager.isIntegralType(new LongType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedByteType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedShortType()));
		assertTrue(TypeManager.isIntegralType(new UnsignedIntType()));
		assertTrue(TypeManager.isIntegralType(new Unsigned12BitType()));

		assertFalse(TypeManager.isIntegralType(new FloatType()));
		assertFalse(TypeManager.isIntegralType(new DoubleType()));
	}

	@Test
	public void testBoundValueToType()
	{
		RealType<?> type;
		
		type = new ByteType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Byte.MIN_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Byte.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new UnsignedByteType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(255,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);

		type = new ShortType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(-32768,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(32767,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new UnsignedShortType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(65535,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new IntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Integer.MIN_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Integer.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new UnsignedIntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals((1L<<32)-1,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new FloatType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(-Float.MAX_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Float.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);

		type = new DoubleType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(-Double.MAX_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Double.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new LongType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(Long.MIN_VALUE,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(Long.MAX_VALUE,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
		
		type = new Unsigned12BitType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertEquals(0,TypeManager.boundValueToType(type, -Double.MAX_VALUE),0);
		assertEquals(4095,TypeManager.boundValueToType(type, Double.MAX_VALUE),0);
	}
	
	@Test
	public void testSameKind()
	{
		RealType<?> type1, type2;

		type1 = new ByteType();
		
		type2 = new ByteType();
		assertTrue(TypeManager.sameKind(type1,type2));
		
		type2 = new UnsignedByteType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new ShortType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new UnsignedShortType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new IntType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new UnsignedIntType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new FloatType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new DoubleType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new LongType();
		assertFalse(TypeManager.sameKind(type1,type2));
		
		type2 = new Unsigned12BitType();
		assertFalse(TypeManager.sameKind(type1,type2));
	}

	@Test
	public void testValidValue()
	{
		RealType<?> type;
		
		type = new ByteType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Byte.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Byte.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Byte.MAX_VALUE+1));
		assertFalse(TypeManager.validValue(type, Byte.MIN_VALUE-1));
		
		type = new UnsignedByteType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, 255));
		assertTrue(TypeManager.validValue(type, 0));
		assertFalse(TypeManager.validValue(type, 256));
		assertFalse(TypeManager.validValue(type, -1));

		type = new ShortType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Short.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Short.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Short.MAX_VALUE+1));
		assertFalse(TypeManager.validValue(type, Short.MIN_VALUE-1));
		
		type = new UnsignedShortType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, 65535));
		assertTrue(TypeManager.validValue(type, 0));
		assertFalse(TypeManager.validValue(type, 65536));
		assertFalse(TypeManager.validValue(type, -1));
		
		type = new IntType();
		assertEquals(0,TypeManager.boundValueToType(type, 0),0);
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Integer.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Integer.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Integer.MAX_VALUE+1.0));
		assertFalse(TypeManager.validValue(type, Integer.MIN_VALUE-1.0));
		
		type = new UnsignedIntType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, (1L<<32)-1));
		assertTrue(TypeManager.validValue(type, 0));
		assertFalse(TypeManager.validValue(type, Math.pow(2, 32)+1));
		assertFalse(TypeManager.validValue(type, -1));
		
		type = new DoubleType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Double.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, -Double.MAX_VALUE));
		
		type = new FloatType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Float.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, -Float.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, Double.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, -Double.MAX_VALUE));

		type = new LongType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, Long.MAX_VALUE));
		assertTrue(TypeManager.validValue(type, Long.MIN_VALUE));
		assertFalse(TypeManager.validValue(type, Double.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, -Double.MAX_VALUE));

		type = new Unsigned12BitType();
		assertTrue(TypeManager.validValue(type, 0));
		assertTrue(TypeManager.validValue(type, 4095));
		assertFalse(TypeManager.validValue(type, -1));
		assertFalse(TypeManager.validValue(type, 4096));
		assertFalse(TypeManager.validValue(type, -Double.MAX_VALUE));
		assertFalse(TypeManager.validValue(type, Double.MAX_VALUE));
	}
}
