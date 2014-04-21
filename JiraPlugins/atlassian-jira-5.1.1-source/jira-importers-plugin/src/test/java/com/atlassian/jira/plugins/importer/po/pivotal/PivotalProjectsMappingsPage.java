/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.pivotal;

import com.atlassian.jira.plugins.importer.po.common.ImporterLogsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.web.ImporterProjectMappingsPage;
import com.atlassian.pageobjects.binder.ValidateState;
import org.junit.Assert;

import javax.annotation.Nullable;

public class PivotalProjectsMappingsPage extends ImporterProjectsMappingsPage<PivotalProjectsMappingsPage> {

	@ValidateState
	@Override
	public void validate() {
		Assert.assertEquals("Verifying page is in expected tab", "PivotalProjectMappingsPage", getActiveTabMeta());
		Assert.assertEquals(i18n.getString("jira-importer-plugin.wizard.projectmappings.title"), getPageTitle());
	}

	@Override
	public PivotalProjectsMappingsPage nextWithError() {
		assertNextEnabled();
		nextButton.click();
		return pageBinder.bind(PivotalProjectsMappingsPage.class);
	}

	public PivotalUserMappingsPage nextToUserMapping() {
		assertNextEnabled();
		nextButton.click();
		return pageBinder.bind(PivotalUserMappingsPage.class);
	}

	public ImporterLogsPage beginImport() {
		nextButton.click();
		return pageBinder.bind(ImporterLogsPage.class);
	}
}
