/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.common;

import org.junit.Assert;

public class HasPrevious extends AbstractImporterWizardPage {

	@Override
	public String getUrl() {
		throw new UnsupportedOperationException();
	}

	public FrontPage previous() {
		Assert.assertTrue(previousButton.isEnabled());
		previousButton.click();
		return pageBinder.bind(FrontPage.class);
	}


}
