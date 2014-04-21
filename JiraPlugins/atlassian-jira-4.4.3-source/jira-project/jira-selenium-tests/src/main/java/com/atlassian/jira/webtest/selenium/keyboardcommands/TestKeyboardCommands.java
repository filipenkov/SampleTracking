package com.atlassian.jira.webtest.selenium.keyboardcommands;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.Quarantine;
import com.atlassian.jira.webtest.selenium.framework.dialogs.GenericDialog;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import com.thoughtworks.selenium.SeleniumException;

import java.util.Map;

/**
 * Tests keyboard commands in JIRA.
 *
 * @since v4.1
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
@Quarantine
public class TestKeyboardCommands extends JiraSeleniumTest
{

    private static final long TIMEOUT = 30000;
    private static final long NOT_PRESENT_TIMEOUT = 5000;
    private static final String CREATE_ISSUE_DIALOG_SELECTOR = "jquery=#inline-dialog-create_issue_popup.active";
    private static final String BODY_SELECTOR = "css=body";
    private static final String QUICK_FIND_SELECTOR = "quickfind";
    private static final String GADGET_SELECTOR = "gadget-10011";
    private static final String GADGET_CONFIGURE_SELECTOR = "jquery=#gadget-10011-chrome .configure";
    private static final String QUICKSEARCH_LABEL_SELECTOR = "css=#quickSearchInput .overlabel-apply";
    private static final String QUICK_SEARCH_SELECTOR = "quickSearchInput";
    private static final String SHORTCUTS_MENU_SELECTOR = "css=#shortcutsmenu";
    private static final String QUICKSEARCH_LABEL_SHOWN_SELECTOR = "jquery=#quicksearch .overlabel-apply.show";

    private static final String DIALOG_SELECTOR = "jquery=.aui-dialog-open";

    // Do a sample
    private static final Map<Shortcuts,String> KEYS_LIST = MapBuilder.<Shortcuts,String>newBuilder()
            .add(Shortcuts.COMMENT,"jquery=#issue-comment-add:visible")
            .add(Shortcuts.CREATE,"jquery=#issue-create-quick")
            .add(Shortcuts.ASSIGN, DIALOG_SELECTOR)
            .toImmutableMap();

    private static class Dashboard
    {
        private final static String[] PAGES = { "browse_link", "find_link"};
        private final static String TABID = "home_link";
    }

    private static class Projects
    {
        private final static String[] PAGES = { "home_link", "find_link" };
        private final static String TABID = "browse_link";
    }

    private static class Navigator
    {
        private final static String[] PAGES = { "home_link", "find_link" };
        private final static String TABID = "find_link";
    }

    private GenericDialog genericDialog;
    private Locator searchFieldLocator;
    private Locator quickFindLocator;

    public void onSetUp()
    {
        super.onSetUp();

        restoreData("keyboardcommands.xml");
        this.genericDialog = new GenericDialog(context());
        this.searchFieldLocator = SeleniumLocators.id(QUICK_SEARCH_SELECTOR, context());
        this.quickFindLocator = SeleniumLocators.id(QUICK_FIND_SELECTOR, context());
    }

    public void testAll()
    {
        _testShortcutsDontWorkWithEmptySearch();
        _testHelpLink();
        _testNextAndPreviousIssue();
        _testPrimaryNav();
        _testCreateIssue();
        _testQuickSearch();
        _testCommentIssue();
        _testEscBlursFields();
        _testKeyboardTitleShortcuts();
        _testKeyboardCommandsCanBeDisabled();
    }

    public void testHelpMenu()
    {
        getNavigator().currentDashboard();
        _testHelpMenu();
        getNavigator().gotoFindIssues();
        _testHelpMenu();
        getNavigator().findAllIssues();
        _testHelpMenu();
        getNavigator().gotoAdmin();
        _testHelpMenu();
        getNavigator().gotoIssue("HSP-1");
        _testHelpMenu();
        getNavigator().browseProject("HSP");
        _testHelpMenu();
    }

    private void _testShortcutsDontWorkWithEmptySearch()
    {
        //goto the simple view and create a new filter so no issues will be shown
        getNavigator().gotoFindIssuesSimple();
        if(client.isElementPresent("id=new_filter"))
        {
            client.click("id=new_filter", true);
        }

        context().ui().pressInBody(Shortcuts.ASSIGN);
        genericDialog.assertNotOpen();
        context().ui().pressInBody(Shortcuts.COMMENT);
        genericDialog.assertNotOpen();
        context().ui().pressInBody(Shortcuts.LABEL);
        genericDialog.assertNotOpen();
        context().ui().pressInBody(Shortcuts.EDIT);
        assertThat.textPresent("Issue Navigator");
        assertThat.textNotPresent("Edit Issue");
        context().ui().pressInBody(Shortcuts.DOT_DIALOG);
        genericDialog.assertNotOpen();
    }

    private void _testHelpLink()
    {
        client.click("jquery=#header-details-user .aui-dd-link");
        assertThat.visibleByTimeout("keyshortscuthelp", TIMEOUT);

        client.click("jquery=#keyshortscuthelp");
        assertThat.visibleByTimeout("shortcutsmenu", TIMEOUT);
    }

    private void _testNextAndPreviousIssue()
    {
        int issueLength = 6;
        getNavigator().findAllIssues();
        getNavigator().gotoIssue("HSP-" + issueLength);
        for (int i = issueLength; i != 1; i--)
        {
            _testFollowLink(Shortcuts.J_NEXT, "#key-val:contains(" + (i - 1) + ")");
        }
        for (int i = 1; i < issueLength - 1; i++)
        {
            _testFollowLink(Shortcuts.K_PREVIOUS, "#key-val:contains(" + (i + 1) + ")");
        }
    }

    private void _testFollowLink(KeySequence shortcut, String assertjQuerySelector)
    {
        context().ui().pressInBody(shortcut);
        client.waitForPageToLoad();
        assertThat.elementPresentByTimeout("jquery=" + assertjQuerySelector, NOT_PRESENT_TIMEOUT);
    }

    private void _testCommentIssue()
    {
        getNavigator().gotoIssue("HSP-1");
        context().ui().pressInBody(Shortcuts.COMMENT);
        assertThat.visibleByTimeout("comment");
    }

    private void _testKeyboardTitleShortcuts()
    {
        getNavigator().findAllIssues();
        getNavigator().gotoIssue("HSP-4");
        assertThat.attributeContainsValue("home_link", "title", "( Type 'g' then 'd' )");
        assertThat.attributeContainsValue("browse_link", "title", "( Type 'g' then 'p' )");
        assertThat.attributeContainsValue("find_link", "title", "( Type 'g' then 'i' )");
        assertThat.attributeContainsValue("comment-issue", "title", "( Type 'm' )");
        assertThat.attributeContainsValue(QUICK_SEARCH_SELECTOR, "title", "( Type '/' )");
        assertThat.attributeContainsValue("assign-issue", "title", "( Type 'a' )");
        assertThat.attributeContainsValue("editIssue", "title", "( Type 'e' )");
        assertThat.attributeContainsValue("previous-issue", "title", "( Type 'k' )");
        assertThat.attributeContainsValue("next-issue", "title", "( Type 'j' )");
    }

    private void _testHelpMenu()
    {
        try
        {
            //the tests for the occurrance of individual characters probably aren't probably very useful assertions but
            //better than nothing
            client.shiftKeyDown();
            context().ui().pressInBody(Shortcuts.SHORTCUTS_HELP);
            assertThat.elementPresentByTimeout(SHORTCUTS_MENU_SELECTOR, TIMEOUT);
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Global Shortcuts");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Issue Actions");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Navigating Issues");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "g then d");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Go to Dashboard");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "g then p");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Browse to a Project");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "g then i");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Find Issues");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "c");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Create an Issue");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "/");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Quick Search");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "?");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Open shortcut help");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "e");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Edit Issue");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "a");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Assign");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "m");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Comment on Issue");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "k");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Next Issue");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "j");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Previous Issue");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "n");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Next Activity");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "p");
            assertThat.elementContainsText(SHORTCUTS_MENU_SELECTOR, "Previous Activity");
        }
        finally
        {
            client.shiftKeyUp();
        }
    }

    private void _testEscBlursFields()
    {
        client.click(Navigator.TABID);
        client.focus(QUICK_SEARCH_SELECTOR);
        assertThat.elementNotPresentByTimeout(QUICKSEARCH_LABEL_SHOWN_SELECTOR, NOT_PRESENT_TIMEOUT);
        assertThat.elementPresentByTimeout(searchFieldLocator.value(), 5000);
        searchFieldLocator.element().type(SpecialKeys.ESC);
        assertThat.elementPresentByTimeout(QUICKSEARCH_LABEL_SHOWN_SELECTOR, TIMEOUT);
        _testMoveToElem("jquery=#create_link", Shortcuts.CREATE);
    }

    private void _testPrimaryNav()
    {
        runPageTests(Dashboard.PAGES, Shortcuts.GO_TO_DASHBOARD, Dashboard.TABID);
        runPageTests(Projects.PAGES, Shortcuts.GO_TO_PROJECT, Projects.TABID);
        runPageTests(Navigator.PAGES, Shortcuts.GO_TO_NAVIGATOR, Navigator.TABID);

        goToNavItemFromGadget(Shortcuts.GO_TO_PROJECT, Projects.TABID);
        goToNavItemFromGadget(Shortcuts.GO_TO_NAVIGATOR, Navigator.TABID);
    }

    private void _testCreateIssue()
    {
        client.click(Navigator.TABID);
        client.waitForPageToLoad();
        _testMoveToElem("create_link", Shortcuts.CREATE);
        assertThat.elementPresentByTimeout(CREATE_ISSUE_DIALOG_SELECTOR, TIMEOUT);
        getNavigator().gotoIssue("HSP-1");
        context().ui().pressInBody(Shortcuts.CREATE);
        assertThat.elementPresentByTimeout(CREATE_ISSUE_DIALOG_SELECTOR, TIMEOUT);
        client.click(BODY_SELECTOR);
        client.focus("jquery=a:eq(0)");
        assertThat.elementNotPresentByTimeout(CREATE_ISSUE_DIALOG_SELECTOR, NOT_PRESENT_TIMEOUT);
        getNavigator().gotoHome();
        assertThat.elementPresentByTimeout(GADGET_CONFIGURE_SELECTOR, 5000);
        client.click(GADGET_CONFIGURE_SELECTOR);
        client.selectFrame(GADGET_SELECTOR);
        assertThat.elementPresentByTimeout(QUICK_FIND_SELECTOR);
        quickFindLocator.element().type(Shortcuts.CREATE);
        selectTopFrame();
        assertThat.elementNotPresentByTimeout(CREATE_ISSUE_DIALOG_SELECTOR, NOT_PRESENT_TIMEOUT);
        client.selectFrame(GADGET_SELECTOR);
        quickFindLocator.element().type(Keys.ESCAPE);
        context().ui().pressInBody(Shortcuts.CREATE);
        selectTopFrame();
        assertThat.elementPresentByTimeout(CREATE_ISSUE_DIALOG_SELECTOR, TIMEOUT);
    }

    private void selectTopFrame() {client.selectFrame("relative=top");}

    private void _testQuickSearch()
    {
        client.click(Navigator.TABID);
        client.waitForPageToLoad();
        _testMoveToElem(QUICK_SEARCH_SELECTOR, Shortcuts.CREATE);
        assertThat.elementNotVisible(QUICKSEARCH_LABEL_SELECTOR);
        getNavigator().gotoIssue("HSP-1");
        context().ui().pressInBody(Shortcuts.DOT_DIALOG);
        assertThat.elementNotVisible(QUICKSEARCH_LABEL_SELECTOR);
    }

    private void _testMoveToElem(final String elementSelector, KeySequence shortcut)
    {
        getNavigator().gotoIssue("HSP-1");
        client.waitForPageToLoad();
        Number offset = client.getElementPositionTop(elementSelector).intValue() - 5;
        client.runScript("jQuery(window).scrollTop(500)");
        context().ui().pressInBody(shortcut);
        Number scrollOffset = getScrollPostion();
        if (!scrollOffset.equals(offset) && scrollOffset.equals(offset.intValue() + 1))
        {
            throw new RuntimeException("Expected scroll position to be that of the create link");
        }
    }

    public void _testKeyboardCommandsCanBeDisabled()
    {
        getUserPreferences().setKeyboardShortcutsEnabled(false);
        for (Shortcuts theKey : KEYS_LIST.keySet())
        {
            getNavigator().gotoIssue("HSP-1");
            client.waitForPageToLoad();
            context().ui().pressInBody(theKey);
            assertThat.elementNotVisible(KEYS_LIST.get(theKey));
        }
        getUserPreferences().setKeyboardShortcutsEnabled(true);
        for (Shortcuts theKey: KEYS_LIST.keySet())
        {
            getNavigator().gotoIssue("HSP-1");
            client.waitForPageToLoad();
            context().ui().pressInBody(theKey);
            try
            {
                Long beforeAjaxWait = System.currentTimeMillis();
                client.waitForAjaxWithJquery(TIMEOUT);
                Long timeInMilliseconds =  System.currentTimeMillis() - beforeAjaxWait;
                assertTrue("The ajax for key command: " + theKey + " is taking longer than 3 seconds it took "
                        + timeInMilliseconds + " milliseconds", (System.currentTimeMillis()-beforeAjaxWait)<3000);
            }
            catch (SeleniumException se)
            {
                throw new SeleniumException("Ajax wait failed for keycommand: "+theKey,se);
            }
            assertThat.elementPresentByTimeout(KEYS_LIST.get(theKey),TIMEOUT);
        }
    }

    private Number getScrollPostion()
    {
        return Integer.parseInt(client.getEval("this.browserbot.getCurrentWindow().jQuery('html, body').attr(\"scrollTop\")"), 10);
    }

    private void runPageTests(final String[] pages, Shortcuts shortcut, final String tabid)
    {
        boolean focusInput = true;

        for (String page : pages)
        {
            getNavigator().click(page);
            client.waitForPageToLoad();
            if (focusInput)
            {
                String[] clientLocation = client.getAttributeFromAllWindows("location");
                runKeyboardCommandInField(shortcut);
                if (!clientLocation[0].equals(client.getAttributeFromAllWindows("location")[0]))
                {
                    throw new RuntimeException("Expected keyboard command to be ignored when field is focused");
                }
            }
            else
            {
                context().ui().pressInBody(shortcut);
                client.waitForPageToLoad();
                assertThat.elementPresent("css=.selected #" + tabid);
            }
            focusInput = !focusInput;
        }
    }

    private void goToNavItemFromGadget(Shortcuts shortcut, final String tabid)
    {
        getNavigator().gotoHome();
        client.selectFrame(GADGET_SELECTOR);
        context().ui().pressInBody(shortcut);
        client.waitForPageToLoad();
        assertThat.elementPresent("css=.selected #" + tabid);
    }

    private void runKeyboardCommandInField(Shortcuts shortcut)
    {
        searchFieldLocator.element().type(shortcut);
    }

}
