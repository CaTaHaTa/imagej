package imagej.dataset;

import java.util.ArrayList;

import imagej.MetaData;
import imagej.data.Type;

// TODOs
// Are there any right to left vs. left to right index accessing that matters here? Especially for getSubset()???
// Dataset matches parent in extent of its subset of axes. i.e. given [1,2,3] and axes [0,1,-1] the dataset has extent 3 in its only free axis.
//   Would like to come up with a subsetting view that could have smaller bounds than parent. This would cause us to have to write bounds checking
//   code for this class on every data access.

// TODO - this is a first pass implementation. it has many instance vars that could be trimmed down. fix as possible.

/** a DatasetView is Dataset that acts like a view into a larger Dataset with some axes fixed. */
public class DatasetView implements Dataset
{
	// Core instance variables for this implementation
	private Dataset fullSpaceDataset;
	private int[] fullSpaceAxisValues;
	private int[] viewDimensions;
	private int[] viewAxesIndices;
	// caching variables for performance
	private int[] fullSpaceIndex;
	private int[] oneDimWorkspace;
	private int firstFixedAxis;
	private int fullDimensionsLength;
	private int viewDimensionsLength;
	// general Dataset support variables
	private Dataset parent;
	private MetaData metadata;
	
	/** Constructor
	 * 
	 * @param referenceDataset - the Dataset this view will be constrained within
	 * @param fullSpaceAxisValues - a specified list of axis values. one entry per axis present in viewed Dataset. An example value of fixed axes
	 *   might be [1,0,3,-1,-1] which implies x=1. y=0, z=3, c and t vary. This creates a two dim view of the larger dataset in c & t.
	 */
	public DatasetView(Dataset referenceDataset, int[] fullSpaceAxisValues)
	{
		int[] fullDimensions = referenceDataset.getDimensions();
		
		this.fullSpaceAxisValues = fullSpaceAxisValues;
		
		this.fullDimensionsLength = fullDimensions.length;
		
		int inputAxesLength = fullSpaceAxisValues.length;

		if (inputAxesLength != this.fullDimensionsLength)
			throw new IllegalArgumentException("specified axes of interest are not the correct length");

		this.firstFixedAxis = Integer.MAX_VALUE;
		for (int i = 0; i < inputAxesLength; i++)
		{
			if (fullSpaceAxisValues[i] != -1)
			{
				this.firstFixedAxis = i;
				i = inputAxesLength;
			}
		}
		
		int[] tempValues = new int[inputAxesLength];
		int[] tempIndices = new int[inputAxesLength];
		int numAxesInView = 0;
		for (int i = 0; i < inputAxesLength; i++)
		{
			if (fullSpaceAxisValues[i] == -1)
			{
				tempValues[numAxesInView] = fullDimensions[i]; 
				tempIndices[numAxesInView] = i;
				numAxesInView++;
			}
		}

		if (numAxesInView == 0)
			throw new IllegalArgumentException("no axes of interest specified");
		
		this.viewDimensions = new int[numAxesInView];
		for (int i = 0; i < numAxesInView; i++)
			this.viewDimensions[i] = tempValues[i];
		
		this.viewAxesIndices = new int[numAxesInView];
		for (int i = 0; i < numAxesInView; i++)
			this.viewAxesIndices[i] = tempIndices[i];
		
		this.fullSpaceDataset = referenceDataset;

		this.metadata = new MetaData(); // TODO - or from reference dataset???
	
		this.parent = null;
		
		this.fullSpaceIndex = fullSpaceAxisValues.clone();
		
		this.oneDimWorkspace = new int[1];
		
		this.viewDimensionsLength = this.viewDimensions.length;
	}
	
	private void fillFullSpaceIndex(int[] fromSubspaceIndex)
	{
		for (int i = 0; i < this.viewDimensionsLength; i++)
		{
			int indexOfAxis = this.viewAxesIndices[i];
			this.fullSpaceIndex[indexOfAxis] = fromSubspaceIndex[i];
		}
	}
	
	@Override
	public int[] getDimensions()
	{
		return this.viewDimensions;
	}

	@Override
	public Type getType()
	{
		return this.fullSpaceDataset.getType();
	}

	@Override
	public MetaData getMetaData()
	{
		return this.metadata;
	}

	@Override
	public void setMetaData(MetaData metadata)
	{
		this.metadata = metadata;
	}

	@Override
	public boolean isComposite()
	{
		return true;
	}

	@Override
	public Dataset getParent()
	{
		return this.parent;
	}

	@Override
	public void setParent(Dataset dataset)
	{
		this.parent = dataset;
	}

	@Override
	public Object getData()
	{
		return null;
	}

	@Override
	public void releaseData()
	{
		// do nothing
	}

	@Override
	public void setData(Object data)
	{
		throw new IllegalArgumentException("cannot setData() on a DatasetView");
	}

	@Override
	public Dataset insertNewSubset(int position)
	{
		throw new IllegalArgumentException("cannot insertNewSubset() on a DatasetView");
	}

	@Override
	public Dataset removeSubset(int position)
	{
		throw new IllegalArgumentException("cannot removeSubset() on a DatasetView");
	}

