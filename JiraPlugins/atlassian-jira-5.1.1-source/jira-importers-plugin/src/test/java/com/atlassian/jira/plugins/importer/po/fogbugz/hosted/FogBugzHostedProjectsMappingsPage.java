/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.po.common.ImporterFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import org.junit.Assert;

public class FogBugzHostedProjectsMappingsPage extends ImporterProjectsMappingsPage {

	public ImporterFieldMappingsPage nextFields() {
		Assert.assertTrue(nextButton.isEnabled());
		nextButton.click();
		return pageBinder.bind(ImporterFieldMappingsPage.class);
	}

}
