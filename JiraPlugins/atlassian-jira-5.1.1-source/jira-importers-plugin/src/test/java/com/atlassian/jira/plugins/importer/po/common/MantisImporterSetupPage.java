/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.po.common;

public class MantisImporterSetupPage extends CommonImporterSetupPage {

	@Override
	public String getUrl() {
		return "/secure/admin/views/ImporterSetupPage!default.jspa?externalSystem=com.atlassian.jira.plugins.jira-importers-plugin:mantisImporter";
	}

}
