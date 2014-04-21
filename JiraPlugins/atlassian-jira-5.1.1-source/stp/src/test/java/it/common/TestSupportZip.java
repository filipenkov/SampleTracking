package it.common;

import com.atlassian.selenium.browsers.AutoInstallClient;

public class TestSupportZip extends AbstractSupportTestCase
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	public void testZipStartPage()
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();
		
		AutoInstallClient.assertThat().elementPresent("id=support-zip");
	}
	
	public void testCreateZip()
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();

		this.client.click("xpath=//a[@href='#support-zip']");
		this.client.submit("name=support-zip", true);

		AutoInstallClient.assertThat().textPresent("Your support zip file has been successfully created");
		AutoInstallClient.assertThat().textNotPresent("You didn't select anything to include in your zip file.");
		AutoInstallClient.assertThat().textNotPresent("was truncated");
	}
	
	public void testCreateEmptyZip()
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();

		this.client.click("xpath=//a[@href='#support-zip']");
		
		// get all checkboxes and untick them
		this.client.getEval("window.AJS.$('form#support-zip input[type=checkbox].checkbox').attr('checked', false)");
		
		this.client.submit("name=support-zip", true);
		
		AutoInstallClient.assertThat().textPresent("You didn't select anything to include in your zip file.");
		
		// Also make sure the original form is displayed again.
		AutoInstallClient.assertThat().elementPresent("id=support-zip");
	}	
}
