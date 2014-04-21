package com.atlassian.jira.webtest.selenium.issue;

import com.atlassian.jira.functest.framework.util.url.ParsedURL;
import com.atlassian.jira.functest.framework.util.url.URLUtil;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.SeleniumAssertions;
import com.atlassian.selenium.SeleniumClient;
import junit.framework.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for testing JIRA operation/action menus
 *
 * @since v4.2
 */
class MenuCondition
{
    private static final Pattern SELECTED_ID_PATTERN = Pattern.compile("selectedIssueId=\\d*");

    private static final String VIEW_ISSUE = "View Issue";
    private static final String ASSIGN_ISSUE = "Assign";
    private static final String ASSIGN_ISSUE_ME = "Assign To Me";
    private static final String ATTACH_FILE = "Attach File";
    private static final String ATTACH_SCREENSHOT = "Attach Screenshot";
    private static final String CLONE = "Clone";
    private static final String COMMENT = "Comment";
    private static final String CREATE_SUB_TASK = "Create Sub-Task";
    private static final String DELETE = "Delete";
    private static final String EDIT = "Edit";
    private static final String MOVE = "Move";
    private static final String EDIT_LABELS = "Labels";
    private static final String CONVERT_SUBTASK = "Convert to Sub-Task";
    private static final String CONVERT_ISSUE = "Convert to Issue";
    private static final String LOG_WORK = "Log Work";
    private static final String RESOLVE = "Resolve Issue";
    private static final String CLOSE = "Close Issue";
    private static final String START = "Start Progress";
    private static final String STOP = "Stop Progress";
    private static final String REOPEN = "Reopen Issue";
    private static final String VOTE = "Add Vote";
    private static final String UNVOTE = "Remove Vote";
    private static final String VIEW_VOTERS = "Voters";
    private static final String UNWATCH = "Stop Watching";
    private static final String MANAGE_WATCHERS = "Watchers";
    

    private final List<ActionItem> actions = new ArrayList<ActionItem>();
    private final List<ActionItem> operations = new ArrayList<ActionItem>();

    private final JiraSeleniumTest test;
    private final SeleniumAssertions assertThat;
    private final SeleniumClient client;

    private long issueId = -1;
    private String returnUrl = null;
    private int timeout = 7000;
    private String issueKey = null;
    private String frameId = null;


    MenuCondition(final JiraSeleniumTest test, final SeleniumAssertions assertThat, final SeleniumClient client,
            long issueId, String issueKey)
    {
        this.test = test;
        this.assertThat = assertThat;
        this.client = client;
        this.issueId = issueId;
        this.issueKey = issueKey;
    }


    MenuCondition setFrameId(String frameId)
    {
        this.frameId = frameId;
        return this;
    }

    MenuCondition setReturnUrl(String returnUrl)
    {
        final Matcher matcher = SELECTED_ID_PATTERN.matcher(returnUrl);
        if (matcher.find())
        {
            this.returnUrl = matcher.replaceAll("selectedIssueId=" + issueId);
        }
        else
        {
            this.returnUrl = addParameter(new StringBuilder(returnUrl), "selectedIssueId", String.valueOf(issueId)).toString();
        }
        return this;
    }

    MenuCondition addCloseIssue()
    {
        return addAction(CLOSE, 2);
    }

    MenuCondition addOtherCloseIssue()
    {
        return addAction(CLOSE, 701);
    }


    MenuCondition addResolveIssue()
    {
        return addAction(RESOLVE, 5);
    }

    MenuCondition addStartProgress()
    {
        return addAction(START, 4);
    }

    MenuCondition addStopProgress()
    {
        return addAction(STOP, 301);
    }

    MenuCondition addReopenIssue()
    {
        return addAction(REOPEN, 3);
    }

    MenuCondition addAssignIssue()
    {
        return addOperation(ASSIGN_ISSUE, "secure/AssignIssue!default.jspa?id=" + issueId);
    }

