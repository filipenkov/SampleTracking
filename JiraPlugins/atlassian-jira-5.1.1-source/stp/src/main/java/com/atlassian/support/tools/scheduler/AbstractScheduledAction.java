package com.atlassian.support.tools.scheduler;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.scheduler.settings.AbstractScheduledTaskSettings;
import com.atlassian.support.tools.scheduler.settings.ScheduledTaskSettings;
import com.atlassian.support.tools.servlet.SafeHttpServletRequest;

public abstract class AbstractScheduledAction implements SupportToolsAction
{
	protected final SupportScheduledTaskController controller;
	protected final SupportApplicationInfo info;

	protected static final Map<String, String> frequencyOptions;
	static
	{
		Map<String, String> tMap = new HashMap<String, String>();
		tMap.put(ScheduledTaskSettings.DAILY, "stp.scheduler.frequency.daily");
		tMap.put(ScheduledTaskSettings.WEEKLY, "stp.scheduler.frequency.weekly");
		frequencyOptions = Collections.unmodifiableMap(tMap);
	}

	public AbstractScheduledAction(SupportScheduledTaskController controller, SupportApplicationInfo info)
	{
		this.controller = controller;
		this.info = info;
	}

	@Override
	public void prepare(Map<String, Object> context, SafeHttpServletRequest request, ValidationLog validationLog)
	{
		context.put("controller", controller);
		context.put("info", info);
		context.put("settings", getSettings());
		context.put("frequencyOptions", frequencyOptions);
	}

	@Override
	public void validate(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		int startTimeHour = parseStartHour(req);
		int startTimeMinute = parseStartMinute(req);
		long frequency = parseFrequency(req);
		
		if( startTimeHour == -1 || startTimeMinute == -1)
		{
			validationLog.addError("stp.scheduler.invalid.start.time");
		}

		if(frequency < ScheduledTaskSettings.MINIMUM_WAIT_PERIOD)
		{
			validationLog.addError("stp.scheduler.invalid.frequency");
		}
	}

	private long parseFrequency(HttpServletRequest req) {
		String frequencyString = req.getParameter("frequency");
		if (ScheduledTaskSettings.DAILY.equals(frequencyString)) {
			return ScheduledTaskSettings.DAILY_MS;
		}
		else if (ScheduledTaskSettings.WEEKLY.equals(frequencyString)) {
			return ScheduledTaskSettings.WEEKLY_MS;
		}
		
		return parseNumber(frequencyString, -1);
	}

	private long parseNumber(String string, long defaultValue)
	{
		if(StringUtils.isEmpty(string) || !StringUtils.isNumeric(string))
			return defaultValue;
		try
		{
			return Long.parseLong(string);
		}
		catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}

	private boolean parseEnabled(HttpServletRequest req)
	{
		return "on".equals(req.getParameter("enabled"));
	}

	@Override
	public void execute(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		if( ! validationLog.hasErrors())
		{
			boolean hasChanged = saveSettings(context, req, validationLog);
			if(hasChanged)
			{
				controller.onSettingsChanged(getSettings());
				if(getSettings().isEnabled())
				{
					String frequencyString = info.getText("stp.scheduler.task.frequency.default.message", getSettings().getFrequencyMs());
					if (getSettings().getFrequencyMs() == ScheduledTaskSettings.DAILY_MS) {
						frequencyString = frequencyOptions.get(ScheduledTaskSettings.DAILY);
					}
					else if (getSettings().getFrequencyMs() == ScheduledTaskSettings.WEEKLY_MS){
						frequencyString = frequencyOptions.get(ScheduledTaskSettings.WEEKLY);
					}
					validationLog.addFeedback("stp.scheduler.task.enabled", info.getText(frequencyString), getSettings().getStartTime());
				}
				else
				{
					validationLog.addFeedback("stp.scheduler.task.disabled");
				}
			}
			else
			{
				validationLog.addFeedback("stp.scheduler.task.unchanged");
			}
			
		}
	}

	protected boolean saveSettings(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		boolean isEnabled = parseEnabled(req);
		int startTimeHour = parseStartHour(req);
		int startTimeMinute = parseStartMinute(req);
		long frequency = parseFrequency(req);

		boolean hasChanged = false;
		if(isEnabled != getSettings().isEnabled())
		{
			hasChanged = true;
			getSettings().setEnabled(isEnabled);
		}
		
		Date time = getSettings().getStartTime();
		if(time.getHours() != startTimeHour || time.getMinutes() != startTimeMinute)
		{
			hasChanged = true;
			getSettings().setStartTime(startTimeHour, startTimeMinute);
		}

		if (getSettings().getFrequencyMs() != frequency) {
			hasChanged = true;
			getSettings().setFrequency(frequency);
		}
		
		return hasChanged;
	}

	private int parseStartMinute(HttpServletRequest req) {
		return (int) parseNumber(req.getParameter("start-time-minute"), -1);
	}

	private int parseStartHour(HttpServletRequest req) {
		return (int) parseNumber(req.getParameter("start-time-hour"), -1);
	}

	@Override
	public String getCategory()
	{
		return "stp.scheduler.title";
	}

	protected abstract AbstractScheduledTaskSettings getSettings();
}
