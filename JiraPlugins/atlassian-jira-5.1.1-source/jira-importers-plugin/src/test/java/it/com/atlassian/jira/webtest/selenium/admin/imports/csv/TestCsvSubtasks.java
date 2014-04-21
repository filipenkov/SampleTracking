/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestCsvSubtasks extends BaseJiraWebTest {

	private JiraRestClient restClient;
	private final NullProgressMonitor pm = new NullProgressMonitor();

	@Before
	public void setUpTest() {
		backdoor.restoreData("issueSecurity.xml"); // subtasks enabled in the dump so sub-task(5) is not stolen by GH

		restClient = ITUtils.createRestClient(jira.environmentData());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-230 - importing subtasks from CSV
	 */
	@Test
	public void testImportWithSubtasks() {
		performTheImport("csv/subtasks.csv", "csv/subtasks.config");

		assertEquals(5, restClient.getSearchClient().searchJql("", pm).getTotal());
		assertIsSubtask("SUB-5", "SUB-2");
		assertIsSubtask("SUB-4", "SUB-2");
		assertIsSubtask("SUB-3", "SUB-1");
	}

	/*
	 * Test case for https://studio.atlassian.com/browse/JIM-411 - error reporting
	 */
	@Test
	public void testImportWithInvalidSubtasks() throws IOException {
		final ImporterFinishedPage logsPage = performTheImport("csv/JIM-411.csv", "csv/JIM-411.config");
		assertEquals("Expecting all issues to have been imported, including improperly linked ones", "4", logsPage
				.getIssuesImported());

		final List<String> errors = logsPage.getGlobalErrors2();
		assertEquals("Import completed with 2 errors:", errors.get(0));
		assertTrue(errors.get(1).matches("Issue 'SUB-1' is not of a sub-task type.*"));
		assertTrue(errors.get(2).matches("Unable to link issue from autoid-.* to 999 with link named 'sub-task-link'.*"));

		final Issue i1 = getIssueByJql("component = C1");
		final Issue i2 = getIssueByJql("component = C2");
		final Issue i3 = getIssueByJql("component = C3");
		final Issue i4 = getIssueByJql("component = C4");

		assertIsSubtask(i4.getKey(), i3.getKey());

		assertNull(i1.getField("parent"));
		assertNull(i2.getField("parent"));
		assertNull(i3.getField("parent"));
		assertNotNull(i4.getField("parent"));

		final String importLog = logsPage.getLog();
		assertTrue(importLog.matches("(?ms).*ERROR - Issue '" + i1.getKey() + "' is not of a sub-task type.*"));
		assertTrue(importLog.matches("(?ms).*ERROR - Unable to link issue from autoid-.* to 999 with link named 'sub-task-link'.*"));
	}

	private ImporterFinishedPage performTheImport(final String csvFile, final String configFile) {
		final CsvFieldMappingsPage fieldMappingsPage = jira.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.setCsvFile(ITUtils.getCurrentWorkingDirectory() + File.separator + "src/test/resources/" + csvFile)
				.setConfigurationFile(
						ITUtils.getCurrentWorkingDirectory() + File.separator + "src/test/resources/" + configFile)
				.next().next();
		return fieldMappingsPage.next().next().waitUntilFinished();
	}

	private void assertIsSubtask(String issueKey, String parentKey) {
		final SearchResult res = restClient.getSearchClient().searchJql("parent = " + parentKey + " and key = " + issueKey, pm);
		assertEquals(1, res.getTotal());
	}

	private Issue getIssueByJql(String jql) throws IOException {
		return Iterables.getOnlyElement(ITUtils.getIssuesByJql(restClient, jql));
	}

}
