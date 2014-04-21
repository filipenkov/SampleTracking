package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * A test case for issue trackbacks
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestIssueTrackback extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestIssueTrackback.xml");
        // enable trackbacks
        administration.enableTrackBacks();
    }

    public void testIssueDoesNotHaveTrackbackIfTrackbacksDisabled() throws Exception
    {
        assertIssueHasTrackback("HSP-1", "10000", "This should be able to be linked to via trackbacks", "This is the description for HSP-1", "homosapien");
        assertIssueHasMinimalTrackback("OTH-1", "10002", "admin should not be able to see this issue", "This is the description for OTH-1", "otherproject");

        // disable receiving trackbacks
        tester.gotoPage("/secure/admin/jira/TrackbackAdmin!default.jspa");
        tester.checkCheckbox("acceptPings", "false");
        tester.submit("Update");
        assertIssueHasNoTrackback("HSP-1");
        assertIssueHasNoMinimalTrackback("OTH-1");

        administration.enableTrackBacks();
        assertIssueHasTrackback("HSP-1", "10000", "This should be able to be linked to via trackbacks", "This is the description for HSP-1", "homosapien");
        assertIssueHasMinimalTrackback("OTH-1", "10002", "admin should not be able to see this issue", "This is the description for OTH-1", "otherproject");
    }

    public void testIssueHasTrackback() throws Exception
    {
        assertIssueHasTrackback("HSP-1", "10000", "This should be able to be linked to via trackbacks", "This is the description for HSP-1", "homosapien");
        assertIssueHasTrackback("HSP-2", "10001", "This also should have RDF comments", "This is the description for HSP-2", "homosapien");
    }

    public void testTrackbackDetailsAreEscaped()
    {
        // add a third issue that has -- in its text
        navigation.issue().goToCreateIssueForm(null,"New Feature");
        tester.setFormElement("summary", "This field has -- in its summary");
        tester.setFormElement("description", "This field has -- in its description");
        tester.submit("Create");

        assertIssueHasTrackback("HSP-3", "10010", "This field has &#45;&#45; in its summary", "This field has &#45;&#45; in its description", "homosapien");
    }

    public void testNoPermissionTrackback() throws Exception {
        // admin cant see OTH-1
        assertIssueHasMinimalTrackback("OTH-1", "10002", "admin should not be able to see this issue", "This is the description for OTH-1", "otherproject");

        // but testuser can
        navigation.login("testuser","testuser");
        assertIssueHasTrackback("OTH-1", "10002", "admin should not be able to see this issue", "This is the description for OTH-1", "otherproject");
    }

    private void assertIssueHasTrackback(String issueKey, String issueId, String issueSummary, String issueDesc, String projectName) {
        navigation.issue().viewIssue(issueKey);
        String pageText = tester.getDialog().getResponseText();

        text.assertTextPresent(pageText, "<rdf:RDF");
        text.assertTextPresent(pageText, "</rdf:RDF");
        text.assertTextPresent(pageText, "dc:title=\"[#" + issueKey + "] " + issueSummary + "\"");
        text.assertTextPresent(pageText, "dc:subject=\"" + projectName + "\"");
        text.assertTextPresent(pageText, "dc:description=\"" + issueDesc + "\"");

        // check that it has a canonical HTTP url in play
        text.assertRegexMatch(pageText, "<rdf:Description rdf:about=\"http://.*/browse/" + issueKey + "\"");
        text.assertRegexMatch(pageText, "dc:identifier=\"http://.*/browse/" + issueKey + "\"");
        text.assertRegexMatch(pageText, "trackback:ping=\"http://.*/rpc/trackback/"+ issueKey + "\"");

        text.assertRegexMatch(pageText, "<rdf:Description rdf:about=\"http://.*/secure/ViewIssue.jspa\\?id=" + issueId + "\"");
        text.assertRegexMatch(pageText, "dc:identifier=\"http://.*/secure/ViewIssue.jspa\\?id=" + issueId + "\"");

        text.assertRegexMatch(pageText, "<rdf:Description rdf:about=\"http://.*/secure/ViewIssue.jspa\\?key=" + issueKey + "\"");
        text.assertRegexMatch(pageText, "dc:identifier=\"http://.*/secure/ViewIssue.jspa\\?key=" + issueKey + "\"");
    }

    private void assertIssueHasNoTrackback(String issueKey) {
        navigation.issue().viewIssue(issueKey);
        String pageText = tester.getDialog().getResponseText();

        text.assertTextNotPresent(pageText, "<rdf:RDF");
        text.assertTextNotPresent(pageText, "</rdf:RDF");

        // check that it has a canonical HTTP url in play
        text.assertTextNotPresent(pageText, "<rdf:Description");
    }

    private void assertIssueHasMinimalTrackback(String issueKey, String issueId, String issueSummary, String issueDesc, String projectName) {
        navigation.issue().viewIssue(issueKey);
        String pageText = tester.getDialog().getResponseText();

        text.assertTextPresent(pageText, "<rdf:RDF");
        text.assertTextPresent(pageText, "</rdf:RDF");
        text.assertTextNotPresent(pageText, "dc:title=");
        text.assertTextNotPresent(pageText, "dc:subject=");
        text.assertTextNotPresent(pageText, "dc:description=");
        text.assertTextNotPresent(pageText, issueSummary);
        text.assertTextNotPresent(pageText, projectName);
        text.assertTextNotPresent(pageText, issueDesc);

        // check that it has a canonical HTTP url in play
        text.assertRegexMatch(pageText, "<rdf:Description rdf:about=\"http://.*/browse/" + issueKey + "\"");
        text.assertRegexMatch(pageText, "dc:identifier=\"http://.*/browse/" + issueKey + "\"");
        text.assertRegexMatch(pageText, "trackback:ping=\"http://.*/rpc/trackback/"+ issueKey + "\"");

        text.assertRegexMatch(pageText, "<rdf:Description rdf:about=\"http://.*/secure/ViewIssue.jspa\\?id=" + issueId + "\"");
        text.assertRegexMatch(pageText, "dc:identifier=\"http://.*/secure/ViewIssue.jspa\\?id=" + issueId + "\"");

        text.assertRegexMatch(pageText, "<rdf:Description rdf:about=\"http://.*/secure/ViewIssue.jspa\\?key=" + issueKey + "\"");
        text.assertRegexMatch(pageText, "dc:identifier=\"http://.*/secure/ViewIssue.jspa\\?key=" + issueKey + "\"");
    }

    private void assertIssueHasNoMinimalTrackback(String issueKey) {
        navigation.issue().viewIssue(issueKey);
        String pageText = tester.getDialog().getResponseText();

        text.assertTextNotPresent(pageText, "<rdf:RDF");
        text.assertTextNotPresent(pageText, "</rdf:RDF");

        // check that it has a canonical HTTP url in play
        text.assertTextNotPresent(pageText, "<rdf:Description");
    }

}