    MenuCondition addAttachFile()
    {
        return addOperation(ATTACH_FILE, "secure/AttachFile!default.jspa?id=" + issueId);
    }

    MenuCondition addAttachScreenshot()
    {
        String app  = client.getEval("dom=this.browserbot.getCurrentWindow().navigator.appVersion");

        if(app.toLowerCase().contains("win") || app.toLowerCase().contains("mac"))
        {
            return addOperation(ATTACH_SCREENSHOT, "secure/AttachScreenshot!default.jspa?id=" + issueId);
        }
        return this;
    }

    MenuCondition addCloneIssue()
    {
        return addOperation(CLONE, "secure/CloneIssueDetails!default.jspa?id=" + issueId);
    }

    MenuCondition addCommentIssue()
    {
        return addOperation(COMMENT, "secure/AddComment!default.jspa?id=" + issueId);
    }

    MenuCondition addCreateSubtask()
    {
        return addOperation(CREATE_SUB_TASK, "secure/CreateSubTaskIssue!default.jspa?parentIssueId=" + issueId);
    }

    MenuCondition addDeleteIssue()
    {
        return addOperation(DELETE, "secure/DeleteIssue!default.jspa?id=" + issueId);
    }

    MenuCondition addEditIssue()
    {
        return addOperation(EDIT, "secure/EditIssue!default.jspa?id=" + issueId);
    }

    MenuCondition addMoveIssue()
    {
        return addOperation(MOVE, "secure/MoveIssue!default.jspa?id=" + issueId);
    }

    MenuCondition addEditLabels()
    {
        return addOperation(EDIT_LABELS, "secure/EditLabels!default.jspa?id=" + issueId);
    }

    public MenuCondition addMoveSubtask()
    {
        return addOperation(MOVE, "secure/MoveSubTaskChooseOperation!default.jspa?id=" + issueId);
    }

    MenuCondition addConvertToSubtask()
    {
        return addOperation(CONVERT_SUBTASK, "secure/ConvertIssue.jspa?id=" + issueId);
    }

    MenuCondition addLogWork()
    {
        return addOperation(LOG_WORK, "secure/CreateWorklog!default.jspa?id=" + issueId);
    }

    MenuCondition addAssignMe(final String user)
    {
        return addOperation(ASSIGN_ISSUE_ME, "secure/AssignIssue.jspa?id=" + issueId + "&assignee=" + user);
    }

    MenuCondition addConvertToIssue()
    {
        return addOperation(CONVERT_ISSUE, "secure/ConvertSubTask.jspa?id=" + issueId);
    }

    MenuCondition addVoteFor()
    {
        return addOperation(VOTE, "secure/VoteOrWatchIssue.jspa?id=" + issueId + "&vote=vote");
    }

    MenuCondition addRemoveVoteFor()
    {
        return addOperation(UNVOTE, "secure/VoteOrWatchIssue.jspa?id=" + issueId + "&vote=unvote");
    }

    MenuCondition addViewVoters()
    {
        return addOperation(VIEW_VOTERS, "secure/ViewVoters!default.jspa?id=" + issueId);
    }

    MenuCondition addStopWatching()
    {
        return addOperation(UNWATCH, "secure/VoteOrWatchIssue.jspa?id=" + issueId + "&watch=unwatch");
    }


    MenuCondition addManageWatchers()
    {
        return addOperation(MANAGE_WATCHERS, "secure/ManageWatchers!default.jspa?id=" + issueId);
    }

    MenuCondition addAction(String title, int actionId)
    {
        actions.add(new ActionItem(title, "secure/WorkflowUIDispatcher.jspa?id=" + issueId + "&action="
                + actionId + "&atl_token=" + test.getXsrfToken()));
        return this;
    }

    MenuCondition addOperation(String label, String uri)
    {
        return addOperation(new ActionItem(label, withXsrf(uri)));
    }

    MenuCondition addOperation(ActionItem item)
    {
        if (item != null)
        {
            operations.add(item);
        }
        return this;
    }

