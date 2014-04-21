/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.trac.transformer;

import com.atlassian.jira.plugins.importer.imports.importer.ResultSetTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class WatchersTransformer implements ResultSetTransformer<Collection<String>> {
	private final String ixBug;

	public WatchersTransformer(String ixBug) {
		this.ixBug = ixBug;
	}

	public String getSqlQuery() {
		return "SELECT cc FROM ticket WHERE id=" + ixBug;
	}

	@Nullable
	public Collection<String> transform(ResultSet rs) throws SQLException {
		final String cc = rs.getString("cc");
		if (StringUtils.isEmpty(cc)) {
			return null;
		}
		final List<String> emails = Lists.newArrayList();
		for(String email : cc.split(",")) {
			email = StringUtils.trimToEmpty(email);
			if (!email.isEmpty()) {
				emails.add(email);
			}
		}
		return emails;
	}
}
