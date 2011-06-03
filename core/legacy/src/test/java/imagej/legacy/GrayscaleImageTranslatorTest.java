package imagej.legacy;

import static org.junit.Assert.*;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import imagej.data.Dataset;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Axis;
import net.imglib2.img.Axes;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

import org.junit.Test;

public class GrayscaleImageTranslatorTest {

	private enum DataType {BYTE, SHORT, FLOAT}
	
	private GrayscaleImageTranslator translator = new GrayscaleImageTranslator();

	// -- helper methods --
	
	private void fill(Dataset ds) {
		ImgPlus<? extends RealType<?>> data = ds.getImgPlus();
		Cursor<? extends RealType<?>> cursor = data.cursor();
		long i = 0;
		while (cursor.hasNext()) {
			cursor.next();
			cursor.get().setReal(i++);
		}
	}
	
	// -- helper tests --
	
	private void testDataSame(Dataset ds, ImagePlus imp, int x, int y, int c, int z, int t) {
		long[] dims = ds.getDims();
		assertEquals(x, dims[0]);
		assertEquals(y, dims[1]);
		assertEquals(c, dims[2]);
		assertEquals(z, dims[3]);
		assertEquals(t, dims[4]);

		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		
		long[] pos = new long[5];
		
		for (int ti = 0; ti < t; ti++) {
			pos[4] = ti;
			for (int zi = 0; zi < z; zi++) {
				pos[3] = zi;
				for (int ci = 0; ci < c; ci++) {
					pos[2] = ci;
					int sliceNumber = ti*c*z + zi*c +ci;
					ImageProcessor proc = imp.getStack().getProcessor(sliceNumber+1);
					for (int yi = 0; yi < y; yi++) {
						pos[1] = yi;
						for (int xi = 0; xi < x; xi++) {
							pos[0] = xi;
							accessor.setPosition(pos);
							assertEquals(accessor.get().getRealDouble(), proc.getf(xi, yi), 0);
						}
					}
				}
			}
		}
	}

	private void testMetadataSame(Dataset ds, ImagePlus imp) {
		// axes
		assertEquals(ds.axis(0), Axes.X);
		assertEquals(ds.axis(1), Axes.Y);
		assertEquals(ds.axis(2), Axes.CHANNEL);
		assertEquals(ds.axis(3), Axes.Z);
		assertEquals(ds.axis(4), Axes.TIME);
		
		// type
		if (imp.getType() == ImagePlus.GRAY8)
			assertTrue(ds.getType() instanceof UnsignedByteType);
		if (imp.getType() == ImagePlus.GRAY16)
			assertTrue(ds.getType() instanceof UnsignedShortType);
		if (imp.getType() == ImagePlus.GRAY32)
			assertTrue(ds.getType() instanceof FloatType);

		// calibration
		Calibration cal = imp.getCalibration();
		assertEquals(ds.calibration(0), cal.pixelWidth, 0);
		assertEquals(ds.calibration(1), cal.pixelHeight, 0);
		assertEquals(ds.calibration(2), 1, 0);
		assertEquals(ds.calibration(3), cal.pixelDepth, 0);
		assertEquals(ds.calibration(4), cal.frameInterval, 0);
		
		// name
		assertEquals(ds.getName(), imp.getTitle());
		
		// integer
		assertEquals(!ds.isInteger(), (imp.getType() == ImagePlus.GRAY32));
		
		// color
		assertFalse(ds.isRGBMerged());
		
		// signed data flag
		assertEquals(ds.isSigned(), (imp.getType() == ImagePlus.GRAY32));
		
	}
	
