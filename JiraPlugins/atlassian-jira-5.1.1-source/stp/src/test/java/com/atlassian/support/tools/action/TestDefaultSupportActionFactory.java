package com.atlassian.support.tools.action;

import junit.framework.TestCase;

import com.atlassian.support.tools.action.impl.DefaultSupportActionFactory;
import com.atlassian.support.tools.action.impl.TabsAction;
import com.atlassian.support.tools.hercules.SupportToolsHerculesScanAction;
import com.atlassian.support.tools.mock.MockApplicationInfo;
import com.atlassian.support.tools.request.CreateSupportRequestAction;
import com.atlassian.support.tools.salext.SupportApplicationInfo;
import com.atlassian.support.tools.salext.mail.JiraMailUtility;
import com.atlassian.support.tools.salext.mail.MailUtility;
import com.atlassian.support.tools.scheduler.MockSupportScheduledTaskController;
import com.atlassian.support.tools.zip.SupportZipAction;

public class TestDefaultSupportActionFactory extends TestCase
{
	private SupportApplicationInfo info;
	private DefaultSupportActionFactory factory;
	private MailUtility mailUtility;
	private MockSupportScheduledTaskController controller;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.info = new MockApplicationInfo();
		this.mailUtility = new JiraMailUtility();
		this.controller = new MockSupportScheduledTaskController();
		this.factory = new DefaultSupportActionFactory(this.info, this.mailUtility, this.controller);
	}

	public void testGetAction()
	{
		SupportToolsAction homeAction = this.factory.getAction(new TabsAction(this.info).getName());
		assertTrue("Couldn't retrieve home action from factory.", homeAction instanceof TabsAction);

		SupportToolsAction supportToolsHerculesScanAction = this.factory.getAction(new SupportToolsHerculesScanAction(this.info)
				.getName());
		assertTrue("Couldn't retrieve hercules action from factory.",
				supportToolsHerculesScanAction instanceof SupportToolsHerculesScanAction);

		SupportToolsAction createSupportZipAction = this.factory.getAction(new SupportZipAction(this.info).getName());
		assertTrue("Couldn't retrieve create support zip action from factory.",
				createSupportZipAction instanceof SupportZipAction);

		SupportToolsAction raiseSupportRequestAction = this.factory.getAction(new CreateSupportRequestAction(this.info,
				this.mailUtility).getName());
		assertTrue("Couldn't retrieve create support request action from factory.",
				raiseSupportRequestAction instanceof CreateSupportRequestAction);

		SupportToolsAction nullAction = this.factory.getAction(null);
		assertTrue("Couldn't retrieve default action by sending null string to factory.",
				nullAction instanceof TabsAction);

		SupportToolsAction bogusAction = this.factory.getAction("bogus-action-should-never-be-found");
		assertTrue("Couldn't retrieve default action by sending bogus action name to factory.",
				bogusAction instanceof TabsAction);
	}
}
