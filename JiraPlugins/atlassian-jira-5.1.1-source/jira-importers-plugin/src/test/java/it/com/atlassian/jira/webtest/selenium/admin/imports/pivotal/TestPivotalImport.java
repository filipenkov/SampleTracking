/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.pivotal;

import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.backdoor.IssueTypeControl;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.atlassian.jira.plugins.importer.po.common.AddProjectDialog;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.pivotal.*;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.*;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ScreenshotFuncTestCase;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junitx.framework.StringAssert.assertContains;

public class TestPivotalImport extends ScreenshotFuncTestCase {
	private String username;
	private String password;
	private String sampleProject;
	private JiraRestClient restClient;
	private IssueRestClient issueClient;
	private ResourceBundle messages;
	private String existingStoryPointField;

	public void setUpTest() {
        super.setUpTest();
        
		username = ITUtils.getProperties().getString("pivotal.username");
		password = ITUtils.getProperties().getString("pivotal.password");
		sampleProject = ITUtils.getProperties().getString("pivotal.sampleProject");
		restClient = ITUtils.createRestClient(environmentData);
		issueClient = restClient.getIssueClient();

		administration.restoreBlankInstance();
        administration.fieldConfigurations().fieldConfiguration("Default Field Configuration").getScreens("Story Points").addFieldToScreen("Default Screen");

		messages = ResourceBundle.getBundle("com.atlassian.jira.plugins.importer.web.action.util.messages");
		existingStoryPointField = null; // null or explicitly set
	}

	@Test
	public void testSampleProjectImport() throws Exception {
		setUpGHLikeCustomFields();
		setUpExistingUsers();
		importSampleProject();

		ImporterFinishedPage finishedPage = jira.visit(ImporterFinishedPage.class, "com.atlassian.jira.plugins.jira-importers-plugin:pivotalTrackerImporter");
		//assertTrue(finishedPage.isImportAgainVisible());

		verifyCurrentState();
		verifyExistingUserReused();
		verifyImportingProjectAgain(); // run this before making any changes to the imported project
		verifyWorkflowNavigation();
		verifyIterations();
		verifyUpdateDatesForSubTasks();
	}

