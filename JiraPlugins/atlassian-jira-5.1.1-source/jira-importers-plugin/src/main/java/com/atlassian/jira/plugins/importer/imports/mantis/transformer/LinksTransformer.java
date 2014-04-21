/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LinksTransformer implements ResultSetTransformer<ExternalLink> {
	private final MantisConfigBean configBean;

	public LinksTransformer(final MantisConfigBean configBean) {
		this.configBean = configBean;
	}

	public String getSqlQuery() {
		return "SELECT source_bug_id,destination_bug_id,relationship_type FROM mantis_bug_relationship_table";
	}

	public ExternalLink transform(final ResultSet rs) throws SQLException {
		return new ExternalLink(configBean.getLinkMapping(getLinkName(rs.getInt("relationship_type"))),
				rs.getString("source_bug_id"), rs.getString("destination_bug_id"));
	}

	private String getLinkName(int relType) {
		switch(relType) {
			case 2:
				return MantisConfigBean.PARENT_LINK_NAME;
			case 1:
				return MantisConfigBean.RELATED_TO_LINK_NAME;
			case 0:
				return  MantisConfigBean.DUPLICATE_LINK_NAME;
			default:
				return "Uknown link type";
		}
	}
}
