/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvProjectMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvValueMappingsPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static junitx.framework.ListAssert.assertContains;

public class TestCsvImport extends ScreenshotFuncTestCase {
	private static final String PRJ_KEY = "CSV";

	private JiraRestClient restClient;

	@Before
	public void setUpTest() {
		administration.restoreBlankInstance();

		ITUtils.doWebSudoCrap(navigation, tester);

		product = TestedProductFactory.create(JiraTestedProduct.class);

		restClient = ITUtils.createRestClient(environmentData);
	}

	@Test
	public void testGoingToSecondPage() throws Exception {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/testcsvmapper.csv");

		setupPage.next();
	}

	/*
	 * Tests uploads CSV file and configuration then checks if fields were correctly initialized
	 * to values read from configuration file.
	 */
	@Test
	public void testIfConfigurationIsReadProperly() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-80.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-80.config");

		CsvProjectMappingsPage projectMappingsPage = setupPage.next();

		assertEquals("@atlassian.com", projectMappingsPage.getUserEmailSuffix());

		assertEquals("TICIRP", projectMappingsPage.getProjectKey());

		assertFalse(projectMappingsPage.isReadingFromCsv());

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = projectMappingsPage.next();

		List<WebElement> mappings = fieldMappingsPage.getMappingFields();
		assertEquals(4, mappings.size());

		assertEquals(IssueFieldConstants.STATUS, mappings.get(0).getValue());
		assertEquals("", mappings.get(1).getValue());
		assertEquals(IssueFieldConstants.SUMMARY, mappings.get(2).getValue());
		assertEquals(IssueFieldConstants.ISSUE_TYPE, mappings.get(3).getValue());

		CsvValueMappingsPage valueMappingsPage = fieldMappingsPage.next();

		// 4th page
		assertEquals("4", valueMappingsPage.getValue("value.0"));
		assertEquals("1", valueMappingsPage.getValue("value.1"));
		assertEquals(ImmutableList.of("Map as is", "Import as blank", "Bug",
				"Improvement", "New Feature", "Task"),
				Immutables.transformThenCopyToList(valueMappingsPage.getSelectOptions("value.1"),
						new Function<WebElement, String>() {
							@Override
							public String apply(@Nullable WebElement input) {
								return input != null ? input.getText() : null;
							}
						}));

