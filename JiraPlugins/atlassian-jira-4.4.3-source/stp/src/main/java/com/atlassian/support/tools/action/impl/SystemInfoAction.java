package com.atlassian.support.tools.action.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class SystemInfoAction implements SupportToolsAction
{
	public static final String ACTION_NAME = "system-info";
	private final SupportApplicationInfo info;

	public SystemInfoAction(SupportApplicationInfo info)
	{
		this.info = info;
	}

	@Override
	public String getName()
	{
		return ACTION_NAME;
	}

	@Override
	public void prepare(Map<String, Object> context, HttpServletRequest request, ValidationLog validationLog)
	{
		context.put("info", this.info);
	}

	@Override
	public String getSuccessTemplatePath()
	{
		return "templates/system-info.vm";
	}

	@Override
	public String getErrorTemplatePath()
	{
		return "templates/system-info.vm";
	}

	@Override
	public String getStartTemplatePath()
	{
		return "templates/system-info.vm";
	}

	@Override
	public SupportToolsAction newInstance()
	{
		return new SystemInfoAction(this.info);
	}

	@Override
	public void validate(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		// we have no data to validate, so this is an empty method to satisfy
		// the requirements of the API
	}

	@Override
	public void execute(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		// we have no form data to process, so this is an empty method to
		// satisfy the requirements of the API
	}

	@Override
	public String getCategory()
	{
		return "stp.troubleshooting.category.title";
	}

	@Override
	public String getTitle()
	{
		return "stp.system.info.tool.title";
	}
}
