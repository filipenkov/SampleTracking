/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.config;

import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractValueMappingDefinition implements ValueMappingDefinition {
	protected final JdbcConnection jdbcConnection;
	protected final JiraAuthenticationContext authenticationContext;

	protected AbstractValueMappingDefinition(final JdbcConnection jdbcConnection, final JiraAuthenticationContext authenticationContext) {
		this.jdbcConnection = jdbcConnection;
		this.authenticationContext = authenticationContext;
	}

	protected I18nHelper getI18n() {
		return authenticationContext.getI18nHelper();
	}

	/**
	 * By default no field is mapped
	 *
	 * @return NULL
	 */
	@Nullable
	public String getJiraFieldId() {
		return null;
	}

	@Nullable
	public Collection<ValueMappingEntry> getTargetValues() {
		return null;
	}

	public Collection<ValueMappingEntry> getDefaultValues() {
		return Collections.emptyList();
	}

	public boolean canBeBlank() {
		return true;
	}

	public boolean canBeImportedAsIs() {
		return true;
	}

	public boolean canBeCustom() {
		return true;
	}

	public boolean isMandatory() {
		return false;
	}
}
