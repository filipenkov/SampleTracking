/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.external;

import com.atlassian.crowd.embedded.api.User;

import javax.annotation.Nullable;

public interface UserProvider {

	@Nullable
	User getUser(@Nullable String username);

}
