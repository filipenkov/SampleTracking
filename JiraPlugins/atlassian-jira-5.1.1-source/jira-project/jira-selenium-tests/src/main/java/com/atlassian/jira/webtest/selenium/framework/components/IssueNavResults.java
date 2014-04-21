package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.dialogs.ActionsDialog;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import com.atlassian.jira.webtest.selenium.framework.model.WorkflowTransition;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.thoughtworks.selenium.SeleniumException;
import junit.framework.Assert;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.JQUERY;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.removeLocatorPrefix;

/**
 * Represents issue navigator results in the Selenium World&trade;
 *
 * @since v4.2
 */
public final class IssueNavResults extends AbstractSeleniumPageObject
{
    public static final String LOCATOR = JQUERY.create("#issuetable");

    private static final String ACTION_TRIGGER_ID_TEMPLATE = asString("actions_%d");

    private static String actionTriggerIdFor(long issueId)
    {
        return String.format(ACTION_TRIGGER_ID_TEMPLATE, issueId);
    }

    // TODO merge this into IssueOperation enumeration so that we have one common representation of issue operations in
    // TODO view issue and issue nav 
    public static enum IssueNavAction
    {
        EDIT("jquery=li.aui-list-item a.aui-list-item-link.issueaction-edit-issue", "Edit", ActionType.JAVASCRIPT, Shortcuts.EDIT),
        RESOLVE(workflowTransitionCogLocatorFor(WorkflowTransition.RESOLVE), WorkflowTransition.RESOLVE.actionName(), ActionType.JAVASCRIPT);


        static String workflowTransitionCogLocatorFor(WorkflowTransition transition)
        {
            return String.format("jquery=li.aui-list-item a.aui-list-item-link.issueaction-workflow-transition[rel=%d]", transition.id()); 
        }

        private final String linkLocator;
        private final String queryName;
        private final KeySequence shortcut;
        private final ActionType actionType;

        IssueNavAction(final String linkLocator, final String queryName, ActionType at, final KeySequence shortcut)
        {
            this.linkLocator = linkLocator;
            this.queryName = queryName;
            this.actionType = at;
            this.shortcut = shortcut;
        }

        IssueNavAction(final String linkId, final String queryName, ActionType at)
        {
            this(linkId, queryName, at, null);
        }

        boolean hasShortcut()
        {
            return shortcut != null;
        }

        KeySequence shortcut()
        {
            if (!hasShortcut())
            {
                throw new AssertionError("No shortcut for me: " + this);
            }
            return shortcut;
        }

        String linkLocator()
        {
            return linkLocator;
        }

        String queryName()
        {
            return queryName;
        }
    }

    /**
     * Represents action over a single issue executable from the issue navigator UI.
     *
     */
    public final class SelectedIssue
    {
        SelectedIssue() {}

        // TODO might return ViewIssue object in the future
        public void view()
        {
            context.ui().pressInBody(Keys.ENTER);
            try
            {
                client.waitForPageToLoad();
            }
            catch(SeleniumException ignored)
            {
                //Sometimes Selenium throws an erroneous selenium exception here (IE?).
                // Ignoring this error.
            }
        }

        // ideally the execute methods would return target object (page,dialog etc.)
        // for this we need custom, generics-enabled enums

        public void executeFromActionsDialog(IssueNavAction action)
        {
            actionsDialog.open();
            actionsDialog.queryActions(action.queryName());
            actionsDialog.selectActionUsingEnter();
            action.actionType.waitForAction(context);
        }

        public void executeFromShortcut(IssueNavAction action)
        {
            context.ui().pressInBody(action.shortcut());
            action.actionType.waitForAction(context);
        }

        public void executeFromCog(IssueNavAction action)
        {
            AjsDropdown currentCog = new AjsDropdown(context, actionTriggerIdFor(selectedIssueId()));
            currentCog.open().byClick().assertIsOpen(context.timeouts().components());
            // clicking using jquery locator doesn't work, and Selenium doesn't know whyyy-y
            client.click(Locators.CSS.create(currentCog.inDropdown(action.linkLocator())));
            action.actionType.waitForAction(context);
        }


    }

    private final SelectedIssue selectedIssue;
    private final ActionsDialog actionsDialog;

    private final IsPresentCondition bodyPresentCondition;

