/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.pivotal;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.imports.importer.impl.DefaultJiraDataImporter;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.pivotal.PivotalImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.pivotal.PivotalProjectsMappingsPage;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.Version;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.webdriver.jira.JiraTestedProduct;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class TestPivotalImport extends FuncTestCase {
	private JiraTestedProduct product;
	private String username;
	private String password;
	private String sampleProject;
	private JiraRestClient restClient;
	private IssueRestClient issueClient;
	private ResourceBundle messages;
	private String existingStoryPointField;

	@Before
	public void setUpTest() {
		username = ITUtils.getProperties().getString("pivotal.username");
		password = ITUtils.getProperties().getString("pivotal.password");
		sampleProject = ITUtils.getProperties().getString("pivotal.sampleProject");
		restClient = ITUtils.createRestClient(environmentData);
		issueClient = restClient.getIssueClient();

		product = TestedProductFactory.create(JiraTestedProduct.class);
		//product.getTester().getDriver().getDriver().manage().setSpeed(Speed.FAST);
		administration.restoreBlankInstance();

		messages = ResourceBundle.getBundle("com.atlassian.jira.plugins.importer.web.action.util.messages");
		existingStoryPointField = null; // null or explicitly set
	}

	@Test
	public void testSampleProjectImport() throws Exception {
		setUpGHLikeCustomFields();
		setUpExistingUsers();
		importSampleProject();
		verifyCurrentState();
		verifyExistingUserReused();
		verifyImportingProjectAgain(); // run this before making any changes to the imported project
		verifyWorkflowNavigation();
		verifyIterations();
		verifyUpdateDatesForSubTasks();
	}

	private void verifyUpdateDatesForSubTasks() {
		// @todo this will fail, update the date, after Slawek fixes PT wizard
		Issue issue = restClient.getIssueClient().getIssue("SAMPLE-5", new NullProgressMonitor());
		assertEquals(new DateTime(2011, 03, 23, 7, 50, 10, 0, DateTimeZone.UTC).toInstant(),
				issue.getUpdateDate().toInstant());
	}

	private void setUpGHLikeCustomFields() throws Exception {
        navigation.gotoAdminSection("issue_types");
		tester.setFormElement("name", "Story");
		final String descr = "Simulated GH Story";
		tester.setFormElement("description", descr);
		tester.submit("Add");

		final XPathLocator locator = new XPathLocator(tester, String.format("//tr[td='%s']/td/ul/li/a[text() = 'Edit']", descr));
		if (!locator.exists())
		{
			Assert.fail("Could not find just created custom field");
		}
		final String href = locator.getNode().getAttributes().getNamedItem("href").getTextContent();
		URI uri = new URI(href);
		String id = StringUtils.removeStart(uri.getQuery(), "id=");

		existingStoryPointField = administration.customFields().addCustomField(
                CustomFieldConstants.NUMBER_FIELD_TYPE, "Story Points", new String[] {id},  new String[] {});
	}

	private void setUpExistingUsers() {
		administration.usersAndGroups().addUser("pawel niewiadomski", "pw", "Pawel Niewiadomski", "pn@address.none"); // deal with our user limit
		administration.usersAndGroups().addUser("ews", "pw", "Existing Wojciech Seliga", "wojciech.seliga@vp.pl");
	}

	public void testImportToIncompatibleProject() throws Exception {
		final PivotalProjectsMappingsPage projectMappingPage = product.gotoLoginPage()
				.loginAsSysAdmin(PivotalImporterSetupPage.class)
				.webSudo()
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
		projectMappingPage.createProject(sampleProject, "New Project Name", "NPN");
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
		Assert.assertEquals(0, logsPage.getGlobalErrors().size());
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

	private void verifyWorklog(Worklog worklog, String author, String startDate, double hrs, String comment)
			throws Exception {
		final Date started = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
		Assert.assertEquals(author, worklog.getAuthor().getDisplayName());
		Assert.assertEquals(started, worklog.getStartDate().toDate());
		Assert.assertEquals(60 * hrs, worklog.getMinutesSpent(), 0.001);
		Assert.assertEquals(comment, worklog.getComment());
	}

	void importSampleProject() {
		final PivotalImporterSetupPage setupPage = getSetupPage();
		Assert.assertEquals("1. Connect", setupPage.getActiveTabText());

		final PivotalProjectsMappingsPage projectMappingPage = setupPage.next();
		Assert.assertEquals("2. Mappings", setupPage.getActiveTabText());
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
		return product.gotoLoginPage()
				.loginAsSysAdmin(PivotalImporterSetupPage.class)
				.webSudo()
				.setUsername(username)
				.setPassword(password);
	}

	@SuppressWarnings({"ConstantConditions"}) // let it burn on NPE
	private void verifyCurrentState() throws IOException {
		final Issue issue = getIssueByStoryId("12249573");
		Assert.assertEquals("New Feature", issue.getIssueType().getName());
		Assert.assertEquals("Not Yet Started", issue.getStatus().getName());
		Assert.assertEquals("Test Member", issue.getAssignee().getDisplayName());
		Assert.assertEquals("Test Member", issue.getReporter().getDisplayName());

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

		final Iterable<Field> fields = issue.getFields();
		final ImmutableMap<String, Field> namedFields = Maps.uniqueIndex(fields, new Function<Field, String>() {
			@Override
			public String apply(Field input) {
				return input.getName();
			}
		});

		Assert.assertEquals("http://www.pivotaltracker.com/story/show/12249573", namedFields.get(
				DefaultJiraDataImporter.EXTERNAL_ISSUE_URL).getValue());
		Assert.assertEquals("12249573", namedFields.get("External issue ID").getValue());
		Assert.assertEquals(3d, namedFields.get("Story Points").getValue());

		if (existingStoryPointField != null) {
			Assert.assertEquals(existingStoryPointField, namedFields.get("Story Points").getId());
		}
	}

	private void verifyWorkflowNavigation() {
		navigation.login("admin");
		final String issueKey = navigation.issue().createIssue(sampleProject, null, "Test issue for workflow navigation");
		final Issue issue = issueClient.getIssue(issueKey, new NullProgressMonitor());
		Assert.assertEquals("Not Yet Started", issue.getStatus().getName());

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
		final PivotalProjectsMappingsPage projectMappingPage = product.gotoLoginPage()
				.loginAsSysAdmin(PivotalImporterSetupPage.class)
				.webSudo()
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
		for (Transition t : issueClient.getTransitions(issue.getTransitionsUri(), new NullProgressMonitor())) {
			if (t.getName().equals(transitionName)) {
				transition = t;
			}
		}
		Assert.assertNotNull(transition);
		final TransitionInput startInput = new TransitionInput(transition.getId());
		issueClient.transition(issue.getTransitionsUri(), startInput, new NullProgressMonitor());
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
		final String jql = String.format("'External Issue ID' = '%s'", storyId);
		return ITUtils.getIssuesByJql(restClient, jql);
	}

}
