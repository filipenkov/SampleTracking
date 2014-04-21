/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.config;

public interface UserNameMapper {
	String getUsernameForLoginName(String loginName);

	UserNameMapper NO_MAPPING = new UserNameMapper() {
		@Override
		public String getUsernameForLoginName(String loginName) {
			return loginName;
		}
	};
}
