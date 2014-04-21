package com.atlassian.support.tools.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.mutable.MutableLong;
import org.apache.log4j.Logger;

import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.mail.MailUtility;
import com.atlassian.support.tools.scheduler.settings.ScheduledTaskSettings;

public class SupportScheduledTaskControllerImpl implements SupportScheduledTaskController
{
	private static final Logger log = Logger.getLogger(SupportScheduledTaskControllerImpl.class);
	private final PluginScheduler pluginScheduler;

	private final Map<String, ScheduledTaskSettings> settingsMap = new HashMap<String, ScheduledTaskSettings>();
	private Map<String, Object> jobDataMap = new TreeMap<String, Object>();

	public static final String APP_INFO_KEY = "appInfo";
	public static final String MAIL_UTILITY_KEY = "mailUtility";

	private SupportScheduledTaskControllerImpl(PluginScheduler pluginScheduler, SupportApplicationInfo info, MailUtility mailUtility, ScheduledTaskSettings... settingsArray)
	{
		this.pluginScheduler = pluginScheduler;

		jobDataMap.put(APP_INFO_KEY, info);
		jobDataMap.put(MAIL_UTILITY_KEY, mailUtility);

		for(ScheduledTaskSettings settings: settingsArray)
		{
			settingsMap.put(settings.getTaskId(), settings);
		}
	}

	@Override
	public ScheduledTaskSettings getTaskSettings(String id)
	{
		return settingsMap.get(id);
	}
	
	@Override
	public void onStart()
	{
		log.debug("Starting support scheduled task controller...");

		for(ScheduledTaskSettings settings: settingsMap.values())
		{
			if(log.isDebugEnabled())
				log.debug("Performing startup checks for task type '" + settings.getTaskId() + "'");
			
			scheduleTask(settings);
		}
	}

	private void unscheduleTask(ScheduledTaskSettings settings)
	{
		try
		{
			pluginScheduler.unscheduleJob(settings.getTaskId());
		}
		catch(IllegalArgumentException e)
		{
			if(log.isDebugEnabled())
				log.debug("I tried to unschedule task '" + settings.getTaskId() + "', but it wasn't running.  This is a normal safety check to ensure that disabled jobs are not run.");
		}
	}

	private void scheduleTask(ScheduledTaskSettings settings)
	{
		if(!settings.isEnabled())
		{
			if(log.isInfoEnabled())
				log.info("Scheduled task " + settings.getTaskId() + " is disabled.");
			return;
		}

		Map<String, Object> taskJobData = new TreeMap<String, Object>(jobDataMap);
		taskJobData.put(ScheduledTaskSettings.SETTINGS_KEY, settings);
		taskJobData.put(ScheduledTaskSettings.LAST_RUN_KEY, new MutableLong());
		
		pluginScheduler.scheduleJob(settings.getTaskId(), settings.getTaskClass(), taskJobData, settings.getStartTime(), settings.getFrequencyMs());
		
		if(log.isInfoEnabled())
			log.info("Task '" + settings.getTaskId() + "' will be executed every " + settings.getFrequencyMs() + " ms starting on " + settings.getStartTime() + ".");
	}

	@Override
	public void onSettingsChanged(ScheduledTaskSettings settings)
	{
		unscheduleTask(settings);
		scheduleTask(settings);
	}
}
