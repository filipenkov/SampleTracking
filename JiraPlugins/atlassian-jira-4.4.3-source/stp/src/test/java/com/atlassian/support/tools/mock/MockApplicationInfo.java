package com.atlassian.support.tools.mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.hercules.ScanItem;
import com.atlassian.support.tools.salext.AbstractSupportApplicationInfo;
import com.atlassian.support.tools.salext.ApplicationInfoBundle;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

@SuppressWarnings("rawtypes")
public class MockApplicationInfo extends AbstractSupportApplicationInfo
{
	private final static I18nResolver mockResolver = mock(I18nResolver.class);
	private final static UserManager mockUserManager = mock(UserManager.class);
	private final static ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);
	private final static TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);

	public MockApplicationInfo()
	{
		super(mockApplicationProperties,
				mockResolver, mockUserManager,
				mockTemplateRenderer);

		Answer parrotAnswer = new Answer()
		{
			@Override
			public Object answer(InvocationOnMock invocation)
			{
				Object[] args = invocation.getArguments();
				return args[0];
			}
		};

		when(mockResolver.getText(anyString())).thenAnswer(parrotAnswer);
		when(mockApplicationProperties.getHomeDirectory()).thenReturn(new File("/tmp"));
	}

	@Override
	public SisyphusPatternSource getPatternSource() throws IOException, ClassNotFoundException, MalformedURLException
	{
		return getPatternSourceByURL("http://confluence.atlassian.com/download/attachments/179443532/confluence_regex.xml");
	}

	@Override
	public String getUserEmail()
	{
		return "aatkins@atlassian";
	}

	@Override
	public String getCreateSupportRequestEmail()
	{
		return "create-support-request@localhost";
	}

	@Override
	public void loadProperties()
	{
	}

	public void clearApplicationFileBundles()
	{
		this.applicationInfoBundles.clear();
	}

	public void addApplicationFileBundle(ApplicationInfoBundle bundle)
	{
		this.applicationInfoBundles.add(bundle);
	}

	@Override
	public String getMailQueueURL(HttpServletRequest req)
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
		return new FileSanitizer(new HashMap<String, List<Pattern>>());
	}

	@Override
	public List<ScanItem> getApplicationLogFilePaths()
	{
		return Collections.emptyList();
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