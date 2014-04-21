package com.atlassian.support.tools.hercules;

import java.util.Map;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.servlet.SafeHttpServletRequest;

public class SupportToolsHerculesScanPercentageAction implements SupportToolsAction
{
	static final String ACTION_NAME = "hercules-percent-read";

	private WebMatchResultVisitor visitor;
	private final SupportApplicationInfo applicationInfo;

	public SupportToolsHerculesScanPercentageAction(SupportApplicationInfo info)
	{
		this.applicationInfo = info;
	}

	@Override
	public String getName()
	{
		return ACTION_NAME;
	}

	@Override
	public String getSuccessTemplatePath()
	{
		return "templates/html/hercules-percent-completed.vm";
	}

	@Override
	public String getErrorTemplatePath()
	{
		return "templates/html/hercules-percent-completed.vm";
	}

	@Override
	public String getStartTemplatePath()
	{
		return "templates/html/hercules-percent-completed.vm";
	}

	@Override
	public void prepare(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
	}

	@Override
	public SupportToolsAction newInstance()
	{
		return new SupportToolsHerculesScanPercentageAction(this.applicationInfo);
	}

	@Override
	public void validate(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
	}

	@Override
	public void execute(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		this.visitor = WebMatchResultVisitor.getAttachedInstance(req.getSession());
		int percentRead = 0;
		if(this.visitor != null)
		{
			percentRead = this.visitor.getPercentRead();
		}
		context.put("percentRead", percentRead);
	}

	@Override
	public String getCategory()
	{
		// AJAX functions don't have a category
		// FIXME: move this to REST instead of a plain old action
		return null;
	}

	@Override
	public String getTitle()
	{
		return null;
	}
}