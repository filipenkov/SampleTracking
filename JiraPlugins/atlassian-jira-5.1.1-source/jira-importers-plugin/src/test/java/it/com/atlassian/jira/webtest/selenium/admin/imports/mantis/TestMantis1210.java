/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.mantis;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.common.MantisImporterSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestMantis1210 extends BaseJiraWebTest {

    private JiraRestClient restClient;

    @Before
    public void setUpTest() {
        backdoor.restoreBlankInstance();
        backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);

        restClient = ITUtils.createRestClient(jira.environmentData());
    }

    /**
     * Smoke test for Mantis 1.2.10
     */
    @Test
    public void testImport() {
        final int expectedIssues = 1;

        MantisImporterSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(MantisImporterSetupPage.class);

        ITUtils.setupConnection(setupPage, ITUtils.MANTIS_1_2_10);

        ImporterFinishedPage logsPage = setupPage.next()
                .createProject("Another test project", "Another test project", "ANO")
                .next().next().next().next().next().waitUntilFinished();
        assertTrue(logsPage.isSuccess());
        assertEquals("1", logsPage.getProjectsImported());
        assertEquals(Integer.toString(expectedIssues), logsPage.getIssuesImported());

        SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
        assertEquals(expectedIssues, search.getTotal());

        Issue issue = restClient.getIssueClient().getIssue("ANO-1",
                new NullProgressMonitor());
        assertEquals("General", issue.getComponents().iterator().next().getName());
        assertEquals("test", issue.getSummary());
        assertEquals("pniewiadomski", issue.getReporter().getDisplayName());
        assertNull(issue.getAssignee());
        assertEquals("Open", issue.getStatus().getName());
        assertEquals("this is a test", issue.getDescription());
    }

}
