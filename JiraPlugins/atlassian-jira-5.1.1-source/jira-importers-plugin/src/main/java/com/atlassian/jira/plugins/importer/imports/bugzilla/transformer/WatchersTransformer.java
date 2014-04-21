/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.bugzilla.transformer;

import com.atlassian.jira.plugins.importer.imports.bugzilla.BugzillaConfigBean;

public class WatchersTransformer extends VotesTransformer {

	public WatchersTransformer(String ixBug, BugzillaConfigBean configBean) {
		super(ixBug, configBean);
	}

	@Override
	public String getSqlQuery() {
		return "SELECT login_name FROM cc AS v, profiles AS p WHERE v.who=p.userid AND v.bug_id = " + ixBug;
	}
}
