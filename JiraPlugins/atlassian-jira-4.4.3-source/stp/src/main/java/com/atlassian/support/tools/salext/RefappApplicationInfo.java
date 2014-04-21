package com.atlassian.support.tools.salext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class RefappApplicationInfo extends AbstractSupportApplicationInfo
{
	public RefappApplicationInfo(ApplicationProperties applicationProperties, I18nResolver i18nResolver, UserManager userManager, TemplateRenderer renderer)
	{
		super(applicationProperties, i18nResolver, userManager, renderer);
	}

	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException
	{
		return getPatternSourceByURL("http://confluence.atlassian.com/download/attachments/179443532/confluence_regex.xml");
	}

	@Override
	public List<ScanItem> getApplicationLogFilePaths()
	{
		return Collections.singletonList(ScanItem.createDefaultItem(System.getProperty("plugin.test.directory") + "/empty.log"));
	}

	@Override
	public String getUserEmail()
	{
		return "aatkins@atlassian.com";
	}

	@Override
	public String getCreateSupportRequestEmail()
	{
		return "confluence-autosupportrequests@atlassian.com";
	}

	@Override
	public void loadProperties()
	{
		Map<String,String> testProperties = new HashMap<String,String>();
		testProperties.put("foo", "bar");
		addApplicationProperties("Test Properties",testProperties);
	}

	@Override
	public String getMailQueueURL(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public String getMailServerConfigurationURL(HttpServletRequest request)
	{
		return null;
	}

	@Override
	public FileSanitizer getFileSanitizer()
	{
		return new FileSanitizer(null);
	}

	@Override
	public String getApplicationSEN()
	{
		return null;
	}

	@Override
	public String getApplicationServerID()
	{
		return null;
	}
}