	/**
	 * If you change ranking manually between executions of this tests it will not be cleared and this test will fail.
	 * In this case you need to clean JIRA instance and try again.
	 * @throws Exception
	 */
	@Test
	public void testImportWithGreenHopper() throws Exception {
        // time bomb license from https://developer.atlassian.com/display/UPM/Timebomb+Licenses+for+Testing
		assertEquals("Valid", jira.gotoLoginPage()
                .loginAsSysAdmin(GHLicensePage.class)
                .setLicense(
                        "AAABpA0ODAoPeNp9U11P2zAUfc+vsLQ3pFRxB9paKQ+08UY3aKOSAkJ7cd3b1sy1o+uk0H+P5xDhR\n" +
                        "IzX43PPOffDX26MJgtREZoQOhyffx8nI5JnBRkmlEY7BNB7U5aAg2spQFtgG1lJo1M2L9gyX85uW\n" +
                        "bTmh7UxnxC20u7hBC2jOJWQ/nAYO8GfMcngCMo4i0goc3x38rSphzqseX1YAy62Kwto04uoVPVOa\n" +
                        "js4G3BRySOkFdYQCaO3HSCvUey5hYxXkA4ppXEyium3KHCb8wOkGbtj14ucLdsX9lJKPPmy/OtV2\n" +
                        "20ofQvoQs6ydPJzVMQPq7vz+Pfj41U8Seh99CSRd1r6NVteEqYrwBKl7U3gX+pO/w5QNWjxAa8dx\n" +
                        "FTV1qnNzQZsmvTW4VUmHvqfaZjwg+0JrIVcq+76pm9gR+iGS+eguUvbG5pA87zpCTikU/326sa40\n" +
                        "n+1edbRAndcS8t9ostKcWsl1++Bwh1METyvv97GOWS21xhiGViBsvRGBdiKqCYM2RokzYGRTZvUd\n" +
                        "v5FKOMnGQLsyFXd5G+usrnwkPIKi15AZTAsAhRNXNoDT5n8OQAYwm8pA7TwPhPMTgIUNl+tH3CnC\n" +
                        "dVz6gON9AZUeMFjWSQ=X02k8")
                .update()
                .getLicenseStatus());

		setUpExistingUsers();
		importSampleProject();
		ImporterFinishedPage finishedPage = jira.visit(ImporterFinishedPage.class, "com.atlassian.jira.plugins.jira-importers-plugin:pivotalTrackerImporter");
		assertFalse(finishedPage.isImportAgainVisible());
		final Map.Entry<String, String> link = Iterables.getOnlyElement(finishedPage.getRapidBoardLinks().entrySet());
		assertEquals("My Test Project", link.getKey());
		assertContains("/jira/secure/RapidBoard.jspa?rapidView=", link.getValue());

		verifyCurrentState();
		verifyExistingUserReused();

		final RapidBoardPage rapidBoardPage = finishedPage.gotoRapidBoard("My Test Project");
		final LinkedHashMap<String, String> columns = rapidBoardPage.getColumnHeaders();

		assertEquals(ImmutableList.of("Icebox", "Backlog", "Current", "Finished", "Delivered", "Done"),
				ImmutableList.copyOf(columns.keySet()));

		// TODO JIM-621
		assertEquals(ImmutableSet.of("SAMPLE-15", "SAMPLE-13", "SAMPLE-14", "SAMPLE-18",
				"SAMPLE-19", "SAMPLE-20", "SAMPLE-21", "SAMPLE-22", "SAMPLE-23", "SAMPLE-24"),
				ImmutableSet.copyOf(rapidBoardPage.getIssuesForColumnInFirstSwimlane(columns.get("Icebox"))));

		assertEquals(ImmutableSet.of("SAMPLE-7", "SAMPLE-12", "SAMPLE-10"),
				ImmutableSet.copyOf(rapidBoardPage.getIssuesForColumnInFirstSwimlane(columns.get("Current"))));

//		assertEquals(ImmutableSet.of("SAMPLE-15", "SAMPLE-18", "SAMPLE-19", "SAMPLE-20", "SAMPLE-21", "SAMPLE-22", "SAMPLE-23", "SAMPLE-24"),
//				ImmutableSet.copyOf(rapidBoardPage.getIssuesForColumnInFirstSwimlane(columns.get("Icebox"))));

		assertTrue(rapidBoardPage.getIssuesForColumnInFirstSwimlane(columns.get("Backlog")).isEmpty());
		assertEquals(ImmutableSet.of("SAMPLE-11"),
				ImmutableSet.copyOf(rapidBoardPage.getIssuesForColumnInFirstSwimlane(columns.get("Finished"))));
		assertTrue(rapidBoardPage.getIssuesForColumnInFirstSwimlane(columns.get("Delivered")).isEmpty());
		assertTrue(rapidBoardPage.getIssuesForColumnInFirstSwimlane(columns.get("Done")).isEmpty());
	}

	private void verifyUpdateDatesForSubTasks() {
		// @todo this will fail, update the date, after Slawek fixes PT wizard
		Issue issue = restClient.getIssueClient().getIssue("SAMPLE-5", new NullProgressMonitor());
		assertEquals(new DateTime(2011, 03, 23, 7, 50, 10, 0, DateTimeZone.UTC).toInstant(),
				issue.getUpdateDate().toInstant());
	}

	private void setUpGHLikeCustomFields() throws Exception {
        final List<IssueTypeControl.IssueType> issueTypes = backdoor.issueType().getIssueTypes();
        IssueTypeControl.IssueType issueType = Iterables.getFirst(Iterables.filter(issueTypes, new Predicate<IssueTypeControl.IssueType>() {
            @Override
            public boolean apply(@Nullable IssueTypeControl.IssueType issueType) {
                return "Story".equals(issueType.getName());
            }
        }), null);

        if (issueType == null) {
            issueType = backdoor.issueType().createIssueType("Story");
        }

        navigation.gotoCustomFields();
        final XPathLocator cfLocator = new XPathLocator(tester, String.format("//tr/td[strong='%s']", "Story Points"));
        if (cfLocator.exists()) {
            existingStoryPointField = StringUtils.removeEnd(StringUtils.removeStart(cfLocator.getNode().getAttributes().getNamedItem("id").getNodeValue(), "custom-fields-"), "-name");
        } else {
            existingStoryPointField = administration.customFields().addCustomField(
                    CustomFieldConstants.NUMBER_FIELD_TYPE, "Story Points", new String[] {issueType.getId()},  new String[] {});
        }
	}

