package imagej.dataset;

import imagej.MetaData;
import imagej.data.Type;

import java.util.ArrayList;

// TODO - our convention is that indexing subsets moves right to left. seems to match imglib

// TODO - I'm not checking that user provided axis values are in a valid range in any of the methods below

// TODO - note that cannot add subsets/remove subsets along user specified access. Right now it just works on the outermost one. It would be
//   great if we could add a blob and have it fixup dimensions and data as needed. In general we could insert one "slice" at a time where a slice
//   is a Dataset whose dimensions match my dimensions except in 1 axis where its value is 1.

// TODO - metadata support is nearly nonexistent. May need global metadata (num dims in primitive access) and then per subset metadata (labels)

// TODO - maybe it would be speed things up considerably to cache the last subset found with getSubset(axis)

public class CompositeDataset implements Dataset, RecursiveDataset
{
	private Type type;
	private int[] dimensions;
	private Dataset parent;
	private MetaData metadata;
	private ArrayList<Dataset> subsets;

	public CompositeDataset(Type type, int[] dimensions, ArrayList<Dataset> subsets)
	{
		this.type = type;
		this.dimensions = dimensions;
		this.subsets = subsets;
		this.parent = null;
		this.metadata = null;
		for (Dataset subset : this.subsets)
			subset.setParent(this);
	}
	
	@Override
	public int[] getDimensions()
	{
		return this.dimensions;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
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
	public double getDouble(int[] index, int axis)
	{
		RecursiveDataset subset = (RecursiveDataset) getSubset(index[axis]); 
		return subset.getDouble(index, axis-1);
	}

	@Override
	public void setDouble(int[] index, int axis, double value)
	{
		RecursiveDataset subset = (RecursiveDataset) getSubset(index[axis]); 
		subset.setDouble(index, axis-1, value);
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
		// throw new IllegalArgumentException("CompositeDatasets do not support getting data array");
	}

	@Override
	public void setData(Object data)
	{
		throw new IllegalArgumentException("CompositeDatasets do not support setting data array");
	}

	@Override
	public void releaseData()
	{
		// not necessary to do anything
	}
	
	@Override
	public Dataset insertNewSubset(int position)
	{
		if (this.parent != null)
			throw new IllegalArgumentException("can only add subsets to outermost Dataset");

		int[] subsetDimensions = new int[this.dimensions.length-1];
		
		for (int i = 0; i < subsetDimensions.length; i++)
			subsetDimensions[i] = this.dimensions[i];
		
		Dataset ds = new PlanarDatasetFactory().createDataset(this.type, subsetDimensions);
		
		ds.setParent(this);
		
		this.subsets.add(position, ds);
		
		int outermostAxis = this.dimensions.length - 1;
		
		this.dimensions[outermostAxis]++;
		
		return ds;
	}

	@Override
	public Dataset removeSubset(int position)
	{
		if (this.parent != null)
			throw new IllegalArgumentException();
		
		Dataset ds = this.subsets.remove(position);
		
		ds.setParent(null);
		
		int outermostAxis = this.dimensions.length - 1;
		
		this.dimensions[outermostAxis]--;

		return ds;
	}

	@Override
	public Dataset getSubset(int position)
	{
		return this.subsets.get(position);
	}

	@Override
	public double getDouble(int[] position)
	{
		// WRONG????
		//if (this.parent != null)
		//	throw new IllegalArgumentException();

		int outermostAxis = this.dimensions.length - 1;
		
		return getDouble(position, outermostAxis);
	}

	@Override
	public void setDouble(int[] position, double value)
	{
		// WRONG????
		//if (this.parent != null)
		//	throw new IllegalArgumentException();
		
		int outermostAxis = this.dimensions.length - 1;

		setDouble(position, outermostAxis, value);
	}

	@Override
	public Dataset getSubset(int[] partialIndex, int axis)
	{
		if (axis < 0)
			throw new IllegalArgumentException("axis index ("+axis+") less than 0");

		RecursiveDataset nextSubset = (RecursiveDataset)getSubset(partialIndex[axis]);
		
		if (axis == 0)
			return (Dataset) nextSubset;
		
		return nextSubset.getSubset(partialIndex, axis-1);
	}

	@Override
	public Dataset getSubset(int[] index)
	{
		int outermostAxis = index.length - 1;

		return getSubset(index, outermostAxis);
	}
	
}