    private String withXsrf(String url)
    {
        return URLUtil.addXsrfToken(test.getXsrfToken(), url);
    }

    void check()
    {
        client.selectFrame("relative=top");
        if (frameId != null)
        {
            client.selectFrame(frameId);
        }

        client.click("actions_" + issueId);

        //Check that the dropdown actually displays.
        client.selectFrame("relative=top");
        assertThat.elementPresentByTimeout(getUl(1), timeout);
        checkLink(getA(1, 1), VIEW_ISSUE, createBrowseUrl());
        assertThat.elementNotPresent(getLi(1, 2));

        int position = 2;
        if (!actions.isEmpty())
        {
            checkItems(position++, actions);
        }
        if (!operations.isEmpty())
        {
            checkItems(position++, operations);
        }

        assertThat.elementNotPresent(getUl(position));
    }

    void check(String gadetId, final String expectedMenuLocation)
    {
        client.selectWindow(gadetId);
        client.click("actions_" + issueId);

        client.selectFrame("relative=parent");
        final String actualLocation = client.getLocation();
        Assert.assertEquals("We are expected to be on this window : " + expectedMenuLocation, expectedMenuLocation, actualLocation);

        //Check that the dropdown actually displays.
        assertThat.elementPresentByTimeout(getUl(1), timeout);
        checkLink(getA(1, 1), VIEW_ISSUE, createBrowseUrl());
        assertThat.elementNotPresent(getLi(1, 2));

        int position = 2;
        if (!actions.isEmpty())
        {
            checkItems(position++, actions);
        }
        if (!operations.isEmpty())
        {
            checkItems(position++, operations);
        }

        assertThat.elementNotPresent(getUl(position));
    }


    private void checkItems(int position, Collection<? extends ActionItem> items)
    {
        assertThat.elementPresent(getUl(position));
        int i = 1;
        for (ActionItem item : items)
        {
            checkLink(getA(position, i++), item);
        }

        assertThat.elementNotPresent(getLi(position, i));
    }

    private void checkLink(String locator, ActionItem item)
    {
        checkLink(locator, item.getTitle(), createReturnUrl(item.getUrl()));
    }

    private void checkLink(String locator, String title, String link)
    {
        assertThat.elementContainsText(locator, title);
        final String attribute = client.getAttribute(locator + "@href");

        ParsedURL expected = new ParsedURL(link);
        ParsedURL actual = new ParsedURL(attribute);

        final boolean samePath = actual.getPath().endsWith(expected.getPath());
        final boolean sameParameters = expected.getQueryParametersIgnoring("returnUrl").equals(actual.getQueryParametersIgnoring("returnUrl"));

        Assert.assertTrue("Expecting link \n\t\t'" + link + "' but got \n\t\t'" + attribute + "'.", samePath && sameParameters);
    }

    private String createReturnUrl(String url)
    {
        if (returnUrl != null)
        {
            return addParameter(new StringBuilder(url), "returnUrl", returnUrl).toString();
        }
        else
        {
            return url;
        }
    }

    private String createBrowseUrl()
    {
        return "browse/" + issueKey;
    }

    private StringBuilder addParameter(StringBuilder builder, String key, String value)
    {
        if (builder.indexOf("?") >= 0)
        {
            builder.append("&");
        }
        else
        {
            builder.append("?");
        }

        try
        {
            builder.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        return builder;
    }

    private String getUl(int ulPos)
    {
        return "jquery=div#actions_" + issueId + "_drop ul:nth-child(" + ulPos + ")";
    }

    private String getLi(int ulPos, int liPos)
    {
        return "jquery=div#actions_" + issueId + "_drop ul:nth-child(" + ulPos + ") > li:nth-child(" + liPos + ")";
    }

    private String getA(int ulPos, int liPos)
    {
        return "jquery=div#actions_" + issueId + "_drop ul:nth-child(" + ulPos + ") > li:nth-child(" + liPos + ") > a";
    }
}
