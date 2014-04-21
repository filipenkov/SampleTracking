/*
 * Copyright (C) 2012 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import javax.annotation.Nullable;

public interface ExternalUserNameMapper {
	@Nullable String extractUserName(@Nullable String externalName);

	ExternalUserNameMapper NOOP = new ExternalUserNameMapper() {
		@Override
		public String extractUserName(String externalName) {
			return externalName;
		}
	};
}
