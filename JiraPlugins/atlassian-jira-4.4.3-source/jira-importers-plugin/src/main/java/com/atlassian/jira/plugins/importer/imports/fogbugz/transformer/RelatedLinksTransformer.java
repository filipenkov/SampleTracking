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

public class RelatedLinksTransformer implements ResultSetTransformer<ExternalLink> {
	private final FogBugzConfigBean configBean;

	public RelatedLinksTransformer(final FogBugzConfigBean configBean) {
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT ixBugFrom, ixBugTo FROM BugRelation ORDER BY ixBugFrom";
	}

	public ExternalLink transform(final ResultSet rs) throws SQLException {
		return new ExternalLink(configBean.getLinkMapping(FogBugzConfigBean.SEE_ALSO_LINK_NAME),
				rs.getString("ixBugFrom"), rs.getString("ixBugTo"));
	}
}