	private void setUpExistingUsers() {
		administration.usersAndGroups().addUser("pawel niewiadomski", "pw", "Pawel Niewiadomski", "pn@address.none"); // deal with our user limit
		administration.usersAndGroups().addUser("ews", "pw", "Existing Wojciech Seliga", "wojciech.seliga@vp.pl");
	}

	public void testImportToIncompatibleProject() throws Exception {
		PivotalProjectsMappingsPage projectMappingPage = jira.gotoLoginPage()
				.loginAsSysAdmin(PivotalImporterSetupPage.class)
                .setUsername(username)
				.setPassword(password)
				.next();
		projectMappingPage.setImportAllProjects(false);
		projectMappingPage.setProjectImported(sampleProject, true);
		Assert.assertFalse("homosapien should be impossible to set as project mapping", projectMappingPage.setProject(sampleProject, "homosapien"));
		Assert.assertTrue(projectMappingPage.hasError(sampleProject));
		final String jsError = "The value homosapien is invalid.";
		Assert.assertEquals(jsError, projectMappingPage.getError(sampleProject));
		Assert.assertFalse(projectMappingPage.isNextEnabled());

		projectMappingPage.setProjectImported(sampleProject, true);
		projectMappingPage.setProject(sampleProject, "");
		// trick JIM to allow project name that is not actually valid
		projectMappingPage = projectMappingPage.createProject(sampleProject, "New Project Name", "NPN");
		administration.project().addProject("New Project Name", "NPN", "admin");

		projectMappingPage.nextWithError();
		final String errorMessage = messages.getString("jira-importer-plugin.pivotal.incompatibleSchema");
		Assert.assertEquals(errorMessage, projectMappingPage.getActionErrorMessage(sampleProject));
	}

	public void testImportTimeTracking() throws Exception {
		administration.timeTracking().enable(TimeTracking.Mode.MODERN);
		final String project = ITUtils.getProperties().getString("pivotal.timeTrackingProject");
		final PivotalProjectsMappingsPage projectMapping = getSetupPage().next();
		projectMapping.setImportAllProjects(false).setProjectImported(project, true);
		projectMapping.createProject(project, project, "TIMETRACKING");

		final ImporterFinishedPage logsPage = projectMapping.beginImport().waitUntilFinished();
		Assert.assertEquals("Unexpected errors: " + StringUtils.join(logsPage.getGlobalErrors(), "\n"), 0, logsPage.getGlobalErrors().size());
		Assert.assertEquals("2", logsPage.getIssuesImported());

		final Issue issue = Iterables.getOnlyElement(ITUtils.getIssuesByJql(restClient, "'External Issue ID' is empty"));
		final Iterable<Worklog> worklogs = issue.getWorklogs();
		Assert.assertEquals(2, Iterables.size(worklogs));

		verifyWorklog(Iterables.get(worklogs, 0), "Test Member", "2011-04-21", 8, "");
		verifyWorklog(Iterables.get(worklogs, 1), "Test Member", "2011-04-22", 6, "six hours");
	}

	public void testImportWithoutTimeTracking() throws Exception {
		administration.timeTracking().disable();
		final String project = ITUtils.getProperties().getString("pivotal.timeTrackingProject");
		final PivotalProjectsMappingsPage projectMapping = getSetupPage().next();
		projectMapping.setImportAllProjects(false).setProjectImported(project, true);
		projectMapping.createProject(project, project, "TIMETRACKING");

		final ImporterFinishedPage logsPage = projectMapping.beginImport().waitUntilFinished();
		Assert.assertEquals(0, logsPage.getGlobalErrors().size());
		Assert.assertEquals("1", logsPage.getIssuesImported());

		Assert.assertEquals(Collections.<Issue>emptyList(), ITUtils.getIssuesByJql(restClient, "'External Issue ID' is empty"));

	}

	public void testImportWithoutGHFields() throws Exception {
		setUpExistingUsers();
		importSampleProject();
		verifyCurrentState();
	}

