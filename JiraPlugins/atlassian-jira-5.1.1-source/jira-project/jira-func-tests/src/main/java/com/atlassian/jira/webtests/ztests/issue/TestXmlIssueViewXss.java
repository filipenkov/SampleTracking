package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableMap;
import com.meterware.httpunit.HttpUnitOptions;

import java.io.IOException;

/**
 * Test for XmlIssueView XSS security.
 *
 * @since v4.4
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES, Category.SECURITY })
public class TestXmlIssueViewXss extends FuncTestCase
{

    private static final String XSS_ALERT_RAW = "\"alert('surprise!')";
    private static final String XSS_ALERT_XML_ESCAPED = "&quot;alert(&apos;surprise!&apos;)";


    @Override
    protected void setUpHttpUnitOptions()
    {
        super.setUpHttpUnitOptions();
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(false);
    }

    protected void setUpTest()
    {
        super.setUpTest();
        navigation.login(ADMIN_USERNAME);
        administration.restoreBlankInstance();
    }

    @Override
    protected void tearDownTest()
    {
        navigation.login(ADMIN_USERNAME);
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);
        super.tearDownTest();
    }

    public void testXssInModuleKeyParam() throws IOException
    {
        tester.gotoPage("/si/jira.issueviews:<script>alert('XSS')<script>/HSP-1/HSP-1.xml");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS')<script>"));
    }

    public void testXssInIssueKeyParam() throws IOException
    {
        tester.gotoPage("/si/jira.issueviews:" + PROJECT_HOMOSAP_KEY + "/<script>alert('XSS')<script>");
        assertFalse(tester.getDialog().getResponse().getText().contains("<script>alert('XSS')<script>"));
    }

    public void testUsernameAndFullnameEscaping()
    {
        administration.usersAndGroups().addUser(XSS_ALERT_RAW, "password", XSS_ALERT_RAW, "xss@xss.com");
        navigation.login(XSS_ALERT_RAW, "password");
        final String issueKey = navigation.issue().createIssue(PROJECT_MONKEY, null, "Just a bug");
        navigation.issue().viewXml(issueKey);
        assertions.getTextAssertions().assertTextPresent(XSS_ALERT_XML_ESCAPED);
        assertions.getTextAssertions().assertTextNotPresent(XSS_ALERT_RAW);
    }

    public void testUsernameAndFullnameEscapingOnUserPicker()
    {
        administration.usersAndGroups().addUser(XSS_ALERT_RAW, "password", XSS_ALERT_RAW, "xss@xss.com");
        final String cfId = administration.customFields().addCustomField(builInCustomFieldKey(CUSTOM_FIELD_TYPE_USERPICKER), "test-xss");
        final String issueKey = navigation.issue().createIssue(PROJECT_MONKEY, null, "Just a bug", ImmutableMap.<String, String[]>of(
                cfId, new String[] { XSS_ALERT_RAW }
        ));
        navigation.issue().viewXml(issueKey);
        assertions.getTextAssertions().assertTextPresent(XSS_ALERT_XML_ESCAPED);
        assertions.getTextAssertions().assertTextNotPresent(XSS_ALERT_RAW);
    }

    public void testUsernameAndFullnameEscapingOnMultiUserPicker()
    {
        administration.usersAndGroups().addUser(XSS_ALERT_RAW, "password", XSS_ALERT_RAW, "xss@xss.com");
        final String cfId = administration.customFields().addCustomField(builInCustomFieldKey(CUSTOM_FIELD_TYPE_MULTIUSERPICKER), "test-xss");
        final String issueKey = navigation.issue().createIssue(PROJECT_MONKEY, null, "Just a bug", ImmutableMap.<String, String[]>of(
                cfId, new String[] { XSS_ALERT_RAW, ADMIN_USERNAME }
        ));
        navigation.issue().viewXml(issueKey);
        // for multiuserpicker we use CDATA
        assertions.getTextAssertions().assertTextPresent("<customfieldvalue><![CDATA[" + XSS_ALERT_RAW + "]]></customfieldvalue>");
        assertions.getTextAssertions().assertTextPresentNumOccurences(XSS_ALERT_RAW, 1);

    }
}
