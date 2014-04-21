package com.atlassian.support.tools.scheduler;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.mutable.MutableLong;
import org.apache.log4j.Logger;

import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.support.tools.scheduler.settings.ScheduledTaskSettings;

public abstract class AbstractScheduledTask implements PluginJob {
	private static final Logger log = Logger.getLogger(AbstractScheduledTask.class);
	
	@Override
	public final void execute(Map<String, Object> jobDataMap) {
		MutableLong tracker = (MutableLong) jobDataMap.get(ScheduledTaskSettings.LAST_RUN_KEY);
		if(tracker.longValue() > (System.currentTimeMillis() - ScheduledTaskSettings.MINIMUM_WAIT_PERIOD))
		{
			log.warn("Refusing to run job '" + getName() + "' too often, last run at: " + new Date(tracker.longValue()));
			return;
		}
		
		tracker.setValue(System.currentTimeMillis());

		try {
			if(log.isInfoEnabled())
				log.info("Executing scheduled task '" + getName() + "'...");
			doExecute(jobDataMap);
		} catch (Exception e) {
			if(log.isInfoEnabled())
				log.info("Error running scheduled task '" + getName() + "'...", e);
		}
		finally {
			if(log.isInfoEnabled())
				log.info("Finished running scheduled task '" + getName() + "'...");
		}
	}

	protected abstract String getName();

	protected abstract void doExecute(Map<String, Object> jobDataMap);
}
