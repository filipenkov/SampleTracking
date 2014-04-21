package com.atlassian.support.tools.zip;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

public class SupportZipAction implements SupportToolsAction
{
	private static final Logger log = Logger.getLogger(SupportZipAction.class);
	private static final String ACTION_NAME = "support-zip";

	private String supportZipPath = "";
	private String supportZipFilename = "";

	private final SupportApplicationInfo applicationInfo;

	public SupportZipAction(SupportApplicationInfo applicationInfo)
	{
		super();
		this.applicationInfo = applicationInfo;
	}

	@Override
	public String getName()
	{
		return ACTION_NAME;
	}

	@Override
	public String getSuccessTemplatePath()
	{
		return "templates/support-zip-execute.vm";
	}

	@Override
	public String getErrorTemplatePath()
	{
		return "templates/support-zip-start.vm";
	}

	@Override
	public String getStartTemplatePath()
	{
		return "templates/support-zip-start.vm";
	}

	@Override
	public SupportToolsAction newInstance()
	{
		return new SupportZipAction(this.applicationInfo);
	}

	@Override
	public void prepare(Map<String, Object> context, HttpServletRequest request, ValidationLog validationLog)
	{
		this.applicationInfo.flagSelectedApplicationFileBundles(request);
	}

	@Override
	public void validate(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		FileOptionsValidator fileOptionsValidator = new FileOptionsValidator(this.applicationInfo);
		fileOptionsValidator.validate(context, req, validationLog);
	}

	@Override
	public void execute(Map<String, Object> context, HttpServletRequest req, ValidationLog validationLog)
	{
		try
		{
			File supportZipFile = ZipUtility.createSupportZip(this.applicationInfo.getSelectedApplicationInfoBundles(req),
					this.applicationInfo);
			this.supportZipPath = supportZipFile.getCanonicalPath();
			this.supportZipFilename = supportZipFile.getName();
		}
		catch(Exception e)
		{
			// FIXME: this error should appear on the screen for the users to see!
			String applicationHomeDirectory = this.applicationInfo.getApplicationHome();
			String msg = "Error creating support zip. Please zip up your " + applicationHomeDirectory
					+ "/logs directory and attach this, with your " + applicationHomeDirectory
					+ "/confluence.cfg.xml file to the issue: " + e.getMessage();
			log.error(msg, e);
		}
	}

	public String getSupportZipPath()
	{
		return this.supportZipPath;
	}

	public void setSupportZipPath(String supportZipPath)
	{
		this.supportZipPath = supportZipPath;
	}

	public String getSupportZipFilename()
	{
		return this.supportZipFilename;
	}

	public void setSupportZipFilename(String supportZipFilename)
	{
		this.supportZipFilename = supportZipFilename;
	}
	
	@Override
	public String getCategory()
	{
		return "stp.contact.category.title";
	}

	@Override
	public String getTitle()
	{
		return "stp.create.support.zip.title";
	}
}
