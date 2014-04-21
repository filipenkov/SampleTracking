/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.po.bugzilla.BugzillaImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.*;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.*;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import junitx.framework.StringAssert;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestBugzillaImporter extends ScreenshotFuncTestCase {

	protected String instance;

	@Override
	public void setUpTest() {
		super.setUpTest();

		this.instance = ITUtils.BUGZILLA_3_6_4;

		administration.restoreBlankInstance();
		administration.attachments().enable();
        administration.timeTracking().enable("8", "5", TimeTracking.Format.PRETTY, TimeTracking.Unit.HOUR, TimeTracking.Mode.MODERN);
	}

	@Test
	public void testSimpleImport() throws Exception {
		final CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

        final int expectedIssues = 45;

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("TestProduct", "TestProduct", "TES");

		// No way to check project nam & key - they are not displayed in the UI
		final ImporterCustomFieldsPage customFieldsPage = projectsMappingsPage.next();

		customFieldsPage.selectFieldMapping("priority", "issue-field:priority");

		final ImporterFieldMappingsPage fieldMappingsPage = customFieldsPage.next();

		fieldMappingsPage.setMappingEnabled("bug_severity", true);
		fieldMappingsPage.setMappingEnabled("login_name", true);
		fieldMappingsPage.setMappingEnabled("resolution", true);

		final ImporterValueMappingsPage valueMappingsPage = fieldMappingsPage.next();

		assertNotNull(valueMappingsPage.getMappingId("login_name", "wseliga@atlassian.com"));
		assertNotNull(valueMappingsPage.getMappingId("login_name", "pniewiadomski@atlassian.com"));

		valueMappingsPage.setMappingSelect("resolution", "DUPLICATE", "Cannot Reproduce");

		valueMappingsPage.setMappingSelect("bug_status", "ASSIGNED", "Resolved");
		valueMappingsPage.setMappingSelect("bug_status", "NEW", "Reopened");

		final ImporterLinksPage linksPage = valueMappingsPage.next();

		final ImporterFinishedPage importerLogsPage = linksPage.next().waitUntilFinished();
		assertTrue(importerLogsPage.isSuccess());

		assertEquals(0, importerLogsPage.getGlobalErrors().size());
		assertEquals("2", importerLogsPage.getProjectsImported());
        assertEquals(Integer.toString(expectedIssues), importerLogsPage.getIssuesImported());

		final JiraRestClient restClient = ITUtils.createRestClient(environmentData);
		final Issue issue = restClient.getIssueClient().getIssue("TES-7", new NullProgressMonitor());
		final List<Attachment> attachments = ImmutableList.copyOf(issue.getAttachments());
		assertEquals(3, attachments.size());
		assertEquals("atlassian-jira-plugin-timesheet-1.8.jar", attachments.get(0).getFilename());
		assertEquals(64892, attachments.get(0).getSize());
		assertEquals("atlassian-universal-plugin-manager-plugin-1.0.1.jar", attachments.get(1).getFilename());
		assertEquals(1931045, attachments.get(1).getSize());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(expectedIssues, search.getTotal());

		final Issue issueWithMultiselect = restClient.getIssueClient().getIssue("TES-43", new NullProgressMonitor());
		final Field multiselect = issueWithMultiselect.getFieldByName("Multiselect");
		assertNotNull(multiselect);

        final JSONArray multiselectValues = (JSONArray) multiselect.getValue();
        assertEquals(3, multiselectValues.length());
        assertEquals("A", ((JSONObject) multiselectValues.get(0)).getString("value"));
        assertEquals("B", ((JSONObject) multiselectValues.get(1)).getString("value"));
        assertEquals("G", ((JSONObject) multiselectValues.get(2)).getString("value"));

		final Issue issueWithVotes = restClient.getIssueClient().getIssue("TES-5", new NullProgressMonitor());
		assertEquals(1, issueWithVotes.getVotes().getVotes());

		final Project project = restClient.getProjectClient().getProject("TES", new NullProgressMonitor());

		assertEquals(ImmutableList.of("Component1|user1@example.com", "Component2|user2@example.com",
				"Component3|user3@example.com", "Component4|user4@example.com",
				"TestComponent|piotr.maruszak@spartez.com"),
				Immutables.transformThenCopyToList(project.getComponents(), new Function<BasicComponent, String>() {
			@Override
			public String apply(@Nullable BasicComponent input) {
				return input.getName() + "|"
						+ restClient.getComponentClient().getComponent(
						input.getSelf(), new NullProgressMonitor()).getLead().getName();
			}
		}));

        final Issue issueWithTimeTracking = restClient.getIssueClient().getIssue("TES-10", new NullProgressMonitor());
        assertNotNull(issueWithTimeTracking.getTimeTracking());
        assertEquals(Integer.valueOf(1020), issueWithTimeTracking.getTimeTracking().getTimeSpentMinutes());
	}

	public void testClearValueMappings() throws Exception {
		final CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		setupPage.setConfigFile(new File("src/test/resources/bugzilla/JIM-318.config").getAbsolutePath());
		final ImporterFieldMappingsPage fieldMappingsPage = setupPage.next().next().next();
		assertTrue(fieldMappingsPage.isMappingSelected("login_name"));

		ImporterValueMappingsPage valueMappingsPage = fieldMappingsPage.next();
		assertTrue(valueMappingsPage.hasMappingFor("login_name"));
		assertEquals("pawel", valueMappingsPage.getMappingValue("login_name", "pniewiadomski@atlassian.com"));

		valueMappingsPage = valueMappingsPage.prev().setMappingEnabled("login_name", false).next();
		assertFalse(valueMappingsPage.hasMappingFor("login_name"));
		assertTrue(valueMappingsPage.hasMappingFor("bug_status"));
		assertTrue(valueMappingsPage.hasMappingFor("resolution"));
	}

	private CommonImporterSetupPage getSetupPage() {
		return jira.gotoLoginPage().loginAsSysAdmin(BugzillaImporterSetupPage.class);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-414
	 */
	public void testReuseAccountsByEmail() throws IOException {
		administration.usersAndGroups().addUser("pn", "abc123", "Pawel Niewiadomski", "pniewiadomski@atlassian.com");
		administration.usersAndGroups().addUser("ws", "abc123", "Wojciech Seliga", "wseliga@atlassian.com");

        CommonImporterSetupPage setupPage = getSetupPage();
        ITUtils.setupConnection(setupPage, instance);
		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("TestProduct", "TestProduct", "TES");
		assertTrue(projectsMappingsPage.next().next().next().next().next().waitUntilFinished().isSuccess());
		JiraRestClient restClient = ITUtils.createRestClient(environmentData);

		Issue issue = restClient.getIssueClient().getIssue("TES-19", new NullProgressMonitor());
		assertEquals("pn", issue.getReporter().getName());

		assertEquals("ws", issue.getAssignee().getName());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-278
	 */
	public void testMappingWorksForMixedCase() {
		CommonImporterSetupPage setupPage = getSetupPage();
        ITUtils.setupConnection(setupPage, instance);
		setupPage.setConfigFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/bugzilla/JIM-278.config");
		assertTrue(setupPage.next().next().next().next().next().next().waitUntilFinished().isSuccess());

		assertTrue(administration.usersAndGroups().userExists("mix"));
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-439
	 */
	public void testImportWhenLicenseLowerThanNumberOfUsers() {
		administration.switchToPersonalLicense();

		final CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("TestProduct", "TestProduct", "TES");
		ImporterFinishedPage logs = projectsMappingsPage.next().next().next().next().next().waitUntilFinished();
		assertTrue(Integer.valueOf(logs.getIssuesImported()) >= 42);
		assertTrue(Integer.valueOf(logs.getProjectsImported()) >= 2);
		assertEquals("11 users associated with import. 1 will be imported as active due to license limits. Check log for details.",
				Iterables.getFirst(logs.getWarnings(), null));
		StringAssert.assertContains(
				"11 users associated with import. 1 will be imported as active due to license limits.", logs.getLog());

		for(String user : new String[] {"user1@example.com", "user2@example.com", "user3@example.com",
				"user4@example.com", "user5@example.com", "user6@example.com", "wseliga@atlassian.com",
				"pniewiadomski@atlassian.com", "piotr.maruszak@spartez.com", "mixedcase@gmail.com"}) {
			administration.usersAndGroups().gotoViewUser(user);
			tester.assertTextNotPresent("jira-users");
		}

		administration.usersAndGroups().gotoViewUser("disabled@localhost.localdomain");
		tester.assertTextPresent("jira-users");
	}

	/**
	 * Test linking works
	 */
	public void testLinkingWorks() {
		administration.issueLinking().enable();
		administration.issueLinking().addIssueLink("Depends", "blocks", "is blocked by");
		administration.issueLinking().addIssueLink("Duplicates", "duplicates", "is duplicated by");

		final CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("TestProduct", "TestProduct", "TES");
		ImporterLinksPage linksPage = projectsMappingsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		ImporterFinishedPage logsPage = linksPage.next().waitUntilFinished(120);
		assertTrue(logsPage.isSuccess());

		StringAssert.assertNotContains("Unable to link issue from", logsPage.getLog());
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-439
	 */
	public void testLinkingWithinBoundariesOfExternalSystemUrl() {
		ITUtils.enableSubtasks(administration);
		administration.issueLinking().enable();
		administration.issueLinking().addIssueLink("Depends", "blocks", "is blocked by");
		administration.issueLinking().addIssueLink("Duplicates", "duplicates", "is duplicated by");

		CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		ImporterProjectsMappingsPage projectsPage = setupPage.next()
				.setProjectImported("A", false)
				.createProject("TestProduct", "TestProduct", "TES");

		ImporterLinksPage linksPage = projectsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		ImporterFinishedPage logsPage = linksPage.next().waitUntilFinished(120);
		assertFalse(logsPage.isSuccess());
		assertEquals("1", logsPage.getProjectsImported());
		String log = logsPage.getLog();
		StringAssert.assertContains("Unable to link issue from 44 to 3 with link named 'Depends': Cannot find imported issue key for external id '44'",	log);
		StringAssert.assertContains("Unable to link issue from 45 to 44 with link named 'Depends': Cannot find imported issue key for external id '45'", log);

		// re-run import, import only one project that was not imported previously, it should link to other issues
		setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		projectsPage = setupPage.next()
				.setProjectImported("TestProduct", false)
				.createProject("A", "AAA", "AAJ");

		linksPage = projectsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		logsPage = linksPage.next().waitUntilFinished();
		assertTrue(logsPage.isSuccess());
		assertEquals("1", logsPage.getProjectsImported());
		log = logsPage.getLog();
		StringAssert.assertNotContains("Unable to link issue from", log);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-511
	 */
	public void testLinkingDoesnCrossBoundariesOfExternalSystemUrl() throws IOException {
		ITUtils.enableSubtasks(administration);
		administration.issueLinking().enable();
		administration.issueLinking().addIssueLink("Depends", "blocks", "is blocked by");
		administration.issueLinking().addIssueLink("Duplicates", "duplicates", "is duplicated by");

		CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		ImporterProjectsMappingsPage projectsPage = setupPage.next()
				.createProject("TestProduct", "TestProduct", "TES")
                .setProjectImported("A", false);

		ImporterLinksPage linksPage = projectsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		ImporterFinishedPage logsPage = linksPage.next().waitUntilFinished(120);
		assertFalse(logsPage.isSuccess());
		assertEquals("1", logsPage.getProjectsImported());
		String log = logsPage.getLog();
		StringAssert.assertContains("Unable to link issue from 45 to 44 with link named 'Depends': Cannot find imported issue key for external id '45'", log);
		StringAssert.assertContains("Unable to link issue from 44 to 3 with link named 'Depends': Cannot find imported issue key for external id '44'", log);

		administration.runJellyScript(
				IOUtils.toString(TestBugzillaImporter.class.getResourceAsStream("/bugzilla/changeExternalSystemUrl.jelly")));
		administration.reIndex();

		// re-run import, import only one project that was not imported previously, it should not link to other issues
		setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		projectsPage = setupPage.next().setImportAllProjects(false).setProjectImported("A", true).createProject("A", "AAA", "AAJ");

		linksPage = projectsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		logsPage = linksPage.next().waitUntilFinished();
		assertFalse(logsPage.isSuccess());
		assertEquals("1", logsPage.getProjectsImported());
		log = logsPage.getLog();
		StringAssert.assertContains("Unable to link issue from 44 to 3 with link named 'Depends': Cannot find imported issue key for external id '3'", log);
	}


	/**
	 * Test upgrading a text field to a free text
	 * https://studio.atlassian.com/browse/JIM-583
	 */
	public void testTextFieldToFreeText() {
		administration.customFields().addCustomField(CustomFieldConstants.TEXT_FIELD_TYPE, "Text field");
		administration.customFields().addCustomField(CustomFieldConstants.FREE_TEXT_FIELD_TYPE, "Free text");

		CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, instance);

		ImporterProjectsMappingsPage projectsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("TestProduct", "TestProduct", "TES");

		ImporterCustomFieldsPage customFieldsPage = projectsPage.next();
		assertEquals(ImmutableList.of("Free text", "Text field", "Other..."),
				Immutables.transformThenCopyToList(customFieldsPage.getSelectOptions("cf_free_text_select"), ITUtils.TEXT_FUNCTION));
		assertEquals(ImmutableList.of("Free text", "Large text box", "Other..."),
				Immutables.transformThenCopyToList(customFieldsPage.getSelectOptions("cf_large_text_box_select"), ITUtils.TEXT_FUNCTION));
	}
}
