package com.atlassian.support.tools.scheduler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.scheduler.settings.AbstractScheduledTaskSettings;
import com.atlassian.support.tools.scheduler.settings.HerculesScheduledTaskSettings;
import com.atlassian.support.tools.servlet.SafeHttpServletRequest;

public class ScheduledHerculesAction extends AbstractScheduledAction
{
	public static final String ACTION_NAME = "scheduled-hercules";
	private static final String RECIPIENTS = "recipients";
	private final HerculesScheduledTaskSettings settings;

	public ScheduledHerculesAction(SupportScheduledTaskController controller, SupportApplicationInfo info)
	{
		super(controller, info);
		this.settings = (HerculesScheduledTaskSettings) controller.getTaskSettings(HerculesScheduledTaskSettings.TASK_ID);
	}

	@Override
	public String getName()
	{
		return ACTION_NAME;
	}

	@Override
	public String getSuccessTemplatePath()
	{
		return "templates/html/scheduler-hercules.vm";
	}

	@Override
	public String getErrorTemplatePath()
	{
		return "templates/html/scheduler-hercules.vm";
	}

	@Override
	public String getStartTemplatePath()
	{
		return "templates/html/scheduler-hercules.vm";
	}

	@Override
	public SupportToolsAction newInstance()
	{
		return new ScheduledHerculesAction(this.controller, this.info);
	}

	@Override
	public void validate(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		super.validate(context, req, validationLog);

		String recipientsString = req.getParameter(RECIPIENTS);
		String[] recipients = StringUtils.split(recipientsString.trim(), ',');

		if (recipients == null || recipients.length == 0) {
			validationLog.addError("stp.scheduler.missing.recipients");
		}
		else {
			for(String recipient: recipients)
			{
				if(!EmailValidator.getInstance().isValid(recipient))
				{
					validationLog.addError("stp.scheduler.invalid.recipient", recipient);
				}
			}
		}
	}

	@Override
	protected boolean saveSettings(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		boolean hasChanged = super.saveSettings(context, req, validationLog);
		String newRecipients = req.getParameter(RECIPIENTS);

		if( ! StringUtils.equals(settings.getRecipients(), newRecipients.trim()))
		{
			settings.setRecipients(newRecipients.trim());
			return hasChanged = true;
		}
		return hasChanged;
	}

	@Override
	public String getTitle()
	{
		return "stp.scheduler.hercules.name";
	}

	@Override
	protected AbstractScheduledTaskSettings getSettings()
	{
		return settings;
	}
}
