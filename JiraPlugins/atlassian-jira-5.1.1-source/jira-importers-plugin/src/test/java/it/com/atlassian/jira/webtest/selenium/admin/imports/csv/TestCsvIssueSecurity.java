/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.po.common.ImporterLogsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvProjectMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.pageobjects.page.LoginPage;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class TestCsvIssueSecurity extends ScreenshotFuncTestCase {

	private JiraRestClient restClient;

	@Before
	public void setUpTest() {
		super.setUpTest();
		administration.restoreData("issueSecurity.xml");

		restClient = ITUtils.createRestClient(environmentData);

	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-85
	 *
	 * Import issues to a project with a issue security scheme that has set a default for security level and check
	 * if issues had the security level set.
	 */
	@Test
	public void testImportIssuesAndCheckIfDefaultIssueSecurityWasSet() throws JSONException {
		CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		setupPage.setCsvFile(ITUtils.getCsvResource("JIM-77.csv"));
		setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-77.config"));

		CsvProjectMappingsPage projectMappings = setupPage.next().setReadFromCsv(false).setExistingProject("Secured!");
		ImporterLogsPage logsPage = projectMappings.next().next().next();
		assertTrue(logsPage.waitUntilFinished().isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("SEC-1", new NullProgressMonitor());
		Field field = issue.getField(IssueFieldConstants.SECURITY);
		assertNotNull(field);
		assertEquals("security", field.getId());
		assertNotNull(field.getValue());
		// Expecting ClassCastException when Security model is properly implemented in JRJC
		assertEquals("Secure", ((JSONObject) field.getValue()).get("name"));
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-85
	 *
	 * Import to a project that has a issue security scheme but doesn't have any level set as a default.
	 * Issues should be imported and have no security level set.
	 */
	@Test
	public void testImportIssuesAndCheckIfIssueSecurityWasNotSet() {
		CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		setupPage.setCsvFile(ITUtils.getCsvResource("JIM-77.csv"));
		setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-77.config"));

		CsvProjectMappingsPage projectMappings = setupPage.next().setReadFromCsv(false).setExistingProject("NOSEC");
		ImporterLogsPage logsPage = projectMappings.next().next().next();
		assertTrue(logsPage.waitUntilFinished().isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("NOSEC-1", new NullProgressMonitor());
		Field field = issue.getField(IssueFieldConstants.SECURITY);
		assertNull(field);
	}
}