	private void testImageFromIJ1(DataType type, int x, int y, int c, int z, int t) {
		
		ImagePlus imp;
		switch (type) {
			case BYTE:
				imp = NewImage.createByteImage("byte image", x, y, c*z*t, NewImage.FILL_RAMP);
				break;
			case SHORT:
				imp = NewImage.createShortImage("short image", x, y, c*z*t, NewImage.FILL_RAMP);
				break;
			case FLOAT:
				imp = NewImage.createFloatImage("float image", x, y, c*z*t, NewImage.FILL_RAMP);
				break;
			default:
				throw new IllegalStateException();
		}
		imp.setDimensions(c, z, t);
		Calibration cal = new Calibration();
		cal.pixelHeight = 3;
		cal.pixelDepth = 4;
		cal.pixelWidth = 7;
		imp.setCalibration(cal);
		Dataset ds = translator.createDataset(imp);
		testDataSame(ds, imp, x, y, c, z, t);
		testMetadataSame(ds, imp);
	}

	private void testImageFromIJ2(DataType type, int x, int y, int c, int z, int t) {
		
		Axes[] axes = new Axes[]{Axes.X, Axes.Y, Axes.CHANNEL, Axes.Z, Axes.TIME};
		
		Dataset ds;
		switch (type) {
			case BYTE:
				ds = Dataset.create(new long[]{x,y,c,z,t}, "byte image", axes, 8, false, false);
				break;
			case SHORT:
				ds = Dataset.create(new long[]{x,y,c,z,t}, "short image", axes, 16, false, false);
				break;
			case FLOAT:
				ds = Dataset.create(new long[]{x,y,c,z,t}, "float image", axes, 32, true, true);
				break;
			default:
				throw new IllegalStateException();
		}
		fill(ds);
		ds.setCalibration(3, 0);
		ds.setCalibration(7, 1);
		ds.setCalibration(1, 2);
		ds.setCalibration(9, 3);
		ds.setCalibration(1, 4);
		ImagePlus imp = translator.createLegacyImage(ds);
		testDataSame(ds, imp, x, y, c, z, t);
		testMetadataSame(ds, imp);
	}

	// -- public tests --
	
	@Test
	public void testFromIJ1() {
		int x,y,c,z,t;
		
		x = 25; y = 35; c = 1; z = 1; t = 1; 
		testImageFromIJ1(DataType.BYTE, x, y, c, z, t);

		x = 95; y = 22; c = 3; z = 5; t = 7; 
		testImageFromIJ1(DataType.BYTE, x, y, c, z, t);

		x = 80; y = 91; c = 1; z = 1; t = 1; 
		testImageFromIJ1(DataType.SHORT, x, y, c, z, t);

		x = 80; y = 48; c = 5; z = 7; t = 3; 
		testImageFromIJ1(DataType.SHORT, x, y, c, z, t);

		x = 107; y = 185; c = 1; z = 1; t = 1; 
		testImageFromIJ1(DataType.FLOAT, x, y, c, z, t);

		x = 83; y = 56; c = 7; z = 3; t = 5; 
		testImageFromIJ1(DataType.FLOAT, x, y, c, z, t);
	}

	@Test
	public void testToImageJ1() {
		int x,y,c,z,t;
		
		x = 25; y = 35; c = 1; z = 1; t = 1; 
		testImageFromIJ2(DataType.BYTE, x, y, c, z, t);

		x = 95; y = 22; c = 3; z = 5; t = 7; 
		testImageFromIJ2(DataType.BYTE, x, y, c, z, t);

		x = 80; y = 91; c = 1; z = 1; t = 1; 
		testImageFromIJ2(DataType.SHORT, x, y, c, z, t);

		x = 80; y = 48; c = 5; z = 7; t = 3; 
		testImageFromIJ2(DataType.SHORT, x, y, c, z, t);

		x = 107; y = 185; c = 1; z = 1; t = 1; 
		testImageFromIJ2(DataType.FLOAT, x, y, c, z, t);

		x = 83; y = 56; c = 7; z = 3; t = 5; 
		testImageFromIJ2(DataType.FLOAT, x, y, c, z, t);
	}

	private boolean allNull(Axis[] axes) {
		for (Axis axis : axes)
			if (axis != null)
				return false;
		return true;
	}
	