	@Override
	public Dataset getSubset(int position)
	{
		this.oneDimWorkspace[0] = position;
		return getSubset(this.oneDimWorkspace);
	}

	/*
	ds = [2,3,4,5];

	view = [-1,-1,2,3];
	view.is2d();
	view.getSubset([] should return [2,3] of master dataset);
	view.getSubset([i] should return [i,2,3] of master dataset);
	view.getSubset([j,i] should return [j,i,2,3] of master dataset);
	
	view = [-1,-1,-1,3];
	view.is3d();
	view.getSubset([] should return [3] of master dataset);
	view.getSubset([i] should return [i,3] of master dataset);
	view.getSubset([j,i] should return [j,i,3] of master dataset);
	view.getSubset([k,j,i] should return [k,j,i,3] of master dataset);
	
	view = [-1,-1,3,-1];
	view.is3d();
	view.getSubset([] is broken as it returns master dataset but z not constrained to 3);
	view.getSubset([i] should work - use index [3,i] of master dataset);
	view.getSubset([j,i] should work - use index [j,3,i] of master dataset);
	view.getSubset([k,j,i] should work - use index [k,j,3,i] of master dataset);
	
	view = [-1,3,-1,-1];
	view.is3d();
	view.getSubset([] is broken as it returns master dataset but y not constrained to 3);
	view.getSubset([i] will not work - gives back a 3d subset of master dataset but y is not constrained to 3);
	view.getSubset([j,i] should work - use index [3,j,i] of master dataset);
	view.getSubset([k,j,i] should work - use index [k,3,j,i] of master dataset);
	
	view = [3,-1,-1,-1];
	view.is3d();
	view.getSubset([] is broken as it returns master dataset but x not constrained to 3);
	view.getSubset([i] will not work - gives back a 3d subset of master dataset but y is not constrained to 3);
	view.getSubset([j,i] will not work - gives back a 2d subset of master dataset but x is not constrained to 3);
	view.getSubset([k,j,i] should work - use index [3,k,j,i] of master dataset);
	
	if no fixed dims left of my last partial index axis then its safe to subset
	
	so scan from right to left
	maybe the way to do this is via a loop that does this:
	  make a partial index of full space dataset
	  Dataset curr = this.parentDS;
	  loop looking from right to left until partial index exhausted
	    calling curr = curr.getSubset(partialIndex[i--])
	    rethrow exceptions if needed
	  return curr
	  
	*/
	
	@Override
	public Dataset getSubset(int[] index)
	{
		int[] partialFullSpaceIndex = createPartialFullSpaceIndex(index);
	
		if (anyAxesFixedLeftOfPartialIndex(partialFullSpaceIndex))
			throw new IllegalArgumentException("dataset has too many fixed axes to successfully find subset with given partial index");
		
		return this.fullSpaceDataset.getSubset(partialFullSpaceIndex);
	}

	private boolean anyAxesFixedLeftOfPartialIndex(int[] partialFullSpaceIndex)
	{
		int unreferencedAxes = this.fullDimensionsLength - partialFullSpaceIndex.length;
		
		return (this.firstFixedAxis < unreferencedAxes);
	}
	
	private int[] createPartialFullSpaceIndex(int[] viewSpaceIndex)
	{
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		int fullSpaceIndex = this.fullDimensionsLength - 1;

		while ((fullSpaceIndex >= 0) && (this.fullSpaceAxisValues[fullSpaceIndex] != -1))
		{
			indices.add(0, this.fullSpaceAxisValues[fullSpaceIndex]);
			fullSpaceIndex--;
		}
		
		int viewIndex = viewSpaceIndex.length-1;
		while (viewIndex >= 0)
		{
			// add specified view coord axis
			indices.add(0, viewSpaceIndex[viewIndex]);
			viewIndex--;
			fullSpaceIndex--;
			
			// add any other fixed axes present
			while ((fullSpaceIndex >= 0) && (this.fullSpaceAxisValues[fullSpaceIndex] != -1))
			{
				indices.add(0, this.fullSpaceAxisValues[fullSpaceIndex]);
				fullSpaceIndex--;
			}
		}
		
		int partialIndexSize = indices.size();
		
		int[] partialFullSpaceIndex = new int [partialIndexSize];
		
		for (int i = 0; i < partialIndexSize; i++)
			partialFullSpaceIndex[i] = indices.get(i);
		
		return partialFullSpaceIndex;
	}
	
	@Override
	public double getDouble(int[] position)
	{
		fillFullSpaceIndex(position);
		return this.fullSpaceDataset.getDouble(this.fullSpaceIndex);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		fillFullSpaceIndex(position);
		this.fullSpaceDataset.setDouble(this.fullSpaceIndex, value);
	}

	@Override
	public long getLong(int[] position)
	{
		fillFullSpaceIndex(position);
		return this.fullSpaceDataset.getLong(this.fullSpaceIndex);
	}

	@Override
	public void setLong(int[] position, long value)
	{
		fillFullSpaceIndex(position);
		this.fullSpaceDataset.setLong(this.fullSpaceIndex, value);
	}

}
