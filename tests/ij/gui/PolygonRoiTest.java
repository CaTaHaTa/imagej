package ij.gui;

import static org.junit.Assert.*;
import org.junit.Test;

import java.awt.Polygon;

import ij.*;
import ij.process.*;
import ij.measure.Calibration;

public class PolygonRoiTest {

	enum Fit {NONE,SPLINE};
	
	PolygonRoi p;
	

	// helper
	private int extent(int[] vals)
	{
		int min = 0;
		int max = 0;
		
		for (int i = 0; i < vals.length; i++)
		{
			if (i == 0)
			{
				max = vals[0];
				min = vals[0];
			}
			else
			{
				if (vals[i] < min) min = vals[i];
				if (vals[i] > max) max = vals[i];
			}
		}
		
		return max - min;
	}

	// helper
	private ImagePlus getCalibratedImagePlus()
	{
		ImagePlus ip = new ImagePlus("Zakky",new ShortProcessor(5,5,new short[5*5],null));
		Calibration cal = new Calibration();
		cal.pixelWidth = 14.1;
		cal.pixelHeight = 8.7;
		ip.setCalibration(cal);
		return ip;
	}
	
	// helper
	private void validateCons1(int[] xs, int[] ys, int type)
	{
		int len = xs.length;
		
		p = new PolygonRoi(xs,ys,len,type);
		
		assertNotNull(p);
		assertEquals(type,p.getType());
		assertEquals(len,p.nPoints);
		
		if (type == Roi.TRACED_ROI) {
			assertSame(xs,p.xp);
			assertSame(ys,p.yp);
		}
		else {
			assertNotSame(xs,p.xp);
			assertNotSame(ys,p.yp);
		}
		
		if ((type != Roi.POINT) &&
				((len < 2) ||
						((type != Roi.FREELINE && type != Roi.POLYLINE && type != Roi.ANGLE) &&
						  ((len < 3) || (extent(ys) == 0) || (extent(ys)==0)))))
		{
			assertEquals(Roi.CONSTRUCTING,p.getState());
			if (len > 0)
			{
				assertEquals(xs[0],p.x);
				assertEquals(ys[0],p.y);
				assertEquals(xs[0],p.xp[0]);
				assertEquals(ys[0],p.yp[0]);
			}
		}
		else
		{
			assertEquals(Roi.NORMAL,p.getState());
			for (int i = 0; i < len; i++) {
				assertEquals(xs[i],p.x+p.xp[i]);
				assertEquals(ys[i],p.y+p.yp[i]);
			}
		}
	}

	// helper: same as previous method
	private void validateCons2(Polygon poly, int type)
	{
		int len = poly.npoints;
		
		p = new PolygonRoi(poly,type);
		
		assertNotNull(p);
		assertEquals(type,p.getType());
		assertEquals(len,p.nPoints);
		
		if (type == Roi.TRACED_ROI) {
			assertSame(poly.xpoints,p.xp);
			assertSame(poly.ypoints,p.yp);
		}
		else {
			assertNotSame(poly.xpoints,p.xp);
			assertNotSame(poly.ypoints,p.yp);
		}
		
		if ((type != Roi.POINT) &&
				((len < 2) ||
						((type != Roi.FREELINE && type != Roi.POLYLINE && type != Roi.ANGLE) &&
						  ((len < 3) || (extent(poly.xpoints) == 0) || (extent(poly.ypoints)==0)))))
		{
			assertEquals(Roi.CONSTRUCTING,p.getState());
			if (len > 0)
			{
				assertEquals(poly.xpoints[0],p.x);
				assertEquals(poly.ypoints[0],p.y);
				assertEquals(poly.xpoints[0],p.xp[0]);
				assertEquals(poly.ypoints[0],p.yp[0]);
			}
		}
		else
		{
			assertEquals(Roi.NORMAL,p.getState());
			for (int i = 0; i < len; i++) {
				assertEquals(poly.xpoints[i],p.x+p.xp[i]);
				assertEquals(poly.ypoints[i],p.y+p.yp[i]);
			}
		}
	}

	// helper
	private void validateConvexHull(int[] xs, int[] ys, int[] chXs, int[] chYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		Polygon poly = p.getConvexHull();
		assertNotNull(poly);
		assertEquals(chXs.length,poly.npoints);
		for (int i = 0; i < chXs.length; i++)
		{
			assertEquals(chXs[i],poly.xpoints[i]);
			assertEquals(chYs[i],poly.ypoints[i]);
		}
	}
	
	// regular version of validator for getPolygon()
	private void validateGetPolygon(Fit fit, int[] xs, int[] ys, int[] pXs, int[] pYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		if (fit == Fit.SPLINE)
			p.fitSpline(5);
		Polygon poly = p.getPolygon();
		assertEquals(pXs.length,poly.npoints);
		for (int i = 0; i < pXs.length; i++)
		{
			assertEquals(pXs[i],poly.xpoints[i]);
			assertEquals(pYs[i],poly.ypoints[i]);
		}
	}
	
