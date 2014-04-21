/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.util.lang.Pair;
import webwork.action.ActionContext;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

public abstract class AbstractImporterController implements ImporterController {
	private final JiraDataImporter importer;
	private final String sessionAttributeName;
	private final String id;

	public AbstractImporterController(JiraDataImporter importer, String sessionAttributeName, String id) {
		this.importer = importer;
		this.sessionAttributeName = sessionAttributeName;
		this.id = id;
	}

	@Override
	public JiraDataImporter getImporter() {
		return importer;
	}

	@Override
	public String getSupportedVersions() {
		return "";
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Pair<String, String> getLogo() {
		return Pair.of("com.atlassian.jira.plugins.jira-importers-plugin:graphics", getId().toLowerCase());
	}

	@Override
	public String getFirstStep() {
		return getSteps().get(0);
	}

	@Override
	public String getSection() {
		return "admin_system_menu/JIMMainSection/" + getId();
	}

	@Override
	@Nullable
	public String getDocumentationUrl() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void storeImportProcessBeanInSession(@Nullable ImportProcessBean bean) {
		ActionContext.getSession().put(sessionAttributeName, bean);
	}

	@Override
	public final ImportProcessBean getImportProcessBeanFromSession() {
		try {
			return (ImportProcessBean) ActionContext.getSession().get(sessionAttributeName);
		} catch (ClassCastException e) {
			return null;
		}
	}

	@Override
	public final ImportProcessBean getImportProcessBean(HttpSession session) {
		try {
			return (ImportProcessBean) session.getAttribute(sessionAttributeName);
		} catch (ClassCastException e) {
			return null;
		}
	}
}

