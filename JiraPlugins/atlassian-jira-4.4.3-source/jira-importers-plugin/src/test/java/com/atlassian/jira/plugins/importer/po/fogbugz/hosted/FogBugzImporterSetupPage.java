/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.po.fogbugz.hosted;

import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;

public class FogBugzImporterSetupPage extends CommonImporterSetupPage {
	@Override
	public String getUrl() {
		return "/secure/admin/views/ImporterSetupPage!default.jspa?externalSystem=FogBugz";
	}
}