	// regular version of validator for getFloatPolygon()
	private void validateGetFloatPolygon1(int[] xs, int[] ys, float[] pXs, float[] pYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		FloatPolygon poly = p.getFloatPolygon();
		assertEquals(pXs.length,poly.npoints);
		for (int i = 0; i < pXs.length; i++)
		{
			assertEquals(pXs[i],poly.xpoints[i],Assert.FLOAT_TOL);
			assertEquals(pYs[i],poly.ypoints[i],Assert.FLOAT_TOL);
		}
	}
	
	// spline version of validator for getFloatPolygon()
	private void validateGetFloatPolygon2(int[] xs, int[] ys, float[] pXs, float[] pYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		p.fitSpline(5);
		FloatPolygon poly = p.getFloatPolygon();
		assertEquals(pXs.length,poly.npoints);
		for (int i = 0; i < pXs.length; i++)
		{
			assertEquals(pXs[i],poly.xpoints[i],Assert.FLOAT_TOL);
			assertEquals(pYs[i],poly.ypoints[i],Assert.FLOAT_TOL);
		}
	}

	// helper
	private void validateClone(int[] xs, int[] ys, int type)
	{
		p = new PolygonRoi(xs,ys,xs.length,type);
		PolygonRoi roi = (PolygonRoi)p.clone();
		
		assertNotNull(roi);
		assertTrue(roi.equals(p));
	}
	
	// helper
	private void validateDrawPixels(Fit fit, int stroke, int[] xs, int[] ys, int type, int[] expectedNonZeroes)
	{
		int size = 10;
		int refVal = 33;
		ImageProcessor proc = new ByteProcessor(25,25);
		proc.setColor(refVal);
		p = new PolygonRoi(xs,ys,xs.length,type);
		if (fit == Fit.SPLINE)
			p.fitSpline(4);
		if (stroke != -1)
			p.setStrokeWidth(stroke);
		p.drawPixels(proc);
		//RoiHelpers.printValues(proc);
		for (int i = 0; i < size*size; i++)
			if (RoiHelpers.find(i,expectedNonZeroes))
				assertEquals(refVal,proc.get(i));
			else
				assertEquals(0,proc.get(i));
	}
	
