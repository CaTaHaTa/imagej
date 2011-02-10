package imagej.iterator;

import imagej.Dimensions;
import imagej.dataset.Dataset;
import imagej.process.Index;
import imagej.process.Span;

/** A SynchronizedIterator simultaneously iterates over N Datasets across a given span. Currently it is written to
 *  iterate datasets in the order that each one organizes its primitive data in order to minimize index lookup times.
 *  However a more straightforward implementation might be just as efficient.
 */
public class SynchronizedIterator
{
	// ************ instance variables *********************************************************************************

	private Dataset[] datasets;
	private Dataset[] directAccessDatasets;
	private int[][] outerPositions;
	private int[][] innerPositions;
	private int[][] outerOrigins;
	private int[][] innerOrigins;
	private int[][] outerSpans;
	private int[][] innerSpans;
	private double[] doubleWorkspace;
	private long[] longWorkspace;
	private int datasetCount;
	
	// ************ constructor *********************************************************************************
	
	/** Constructs a SynchronizedIterator from input Datasets and regions. The user specifies if internal workspace
	 *  should be filled with doubles or longs.
	 */
	public SynchronizedIterator(Dataset[] datasets, int[][] origins, int[] span, boolean workingInReals)
	{
		for (int i = 0; i < datasets.length; i++)
			Dimensions.verifyDimensions(datasets[i].getDimensions(), origins[i], span);
		
		this.datasets = datasets;
		if (workingInReals)
			this.doubleWorkspace = new double[datasets.length];
		else
			this.longWorkspace = new long[datasets.length];
		this.datasetCount = datasets.length;
		this.directAccessDatasets = new Dataset[this.datasetCount];
		this.outerPositions = new int[this.datasetCount][];
		this.innerPositions = new int[this.datasetCount][];
		this.outerOrigins = new int[this.datasetCount][];
		this.innerOrigins = new int[this.datasetCount][];
		this.outerSpans = new int[this.datasetCount][];
		this.innerSpans = new int[this.datasetCount][];
		for (int i = 0; i < this.datasetCount; i++)
		{
			int[] dimensions = this.datasets[i].getDimensions();
			int directAxisCount = this.datasets[i].getMetaData().getDirectAccessDimensionCount();
			int outerSize = dimensions.length - directAxisCount;
			int innerSize = directAxisCount;
			this.outerPositions[i] = Index.create(outerSize);
			this.innerPositions[i] = Index.create(innerSize);
			this.outerOrigins[i] = Index.create(outerSize);
			for (int j = 0; j < outerSize; j++)
			{
				this.outerOrigins[i][j] = origins[i][j+innerSize];
				this.outerPositions[i][j] = origins[i][j+innerSize];
			}
			this.innerOrigins[i] = Index.create(innerSize);
			for (int j = 0; j < innerSize; j++)
			{
				this.innerOrigins[i][j] = origins[i][j];
				this.innerPositions[i][j] = origins[i][j];
			}
			int[] outerSpan = new int[outerSize];
			for (int j = 0; j < outerSize; j++)
				outerSpan[j] = span[innerSize+j];
			int[] innerSpan = new int[innerSize];
			for (int j = 0; j < innerSize; j++)
				innerSpan[j] = span[j];
			this.outerSpans[i] = Span.create(outerSpan);
			this.innerSpans[i] = Span.create(innerSpan);
		}
	}
	
	// ************ public interface *********************************************************************************

	/** returns true if the iterator's current position contains valid data */
	public boolean positionValid()
	{
		for (int i = 0; i < this.datasetCount; i++)
		{
			boolean outerIsValid = Index.isValid(this.outerPositions[i], this.outerOrigins[i], this.outerSpans[i]);
			
			if (!outerIsValid)
				return false;
			
			// past here we know outer is valid
			// but there is a case where its valid and yet the whole position is invalid: when outerPosition == []
			//   and not innerIsValid
			
			if (this.outerPositions[i].length == 0)
			{
				boolean innerIsValid = Index.isValid(this.innerPositions[i], this.innerOrigins[i], this.innerSpans[i]); 
				
				if (!innerIsValid)
					return false;
			}
		}
		
		return true;
	}

	/** loads internal workspace with data from the iterator's current postion */
	public void loadWorkspace()
	{
		for (int i = 0; i < this.datasetCount; i++)
		{
			if (this.directAccessDatasets[i] == null) // first pass
			{
				this.directAccessDatasets[i] = this.datasets[i].getSubset(this.outerPositions[i]);
			}
			
			if (this.doubleWorkspace != null)
				this.doubleWorkspace[i] = this.directAccessDatasets[i].getDouble(this.innerPositions[i]);
			else
				this.longWorkspace[i] = this.directAccessDatasets[i].getLong(this.innerPositions[i]);
		}
	}

	/** moves the iterator position forward */
	public void incrementPosition()
	{
		int[] innerPosition;
		int[] innerOrigin;
		int[] innerSpan;
		int[] outerPosition;
		int[] outerOrigin;
		int[] outerSpan;

		for (int i = 0; i < this.datasetCount; i++)
		{
			innerPosition = this.innerPositions[i];
			innerOrigin = this.innerOrigins[i];
			innerSpan = this.innerSpans[i];
			
			Index.increment(innerPosition, innerOrigin, innerSpan);
			
			if (!Index.isValid(innerPosition, innerOrigin, innerSpan))
			{
				outerPosition = this.outerPositions[i];
				
				if (outerPosition.length == 0)
					return;
				
				outerOrigin = this.outerOrigins[i];
				
				outerSpan = this.outerSpans[i];

				Index.increment(outerPosition, outerOrigin, outerSpan);
				
				if (Index.isValid(outerPosition, outerOrigin, outerSpan))
				{
					this.directAccessDatasets[i] = this.datasets[i].getSubset(outerPosition);
					
					for (int j = 0; j < innerPosition.length; j++)
						innerPosition[j] = this.innerOrigins[i][j];
				}
			}
		}
	}

	/** changes the value of a Dataset, specified by its offset within the collection of Datasets within the iterator,
	 * at its current position to the specified long value.
	 */
	public void setLong(int datasetNumber, long value)
	{
		int[] subPosition = this.innerPositions[datasetNumber];
		this.directAccessDatasets[datasetNumber].setLong(subPosition, value);
	}
	
	/** changes the value of a Dataset, specified by its offset within the collection of Datasets within the iterator,
	 * at is current position to the specified double value.
	 */
	public void setDouble(int datasetNumber, double value)
	{
		int[] subPosition = this.innerPositions[datasetNumber];
		this.directAccessDatasets[datasetNumber].setDouble(subPosition, value);
	}

	/** returns the internal workspace of longs this iterator owns. Is null if user specified to work in doubles in constructor. */
	public long[] getLongWorkspace()
	{
		return this.longWorkspace;
	}

	/** returns the internal workspace of doubles this iterator owns. Is null if user specified to work in longs in constructor. */
	public double[] getDoubleWorkspace()
	{
		return this.doubleWorkspace;
	}
}

