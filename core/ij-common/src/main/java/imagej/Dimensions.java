//
// Dimensions.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej;

/**
 * General purpose methods.
 *
 * @author Barry DeZonia
 */
public final class Dimensions {

	private Dimensions() {
		// prevent instantiation of utility class
	}

	/** gets the last n-2 dimensions of a n-dimensional int array. */
	public static int[] getDims3AndGreater(int[] fullDims)
	{
		if (fullDims.length < 2)
			throw new IllegalArgumentException("Image must be at least 2-D");

		int[] extraDims = new int[fullDims.length-2];

		for (int i = 0; i < extraDims.length; i++)
			extraDims[i] = fullDims[i+2];

		return extraDims;
	}

	/** returns, as a long, the total number of samples present in an image of given dimensions */
	public static long getTotalSamples(int[] dimensions)
	{
		int numDims = dimensions.length;

		if (numDims == 0)
			return 0;

		long totalSamples = 1;

		for (int i = 0; i < numDims; i++)
			totalSamples *= dimensions[i];

		return totalSamples;
	}

	/** returns the number of planes present in an image of given dimensions. assumes the planes lie
	 *  in the 1st two dimensions
	 */
	public static long getTotalPlanes(int[] dimensions)
	{
		int numDims = dimensions.length;

		if (numDims < 2)
			return 0;

		if (numDims == 2)
			return 1;

		// else numDims > 2

		int[] sampleSpace = getDims3AndGreater(dimensions);

		return getTotalSamples(sampleSpace);
	}

	/** throws an exception if the combination of origins and spans is outside an image's dimensions */
	public static void verifyDimensions(int[] imageDimensions, int[] origin, int[] span)
	{
		// span dims should match origin dims
		if (origin.length != span.length)
			throw new IllegalArgumentException("verifyDimensions() : origin (dim="+origin.length+") and span (dim="+span.length+") arrays are of differing sizes");

		// origin/span dimensions should match image dimensions
		if (origin.length != imageDimensions.length)
			throw new IllegalArgumentException("verifyDimensions() : origin/span (dim="+origin.length+") different size than input image (dim="+imageDimensions.length+")");

		// make sure origin in a valid range : within bounds of source image
		for (int i = 0; i < origin.length; i++)
			if ((origin[i] < 0) || (origin[i] >= imageDimensions[i]))
				throw new IllegalArgumentException("verifyDimensions() : origin outside bounds of input image at index " + i);

		// make sure span in a valid range : >= 1
		for (int i = 0; i < span.length; i++)
			if (span[i] < 1)
				throw new IllegalArgumentException("verifyDimensions() : span size < 1 at index " + i);

		// make sure origin + span within the bounds of the input image
		for (int i = 0; i < span.length; i++)
			if ( (origin[i] + span[i]) > imageDimensions[i] )
				throw new IllegalArgumentException("verifyDimensions() : span range (origin+span) beyond input image boundaries at index " + i);
	}


	/** given an array of dimensions this returns the number of axes whose dimension is > 1 */
	public static int countNontrivialDimensions(int[] dimensions)
	{
		int numNonTrivial = 0;
		
		for (int dim : dimensions)
			if (dim > 1)
				numNonTrivial++;
		
		return numNonTrivial;
	}

}
