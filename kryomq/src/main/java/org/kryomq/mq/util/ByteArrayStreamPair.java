package org.kryomq.mq.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteArrayStreamPair {
	protected byte[] buffer;
	
	protected long start;
	protected long end;
	
	protected OutputStream out = new Output();
	protected InputStream in = new Input();
	
	public ByteArrayStreamPair() {
		this(4096);
	}
	
	public ByteArrayStreamPair(int size) {
		buffer = new byte[size];
	}
	
	public boolean available() {
		return end > start;
	}
	
	public OutputStream getOutputStream() {
		return out;
	}
	
	public InputStream getInputStream() {
		return in;
	}
	
	private void grow() {
		byte[] nbuf = new byte[buffer.length * 2];
		int bsize = (int)(end - start);
		int so = (int)(start % buffer.length);
		int eo = (int)(end % buffer.length);
		if(so < eo)
			System.arraycopy(buffer, so, nbuf, 0, eo - so);
		else if(so > eo) {
			System.arraycopy(buffer, so, nbuf, 0, buffer.length - so);
			System.arraycopy(buffer, 0, nbuf, buffer.length - so, eo);
		}
		start = 0;
		end = bsize;
	}

	private class Input extends InputStream {
		@Override
		public int read() throws IOException {
			synchronized(ByteArrayStreamPair.this) {
				while(start == end) {
					try {
						ByteArrayStreamPair.this.wait();
					} catch(InterruptedException ie) {
					}
				}
				return 0xff & buffer[(int)(start++ % buffer.length)];
			}
		}
	}

	private class Output extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			synchronized(ByteArrayStreamPair.this) {
				if(end - start == buffer.length)
					grow();
				buffer[(int)(end++ % buffer.length)] = (byte) b;
			}
		}
	}
}
