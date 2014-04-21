package com.atlassian.support.tools.action.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.servlet.SafeHttpServletRequest;

public class HomeAction implements SupportToolsAction
{
	public static final String ACTION_NAME = "home";
	private final SupportApplicationInfo info;

	public HomeAction(SupportApplicationInfo info)
	{
		this.info = info;
	}

	@Override
	public String getName()
	{
		return ACTION_NAME;
	}

	@Override
	public void prepare(Map<String, Object> context, SafeHttpServletRequest request, ValidationLog validationLog)
	{
	}

	@Override
	public String getSuccessTemplatePath()
	{
		return "templates/html/home.vm";
	}

	@Override
	public String getErrorTemplatePath()
	{
		return "templates/html/home.vm";
	}

	@Override
	public String getStartTemplatePath()
	{
		return "templates/html/home.vm";
	}

	@Override
	public SupportToolsAction newInstance()
	{
		return new HomeAction(this.info);
	}

	@Override
	public void validate(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		// we have no data to validate, so this is an empty method to satisfy
		// the requirements of the API
	}

	@Override
	public void execute(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		// we have no form data to process, so this is an empty method to
		// satisfy the requirements of the API
	}

	@Override
	public String getCategory()
	{
		return null;
	}

	@Override
	public String getTitle()
	{
		return "stp.home.title";
	}
}
