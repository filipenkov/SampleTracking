package it.com.atlassian.jira.webtest.selenium.admin.imports.json;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.plugins.importer.backdoor.SampleDataBackdoorControl;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.IssueLink;
import com.atlassian.jira.rest.client.domain.Subtask;
import com.google.common.collect.Iterables;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static it.com.atlassian.jira.webtest.selenium.admin.imports.json.TestSampleData.getJsonResource;

public class TestSampleDataFunc extends FuncTestCase {

    private SampleDataBackdoorControl control;
    private JiraRestClient rest;

    @Override
    protected void setUpTest() {
        super.setUpTest();

        backdoor.restoreBlankInstance();
        control = new SampleDataBackdoorControl(environmentData);
        rest = ITUtils.createRestClient(environmentData);
    }


    public void testImportLinks() throws IOException {
        ITUtils.enableSubtasks(administration);
        administration.subtasks().addSubTaskType("Subtask", null);
        administration.issueLinking().enable();
        administration.issueLinking().addIssueLink("Duplicate", "duplicates", "is duplicate of");

        control.importSampleData(FileUtils.readFileToString(new File(getJsonResource("links.json"))));

        {
            Issue i = rest.getIssueClient().getIssue("SAM-1", new NullProgressMonitor());
            assertEquals("Parent case", i.getSummary());

            final Subtask subtask = Iterables.get(i.getSubtasks(), 0, null);
            assertNotNull(subtask);
            assertEquals("SAM-2", subtask.getIssueKey());
            assertEquals("Open", subtask.getStatus().getName());
            assertEquals("Subtask", subtask.getIssueType().getName());

            assertTrue(Iterables.isEmpty(i.getIssueLinks()));
        }

        {
            Issue i = rest.getIssueClient().getIssue("SAM-2", new NullProgressMonitor());
            assertEquals("Sub-task", i.getSummary());

            IssueLink link = Iterables.get(i.getIssueLinks(), 0, null);
            assertEquals("Duplicate", link.getIssueLinkType().getName());
            assertEquals("SAM-3", link.getTargetIssueKey());
            assertEquals("is duplicate of", link.getIssueLinkType().getDescription());
        }
    }
}
