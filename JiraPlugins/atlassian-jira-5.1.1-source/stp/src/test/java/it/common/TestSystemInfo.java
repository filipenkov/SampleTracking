package it.common;

import com.atlassian.selenium.browsers.AutoInstallClient;
import com.thoughtworks.selenium.Wait;

public class TestSystemInfo extends AbstractSupportTestCase
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	public void testStartPage()
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();
		
		AutoInstallClient.assertThat().elementPresent("id=system-info");
		this.client.click("xpath=//a[@href='#system-info']");
		
		Wait wait = new Wait()
		{
			@Override
			public boolean until()
			{
				return client.isTextPresent("Java Runtime Environment");
			}
		};
		wait.wait("Timed out waiting for system information.",60000);
	}
	
	public void testPluginData() 
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();
		
		AutoInstallClient.assertThat().textNotPresent("No Plugins Found");
	}
}
