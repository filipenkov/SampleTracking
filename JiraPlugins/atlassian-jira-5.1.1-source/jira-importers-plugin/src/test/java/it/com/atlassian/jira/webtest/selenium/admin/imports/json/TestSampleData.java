/*
 * Copyright (C) 2012 Atlassian
 * All rights reserved.
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.json;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.DateTimeMatcher;
import com.atlassian.jira.plugins.importer.backdoor.SampleDataBackdoorControl;
import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.ChangelogGroup;
import com.atlassian.jira.rest.client.domain.ChangelogItem;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Worklog;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.io.FileUtils;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestSampleData extends BaseJiraWebTest {

    private SampleDataBackdoorControl control;
    private JiraRestClient rest;

    public static String getJsonResource(String resource) {
        return ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/sample/" + resource;
    }

    @Before
    public void setUp() {
        control = new SampleDataBackdoorControl(jira.environmentData());
        backdoor.restoreBlankInstance();
        rest = ITUtils.createRestClient(jira.environmentData());
    }

    @Test
    public void importSimple() throws IOException {
        control.importSampleData(FileUtils.readFileToString(new File(getJsonResource("simple.json"))));

        {
            Issue i = rest.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
            assertEquals("Closed", i.getStatus().getName());
            assertEquals("Resolved", i.getResolution().getName());
        }
    }

    @Test
    public void importComments() throws IOException {
        control.importSampleData(FileUtils.readFileToString(new File(getJsonResource("comments.json"))));

        {
            Issue i = rest.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
            Iterable<Comment> comments = i.getComments();
            assertEquals(2, Iterables.size(comments));
        }
    }

    @Test
    public void importWorklog() throws IOException {
        backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_TIMETRACKING, true);
        control.importSampleData(FileUtils.readFileToString(new File(getJsonResource("worklogs.json"))));

        {
            Issue i = rest.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
            Iterable<Worklog> worklogs = i.getWorklogs();
            assertEquals(2, Iterables.size(worklogs));
            assertEquals(1, Iterables.get(worklogs, 0, null).getMinutesSpent());
            assertEquals(180, Iterables.get(worklogs, 1, null).getMinutesSpent());
            assertEquals(Integer.valueOf(14400), i.getTimeTracking().getOriginalEstimateMinutes());
            assertEquals(Integer.valueOf(421), i.getTimeTracking().getTimeSpentMinutes());
            assertEquals(Integer.valueOf(2880), i.getTimeTracking().getRemainingEstimateMinutes());
        }
    }

	@Test
	public void importHistory() throws Exception {
		control.importSampleData(FileUtils.readFileToString(new File(getJsonResource("history.json"))));

		final Issue issue = rest.getIssueClient().getIssue("SAM-1", EnumSet.of(IssueRestClient.Expandos.CHANGELOG), new NullProgressMonitor());
		assertEquals("My chore", issue.getSummary());

		final Iterable<ChangelogGroup> changelog = issue.getChangelog();
		assertNotNull(changelog);
		assertEquals(1, Iterables.size(changelog));

		final ChangelogGroup changelogGroup = Iterables.getOnlyElement(changelog);
		assertEquals("wseliga", changelogGroup.getAuthor().getName());
		Assert.assertThat(changelogGroup.getCreated(), DateTimeMatcher.ago(Period.days(1), 10)); // longer tolerance for lengthy imports
		final ImmutableList<ChangelogItem> expectedItems = ImmutableList.of(
				new ChangelogItem(ChangelogItem.FieldType.JIRA, "status", "1", "Open", "5", "Resolved")
		);
		assertEquals(expectedItems, changelogGroup.getItems());


	}
}
