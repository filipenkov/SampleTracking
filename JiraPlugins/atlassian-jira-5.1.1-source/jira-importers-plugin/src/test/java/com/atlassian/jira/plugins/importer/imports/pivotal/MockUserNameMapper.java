/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.pivotal;

import com.atlassian.jira.plugins.importer.imports.config.UserNameMapper;

import java.util.Collections;
import java.util.Map;

public class MockUserNameMapper implements UserNameMapper {
	private final Map<String, String> map;

	public MockUserNameMapper(Map<String, String> map) {
		this.map = map;
	}

	public MockUserNameMapper(String key, String value) {
		map = Collections.singletonMap(key, value);
	}

	@Override
	public String getUsernameForLoginName(String loginName) {
		return map.containsKey(loginName) ? map.get(loginName) : loginName;
	}
}
