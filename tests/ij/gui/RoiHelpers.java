package ij.gui;

import static org.junit.Assert.assertEquals;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import ij.Assert;
import ij.process.ImageProcessor;

public class RoiHelpers {
	
	public static boolean find(int val, int[] arr) {
		for (int i = 0; i < arr.length; i++)
			if (arr[i] == val)
				return true;
		return false;
	}
	
	public static void validateResult(ImageProcessor proc, int refVal, int[] expectedNonZeroes)
	{
		int h = proc.getHeight();
		int w = proc.getWidth();
		
		for (int i = 0; i < h*w; i++)
			if (find(i,expectedNonZeroes))
				assertEquals(refVal,proc.get(i));
			else
				assertEquals(0,proc.get(i));
	}
	
	public static void printValues(ImageProcessor proc)
	{
		int h = proc.getHeight();
		int w = proc.getWidth();
		
		System.out.println(""+w+"x"+h+" pixels ----------------");
		for (int i = 0; i < w*h; i++)
			if (proc.get(i) != 0)
				System.out.println("("+i+") == "+proc.get(i));
	}
	
	public static boolean doublesEqual(Double[] a, Double[] b, double tol) {
		
		if (a.length != b.length)
			return false;
		
		for (int i = 0; i < a.length; i++)
			if (Math.abs(a[i]-b[i]) > tol)
				return false;
		
		return true;
	}

	private static ArrayList<Double> getCoords(Polygon p) {
		ArrayList<Double> vals = new ArrayList<Double>();
		
		for (PathIterator iter = p.getPathIterator(null); !iter.isDone();)
		{
			double[] coords = new double[2];
			iter.currentSegment(coords);
			vals.add(coords[0]);
			vals.add(coords[1]);
			iter.next();
		}
		return vals;
	}

	public static boolean polysEqual(Polygon a, Polygon b){
		Double[] da = new Double[]{}, db = new Double[]{};
		ArrayList<Double> ptsA = getCoords(a);
		ArrayList<Double> ptsB = getCoords(b);
		return doublesEqual(ptsA.toArray(da), ptsB.toArray(db), Assert.DOUBLE_TOL);
	}
	
	public static boolean rectsEqual(Rectangle a, Rectangle b) {
		if (a.x != b.x) return false;
		if (a.y != b.y) return false;
		if (a.width != b.width) return false;
		if (a.height != b.height) return false;
		return true;
	}

}
