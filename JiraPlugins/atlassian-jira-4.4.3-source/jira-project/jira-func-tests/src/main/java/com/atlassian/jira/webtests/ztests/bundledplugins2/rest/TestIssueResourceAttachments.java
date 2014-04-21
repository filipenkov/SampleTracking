package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Attachment;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;

/**
 * Func test case for issue resource attachments functionality.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceAttachments extends RestFuncTest
{
    private static final String ISSUE_KEY = "MKY-1";
    private IssueClient issueClient;

    public void testAttachmentsExpanded() throws Exception
    {
        Issue issue = issueClient.get(ISSUE_KEY);
        assertEquals(ISSUE_KEY, issue.key);
        assertEquals(2, issue.fields.attachment.value.size());

        // check only attachment 1:
        // {
        //     self: "http://localhost:8090/jira/rest/api/2.0.alpha1/attachment/10000",
        //     filename: "attachment.txt",
        //     author": {
        //       self: "http://localhost:8090/jira/rest/api/2.0.alpha1/user/admin",
        //       name: "admin",
        //       fullName: "Administrator"
        //     },
        //     created: "2010-06-09T15:59:34.602+1000",
        //     size: 19,
        //     mimeType: "text/plain",
        //     content: "http://localhost:8090/jira/secure/attachment/10000/attachment.txt"
        // }
        Attachment attachment1 = issue.fields.attachment.value.get(0);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/attachment/10000", attachment1.self);
        assertEquals("attachment.txt", attachment1.filename);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", attachment1.author.self);
        assertEquals(ADMIN_USERNAME, attachment1.author.name);
        assertEquals(ADMIN_FULLNAME, attachment1.author.displayName);
        assertEqualDateStrings("2010-06-09T15:59:34.602+1000", attachment1.created);
        assertEquals(19, attachment1.size);
        assertEquals("text/plain", attachment1.mimeType);
        assertEquals(getBaseUrl() + "/secure/attachment/10000/attachment.txt", attachment1.content);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceAttachments.xml");
    }
}
