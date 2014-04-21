package itdisabled.confluence;

import it.common.AbstractSupportTestCase;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.atlassian.confluence.it.PropertiesManagerFactory;
import com.atlassian.confluence.it.User;
import com.atlassian.confluence.it.maven.MavenDependency;
import com.atlassian.confluence.it.maven.MavenDependencyHelper;
import com.atlassian.confluence.it.maven.MavenUploadablePlugin;
import com.atlassian.confluence.it.plugin.UploadablePlugin;
import com.atlassian.confluence.it.rpc.ConfluenceRpc;
import com.atlassian.selenium.browsers.AutoInstallClient;
import com.icegreen.greenmail.util.GreenMail;


public class DisabledTestSupportRequest extends AbstractSupportTestCase
{
	private static final String TEST_MAIL_SERVER_NAME = "Green Mail";


	public void testStartPage()
	{
		this.client.open(this.baseURL);
		this.client.waitForPageToLoad();

		this.client.click("xpath=//a[@href='#create-support-request']");
		AutoInstallClient.assertThat().elementPresent("id=create-support-request");
		AutoInstallClient.assertThat().textPresent("To:");	
		AutoInstallClient.assertThat().textPresent("Contact Email:");	
		AutoInstallClient.assertThat().textPresent("Summary:");	
		AutoInstallClient.assertThat().textPresent("Description:");	
		AutoInstallClient.assertThat().textPresent("Support Data to Attach");	
		
	}

	public void testEmailSending() throws InterruptedException, IOException {
		// set up a green mail instance
		GreenMail greenMail = new GreenMail(); 
	    greenMail.start();		
	    
	    ConfluenceRpc rpc = ConfluenceRpc.newInstance(BaseUrlThreadLocal.getBaseUrl(), ConfluenceRpc.Version.V2);


	    final MavenDependency dependency = MavenDependencyHelper.resolve("com.atlassian.confluence.plugins", "confluence-functestrpc-plugin", "4.0-beta3");
	    final UploadablePlugin functestPlugin = new MavenUploadablePlugin("confluence.extra.functestrpc", "Confluence Functional Test Remote API", dependency);
	    
	    rpc.logIn(User.ADMIN);
        if (!rpc.getPluginHelper().isPluginEnabled(functestPlugin))
        {
            rpc.getPluginHelper().installPlugin(functestPlugin);
        }
	    
	    rpc.addMailServer(TEST_MAIL_SERVER_NAME, "root@localhost.com", "[Mail Acceptance Test]", greenMail.getSmtp().getBindTo() , greenMail.getSmtp().getPort());

			
		this.client.click("xpath=//a[@href='#create-support-request']");
		AutoInstallClient.assertThat().elementPresent("id=create-support-request");
		
		this.client.type("name=contactEmail", "nobody@nowhere.org");
		this.client.type("name=subject", "This is a test messsage.");
		this.client.type("name=description", "This is a test description");
		
		this.client.clickButton("Send", true);
		
		this.client.waitForPageToLoad();
		AutoInstallClient.assertThat().textPresent("has been successfully sent");
		
		try {
			greenMail.waitForIncomingEmail(1);
			assertTrue("No messages received by local mail server.", greenMail.getReceivedMessages().length > 0);
		}
		finally {
			greenMail.stop();
			rpc.removeMailServer(TEST_MAIL_SERVER_NAME);
		}
	}
	
	public void testConfiguredMailServer() {
		this.client.click("xpath=//a[@href='#create-support-request']");
		AutoInstallClient.assertThat().elementPresent("id=create-support-request");
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textNotPresent("You do not have a mail server configured.");
	}
	
	public void testNoDescription() {
		this.client.click("xpath=//a[@href='#create-support-request']");
		AutoInstallClient.assertThat().elementPresent("id=create-support-request");

		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textNotPresent("You do not have a mail server configured.");
		
		this.client.type("name=contactEmail", "noone@all.you.monkeys.com");
		this.client.type("name=subject", "This is a test messsage.");

		this.client.clickButton("Send", true);

		this.client.waitForPageToLoad();
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textPresent("You must provide a description of the problem.");
	}
	
	public void testNoSummary() {
		this.client.click("xpath=//a[@href='#create-support-request']");
		AutoInstallClient.assertThat().elementPresent("id=create-support-request");
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textNotPresent("You do not have a mail server configured.");
		
		this.client.type("name=contactEmail", "noone@all.you.monkeys.com");
		this.client.type("name=description", "This is a test description");
		
		this.client.clickButton("Send", true);
		
		this.client.waitForPageToLoad();
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textPresent("You must provide a summary of the problem.");
	}
	
	public void testNoEmail() {
		this.client.click("xpath=//a[@href='#create-support-request']");
		AutoInstallClient.assertThat().elementPresent("id=create-support-request");
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textNotPresent("You do not have a mail server configured.");
		
		this.client.type("name=subject", "This is a test messsage.");
		this.client.type("name=description", "This is a test description");
		
		this.client.clickButton("Send", true);
		
		this.client.waitForPageToLoad();
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textPresent("You must provide the email address of the person sending this message.");
	}
	
	public void testBadEmail() {
		this.client.click("xpath=//a[@href='#create-support-request']");
		AutoInstallClient.assertThat().elementPresent("id=create-support-request");
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textNotPresent("You do not have a mail server configured.");
		
		this.client.type("name=contactEmail", "bogus");
		this.client.type("name=subject", "This is a test messsage.");
		this.client.type("name=description", "This is a test description");
		
		this.client.clickButton("Send", true);
		
		this.client.waitForPageToLoad();
		
		// Make sure we aren't seeing the warning about not having a mail server, we should have a mail server configured
		AutoInstallClient.assertThat().textPresent("The email address you provided (bogus) is not valid.");
	}
	
	private static class BaseUrlThreadLocal
	{
	    private static final AtomicInteger counter = new AtomicInteger();

	    private static final ThreadLocal<String> baseUrlThreadLocal = new ThreadLocal<String>()
	    {
	        @Override
	        protected String initialValue()
	        {
	            int i = counter.incrementAndGet();
	            return PropertiesManagerFactory.get().getPropertyValue("baseurl" + (i == 1 ? "" : String.valueOf(i)));
	        }
	    };

	    public static String getBaseUrl()
	    {
	        return baseUrlThreadLocal.get();
	    }
	}

}
