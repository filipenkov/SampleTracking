package com.atlassian.support.tools.salext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sisyphus.SisyphusPatternSource;
import com.atlassian.support.tools.request.CreateSupportRequestAction;
import com.atlassian.support.tools.zip.FileSanitizer;
import com.atlassian.templaterenderer.TemplateRenderer;

public class TestConfluenceApplicationInfo extends TestCase
{
	private ConfluenceApplicationInfo info;

	private final static I18nResolver mockResolver = mock(I18nResolver.class);
	private final static UserManager mockUserManager = mock(UserManager.class);
	private final static ApplicationProperties mockApplicationProperties = mock(ApplicationProperties.class);
	private final static TemplateRenderer mockTemplateRenderer = mock(TemplateRenderer.class);

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		when(mockApplicationProperties.getHomeDirectory()).thenReturn(new File("/tmp"));
		when(mockApplicationProperties.getDisplayName()).thenReturn("MockApplication");

		this.info = new ConfluenceApplicationInfo(mockApplicationProperties, mockResolver, mockUserManager, mockTemplateRenderer, null, null, null, null);
	}

// DISABLED because it requires network access and for CAC to be up.  
// FIXME:  write a better test for this that works offline.
//
//	public void testConfluenceRegexPath() throws Exception
//	{
//		SisyphusPatternSource source = this.info.getPatternSource();
//		assertNotNull(source);
//	}

	public void testApplicationLogFilePath()
	{
		File homeDir = mockApplicationProperties.getHomeDirectory();
		File logFile = new File(homeDir.getAbsolutePath() + "/logs/atlassian-confluence.log");

		try
		{
			logFile.mkdirs();
			logFile.createNewFile();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		assertFalse("Log file path is null when a file exists.", this.info.getApplicationLogFilePaths().isEmpty());

		logFile.delete();

		assertTrue("Log file path is not null when no log file is found.", this.info.getApplicationLogFilePaths().isEmpty());
	}

	public void testCreateSupportRequestEmail()
	{
		assertTrue("Create support request email is not valid.", CreateSupportRequestAction.isValidEmail(this.info.getCreateSupportRequestEmail()));
	}

	public void testApplicationName()
	{
		assertNotNull("getApplicationName() returns null", this.info.getApplicationName());
	}

	public void testApplicationHome()
	{
		assertNotNull("getApplicationHome() returns null", this.info.getApplicationHome());
	}
	
	public void testAtlassianUserSanitizing() throws IOException 
	{
		FileSanitizer sanitizer = new FileSanitizer(ConfluenceApplicationInfo.FILE_PATTERNS);

		File originalFile = new File("target/test-classes/atlassian-user.xml");
		File sanitizedFile = sanitizer.sanitize(originalFile);
		
		char[] buffer = new char[(int) sanitizedFile.length()];
		FileReader reader = new FileReader(sanitizedFile);
		reader.read(buffer);
		reader.close();
		
		String sanitizedOutput = String.valueOf(buffer);
		
		assertFalse("At least one piece of sensitive data wasn't sanitized, I still see the original 'secret' text at least once.",sanitizedOutput.contains("secret"));
		sanitizedFile.delete();

	}
	
	public void testConfluenceCfgSanitizing() throws IOException 
	{
		FileSanitizer sanitizer = new FileSanitizer(ConfluenceApplicationInfo.FILE_PATTERNS);
		
		File originalFile = new File("target/test-classes/confluence.cfg.xml");
		File sanitizedFile = sanitizer.sanitize(originalFile);
		
		char[] buffer = new char[(int) sanitizedFile.length()];
		FileReader reader = new FileReader(sanitizedFile);
		reader.read(buffer);
		reader.close();
		
		String sanitizedOutput = String.valueOf(buffer);
		
		assertFalse("At least one piece of sensitive data wasn't sanitized, I still see the original 'secret' text at least once.",sanitizedOutput.contains("secret"));
		sanitizedFile.delete();
		
	}
	
}