		assertTrue(valueMappingsPage.next().waitUntilFinished().isSuccess());
	}

	/**
	 * test for JIM-297 and overall smoke-test for CSV importer
	 */
	@Test
	public void testImportingVersionsAndComponents() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-297.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-297.cfg");

		assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());

		navigation.browseProject(PRJ_KEY);
		// are versions created?
		navigation.browseVersionTabPanel(PRJ_KEY, "Version ABC");
		navigation.browseVersionTabPanel(PRJ_KEY, "Version BBB");

		// are components created?
		navigation.browseComponentTabPanel(PRJ_KEY, "CompA");
		navigation.browseComponentTabPanel(PRJ_KEY, "CompB");

		// are all issues created
	    navigation.issueNavigator().gotoNavigator();
		navigation.issueNavigator().createSearch("project = " + PRJ_KEY + " order by key");
		assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("CSV-1", "CSV-2", "CSV-3");

		assertIssue("2.My second issue", "Version BBB", "CompB", "Closed");
		assertIssue("1.My first issue", "Version ABC", "CompA", "Open");
		assertIssue("3.My third issue", "Version ABC", "CompB", "Open");

	}

	private void assertIssue(String summary, String fixVersion, String component, String status) {
		navigation.issueNavigator().createSearch("project = " + PRJ_KEY + " order by key");
		navigation.clickLinkWithExactText(summary);
		assertions.getViewIssueAssertions().assertAffectsVersionsNone();
		assertions.getViewIssueAssertions().assertFixVersions(fixVersion);
		assertions.getViewIssueAssertions().assertComponents(component);
		assertions.getViewIssueAssertions().assertStatus(status);
	}

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-225
     */
	@Test
    public void testInvalidProjectData() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-225.csv");

		List<String> errors = setupPage.next().setReadFromCsv(true).next()
				.setFieldMapping("ProjectName", "project.name")
				.setFieldMapping("ProjectKey", "project.key")
				.setFieldMapping("ProjectDescription", "project.description")
				.setFieldMapping("ProjectUrl", "project.url")
				.setFieldMapping("summary", "summary").nextWithError().getGlobalErrors2();

		assertEquals(ImmutableList.of(
				"Invalid project key TP23: You must specify a unique project key, at least 2 characters long, containing only uppercase letters.",
				"Invalid project key newx: You must specify a unique project key, at least 2 characters long, containing only uppercase letters.",
				"Invalid project url hzxsttp://la: The URL specified is not valid - it must start with http://",
				"Invalid project key NEWX12: You must specify a unique project key, at least 2 characters long, containing only uppercase letters."), errors);
    }

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-77
	 */
	@Test
	public void testImportDateFieldsAndEmptyHeaders() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-77.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-77.config");

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next().next();

		assertEquals(ImmutableList.of("CSV file has at least one empty header, corresponding column will not be imported."),
				fieldMappingsPage.getWarnings());

		assertTrue(fieldMappingsPage.hasDuplicateColumnInfo(""));

		assertEquals("CSV file has multiple columns having the same name. "
				+ "There's only a limited set of JIRA fields that can have multiple values assigned to. "
				+ "Mappings for those columns will be limited to those fields only.",
				fieldMappingsPage.getDuplicateColumnMessage(""));

		assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("CSVI-1", new NullProgressMonitor());
		assertNotNull(issue);
		assertEquals(new DateTime(1999, 1, 21, 12, 0, 0, 0).toInstant(), issue.getCreationDate().toInstant());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-77
	 */
	@Test
	public void testInvalidDateFormat() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-77.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-77.config");

		CsvProjectMappingsPage projectMappingsPage = setupPage.next().setDateImportFormat("dd/MM/yyyy HH:mm:ssf").nextWithError();

		assertContains(projectMappingsPage.getFieldErrors(), "Invalid date format: Illegal pattern character 'f'");
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-236
	 */
	@Test
	public void testMappingToExistingCustomFieldIsPreserved() {
		administration.customFields().addCustomField(CustomFieldConstants.SELECT_FIELD_TYPE, "Z");
		administration.customFields().addCustomField(CustomFieldConstants.SELECT_FIELD_TYPE, "A");
		administration.customFields().addCustomField(CustomFieldConstants.SELECT_FIELD_TYPE, "B");

		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-67.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-236.config");

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next().next();

		assertEquals("Z", fieldMappingsPage.getSelectedOptionText(fieldMappingsPage.getTargetFieldName("drag & drop")));
	}

	/**
	 * Import fails when delimiter is set to ,
	 *
	 * https://studio.atlassian.com/browse/JIM-373
	 */
	@Test
	public void testDelimiterSetToCommaShouldNotBreakImport() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-67.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-67.config");
		setupPage.setDelimiter(",");
		assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());
	}

	/**
	 * Test case for
	 */
	@Test
	public void testCustomFieldMappingFromConfiguration() {
		administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 1");
		administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 2");
		administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 3");
		administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 4");

		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/testcsvmapper.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-375.config");
		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next().next();

		assertEquals("Text field 4",
				fieldMappingsPage.getSelectedOptionText(fieldMappingsPage.getTargetFieldName("project.key")));
		assertEquals("Text field 3",
				fieldMappingsPage.getSelectedOptionText(fieldMappingsPage.getTargetFieldName("project")));
		assertEquals("Text field 1",
				fieldMappingsPage.getSelectedOptionText(fieldMappingsPage.getTargetFieldName("reporter")));
		assertEquals("Text field 2",
				fieldMappingsPage.getSelectedOptionText(fieldMappingsPage.getTargetFieldName("assignee")));
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-420
	 */
	public void testImportMultilineFields() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-420.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-420.config");

		final ImporterFinishedPage logsPage = setupPage.next().next().next().next().waitUntilFinished();
		assertTrue(logsPage.isSuccess());
		assertEquals("3", logsPage.getIssuesImported());
		Issue issue = restClient.getIssueClient().getIssue("MIF-1", new NullProgressMonitor());
		assertEquals("Если в xml для построения моделей приходит Devision, Launcher падает. \n"
				+ "\n"
				+ "А должен - отрабатывать все остальные и не падать",
				issue.getField(IssueFieldConstants.DESCRIPTION).getValue());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-466
	 */
	public void testCreatedDate() throws IOException {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-466.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-466.config");
		ImporterFinishedPage finishPage = setupPage.next().next().next().next().waitUntilFinished();
		assertTrue(finishPage.isSuccess());
		assertEquals(ImmutableList.of("Import completed with 2 warnings:",
				"Commenter named not found. Creating issue with currently logged in user instead",
				"Commenter named kirkpsc not found. Creating issue with currently logged in user instead"), finishPage.getWarnings());

		Issue issue = restClient.getIssueClient().getIssue("DC-1", new NullProgressMonitor());
		assertEquals(new DateTime(2001, 05, 30, 12, 00, 0, 0), issue.getCreationDate());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-91
	 */
	public void testDateCustomFieldsParsing() throws IOException {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-91.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-91.config");
		assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("CDAT-1", new NullProgressMonitor());
		Map<String, Field> fields = ITUtils.getFieldsMap(issue.getFields());
		assertTrue(fields.containsKey("Custom date time"));
		assertTrue(fields.containsKey("Custom date picker"));

		assertEquals(new DateTime(2003, 03, 02, 12, 23, 00, 000, DateTimeZone.UTC).toInstant(),
				ISODateTimeFormat.dateTimeParser().parseDateTime(
						(String) fields.get("Custom date time").getValue()).toInstant());

		assertEquals(new DateTime(2003, 05, 3, 22, 00, 0, 0, DateTimeZone.UTC).toInstant(),
				ISODateTimeFormat.dateTimeParser().parseDateTime(
						(String) fields.get("Custom date picker").getValue()).toInstant());
	}

	// There is no explicit requirement for this functionality, so we may consider disabling the test if it collides with
	// some requirement
	public void testImportOnlyChangedIssues() throws Exception {
		administration.subtasks().enable(); // optherwise Issue Id field is not visible
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM_242/JIM-242-orig.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM_242/JIM-242-configuration.txt");
		final ImporterFinishedPage logsPage = setupPage.next().next().next().next().waitUntilFinished();
		assertTrue(logsPage.isSuccess());
		assertEquals("2", logsPage.getIssuesImported());

		CsvSetupPage setupPage2 = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();
		setupPage2.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM_242/JIM-242-new.csv");
		setupPage2.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM_242/JIM-242-configuration-new.txt");
		final ImporterFinishedPage logsPage2 = setupPage.next().next().next().next().waitUntilFinished();
		assertTrue(logsPage2.isSuccess());
		assertEquals(ImmutableList.of("2 of 3 issues have been skipped because they already exist in destination projects."), logsPage2.getWarnings());
		assertEquals("1", logsPage2.getIssuesImported());

		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(3, result.getTotal());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-515
	 */
	public void testReuseSelectOptions() throws IOException {
		String customFieldId = administration.customFields().addCustomField(
				CustomFieldConstants.SELECT_FIELD_TYPE, "Select");
		administration.customFields().addOptions(customFieldId.replace("customfield_", ""),
				"Merchandise", "Service");

		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-515.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-515.config");
		assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("SCF-1", new NullProgressMonitor());
		Field field = issue.getField(customFieldId);
		assertEquals("{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10000\",\"value\":\"Merchandise\"}", field.getValue());
	}

	/**
	 * Empty mappings cause import to ingore issues.
	 * Test case for https://studio.atlassian.com/browse/JIM-534
	 */
	public void testEmptyMappings() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/comments.csv");
		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
				.setReadFromCsv(false)
				.setExistingProject("MKY").next();
		fieldMappingsPage.setFieldMapping("Summary", "summary");
		fieldMappingsPage.setMapValues("Summary", true);

		assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

		SearchResult search = restClient.getSearchClient().searchJql("project=MKY", new NullProgressMonitor());
		assertEquals(4, search.getTotal());

		Issue issue = restClient.getIssueClient().getIssue("MKY-1", new NullProgressMonitor());
		assertEquals("Z", issue.getSummary());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-559
	 */
	public void testCsvIncludesConflictingProjects() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-559.csv");

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
				.setReadFromCsv(true)
				.next();

		fieldMappingsPage.setFieldMapping("pkey", "project.key");
		fieldMappingsPage.setFieldMapping("plead", "project.lead");
		fieldMappingsPage.setFieldMapping("project", "project.name");
		fieldMappingsPage.setFieldMapping("summary", "summary");

		fieldMappingsPage.next();

		List<String> errors = fieldMappingsPage.getGlobalErrors2();
		assertEquals(ImmutableList.of("Invalid project key HOSP: Project key or name is already used. "
				+ "If you want to import to an existing project the name and the key must match it exactly."), errors);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-559
	 */
	public void testCsvIncludesProjectsThatExist() {
		CsvSetupPage setupPage = product.visit(LoginPage.class)
				.loginAsSysAdmin(CsvSetupPage.class)
				.webSudo();

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/JIM-559-1.csv");

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
				.setReadFromCsv(true).next();

		fieldMappingsPage.setFieldMapping("pkey", "project.key");
		fieldMappingsPage.setFieldMapping("project", "project.name");
		fieldMappingsPage.setFieldMapping("summary", "summary");

		assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("HSP-1", new NullProgressMonitor());
		assertEquals("Summary 1", issue.getSummary());

		issue = restClient.getIssueClient().getIssue("MKY-1", new NullProgressMonitor());
		assertEquals("Summary", issue.getSummary());
	}

}
