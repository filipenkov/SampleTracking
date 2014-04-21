/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.imports.importer.impl;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.io.IOUtils;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfigBean {
	private final JiraAuthenticationContext authenticationContext;

	protected AbstractConfigBean(JiraAuthenticationContext authenticationContext) {
		this.authenticationContext = authenticationContext;
	}

	protected boolean constantInGvList(List constantGvs, IssueConstant constant) {
		if (constantGvs != null && constant != null) {
			for (Iterator iterator = constantGvs.iterator(); iterator.hasNext();) {
				GenericValue constantGv = (GenericValue) iterator.next();
				if (constant.getName().equals(constantGv.getString("name"))) {
					return true;
				}
			}
		}

		return false;
	}

	public I18nHelper getI18n() {
		return authenticationContext.getI18nHelper();
	}

	public void copyFromProperties(File config) throws Exception {
		final FileInputStream inputStream = new FileInputStream(config);
		try {
			copyFromProperties(inputStream);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}

	abstract public void copyFromProperties(InputStream configFile) throws Exception;

	abstract public void copyToNewProperties(Map<String, Object> configFile);

	@Nullable
	abstract public String getProjectKey(String projectName);

    abstract public String getProjectName(String projectName);

	@Nullable
	abstract public String getProjectLead(String projectName);

    public void validateJustBeforeImport(ErrorCollection errors) {
        // you can override it
    }

}