package imagej2.process;

import static org.junit.Assert.*;
import imagej2.process.Index;
import imagej2.process.Span;

import org.junit.Test;

public class IndexTest {

	@Test
	public void testCreateInt() {
		int[] vals;
		
		try {
			vals = Span.create(-1);
			fail();
		} catch (NegativeArraySizeException e) {
			assertTrue(true);
		}

		vals = Span.create(0);
		assertArrayEquals(new int[]{},vals);
		
		vals = Span.create(1);
		assertArrayEquals(new int[]{0},vals);
		
		vals = Span.create(2);
		assertArrayEquals(new int[]{0,0},vals);
		
		vals = Span.create(3);
		assertArrayEquals(new int[]{0,0,0},vals);
	}

	@Test
	public void testCreateIntArray() {
		int[] vals;
		
		vals = Span.create(new int[]{});
		assertArrayEquals(new int[]{},vals);
		
		vals = Span.create(new int[]{4});
		assertArrayEquals(new int[]{4},vals);
		
		vals = Span.create(new int[]{1,7});
		assertArrayEquals(new int[]{1,7},vals);
		
		vals = Span.create(new int[]{1,2,3,4,5,6,7,8,9});
		assertArrayEquals(new int[]{1,2,3,4,5,6,7,8,9},vals);
	}