	private boolean repeated(Axis[] axes) {
		int cCount = 0, zCount = 0, tCount = 0;
		for (Axis axis : axes) {
			if (axis == Axes.CHANNEL) cCount++;
			if (axis == Axes.Z) zCount++;
			if (axis == Axes.TIME) tCount++;
		}
		return (cCount > 1 || zCount > 1 || tCount > 1);
	}
	
	private void testOrdering(Axis[] axes) {
		//System.out.println("Testing order : "+axes[0]+","+axes[1]+","+axes[2]);
		int nullAxes = 0;
		for (Axis axis : axes)
			if (axis == null)
				nullAxes++;
		Axis[] fullAxes = new Axis[2 + axes.length - nullAxes];
		fullAxes[0] = Axes.X;
		fullAxes[1] = Axes.Y;
		int axisIndex = 2;
		for (int i = 0; i < axes.length; i++)
			if (axes[i] != null)
				fullAxes[axisIndex++] = axes[i];
		long[] dims = new long[fullAxes.length];
		dims[0] = 3;
		dims[1] = 1;
		for (int i = 2; i < dims.length; i++)
			dims[i] = 5 + i*2;
		Dataset ds = Dataset.create(new UnsignedByteType(), dims, "temp", fullAxes);
		int cIndex = ds.getAxisIndex(Axes.CHANNEL);
		int zIndex = ds.getAxisIndex(Axes.Z);
		int tIndex = ds.getAxisIndex(Axes.TIME);
		long cCount = (cIndex < 0) ? 1 : dims[cIndex];   
		long zCount = (zIndex < 0) ? 1 : dims[zIndex];   
		long tCount = (tIndex < 0) ? 1 : dims[tIndex];
		long[] position = new long[dims.length];
		RandomAccess<? extends RealType<?>> accessor = ds.getImgPlus().randomAccess();
		for (int t = 0; t < tCount; t++) {
			if (tIndex >= 0) position[tIndex] = t;
			for (int z = 0; z < zCount; z++) {
				if (zIndex >= 0) position[zIndex] = z;
				for (int c = 0; c < cCount; c++) {
					if (cIndex >= 0) position[cIndex] = c;
					position[1] = 0;
					position[0] = 0;
					accessor.setPosition(position);
					accessor.get().setReal(c);
					position[0] = 1;
					accessor.setPosition(position);
					accessor.get().setReal(z);
					position[0] = 2;
					accessor.setPosition(position);
					accessor.get().setReal(t);
				}
			}
		}
		ImagePlus imp = translator.createLegacyImage(ds);
		for (int t = 0; t < tCount; t++) {
			if (tIndex >= 0) position[tIndex] = t;
			for (int z = 0; z < zCount; z++) {
				if (zIndex >= 0) position[zIndex] = z;
				for (int c = 0; c < cCount; c++) {
					if (cIndex >= 0) position[cIndex] = c;
					imp.setPositionWithoutUpdate(c+1, z+1, t+1);
					ImageProcessor proc = imp.getProcessor();
					position[1] = 0;
					position[0] = 0;
					accessor.setPosition(position);
					assertEquals(accessor.get().getRealDouble(), proc.get(0,0), 0);
					position[0] = 1;
					accessor.setPosition(position);
					assertEquals(accessor.get().getRealDouble(), proc.get(1,0), 0);
					position[0] = 2;
					accessor.setPosition(position);
					assertEquals(accessor.get().getRealDouble(), proc.get(2,0), 0);
				}
			}
		}
	}
	
	@Test
	public void testAxisOrderingIJ2DatasetToImageJ1() {
		Axis[] axes = new Axis[]{null, Axes.CHANNEL, Axes.Z, Axes.TIME};
		for (Axis outer : axes) {
			for (Axis middle : axes) {
				for (Axis inner : axes) {
					if (allNull(new Axis[]{outer,middle,inner})) continue;
					if (repeated(new Axis[]{outer,middle,inner})) continue;
					testOrdering(new Axis[]{outer,middle,inner});
				}
			}
		}
	}
}
