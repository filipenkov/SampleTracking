package it.common;

import com.atlassian.selenium.browsers.AutoInstallClient;
import com.thoughtworks.selenium.Wait;

public class TestScheduledHealthReport extends AbstractSupportTestCase
{
	public void testStartPage()
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();

		this.client.click("xpath=//a[@href='#scheduled-hercules']");
		AutoInstallClient.assertThat().elementPresent("id=scheduled-health-report");
		AutoInstallClient.assertThat().textPresent("Health Check");
	}

	public void testInvalidEmail() throws InterruptedException
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();

		this.client.click("xpath=//a[@href='#scheduled-health-report']");
		
		this.client.type("id=ccRecipients", "<script type='text/javascript'>window.alert('hey');</script>");
		this.client.click("css=#scheduled-health-report input[type='submit']", false);	
		
		Wait wait = new Wait()
		{
			@Override
			public boolean until()
			{
				return (TestScheduledHealthReport.this.client.isElementPresent("css=#scheduled-health-report .aui-message"));
			}
		};
		wait.wait("Timed out waiting for results",60000);
		
		// Make sure the data is escaped to avoid XSRF attacks.
		AutoInstallClient.assertThat().htmlNotPresent("window.alert('hey');</script>");

		// This should be detected as an invalid email address.
		AutoInstallClient.assertThat().htmlPresent("not a valid email address");
	}

}
