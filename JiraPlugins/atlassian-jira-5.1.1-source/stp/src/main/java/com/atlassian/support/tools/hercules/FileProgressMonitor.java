package com.atlassian.support.tools.hercules;

public interface FileProgressMonitor
{
	void setTotalSize(long size);

	void setProgress(long numRead);

	String getLogFilePath();

	boolean isCancelled();
}
