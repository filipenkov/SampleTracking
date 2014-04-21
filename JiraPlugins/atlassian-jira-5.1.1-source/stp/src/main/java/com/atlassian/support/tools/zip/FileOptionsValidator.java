package com.atlassian.support.tools.zip;

import java.util.Map;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.Validateable;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.bundle.ApplicationInfoBundle;
import com.atlassian.support.tools.servlet.SafeHttpServletRequest;

public class FileOptionsValidator implements Validateable
{
	private SupportApplicationInfo applicationInfo;
	private boolean emptyZipIsError = true;

	public FileOptionsValidator(SupportApplicationInfo applicationInfo)
	{
		this.applicationInfo = applicationInfo;
	}

	public FileOptionsValidator(SupportApplicationInfo applicationInfo, boolean emptyZipIsError)
	{
		this.applicationInfo = applicationInfo;
		this.emptyZipIsError = emptyZipIsError;
	}

	@Override
	public void validate(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{

		int selectedBundles = 0;
		for(ApplicationInfoBundle applicationFileBundle: this.applicationInfo.getApplicationFileBundles())
		{
			if(Boolean.parseBoolean(req.getParameter(applicationFileBundle.getKey())))
			{
				applicationFileBundle.validate(validationLog);
				selectedBundles++;
			}
		}

		if(selectedBundles == 0)
		{
			if(this.emptyZipIsError)
			{
				validationLog.addError("stp.create.support.zip.no.options.error");
			}
			else
			{
				validationLog.addWarning("stp.create.support.zip.no.options.warning");
			}
		}

	}
}