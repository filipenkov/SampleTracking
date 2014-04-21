/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.json;

import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.json.web.JsonSetupPage;
import com.atlassian.jira.plugins.importer.sample.SampleData;
import com.atlassian.jira.plugins.importer.sample.SampleDataImporter;
import com.atlassian.jira.plugins.importer.web.AbstractImporterController;
import com.atlassian.jira.plugins.importer.web.AbstractSetupPage;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import webwork.action.ActionContext;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class JsonImporterController extends AbstractImporterController {
    private final SampleDataImporter sampleDataImporter;

    public JsonImporterController(JiraDataImporter importer, SampleDataImporter sampleDataImporter) {
        super(importer, "issue.importer.jira.json.import.bean", "JSON");
        this.sampleDataImporter = sampleDataImporter;
    }

    @Override
    public ImportProcessBean getImportProcessBeanFromSession() {
        return new ImportProcessBean();
    }

    @Override
    public ImportProcessBean getImportProcessBean(HttpSession session) {
        return null;
    }

    @Override
    public boolean createImportProcessBean(AbstractSetupPage setupPage) {
        JsonSetupPage jsonSetupPage = (JsonSetupPage) setupPage;

        try {
            ActionContext.getSession().put(getSessionAttributeName(), FileUtils.readFileToString(jsonSetupPage.getMultipart().getFile(JsonSetupPage.FILE_INPUT_NAME)));
            return true;
        } catch (IOException e) {
            setupPage.addErrorMessage(e.getMessage());
            return false;
        }
    }

    @Override
    public ImportDataBean createDataBean() throws Exception {
        final String json = (String) ActionContext.getSession().get(getSessionAttributeName());
        final SampleData sampleData = sampleDataImporter.parseSampleData(json);

        return sampleDataImporter.createDataBean(sampleData, null, null);
    }

    @Override
    public List<String> getSteps() {
        return Lists.newArrayList(
                JsonSetupPage.class.getSimpleName()
        );
    }

    @Override
    public boolean isUsingConfiguration() {
        return false;
    }
}