    public IssueNavResults(SeleniumContext ctx)
    {
        super(ctx);
        this.selectedIssue = new SelectedIssue();
        this.actionsDialog = new ActionsDialog(context);
        this.bodyPresentCondition = new IsPresentCondition(context, Locators.Common.BODY);
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    public String inResults(String jQueryLocator)
    {
        return LOCATOR + " " + removeLocatorPrefix(jQueryLocator);
    }

    public String inSelectedRow(String jQueryLocator)
    {
        return currentSelectedIssueRowLocator() + " " + removeLocatorPrefix(jQueryLocator);
    }

    public String currentSelectedIssueRowLocator()
    {
        return inResults("tr.issuerow.focused");
    }

    public String selectedIssueRowLocator(String issueKey)
    {
        return inResults("tr.issuerow.focused td.nav.issuekey a:contains(" + issueKey + ")");
    }

    public String selectedIssueRowLocator(long issueId)
    {
        return inResults("tr#issuerow" + issueId + ".issuerow.focused");
    }

    public String issueRowLocatorByKey(String issueKey)
    {
        return inResults("tr.issuerow td.nav.issuekey a:contains(" + issueKey + ")");
    }

    public String issueRowLocatorById(long issueId)
    {
        return inResults("tr#issuerow" + issueId);
    }

    /**
     * Locator for issue at given <tt>position</tt> in the issue nav results table (1-based)
     *
     * @param position 1-based issue position ordinal
     * @return locator for issue at given <tt>position</tt>
     */
    public String issueRowLocatorByPosition(int position)
    {
        return inResults("tr:nth(" + position + ")");
    }

    /* ---------------------------------------------- ACTIONS ------------------------------------------------------- */

    public IssueNavResults up()
    {
        return up(1);
    }

    public IssueNavResults down()
    {
        return down(1);
    }

    /**
     * Press up arrow a certain number of times.
     */
    public IssueNavResults up(int times)
    {
        return press(Shortcuts.K_PREVIOUS, times);
    }

    /**
     * Press down arrow a certain number of times.
     */
    public IssueNavResults down(int times)
    {
        return press(Shortcuts.J_NEXT, times);
    }

    private IssueNavResults press(KeySequence keys, int times)
    {
        waitForBody();
        for (int i = 0; i < times; i++)
        {
            context.ui().pressInBody(keys);
        }
        return this;
    }

    private void waitForBody()
    {
        Assert.assertTrue("HTML body not present", bodyPresentCondition.byDefaultTimeout());
    }

    public IssueNavResults selectFirstIssue()
    {
        long firstIssueId = issueIdFor(1);
        int count = 0;
        while (!isIssueSelectedWith(firstIssueId))
        {
            up();
            count = dontTryTooHard(count);
        }
        return this;
    }

    public IssueNavResults selectIssue(String issueKey)
    {
        assertHasIssue(issueKey);
        selectFirstIssue();
        int count = 0;
        while (!isIssueSelectedWith(issueKey))
        {
            down();
            count = dontTryTooHard(count);
        }
        return this;
    }

    private int dontTryTooHard(int count)
    {
        if (count > 50)
        {
            throw new AssertionError("Tried waaay too hard");
        }
        return count + 1;
    }

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    public SelectedIssue selectedIssue()
    {
        assertAnyIssueSelected();
        return selectedIssue;
    }

    /* ---------------------------------------------- QUERIES ------------------------------------------------------- */

    public String selectedIssueKey()
    {
        assertAnyIssueSelected();
        return client.getAttribute(currentSelectedIssueRowLocator() + "@data-issuekey").trim();
    }

    public long selectedIssueId()
    {
        assertAnyIssueSelected();
        return Long.valueOf(client.getAttribute(currentSelectedIssueRowLocator() + "@rel").trim());
    }

    public long issueIdFor(int position)
    {
        return Long.valueOf(client.getAttribute(issueRowLocatorByPosition(position) + "@rel").trim());
    }

    public boolean isAnyIssueSelected()
    {
        return client.isElementPresent(currentSelectedIssueRowLocator());
    }

    public boolean isIssueSelectedWith(String issueKey)
    {
        return client.isElementPresent(selectedIssueRowLocator(issueKey));
    }

    public boolean isIssueSelectedWith(long issueId)
    {
        return client.isElementPresent(selectedIssueRowLocator(issueId));
    }

    public boolean isIssueSelectedAt(int position)
    {
        return isIssueSelectedWith(issueIdFor(position));
    }

    /* --------------------------------------------- ASSERTIONS ----------------------------------------------------- */

    /**
     * Assert that any issue row is currently selected.
     *
     */
    public void assertAnyIssueSelected()
    {
        assertThat.elementPresentByTimeout(currentSelectedIssueRowLocator());
    }


    /**
     * Assert that any issue with given <tt>issueKey</tt> is currently selected.
     *
     * @param issueKey key of the issue to check
     */
    public void assertIssueSelectedWith(String issueKey)
    {
        assertThat.elementPresentByTimeout(selectedIssueRowLocator(issueKey));
    }

    /**
     * Assert that any issue with given <tt>issueId</tt> is currently selected.
     *
     * @param issueId ID of the issue to check
     */
    public void assertIssueSelectedWith(long issueId)
    {
        assertThat.elementPresentByTimeout(selectedIssueRowLocator(issueId));
    }


    public void assertIssueSelectedAt(int position)
    {
        assertThat.elementPresentByTimeout(selectedIssueRowLocator(issueIdFor(position)));
    }

    /**
     * Assert that issue identified by <tt>issueKey</tt> is contained in this results page.
     *
     * @param issueKey key of the issue to check
     */
    public void assertHasIssue(String issueKey)
    {
        assertThat.elementPresentByTimeout(issueRowLocatorByKey(issueKey));
    }

    /**
     * Assert that issue identified by <tt>issueId</tt> is contained in this results page.
     *
     * @param issueId ID of the issue to check
     */
    public void assertHasIssue(long issueId)
    {
        assertThat.elementPresentByTimeout(issueRowLocatorById(issueId));
    }
}
