/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.pivotal.config;

import com.atlassian.jira.plugins.importer.imports.config.AbstractValueMappingDefinition;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.base.Supplier;

import java.util.Set;

public class LoginNameValueMapper extends AbstractValueMappingDefinition {
	public static final String FIELD = "Username";
	private final Supplier<Set<String>> usernameProvider;

	public LoginNameValueMapper(Supplier<Set<String>> usernameProvider, JiraAuthenticationContext authenticationContext) {
		super(null, authenticationContext);
		this.usernameProvider = usernameProvider;
	}

	@Override
	public String getExternalFieldId() {
		return FIELD;
	}

	@Override
	public String getDescription() {
		return getI18n().getText("jira-importer-plugin.config.mappings.value.login.name");
	}

	@Override
	public Set<String> getDistinctValues() {
		return usernameProvider.get();
	}

	@Override
	public boolean isMandatory() {
		return true;
	}

	@Override
	public boolean canBeBlank() {
		return false;
	}
}
