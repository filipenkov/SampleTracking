/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.util.env.EnvironmentUtils;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.pageobjects.TestedProductFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;

public abstract class ScreenshotFuncTestCase extends FuncTestCase {

	protected JiraTestedProduct jira;

	@Override
	protected void setUpTest() {
		jira = TestedProductFactory.create(JiraTestedProduct.class);
	}

	@Override
	protected void runTest() throws Throwable {
		try {
			super.runTest();
		} catch (Throwable e) {
			if (jira != null) {
				final File path = new File(EnvironmentUtils.getMavenAwareOutputDir(), "/screenshots");
				FileUtils.forceMkdir(path);

				final File destFile = new File(path, getClass().getCanonicalName() + "." + getName() + ".png");
				jira.getTester().getDriver().takeScreenshotTo(destFile);
				log.log("Dumped screenshot to " + destFile);
			}
			throw e;
		}
	}


}
