package com.atlassian.support.tools.zip;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.mock.MockApplicationInfo;
import com.atlassian.support.tools.salext.DefaultApplicationFileBundle;

public class TestFileOptionsValidator extends TestCase
{
	MockApplicationInfo info = new MockApplicationInfo();
	Map<String, Object> context = new HashMap<String, Object>();
	HttpServletRequest emptyReq = mock(org.apache.struts.mock.MockHttpServletRequest.class);

	public void testEmptyRequestError() {
		
		// validate once with no options selected and the lack of options set to
		// be an error
		ValidationLog log = new ValidationLog(this.info);
		FileOptionsValidator validator = new FileOptionsValidator(this.info, true);
		validator.validate(this.context, this.emptyReq, log);
		assertTrue("Validation Log doesn't contain errors.", log.hasErrors());
		assertFalse("Validation Log contains warnings.", log.hasWarnings());
	}

	public void testEmptyRequestWarning() {
		// validate once with no options selected and the lack of options set to
		// be a warning
		ValidationLog log2 = new ValidationLog(this.info);
		FileOptionsValidator validator2 = new FileOptionsValidator(this.info, false);
		validator2.validate(this.context, this.emptyReq, log2);
		assertTrue("Validation Log doesn't contain warnings.", log2.hasWarnings());
		assertFalse("Validation Log contains errors.", log2.hasErrors());
	}
	
	
	public void testValidationNoErrorsOrWarnings()
	{
		String tempFilePath = "/tmp/foo.txt";
		File tempFile = new File(tempFilePath);
		if( ! tempFile.exists()) try
		{
			tempFile.createNewFile();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}


		// add a bundle that points to a file that exists, then validate. There
		// should be no warnings or errors.
		DefaultApplicationFileBundle validFileBundle = new DefaultApplicationFileBundle("test", "test", "test",
				tempFilePath);

		this.info.addApplicationFileBundle(validFileBundle);
		HttpServletRequest validFileReq = mock(HttpServletRequest.class);
		when(validFileReq.getParameter("test")).thenReturn("true");

		ValidationLog log3 = new ValidationLog(this.info);
		FileOptionsValidator validator3 = new FileOptionsValidator(this.info);
		validator3.validate(this.context, validFileReq, log3);
		assertFalse("Validation Log contains warnings.", log3.hasWarnings());
		assertFalse("Validation Log contains errors.", log3.hasErrors());
	}
}