	@Test
	public void testCreateIntIntIntArray() {
		int[] vals;
	
		// failure cases first

		//   x < 0
		
		try {
			vals = Index.create(-1, 1, new int[]{1,1,1});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		//   y < 0
		
		try {
			vals = Index.create(1, -1, new int[]{1,1,1});
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// otherwise good input

		vals = Index.create(0, 0, new int[]{});
		assertArrayEquals(new int[]{0,0},vals);
		
		vals = Index.create(0, 0, new int[]{1});
		assertArrayEquals(new int[]{0,0,1},vals);
		
		vals = Index.create(0, 0, new int[]{1,2});
		assertArrayEquals(new int[]{0,0,1,2},vals);
		
		vals = Index.create(0, 0, new int[]{1,2,3});
		assertArrayEquals(new int[]{0,0,1,2,3},vals);
		
		vals = Index.create(5, 6, new int[]{9,8,7,3,2,1});
		assertArrayEquals(new int[]{5,6,9,8,7,3,2,1},vals);		
	}

	@Test
	public void testIsValid()
	{
		assertFalse(Index.isValid(new int[]{-1}, new int[]{0}, new int[]{1}));
		assertTrue(Index.isValid(new int[]{0}, new int[]{0}, new int[]{1}));
		assertFalse(Index.isValid(new int[]{1}, new int[]{0}, new int[]{1}));

		assertFalse(Index.isValid(new int[]{-1}, new int[]{0}, new int[]{2}));
		assertTrue(Index.isValid(new int[]{0}, new int[]{0}, new int[]{2}));
		assertTrue(Index.isValid(new int[]{1}, new int[]{0}, new int[]{2}));
		assertFalse(Index.isValid(new int[]{2}, new int[]{0}, new int[]{2}));

		assertFalse(Index.isValid(new int[]{-1,0}, new int[]{0,0}, new int[]{2,2}));
		assertFalse(Index.isValid(new int[]{0,-1}, new int[]{0,0}, new int[]{2,2}));
		assertTrue(Index.isValid(new int[]{0,0}, new int[]{0,0}, new int[]{2,2}));
		assertTrue(Index.isValid(new int[]{0,1}, new int[]{0,0}, new int[]{2,2}));
		assertTrue(Index.isValid(new int[]{1,0}, new int[]{0,0}, new int[]{2,2}));
		assertTrue(Index.isValid(new int[]{1,1}, new int[]{0,0}, new int[]{2,2}));
		assertFalse(Index.isValid(new int[]{2,1}, new int[]{0,0}, new int[]{2,2}));
		assertFalse(Index.isValid(new int[]{1,2}, new int[]{0,0}, new int[]{2,2}));

		assertFalse(Index.isValid(new int[]{1,0}, new int[]{1,1}, new int[]{3,3}));
		assertFalse(Index.isValid(new int[]{0,1}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{1,1}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{1,2}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{1,3}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{2,1}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{2,2}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{2,3}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{3,1}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{3,2}, new int[]{1,1}, new int[]{3,3}));
		assertTrue(Index.isValid(new int[]{3,3}, new int[]{1,1}, new int[]{3,3}));
		assertFalse(Index.isValid(new int[]{3,4}, new int[]{1,1}, new int[]{3,3}));
		assertFalse(Index.isValid(new int[]{4,3}, new int[]{1,1}, new int[]{3,3}));
	}

	private void shouldFailIncrement(int[] position, int[] origin, int[] span)
	{
		Index.increment(position, origin, span);
		
		assertFalse(Index.isValid(position,origin,span));
	}
	
	@Test
	public void testIncrement()
	{
		int[] position, origin, span;
		
		// one element 1D array
		
		origin = new int[]{0};
		span =  new int[]{1};
		position =  origin.clone();
		assertTrue(Index.isValid(position,origin,span));
		shouldFailIncrement(position,origin,span);

		// two element 1D array
		
		origin = new int[]{0};
		span =  new int[]{2};
		position =  origin.clone();
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1},position);
		
		assertTrue(Index.isValid(position,origin,span));
		shouldFailIncrement(position,origin,span);

		// three element 1D array
		
		origin = new int[]{0};
		span =  new int[]{3};
		position =  origin.clone();
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1},position);
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{2},position);

		assertTrue(Index.isValid(position,origin,span));
		shouldFailIncrement(position,origin,span);
		
		// four element 2D array
		origin = new int[]{0,0};
		span =  new int[]{2,2};
		position =  origin.clone();
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,0},position);
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{0,1},position);

		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,1},position);
		
		assertTrue(Index.isValid(position,origin,span));
		shouldFailIncrement(position,origin,span);

		// eight element 3D array
		origin = new int[]{1,1,1};
		span =  new int[]{2,2,2};
		position =  origin.clone();
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{2,1,1},position);
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,2,1},position);

		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{2,2,1},position);
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,1,2},position);

		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{2,1,2},position);
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,2,2},position);
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{2,2,2},position);

		assertTrue(Index.isValid(position,origin,span));
		shouldFailIncrement(position,origin,span);
		
		// 2x3x1 array
		origin = new int[]{0,0,0};
		span =  new int[]{2,3,1};
		position =  origin.clone();

		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,0,0},position);

		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{0,1,0},position);

		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,1,0},position);

		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{0,2,0},position);
		
		assertTrue(Index.isValid(position,origin,span));
		Index.increment(position, origin, span);
		assertArrayEquals(new int[]{1,2,0},position);
		
		assertTrue(Index.isValid(position,origin,span));
		shouldFailIncrement(position,origin,span);
	}

	private void getPlanePositionShouldFail(int[] dimensions, int index)
	{
		try {
			Index.getPlanePosition(dimensions, index);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testGetPlanePosition() {
		
		int[] dimensions;
		
		dimensions = new int[]{};
		getPlanePositionShouldFail(dimensions, -1);
		getPlanePositionShouldFail(dimensions, 0);
		getPlanePositionShouldFail(dimensions, 1);
		
		dimensions = new int[]{50};
		getPlanePositionShouldFail(dimensions, -1);
		getPlanePositionShouldFail(dimensions, 0);
		getPlanePositionShouldFail(dimensions, 1);

		// TODO - the middle case is unintuitive. Its up to the user to specify a MxNx1 image for a single plane
		dimensions = new int[]{50,60};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{}, Index.getPlanePosition(dimensions, 0));
		getPlanePositionShouldFail(dimensions, 1);

		dimensions = new int[]{50,60,3};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0}, Index.getPlanePosition(dimensions, 0));
		assertArrayEquals(new int[]{1}, Index.getPlanePosition(dimensions, 1));
		assertArrayEquals(new int[]{2}, Index.getPlanePosition(dimensions, 2));
		getPlanePositionShouldFail(dimensions, 3);

		dimensions = new int[]{50,60,2,2};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0,0}, Index.getPlanePosition(dimensions, 0));
		assertArrayEquals(new int[]{1,0}, Index.getPlanePosition(dimensions, 1));
		assertArrayEquals(new int[]{0,1}, Index.getPlanePosition(dimensions, 2));
		assertArrayEquals(new int[]{1,1}, Index.getPlanePosition(dimensions, 3));
		getPlanePositionShouldFail(dimensions, 4);

		dimensions = new int[]{50,60,2,2,2};
		getPlanePositionShouldFail(dimensions, -1);
		assertArrayEquals(new int[]{0,0,0}, Index.getPlanePosition(dimensions, 0));
		assertArrayEquals(new int[]{1,0,0}, Index.getPlanePosition(dimensions, 1));
		assertArrayEquals(new int[]{0,1,0}, Index.getPlanePosition(dimensions, 2));
		assertArrayEquals(new int[]{1,1,0}, Index.getPlanePosition(dimensions, 3));
		assertArrayEquals(new int[]{0,0,1}, Index.getPlanePosition(dimensions, 4));
		assertArrayEquals(new int[]{1,0,1}, Index.getPlanePosition(dimensions, 5));
		assertArrayEquals(new int[]{0,1,1}, Index.getPlanePosition(dimensions, 6));
		assertArrayEquals(new int[]{1,1,1}, Index.getPlanePosition(dimensions, 7));
		getPlanePositionShouldFail(dimensions, 8);
	}
	
}
