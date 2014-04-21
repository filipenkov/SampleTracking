package com.atlassian.jira.webtest.selenium.auidialog.actions;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.auidialog.AbstractAuiDialogTest;
import com.atlassian.jira.webtest.selenium.framework.dialogs.ActionsDialog;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import junit.framework.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Selenium test for the Dot Dialog and all related actions.
 *
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestIssueActionsDialog extends AbstractAuiDialogTest
{
    private static final String TEST_XML = "issueactionsdialog.xml";
    private static final String JQUERY_BODY = "jquery=body";
    private static final String JQUERY_AUI_POPUP = "jquery=.aui-popup:visible";
    private static final String JQUERY_SUBMIT = JQUERY_AUI_POPUP + " :submit";

    private static final String ATTACH_FILE = "Attach File";
    private static final String LOG_WORK = "Log Work";
    private static final String DELETE = "Delete";
    private static final String CLONE = "Clone";
    private static final String LINK_ISSUE = "Link";
    private static final String EDIT_ISSUE_LABELS = "Edit issue labels";
    private static final String START_PROGRESS = "Start Progress";
    private static final String STOP_PROGRESS = "Stop Progress";
    private static final String REOPEN_ISSUE = "Reopen Issue";
    private static final String RESOLVE_ISSUE = "Resolve Issue";
    private static final String CLOSE_ISSUE = "Close Issue";
    private static final String EDIT_ISSUE = "Edit";
    private static final String WATCHERS = "Watchers";
    private static final String VOTERS = "Voters";
    private static final String COMMENT = "Comment";
    private static final String MOVE_ISSUE = "Move";
    private static final String CREATE_SUBTASK = "Create Sub-Task";
    private static final String WATCH = "Watch Issue";
    private static final String UNWATCH = "Stop Watching";

    private static final String ESCAPE_KEY = "\\27";

    private static final int WAIT_TIME = 25000;

    private final Map<String, Dialog> actionsWithDialogs = new LinkedHashMap<String, Dialog>();
    private final Map<String, IssueAction> actions = new LinkedHashMap<String, IssueAction>();
    private final Map<String, IssueAction> transitions = new LinkedHashMap<String, IssueAction>();
    private ActionsDialog actionsDialog;

    private static final String ISSUE_NAVIGATOR = "Issue Navigator";
    private static final String HSP_1 = "HSP-1";
    private static final String MKY_1 = "MKY-1";
    private static final String ASSIGN = "Assign";

    {
        actions.put(EDIT_ISSUE, new PageLink(EDIT_ISSUE, "jquery=#issue-edit", "(HSP|MKY)-[0-9]+ has been updated.", "e"));
        actions.put(COMMENT, new CommentIssue(COMMENT, "m"));
        actions.put(WATCHERS, new PageLinkThatDoesNotReturnToPage(WATCHERS, "jquery=h2:contains(Watchers)", null, null));
        actions.put(VOTERS, new PageLinkThatDoesNotReturnToPage(VOTERS, "jquery=h2:contains(Voters)", null, null));
        actions.put(MOVE_ISSUE, new PageLinkThatDoesNotReturnToPage(MOVE_ISSUE, "jquery=.formtitle:contains(Move Issue)", "(MKY|HSP)-[0-9] has been updated.", null));
        actions.put(CREATE_SUBTASK, new CreateSubTaskLink(CREATE_SUBTASK, "jquery=h2:contains(Create Sub-Task)", "(MKY|HSP)-[0-9] has been updated.", null));
        actions.put(WATCH, new WatchIssue(WATCH));
        actions.put(UNWATCH, new UnWatchIssue(UNWATCH));

        transitions.put(START_PROGRESS, new StartProgress(START_PROGRESS));
        transitions.put(STOP_PROGRESS, new StopProgress(STOP_PROGRESS));
        transitions.put(RESOLVE_ISSUE, new Dialog(RESOLVE_ISSUE, RESOLVE_ISSUE, "(MKY|HSP)-[0-9] has been updated.", null));
        transitions.put(REOPEN_ISSUE, new Dialog(REOPEN_ISSUE, REOPEN_ISSUE, "(MKY|HSP)-[0-9] has been updated.", null));
        transitions.put(CLOSE_ISSUE, new CloseIssue(CLOSE_ISSUE, CLOSE_ISSUE, "(MKY|HSP)-[0-9] has been updated.", null));

        actionsWithDialogs.put(LOG_WORK, new LogWorkDialog());
        actionsWithDialogs.put(CLONE, new Dialog(CLONE, CLONE, "(MKY|HSP)-[0-9] has been cloned.", null));
        actionsWithDialogs.put(ASSIGN, new AssignDialog(ASSIGN, ASSIGN, "a"));
        actionsWithDialogs.put(ATTACH_FILE, new AttachFileDialog());//MLQ temporarily removed
        actionsWithDialogs.put(LINK_ISSUE, new LinkIssueDialog());
//        actionsWithDialogs.put(EDIT_ISSUE_LABELS, new EditIssueLabelsDialog());//MLQ temporarily removed
        actionsWithDialogs.put(DELETE, new Dialog(DELETE, DELETE, "(MKY|HSP)-[0-9] has been deleted.", null)); // I have to be last
    }

    private boolean issueNavigatorTest = false;
    private static final String HSP1_RESOLVE_ACTION = "action_id_5";
    private static final String FORM_ISSUE_WORKFLOW_TRANSITION_ID = "issue-workflow-transition";

    private static final Pattern ampRegex = Pattern.compile("&amp;");

    public void onSetUp()
    {
        super.onSetUp();
        issueNavigatorTest = false;
        restoreData(TEST_XML);
        actionsDialog = new ActionsDialog(context());
    }

    public static Test suite()
    {
        return suiteFor(TestIssueActionsDialog.class);
    }

    public void testActionsDialogsQuerying()
    {
        getNavigator().gotoIssue(HSP_1);
        actionsDialog.open();
        _testSuggestionsOnEditingQuery();
        _testNoMatches();
        _testOnlyQueryStartOfWords();
        _testBoldingOfMatch();
    }

    public void testActionsFromDialogOnIssueNav()
    {
        issueNavigatorTest = true;
        getNavigator().findAllIssues();

        _testActionsFromDialog(actions, ISSUE_NAVIGATOR);
        _testActionsFromDialog(transitions, ISSUE_NAVIGATOR);
        _testActionsFromDialog(actionsWithDialogs, ISSUE_NAVIGATOR);
    }

    public void testActionsOnIssueNavigator()
    {
        issueNavigatorTest = true;
        getNavigator().findAllIssues();

        _testActions(actions, ISSUE_NAVIGATOR);
        _testActions(transitions, ISSUE_NAVIGATOR);
        _testActions(actionsWithDialogs, ISSUE_NAVIGATOR);
    }

    public void testActionsFromDialogOnViewIssue()
    {
        getNavigator().gotoIssue(MKY_1);

        _testActionsFromDialog(actions, MKY_1);
        _testActionsFromDialog(transitions, MKY_1);
        _testActionsFromDialog(actionsWithDialogs, MKY_1);
    }

    public void testActionsOnViewIssue()
    {
        getNavigator().gotoIssue(HSP_1);

        _testActions(actions, HSP_1);
        _testActions(transitions, HSP_1);
        _testActions(actionsWithDialogs, HSP_1);
    }

    /**
     * If the action AJAX call throws an error, can the dialogs handle it.
     */
    public void testDialogAjaxErrorHandling()
    {
        getNavigator().gotoIssue(HSP_1);

        // #action_id_5 is resolve issue workflow.  We need to insert a new bad initial url
        // so it will go POP!
        client.getEval("(function ($) {"
                + "     var url = $('#"+ HSP1_RESOLVE_ACTION +"').attr('href'); \n"
                + "     url = url.replace('.jspa','!POPGOESTHEWEASEL.jspa'); \n"
                + "     $('#"+ HSP1_RESOLVE_ACTION +"').attr('href',url);"
                + "})(this.browserbot.getCurrentWindow().jQuery);"
        );
        client.clickAndWaitForAjaxWithJquery(HSP1_RESOLVE_ACTION, WAIT_TIME);
        //
        // it should come up inside an error div and also tell us what went wrong
        assertThat.elementPresentByTimeout("jquery=div.aui-popup-content .ajaxerror");
        assertThat.elementContainsText("jquery=div.aui-popup-content .ajaxerror", "No command 'POPGOESTHEWEASEL' in action");
        closeDialogByClickingCancel();

        // ok after that we can bring up the 2nd stage (eg submit stage) and cause that url to be bad and ensure that is also caught
        //
        // Ideally this code would be in action, in that it would help prove that the submit phase has error handling, however Scott H, Scott H
        // have proved that you cant change the form action attribute for security reasons.
        //
        // Long term we should have a testing plugin that truly went POP on the server but until then.....
        //
        //        getNavigator().gotoIssue(HSP_1);
        //        client.clickAndWaitForAjaxWithJquery(HSP1_RESOLVE_ACTION);
        //
        //        final String eval = "(function ($) {"
        //                + "     var url = $('#" + FORM_ISSUE_WORKFLOW_TRANSITION_ID + "').attr('action'); \n"
        //                + "     url = url.replace('.jspa','!POPGOESTHEWEASEL.jspa'); \n"
        //                + "     $('#" + FORM_ISSUE_WORKFLOW_TRANSITION_ID + "').attr('action',url);\n"
        //                + "})(this.browserbot.getCurrentWindow().jQuery);";
        //        client.getEval(eval);
        //        submitDialog();
        //
        //        assertThat.elementPresentByTimeout("jquery=div.aui-popup-content .ajaxerror");
        //        assertThat.elementContainsText("jquery=div.aui-popup-content .ajaxerror", "No command 'POPGOESTHEWEASEL' in action");
        //        closeDialogByClickingCancel();
        
    }

    //JRADEV-2443
    public void testCancelReturnsToNavigatorWithoutNotification()
    {
        getNavigator().findAllIssues();

        //first lets try an AUI form
        actionsDialog.open();
        actionsDialog.queryActions("edit");
        actionsDialog.selectSuggestionUsingClick();
        client.waitForPageToLoad();

        assertThat.textPresent("Edit Issue");
        client.click("issue-edit-cancel", true);

        assertThat.elementNotPresentByTimeout("id=affectedIssueMsg", DROP_DOWN_WAIT);

        //now lets try an old JIRA form
        actionsDialog.open();
        actionsDialog.queryActions("move");
        actionsDialog.selectSuggestionUsingClick();
        client.waitForPageToLoad();

        assertThat.textPresent("Move Issue");
        client.click("cancelButton", true);

        assertThat.elementNotPresentByTimeout("id=affectedIssueMsg", DROP_DOWN_WAIT);
    }

    private void _testBoldingOfMatch()
    {
        // only query start of words
        actionsDialog.queryActions("close");
        actionsDialog.assertStringIsBolded("Close");

        actionsDialog.queryActions("File");
        actionsDialog.assertStringIsBolded("File");
    }


    private void _testSuggestionsOnEditingQuery()
    {
        actionsDialog.queryActions("st");
        actionsDialog.assertSuggestionPresent("Start Progress");
        actionsDialog.assertSuggestionNotPresent("Create Sub-Task");
        actionsDialog.assertStringIsBolded("St");
        actionsDialog.inputLocatorObject().element().type(SpecialKeys.BACKSPACE);
        actionsDialog.assertSuggestionPresent("Start Progress", "Create Sub-Task");
        actionsDialog.assertStringIsBolded("S");

        actionsDialog.queryActions("st");
        actionsDialog.assertSuggestionPresent("Start Progress");
        actionsDialog.assertSuggestionNotPresent("Create Sub-Task");
        actionsDialog.assertStringIsBolded("St");
        actionsDialog.inputLocatorObject().element().type(SpecialKeys.BACKSPACE);
        actionsDialog.assertSuggestionPresent("Start Progress", "Create Sub-Task");
        actionsDialog.assertStringIsBolded("S");

        actionsDialog.inputLocatorObject().element().type(SpecialKeys.BACKSPACE);
        assertFalse(actionsDialog.isSuggestionsOpen());
    }

    private void _testOnlyQueryStartOfWords()
    {
        // only query start of words
        actionsDialog.queryActions("gress");
        actionsDialog.assertNoSuggestions();
    }

    private void _testNoMatches()
    {
        final String userQuery = "zzz";
        actionsDialog.queryActions(userQuery);
        actionsDialog.assertNoSuggestions();

        // Pressing enter on a query with no matches should be a no-op.
        actionsDialog.selectActionUsingEnter();
        actionsDialog.assertNoSuggestions();
        assertEquals(userQuery, client.getValue(actionsDialog.inputAreaLocator()));
    }

    private void assertThanksMsg(final IssueAction action)
    {
        if (issueNavigatorTest)
        {
            assertThat.elementPresentByTimeout("jquery=#affectedIssueMsg", WAIT_TIME);

            final String msg = client.getText("jquery=#affectedIssueMsg");
            final String thanksMsgRegex = action.getThanksMessageRegex();
            if (thanksMsgRegex != null)
            {
                final Matcher matcher = Pattern.compile(thanksMsgRegex).matcher(msg);
                final boolean itMatches = matcher.find();
                assertTrue("Thanks msg " + thanksMsgRegex + " was not matched for action " + action.getLabel() + " was : " + msg, itMatches);
            }
        }
    }

    private void assertNotDoubleEscaped(final String url)
    {
        if (issueNavigatorTest)
        {
            final Matcher matcher = ampRegex.matcher(url);
            final boolean itMatches = matcher.find();
            assertFalse("the url " + url + " was double escaped ", itMatches);
        }
    }


    private void _testActions(Map<String, ? extends IssueAction> actions, String returnPageTitle)
    {
        for (IssueAction action : actions.values())
        {
            try
            {
                FuncTestOut.log("Testing '" + action.getLabel() + "' action");
                if (action.getKeyboardCommand() != null)
                {
                    client.typeKeys(JQUERY_BODY, action.getKeyboardCommand());
                    action.performAction();
                    if (action.isDeleteAction())
                    {
                        assertThat.elementPresentByTimeout("jquery=title:contains(" + ISSUE_NAVIGATOR + ")", WAIT_TIME);
                    }
                    else
                    {
                        assertThat.elementPresentByTimeout("jquery=title:contains(" + returnPageTitle + ")", WAIT_TIME);
                    }
                    assertThanksMsg(action);
                    assertNotDoubleEscaped(client.getLocation());

                }
            }
            catch (Throwable t)
            {
                rethrowActionError(action, t);
            }
        }
    }

    private void _testActionsFromDialog(Map<String, ? extends IssueAction> actions, String returnPageTitle)
    {
        for (IssueAction action : actions.values())
        {
                FuncTestOut.log("Testing '" + action.getLabel() + "' dialog");
                actionsDialog.open();
                actionsDialog.queryActions(action.getLabel());
                actionsDialog.assertSuggestionPresent(action.getLabel(), true);
                actionsDialog.selectSuggestionUsingClick();
                action.performAction();
                if (action.isDeleteAction())
                {
                    assertThat.elementPresentByTimeout("jquery=title:contains(" + ISSUE_NAVIGATOR + ")", WAIT_TIME);
                }
                else
                {
                    assertThat.elementPresentByTimeout("jquery=title:contains(" + returnPageTitle + ")", WAIT_TIME);
                }
                if (!(action instanceof  AttachFileDialog))
                {
                    assertThanksMsg(action);
                    assertNotDoubleEscaped(client.getLocation());
                }
        }
    }



    private void rethrowActionError(final IssueAction action, final Throwable throwable)
    {
        throw new RuntimeException("Failed assertions on dialog : " + action.getLabel(), throwable);
    }

    private interface IssueAction
    {
        String getLabel();

        String getKeyboardCommand();

        String getThanksMessageRegex();

        void performAction();

        boolean isDeleteAction();

    }

    private class PageLink implements IssueAction
    {
        final private String label;
        final private String assertionSelector;
        final private String thanksMsgRegex;
        final private String key;

        public PageLink(final String label, final String assertionSelector, final String thanksMsgRegex, final String key)
        {
            this.label = label;
            this.assertionSelector = assertionSelector;
            this.thanksMsgRegex = thanksMsgRegex;
            this.key = key;
        }

        public boolean isDeleteAction()
        {
            return false;
        }

        public String getAssertionSelector()
        {
            return assertionSelector;
        }

        public String getThanksMessageRegex()
        {
            return thanksMsgRegex;
        }

        public String getLabel()
        {
            return label;
        }

        public void performAction()
        {
            client.waitForPageToLoad();
            assertThat.elementPresentByTimeout(getAssertionSelector(), WAIT_TIME);
            client.click("jquery=form.aui :submit", true);
            client.waitForPageToLoad();
        }

        public String getKeyboardCommand()
        {
            return key;
        }
    }

    private class WatchIssue implements IssueAction
    {

        final private String label;


        public WatchIssue(final String label)
        {
            this.label = label;
        }

        public boolean isDeleteAction()
        {
            return false;
        }

        public String getKeyboardCommand()
        {
            return null;
        }

        public String getThanksMessageRegex()
        {
            return "(MKY|HSP)-[0-9] has been updated.";
        }

        public String getLabel()
        {
            return label;
        }

        public void performAction()
        {
            client.waitForPageToLoad();
            actionsDialog.open();
            actionsDialog.queryActions("S");
            actionsDialog.assertSuggestionPresent(UNWATCH);
            actionsDialog.close();
        }
    }

    private class UnWatchIssue implements IssueAction
    {

        final private String label;

        public UnWatchIssue(final String label)
        {
            this.label = label;
        }

        public boolean isDeleteAction()
        {
            return false;
        }

        public String getKeyboardCommand()
        {
            return null;
        }

        public String getLabel()
        {
            return label;
        }

        public String getThanksMessageRegex()
        {
            return "(MKY|HSP)-[0-9] has been updated.";
        }

        public void performAction()
        {
            client.waitForPageToLoad();
            actionsDialog.open();
            actionsDialog.queryActions("W");
            actionsDialog.assertSuggestionPresent(WATCH);
            actionsDialog.close();
        }
    }

    private class CreateSubTaskLink extends PageLink
    {

        public CreateSubTaskLink(final String label, final String assertionSelector, final String thanksMsgRegex, final String key)
        {
            super(label, assertionSelector, thanksMsgRegex, key);
        }

        public void performAction()
        {
            client.waitForPageToLoad();
            assertThat.elementPresentByTimeout(getAssertionSelector(), WAIT_TIME);
            client.type("jquery=#summary", "test");
            client.click("jquery=#subtask-create-details-submit", true);
        }
    }

    private class CommentIssue extends PageLink
    {
        public CommentIssue(final String label, final String key)
        {
            super(label, "jquery=form#issue-comment-add", "(HSP|MKY)-[0-9]+ has been updated with your comment", key);
        }

        public void performAction()
        {
            waitFor(100);
            if (onViewIssuePage() && inlineCommentDialogOpen())
            {
                handleInlineComment();
            }
            else
            {
                handleDialogComment();
            }
            client.waitForPageToLoad(WAIT_TIME);
        }

        private boolean onViewIssuePage()
        {
            return client.isElementPresent("jquery=form#issue-comment-add");
        }

        private boolean inlineCommentDialogOpen()
        {
            return client.isVisible("jquery=form#issue-comment-add");
        }

        private void handleDialogComment()
        {
            assertThat.visibleByTimeout("jquery=form#comment-add", WAIT_TIME);
            client.typeWithFullKeyEvents("jquery=form#comment-add textarea#comment", "test");
            client.focus("jquery=form#comment-add #comment-add-submit");
            client.click("jquery=form#comment-add #comment-add-submit");
        }

        private void handleInlineComment()
        {
            assertThat.elementVisible("jquery=form#issue-comment-add");
            client.typeWithFullKeyEvents("jquery=form#issue-comment-add textarea#comment", "test");
            client.click("jquery=form#issue-comment-add #issue-comment-add-submit");
        }
    }

    private class PageLinkThatDoesNotReturnToPage extends PageLink
    {

        public PageLinkThatDoesNotReturnToPage(final String label, final String assertionSelector, final String thanksMsgRegex, final String key)
        {
            super(label, assertionSelector, thanksMsgRegex, key);
        }

        public void performAction()
        {
            assertThat.elementPresentByTimeout(getAssertionSelector(), WAIT_TIME);
            client.goBack();
            client.waitForPageToLoad();
            waitFor(1000);
        }
    }

    private class CloseIssue extends Dialog
    {

        public CloseIssue(final String label, final String title, final String thanksMsgRegex, final String key)
        {
            super(label, title, thanksMsgRegex, key);
        }

        public boolean isDeleteAction()
        {
            return false;
        }

        public String getKeyboardCommand()
        {
            return null;
        }


        public void performAction()
        {
            super.performAction();

            // now reset so subsequent dialogs (such as log work) still run
            actionsDialog.open();
            actionsDialog.queryActions(transitions.get(REOPEN_ISSUE).getLabel());
            actionsDialog.selectSuggestionUsingClick();
            transitions.get(REOPEN_ISSUE).performAction();
        }
    }

    private class StartProgress implements IssueAction
    {

        final private String label;

        public StartProgress(final String label)
        {
            this.label = label;
        }

        public String getKeyboardCommand()
        {
            return null;
        }

        public String getLabel()
        {
            return label;
        }

        public String getThanksMessageRegex()
        {
            return "(MKY|HSP)-[0-9] has been updated.";
        }

        public boolean isDeleteAction()
        {
            return false;
        }

        public void performAction()
        {
            client.waitForPageToLoad();
            actionsDialog.open();
            actionsDialog.queryActions("S");
            actionsDialog.assertSuggestionPresent("Stop Progress");
            actionsDialog.close();
        }
    }

    private class StopProgress implements IssueAction
    {

        final private String label;

        public StopProgress(final String label)
        {
            this.label = label;
        }

        public String getKeyboardCommand()
        {
            return null;
        }

        public String getLabel()
        {
            return label;
        }

        public String getThanksMessageRegex()
        {
            return "(MKY|HSP)-[0-9] has been updated.";
        }

        public boolean isDeleteAction()
        {
            return false;
        }

        public void performAction()
        {
            client.waitForPageToLoad(10000000);
            actionsDialog.open();
            actionsDialog.queryActions("S");
            actionsDialog.assertSuggestionPresent("Start Progress");
        }
    }

    private class Dialog implements IssueAction
    {

        private final String JQUERY_AUI_POPUP_HEADING_CONTAINS = "jquery=.aui-popup-heading:visible:contains";
        private final String label;
        private final String title;
        private final String thanksMsgRegex;
        private final String key;
        private boolean isDeleteAction = false;

        Dialog(final String label, final String title, final String thanksMsgRegex, final String key)
        {
            this.label = label;
            this.title = title;
            this.thanksMsgRegex = thanksMsgRegex;
            this.key = key;

            if (label != null && label.equals(DELETE))
            {
                this.isDeleteAction = true;
            }
        }

        public String getLabel()
        {
            return label;
        }

        public String getTitle()
        {
            return title;
        }

        public String getThanksMessageRegex()
        {
            return thanksMsgRegex;
        }

        public boolean isDeleteAction()
        {
            return isDeleteAction;
        }

        public void performAction()
        {
            _assertTitleContains(title);
            client.click(JQUERY_SUBMIT, true);
        }

        private void _assertTitleContains(final String text)
        {
            assertThat.elementPresentByTimeout(JQUERY_AUI_POPUP_HEADING_CONTAINS + "(" + text + ")", WAIT_TIME);
        }

        void assertIsOpen()
        {
            _assertTitleContains(title);
        }

        void close()
        {
            // close dropdown
            client.keyDown(JQUERY_BODY, ESCAPE_KEY);
            // close dialog
            client.keyDown(JQUERY_BODY, ESCAPE_KEY);
            assertThat.elementNotPresentByTimeout(JQUERY_AUI_POPUP);
        }

        public String getKeyboardCommand()
        {
            return key;
        }
    }

    private class LogWorkDialog extends Dialog
    {
        LogWorkDialog()
        {
            super("Log Work", "Log Work", "Work has been logged on (MKY|HSP)-[0-9].", null);
        }

        public void performAction()
        {
            assertIsOpen();
            assertThat.elementPresentByTimeout("jquery=#log-work-time-logged");
            client.type("jquery=#log-work-time-logged", "10m");
            super.performAction();
        }
    }

    private class EditIssueLabelsDialog extends Dialog
    {
        private static final String DIALOG_SELECTOR = VISIBLE_DIALOG_CONTENT_SELECTOR;
        private static final String FORM_SELECTOR = DIALOG_SELECTOR + " form.edit-labels";
        private static final String TEXTAREA_SELECTOR = FORM_SELECTOR + " textarea";

        EditIssueLabelsDialog()
        {
            super("Edit Labels", "Labels", "The labels on (MKY|HSP)-[0-9] have been updated.", "l");
        }

        public void performAction()
        {
            assertIsOpen();
            client.typeWithFullKeyEvents(TEXTAREA_SELECTOR, "label");
            SeleniumLocators.jQuery(TEXTAREA_SELECTOR,context()).element().type(SpecialKeys.ENTER);
            assertIsOpen();
            close();
        }
    }

    private class LinkIssueDialog extends Dialog
    {
        private static final String JQUERY_LINK_KEY_TEXTAREA = "jquery=#linkKey-textarea";

        LinkIssueDialog()
        {
            super("Link", "Link Issue", "The links on (HSP|MKY)-[0-9]+ have been updated", null);
        }

        public void performAction()
        {
            assertIsOpen();
            assertThat.elementPresentByTimeout(JQUERY_LINK_KEY_TEXTAREA, DROP_DOWN_WAIT);
            client.keyPress(JQUERY_LINK_KEY_TEXTAREA, "m");
            client.keyPress(JQUERY_LINK_KEY_TEXTAREA, "k");
            client.keyPress(JQUERY_LINK_KEY_TEXTAREA, "y");
            assertThat.elementPresentByTimeout("jquery=#linkKey-suggestions li.active:contains('MKY-')", 30000); // wait for ajax to come back
            client.click("jquery=#linkKey-suggestions li.active a");
            super.performAction();
        }
    }

    private class AttachFileDialog extends Dialog
    {
        AttachFileDialog()
        {
            super("Attach File", "Attach Files", "The files(s) have been attached to (HSP|MKY)-[0-9]+.", null);
        }

        public void performAction()
        {
            assertIsOpen();
            // cannot add files with selenium, so bailing
            closeDialogByClickingCancel();
        }
    }

    private class AssignDialog extends Dialog
    {
        AssignDialog(final String label, final String title, final String key)
        {
            super(label, title, "(HSP|MKY)-[0-9]+ has been assigned.", key);
        }

        @Override
        public void performAction()
        {
            assertIsOpen();
            client.selectOption("jquery=#assign-issue #assignee", "- Automatic -");
            super.performAction();
        }
    }

}
