package com.atlassian.support.tools.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.atlassian.sisyphus.DefaultSisyphusPatternMatcher;
import com.atlassian.sisyphus.SisyphusPatternMatcher;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.hercules.FileProgressMonitorInputStream;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.hercules.WebMatchResultVisitor;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.mail.MailUtility;
import com.atlassian.support.tools.salext.mail.ProductAwareEmail;
import com.atlassian.support.tools.scheduler.settings.HerculesScheduledTaskSettings;
import com.atlassian.support.tools.scheduler.settings.ScheduledTaskSettings;
import com.atlassian.support.tools.scheduler.utils.RenderingUtils;
import com.atlassian.templaterenderer.RenderingException;

public class ScheduledHerculesScanTask extends AbstractScheduledTask
{
	private static final Logger log = Logger.getLogger(ScheduledHerculesScanTask.class);

	@Override
	public void doExecute(Map<String, Object> jobDataMap)
	{
		MailUtility mailUtility = (MailUtility) jobDataMap.get(SupportScheduledTaskControllerImpl.MAIL_UTILITY_KEY);
		SupportApplicationInfo info = (SupportApplicationInfo) jobDataMap.get(SupportScheduledTaskControllerImpl.APP_INFO_KEY);
		HerculesScheduledTaskSettings settings = (HerculesScheduledTaskSettings) jobDataMap.get(ScheduledTaskSettings.SETTINGS_KEY);

		if(mailUtility == null || info == null)
		{
			log.error("A required utility class was not provided, can't execute.");
			return;
		}

		List<ScanItem> applicationLogs = info.getApplicationLogFilePaths();
		if(applicationLogs == null || applicationLogs.size() == 0)
		{
			log.error("Couldn't find any application logs to scan, can't continue.");
			return;
		}

		final String logFilePath = applicationLogs.get(0).getPath();
		File primaryLog = new File(logFilePath);
		if(!primaryLog.exists())
		{
			log.error("Log file '" + logFilePath + "' doesn't exist, can't continue with the scan.");
			return;
		}

		if(log.isInfoEnabled())
			log.info("Scanning log file '" + logFilePath + "'...");

		final WebMatchResultVisitor visitor = new WebMatchResultVisitor(logFilePath);

		// Get the rules using a RemoteXMLPatternSource
		SisyphusPatternSource patternSource;
		long time = System.currentTimeMillis();
		try
		{
			patternSource = info.getPatternSource();

			// Run SisyphusPatternMatcher.match against the results
			final SisyphusPatternMatcher spm = new DefaultSisyphusPatternMatcher(patternSource);

			InputStream in = new FileProgressMonitorInputStream(new File(logFilePath), visitor);
			final BufferedReader br = new BufferedReader(new InputStreamReader(in));
			try
			{
				spm.match(br, visitor);
			}
			catch(InterruptedException e)
			{
				visitor.setCancelled();
			}
			catch(IOException e)
			{
				visitor.scanFailed(e);
			}
			finally
			{
				visitor.scanCompleted();
				try
				{
					br.close();
				}
				catch(Exception e)
				{
					log.debug(e.getMessage(), e);
				}
			}
		}
		catch(MalformedURLException e)
		{
			visitor.scanFailed(e);
		}
		catch(IOException e)
		{
			visitor.scanFailed(e);
		}
		catch(ClassNotFoundException e)
		{
			log.error("Error running scheduled Hercules scan:", e);
		}

		if(visitor.getScanException() != null)
		{
			log.error("There was an exception when scanning log file "+visitor.getLogFilePath()+" using Hercules: "+visitor.getScanException().getMessage(), visitor.getScanException());
			return;
		}
		else if(visitor.getResults() == null && visitor.getResults().size() == 0)
		{
			log.warn("No results were returned when scanning log file "+visitor.getLogFilePath());
			return;
		}
		else
		{
			log.info("Finished scanning "+visitor.getLogFilePath()+" using Hercules. Total size: "+visitor.getTotalSize()+" bytes. Time taken: "+(System.currentTimeMillis()-time)+" ms. Patterns matched: "+visitor.getResults().size());
		}

		log.info("Preparing hercules report...");
		// prepare the results
		try
		{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("info", info);
			params.put("fileName", FilenameUtils.getName(visitor.getLogFilePath()));
			params.put("results", visitor.getResults().values());
			params.put("scanException", visitor.getScanException());
			
			String mailBody = RenderingUtils.render(info.getTemplateRenderer(), "/templates/email/hercules-report.vm", params);

			String recipients = settings.getRecipients();
			ProductAwareEmail email = new ProductAwareEmail(recipients,info);
			email.setFrom(info.getFromAddress());
			email.setSubject(info.getText("stp.scheduler.hercules.mail.subject", DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));

			email.setBody(mailBody);
			email.setMimeType("text/html");
			
			// send the email
			log.info("Sending Hercules report...");
			mailUtility.sendMail(email);
		}
		catch(RenderingException e)
		{
			log.error("Error rendering Hercules report: "+e.getMessage(), e);
		}
		catch(IOException e)
		{
			log.error("I/O error while generating Hercules report: "+e.getMessage(), e);
		}
	}

	@Override
	protected String getName() {
		return "Hercules scheduled log scan";
	}

}
