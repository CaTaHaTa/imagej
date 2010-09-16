package imagej.process.operation;

import ij.process.ImageProcessor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;

public class ResetUsingMaskOperation<T extends RealType<T>> extends DualCursorRoiOperation<T>
{
	private byte[] maskPixels;
	private int pixNum;
	
	public ResetUsingMaskOperation(Image<T> img1, int[] origin1, int[] span1, Image<T> img2, int[] origin2, int[] span2, ImageProcessor mask)
	{
		super(img1,origin1,span1,img2,origin2,span2);
		
		this.maskPixels = (byte[])mask.getPixels();
	}
	
	@Override
	public void beforeIteration(RealType<T> type) {
		pixNum = 0;
	}

	@Override
	public void insideIteration(RealType<T> sample1, RealType<T> sample2) {
		if (maskPixels[pixNum++] == 0)
		{
			double pix = sample1.getRealDouble();
			sample2.setReal(pix);
		}
	}
	
	@Override
	public void afterIteration() {
	}

}

