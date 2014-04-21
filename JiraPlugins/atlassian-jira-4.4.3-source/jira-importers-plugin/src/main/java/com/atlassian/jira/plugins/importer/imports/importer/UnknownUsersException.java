/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.importer;

import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Set;

public class UnknownUsersException extends Exception {

	private final Set<ExternalUser> unknownUsers;

	public UnknownUsersException(String text, Set<ExternalUser> unknownUsers) {
		super(text + " " + getUnknownUsers(unknownUsers));
		this.unknownUsers = unknownUsers;
	}

	private static String getUnknownUsers(Set<ExternalUser> unknownUsers) {
		return StringUtils.join(Sets.<Object>newHashSet(
				Collections2.transform(unknownUsers, new Function<ExternalUser, String>() {
					@Override
					public String apply(@Nullable ExternalUser input) {
						return input != null ? input.getName() : "";
					}
				})), ", ");
	}

	public Set<ExternalUser> getUnknownUsers() {
		return unknownUsers;
	}
}
