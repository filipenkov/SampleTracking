package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Test inline editing of fields from the view issue page.
 * <p/>
 * Note: this test should ideally be moved to the issue-nav plugin. The problem is that the build plan for the issue-nav
 * plugin master doesn't compile against jira master.
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestInlineEditIssueFields extends RestFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestEditIssueVersion.xml");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testInlineEditIssueType() throws Exception
    {
        testInlineEditField("issuetype", "type-val", "Bug", "4", "Improvement");
    }

    public void testInlineEditPriority() throws Exception
    {
        testInlineEditField("priority", "priority-val", "Major", "4", "Minor");
    }

    public void testInlineEditDescription() throws Exception
    {
        testInlineEditField("description", "description-val", "oneoneoneoneoneoneoneoneone", "blablabla", "blablabla");
    }

    private void testInlineEditField(String fieldName, String fieldId, String oldValue, String newFormValue, String newTextValue)
            throws Exception
    {
        navigation.issue().gotoIssue("MK-1");
        assertions.assertNodeByIdHasText(fieldId, oldValue);

        // Get the token to be able to make the next request
        Element node = (Element) locator.css("meta[name=atlassian-token]").getNode();
        String token = node.getAttribute("content");

        // Simulate inline edit of field
        String body = fieldName + "=" + newFormValue + "&"
                + "issueId=10020&"
                + "singleFieldEdit=true&"
                + "fieldsToForcePresent=" + fieldName + "&"
                + "atl_token=" + token;
        WebResponse response = POST("IssueAction.jspa?decorator=none", body);

        // Refresh the page
        navigation.issue().gotoIssue("MK-1");
        // Make sure the field has the new value
        assertions.assertNodeByIdDoesNotHaveText(fieldId, oldValue);
        assertions.assertNodeByIdHasText(fieldId, newTextValue);
    }

    // IssueAction.jspa acts as a fake REST endpoint, but the content type is not JSON, so we need to override this method
    @Override
    public WebResponse POST(final String url, final String postBody) throws IOException, SAXException
    {
        tester.getDialog().getWebClient().setExceptionsThrownOnErrorStatus(false);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);

        final PostMethodWebRequest request = new PostMethodWebRequest(getBaseUrlPlus(url), new ByteArrayInputStream(postBody.getBytes()), "application/x-www-form-urlencoded");
        return tester.getDialog().getWebClient().sendRequest(request);
    }
}
