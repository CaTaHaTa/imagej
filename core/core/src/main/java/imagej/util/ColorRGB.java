//
// ColorRGB.java
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

package imagej.util;

/** A color with red, green and blue color components. */
public class ColorRGB {

	private final int red;
	private final int green;
	private final int blue;

	public ColorRGB(final int red, final int green, final int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	/** Parses a color from a string of the form "r,g,b". */
	public ColorRGB(final String value) {
		final String[] tokens = value.split(",");
		red = Integer.parseInt(tokens[0]);
		green = Integer.parseInt(tokens[1]);
		blue = Integer.parseInt(tokens[2]);
	}

	public int getRed() {
		return red;
	}

	public int getGreen() {
		return green;
	}

	public int getBlue() {
		return blue;
	}

	public int getAlpha() {
		return 0xff;
	}

	/**
	 * Gets the color as a packed integer, 8 bits per color component.
	 * HSB is alpha, next is red, then green, and finally blue is LSB.
	 */
	public int getARGB() {
		final int a = getAlpha();
		final int r = getRed();
		final int g = getGreen();
		final int b = getBlue();
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	@Override
	public String toString() {
		return red + "," + green + "," + blue;
	}

}
