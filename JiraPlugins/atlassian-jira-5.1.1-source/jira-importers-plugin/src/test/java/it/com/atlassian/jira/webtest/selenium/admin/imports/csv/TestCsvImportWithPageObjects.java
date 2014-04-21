/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.backdoor.JimBackdoor;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvProjectMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvValueMappingsPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import junitx.framework.ListAssert;
import junitx.framework.StringAssert;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junitx.framework.ListAssert.assertContains;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
public class TestCsvImportWithPageObjects extends BaseJiraWebTest {

    public static final NullProgressMonitor PROGRESS_MONITOR = new NullProgressMonitor();
    private JiraRestClient restClient;
    private JimBackdoor jimBackdoor;
    private CsvSetupPage setupPage;

    @Before
    public void setUpTest() {
        restClient = ITUtils.createRestClient(jira.environmentData());
        backdoor.restoreData("blankprojects.xml");
        jimBackdoor = new JimBackdoor(jira.environmentData());
        setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-77
     */
    @Test
    public void testInvalidDateFormat() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-77.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-77.config"));

        CsvProjectMappingsPage projectMappingsPage = setupPage.next().setDateImportFormat("dd/MM/yyyy HH:mm:ssf").nextWithError();

        assertContains(projectMappingsPage.getFieldErrors(), "Invalid date format: Illegal pattern character 'f'");
    }


    /**
     * Test case for https://studio.atlassian.com/browse/JIM-77
     */
    @Test
    public void testImportDateFieldsAndEmptyHeaders() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-77.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-77.config"));

        com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next().next();

        ListAssert.assertContains(fieldMappingsPage.getHints(),
                "CSV file has at least one empty header, corresponding column will not be imported.");

        assertTrue(fieldMappingsPage.hasDuplicateColumnInfo(""));

        assertEquals("CSV file has multiple columns having the same name. "
                + "There's only a limited set of JIRA fields that can have multiple values assigned to. "
                + "Mappings for those columns will be limited to those fields only.",
                fieldMappingsPage.getDuplicateColumnMessage(""));

        assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

