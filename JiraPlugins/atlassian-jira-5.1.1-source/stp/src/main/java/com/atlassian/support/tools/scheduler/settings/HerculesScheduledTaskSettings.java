package com.atlassian.support.tools.scheduler.settings;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.support.tools.scheduler.ScheduledHerculesScanTask;

public class HerculesScheduledTaskSettings extends AbstractScheduledTaskSettings
{
	private static final String RECIPIENTS = "recipients";
	public static final String TASK_ID = "HerculesScheduledScanTask";

	public HerculesScheduledTaskSettings(PluginSettingsFactory pluginSettingsFactory)
	{
		super(pluginSettingsFactory);
	}

	@Override
	public String getViewTemplateFile()
	{
		return "/templates/scheduler-hercules-view-config.vm";
	}

	@Override
	public String getEditTemplateFile()
	{
		return "/templates/scheduler-hercules-edit-config.vm";
	}

	@Override
	public String getTaskId()
	{
		return TASK_ID;
	}

	@Override
	public Class<? extends PluginJob> getTaskClass()
	{
		return ScheduledHerculesScanTask.class;
	}

	public String getRecipients()
	{
//		return (String) settings.get(RECIPIENTS);
		return (String) settings.get(getTaskId() + "." + RECIPIENTS);
	}

	public void setRecipients(String recipients)
	{
//		settings.put(RECIPIENTS, recipients);
		settings.put(getTaskId() + "." + RECIPIENTS, recipients);
	}
}
