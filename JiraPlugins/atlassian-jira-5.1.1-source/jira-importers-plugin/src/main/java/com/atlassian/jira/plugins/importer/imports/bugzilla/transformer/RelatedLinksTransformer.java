/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RelatedLinksTransformer implements ResultSetTransformer<ExternalLink> {
	private final BugzillaConfigBean configBean;

	public RelatedLinksTransformer(final BugzillaConfigBean configBean) {
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT blocked, dependson FROM dependencies";
	}

	public ExternalLink transform(final ResultSet rs) throws SQLException {
		return new ExternalLink(configBean.getLinkMapping(BugzillaConfigBean.DEPENDS_LINK_NAME),
				rs.getString("dependson"), rs.getString("blocked"));
	}
}
