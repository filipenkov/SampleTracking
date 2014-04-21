/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.fogbugz.transformer;

import com.atlassian.jira.plugins.importer.imports.fogbugz.FogBugzConfigBean;

public class AttachmentTransformerVer8OrNewer extends AttachmentTransformerVer7OrOlder {
	public AttachmentTransformerVer8OrNewer(final String ixBug, final FogBugzConfigBean configBean) {
		super(ixBug, configBean);
	}

	@Override
	public String getSqlQuery() {
		return "SELECT a.sFileName, p.sFullName, b.dt, a.sData FROM BugEvent b, Attachment a, AttachmentReference ar, Person p"
				+ " WHERE b.ixPerson = p.ixPerson AND b.ixBugEvent = ar.ixBugEvent AND ar.ixAttachment = a.ixAttachment"
				+ " AND b.ixBug = " + ixBug + " AND ar.fLatest=1 ORDER BY b.dt";
	}

}
