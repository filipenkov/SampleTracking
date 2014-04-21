/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.bugzilla;

import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.po.bugzilla.BugzillaImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.CommonImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterCustomFieldsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterLinksPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.po.common.ImporterValueMappingsPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Attachment;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import junitx.framework.StringAssert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestBugzillaImporter extends ScreenshotFuncTestCase {

	@Override
	public void setUpTest() {
		if (ITUtils.skipExternalSystems()) {
			return;
		}
		product = TestedProductFactory.create(JiraTestedProduct.class);
		administration.restoreBlankInstance();
		administration.attachments().enable();
	}

	@Test
	public void testSimpleImport() throws Exception {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("Invalid component id", "Invalid component id", "INV")
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
		assertEquals("3", importerLogsPage.getProjectsImported());
		assertEquals("47", importerLogsPage.getIssuesImported());

		final JiraRestClient restClient = ITUtils.createRestClient(environmentData);
		final Issue issue = restClient.getIssueClient().getIssue("TES-7", new NullProgressMonitor());
		final List<Attachment> attachments = ImmutableList.copyOf(issue.getAttachments());
		assertEquals(4, attachments.size());
		assertEquals("atlassian-jira-plugin-timesheet-1.8.jar", attachments.get(0).getFilename());
		assertEquals(64892, attachments.get(0).getSize());
		assertEquals("atlassian-universal-plugin-manager-plugin-1.0.1.jar", attachments.get(1).getFilename());
		assertEquals(1931045, attachments.get(1).getSize());

		final SearchResult search = restClient.getSearchClient().searchJql("", new NullProgressMonitor());
		assertEquals(47, search.getTotal());

		final Issue issueWithMultiselect = restClient.getIssueClient().getIssue("TES-43", new NullProgressMonitor());
		final Field multiselect = issueWithMultiselect.getField("customfield_10007");
		assertNotNull(multiselect);
		assertEquals("Multiselect", multiselect.getName());
		assertEquals(CustomFieldConstants.MULTISELECT_FIELD_TYPE, multiselect.getType());
		assertEquals("[{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10002\",\"value\":\"A\"},{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10012\",\"value\":\"B\"},{\"self\":\"http:\\/\\/localhost:2990\\/jira\\/rest\\/api\\/latest\\/customFieldOption\\/10013\",\"value\":\"G\"}]", multiselect.getValue());
	}

	public void testClearValueMappings() throws Exception {
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		final CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

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
		return product.gotoLoginPage().loginAsSysAdmin(BugzillaImporterSetupPage.class).webSudo();
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-414
	 */
	public void testReuseAccountsByEmail() throws IOException {
		administration.usersAndGroups().addUser("pn", "abc123", "Pawel Niewiadomski", "pniewiadomski@atlassian.com");
		administration.usersAndGroups().addUser("ws", "abc123", "Wojciech Seliga", "wseliga@atlassian.com");

		if (ITUtils.skipExternalSystems()) {
			return;
		}

        CommonImporterSetupPage setupPage = getSetupPage();
        ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);
		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("Invalid component id", "Invalid component id", "INV")
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
		if (ITUtils.skipExternalSystems()) {
			return;
		}

		CommonImporterSetupPage setupPage = getSetupPage();
        ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);
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
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("Invalid component id", "Invalid component id", "INV")
				.createProject("TestProduct", "TestProduct", "TES");
		ImporterFinishedPage logs = projectsMappingsPage.next().next().next().next().next().waitUntilFinished();
		assertTrue(Integer.valueOf(logs.getIssuesImported()) >= 47);
		assertTrue(Integer.valueOf(logs.getProjectsImported()) >= 3);
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
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		final ImporterProjectsMappingsPage projectsMappingsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("Invalid component id", "Invalid component id", "INV")
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
		administration.subtasks().enable();
		administration.issueLinking().enable();
		administration.issueLinking().addIssueLink("Depends", "blocks", "is blocked by");
		administration.issueLinking().addIssueLink("Duplicates", "duplicates", "is duplicated by");

		CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		ImporterProjectsMappingsPage projectsPage = setupPage.next()
				.setImportAllProjects(true)
				.setProjectImported("Invalid component id", false)
				.createProject("A", "A", "AJJ")
				.createProject("TestProduct", "TestProduct", "TES");

		ImporterLinksPage linksPage = projectsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		ImporterFinishedPage logsPage = linksPage.next().waitUntilFinished(120);
		assertFalse(logsPage.isSuccess());
		assertEquals("2", logsPage.getProjectsImported());
		String log = logsPage.getLog();
		StringAssert.assertContains(
				"Unable to link issue from 3 to 46 with link named 'Depends': Cannot find imported issue key for external id '46'",
				log);
		StringAssert.assertContains("Unable to link issue from 46 to 47 with link named 'Duplicates': Cannot find imported issue key for external id '46'", log);

		// re-run import, import only one project that was not imported previously, it should link to other issues
		setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		projectsPage = setupPage.next()
				.setImportAllProjects(false)
				.setProjectImported("Invalid component id", true)
				.createProject("Invalid component id", "Invalid component id", "INV");

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
		administration.subtasks().enable();
		administration.issueLinking().enable();
		administration.issueLinking().addIssueLink("Depends", "blocks", "is blocked by");
		administration.issueLinking().addIssueLink("Duplicates", "duplicates", "is duplicated by");

		CommonImporterSetupPage setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		ImporterProjectsMappingsPage projectsPage = setupPage.next()
				.createProject("A", "A", "AJJ")
				.createProject("TestProduct", "TestProduct", "TES");
		projectsPage.setProjectImported("Invalid component id", false);

		ImporterLinksPage linksPage = projectsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		ImporterFinishedPage logsPage = linksPage.next().waitUntilFinished(120);
		assertFalse(logsPage.isSuccess());
		assertEquals("2", logsPage.getProjectsImported());
		String log = logsPage.getLog();
		StringAssert.assertContains("Unable to link issue from 3 to 46 with link named 'Depends': Cannot find imported issue key for external id '46'", log);
		StringAssert.assertContains("Unable to link issue from 46 to 47 with link named 'Duplicates': Cannot find imported issue key for external id '46'", log);

		administration.runJellyScript(
				IOUtils.toString(TestBugzillaImporter.class.getResourceAsStream("/bugzilla/changeExternalSystemUrl.jelly")));
		administration.reIndex();

		// re-run import, import only one project that was not imported previously, it should not link to other issues
		setupPage = getSetupPage();
		ITUtils.setupConnection(setupPage, ITUtils.BUGZILLA_3_6_4);

		projectsPage = setupPage.next();
		projectsPage.setImportAllProjects(false).setProjectImported("Invalid component id", true).createProject("Invalid component id", "Invalid component id", "INV");

		linksPage = projectsPage.next().next().next().next();
		linksPage.setSelect("Depends on / Blocks", "Depends");
		linksPage.setSelect("Duplicates", "Duplicates");

		logsPage = linksPage.next().waitUntilFinished();
		assertFalse(logsPage.isSuccess());
		assertEquals("1", logsPage.getProjectsImported());
		log = logsPage.getLog();
		StringAssert.assertContains("Unable to link issue from 3 to 46 with link named 'Depends': Cannot find imported issue key for external id '3'", log);
		StringAssert.assertContains("Unable to link issue from 46 to 33 with link named 'Depends': Cannot find imported issue key for external id '33'", log);
	}


}
