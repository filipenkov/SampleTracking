/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.web;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.util.lang.Pair;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import java.util.List;

public interface ImporterController {
	String getTitle();
	String getId();
	String getSupportedVersions();
    String getFirstStep();
	String getDescription();

	@Nullable
	String getSection();

	Pair<String, String> getLogo();

	@Nullable
	String getDocumentationUrl();

    @Nullable
	ImportProcessBean getImportProcessBeanFromSession();

    @Nullable
	ImportProcessBean getImportProcessBean(HttpSession session);

    void storeImportProcessBeanInSession(@Nullable ImportProcessBean bean);

	@Nullable
	ImportProcessBean createImportProcessBean(AbstractSetupPage setupPage);

	ImportDataBean createDataBean(DateTimeFormatterFactory dateTimeFormatterFactory) throws Exception;

	JiraDataImporter getImporter();

    List<String> getSteps();

}
