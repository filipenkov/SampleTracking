/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.pivotal;

import com.atlassian.jira.plugins.importer.po.common.ImporterValueMappingsPage;
import com.atlassian.pageobjects.binder.ValidateState;
import org.junit.Assert;

public class PivotalUserMappingsPage extends ImporterValueMappingsPage {
	@ValidateState
	public void validate() {
		Assert.assertEquals("Verifying page is in expected tab", "PivotalUserMappingsPage", getActiveTabMeta());
		Assert.assertEquals("Set up user mappings", getPageTitle());
	}

}
