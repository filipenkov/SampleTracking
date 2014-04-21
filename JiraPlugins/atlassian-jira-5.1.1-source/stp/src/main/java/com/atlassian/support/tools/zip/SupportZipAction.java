package com.atlassian.support.tools.zip;

import java.io.File;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.action.SupportToolsAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.servlet.SafeHttpServletRequest;

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
		return "templates/html/support-zip-execute.vm";
	}

	@Override
	public String getErrorTemplatePath()
	{
		return "templates/html/support-zip-start.vm";
	}

	@Override
	public String getStartTemplatePath()
	{
		return "templates/html/support-zip-start.vm";
	}

	@Override
	public SupportToolsAction newInstance()
	{
		return new SupportZipAction(this.applicationInfo);
	}

	@Override
	public void prepare(Map<String, Object> context, SafeHttpServletRequest request, ValidationLog validationLog)
	{
		this.applicationInfo.flagSelectedApplicationFileBundles(request);

		context.put("maxBytesPerFile", ZipUtility.MAX_BYTES_PER_FILE/(1024*1024) + "Mb");
	}

	@Override
	public void validate(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		FileOptionsValidator fileOptionsValidator = new FileOptionsValidator(this.applicationInfo);
		fileOptionsValidator.validate(context, req, validationLog);
	}

	@Override
	public void execute(Map<String, Object> context, SafeHttpServletRequest req, ValidationLog validationLog)
	{
		try
		{
			boolean limitFileSizes = req.getParameter("limit-file-sizes") != null ? true : false;
			
			File supportZipFile = ZipUtility.createSupportZip(this.applicationInfo.getSelectedApplicationInfoBundles(req),this.applicationInfo, validationLog, limitFileSizes);
			this.supportZipPath = supportZipFile.getCanonicalPath();
			this.supportZipFilename = supportZipFile.getName();
		}
		catch(Exception e)
		{
			// FIXME: this error should appear on the screen for the users to see!
			String msg = "Error creating support zip. Please zip up your " + this.applicationInfo.getApplicationLogDir()
					+ "/logs directory and attach this file to the issue: " + e.getMessage();
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
		return "stp.contact.title";
	}

	@Override
	public String getTitle()
	{
		return "stp.create.support.zip.title";
	}
}
