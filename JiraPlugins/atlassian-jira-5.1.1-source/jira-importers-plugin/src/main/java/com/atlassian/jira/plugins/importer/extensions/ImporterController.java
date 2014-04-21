/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.extensions;

import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import java.util.List;

public interface ImporterController {
	String getId();

	@Nullable
	String getSection();

    @Nullable
	ImportProcessBean getImportProcessBeanFromSession();

    @Nullable
	ImportProcessBean getImportProcessBean(HttpSession session);

	boolean createImportProcessBean(AbstractSetupPage setupPage);

	ImportDataBean createDataBean() throws Exception;

	JiraDataImporter getImporter();

    List<String> getSteps();

    boolean isUsingConfiguration();

}
