package com.atlassian.support.tools.scheduler.settings;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.support.tools.scheduler.ScheduledHealthReportTask;

public class HealthReportScheduledTaskSettings extends AbstractScheduledTaskSettings
{
	public static final String TASK_ID = "HealthReportScheduledTask";
	private static final String CC_RECIPIENTS = "ccRecipients";

	public HealthReportScheduledTaskSettings(PluginSettingsFactory pluginSettingsFactory)
	{
		super(pluginSettingsFactory);
	}

	@Override
	public Class<? extends PluginJob> getTaskClass()
	{
		return ScheduledHealthReportTask.class;
	}

	@Override
	public String getTaskId()
	{
		return TASK_ID;
	}

	@Override
	public String getViewTemplateFile()
	{
		return "/templates/scheduler-health-report-view-config.vm";
	}

	@Override
	public String getEditTemplateFile()
	{
		return "/templates/scheduler-health-report-edit-config.vm";
	}

	public String getCcRecipients()
	{
//		return (String) settings.get(CC_RECIPIENTS);
		return (String) settings.get(getTaskId() + "." + CC_RECIPIENTS);
	}

	public void setCcRecipients(String newRecipients)
	{
//		settings.put(CC_RECIPIENTS, newRecipients);
		settings.put(getTaskId() + "." + CC_RECIPIENTS, newRecipients);
	}
}
