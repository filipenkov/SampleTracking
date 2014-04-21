package com.atlassian.support.tools.scheduler;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.support.tools.scheduler.settings.ScheduledTaskSettings;

public interface SupportScheduledTaskController extends LifecycleAware 
{
	public void onSettingsChanged(ScheduledTaskSettings settings);
	public ScheduledTaskSettings getTaskSettings(String id);
}
