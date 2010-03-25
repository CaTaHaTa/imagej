package ij.measure;

import static org.junit.Assert.*;
import ij.IJInfo;
import ij.Assert;

import org.junit.Test;

// note - fully testing this would be prohibitive (would need an independent curve fitter)
//  will create tests from current behavior and test against that for now and the future.
//  if in the future we change curve fitting code these tests may break.
//  choosing an "isEquals" tolerance may be key. I'll make a local tolerance that can be
//  adjusted in case future implementation needs to relax it.

public class CurveFitterTest {

	private static final double Tolerance = 0.001;
	CurveFitter cf;
	double[] xs,ys;

	@Test
	public void testConstants() {

		assertEquals(0,CurveFitter.STRAIGHT_LINE);
		assertEquals(1,CurveFitter.POLY2);
		assertEquals(2,CurveFitter.POLY3);
		assertEquals(3,CurveFitter.POLY4);
		assertEquals(4,CurveFitter.EXPONENTIAL);
		assertEquals(5,CurveFitter.POWER);
		assertEquals(6,CurveFitter.LOG);
		assertEquals(7,CurveFitter.RODBARD);
		assertEquals(8,CurveFitter.GAMMA_VARIATE);
		assertEquals(9,CurveFitter.LOG2);
		assertEquals(10,CurveFitter.RODBARD2);
		assertEquals(11,CurveFitter.EXP_WITH_OFFSET);
		assertEquals(12,CurveFitter.GAUSSIAN);
		assertEquals(13,CurveFitter.EXP_RECOVERY);
		
		assertEquals(500,CurveFitter.IterFactor);
		
		assertEquals(CurveFitter.fitList[0],"Straight Line");
		assertEquals(CurveFitter.fitList[1],"2nd Degree Polynomial");
		assertEquals(CurveFitter.fitList[2],"3rd Degree Polynomial");
		assertEquals(CurveFitter.fitList[3],"4th Degree Polynomial");
		assertEquals(CurveFitter.fitList[4],"Exponential");
		assertEquals(CurveFitter.fitList[5],"Power");
		assertEquals(CurveFitter.fitList[6],"Log");
		assertEquals(CurveFitter.fitList[7],"Rodbard");
		assertEquals(CurveFitter.fitList[8],"Gamma Variate");
		assertEquals(CurveFitter.fitList[9],"y = a+b*ln(x-c)");
		assertEquals(CurveFitter.fitList[10],"Rodbard (NIH Image)");
		assertEquals(CurveFitter.fitList[11],"Exponential with Offset");
		assertEquals(CurveFitter.fitList[12],"Gaussian");
		assertEquals(CurveFitter.fitList[13],"Exponential Recovery");

		assertEquals(CurveFitter.fList[0],"y = a+bx");
		assertEquals(CurveFitter.fList[1],"y = a+bx+cx^2");
		assertEquals(CurveFitter.fList[2],"y = a+bx+cx^2+dx^3");
		assertEquals(CurveFitter.fList[3],"y = a+bx+cx^2+dx^3+ex^4");
		assertEquals(CurveFitter.fList[4],"y = a*exp(bx)");
		assertEquals(CurveFitter.fList[5],"y = ax^b");
		assertEquals(CurveFitter.fList[6],"y = a*ln(bx)");
		assertEquals(CurveFitter.fList[7],"y = d+(a-d)/(1+(x/c)^b)");
		assertEquals(CurveFitter.fList[8],"y = a*(x-b)^c*exp(-(x-b)/d)");
		assertEquals(CurveFitter.fList[9],"y = a+b*ln(x-c)");
		assertEquals(CurveFitter.fList[10],"y = d+(a-d)/(1+(x/c)^b)");
		assertEquals(CurveFitter.fList[11],"y = a*exp(-bx) + c");
		assertEquals(CurveFitter.fList[12],"y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))");
		assertEquals(CurveFitter.fList[13],"y=a*(1-exp(-b*x)) + c"); 
	}

