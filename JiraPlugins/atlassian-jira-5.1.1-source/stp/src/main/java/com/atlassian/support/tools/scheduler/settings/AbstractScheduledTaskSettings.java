package com.atlassian.support.tools.scheduler.settings;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public abstract class AbstractScheduledTaskSettings implements ScheduledTaskSettings
{
	private static final Logger log = Logger.getLogger(AbstractScheduledTaskSettings.class);

	private static final int DEFAULT_FREQUENCY = 86400000;
	protected final PluginSettings settings;

	public AbstractScheduledTaskSettings(PluginSettingsFactory pluginSettingsFactory)
	{
//		this.settings = pluginSettingsFactory.createSettingsForKey(getTaskId());
		this.settings = pluginSettingsFactory.createGlobalSettings();
	}

	@Override
	public Date getStartTime()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);

//		String strVal = (String) settings.get("startTime");
		String strVal = (String) settings.get(getTaskId() + ".startTime");
		if( ! StringUtils.isEmpty(strVal))
		{
			String[] tokens = StringUtils.split(strVal, ':');
			if(tokens.length == 2 && StringUtils.isNumeric(tokens[0]) && StringUtils.isNumeric(tokens[1]))
			{
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tokens[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(tokens[1]));
			}
		}

		return cal.getTime();
	}

	public void setStartTime(int hour, int min)
	{
//		settings.put("startTime", hour + ":" + min);
		settings.put(getTaskId() + ".startTime", hour + ":" + min);
	}

	
	 @Override
	public String getFrequencyName() {
		 if (getFrequencyMs() == ScheduledTaskSettings.DAILY_MS) {
			 return DAILY;
		 }
		 else if (getFrequencyMs() == ScheduledTaskSettings.WEEKLY_MS) {
			 return WEEKLY;
		 }
		 
		 return null;
	}
	
	@Override
	public long getFrequencyMs()
	{
//		String strVal = (String) settings.get("frequency");
		String strVal = (String) settings.get(getTaskId() + ".frequency");
		if(StringUtils.isEmpty(strVal))
			return DEFAULT_FREQUENCY;

		if( ! StringUtils.isNumeric(strVal))
			return DEFAULT_FREQUENCY;

		try
		{
			return Long.parseLong(strVal);
		}
		catch(NumberFormatException e)
		{
			log.error("Failed to parse frequency for " + getTaskId() + " '" + strVal + "'");
			return DEFAULT_FREQUENCY;
		}
	}

	public void setFrequency(long frequency)
	{
//		settings.put("frequency", String.valueOf(frequency));
		settings.put(getTaskId() + ".frequency", String.valueOf(frequency));
	}

	@Override
	public boolean isEnabled()
	{
//		return "true".equals(settings.get("enabled"));
		return "true".equals(settings.get(getTaskId() + ".enabled"));
	}

	public void setEnabled(boolean isEnabled)
	{
//		settings.put("enabled", String.valueOf(isEnabled));
		settings.put(getTaskId() + ".enabled", String.valueOf(isEnabled));
	}
}
