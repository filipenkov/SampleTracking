package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;

/**
 * @since v4.1
 */
@Quarantine
@WebTest({Category.SELENIUM_TEST })
public class TestAvailableActionsAndOperationsInGadgets extends JiraSeleniumTest
{
    private static final long NO_OPERATIONS = 10035;
    private static final long NO_ACTIONS = 10040;

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestActionsAndOperations.xml");
    }

    @Override
    protected void onTearDown() throws Exception
    {
        super.onTearDown();
        //NB: THIS HERE IS DONE SO THE FOLLOWING TESTS ARE NOT FAILING WHEN CALLING client.waitForPageToLoad()
        //OTHERWISE THEY WILL FAIL with the error 'Current window or frame is closed!'
        client.selectWindow(null);
    }

    public void testAvailableIssuesAndOperations() throws Exception
    {
        getWebUnitTest().getAdministration().attachments().enable();

        getNavigator().gotoHome();
        assertThat.elementPresentByTimeout("css=#gadget-10010", 10000);
        client.selectFrame("gadget-10010");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        checkActionMenuLinksForFitlerResults("gadget-10010");

        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10020", true);
        assertThat.elementPresentByTimeout("css=#gadget-10021", 10000);
        client.selectFrame("gadget-10021");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        checkActionMenuLinksForAssignedToMe("gadget-10021");

        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10021", true);
        assertThat.elementPresentByTimeout("css=#gadget-10022", 10000);
        client.selectFrame("gadget-10022");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        checkActionMenuLinksForInProgress("gadget-10022");

        // Vore for issue
        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10010", true);
        assertThat.elementPresentByTimeout("css=#gadget-10010", 10000);
        client.selectFrame("gadget-10010");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        client.click("actions_10010");
        client.selectFrame("relative=parent");
        assertThat.elementPresentByTimeout("jquery=#actions_10010_drop");
        assertThat.elementPresentByTimeout("jquery=a:contains('Add Vote')", 10000);
        client.click("jquery=a:contains('Add Vote')", true);

        assertThat.elementPresentByTimeout("css=#gadget-10010", 10000);
        client.selectFrame("gadget-10010");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        client.click("jquery=#actions_10010");
        client.selectFrame("relative=parent");
        assertThat.elementPresentByTimeout("jquery=#actions_10010_drop");
        assertThat.elementPresentByTimeout("jquery=#actions_10010_drop a:contains('Remove Vote')", 10000);

        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10022", true);
        assertThat.elementPresentByTimeout("css=#gadget-10023", 10000);
        client.selectFrame("gadget-10023");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        checkActionMenuLinksForVoted("gadget-10023");

        // Watched issues
        getNavigator().gotoAdmin();
        client.click("css=a#general_configuration", true);
        client.click("jquery=a:contains('Edit Configuration')", true);
        client.click("jquery=input:radio[name='watching'][value='true']");
        client.click("jquery=#edit_property", true);
        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10010", true);

        assertThat.elementPresentByTimeout("css=#gadget-10010", 10000);
        client.selectFrame("gadget-10010");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        client.click("actions_10010");
        client.selectFrame("relative=parent");
        assertThat.elementPresentByTimeout("jquery=#actions_10010_drop");
        assertThat.elementPresentByTimeout("jquery=#actions_10010_drop a[href*='VoteOrWatchIssue.jspa']:contains('Watch')", 10000);
        client.click("jquery=#actions_10010_drop a[href*='VoteOrWatchIssue.jspa']:contains('Watch')", true);

        assertThat.elementPresentByTimeout("css=#gadget-10010", 10000);
        client.selectFrame("gadget-10010");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        client.click("jquery=#actions_10010");
        client.selectFrame("relative=parent");
        assertThat.elementPresentByTimeout("jquery=#actions_10010_drop");
        assertThat.elementPresentByTimeout("jquery=#actions_10010_drop a:contains('Stop Watching')", 10000);

        getNavigator().gotoPage("/secure/Dashboard.jspa?selectPageId=10023", true);
        assertThat.elementPresentByTimeout("css=#gadget-10024", 10000);
        client.selectFrame("gadget-10024");
        assertThat.elementPresentByTimeout("css=#issuetable", 10000);
        checkActionMenuLinksForWatched("gadget-10024");
    }

    private void checkActionMenuLinksForFitlerResults(String gadgetId)
    {
        client.selectFrame("relative=parent");
        final String currentLocation = client.getLocation();

        MenuCondition condition = new MenuCondition(this, this.assertThat, this.client, NO_OPERATIONS, "NOOP-1")
                .setReturnUrl(currentLocation)
                .addResolveIssue();
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, NO_ACTIONS, "NOACTION-1")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10050, "HSP-9")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10034, "HSP-8")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10033, "HSP-7")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10032, "HSP-6")
                .setReturnUrl(currentLocation)
                .addReopenIssue()
                .addCommentIssue()
                .addViewVoters()
                .addConvertToIssue()
                .addMoveSubtask()
                .addCloneIssue()
                .addDeleteIssue();
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10031, "HSP-5")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10030, "HSP-4")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10010, "HSP-2")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10000, "HSP-1")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);
    }

    private void checkActionMenuLinksForAssignedToMe(String gadgetId)
    {

        client.selectFrame("relative=parent");
        final String currentLocation = client.getLocation();

        MenuCondition condition = new MenuCondition(this, this.assertThat, this.client, 10010, "HSP-2")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10030, "HSP-4")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10031, "HSP-5")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10033, "HSP-7")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, 10034, "HSP-8")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);

        condition = new MenuCondition(this, this.assertThat, this.client, NO_ACTIONS, "NOACTION-1")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);


    }

    private void checkActionMenuLinksForVoted(String gadgetId)
    {

        client.selectFrame("relative=parent");
        final String currentLocation = client.getLocation();

        MenuCondition condition = new MenuCondition(this, this.assertThat, this.client, 10010, "HSP-2")
                .setReturnUrl(currentLocation)
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addRemoveVoteFor()
                .addViewVoters()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check(gadgetId, currentLocation);


    }
    private void checkActionMenuLinksForWatched(String gadgetId)
    {

        client.selectFrame("relative=parent");
        final String currentLocation = client.getLocation();

        MenuCondition condition = new MenuCondition(this, this.assertThat, this.client, 10010, "HSP-2")
                .setReturnUrl(currentLocation)
                .addStartProgress()
                .addResolveIssue()
                .addCloseIssue()
                .addEditIssue()
                .addAssignIssue()
                .addCommentIssue()
                .addLogWork()
                .addAttachFile()
                .addAttachScreenshot()
                .addRemoveVoteFor()
                .addViewVoters()
                .addStopWatching()
                .addManageWatchers()
                .addCreateSubtask()
                .addConvertToSubtask()
                .addMoveIssue()
                .addCloneIssue()
                .addEditLabels()
                .addDeleteIssue();
        condition.check(gadgetId, currentLocation);


    }

    private void checkActionMenuLinksForInProgress(String gadgetId)
    {

        client.selectFrame("relative=parent");
        final String currentLocation = client.getLocation();

        MenuCondition condition = new MenuCondition(this, this.assertThat, this.client, 10033, "HSP-7")
                .setReturnUrl(currentLocation)
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
        condition.check(gadgetId, currentLocation);


    }

}