	// helper
	private void validateContains(Fit fit, int xs[], int ys[], int x, int y, boolean val)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		if (fit == Fit.SPLINE)
			p.fitSpline(5);
		assertEquals(val,p.contains(x,y));
	}

	// helper
	private void validateGetMask(Fit fit, int[] xs, int[] ys, int[] expectedNonZeroes)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		if (fit == Fit.SPLINE)
			p.fitSpline(5);
		ImageProcessor proc = p.getMask();
		//RoiHelpers.printValues(proc);
		for (int i = 0; i < proc.getWidth()*proc.getHeight(); i++)
			if (RoiHelpers.find(i,expectedNonZeroes))
				assertEquals(255,proc.get(i));
			else
				assertEquals(0,proc.get(i));
	}

	// helper
	private void printIntArr(String label, int[] vals)
	{
		System.out.println(label);
		for (int i = 0; i < vals.length; i++)
			System.out.print(","+vals[i]);
		System.out.println();
	}
	
	// helper
	private void validateFitSplineInt(int[] xs, int[]ys, int numSplinePts, int[] spXs, int[] spYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYLINE);
		p.fitSpline(numSplinePts);
		//printIntArr("X",p.getXCoordinates());
		//printIntArr("Y",p.getYCoordinates());
		assertTrue(p.isSplineFit());
		assertEquals(numSplinePts,p.getNCoordinates());
		assertArrayEquals(spXs,p.getXCoordinates());
		assertArrayEquals(spYs,p.getYCoordinates());
	}

	// helper
	private void validateFitSpline(int[] xs, int[] ys)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYLINE);

		double length = p.getUncalibratedLength();
		
		int evaluationPoints = (int)(length/2.0);
		
		if (evaluationPoints < 100)
			evaluationPoints = 100;
		
		p.fitSpline();
		
		assertEquals(evaluationPoints,p.getNCoordinates());
		
		// the rest is calling fitSpline(int) and has already been tested
	}

	// helper
	private void validateFitSplineStraighten(int[] xs, int[] ys, int[] spXs, int[] spYs)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYLINE);
		p.fitSplineForStraightening();
		//printIntArr("X",p.getXCoordinates());
		//printIntArr("Y",p.getYCoordinates());
		assertArrayEquals(spXs,p.getXCoordinates());
		assertArrayEquals(spYs,p.getYCoordinates());
	}
	
	// helper
	private void validateUncalibLength(int[] xs, int[] ys, double len)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYLINE);
		assertEquals(len,p.getUncalibratedLength(),Assert.DOUBLE_TOL);
	}
	
	// helper
	private void validateGetAngle(int xs[], int ys[], double ang)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		p.getAngleAsString();  // force angle to get set
		assertEquals(ang,p.getAngle(),Assert.DOUBLE_TOL);
	}
	
	// helper
	private void validateGetNonSpline(int[] xs, int[] ys)
	{
		p = new PolygonRoi(xs,ys,xs.length,Roi.POLYGON);
		Polygon poly = p.getNonSplineCoordinates();
		for (int i = 0; i < xs.length; i++)
			assertEquals(xs[i]-xs[0], poly.xpoints[i]);
		for (int i = 0; i < ys.length; i++)
			assertEquals(ys[i]-ys[0], poly.ypoints[i]);
	}
	
	// ***********************************  TESTS  *****************************************************
	
	@Test
	public void testPolygonRoiIntArrayIntArrayIntInt() {
		// vary type
		validateCons1(new int[]{1},new int[]{4},Roi.POLYGON);
		validateCons1(new int[]{1},new int[]{4},Roi.FREEROI);
		validateCons1(new int[]{1},new int[]{4},Roi.TRACED_ROI);
		validateCons1(new int[]{1},new int[]{4},Roi.POLYLINE);
		validateCons1(new int[]{1},new int[]{4},Roi.FREELINE);
		validateCons1(new int[]{1},new int[]{4},Roi.ANGLE);
		validateCons1(new int[]{1},new int[]{4},Roi.POINT);

		// attempt type other than above ones
		try {
			validateCons1(new int[]{1,2,3,4},new int[]{5,6,7,8},Roi.OVAL);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// vary number of input points
		validateCons1(new int[]{},new int[]{},Roi.POLYGON);
		validateCons1(new int[]{1},new int[]{1},Roi.POLYGON);
		validateCons1(new int[]{1,2},new int[]{4,5},Roi.POLYGON);
		validateCons1(new int[]{1,2,3},new int[]{4,5,6},Roi.POLYGON);
		validateCons1(new int[]{1,2,3,4},new int[]{7,5,3,1},Roi.POLYGON);

		// vary both inputs
		validateCons1(new int[]{},new int[]{},Roi.FREELINE);
		validateCons1(new int[]{1},new int[]{1},Roi.POINT);
		validateCons1(new int[]{1,2},new int[]{4,5},Roi.POLYLINE);
		validateCons1(new int[]{1,2,3},new int[]{4,5,6},Roi.FREEROI);
		validateCons1(new int[]{1,2,3,4},new int[]{7,5,3,1},Roi.POLYGON);
	}
	
	@Test
	public void testPolygonRoiPolygonInt() {
		// vary type
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.FREEROI);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.TRACED_ROI);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.POLYLINE);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.FREELINE);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.ANGLE);
		validateCons2(new Polygon(new int[]{1},new int[]{4},1),Roi.POINT);

		// attempt type other than above ones
		try {
			validateCons2(new Polygon(new int[]{1,2,3,4},new int[]{5,6,7,8},4),Roi.OVAL);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// vary number of input points
		validateCons2(new Polygon(new int[]{},new int[]{},0),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1},new int[]{1},1),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1,2},new int[]{4,5},2),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1,2,3},new int[]{4,5,6},3),Roi.POLYGON);
		validateCons2(new Polygon(new int[]{1,2,3,4},new int[]{7,5,3,1},4),Roi.POLYGON);

		// vary both inputs
		validateCons2(new Polygon(new int[]{},new int[]{},0),Roi.FREELINE);
		validateCons2(new Polygon(new int[]{1},new int[]{1},1),Roi.POINT);
		validateCons2(new Polygon(new int[]{1,2},new int[]{4,5},2),Roi.POLYLINE);
		validateCons2(new Polygon(new int[]{1,2,3},new int[]{4,5,6},3),Roi.FREEROI);
		validateCons2(new Polygon(new int[]{1,2,3,4},new int[]{7,5,3,1},4),Roi.POLYGON);
	}

	@Test
	public void testPolygonRoiIntIntImagePlus() {
		// note - can't test this method. its expecting an active imagecanvas (gui) to run without crashing
	}

	@Test
	public void testGetLength() {
		// traced roi subcase
		p = new PolygonRoi(new int[]{1,2,3,4},new int[]{2,4,6,8},4,Roi.TRACED_ROI);
		assertEquals(15.65685,p.getLength(),Assert.DOUBLE_TOL);
		
		// npoints > 2 and FREEROI
		p = new PolygonRoi(new int[]{4,7,3,9},new int[]{1,3,5,7},4,Roi.FREEROI);
		assertEquals(15.85518,p.getLength(),Assert.DOUBLE_TOL);

		// npoints > 2 and FREELINE (and both h and w > 0)
		p = new PolygonRoi(new int[]{4,7,3},new int[]{1,3,5},3,Roi.FREELINE);
		assertEquals(4.71160,p.getLength(),Assert.DOUBLE_TOL);

		// otherwise
		
		//   with calibration: spline and POLYGON
		p = new PolygonRoi(new int[]{104,88,66,31},new int[]{10,20,30,40},4,Roi.POLYGON);
		p.setImage(getCalibratedImagePlus());
		p.fitSpline(20);
		assertEquals(2142.30092,p.getLength(),Assert.DOUBLE_TOL);
		
		//   with calibration: spline and not a POLYGON
		p = new PolygonRoi(new int[]{18,73,44,33},new int[]{40,30,20,10},4,Roi.POLYLINE);
		p.setImage(getCalibratedImagePlus());
		p.fitSpline(20);
		assertEquals(1398.21248,p.getLength(),Assert.DOUBLE_TOL);
		
		//   with calibration: no spline and POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYGON);
		p.setImage(getCalibratedImagePlus());
		assertEquals(5987.81253,p.getLength(),Assert.DOUBLE_TOL);

		//   with calibration: no spline and not a POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYLINE);
		p.setImage(getCalibratedImagePlus());
		assertEquals(3314.55616,p.getLength(),Assert.DOUBLE_TOL);

		//   no calibration: spline and POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYGON);
		p.fitSpline(20);
		assertEquals(662.80068,p.getLength(),Assert.DOUBLE_TOL);
		
		//   no calibration: spline and not a POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYLINE);
		p.fitSpline(20);
		assertEquals(344.70283,p.getLength(),Assert.DOUBLE_TOL);
		
		//   no calibration: no spline and POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYGON);
		assertEquals(638.07205,p.getLength(),Assert.DOUBLE_TOL);

		//   no calibration: no spline and not a POLYGON
		p = new PolygonRoi(new int[]{22,4,88,63},new int[]{100,200,300,400},4,Roi.POLYLINE);
		assertEquals(335.28335,p.getLength(),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetConvexHull() {
		Polygon poly;

		// 0 point poly
		validateConvexHull(new int[]{}, new int[]{}, new int[]{}, new int[]{});

		// 1 point polys
		// TODO - correct case commented out. Reported bug to Wayne. Waiting to hear back.
		//validateConvexHull(new int[]{1},new int[]{3},new int[]{1},new int[]{3});
		validateConvexHull(new int[]{1},new int[]{3},new int[]{2},new int[]{6});

		// 2 point polys
		// TODO - correct case commented out. Reported bug to Wayne. Waiting to hear back.
		//validateConvexHull(new int[]{1,7},new int[]{6,3},new int[]{1,7},new int[]{6,3});
		validateConvexHull(new int[]{1,7},new int[]{6,3},new int[]{2,8},new int[]{9,6});

		// 3 point polys
		validateConvexHull(new int[]{1,4,7},new int[]{8,3,6},new int[]{4,1,7},new int[]{3,8,6});

		// 4 point polys
		validateConvexHull(new int[]{1,3,5,7},new int[]{3,5,7,9},new int[]{1,3,5,7},new int[]{3,5,7,9});
		validateConvexHull(new int[]{1,15,23,17},new int[]{14,0,12,6},new int[]{15,1,23},new int[]{0,14,12});
	}

	@Test
	public void testGetPolygon() {
		
		// regular case
		validateGetPolygon(Fit.NONE,new int[]{},new int[]{},new int[]{},new int[]{});
		validateGetPolygon(Fit.NONE,new int[]{1},new int[]{5},new int[]{2},new int[]{10});
		validateGetPolygon(Fit.NONE,new int[]{4,1},new int[]{8,3},new int[]{5,2},new int[]{11,6});
		validateGetPolygon(Fit.NONE,new int[]{3,8,2},new int[]{7,1,5},new int[]{3,8,2},new int[]{7,1,5});
		validateGetPolygon(Fit.NONE,new int[]{17,3,31,44},new int[]{8,11,2,23},new int[]{17,3,31,44},new int[]{8,11,2,23});
		
		// spline case
		//TODO : crashes - validateGetPolygon2(new int[]{},new int[]{},new int[]{},new int[]{});
		validateGetPolygon(Fit.SPLINE,new int[]{1},new int[]{5},new int[]{2,2,2,2,2},new int[]{10,10,10,10,10});
		validateGetPolygon(Fit.SPLINE,new int[]{4,1},new int[]{8,3},new int[]{5,3,2,3,5},new int[]{11,8,6,8,11});
		validateGetPolygon(Fit.SPLINE,new int[]{3,8,2},new int[]{7,1,5},new int[]{3,8,5,1,3},new int[]{7,2,2,6,7});
		validateGetPolygon(Fit.SPLINE,new int[]{17,3,31,44},new int[]{8,11,2,23},new int[]{17,3,31,44,17},new int[]{8,11,2,23,8});
	}

	@Test
	public void testGetFloatPolygon() {
		
		// regular case
		validateGetFloatPolygon1(new int[]{},new int[]{},new float[]{},new float[]{});
		validateGetFloatPolygon1(new int[]{1},new int[]{5},new float[]{2},new float[]{10});
		validateGetFloatPolygon1(new int[]{4,1},new int[]{8,3},new float[]{5,2},new float[]{11,6});
		validateGetFloatPolygon1(new int[]{3,8,2},new int[]{7,1,5},new float[]{3,8,2},new float[]{7,1,5});
		validateGetFloatPolygon1(new int[]{17,3,31,44},new int[]{8,11,2,23},new float[]{17,3,31,44},new float[]{8,11,2,23});
		
		// spline case
		// TODO - crashes: validateGetFloatPolygon2(new int[]{},new int[]{},new float[]{},new float[]{});
		validateGetFloatPolygon2(new int[]{1},new int[]{5},
									new float[]{2,2,2,2,2},new float[]{10,10,10,10,10});
		validateGetFloatPolygon2(new int[]{4,1},new int[]{8,3},
									new float[]{5,2.9375f,2,2.9375f,5},new float[]{11,7.5625f,6,7.5625f,11});
		validateGetFloatPolygon2(new int[]{3,8,2},new int[]{7,1,5},
									new float[]{3,7.86562f,5.30000f,1.39688f,3},new float[]{7,1.58125f,2.40000f,5.89375f,7});
		validateGetFloatPolygon2(new int[]{17,3,31,44},new int[]{8,11,2,23},
									new float[]{17,3,31,44,17},new float[]{8,11,2,23,8});
	}

	@Test
	public void testClone() {
		// test a number of cases
		validateClone(new int[]{6,4,19},new int[]{22,44,1},Roi.POLYLINE);
		validateClone(new int[]{1},new int[]{8},Roi.POINT);
		validateClone(new int[]{1,2},new int[]{8,4},Roi.FREELINE);
		validateClone(new int[]{1,2,3},new int[]{8,4,7},Roi.POLYGON);
		validateClone(new int[]{4,7,-2,11},new int[]{8,32,13,15},Roi.FREEROI);
	}

	@Test
	public void testDraw() {
		// note - can't test : gui oriented
	}

	@Test
	public void testDrawPixelsImageProcessor() {
		// test various combos
		
		// FREEROI
		validateDrawPixels(Fit.NONE,-1,new int[]{1,2,3},new int[]{5,2,6},Roi.FREEROI, new int[]{52,77,101,103,126,128,152,153});
		validateDrawPixels(Fit.NONE,2,new int[]{1,2,3},new int[]{5,2,6},Roi.FREEROI, new int[]{26,27,51,52,75,76,77,78,100,101,102,103,
																							125,126,127,128,151,152,153});
		validateDrawPixels(Fit.SPLINE,-1,new int[]{1,2,3},new int[]{5,2,6},Roi.FREEROI, new int[]{51,76,77,101,102,126,128,152,153});
		
		// POLYLINE
		validateDrawPixels(Fit.NONE,-1,new int[]{6,3,8},new int[]{1,16,7},Roi.POLYLINE, new int[]{31,56,81,105,130,155,180,183,205,207,
																								229,232,254,256,279,281,304,305,329,
																								330,353,354,378,379,403});
		validateDrawPixels(Fit.NONE,2,new int[]{6,3,8},new int[]{1,16,7},Roi.POLYLINE, new int[]{5,6,30,31,55,56,79,80,81,104,105,129,
																								130,154,155,157,158,179,180,181,182,
																								183,203,204,205,206,207,228,229,230,
																								231,232,253,254,255,256,278,279,280,
																								281,303,304,305,327,328,329,330,352,
																								353,354,377,378,379,402,403});
		validateDrawPixels(Fit.SPLINE,-1,new int[]{6,3,8},new int[]{1,16,7},Roi.POLYLINE, new int[]{31,56,81,105,130,155,180,183,204,207,
																								229,232,254,256,279,281,303,305,328,
																								329,353,354,378});

		// POINT
		// next one unintuitive: nothing drawn rather than 1 point
		validateDrawPixels(Fit.NONE,-1,new int[]{7},new int[]{7},Roi.POINT, new int[]{});
		// next one unintuitive: nothing drawn
		validateDrawPixels(Fit.NONE,4,new int[]{7},new int[]{7},Roi.POINT, new int[]{});
		// next one unintuitive: why not just 3 points?
		validateDrawPixels(Fit.NONE,-1,new int[]{3,7,1},new int[]{2,2,8},Roi.POINT, new int[]{53,54,55,56,57,81,105,129,153,177,201});
		validateDrawPixels(Fit.NONE,2,new int[]{3,7,1},new int[]{2,2,8},Roi.POINT, new int[]{27,28,29,30,31,32,52,53,54,55,56,57,79,80,
																							81,103,104,105,127,128,129,151,152,153,
																							175,176,177,200,201});
		validateDrawPixels(Fit.SPLINE,-1,new int[]{3,7,1},new int[]{2,2,8},Roi.POINT, new int[]{30,31,53,54,56,80,104,128,153,177,201});
		
		// POLYGON
		// next one unintuitive: why not 9 pixels?
		validateDrawPixels(Fit.NONE,-1,new int[]{0,2,2,0},new int[]{0,0,2,2},Roi.POLYGON, new int[]{0,1,2,25,27,50,51,52});
		validateDrawPixels(Fit.NONE,3,new int[]{0,2,2,0},new int[]{0,0,2,2},Roi.POLYGON, new int[]{0,1,2,3,25,26,27,28,50,51,52,53,75,
																								76,77,78});
		validateDrawPixels(Fit.SPLINE,-1,new int[]{0,2,2,0},new int[]{0,0,2,2},Roi.POLYGON, new int[]{0,25,26,27,50,51});
		
		// TRACED_ROI
		validateDrawPixels(Fit.NONE,-1,new int[]{3,5,7},new int[]{3,5,4},Roi.TRACED_ROI, new int[]{78,79,104,105,106,107,130,131});
		validateDrawPixels(Fit.NONE,2,new int[]{3,5,7},new int[]{3,5,4},Roi.TRACED_ROI, new int[]{52,53,54,77,78,79,80,81,82,103,104,
																								105,106,107,129,130,131});
		validateDrawPixels(Fit.SPLINE,-1,new int[]{3,5,7},new int[]{3,5,4},Roi.TRACED_ROI, new int[]{78,79,104,105,106,107,129,130,131});

		// FREELINE
		validateDrawPixels(Fit.NONE,-1,new int[]{4,6,2},new int[]{2,7,6},Roi.FREELINE, new int[]{54,79,105,130,152,153,156,179,180,181});
		validateDrawPixels(Fit.NONE,2,new int[]{4,6,2},new int[]{2,7,6},Roi.FREELINE, new int[]{28,29,53,54,78,79,80,104,105,126,127,
																								128,129,130,131,151,152,153,154,155,
																								156,178,179,180,181});
		validateDrawPixels(Fit.SPLINE,-1,new int[]{4,6,2},new int[]{2,7,6},Roi.FREELINE, new int[]{54,79,105,130,152,153,155,179,180});

		// ANGLE
		validateDrawPixels(Fit.NONE,-1,new int[]{8,1,5},new int[]{6,3,9},Roi.ANGLE, new int[]{76,77,102,103,104,127,130,131,153,157,158,
																							179,204,230});
		validateDrawPixels(Fit.NONE,2,new int[]{8,1,5},new int[]{6,3,9},Roi.ANGLE, new int[]{50,51,52,75,76,77,78,79,101,102,103,104,
																							105,106,126,127,128,129,130,131,132,133,
																							152,153,154,156,157,158,179,203,204,205,
																							229,230});
		validateDrawPixels(Fit.SPLINE,-1,new int[]{8,1,5},new int[]{6,3,9},Roi.ANGLE, new int[]{77,101,103,104,127,130,131,153,157,158,178,
																							204,230});
	}

	@Test
	public void testContains() {
		// regular cases
		
		// test all corners : should act like Java's Polygon::contains() here.
		validateContains(Fit.NONE,new int[]{0,2,2,0},new int[]{0,0,2,2},0,0,true);
		validateContains(Fit.NONE,new int[]{0,2,2,0},new int[]{0,0,2,2},0,2,false);
		validateContains(Fit.NONE,new int[]{0,2,2,0},new int[]{0,0,2,2},2,2,false);
		validateContains(Fit.NONE,new int[]{0,2,2,0},new int[]{0,0,2,2},2,0,false);
		// and an interior point
		validateContains(Fit.NONE,new int[]{0,2,2,0},new int[]{0,0,2,2},1,1,true);
		// an edge point that skips Polygon::contains()
		validateContains(Fit.NONE,new int[]{0,4,2},new int[]{0,0,4},1,2,true);
		// points outside
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},0,0,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},1,0,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},2,0,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},3,0,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},3,1,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},3,2,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},3,3,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},2,3,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},1,3,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},0,3,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},0,2,false);
		validateContains(Fit.NONE,new int[]{1,2,2,1},new int[]{1,1,2,2},0,1,false);

		// now need to try some spline cases where the fit changes the result
		
		validateContains(Fit.SPLINE,new int[]{10,5,17,11},new int[]{1,7,13,3},11,10,true);
	}

	@Test
	public void testIsHandle() {

		// special cases
		
		// not spline fit and TRACED_ROI
		p = new PolygonRoi(new int[]{1,2,3},new int[]{4,7,2},3,Roi.TRACED_ROI);
		assertEquals(-1,p.isHandle(0,0));

		// not spline fit and FREEROI
		p = new PolygonRoi(new int[]{1,2,3},new int[]{4,7,2},3,Roi.FREEROI);
		assertEquals(-1,p.isHandle(0,0));

		// not spline fit and FREELINE
		p = new PolygonRoi(new int[]{1,2,3},new int[]{4,7,2},3,Roi.FREELINE);
		assertEquals(-1,p.isHandle(0,0));
		
		// not going to worry about clipboard status - not sure existing code is correct in this case
		
		// general cases
		
		// note - can't test cases that get past special case logic. the isHandle() method relies on instance vars xp2 and yp2
		//          being nonZero. But that is only true when we have an imageCanvas (gui only).
		/*
		p = new PolygonRoi(new int[]{1,17,33},new int[]{8,24,15},3,Roi.POLYLINE);
		p.fitSpline(3);
		assertEquals(0,p.isHandle(1,8));
		assertEquals(1,p.isHandle(17,24));
		assertEquals(2,p.isHandle(33,15));
		*/
	}

	@Test
	public void testGetMask() {
		// regular cases
		validateGetMask(Fit.NONE, new int[]{1,5,3}, new int[]{1,1,3}, new int[]{1,2,3,6});
		validateGetMask(Fit.NONE, new int[]{1,2,4}, new int[]{1,7,3}, new int[]{0,3,4,6,7,8,10,13});
		validateGetMask(Fit.NONE, new int[]{6,12,5}, new int[]{13,10,7}, new int[]{0,7,8,9,10,14,15,16,17,18,19,22,23,24,25,26,29,30,31,36});
		validateGetMask(Fit.NONE, new int[]{1,4,4,1}, new int[]{1,1,4,4}, new int[]{0,1,2,3,4,5,6,7,8});
		validateGetMask(Fit.NONE, new int[]{1,4,7,3}, new int[]{1,2,4,2}, new int[]{1,9});

		// spline cases
		validateGetMask(Fit.SPLINE, new int[]{1,5,3}, new int[]{1,1,3}, new int[]{0,1,2,3,5});
		validateGetMask(Fit.SPLINE, new int[]{1,2,4}, new int[]{1,7,3}, new int[]{0,1,3,4,5,7,8,10,13});
		validateGetMask(Fit.SPLINE, new int[]{6,12,5}, new int[]{13,10,7}, new int[]{0,1,2,3,4,5,9,10,11,12,13,14,17,18,19,20,21,22,23,25,26,27,28,29,30,34,35});
		validateGetMask(Fit.SPLINE, new int[]{1,4,4,1}, new int[]{1,1,4,4}, new int[]{0,1,2,3,4,5,6,7,8});
		validateGetMask(Fit.SPLINE, new int[]{1,4,7,3}, new int[]{1,2,4,2}, new int[]{1,9});
	}

	private void validateExitNoChange(int type)
	{
		PolygonRoi before = new PolygonRoi(new int[]{1,4,6,3},new int[]{8,3,6,1},4,type);
		PolygonRoi after = (PolygonRoi)before.clone();
		assertEquals(before,after);
		after.exitConstructingMode();
		assertEquals(before,after);
	}
	
	@Test
	public void testExitConstructingMode() {
		// things that shouldn't change
		validateExitNoChange(Roi.ANGLE);
		validateExitNoChange(Roi.POINT);
		validateExitNoChange(Roi.TRACED_ROI);
		validateExitNoChange(Roi.FREEROI);
		validateExitNoChange(Roi.FREELINE);
		validateExitNoChange(Roi.POLYGON);
		
		// things that should change
		// only a CONSTRUCTING POLYLINE
		// note - can't test. you can only be in CONSTRUCTING state when you have a gui (needs imagecanvas)
	}

	@Test
	public void testFitSplineInt() {
		// this next test crashes
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			p = new PolygonRoi(new int[]{1,2,3},new int[]{3,6,4},3,Roi.POLYLINE);
			p.fitSpline(-1);
			assertFalse(p.isSplineFit());
		}
		
		// fit various cases: varying input poly pts, input poly numPts, numSplinePts
		
		validateFitSplineInt(new int[]{1,2,3}, new int[]{3,6,4}, 0,
								new int[]{}, new int[]{});
		
		validateFitSplineInt(new int[]{1,2,3}, new int[]{3,6,4}, 10,
								new int[]{0,0,0,1,1,1,1,2,2,2}, new int[]{0,1,2,2,3,3,3,2,2,1});
		
		validateFitSplineInt(new int[]{10,15,23,16}, new int[]{1,17,14,4}, 15,
								new int[]{0,1,2,3,4,6,8,10,12,13,13,12,10,8,6},
								new int[]{0,4,8,12,15,16,17,16,15,14,12,10,7,5,3});
		
		validateFitSplineInt(new int[]{8,17,3,28}, new int[]{33,14,16,1}, 25,
								new int[]{5,7,9,11,13,14,15,15,14,13,11,8,6,4,2,0,0,1,2,5,8,12,16,20,25},
								new int[]{32,29,26,23,20,18,16,14,13,13,13,13,14,14,15,15,15,14,13,12,10,8,5,3,0});
	}

	@Test
	public void testFitSpline() {
		// these next two classes bomb out
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// no pts
			validateFitSpline(new int[]{}, new int[]{});
			//one pt
			validateFitSpline(new int[]{1}, new int[]{99});
		}
		// two pts
		validateFitSpline(new int[]{1,81}, new int[]{99,1008});
		// three pts
		validateFitSpline(new int[]{1,81,45}, new int[]{99,1008,234});
		// four pts
		validateFitSpline(new int[]{1,81,45,22}, new int[]{99,1008,234,403});
		// five pts
		validateFitSpline(new int[]{1,81,45,22,66}, new int[]{99,1008,234,403,505});
	}

	@Test
	public void testRemoveSplineFit() {
		p = new PolygonRoi(new int[]{1,2,3},new int[]{7,4,1},3,Roi.POLYLINE);
		p.fitSpline();
		assertTrue(p.isSplineFit());
		p.removeSplineFit();
		assertFalse(p.isSplineFit());
	}

	@Test
	public void testIsSplineFit() {
		p = new PolygonRoi(new int[]{1,2,3},new int[]{7,4,1},3,Roi.POLYLINE);
		assertFalse(p.isSplineFit());
		p.fitSpline();
		assertTrue(p.isSplineFit());
	}

	@Test
	public void testFitSplineForStraightening() {
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			validateFitSplineStraighten(new int[]{}, new int[]{}, new int[]{}, new int[]{});
			validateFitSplineStraighten(new int[]{1}, new int[]{1}, new int[]{}, new int[]{});
		}
		validateFitSplineStraighten(new int[]{3,5}, new int[]{2,4}, new int[]{0,1,1,0}, new int[]{0,1,1,0});
		validateFitSplineStraighten(new int[]{4,8,12}, new int[]{3,6,4}, new int[]{0,1,2,0}, new int[]{0,0,0,0});
		validateFitSplineStraighten(new int[]{66,55,23}, new int[]{17,8,21}, new int[]{43,42,41,0}, new int[]{0,0,0,0});
		validateFitSplineStraighten(new int[]{66,55,23,33}, new int[]{17,8,21,22},
									new int[]{43,42,41,41,40,39,38,0}, new int[]{9,8,8,7,6,6,5,0});
		validateFitSplineStraighten(new int[]{18,77,45,61,33}, new int[]{103,14,34,81,71},
									new int[]{0,0,1,1,2,2,3,0}, new int[]{93,92,91,90,90,89,88,0});
	}

	@Test
	public void testGetUncalibratedLength() {
		validateUncalibLength(new int[]{}, new int[]{}, 0);
		validateUncalibLength(new int[]{1}, new int[]{5}, 0);
		validateUncalibLength(new int[]{1,3}, new int[]{5,11}, 1);
		validateUncalibLength(new int[]{1,3,6}, new int[]{5,11,18}, 1);
		validateUncalibLength(new int[]{1,3,6,9}, new int[]{5,11,18,23}, 2);
		validateUncalibLength(new int[]{1,3,6,9,11}, new int[]{5,11,18,23,53}, 2);
		validateUncalibLength(new int[]{1,3,6,9,11,9,5,4,3}, new int[]{5,11,18,23,53,31,32,21,22}, 4);
	}

	@Test
	public void testGetAngle() {
		// make sure default behavior tested
		p = new PolygonRoi(new int[]{1,2,3},new int[]{7,5,1},3,Roi.POLYGON);
		assertEquals(Double.NaN,p.getAngle(),0);
		
		// try the eight ways to get 90
		validateGetAngle(new int[]{0,1,1},new int[]{0,0,1},90);
		validateGetAngle(new int[]{0,1,1},new int[]{0,0,-1},90);
		validateGetAngle(new int[]{0,0,1},new int[]{0,1,1},90);
		validateGetAngle(new int[]{0,0,-1},new int[]{0,1,1},90);
		validateGetAngle(new int[]{0,-1,-1},new int[]{0,0,1},90);
		validateGetAngle(new int[]{0,-1,-1},new int[]{0,0,-1},90);
		validateGetAngle(new int[]{0,0,-1},new int[]{0,-1,-1},90);
		validateGetAngle(new int[]{0,0,1},new int[]{0,-1,-1},90);

		// try a 180
		validateGetAngle(new int[]{0,1,2},new int[]{0,0,0},180);
		
		// try greater than 180 one way or other
		validateGetAngle(new int[]{1,0,-7},new int[]{0,0,7},135);
		validateGetAngle(new int[]{-7,0,1},new int[]{7,0,0},135);
		
		// test an arbitrary polygon
		validateGetAngle(new int[]{1,2,3},new int[]{7,5,1},167.47119);
	}

	@Test
	public void testGetNCoordinates() {
		int refVal = 55;
		p = new PolygonRoi(new int[]{1,2,3},new int[]{7,5,1},3,Roi.POLYGON);
		p.fitSpline(refVal);
		assertEquals(refVal,p.getNCoordinates());
	}

	@Test
	public void testGetXCoordinates() {
		// tested elsewhere
	}

	@Test
	public void testGetYCoordinates() {
		// tested elsewhere
	}

	@Test
	public void testGetNonSplineCoordinates() {
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			validateGetNonSpline(new int[]{}, new int[]{});
			validateGetNonSpline(new int[]{1}, new int[]{1});
			validateGetNonSpline(new int[]{1,2}, new int[]{3,4});
		}
		validateGetNonSpline(new int[]{1,2,3}, new int[]{4,5,6});
		validateGetNonSpline(new int[]{1,2,3,4}, new int[]{5,6,7,8});
	}

}