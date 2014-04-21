package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Attachment;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.AttachmentClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

/**
 * Func test for the attachment resource.
 *
 * @since v4.2
 */
@WebTest({ Category.FUNC_TEST, Category.REST })
public class TestAttachmentResource extends RestFuncTest
{
    private AttachmentClient attachmentClient;

    public void testViewAttachment() throws Exception
    {
        // {
        //   self: http://localhost:8090/jira/rest/api/2.0.alpha1/attachment/10000
        //   filename: attachment.txt
        //   author: {
        //     self: http://localhost:8090/jira/rest/api/2.0.alpha1/user?username=admin
        //     name: admin
        //     displayName: Administrator
        //   }
        //   created: 2010-06-09T15:59:34.602+1000
        //   size: 19
        //   mimeType: text/plain
        //   content: http://localhost:8090/jira/secure/attachment/10000/attachment.txt
        // }

        Attachment attachment1 = attachmentClient.get("10000");
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/attachment/10000", attachment1.self);
        assertEquals("attachment.txt", attachment1.filename);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", attachment1.author.self);
        assertEquals(ADMIN_USERNAME, attachment1.author.name);
        assertEquals(ADMIN_FULLNAME, attachment1.author.displayName);
        assertEqualDateStrings("2010-06-09T15:59:34.602+1000", attachment1.created);
        assertEquals(19L, attachment1.size);
        assertEquals("text/plain", attachment1.mimeType);
        assertEquals(getBaseUrl() + "/secure/attachment/10000/attachment.txt", attachment1.content);
    }

    public void testViewAttachmentNotFound() throws Exception
    {
        // {"errorMessages":["The attachment with id '123' does not exist"],"errors":[]}
        Response response123 = attachmentClient.getResponse("123");
        assertEquals(404, response123.statusCode);
        assertEquals(1, response123.entity.errorMessages.size());
        assertTrue(response123.entity.errorMessages.contains("The attachment with id '123' does not exist"));

        // {"errorMessages":["The attachment with id 'abc' does not exist"],"errors":[]}
        Response responseAbc = attachmentClient.getResponse("abc");
        assertEquals(404, responseAbc.statusCode);
        assertEquals(1, responseAbc.entity.errorMessages.size());
        assertTrue(responseAbc.entity.errorMessages.contains("The attachment with id 'abc' does not exist"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        attachmentClient = new AttachmentClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceAttachments.xml");
    }
}
