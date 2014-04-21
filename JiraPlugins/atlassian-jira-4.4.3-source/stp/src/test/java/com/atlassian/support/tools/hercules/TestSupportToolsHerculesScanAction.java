package com.atlassian.support.tools.hercules;

import junit.framework.TestCase;

import com.atlassian.support.tools.action.impl.HomeAction;
import com.atlassian.support.tools.mock.MockApplicationInfo;

public class TestSupportToolsHerculesScanAction extends TestCase
{
	private HomeAction action;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.action = new HomeAction(new MockApplicationInfo());
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
	// Also, in the case of the home action, these are empty methods that don't
	// really do anything
	// public void prepare(Map<String, Object> context, HttpServletRequest
	// request)
	// public void validate(Map<String, Object> context, HttpServletRequest req,
	// ValidationLog validationLog)
	// public void execute(Map<String, Object> context, HttpServletRequest req)
}
