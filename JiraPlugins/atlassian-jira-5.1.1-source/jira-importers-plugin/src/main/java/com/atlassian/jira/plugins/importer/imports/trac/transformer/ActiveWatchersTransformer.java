/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.trac.TracConfigBean;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class ActiveWatchersTransformer implements ResultSetTransformer<Collection<ExternalUser>> {
	private final TracConfigBean configBean;
	private final ImportLogger log;

	public ActiveWatchersTransformer(TracConfigBean configBean, ImportLogger log) {
		this.configBean = configBean;
		this.log = log;
	}

	public String getSqlQuery() {
		return "SELECT cc FROM ticket WHERE cc IS NOT NULL AND cc!=''";
	}

	@Nullable
	public Collection<ExternalUser> transform(ResultSet rs) throws SQLException {
		final String cc = rs.getString("cc");
		if (StringUtils.isEmpty(cc)) {
			return null;
		}
		final List<ExternalUser> users = Lists.newArrayList();
		for(String email : cc.split(",")) {
			users.add(RequiredUserTransformer.transform(configBean, StringUtils.trimToEmpty(email), log));
		}
		return users;
	}
}
