package it.common;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;

import com.atlassian.selenium.SeleniumClient;
import com.atlassian.selenium.browsers.AutoInstallClient;

public class AbstractSupportTestCase extends TestCase {
	protected static final int TIMEOUT_MILLIS = 60000;
	
	protected SeleniumClient client;
	protected String baseURL;
	protected String startPageUrl;
	protected int screenShotIndex = 0;
	protected int htmlOutputIndex = 0;
	protected int textOutputIndex = 0;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.screenShotIndex = 0;
		this.htmlOutputIndex = 0;

		this.baseURL = System.getProperty("baseurl") + "/plugins/servlet/stp/view/";
		this.client = AutoInstallClient.seleniumClient();
		this.client.setTimeout(String.valueOf(TIMEOUT_MILLIS));
		this.client.open(this.baseURL);

		// Stash
		if (this.client.isElementPresent("name=j_password") && this.client.isElementPresent("name=j_username")) {
			this.client.type("name=j_username", "admin");
			this.client.type("name=j_password", "admin");
			this.client.clickButton("Log In", true);
		}
		// Fisheye
		else if (this.client.isElementPresent("name=password") && this.client.isElementPresent("name=username")) {
			this.client.type("name=username", "admin");
			this.client.type("name=password", "admin");
			this.client.clickButton("Log in", true);
		}
		// Everything else
		else if (this.client.isElementPresent("name=os_password") && this.client.isElementPresent("name=os_username")) {
			this.client.type("name=os_username", "admin");
			this.client.type("name=os_password", "admin");
			this.client.check("name=os_cookie");

			// Bamboo
			if (this.client.isElementPresent("name=save")) {
				this.client.clickButton("Log in", true);
			} else {
				this.client.clickButton("Log In", true);
			}
		}

		// WebSudo prompt
		if (this.client.isElementPresent("name=authenticateform") || this.client.isElementPresent("id=login-form")) {
			if (this.client.isElementPresent("id=login-form-authenticatePassword")) {
				this.client.type("id=login-form-authenticatePassword", "admin");
				this.client.clickButton("Confirm", true);
			}
			// another WebSudo variation
			else if (this.client.isElementPresent("id=password")) {
				this.client.type("id=password", "admin");
				this.client.clickButton("Confirm", true);
			}

		}
	}

	@Override
	protected void tearDown() throws Exception {
		this.client.deleteAllVisibleCookies();
		super.tearDown();
	}

	@Override
	protected void runTest() throws Throwable {
		try {
			super.runTest();
		} catch (Throwable t) {
			captureScreenShot();
			saveHtmlOutput();
			saveTextOutput();
			throw t;
		}
	}

	protected void saveHtmlOutput() {
		String htmlOutputFileName = getClass().getCanonicalName() + (getName() == null ? "" : "-" + getName())
				+ (this.htmlOutputIndex > 0 ? "-" + this.htmlOutputIndex : "") + ".html";

		saveHtmlOutput(htmlOutputFileName);
	}

	private void saveHtmlOutput(String htmlOutputFileName) {
		try {
			if (this.client != null) {
				String baseDir;
				if (System.getProperty("basedir") == null)
					baseDir = "";
				else
					baseDir = System.getProperty("basedir") + "/";

				this.htmlOutputIndex++;
				File sureFireDirectory = getDirectory(baseDir, "target");
				sureFireDirectory = getDirectory(sureFireDirectory.getAbsolutePath(), "/selenium-html");
				File htmlOutputFile = new File(sureFireDirectory.getAbsolutePath() + "/" + htmlOutputFileName);
				FileWriter out = new FileWriter(htmlOutputFile);
				out.append(this.client.getHtmlSource());
			}
		} catch (Exception e) {
			// swallow error
		}
	}

	protected void saveTextOutput() {
		String textOutputFileName = getClass().getCanonicalName() + (getName() == null ? "" : "-" + getName())
				+ (this.textOutputIndex > 0 ? "-" + this.textOutputIndex : "") + ".txt";

		saveTextOutput(textOutputFileName);
	}

	private void saveTextOutput(String textOutputFileName) {
		try {
			if (this.client != null) {
				String baseDir;
				if (System.getProperty("basedir") == null)
					baseDir = "";
				else
					baseDir = System.getProperty("basedir") + "/";

				this.htmlOutputIndex++;
				File sureFireDirectory = getDirectory(baseDir, "target");
				sureFireDirectory = getDirectory(sureFireDirectory.getAbsolutePath(), "/selenium-text");
				File htmlOutputFile = new File(sureFireDirectory.getAbsolutePath() + "/" + textOutputFileName);
				FileWriter out = new FileWriter(htmlOutputFile);
				out.append(this.client.getBodyText());
			}
		} catch (Exception e) {
			// swallow error
		}
	}

	protected void captureScreenShot(String screenShotFilePrefix) {
		try {
			if (this.client != null) {
				String baseDir;
				if (System.getProperty("basedir") == null)
					baseDir = "";
				else
					baseDir = System.getProperty("basedir") + "/";

				this.screenShotIndex++;
				File sureFireDirectory = getDirectory(baseDir, "target");
				sureFireDirectory = getDirectory(sureFireDirectory.getAbsolutePath(), "/selenium-screenshots");
				File entirePageScreenShot = new File(sureFireDirectory.getAbsolutePath() + "/" + screenShotFilePrefix
						+ "-entirepage.png");
				this.client.captureEntirePageScreenshot(entirePageScreenShot.getAbsolutePath(), "");

				File simpleScreenShot = new File(sureFireDirectory.getAbsolutePath() + "/" + screenShotFilePrefix
						+ ".png");
				this.client.captureScreenshot(simpleScreenShot.getAbsolutePath());
			}
		} catch (Exception e) {
			// swallow error
		}
	}

	protected void captureScreenShot() {
		String screenShotPrefix = getClass().getCanonicalName() + (getName() == null ? "" : "-" + getName())
				+ (this.screenShotIndex > 0 ? "-" + this.screenShotIndex : "");

		captureScreenShot(screenShotPrefix);
	}

	private File getDirectory(String baseDir, String directoryName) {
		File sureFireDirectory = new File(baseDir + directoryName);
		if (!sureFireDirectory.exists()) {
			sureFireDirectory.mkdir();
		}
		return sureFireDirectory;
	}
}
