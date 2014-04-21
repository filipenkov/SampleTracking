package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.framework.dialogs.AddCommentDialog;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;
import com.atlassian.jira.webtest.selenium.framework.model.Mouse;
import com.atlassian.jira.webtest.selenium.framework.model.SubmitType;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;

/**
 * 
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) // JS Error - Responsibility: JIRA Team
@Quarantine
@WebTest({Category.SELENIUM_TEST })
public class TestAvailableActionsAndOperations extends JiraSeleniumTest
{
    private static final long NO_OPERATIONS = 10035;
    private static final long NO_ACTIONS = 10040;


    protected static final int TIMEOUT = 30000;
    private String xsrfToken;

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestActionsAndOperations.xml");
    }

    public void testAvailableIssuesAndOperations() throws Exception
    {
        getWebUnitTest().getAdministration().attachments().enable();

        getNavigator().findAllIssues();
        checkActionMenuLinksFromCurrentLocation();

        getNavigator().findIssuesWithJql("summary is not empty");
        checkActionMenuLinksFromCurrentLocation();

        getNavigator().gotoPage("secure/IssueNavigator.jspa?selectedIssueId=10000", true);
        checkActionMenuLinksFromCurrentLocation();
    }

    // JRA-18745
    public void testActions() throws Exception
    {
        final String expectedLocation = "secure/IssueNavigator.jspa?reset=true&jqlQuery=project+%3D+HSP";
        final String resolveIssueXpath = "//*[@id='actions_10000_drop']//a[normalize-space(text())='Close Issue']";

        getNavigator().findIssuesWithJql("project = HSP");
        client.click("permlink", true);

        //Check that cancel works.
        client.click("actions_10000");
        assertThat.elementPresentByTimeout(resolveIssueXpath, 5000);
        String prevLocation = client.getLocation();
        client.click(resolveIssueXpath);
        assertThat.elementPresentByTimeout("issue-workflow-transition-cancel", 5000);
        client.click("id=issue-workflow-transition-cancel");
        String location = client.getLocation();
        assertTrue("Expected to be directed back to '" + prevLocation + "' but was directed to '" + location + "'.", location.endsWith(prevLocation));

        //Check that resolving the issue works.
        client.click("actions_10000");
        assertThat.elementPresentByTimeout(resolveIssueXpath, 5000);
        client.click(resolveIssueXpath);
        assertThat.elementPresentByTimeout("issue-workflow-transition-submit", 5000);
        client.click("issue-workflow-transition-submit");
        client.waitForPageToLoad();
        location = client.getLocation();
        assertTrue("Expected to be directed back to '" + expectedLocation + "' but was directed to '" + location + "'.", location.endsWith(expectedLocation));

        //Make sure the issue status has changed.
        getNavigator().gotoIssue("HSP-1");
        assertThat.elementHasText("issuedetails", "Closed");
    }

    private void checkActionMenuLinksFromCurrentLocation()
    {
        final String currentLocation = client.getLocation();

        MenuCondition condition = new MenuCondition(this, assertThat, client, NO_OPERATIONS, "NOOP-1").setReturnUrl(currentLocation);
        condition.addResolveIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, NO_ACTIONS, "NOACTION-1").setReturnUrl(currentLocation);
        condition
            .addEditIssue()
            .addAssignIssue()
            .addCommentIssue()
            .addLogWork()
            .addAttachFile()
            .addAttachScreenshot()
            .addViewVoters()
            .addCreateSubtask()
            .addConvertToSubtask()
            .addMoveIssue()
            .addCloneIssue()
            .addEditLabels()   
            .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10050, "HSP-9").setReturnUrl(currentLocation);
        condition.addResolveIssue()
            .addCloseIssue()
            .addEditIssue()
            .addAssignIssue()
            .addAssignMe("admin")
            .addCommentIssue()
            .addLogWork()
            .addAttachFile()
            .addAttachScreenshot()
            .addViewVoters()
            .addCreateSubtask()
            .addConvertToSubtask()
            .addMoveIssue()
            .addCloneIssue()
            .addEditLabels()
            .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10034, "HSP-8").setReturnUrl(currentLocation);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10033, "HSP-7").setReturnUrl(currentLocation);
        condition
                .addStopProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10032, "HSP-6").setReturnUrl(currentLocation);
        condition
                .addReopenIssue()
                .addCommentIssue()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10031, "HSP-5").setReturnUrl(currentLocation);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10030, "HSP-4").setReturnUrl(currentLocation);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addCreateSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10010, "HSP-2").setReturnUrl(currentLocation);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addVoteFor()
                .addViewVoters()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10000, "HSP-1").setReturnUrl(currentLocation);
        condition
                .addOtherCloseIssue()
                .addReopenIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

    }

    // JRADEV-3179
    public void testActionsMenuUnbindsEventsOnClose()
    {
        final String issueActionsCog = "actions_10000";
        final String issueActionsMenuLocator = "jquery=#actions_10000_drop";
        final String addCommentLinkLocator = issueActionsMenuLocator + " a.issueaction-comment-issue";

        getNavigator().findIssuesWithJql("project = HSP");
        final String issueNavigatorUrl = client.getLocation();

        client.click(issueActionsCog);
        assertThat.elementPresentByTimeout(issueActionsMenuLocator, context().timeouts().components());
        context().ui().pressInBody(Keys.ESCAPE);

        client.click(issueActionsCog);
        assertThat.elementPresentByTimeout(issueActionsMenuLocator, context().timeouts().components());
        Mouse.mouseover(client, addCommentLinkLocator);
        client.click(addCommentLinkLocator);
        AddCommentDialog addCommentDialog = new AddCommentDialog(context());
        addCommentDialog.assertReady();
        // We need to use the keyboard because mouseover on the cog menu will bind events for the enter key.
        context().ui().pressInBody(Keys.ENTER);

        addCommentDialog.submit(SubmitType.BY_CLICK, false);
        assertThat.elementPresentByTimeout(addCommentDialog.locator() + " div.error", context().timeouts().dialogLoad());
        addCommentDialog.cancel(CancelType.BY_CLICK);
        assertEquals("We shouldn't redirect away from the issue navigator to view issue.", issueNavigatorUrl, client.getLocation());
    }

    public void testGadgetIssueOperations()
    {
        // seed the token into the test. Definitely side effect free.
        getXsrfToken();

        waitFor(2500);
        final String currentLocation = client.getLocation();

        String frameId = selectGadget("Filter Results: All Issues");

        MenuCondition condition = new MenuCondition(this, assertThat, client, NO_OPERATIONS, "NOOP-1").setReturnUrl(currentLocation).setFrameId(frameId);
        condition.addResolveIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, NO_ACTIONS, "NOACTION-1").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10050, "HSP-9").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addAssignMe("admin")
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10034, "HSP-8").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10033, "HSP-7").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addStopProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10032, "HSP-6").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addReopenIssue()
                .addCommentIssue()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10031, "HSP-5").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10030, "HSP-4").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addCreateSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10010, "HSP-2").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addVoteFor()
                .addViewVoters()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();

        condition = new MenuCondition(this, assertThat, client, 10000, "HSP-1").setReturnUrl(currentLocation).setFrameId(frameId);
        condition
                .addOtherCloseIssue()
                .addReopenIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addViewVoters()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check();


    }

    @Override
    public String getXsrfToken()
    {
        if (xsrfToken == null)
        {
            xsrfToken = super.getXsrfToken();
        }
        return xsrfToken;
    }

    private String selectGadget(String gadgetTitle)
    {
        final String frameId = client.getEval("this.browserbot.getCurrentWindow().jQuery(\"div.dashboard h3:contains('"
                + gadgetTitle + "')\").closest('.dashboard-item-frame').find('iframe').attr('id')");
        client.selectFrame(frameId);
        return frameId;
    }


}
