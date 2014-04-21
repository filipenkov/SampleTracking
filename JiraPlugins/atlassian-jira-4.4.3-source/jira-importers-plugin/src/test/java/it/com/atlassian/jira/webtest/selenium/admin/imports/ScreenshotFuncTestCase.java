/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import org.apache.commons.io.FileUtils;

import java.io.File;

public abstract class ScreenshotFuncTestCase extends FuncTestCase {

	protected JiraTestedProduct product;

	@Override
	protected void runTest() throws Throwable {
		try {
			super.runTest();
		} catch (Throwable e) {
			if (product != null) {
				final File path = new File(EnvironmentUtils.getMavenAwareOutputDir(), "/screenshots");
				FileUtils.forceMkdir(path);

				final File destFile = new File(path, getClass().getCanonicalName() + "." + getName() + ".png");
				product.getTester().getDriver().takeScreenshotTo(destFile);
				log.log("Dumped screenshot to " + destFile);
			}
			throw e;
		}
	}


}
