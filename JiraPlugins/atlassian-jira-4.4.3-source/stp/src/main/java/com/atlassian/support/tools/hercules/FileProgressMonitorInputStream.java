package com.atlassian.support.tools.hercules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

public class FileProgressMonitorInputStream extends FilterInputStream
{
	private final long size;
	private long nread = 0;
	private final FileProgressMonitor monitor;

	public FileProgressMonitorInputStream(File file, FileProgressMonitor monitor) throws FileNotFoundException
	{
		super(new FileInputStream(file));
		this.monitor = monitor;
		this.size = file.length();
		monitor.setTotalSize(this.size);
	}

	/**
	 * Overrides <code>FilterInputStream.read</code> to update the progress
	 * monitor after the read.
	 */
	@Override
	public int read() throws IOException
	{
		int c = this.in.read();
		// This should always be one byte
		if(c >= 0) this.monitor.setProgress(this.nread += 1);
		if(this.monitor.isCancelled())
		{
			InterruptedIOException exc = new InterruptedIOException("progress");
			exc.bytesTransferred = (int) this.nread;
			throw exc;
		}
		return c;
	}

	/**
	 * Overrides <code>FilterInputStream.read</code> to update the progress
	 * monitor after the read.
	 */
	@Override
	public int read(byte b[]) throws IOException
	{
		int nr = this.in.read(b);
		if(nr > 0) this.monitor.setProgress(this.nread += nr);
		if(this.monitor.isCancelled())
		{
			InterruptedIOException exc = new InterruptedIOException("progress");
			exc.bytesTransferred = (int) this.nread;
			throw exc;
		}
		return nr;
	}

	/**
	 * Overrides <code>FilterInputStream.read</code> to update the progress
	 * monitor after the read.
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		int nr = this.in.read(b, off, len);
		if(nr > 0) this.monitor.setProgress(this.nread += nr);
		if(this.monitor.isCancelled())
		{
			InterruptedIOException exc = new InterruptedIOException("progress");
			exc.bytesTransferred = (int) this.nread;
			throw exc;
		}
		return nr;
	}

	/**
	 * Overrides <code>FilterInputStream.skip</code> to update the progress
	 * monitor after the skip.
	 */
	@Override
	public long skip(long n) throws IOException
	{
		long nr = this.in.skip(n);
		if(nr > 0) this.monitor.setProgress(this.nread += nr);
		return nr;
	}

	/**
	 * Overrides <code>FilterInputStream.close</code> to close the progress
	 * monitor as well as the stream.
	 */
	@Override
	public void close() throws IOException
	{
		this.in.close();
	}

	/**
	 * Overrides <code>FilterInputStream.reset</code> to reset the progress
	 * monitor as well as the stream.
	 */
	@Override
	public synchronized void reset() throws IOException
	{
		this.in.reset();
		this.nread = this.size - this.in.available();
		this.monitor.setProgress(this.nread);
	}

}
