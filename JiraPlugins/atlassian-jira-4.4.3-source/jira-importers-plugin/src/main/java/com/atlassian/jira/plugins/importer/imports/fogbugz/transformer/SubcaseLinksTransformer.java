/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SubcaseLinksTransformer implements ResultSetTransformer<ExternalLink> {
	private final FogBugzConfigBean configBean;

	public SubcaseLinksTransformer(final FogBugzConfigBean configBean) {
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT ixBug, ixBugParent FROM Bug WHERE ixBugParent IS NOT NULL AND ixBugParent != 0";
	}

	public ExternalLink transform(final ResultSet rs) throws SQLException {
		return new ExternalLink(configBean.getLinkMapping(FogBugzConfigBean.SUBCASE_LINK_NAME),
				rs.getString("ixBugParent"), rs.getString("ixBug"));
	}
}
