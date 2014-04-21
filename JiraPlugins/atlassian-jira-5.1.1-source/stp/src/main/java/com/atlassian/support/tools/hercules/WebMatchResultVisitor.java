package com.atlassian.support.tools.hercules;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import com.atlassian.sisyphus.DefaultMatchResultVisitor;

public class WebMatchResultVisitor extends DefaultMatchResultVisitor implements FileProgressMonitor
{
	private static final String SESSION_KEY = "HERCULES_SCANNING_MONITOR";
	private boolean isScanComplete;
	private Exception scanException;
	private final String logFilePath;

	public WebMatchResultVisitor(String logFilePath)
	{
		this.logFilePath = logFilePath;
	}

	public String getPrettyErrorMessage()
	{
		return this.prettyErrorMessage;
	}

	public void setPrettyErrorMessage(String prettyErrorMessage)
	{
		this.prettyErrorMessage = prettyErrorMessage;
	}

	private String prettyErrorMessage;
	private double bytesRead = 0;
	private double totalBytes = 0;

	public void scanCompleted()
	{
		this.isScanComplete = true;
	}

	public boolean isScanComplete()
	{
		return this.isScanComplete;
	}

	public void detach(HttpSession httpSession)
	{
		httpSession.removeAttribute(SESSION_KEY);
	}

	void attach(HttpSession httpSession)
	{
		httpSession.setAttribute(SESSION_KEY, this);
	}

	public void scanFailed(IOException e)
	{
		this.scanException = e;

		if(e instanceof FileNotFoundException)
		{
			this.prettyErrorMessage = "<p>The log file you asked me to scan doesn't exist or couldn't be read:</p>\r\n<p>"
					+ e.getMessage() + "</p>\r\n";
		}
		else
		{
			this.prettyErrorMessage = "<p>There was a problem reading the log file you asked me to scan:</p>\r\n<p>"
					+ e.getMessage() + "</p>\r\n";
		}
	}

	public Exception getScanException()
	{
		return this.scanException;
	}

	public static WebMatchResultVisitor getAttachedInstance(HttpSession httpSession)
	{
		return (WebMatchResultVisitor) httpSession.getAttribute(SESSION_KEY);
	}

	@Override
	public void setProgress(long numRead)
	{
		this.bytesRead = numRead;
	}

	public int getPercentRead()
	{
		if(this.totalBytes > 0)
		{
			return Math.min(100, (int) Math.round(this.bytesRead / this.totalBytes * 100.0));
		}
		else
		{
			return 0;
		}
	}

	@Override
	public void setTotalSize(long size)
	{
		this.totalBytes = size;
	}

	public int getTotalSize()
	{
		return (int) this.bytesRead;
	}

	@Override
	public String getLogFilePath()
	{
		return this.logFilePath;
	}
}
