package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestTrackbackSettings extends FuncTestCase
{
    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.gotoAdminSection("trackbacks");
    }

    public void testSettingOutgoingTrackbackPings()
    {
        _testSendOutgoingPingsForPublicIssues();
        _testSendOutgoingPingsOff();
        _testSettingSendOutgoingPingsForAllIssues();
    }

    public void _testSendOutgoingPingsForPublicIssues()
    {
        tester.clickLinkWithText("Edit Configuration");
        tester.checkCheckbox("sendPings", "public");
        tester.submit("Update");
        text.assertTextSequence(locator.page(), "Send Outgoing Trackback Pings", "(for public issues only)");
    }

    public void _testSendOutgoingPingsOff()
    {
        tester.clickLinkWithText("Edit Configuration");
        tester.checkCheckbox("sendPings", "false");
        tester.submit("Update");
        text.assertTextSequence(locator.page(), "Send Outgoing Trackback Pings", "OFF");
    }

    public void _testSettingSendOutgoingPingsForAllIssues()
    {
        tester.clickLinkWithText("Edit Configuration");
        tester.checkCheckbox("sendPings", "allIssues");
        tester.submit("Update");
        text.assertTextSequence(locator.page(), "Send Outgoing Trackback Pings", "(for all issues)");
    }
}
