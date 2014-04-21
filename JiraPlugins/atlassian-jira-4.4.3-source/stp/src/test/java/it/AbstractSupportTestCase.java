package it;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

import com.atlassian.selenium.SeleniumClient;
import com.atlassian.selenium.browsers.AutoInstallClient;

public class AbstractSupportTestCase extends TestCase
{
	protected SeleniumClient client;
	protected String baseURL;
	protected String startPageUrl;
	protected int screenShotIndex = 0;
	protected int htmlOutputIndex = 0;

	@Override
	protected void setUp() throws Exception
	{
		this.screenShotIndex = 0;
		this.htmlOutputIndex = 0;
		super.setUp();

		this.baseURL = System.getProperty("baseurl")  + "/plugins/servlet/stp/view/";
		this.client = AutoInstallClient.seleniumClient();
		this.client.open(this.baseURL);

		// JIRA and Confluence login forms
		if(this.client.isElementPresent("name=os_password") && this.client.isElementPresent("name=os_username"))
		{
			this.client.type("name=os_username", "admin");
			this.client.type("name=os_password", "admin");
			this.client.check("name=os_cookie");
			this.client.clickButton("Log In", true);
		}
		
		// WebSudo prompt
		if (this.client.isElementPresent("id=login-form-authenticatePassword") || this.client.isElementPresent("name=authenticateform")) {
			this.client.type("id=login-form-authenticatePassword", "admin");
			this.client.clickButton("Confirm", true);
		}
	}

	
	
	@Override
	protected void tearDown() throws Exception
	{
		this.client.deleteAllVisibleCookies();
		super.tearDown();
	}



	@Override
	protected void runTest() throws Throwable
    {
        try
        {
            super.runTest();
        }
        catch (Throwable t)
        {
            captureScreenShot();
            saveHtmlOutput();
            throw t;
        }
    }

	
   protected void saveHtmlOutput()
    {
        String htmlOutputFileName = getClass().getCanonicalName() +
            (getName() == null ? "" : "-" + getName()) +
            (this.htmlOutputIndex > 0 ? "-" + this.htmlOutputIndex : "") + ".html";

        saveHtmlOutput(htmlOutputFileName);
    }
	
    private void saveHtmlOutput(String screenShotFileName)
	{
        try
        {
            if (this.client != null)
            {
                String baseDir;
                if (System.getProperty("basedir") == null)
                    baseDir = "";
                else
                    baseDir = System.getProperty("basedir") + "/";

                this.htmlOutputIndex++;
                File sureFireDirectory = getDirectory(baseDir, "target");
                sureFireDirectory = getDirectory(sureFireDirectory.getAbsolutePath(), "/selenium-html");
                File htmlOutputFile = new File(sureFireDirectory.getAbsolutePath() + "/" + screenShotFileName);
                FileWriter out = new FileWriter(htmlOutputFile);
                out.append(this.client.getHtmlSource());
            }
        }
        catch (Exception e)
        {
            // swallow error
        }
	}

	protected void captureScreenShot(String screenShotFileName)
    {
        try
        {
            if (this.client != null)
            {
                String baseDir;
                if (System.getProperty("basedir") == null)
                    baseDir = "";
                else
                    baseDir = System.getProperty("basedir") + "/";

                this.screenShotIndex++;
                File sureFireDirectory = getDirectory(baseDir, "target");
                sureFireDirectory = getDirectory(sureFireDirectory.getAbsolutePath(), "/selenium-screenshots");
                File screenShot = new File(sureFireDirectory.getAbsolutePath() + "/" + screenShotFileName);
                this.client.captureEntirePageScreenshot(screenShot.getAbsolutePath(), "");
//                client.captureScreenshot(screenShot.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            // swallow error
        }
    }
    
    protected void captureScreenShot()
    {
        String screenShotFileName = getClass().getCanonicalName() +
            (getName() == null ? "" : "-" + getName()) +
            (this.screenShotIndex > 0 ? "-" + this.screenShotIndex : "") + ".png";

        captureScreenShot(screenShotFileName);
    }

    private File getDirectory(String baseDir, String directoryName)
    {
        File sureFireDirectory = new File(baseDir + directoryName);
        if (!sureFireDirectory.exists())
        {
            sureFireDirectory.mkdir();
        }
        return sureFireDirectory;
    }
}
