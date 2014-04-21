/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.bugzilla;

import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;

public class BugzillaImporterSetupPage extends CommonImporterSetupPage {
	@Override
	public String getUrl() {
		return "/secure/admin/views/ImporterSetupPage!default.jspa?externalSystem=Bugzilla";
	}
}
