/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import webwork.action.ActionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;

public abstract class AbstractImporterController implements ImporterController {
	private final JiraDataImporter importer;
	private final String sessionAttributeName;
	private final String id;
    private final boolean studio;

    public AbstractImporterController(JiraDataImporter importer, String sessionAttributeName, String id) {
		this.importer = importer;
		this.sessionAttributeName = sessionAttributeName;
		this.id = id;
        this.studio = ComponentAccessor.getComponent(FeatureManager.class).isEnabled(com.atlassian.jira.config.CoreFeatures.ON_DEMAND);
	}

	@Override
	public JiraDataImporter getImporter() {
		return importer;
	}

    @Override
    public boolean isUsingConfiguration() {
        return true;
    }

    @Override
	public String getId() {
		return id;
	}

	@Override
	public String getSection() {
        return "admin_system_menu/JIMMainSection/" + (studio ? "Studio" : getId());
	}

	@SuppressWarnings("unchecked")
	public void storeImportProcessBeanInSession(@Nullable ImportProcessBean bean) {
		ActionContext.getSession().put(sessionAttributeName, bean);
	}

	@Override
	public ImportProcessBean getImportProcessBeanFromSession() {
		try {
			return (ImportProcessBean) ActionContext.getSession().get(sessionAttributeName);
		} catch (ClassCastException e) {
			return null;
		}
	}

	@Override
	public ImportProcessBean getImportProcessBean(HttpSession session) {
		try {
			return (ImportProcessBean) session.getAttribute(sessionAttributeName);
		} catch (ClassCastException e) {
			return null;
		}
	}

    @Nonnull
    public String getSessionAttributeName() {
        return sessionAttributeName;
    }
}

