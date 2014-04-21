package com.atlassian.support.tools.scheduler.settings;

import java.util.Date;

import com.atlassian.sal.api.scheduling.PluginJob;

public interface ScheduledTaskSettings
{
	public static final String SETTINGS_KEY = "scheduledTaskSettings";
	public static final String LAST_RUN_KEY = "lastRunTracker";
	public static final long MINIMUM_WAIT_PERIOD = 120000;
	
	public static final String DAILY = "daily";
	public static final long DAILY_MS = 1000 * 60 * 60 * 24;
	public static final String WEEKLY = "weekly";
	public static final long WEEKLY_MS = DAILY_MS * 7;

	public boolean isEnabled();

	public String getFrequencyName();
	
	public long getFrequencyMs();

	public Date getStartTime();

	public Class<? extends PluginJob> getTaskClass();

	public String getTaskId();

	public String getViewTemplateFile();

	public String getEditTemplateFile();
}
