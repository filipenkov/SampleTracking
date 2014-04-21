package com.atlassian.support.tools.request;

import junit.framework.TestCase;

import com.atlassian.support.tools.mock.MockApplicationInfo;
import com.atlassian.support.tools.salext.mail.JiraMailUtility;
import com.atlassian.support.tools.salext.mail.MailUtility;

public class TestRaiseSupportRequestAction extends TestCase
{
	private CreateSupportRequestAction action;
	private MailUtility mailUtility = new JiraMailUtility();

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.action = new CreateSupportRequestAction(new MockApplicationInfo(), this.mailUtility);
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

	// These portions of the contract are tested by functional tests

	// public void prepare(Map<String, Object> context, HttpServletRequest
	// request)
	// public void validate(Map<String, Object> context, HttpServletRequest req,
	// ValidationLog validationLog)
	// public void execute(Map<String, Object> context, HttpServletRequest req)
}
