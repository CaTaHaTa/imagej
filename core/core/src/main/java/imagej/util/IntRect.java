/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.util;

/**
 * A class for representing a rectangular region, in integer coordinates. This
 * class is very similar to {@link java.awt.Rectangle}; it mainly exists to
 * avoid problems with AWT, JNI and headless operation. Adapted from BioFormats'
 * Region class.
 * 
 * @author Barry DeZonia
 */
public class IntRect {

	// -- Fields --

	public int x;
	public int y;
	public int width;
	public int height;

	// -- Constructor --

	public IntRect() {
		// default constructor - allow all instance vars to be initialized to 0
	}

	public IntRect(final int x, final int y, final int width, final int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	// -- IntRect methods --

	/** Returns true if this rect intersects the given rect. */
	public boolean intersects(final IntRect r) {
		int tw = this.width;
		int th = this.height;
		int rw = r.width;
		int rh = r.height;
		if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
			return false;
		}
		final int tx = this.x;
		final int ty = this.y;
		final int rx = r.x;
		final int ry = r.y;
		rw += rx;
		rh += ry;
		tw += tx;
		th += ty;
		final boolean rtn =
			(rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) &&
				(th < ty || th > ry);
		return rtn;
	}

	/**
	 * Returns a Rect representing the intersection of this Rect with the given
	 * Rect. If the two Rects do not intersect, the result is an empty Rect.
	 */
	public IntRect intersection(final IntRect r) {
		final int newX = Math.max(this.x, r.x);
		final int newY = Math.max(this.y, r.y);
		int newW = Math.min(this.x + this.width, r.x + r.width) - x;
		int newH = Math.min(this.y + this.height, r.y + r.height) - y;

		if (newW < 0) newW = 0;
		if (newH < 0) newH = 0;

		return new IntRect(newX, newY, newW, newH);
	}

	@Override
	public String toString() {
		return "x=" + x + ", y=" + y + ", w=" + width + ", h=" + height;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof IntRect)) return false;

		final IntRect rect = (IntRect) o;
		return x == rect.x && y == rect.y && width == rect.width &&
			height == rect.height;
	}

	@Override
	public int hashCode() {
		// combine 8 least significant bits of x, y, width and height
		final int b1 = x & 0xff;
		final int b2 = y & 0xff;
		final int b3 = width & 0xff;
		final int b4 = height & 0xff;
		return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
	}

}
