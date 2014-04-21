package it;

import com.atlassian.selenium.browsers.AutoInstallClient;

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
		
		AutoInstallClient.assertThat().elementPresent("id=system-info-module");
	}
}
