package com.atlassian.support.tools.zip;

import junit.framework.TestCase;

import com.atlassian.support.tools.mock.MockApplicationInfo;

public class TestCreateSupportZipAction extends TestCase
{
	private SupportZipAction action;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.action = new SupportZipAction(new MockApplicationInfo());
	}

	public void testName()
	{
		assertNotNull("Action name is null.", this.action.getName());
	}

	public void testSuccessTemplatePath()
	{
		assertNotNull("Success template path is null.", this.action.getSuccessTemplatePath());
	}

	public void testErrorTemplatePath()
	{
		assertNotNull("Error template path is null.", this.action.getErrorTemplatePath());
	}

	public void testStartTemplatePath()
	{
		assertNotNull("Start template path is null.", this.action.getStartTemplatePath());
	}

	public void testNewInstance()
	{
		assertTrue("New instance returned is not the same class as the spawning action.",
				this.action.getClass().equals(this.action.newInstance().getClass()));
	}

	public void testSupportZipPath()
	{
		String testSupportZipPath = "/dev/null";
		this.action.setSupportZipPath(testSupportZipPath);
		assertEquals("Support zip path getter returns something other than what the setter was given.",
				testSupportZipPath, this.action.getSupportZipPath());
	}

	public void testSupportZipFilename()
	{
		String testSupportZipFilename = "foo.txt";
		this.action.setSupportZipFilename(testSupportZipFilename);
		assertEquals("Support zip filename getter returns something other than what the setter was given.",
				testSupportZipFilename, this.action.getSupportZipFilename());
	}

	// These portions of the contract are tested by functional tests

	// public void prepare(Map<String, Object> context, HttpServletRequest
	// request)
	// public void validate(Map<String, Object> context, HttpServletRequest req,
	// ValidationLog validationLog)
	// public void execute(Map<String, Object> context, HttpServletRequest req)
}
