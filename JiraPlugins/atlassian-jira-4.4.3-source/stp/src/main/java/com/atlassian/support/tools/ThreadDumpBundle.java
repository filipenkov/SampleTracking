package com.atlassian.support.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.atlassian.support.tools.salext.AbstractApplicationFileBundle;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

/**
 * Shamelessly ripped off Fisheye @see {@link com.cenqua.fisheye.support.ThreadDumpHelper}
 * @author skarimov
 *
 */
public class ThreadDumpBundle extends AbstractApplicationFileBundle
{
	private static final Logger log = Logger.getLogger(ThreadDumpBundle.class);
	
	private final SupportApplicationInfo applicationInfo;

	public ThreadDumpBundle(String key, String title, String description, SupportApplicationInfo applicationInfo)
	{
		super(key, title, description);
		this.applicationInfo = applicationInfo;
	}

	public static final int MAX_THREAD_DEPTH = Integer.MAX_VALUE;
    private static final DateFormat FILE_NAME_TS_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssS");

	@Override
	public List<String> getFiles()
	{
		try
		{
			return Collections.singletonList(dumpThreadsToLogDir().getAbsolutePath());
		}
		catch(IOException e)
		{
			log.error("Failed to generate a thread dump.", e);
			return Collections.emptyList();
		}
	}

	public File dumpThreadsToLogDir() throws IOException
	{
		String name = "threaddump_" + FILE_NAME_TS_FORMAT.format(new Date()) + "-";
		int count = 0;
		final File logDirectory = new File(applicationInfo.getApplicationHome(),"logs/support");
		if(!logDirectory.exists()) 
		{
			logDirectory.mkdirs();
		}
		
		while(new File(logDirectory, name+count+".log").exists())
			count++;
		
		File threadDump = new File(logDirectory, name+count+".log");
		if(!threadDump.createNewFile())
		{
			throw new IOException("Failed to create file "+threadDump.getAbsolutePath());
		}

		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter(new FileWriter(threadDump));
			getThreadDump(pw, applicationInfo);
		}
		finally
		{
			IOUtils.closeQuietly(pw);
		}
		return threadDump;
	}
	
	public void getThreadDump(Appendable a, SupportApplicationInfo info) throws IOException
	{
		a.append(MessageFormat.format("{0} {1} {2} {3}\n Thread dump taken on {4,date,medium} at {4,time,medium}:\n", info.getApplicationName(), info.getApplicationVersion(), info.getApplicationBuildDate(), info.getApplicationBuildNumber(), new Date()));

		final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		if(threadMXBean == null)
		{
			a.append("No thread dump facility available.");
			return;
		}
		
		final ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), MAX_THREAD_DEPTH);
		if(threadInfo == null || threadInfo.length == 0)
		{
			a.append("No thread information was generated.");
			return;
		}


		for(ThreadInfo ti: threadInfo)
		{
			if(ti != null)
			{
				a.append("[").append("" + ti.getThreadId()).append("] ").append(ti.getThreadName()).append(": ")
						.append(ti.getThreadState().toString());
				if(ti.getLockName() != null)
				{
					a.append(" (waiting on ").append(ti.getLockName().trim());
					if(ti.getLockOwnerId() != - 1)
					{
						a.append(" held by ").append("" + ti.getLockOwnerId());
					}
					a.append(")");
				}
				a.append("\n");

				for(StackTraceElement ste: ti.getStackTrace())
				{
					a.append("   ").append(ste.toString()).append("\n");
				}
				a.append("\n");
			}
		}
	}

	@Override
	public void validate(ValidationLog validationLog)
	{
		final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		if(threadMXBean == null)
			log.warn("Thread MXBean is not available. No Thread Dump facility available.");
	}
}
