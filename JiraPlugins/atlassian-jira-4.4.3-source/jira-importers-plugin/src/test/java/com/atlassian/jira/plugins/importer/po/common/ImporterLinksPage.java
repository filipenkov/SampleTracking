/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.common;

import org.junit.Assert;

public class ImporterLinksPage extends AbstractImporterWizardPage {

	@Override
	public String getUrl() {
		return null;
	}

	public ImporterLogsPage next() {
		Assert.assertTrue("Next button is disabled", nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterLogsPage.class);
	}
}
