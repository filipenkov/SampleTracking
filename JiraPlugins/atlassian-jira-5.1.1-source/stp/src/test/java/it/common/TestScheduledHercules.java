package it.common;

import com.atlassian.selenium.browsers.AutoInstallClient;
import com.thoughtworks.selenium.Wait;

public class TestScheduledHercules extends AbstractSupportTestCase
{
	public void testStartPage()
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();

		this.client.click("xpath=//a[@href='#scheduled-hercules']");
		AutoInstallClient.assertThat().elementPresent("id=scheduled-hercules");
		AutoInstallClient.assertThat().textPresent("Scheduled Log Scan");
	}

	public void testInvalidEmail() throws InterruptedException
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();

		this.client.click("xpath=//a[@href='#scheduled-hercules']");
		
		this.client.type("id=recipients", "<script type='text/javascript'>window.alert('hey');</script>");
		this.client.clickButton("Save Changes", false);	

		Wait wait = new Wait()
		{
			@Override
			public boolean until()
			{
				return (TestScheduledHercules.this.client.isElementPresent("css=#scheduled-hercules .aui-message"));
			}
		};
		wait.wait("Timed out waiting for results",60000);
		
		// Make sure the data is escaped to avoid XSRF attacks.
		AutoInstallClient.assertThat().htmlNotPresent("window.alert('hey');</script>");
		
		// This should be detected as an invalid email address.
		AutoInstallClient.assertThat().htmlPresent("not a valid email address");
	}

}
