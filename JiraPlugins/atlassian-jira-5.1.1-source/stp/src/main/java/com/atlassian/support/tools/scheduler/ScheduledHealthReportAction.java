package com.atlassian.support.tools.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.EmailValidator;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.bundle.ApplicationInfoBundle;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.scheduler.settings.AbstractScheduledTaskSettings;
import com.atlassian.support.tools.scheduler.settings.HealthReportScheduledTaskSettings;
import com.atlassian.support.tools.servlet.SafeHttpServletRequest;

public class ScheduledHealthReportAction extends AbstractScheduledAction {
	public static final String ACTION_NAME = "scheduled-health-report";
	private static final String CC_RECIPIENTS = "ccRecipients";
	public final HealthReportScheduledTaskSettings settings;
	
	public ScheduledHealthReportAction(SupportScheduledTaskController controller, SupportApplicationInfo info) {
		super(controller, info);
		this.settings = (HealthReportScheduledTaskSettings) controller.getTaskSettings(HealthReportScheduledTaskSettings.TASK_ID);
	}

	@Override
	public String getName() {
		return ACTION_NAME;
	}
	
	@Override
	public void prepare(Map<String, Object> context, SafeHttpServletRequest request, ValidationLog validationLog) {
		super.prepare(context, request, validationLog);

		List<ApplicationInfoBundle> filteredApplicationInfoBundles = new ArrayList<ApplicationInfoBundle>();
		for (ApplicationInfoBundle bundle : info.getApplicationFileBundles()) {
			if (bundle.getKey().equals(BundleManifest.APPLICATION_LOGS.getKey()) || bundle.getKey().equals(BundleManifest.APPLICATION_PROPERTIES.getKey())) {
				filteredApplicationInfoBundles.add(bundle);
			}
		}
		
		context.put("bundles", filteredApplicationInfoBundles);
	}
	
	@Override
	public String getSuccessTemplatePath() {
		return "templates/html/scheduler-health-report.vm";
	}

	@Override
	public String getErrorTemplatePath() {
		return "templates/html/scheduler-health-report.vm";
	}

	@Override
	public String getStartTemplatePath() {
		return "templates/html/scheduler-health-report.vm";
	}

	@Override
	public SupportToolsAction newInstance() {
		return new ScheduledHealthReportAction(this.controller, this.info);
	}
	
	@Override
	public void validate(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog) {
		super.validate(context, req, validationLog);
		
		String recipientsString = req.getParameter(CC_RECIPIENTS);
		String[] recipients = StringUtils.split(recipientsString, ',');
		
		if (recipients == null || recipients.length == 0) {
			validationLog.addFeedback("stp.scheduler.health.recipients.missing");
		}
		else {
			for(String recipient : recipients) 
			{
				if (!EmailValidator.getInstance().isValid(recipient)) 
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
		String newRecipients = req.getParameter(CC_RECIPIENTS);

		if(!StringUtils.equals(settings.getCcRecipients(), newRecipients.trim())) 
		{
			settings.setCcRecipients(newRecipients.trim());
			return hasChanged = true;
		}
		return hasChanged;
	}

	@Override
	public String getTitle() {
		return "stp.scheduler.health.name";
	}

	@Override
	protected AbstractScheduledTaskSettings getSettings() {
		return settings;
	}
}
