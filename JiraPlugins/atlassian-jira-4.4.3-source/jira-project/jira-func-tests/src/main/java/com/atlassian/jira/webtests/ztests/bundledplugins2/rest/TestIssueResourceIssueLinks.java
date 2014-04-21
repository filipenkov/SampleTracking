package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueLink;

import java.io.IOException;
import java.util.List;

/**
 * Functional tests for REST issue linking (JRADEV-1657).
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceIssueLinks extends RestFuncTest
{
    private IssueClient issueClient;

    public void testIssueLinksDisabled() throws Exception
    {
        restoreData(false);
        Issue issue = issueClient.get("LNK-4");
        assertNull(issue.fields.links);
    }

    // LNK-5 has no links
    public void testNoIssueLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.get("LNK-5");
        assertEquals("Object should have no issue links", 0, issue.fields.links.value.size());
    }

    // LNK-1 has inward links, but no outward links
    public void testNoOutwardIssueLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.get("LNK-1");
        List<IssueLink> links = issue.fields.links.value;
        for (IssueLink link : links)
        {
            assertEquals("INBOUND", link.type.direction);
        }
    }

    // LNK-2 has a link to another issue that's not visible to user

    public void testNoVisibleLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.loginAs("reporter").get("LNK-2");
        assertEquals("Object should have no visible issue links", 0, issue.fields.links.value.size());
    }
    // LNK-4 has 1 visible and 1 invisible link

    public void testInvisibleIssueNotShown() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.loginAs("reporter").get("LNK-4");
        List<IssueLink> links = issue.fields.links.value;
        assertEquals(1, links.size());

        // only LNK-1 should be visible to reporter
        assertEquals("LNK-1", links.get(0).issueKey);
    }
    // LNK-4 has several issue links

    public void testSeveralIssueLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.get("LNK-4");
        List<IssueLink> links = issue.fields.links.value;

        // LNK-1 and LNK-3 should both be visible to admin
        assertEquals(2, links.size());
        if ("LNK-1".equals(links.get(0).issueKey) && "LNK-3".equals(links.get(1).issueKey))
        {
            return;
        }
        if ("LNK-3".equals(links.get(0).issueKey) && "LNK-1".equals(links.get(1).issueKey))
        {
            return;
        }
        fail("Issue links LNK-1 and LNK-3 should both be visible");
    }
    // tests that link metadata returned

    public void testIssueLinkMetadataPresent() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.loginAs("reporter").get("LNK-6");
        List<IssueLink> links = issue.fields.links.value;

        assertEquals(1, links.size());
        IssueLink linktoLnk1 = links.get(0);

        final String baseUrl = getEnvironmentData().getBaseUrl().toExternalForm();

        // expected output:
        //
        //{
        //  "key":"LNK-1",
        //  "self": "http://localhost:8090/jira/rest/api/2.0.alpha1/issue/LNK-1"
        //  }
        //  
        //}

        assertEquals("LNK-1", linktoLnk1.issueKey);
        assertEquals(baseUrl + "/rest/api/2.0.alpha1/issue/LNK-1", linktoLnk1.issue);
        assertEquals("Duplicate", linktoLnk1.type.name);
        assertEquals("OUTBOUND", linktoLnk1.type.direction);
        assertEquals("duplicates", linktoLnk1.type.description);
    }

    /**
     * Setup for an actual test
     */
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    private void restoreData(boolean issueLinkingEnabled) throws IOException
    {
        administration.restoreData("TestIssueResourceIssueLinks.xml");
        if (!issueLinkingEnabled)
        {
            administration.issueLinking().disable();
        }
    }
}
