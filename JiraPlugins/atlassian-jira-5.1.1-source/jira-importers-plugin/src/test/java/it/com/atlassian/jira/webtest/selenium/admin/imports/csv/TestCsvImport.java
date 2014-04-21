/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import junitx.framework.StringAssert;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Map;

public class TestCsvImport extends ScreenshotFuncTestCase {
	private static final String PRJ_KEY = "CSV";

	private JiraRestClient restClient;
    private CsvSetupPage setupPage;

    @Before
	public void setUpTest() {
		super.setUpTest();
		administration.restoreBlankInstance();

		ITUtils.doWebSudoCrap(navigation, tester);

		restClient = ITUtils.createRestClient(environmentData);

        setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);
	}

	/**
	 * test for JIM-297 and overall smoke-test for CSV importer
	 */
	@Test
	public void testImportingVersionsAndComponents() {
		final ImporterFinishedPage finishedPage = doImport("JIM-297.csv", "JIM-297.cfg");
		assertTrue(finishedPage.isSuccess());

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

	private String prepareConfiguration(String source, Map<String, String> replacements) throws IOException {
		final File tmpConfiguration = File.createTempFile("csv", ".config");
		tmpConfiguration.deleteOnExit();

		final InputStream is = new FileInputStream(source);
		try {
			String output = IOUtils.toString(is);
			for(Map.Entry<String, String> replacement : replacements.entrySet()) {
				output = output.replace(replacement.getKey(), replacement.getValue());
			}

			final OutputStream os = new FileOutputStream(tmpConfiguration);
			try {
				IOUtils.write(output, os);
			} finally {
				IOUtils.closeQuietly(os);
			}
		} finally {
			IOUtils.closeQuietly(is);
		}
		return tmpConfiguration.getPath();
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-236
	 */
	@Test
	public void testMappingToExistingCustomFieldIsPreserved() throws IOException {
		final String customFieldId = administration.customFields().addCustomField(CustomFieldConstants.SELECT_FIELD_TYPE, "Z");
		administration.customFields().addCustomField(CustomFieldConstants.SELECT_FIELD_TYPE, "A");
		administration.customFields().addCustomField(CustomFieldConstants.SELECT_FIELD_TYPE, "B");



		setupPage.setCsvFile(ITUtils.getCsvResource("JIM-67.csv"));
		setupPage.setConfigurationFile(prepareConfiguration(ITUtils.getCsvResource("JIM-236.config"), MapBuilder
				.<String, String>newBuilder().add("customfield_10000", customFieldId).toMap()));

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next().next();

		assertEquals("Z", fieldMappingsPage.getDisplayedFieldMapping("drag & drop"));
	}

	/**
	 * Test case for
	 */
	@Test
	public void testCustomFieldMappingFromConfiguration() throws IOException {
		final String cid1 = administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 1");
		final String cid2 = administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 2");
		final String cid3 = administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 3");
		final String cid4 = administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field 4");

		setupPage.setCsvFile(ITUtils.getCsvResource("testcsvmapper.csv"));
		setupPage.setConfigurationFile(prepareConfiguration(ITUtils.getCsvResource("JIM-375.config"), MapBuilder.<String, String>newBuilder().add(
				"customfield_10000", cid1).add("customfield_10001", cid2)
				.add("customfield_10002", cid3).add("customfield_10003", cid4).toMap()));
		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next().next();

		assertEquals("Text field 4", fieldMappingsPage.getDisplayedFieldMapping("project.key"));
		assertEquals("Text field 3", fieldMappingsPage.getDisplayedFieldMapping("project"));
		assertEquals("Text field 1", fieldMappingsPage.getDisplayedFieldMapping("reporter"));
		assertEquals("Text field 2", fieldMappingsPage.getDisplayedFieldMapping("assignee"));
	}

	// There is no explicit requirement for this functionality, so we may consider disabling the test if it collides with
	// some requirement
	public void testImportOnlyChangedIssues() throws Exception {
		ITUtils.enableSubtasks(administration);
		final ImporterFinishedPage logsPage = doImport("JIM_242/JIM-242-orig.csv", "JIM_242/JIM-242-configuration.txt");
		assertTrue(logsPage.isSuccess());
		assertEquals("2", logsPage.getIssuesImported());

		final ImporterFinishedPage logsPage2 = doImport("JIM_242/JIM-242-new.csv", "JIM_242/JIM-242-configuration-new.txt");
		assertTrue(logsPage2.isSuccess());
		assertEquals(ImmutableList.of("2 of 3 issues have been skipped because they already exist in destination projects."), logsPage2.getWarnings());
		assertEquals("1", logsPage2.getIssuesImported());

		SearchResult result = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(3, result.getTotal());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-515
	 */
	public void testReuseSelectOptions() throws IOException, JSONException, SAXException {
		String customFieldId = administration.customFields().addCustomField(
				CustomFieldConstants.SELECT_FIELD_TYPE, "Select custom field_515");
		final String fieldId = StringUtils.removeStart(customFieldId, "customfield_");
		administration.customFields().addOptions(fieldId, "Merchandise", "Service");

		tester.gotoPage("/secure/admin/ConfigureCustomField!default.jspa?customFieldId=" + fieldId);
  		tester.clickLinkWithText("Edit Options");
		final String merchandiseId = StringUtils.removeStart(tester.getDialog().getResponse().getLinkWith("Disable").getID(), "disable_");

		final ImporterFinishedPage finishedPage = doImport("JIM-515.csv", "JIM-515.config");
		assertTrue(finishedPage.isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("SCF-1", new NullProgressMonitor());
		Field field = issue.getField(customFieldId);
		assertEquals(merchandiseId, ((JSONObject) field.getValue()).get("id")); // this is a hack - using fieldId for optionId

		final Issue scf2 = restClient.getIssueClient().getIssue("SCF-2", new NullProgressMonitor());
		final Field field2 = scf2.getField(customFieldId);
        assertEquals(String.valueOf(Long.valueOf(merchandiseId)+2), ((JSONObject) field2.getValue()).get("id"));
	}

	/**
	 * Empty mappings cause import to ingore issues.
	 * Test case for https://studio.atlassian.com/browse/JIM-534
	 */
	public void testEmptyMappings() {
		setupPage.setCsvFile(ITUtils.getCsvResource("comments.csv"));
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
		setupPage.setCsvFile(ITUtils.getCsvResource("JIM-559.csv"));

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
				.setReadFromCsv(true)
				.next();

		fieldMappingsPage.setFieldMappingByVal("pkey", "project.key");
		fieldMappingsPage.setFieldMappingByVal("plead", "project.lead");
		fieldMappingsPage.setFieldMappingByVal("project", "project.name");
		fieldMappingsPage.setFieldMappingByVal("summary", "summary");

		fieldMappingsPage.next();

		List<String> errors = fieldMappingsPage.getGlobalErrors2();
		assertEquals(ImmutableList.of("Invalid project key HOSP: Project key or name is already used. "
				+ "If you want to import to an existing project the name and the key must match it exactly."), errors);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-559
	 */
	public void testCsvIncludesProjectsThatExist() {
		setupPage.setCsvFile(ITUtils.getCsvResource("JIM-559-1.csv"));

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
				.setReadFromCsv(true).next();

		fieldMappingsPage.setFieldMappingByVal("pkey", "project.key");
		fieldMappingsPage.setFieldMappingByVal("project", "project.name");
		fieldMappingsPage.setFieldMappingByVal("summary", "summary");

		assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("HSP-1", new NullProgressMonitor());
		assertEquals("Summary 1", issue.getSummary());

		issue = restClient.getIssueClient().getIssue("MKY-1", new NullProgressMonitor());
		assertEquals("Summary", issue.getSummary());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-585
	 */
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
	 * Test case for https://studio.atlassian.com/browse/JIM-610
	 */
	public void testComponentMatchingIsNotCaseSensitive() {
		administration.project().addProject("Test for JIM-610", "TJIM", "admin");
		administration.project().addComponent("TJIM", "core", null, null);

		setupPage.setCsvFile(ITUtils.getCsvResource("JIM-610.csv"))
				.setConfigurationFile(ITUtils.getCsvResource("JIM-610.config"));

		com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
				.setExistingProject("TJIM").next();

		ImporterFinishedPage finishedPage = fieldMappingsPage.next().next().waitUntilFinished();
		assertTrue(finishedPage.isSuccess());

		Issue issue = restClient.getIssueClient().getIssue("TJIM-1", new NullProgressMonitor());
		assertEquals(ImmutableList.of("core"),
				Immutables.transformThenCopyToList(issue.getComponents(), new Function<BasicComponent, String>() {
			@Override
			public String apply(@Nullable BasicComponent input) {
				return input.getName();
			}
		}));
	}

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-664
     */
    @Test
    public void testIssueCyclicReference() {
        ITUtils.enableSubtasks(administration);
        administration.subtasks().addSubTaskType("Sub-Task", "");

        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-664.csv")).setConfigurationFile(ITUtils.getCsvResource("JIM-664.config"))
                .next().next().next().next().waitUntilFinished();

        final Issue issue = restClient.getIssueClient().getIssue("MKY-1", new NullProgressMonitor());
        assertEquals("This is just a sampe", issue.getSummary());
        assertNull(issue.getFieldByName("Parent"));
    }

    /**
     */
    @Test
    public void testMultiuserPicker() throws IOException, JSONException {
        administration.usersAndGroups().addUser("luser");
        administration.usersAndGroups().addUser("luser1");
        administration.usersAndGroups().addUser("luser2");
        administration.usersAndGroups().addGroup("Group 1");
        administration.usersAndGroups().addGroup("Group 2");
        administration.usersAndGroups().addGroup("Group 3");

        final String multiUserPicker = administration.customFields().addCustomField(CustomFieldConstants.MULTIUSER_PICKER_FIELD_TYPE, "Multi-user picker");
        final String multiGroupPicker = administration.customFields().addCustomField(CustomFieldConstants.MULTIGROUP_PICKER_FIELD_TYPE, "Multi-group picker");
        final String groupPicker = administration.customFields().addCustomField(CustomFieldConstants.GROUP_PICKER_FIELD_TYPE, "Group picker");
        final String userPicker = administration.customFields().addCustomField(CustomFieldConstants.USER_PICKER_FIELD_TYPE, "User picker");

        setupPage.setCsvFile(ITUtils.getCsvResource("JIM-620.csv"));
        setupPage.setConfigurationFile(prepareConfiguration(ITUtils.getCsvResource("JIM-620.config"),
                MapBuilder.<String, String>newBuilder().add("customfield_multiUserPicker", multiUserPicker)
                        .add("customfield_multiGroupPicker", multiGroupPicker)
                        .add("customfield_groupPicker", groupPicker)
                        .add("customfield_userPicker", userPicker).toMap()));

        assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());

        {
            Issue i1 = restClient.getIssueClient().getIssue("MKY-1", new NullProgressMonitor());
            assertEquals("Test", i1.getSummary());
            final JSONArray users = (JSONArray) i1.getField(multiUserPicker).getValue();
            assertEquals(3, users.length());
            assertEquals("admin", ((JSONObject) users.get(0)).get("name"));
            assertEquals("luser", ((JSONObject) users.get(1)).get("name"));
            assertEquals("luser1", ((JSONObject) users.get(2)).get("name"));

            final JSONArray groups = (JSONArray) i1.getField(multiGroupPicker).getValue();
            assertEquals(2, groups.length());
            assertEquals("Group 1", ((JSONObject) groups.get(0)).get("name"));
            assertEquals("Group 2", ((JSONObject) groups.get(1)).get("name"));

            assertNull(i1.getField(groupPicker).getValue());
            JSONObject user = (JSONObject) i1.getField(userPicker).getValue();
            assertEquals("luser2", user.get("name"));
        }

        {
            Issue i1 = restClient.getIssueClient().getIssue("MKY-2", new NullProgressMonitor());
            assertEquals("Test 2", i1.getSummary());
            final JSONArray users = (JSONArray) i1.getField(multiUserPicker).getValue();
            assertEquals(1, users.length());
            assertEquals("admin", ((JSONObject) users.get(0)).get("name"));

            assertNull(i1.getField(multiGroupPicker).getValue());

            JSONObject group = (JSONObject) i1.getField(groupPicker).getValue();
            assertEquals("Group 3", group.get("name"));

            assertNull(i1.getField(userPicker).getValue());
        }

        {
            Issue i1 = restClient.getIssueClient().getIssue("MKY-3", new NullProgressMonitor());
            assertEquals("Test", i1.getSummary());
            final JSONArray users = (JSONArray) i1.getField(multiUserPicker).getValue();
            assertEquals(3, users.length());
            assertEquals("admin", ((JSONObject) users.get(0)).get("name"));
            assertEquals("luser", ((JSONObject) users.get(1)).get("name"));
            assertEquals("luser1", ((JSONObject) users.get(2)).get("name"));

            final JSONArray groups = (JSONArray) i1.getField(multiGroupPicker).getValue();
            assertEquals(2, groups.length());
            assertEquals("Group 1", ((JSONObject) groups.get(0)).get("name"));
            assertEquals("Group 3", ((JSONObject) groups.get(1)).get("name"));

            JSONObject group = (JSONObject) i1.getField(groupPicker).getValue();
            assertEquals("Group 1", group.get("name"));

            JSONObject user = (JSONObject) i1.getField(userPicker).getValue();
            assertEquals("luser2", user.get("name"));
        }
    }

	public void testMappingCommentAuthor() throws Exception {
		administration.usersAndGroups().addUser("userone", "pass", "User One", "userone@localhost");
		administration.usersAndGroups().addUser("userfour", "pass", "User Four", "userfour@localhost");

		final ImporterFinishedPage finishedPage = doImport("comment_user_mapper.csv", "comment_user_mapper.properties");
		assertTrue(finishedPage.isSuccessWithNoWarnings());

		verifyMappedReporterAndCommentAuthorIssue("HSP-1", "Test Issue 1", "userone", "User One", "comment by userone");
		verifyMappedReporterAndCommentAuthorIssue("HSP-2", "Test Issue 2", "usertwo", "User Two", "comment by User Two");
		verifyMappedReporterAndCommentAuthorIssue("HSP-3", "Test Issue 3", "userthree", "User Three", "comment by User Three");
		verifyMappedCommentAuthorIssue("HSP-4", "Test Issue 4", "userfour", "User Four", "comment by User Four");
		verifyMappedCommentAuthorIssue("HSP-5", "Test Issue 5", "userfour", "User Four", "comment by User Four");
	}


	private void verifyMappedReporterAndCommentAuthorIssue(String issueKey, String summary, String userName, String userDisplayName, String commentBody) {
		final Issue issue = verifyMappedCommentAuthorIssue(issueKey, summary, userName, userDisplayName, commentBody);

		final BasicUser reporter = issue.getReporter();
		assertNotNull(reporter);
		assertEquals(userDisplayName, reporter.getDisplayName());
		assertEquals(userName, reporter.getName());

	}
	@SuppressWarnings("ConstantConditions")
	private Issue verifyMappedCommentAuthorIssue(String issueKey, String summary, String userName, String userDisplayName, String commentBody) {
		final Issue issue = restClient.getIssueClient().getIssue(issueKey, new NullProgressMonitor());

		assertEquals(summary, issue.getSummary());
		assertEquals(1, Iterables.size(issue.getComments()));

		final Comment comment = Iterables.getOnlyElement(issue.getComments());
		assertEquals(userName, comment.getAuthor().getName());
		assertEquals(userDisplayName, comment.getAuthor().getDisplayName());
		assertEquals(commentBody, comment.getBody());

		return issue;
	}

	private ImporterFinishedPage doImport(String csv, String config) {
        CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);
		setupPage.setCsvFile(ITUtils.getCsvResource(csv));
		setupPage.setConfigurationFile(ITUtils.getCsvResource(config));
		return setupPage.next().next().next().next().waitUntilFinished();
	}
}