	@Test
	public void testCurveFitter() {
	
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// null case - crashes with a NullPtrExcep
			cf = new CurveFitter(null,null);
			assertNotNull(cf);

			// note it is possible to construct with xs and ys being different lengths!
		}

		// x len && y len both 0
		xs = new double[] {};
		ys = new double[] {};
		cf = new CurveFitter(xs,ys);
		assertNotNull(cf);
		assertEquals(xs,cf.getXPoints());
		assertEquals(ys,cf.getYPoints());

		// valid case x len == y len
		xs = new double[] {3};
		ys = new double[] {7};
		cf = new CurveFitter(xs,ys);
		assertNotNull(cf);
		assertEquals(xs,cf.getXPoints());
		assertEquals(ys,cf.getYPoints());
		
	}

	@Test
	public void testDoFitInt() {
		// note - no need to test. just calls doFit() with false as param. I test doFit() next.
	}
	
	private void tryFit(int func, double[] xs, double[] ys, double[] expParams)
	{
		cf = new CurveFitter(xs,ys);
		cf.doFit(func,false);
		double[] actParams = cf.getParams();
		Assert.assertDoubleArraysEqual(expParams, actParams, Tolerance);
	}

	@Test
	public void testDoFitIntBoolean() {
		
		// note - doFit(func,true) case runs gui stuff - can't test
		//        doFit(func,false) cases all follow

		xs = new double[] {1,2,3};
		
		// perfect fit tests
		tryFit(CurveFitter.STRAIGHT_LINE, xs, new double[] {4,6,8}, new double[] {2,2,0});
		tryFit(CurveFitter.POLY2, xs, new double[] {10,21,37}, new double[] {4,3.5,2.5,0});


		// approximate fit tests
		tryFit(CurveFitter.STRAIGHT_LINE, xs, new double[] {4,6.5,8.2}, new double[] {2.03333,2.10000,0.10667});
		tryFit(CurveFitter.POLY2, xs, new double[] {10.6,24.2,33}, new double[] {-7.8,20.8,-2.4,0});
		tryFit(CurveFitter.POLY3, xs, new double[] {6,63,104.2}, new double[] {-60.16957,68.54422,-1.26957,-1.10507,0});
		tryFit(CurveFitter.POLY4, xs, new double[] {106.3,97.2,55.5}, new double[] {104.18837,3.55676,-0.35439,-0.59594,-0.49480,0});
		tryFit(CurveFitter.EXPONENTIAL, xs, new double[] {12,44,115}, new double[] {5.46507,1.01669,14.86054});
		tryFit(CurveFitter.POWER, xs, new double[] {3,15,99}, new double[] {0.62482,4.61051,5.71156});
		tryFit(CurveFitter.LOG, xs, new double[] {14,11,4}, new double[] {-8.58828,0.17856,7.13782});
		tryFit(CurveFitter.RODBARD, xs, new double[] {10,15,11}, new double[] {-1.12844,32.03943,0.95991,13,8});
		tryFit(CurveFitter.GAMMA_VARIATE, xs, new double[] {1,17,14}, new double[] {1.11196,46.72393,1.06347,1.00370,1});
		tryFit(CurveFitter.LOG2, xs, new double[] {1,3,5}, new double[] {-729.49201,165.80871,-80.90891,0});
		tryFit(CurveFitter.RODBARD2, xs, new double[] {808,244,612}, new double[] {2.5,71.53400,830.68842,-9.87467,0.5});
		tryFit(CurveFitter.EXP_WITH_OFFSET, xs, new double[] {44,88,257}, new double[] {182.24613,14.64237,129.66677,25288.68033});
		tryFit(CurveFitter.GAUSSIAN, xs, new double[] {1,58,14},  new double[] {0.03542,68.17796,2.24211,-0.42566,0});
		tryFit(CurveFitter.EXP_RECOVERY, xs, new double[] {44,22,12},  new double[] {-88.73333,0.78845,92.4,0});
	}

	@Test
	public void testDoCustomFit() {
		
		xs = new double[] {1,2,3};
		ys = new double[] {3,251,1004};
		cf = new CurveFitter(xs,ys);
		
		// equation has no x's
		assertEquals(0,cf.doCustomFit("y=a*h", new double[]{5,8,11}, false));
		
		// equation has no y's
		assertEquals(0,cf.doCustomFit("z=a*x", new double[]{5,8,11}, false));
		
		// equation has none of a,b,c,d,e
		assertEquals(0,cf.doCustomFit("y=m*x", new double[]{5,8,11}, false));
		
		if (IJInfo.RUN_GUI_TESTS)
		{
			// TODO - this test requires GUI interaction
			
			// equation has bad syntax
			assertEquals(0,cf.doCustomFit("y=a*x+BadSyntax", new double[]{5,8,11}, false));
		}
		
		// otherwise success - test a couple custom fits and the underlying results
		
		assertEquals(1,cf.doCustomFit("y=a*x", new double[]{5,8,11}, false));
		assertEquals(251.21464,cf.getParams()[0],Tolerance);

		assertEquals(2,cf.doCustomFit("y=a*x*x+b", new double[]{5,8,11}, false));
		assertEquals(127.72446,cf.getParams()[0],Tolerance);
		assertEquals(-176.71432,cf.getParams()[1],Tolerance);

		assertEquals(4,cf.doCustomFit("y=a*x*x+b*x*x+c*x+d", new double[]{5,8,11}, false));
		assertEquals(-422.75140,cf.getParams()[0],Tolerance);
		assertEquals(675.25140,cf.getParams()[1],Tolerance);
		assertEquals(-509.5,cf.getParams()[2],Tolerance);
		assertEquals(260,cf.getParams()[3],Tolerance);
	}

    @Test
	public void testGetNumParams() {
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);
		
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.POLY2);
		assertEquals(3,cf.getNumParams());
		cf.doFit(CurveFitter.POLY3);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.POLY4);
		assertEquals(5,cf.getNumParams());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.POWER);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.LOG);
		assertEquals(2,cf.getNumParams());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.LOG2);
		assertEquals(3,cf.getNumParams());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals(3,cf.getNumParams());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals(4,cf.getNumParams());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals(3,cf.getNumParams());
		cf.doCustomFit("y=a*x*x*x+b*x*x+c*x+d", new double[]{1,2,3,4}, false);
		assertEquals(4,cf.getNumParams());
	}

	@Test
	public void testFDoubleArrayDouble() {
		//fail("Not yet implemented");
	}

	@Test
	public void testFIntDoubleArrayDouble() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetParams() {
		// note - can't really test this - it just modifies internal state of private vars
		// put in compile time checks
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);
		cf.doFit(CurveFitter.POLY4);
		assertNotNull(cf.getParams());
	}

	@Test
	public void testGetResiduals() {
		double[] resids;
		
		// setup initial data
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {14,96,44,82,51,77,57};
		cf = new CurveFitter(xs,ys);

		// try one method
		cf.doFit(CurveFitter.EXP_RECOVERY);
		resids = cf.getResiduals();
		assertNotNull(resids);
		assertEquals(0.0,resids[0],Tolerance);
		assertEquals(28.16667,resids[1],Tolerance);
		assertEquals(-23.83337,resids[2],Tolerance);
		assertEquals(14.16663,resids[3],Tolerance);
		assertEquals(-16.83337,resids[4],Tolerance);
		assertEquals(9.16663,resids[5],Tolerance);
		assertEquals(-10.83337,resids[6],Tolerance);

		// try another method
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		resids = cf.getResiduals();
		assertNotNull(resids);
		assertEquals(-46.14286,resids[0],Tolerance);
		assertEquals(35.85714,resids[1],Tolerance);
		assertEquals(-16.14286,resids[2],Tolerance);
		assertEquals(21.85714,resids[3],Tolerance);
		assertEquals(-9.14286,resids[4],Tolerance);
		assertEquals(16.85714,resids[5],Tolerance);
		assertEquals(-3.14286,resids[6],Tolerance);
	}

	@Test
	public void testGetSumResidualsSqr() {
		// setup initial data
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {14,96,44,82,51,77,57};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.POLY4);
		assertEquals(1794.37229,cf.getSumResidualsSqr(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals(1890.14122,cf.getSumResidualsSqr(),Tolerance);
	}

	@Test
	public void testGetSD() {
		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {96,108,77,55,51};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.LOG2);
		assertEquals(10.45104,cf.getSD(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals(10.44390,cf.getSD(),Tolerance);
	}

	@Test
	public void testGetRSquared() {
		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,33,24,48,62};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.LOG);
		assertEquals(-0.00636,cf.getRSquared(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals(0.84155,cf.getRSquared(),Tolerance);
	}

	@Test
	public void testGetFitGoodness() {
		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,99,18,24,3};
		cf = new CurveFitter(xs,ys);
		
		// try one method
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals(0.77884,cf.getFitGoodness(),Tolerance);

		// try another method
		cf.doFit(CurveFitter.POLY3);
		assertEquals(-0.59996,cf.getFitGoodness(),Tolerance);  // negative fit measure is not a bug
	}

	// remove the Time substring from the expected results - otherwise assertions fail randomly
	private String removeTime(String str)
	{
		String tmp = "";

		for (String s : str.split("\n"))
			if (!s.startsWith("Time"))
				tmp += s + "\n";
		
		return tmp;
	}
	
	@Test
	public void testGetResultString() {
		
		String exp,actual;

		// setup initial data
		xs = new double[] {1,2,3,4,5};
		ys = new double[] {4,6,8,10,12};
		cf = new CurveFitter(xs,ys);

		// test one method : choose arbitrarily
		cf.doFit(CurveFitter.RODBARD);
		exp = removeTime(
				"\nFormula: y = d+(a-d)/(1+(x/c)^b)\nTime: 9ms\nNumber of iterations: 8000 (8000)\nNumber of restarts: 0 (2)" + 
				"\nSum of residuals squared: 0.0000\nStandard deviation: 0.0000\nR^2: 1.0000\nParameters:\n  a = 2.0002" + 
				"\n  b = 1.0001\n  c = 63234.9017\n  d = 126581.3747"
			);
		actual = removeTime(cf.getResultString());
		assertEquals(exp,actual);

		// test another method : choose arbitrarily
		cf.doFit(CurveFitter.GAUSSIAN);
		exp = removeTime(
				"\nFormula: y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))\nTime: 9ms\nNumber of iterations: 8000 (8000)\nNumber of restarts: 0 (2)" + 
				"\nSum of residuals squared: 0.0000\nStandard deviation: 0.0000\nR^2: 1.0000\nParameters:\n  a = -2717823.7529" + 
				"\n  b = 1746275.5590\n  c = 1348680.9656\n  d = 1353787.2431"
			);
		actual = removeTime(cf.getResultString());
		assertEquals(exp,actual);
	}

	@Test
	public void testGetIterations() {
		// simply a getter - nothing to test - put in place code for compile time check
		int tmp;
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,6,8,10,12,14};
		cf = new CurveFitter(xs,ys);
		tmp = cf.getIterations();
	}

	@Test
	public void testSetAndGetMaxIterations() {
		int tmp;
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,6,8,10,12,14};
		cf = new CurveFitter(xs,ys);
		tmp = cf.getMaxIterations();
		cf.setMaxIterations(tmp+500);
		assertEquals(tmp+500,cf.getMaxIterations());
	}

	@Test
	public void testSetAndGetRestarts() {
		int tmp;
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,6,8,10,12,14};
		cf = new CurveFitter(xs,ys);
		tmp = cf.getRestarts();
		cf.setRestarts(tmp+500);
		assertEquals(tmp+500,cf.getRestarts());
	}

	@Test
	public void testSetInitialParameters() {
		/*
		//TODO - figure out a way to test this. setInitialParameters() simply tweaks internals so that maybe
		//   the speed of the fit is improved. But I can't seem to affect fit speed with any inputs. The below code seems
		//   like a logical test but does not work.
		   
		int iters1, iters2;
		double[] results1,results2;
		
		xs = new double[] {1,2,3,4,5,6,7,8};
		ys = new double[] {10,20,30,40,50,60,70,80};
		cf = new CurveFitter(xs,ys);
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		results1 = cf.getParams().clone();
		iters1 = cf.getIterations();
		cf.setInitialParameters(new double[] {14,-3});
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		results2 = cf.getParams().clone();
		iters2 = cf.getIterations();
		System.out.println(iters1 + " " + iters2);
		Assert.assertDoubleArraysEqual(results1,results2,Tolerance);
		assertTrue(iters1 != iters2);
		 */
	}

	@Test
	public void testGetMax() {
		double[] tmp;
		
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// null list
			tmp = null;
			assertEquals(0,CurveFitter.getMax(tmp));
			
			// empty list
			tmp = new double[]{};
			assertEquals(0,CurveFitter.getMax(tmp));
		}
		
		// one item
		tmp = new double[]{1};
		assertEquals(0,CurveFitter.getMax(tmp));
		
		// two items : max at end of list
		tmp = new double[]{0,1};
		assertEquals(1,CurveFitter.getMax(tmp));

		// two items : max at beginning of list
		tmp = new double[]{1,0};
		assertEquals(0,CurveFitter.getMax(tmp));

		// two maximums in list - getmax() returns the first one found when multiple positions are max
		tmp = new double[]{0,1,0,1};
		assertEquals(1,CurveFitter.getMax(tmp));

		// ascending list
		tmp = new double[]{0,1,2,3};
		assertEquals(3,CurveFitter.getMax(tmp));

		// descending list
		tmp = new double[]{3,2,1,0};
		assertEquals(0,CurveFitter.getMax(tmp));

		// negative values
		tmp = new double[]{-203,-108,-410,-155};
		assertEquals(1,CurveFitter.getMax(tmp));

		// MAX_VALUE
		tmp = new double[]{0,0,Double.MAX_VALUE,0,0};
		assertEquals(2,CurveFitter.getMax(tmp));

		// MIN_VALUE
		tmp = new double[]{0,0,-Double.MAX_VALUE,0,0};
		assertEquals(0,CurveFitter.getMax(tmp));

		// NaN
		tmp = new double[]{0,0,Double.NaN,0,0};
		assertEquals(0,CurveFitter.getMax(tmp));
	}

	@Test
	public void testGetXandYPoints() {
		xs = new double[] {1,2,3};
		ys = new double[] {4,6,8};
		cf = new CurveFitter(xs,ys);
		Assert.assertDoubleArraysEqual(xs, cf.getXPoints(),Tolerance);
		Assert.assertDoubleArraysEqual(ys, cf.getYPoints(),Tolerance);
	}

	@Test
	public void testGetFit() {
		
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x",new double[] {1,2,3,4},false);
		assertEquals(20,cf.getFit());

		// test a basic ones
		
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals(CurveFitter.STRAIGHT_LINE,cf.getFit());
		cf.doFit(CurveFitter.POLY2);
		assertEquals(CurveFitter.POLY2,cf.getFit());
		cf.doFit(CurveFitter.POLY3);
		assertEquals(CurveFitter.POLY3,cf.getFit());
		cf.doFit(CurveFitter.POLY4);
		assertEquals(CurveFitter.POLY4,cf.getFit());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals(CurveFitter.EXPONENTIAL,cf.getFit());
		cf.doFit(CurveFitter.POWER);
		assertEquals(CurveFitter.POWER,cf.getFit());
		cf.doFit(CurveFitter.LOG);
		assertEquals(CurveFitter.LOG,cf.getFit());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals(CurveFitter.RODBARD,cf.getFit());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals(CurveFitter.GAMMA_VARIATE,cf.getFit());
		cf.doFit(CurveFitter.LOG2);
		assertEquals(CurveFitter.LOG2,cf.getFit());
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals(CurveFitter.RODBARD2,cf.getFit());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals(CurveFitter.EXP_WITH_OFFSET,cf.getFit());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals(CurveFitter.GAUSSIAN,cf.getFit());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals(CurveFitter.EXP_RECOVERY,cf.getFit());
	}

	@Test
	public void testGetName() {
		
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x*x*x*x*x*x*x",new double[] {6,5,4,3},false);
		assertEquals("User-defined",cf.getName());

		// test a basic ones
		
		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals("Straight Line",cf.getName());
		cf.doFit(CurveFitter.POLY2);
		assertEquals("2nd Degree Polynomial",cf.getName());
		cf.doFit(CurveFitter.POLY3);
		assertEquals("3rd Degree Polynomial",cf.getName());
		cf.doFit(CurveFitter.POLY4);
		assertEquals("4th Degree Polynomial",cf.getName());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals("Exponential",cf.getName());
		cf.doFit(CurveFitter.POWER);
		assertEquals("Power",cf.getName());
		cf.doFit(CurveFitter.LOG);
		assertEquals("Log",cf.getName());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals("Rodbard",cf.getName());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals("Gamma Variate",cf.getName());
		cf.doFit(CurveFitter.LOG2);
		assertEquals("y = a+b*ln(x-c)",cf.getName());
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals("Rodbard (NIH Image)",cf.getName());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals("Exponential with Offset",cf.getName());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals("Gaussian",cf.getName());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals("Exponential Recovery",cf.getName());
	}

	@Test
	public void testGetFormula() {
		xs = new double[] {1,2,3,4,5,6,7};
		ys = new double[] {4,2,6,4,5,8,9};
		cf = new CurveFitter(xs,ys);

		// test a custom one
		cf.doCustomFit("y=a*x*x*x*x*x*x",new double[] {1,2,3,4},false);
		assertEquals("y=a*x*x*x*x*x*x",cf.getFormula());

		// test a basic ones

		cf.doFit(CurveFitter.STRAIGHT_LINE);
		assertEquals("y = a+bx",cf.getFormula());
		cf.doFit(CurveFitter.POLY2);
		assertEquals("y = a+bx+cx^2",cf.getFormula());
		cf.doFit(CurveFitter.POLY3);
		assertEquals("y = a+bx+cx^2+dx^3",cf.getFormula());
		cf.doFit(CurveFitter.POLY4);
		assertEquals("y = a+bx+cx^2+dx^3+ex^4",cf.getFormula());
		cf.doFit(CurveFitter.EXPONENTIAL);
		assertEquals("y = a*exp(bx)",cf.getFormula());
		cf.doFit(CurveFitter.POWER);
		assertEquals("y = ax^b",cf.getFormula());
		cf.doFit(CurveFitter.LOG);
		assertEquals("y = a*ln(bx)",cf.getFormula());
		cf.doFit(CurveFitter.RODBARD);
		assertEquals("y = d+(a-d)/(1+(x/c)^b)",cf.getFormula());
		cf.doFit(CurveFitter.GAMMA_VARIATE);
		assertEquals("y = a*(x-b)^c*exp(-(x-b)/d)",cf.getFormula());
		cf.doFit(CurveFitter.LOG2);
		assertEquals("y = a+b*ln(x-c)",cf.getFormula());
		cf.doFit(CurveFitter.RODBARD2);
		assertEquals("y = d+(a-d)/(1+(x/c)^b)",cf.getFormula());
		cf.doFit(CurveFitter.EXP_WITH_OFFSET);
		assertEquals("y = a*exp(-bx) + c",cf.getFormula());
		cf.doFit(CurveFitter.GAUSSIAN);
		assertEquals("y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d))",cf.getFormula());
		cf.doFit(CurveFitter.EXP_RECOVERY);
		assertEquals("y=a*(1-exp(-b*x)) + c",cf.getFormula());
	}

}