	public void testImportWithUsernameMapping() throws Exception {
		administration.timeTracking().enable(TimeTracking.Mode.MODERN);
		administration.attachments().enable();
		final PivotalImporterSetupPage setupPage = getSetupPage();
		Assert.assertEquals("1. Connect", setupPage.getActiveTabText());

		setupPage.showAdvanced().setMapUsernames(true).setConfigFile(
				ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/pivotal/usermapping.config");
		final PivotalProjectsMappingsPage projectMappingPage = setupPage.next();
		Assert.assertEquals("2. Project Mapping", setupPage.getActiveTabText());

		final PivotalUserMappingsPage pivotalUserMappingsPage = projectMappingPage.nextToUserMapping();
		Assert.assertEquals("3. User Mapping", pivotalUserMappingsPage.getActiveTabText());

		final ImporterFinishedPage importerLogsPage = pivotalUserMappingsPage.nextToLogs().waitUntilFinished();
		Assert.assertFalse("JRADEV-7692 seems fixed, time to revert this test",
				importerLogsPage.getLog().contains("org.ofbiz.core.entity.GenericEntity.getLong"));

		Assert.assertTrue("Problems: " + StringUtils.join(importerLogsPage.getGlobalErrors(), "\n"), importerLogsPage.isSuccess());
		Assert.assertEquals(0, importerLogsPage.getGlobalErrors().size());
		Assert.assertEquals("2", importerLogsPage.getProjectsImported());

		final Project project = restClient.getProjectClient().getProject("MUT", new NullProgressMonitor());
		Assert.assertEquals("mapped wseliga", project.getLead().getName());

		final Issue issue = getIssueByStoryId("9972441");
		Assert.assertEquals("mapped wseliga", issue.getReporter().getName());
		Assert.assertEquals("mapped test member", issue.getAssignee().getName()); // name expected to be lowercase
		Assert.assertEquals("mapped Test Member", issue.getAssignee().getDisplayName()); // displayName is verbatim from config
		final BasicUser commentAuthor = issue.getComments().iterator().next().getAuthor();
		Assert.assertEquals("mapped wseliga", commentAuthor.getName());

        final Attachment attachment = Iterables.getOnlyElement(issue.getAttachments());
		Assert.assertEquals("mapped wseliga", attachment.getAuthor().getName());
        Assert.assertEquals(51636, attachment.getSize());
        Assert.assertEquals("1.png", attachment.getFilename());

		final Issue timelogIssue = Iterables.getOnlyElement(ITUtils.getIssuesByJql(restClient, "project = MTT and 'External Issue ID' is empty"));
		final Iterable<Worklog> worklogs = timelogIssue.getWorklogs();
		Assert.assertEquals(2, Iterables.size(worklogs));
		verifyWorklog(Iterables.get(worklogs, 0), "mapped Test Member", "2011-04-21", 8, "");
		verifyWorklog(Iterables.get(worklogs, 1), "mapped Test Member", "2011-04-22", 6, "six hours");

	}

	@Test
	public void testReenteringProjectName() throws Exception {
		PivotalProjectsMappingsPage projectMappingPage = getSetupPage().next();
		projectMappingPage.setImportAllProjects(false);
		projectMappingPage.setProjectImported(sampleProject, true);
		final String projectKey = "SSS";
		final String projectName = "Whatever";
		projectMappingPage = projectMappingPage.createProject(sampleProject, projectName, projectKey);
		// now let see if my Key and Name are remembered
		final AddProjectDialog addProjectDialog = projectMappingPage.openProjectEdit(sampleProject);
		assertEquals(projectKey, addProjectDialog.getKey());
		assertEquals(projectName, addProjectDialog.getName());
		// and now see if I change just the key it is still remembered
		addProjectDialog.setKey("NK");

        final PivotalProjectsMappingsPage projectMappingPage2 = addProjectDialog.submitSuccess(PivotalProjectsMappingsPage.class);
		final AddProjectDialog addProjectDialog2 = projectMappingPage2.openProjectEdit(sampleProject);
		assertEquals("NK", addProjectDialog.getKey());
		assertEquals(projectName, addProjectDialog.getName());
	}


	private void verifyWorklog(Worklog worklog, String author, String startDate, double hrs, String comment)
			throws Exception {
		final Date started = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
		Assert.assertEquals(StringUtils.lowerCase(author), worklog.getAuthor().getName()); // name expected to be lowercase
		Assert.assertEquals(author, worklog.getAuthor().getDisplayName());
		Assert.assertEquals(started, worklog.getStartDate().toDate());
		Assert.assertEquals(60 * hrs, worklog.getMinutesSpent(), 0.001);
		Assert.assertEquals(comment, worklog.getComment());
	}

	void importSampleProject() {
		final PivotalImporterSetupPage setupPage = getSetupPage();
		Assert.assertEquals("1. Connect", setupPage.getActiveTabText());

		final PivotalProjectsMappingsPage projectMappingPage = setupPage.next();
		Assert.assertEquals("2. Project Mapping", setupPage.getActiveTabText());
		Assert.assertTrue("Expecting all project checkboxes to be enabled", projectMappingPage.areAllProjectsSelected());
		projectMappingPage.setImportAllProjects(false);
		projectMappingPage.setProjectImported(sampleProject, true);
		projectMappingPage.createProject(sampleProject, sampleProject, "SAMPLE");

		final ImporterFinishedPage importerLogsPage = projectMappingPage.beginImport().waitUntilFinished();
		Assert.assertTrue(importerLogsPage.isSuccess());
		Assert.assertEquals(0, importerLogsPage.getGlobalErrors().size());
		Assert.assertEquals("1", importerLogsPage.getProjectsImported());
		// todo see JIM-526
		//Assert.assertTrue(Integer.parseInt(importerLogsPage.getIssuesImported()) > 20);
	}

	private PivotalImporterSetupPage getSetupPage() {
		return jira.gotoLoginPage()
				.loginAsSysAdmin(PivotalImporterSetupPage.class)
				.setUsername(username)
				.setPassword(password);
	}

	@SuppressWarnings({"ConstantConditions"}) // let it burn on NPE
	private void verifyCurrentState() throws IOException, JSONException {
        final Issue issue = getIssueByStoryId("12249573");
		Assert.assertEquals("New Feature", issue.getIssueType().getName());
		Assert.assertEquals("IceBox", issue.getStatus().getName());
		Assert.assertEquals("Test Member", issue.getAssignee().getDisplayName());
		Assert.assertEquals("Test Member", issue.getReporter().getDisplayName());

		final Set<String> labels = issue.getLabels();
		Assert.assertNotNull(labels);
		Assert.assertEquals("impossible", Iterables.getOnlyElement(labels));

		final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withZone(DateTimeZone.UTC);
		Assert.assertEquals(formatter.parseDateTime("2011/04/13 13:55:12").toInstant(), issue.getCreationDate().toInstant());

		final List<Issue> subtasks = ITUtils.getIssuesByJql(restClient, "parent = " + issue.getKey());
		Assert.assertEquals(2, subtasks.size());

		for (Issue subtask : subtasks) {
			Assert.assertEquals("Sub-task", subtask.getIssueType().getName());
			Assert.assertEquals("Test Member", subtask.getAssignee().getDisplayName());
			Assert.assertEquals("Test Member", subtask.getReporter().getDisplayName());

			final String summary = subtask.getSummary();
			if (summary.equals("Unfinished Task")) {
				Assert.assertEquals("Open", subtask.getStatus().getName());
			} else if (summary.equals("Finished Task")) {
				Assert.assertEquals("Closed", subtask.getStatus().getName());
			} else {
				fail("Unexpected subtask summary: " + summary);
			}
		}

		Assert.assertEquals("http://www.pivotaltracker.com/story/show/12249573", issue.getFieldByName(DefaultJiraDataImporter.EXTERNAL_ISSUE_URL).getValue());
		Assert.assertEquals("12249573", issue.getFieldByName("External issue ID").getValue());
        final Field storyPoints = issue.getFieldByName("Story Points");
		Assert.assertEquals(3d, storyPoints.getValue());

		if (existingStoryPointField != null) {
			Assert.assertEquals(existingStoryPointField, storyPoints.getId());
		}
	}

	private void verifyWorkflowNavigation() {
		navigation.login("admin");
		final String issueKey = navigation.issue().createIssue(sampleProject, null, "Test issue for workflow navigation");
		final Issue issue = issueClient.getIssue(issueKey, new NullProgressMonitor());
		Assert.assertEquals("IceBox", issue.getStatus().getName());

		testSingleTransition(issueKey, "Start", "Started", null);
		testSingleTransition(issueKey, "Stop", "Not Yet Started", null);

		testSingleTransition(issueKey, "Start", "Started", null);
		testSingleTransition(issueKey, "Finish", "Finished", null);
		testSingleTransition(issueKey, "Restart", "Started", null);

		testSingleTransition(issueKey, "Finish", "Finished", null);
		testSingleTransition(issueKey, "Deliver", "Delivered", null);
		testSingleTransition(issueKey, "Reject", "Rejected", null);
		testSingleTransition(issueKey, "Restart", "Started", null);

		testSingleTransition(issueKey, "Finish", "Finished", null);
		testSingleTransition(issueKey, "Deliver", "Delivered", null);
		testSingleTransition(issueKey, "Reject", "Rejected", null);
		testSingleTransition(issueKey, "Stop", "Not Yet Started", null);

		testSingleTransition(issueKey, "Start", "Started", null);
		testSingleTransition(issueKey, "Finish", "Finished", null);
		testSingleTransition(issueKey, "Deliver", "Delivered", null);
		testSingleTransition(issueKey, "Accept", "Accepted", "Fixed");
		testSingleTransition(issueKey, "Restart", "Started", null);

		testSingleTransition(issueKey, "Finish", "Finished", null);
		testSingleTransition(issueKey, "Deliver", "Delivered", null);
		testSingleTransition(issueKey, "Reject", "Rejected", null);
		testSingleTransition(issueKey, "Stop", "Not Yet Started", null);


		final String subtaskKey = navigation.issue().createSubTask(issueKey, null, "Subtask summary", null);
		final Issue subtask = issueClient.getIssue(subtaskKey, new NullProgressMonitor());
		Assert.assertEquals("Open", subtask.getStatus().getName());

		testSingleTransition(subtaskKey, "Close", "Closed", "Fixed");
		testSingleTransition(subtaskKey, "Reopen", "Open", null);
	}

	private void verifyImportingProjectAgain() throws IOException {
		final PivotalProjectsMappingsPage projectMappingPage = jira.gotoLoginPage()
				.loginAsSysAdmin(PivotalImporterSetupPage.class)
				.setUsername(username)
				.setPassword(password)
				.next();
		projectMappingPage.setImportAllProjects(false);
		projectMappingPage.setProjectImported(sampleProject, true);
		projectMappingPage.setProject(sampleProject, sampleProject);
		final int existingIssueCount = ITUtils.getIssuesByJql(restClient, "project = SAMPLE").size();

		final ImporterFinishedPage importerLogsPage = projectMappingPage.beginImport().waitUntilFinished();
		Assert.assertEquals("17 of 17 issues have been skipped because they already exist in destination projects.",
				Iterables.getFirst(importerLogsPage.getWarnings(), null));

		final int newIssueCount = ITUtils.getIssuesByJql(restClient, "project = SAMPLE").size();
		Assert.assertTrue(newIssueCount == existingIssueCount);
		final List<Issue> duplicates = getIssuesByStoryId("12249573");
		Assert.assertEquals(1, duplicates.size());
	}

	private void verifyIterations() {
		final Iterable<Version> versions = restClient.getProjectClient().getProject("SAMPLE", new NullProgressMonitor()).getVersions();
		for (Version version : versions) {
			final DateTime releaseDate = version.getReleaseDate();
			Assert.assertNotNull(releaseDate);
			final boolean inThePast = releaseDate.isBeforeNow();
			Assert.assertEquals("Revision " + version.getName(), inThePast, version.isReleased());
		}
	}

	private void verifyExistingUserReused() throws IOException {
		final Issue ownedByExisting = getIssueByStoryId("9974265");
		Assert.assertEquals("ews", ownedByExisting.getAssignee().getName());
	}

	private void testSingleTransition(String issueKey, final String transitionName, final String expectedState, @Nullable String expectedResolution) {
		final Issue issue = issueClient.getIssue(issueKey, new NullProgressMonitor());
		Transition transition = null;
		for (Transition t : issueClient.getTransitions(issue, new NullProgressMonitor())) {
			if (t.getName().equals(transitionName)) {
				transition = t;
			}
		}
		Assert.assertNotNull(transition);
		final TransitionInput startInput = new TransitionInput(transition.getId());
		issueClient.transition(issue, startInput, new NullProgressMonitor());
		final Issue afterTransition = issueClient.getIssue(issueKey, new NullProgressMonitor());
		Assert.assertEquals(expectedState, afterTransition.getStatus().getName());
		if (expectedResolution == null) {
			Assert.assertNull(afterTransition.getResolution());
		} else {
			Assert.assertEquals(expectedResolution, afterTransition.getResolution().getName());
		}
	}

	private Issue getIssueByStoryId(String storyId) throws IOException {
		return Iterables.getOnlyElement(getIssuesByStoryId(storyId));
	}

	private List<Issue> getIssuesByStoryId(String storyId) throws IOException {
		final String jql = String.format("'External Issue ID' ~ '%s'", storyId);
		return ITUtils.getIssuesByJql(restClient, jql);
	}

}
