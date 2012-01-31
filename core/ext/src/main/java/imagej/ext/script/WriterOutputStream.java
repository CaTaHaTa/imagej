//
// WriterOutputStream.java
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

package imagej.ext.script;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * An adapter to support scripting languages which want to output to an
 * OutputStream The javax.script framework only has writers for stdout and
 * stderr. With this adapter, we can wrap Writers in OutputStream instances and
 * pass them to the script engine.
 * 
 * @author Johannes Schindelin
 */
public class WriterOutputStream extends OutputStream {

	protected Writer writer;
	protected byte[] buffer = new byte[16384];
	protected int len;

	public WriterOutputStream(final Writer writer) {
		this.writer = writer;
	}

	protected synchronized void ensure(final int length) {
		if (buffer.length >= length) return;

		int newLength = buffer.length * 3 / 2;
		if (newLength < length) newLength = length + 16;
		final byte[] newBuffer = new byte[newLength];
		System.arraycopy(buffer, 0, newBuffer, 0, len);
		buffer = newBuffer;
	}

	@Override
	public synchronized void write(final int b) throws IOException {
		ensure(len + 1);
		buffer[len++] = (byte) b;
		if (b == '\n') flush();
	}

	@Override
	public synchronized void write(final byte[] buffer) throws IOException {
		write(buffer, 0, buffer.length);
	}

	@Override
	public synchronized void write(final byte[] buffer, int offset, int length)
		throws IOException
	{
		int eol = length;
		while (eol > 0)
			if (buffer[eol - 1] == '\n') break;
			else eol--;
		if (eol >= 0) {
			ensure(len + eol);
			System.arraycopy(buffer, offset, this.buffer, len, eol);
			len += eol;
			flush();
			length -= eol;
			if (length == 0) return;
			offset += eol;
		}
		ensure(len + length);
		System.arraycopy(buffer, offset, this.buffer, len, length);
		len += length;
	}

	@Override
	public void close() throws IOException {
		flush();
	}

	@Override
	public synchronized void flush() throws IOException {
		if (len > 0) {
			if (buffer[len - 1] == '\n') len--;
			writer.write(new String(buffer, 0, len));
		}
		len = 0;
	}

}
