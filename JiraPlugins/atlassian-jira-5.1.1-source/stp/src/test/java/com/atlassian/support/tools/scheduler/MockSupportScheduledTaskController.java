package com.atlassian.support.tools.scheduler;

import com.atlassian.support.tools.scheduler.settings.ScheduledTaskSettings;

public class MockSupportScheduledTaskController implements SupportScheduledTaskController {
	@Override
	public void onStart() {
	}

	@Override
	public void onSettingsChanged(ScheduledTaskSettings settings) {
	}

	@Override
	public ScheduledTaskSettings getTaskSettings(String id)
	{
		return null;
	}
}