        Issue issue = restClient.getIssueClient().getIssue("CSVI-1", PROGRESS_MONITOR);
        assertNotNull(issue);
        assertEquals(new DateTime(1999, 1, 21, 12, 0, 0, 0).toInstant(), issue.getCreationDate().toInstant());
    }


    /**
     * Test case for https://studio.atlassian.com/browse/JIM-225
     */
    @Test
    public void testInvalidProjectData() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-225.csv"));

        List<String> errors = setupPage.next().setReadFromCsv(true).next()
                .setFieldMappingByVal("ProjectName", "project.name")
                .setFieldMappingByVal("ProjectKey", "project.key")
                .setFieldMappingByVal("ProjectDescription", "project.description")
                .setFieldMappingByVal("ProjectUrl", "project.url")
                .setFieldMapping("summary", "summary").nextWithError().getGlobalErrors2();

        assertEquals(ImmutableList.of(
                "Invalid project key TP23: You must specify a unique project key, at least 2 characters long, containing only uppercase letters.",
                "Invalid project key newx: You must specify a unique project key, at least 2 characters long, containing only uppercase letters.",
                "Invalid project url hzxsttp://la: The URL specified is not valid - it must start with http://",
                "Invalid project key NEWX12: You must specify a unique project key, at least 2 characters long, containing only uppercase letters."), errors);
    }

    /*
      * Tests uploads CSV file and configuration then checks if fields were correctly initialized
      * to values read from configuration file.
      */
    @Test
    public void testIfConfigurationIsReadProperly() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-80.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-80.config"));

        CsvProjectMappingsPage projectMappingsPage = setupPage.next();

        assertEquals("@atlassian.com", projectMappingsPage.getUserEmailSuffix());

        assertEquals("TICIRP", projectMappingsPage.getProjectKey());

        assertFalse(projectMappingsPage.isReadingFromCsv());

        com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = projectMappingsPage.next();

        List<WebElement> mappings = fieldMappingsPage.getMappingFields();
        assertEquals(4, mappings.size());

        assertEquals(IssueFieldConstants.STATUS, mappings.get(0).getAttribute("value"));
        assertEquals("", mappings.get(1).getAttribute("value"));
        assertEquals(IssueFieldConstants.SUMMARY, mappings.get(2).getAttribute("value"));
        assertEquals(IssueFieldConstants.ISSUE_TYPE, mappings.get(3).getAttribute("value"));

        CsvValueMappingsPage valueMappingsPage = fieldMappingsPage.next();

        // 4th page
        assertEquals("4", valueMappingsPage.getValue("value.0"));
        assertEquals("1", valueMappingsPage.getValue("value.1"));

        // If GH is installed there are additional fields available
        assertThat(Immutables.transformThenCopyToList(valueMappingsPage.getSelectOptions("value.1"), ITUtils.TEXT_FUNCTION),
                anyOf(
                        equalTo(ImmutableList.of("Map as is", "Import as blank", "Bug", "Epic",
                                "Improvement", "New Feature", "Story", "Task", "Technical task")),
                        equalTo(ImmutableList.of("Map as is", "Import as blank", "Bug",
                                "Improvement", "New Feature", "Task"))));


        assertTrue(valueMappingsPage.next().waitUntilFinished().isSuccess());
    }

    @Test
    public void testGoingToSecondPage() throws Exception {
        setupPage.setCsvFile(ITUtils.getCsvResource("testcsvmapper.csv"));
        setupPage.next();
    }

    /**
	 * Test case for https://studio.atlassian.com/browse/JIM-480
	 */
	@Test
	public void testAddConstants() {
		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/medium.csv");
		setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/existingCustomDateTime.properties");
		CsvValueMappingsPage valueMappingsPage = setupPage.next().next().next();

		assertEquals(5, valueMappingsPage.getAddConstantLinks().size());

		valueMappingsPage.addConstant("priority", "normal");

		assertEquals(4, valueMappingsPage.getAddConstantLinks().size());
		assertEquals("6", valueMappingsPage.getValue("value.2"));
	}

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-669
     */
    @Test
    public void testSummaryIsRequired() {
        setupPage.setCsvFile(ITUtils.getCsvResource("comments.csv"));
        com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next().setReadFromCsv(false).setExistingProject("MKY").next();

        assertFalse(fieldMappingsPage.isNextEnabled());

        fieldMappingsPage.setFieldMapping("Summary", "Summary");

        assertTrue(fieldMappingsPage.isNextEnabled());
    }


    /**
     * Empty mappings cause import to ingore issues.
     * Test case for https://studio.atlassian.com/browse/JIM-534
     */
    @Test
    public void testEmptyMappings() {
        setupPage.setCsvFile(ITUtils.getCsvResource("comments.csv"));
        com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
                .setReadFromCsv(false)
                .setExistingProject("MKY").next();
        fieldMappingsPage.setFieldMapping("Summary", "summary")
                .setFieldMapping("Status", "reporter");
        fieldMappingsPage.setMapValues("Summary", true);

        assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

        SearchResult search = restClient.getSearchClient().searchJql("project=MKY", PROGRESS_MONITOR);
        assertEquals(4, search.getTotal());

        Issue issue = restClient.getIssueClient().getIssue("MKY-1", PROGRESS_MONITOR);
        assertEquals("Z", issue.getSummary());
    }

    /**
     * Import fails when delimiter is set to ,
     *
     * https://studio.atlassian.com/browse/JIM-373
     */
    @Test
    public void testDelimiterSetToCommaShouldNotBreakImport() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-67.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-67.config"));
        setupPage.setDelimiter(",");
        assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-420
     */
    @Test
    public void testImportMultilineFields() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-420.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-420.config"));

        final ImporterFinishedPage logsPage = setupPage.next().next().next().next().waitUntilFinished();
        assertTrue(logsPage.isSuccess());
        assertEquals("3", logsPage.getIssuesImported());
        Issue issue = restClient.getIssueClient().getIssue("MIF-1", PROGRESS_MONITOR);
        assertEquals("Если в xml для построения моделей приходит Devision, Launcher падает. \n"
                + "\n"
                + "А должен - отрабатывать все остальные и не падать",
                issue.getDescription());
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-466
     */
    @Test
    public void testCreatedDate() throws IOException {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-466.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-466.config"));
        ImporterFinishedPage finishPage = setupPage.next().next().next().next().waitUntilFinished();
        assertTrue(finishPage.isSuccess());
        assertEquals(ImmutableList.of("Import completed with 2 warnings:",
                "Commenter named not found. Creating issue with currently logged in user instead",
                "Commenter named kirkpsc not found. Creating issue with currently logged in user instead"), finishPage.getWarnings());

        Issue issue = restClient.getIssueClient().getIssue("DC-1", PROGRESS_MONITOR);
        assertEquals(new DateTime(2001, 05, 30, 12, 00, 0, 0), issue.getCreationDate());
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-91
     */
    @Test
    public void testDateCustomFieldsParsing() throws IOException {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-91.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("JIM-91.config"));
        assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());

        Issue issue = restClient.getIssueClient().getIssue("CDAT-1", PROGRESS_MONITOR);
        Map<String, Field> fields = ITUtils.getFieldsMap(issue.getFields());
        assertTrue(fields.containsKey("Custom date time"));
        assertTrue(fields.containsKey("Custom date picker"));

        assertEquals(new DateTime(2003, 03, 02, 13, 23, 00, 000),
                ISODateTimeFormat.dateTimeParser().parseDateTime(
                        (String) fields.get("Custom date time").getValue()));

        assertEquals(new DateTime(2003, 05, 04, 00, 00, 0, 0),
                ISODateTimeFormat.dateTimeParser().parseDateTime(
                        (String) fields.get("Custom date picker").getValue()));
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-559
     */
    @Test
    public void testCsvIncludesConflictingProjects() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-559.csv"));

        com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
                .setReadFromCsv(true)
                .next();

        fieldMappingsPage.setFieldMappingByVal("pkey", "project.key")
            .setFieldMappingByVal("plead", "project.lead")
            .setFieldMappingByVal("project", "project.name")
            .setFieldMappingByVal("summary", "summary");

        fieldMappingsPage.next();

        List<String> errors = fieldMappingsPage.getGlobalErrors2();
        assertEquals(ImmutableList.of("Invalid project key HOSP: Project key or name is already used. "
                + "If you want to import to an existing project the name and the key must match it exactly."), errors);
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-559
     */
    @Test
    public void testCsvIncludesProjectsThatExist() {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-559-1.csv"));

        com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
                .setReadFromCsv(true).next();

        fieldMappingsPage.setFieldMappingByVal("pkey", "project.key")
            .setFieldMappingByVal("project", "project.name")
            .setFieldMappingByVal("summary", "summary");

        assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

        Issue issue = restClient.getIssueClient().getIssue("HSP-1", PROGRESS_MONITOR);
        assertEquals("Summary 1", issue.getSummary());

        issue = restClient.getIssueClient().getIssue("MKY-1", PROGRESS_MONITOR);
        assertEquals("Summary", issue.getSummary());
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-585
     */
    @Test
    public void testReadFromCsvIsStoreInConfiguration() throws IOException {
        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-559-1.csv"))
                .setConfigurationFile(ITUtils.getCsvResource("JIM-585.config"));

        com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
                .setReadFromCsv(true).next();

        ImporterFinishedPage finishedPage = fieldMappingsPage.next().next().waitUntilFinished();
        assertTrue(finishedPage.isSuccess());
        StringAssert.assertContains("\"mapfromcsv\" : \"true\",", finishedPage.getConfiguration());
    }



    /**
     * Tests to see that if time-tracking is disabled that the wizard does not offer up the fields to map to.
     */
    @Test
    public void testImportWizardWithTimeTrackingDisabled()
    {
        backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_TIMETRACKING, false);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-with-timetracking.csv");
        CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
                .setReadFromCsv(false).setExistingProject("MKY").next();

        Collection<String> options = Collections2.transform(fieldMappingsPage.getSelectOptions(
                fieldMappingsPage.getTargetFieldName("OriginalEstimate")),
                new Function<WebElement, String>() {
                    @Override
                    public String apply(@Nullable WebElement webElement) {
                        return webElement != null ? webElement.getAttribute("value") : "";
                    }
                });

        // Make sure the time tracking options are not there
        assertFalse(Iterables.contains(options, "timeoriginalestimate"));
        assertFalse(Iterables.contains(options, "timeestimate")); // remaining estimate
        assertFalse(Iterables.contains(options, "timespent"));
        assertTrue(Iterables.contains(options, "summary"));
    }

    @Test
    public void testPropertiesFileValidation()
    {
        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/mozilla.csv");
        setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-missingprojectkey.properties");
        assertFalse(setupPage.next().isNextEnabled());

        setupPage = jira.visit(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/mozilla.csv");
        setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-invalidprojectlead.properties");
        setupPage.next().next();
        assertEquals("The user you have specified as project lead does not exist.", setupPage.getFieldErrors().get(0));

        setupPage = jira.visit(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/mozilla.csv");
        setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-duplicateprojectname.properties");
        setupPage.next().next();
        assertEquals("A project with that name already exists.", setupPage.getFieldErrors().get(0));


        setupPage = jira.visit(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/mozilla.csv");
        setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-invalidprojecturl.properties");
        setupPage.next().next();
        assertEquals("The URL specified is not valid - it must start with http://", setupPage.getFieldErrors().get(0));
    }

    @Test
    public void testImportWizardValidation()
    {
        assertFalse(setupPage.isNextEnabled());

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/oneline.csv");
        assertTrue(setupPage.isNextEnabled());
        setupPage.next();
        ListAssert.assertContains(setupPage.getGlobalErrors(), "Could not parse second line (after header row) of CSV file");

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/empty.csv");
        setupPage.next();
        ListAssert.assertContains(setupPage.getFieldErrors(), "empty.csv does not exist or is zero bytes in size.");
    }

    @Test
    public void testImportWithExternalUserManagement()
    {
        backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT, true);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/external-user-management.csv");
        setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial.properties");
        ImporterFinishedPage finishPage = setupPage.next().next().next().next().waitUntilFinished();
        final String error = Iterables.getFirst(finishPage.getGlobalErrors2(), null);
        final String expectedError = ITUtils.getText("jira-importer-plugin.external.user.externalusermanagementenabled");
        assertEquals(expectedError.replaceAll("(  )+", " ") + " chai", error.replaceAll("(  )+", " "));
        StringAssert.assertContains(expectedError + " chai", finishPage.getLog());
    }

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-658
     */
    @Test
    public void hashDoesntMeanComment() {
        assertTrue(setupPage.setCsvFile(ITUtils.getCsvResource("JIM-658.csv")).next()
                .createProject("Hash", "HSH").next().setFieldMapping("Summary", "Summary").setFieldMapping("De#scription", "Description").next().next().waitUntilFinished().isSuccessWithNoWarnings());

        Issue is = restClient.getIssueClient().getIssue("HSH-1", PROGRESS_MONITOR);
        assertEquals("test#ing", is.getSummary());
        assertEquals("this is a test for hash handling", is.getDescription());
    }

    @Test
    public void assigneeIsAddedToDevelopers() {
        backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_TIMETRACKING, true);
        assertTrue(setupPage.setCsvFile(ITUtils.getCsvResource("JIM-360.csv")).setConfigurationFile(ITUtils.getCsvResource("JIM-360.config")).next()
                .createProject("Assignee", "ASG").next().next().next().waitUntilFinished().isSuccess());

        assertEquals(ImmutableList.of("Developers", "Users"), jimBackdoor.project().getUserRoles("ASG", "adam"));
    }

}
