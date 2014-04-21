package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.SeleniumDotDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.SeleniumAbstractGlobalPage;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueTable;
import com.atlassian.jira.webtest.framework.page.issuenavigator.SimpleSearchFilter;

import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * Selenium implementation of the {@link com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator} interface.
 *
 * @since v4.3
 */
public class SeleniumIssueNav extends SeleniumAbstractGlobalPage<IssueNavigator> implements IssueNavigator
{
    private static final String DETECTOR = "section#content header h1:contains(Issue Navigator)";
    private static final String LINK_ID = "find_link";
    private static final String SWITCH_MODE_ID = "filter-switch";
    private static final String SWITCH_MODE_LINK_ID = "switchnavtype";

    private final SeleniumLocator detector;
    private final SeleniumLocator linkLocator;
    private final SeleniumLocator switchModeContainerLocator;
    private final SeleniumLocator switchModeLinkLocator;

    private final SeleniumIssueTable results;
    private final SeleniumSimpleSearchFilter simpleSearch;
    private final IssueNavDD dotDialog;
    private final IssueData issueData;

    public SeleniumIssueNav(SeleniumContext ctx)
    {
        super(IssueNavigator.class, ctx);
        this.detector = jQuery(DETECTOR);
        this.linkLocator = id(LINK_ID);
        this.switchModeContainerLocator = id(SWITCH_MODE_ID);
        this.switchModeLinkLocator = id(SWITCH_MODE_LINK_ID);
        this.results = new SeleniumIssueTable(this, context);
        this.simpleSearch = new SeleniumSimpleSearchFilter(this, context);
        this.dotDialog = new IssueNavDD(context);
        this.issueData = new INIssueData();
    }

    /* ----------------------------------------------- QUERIES ------------------------------------------------------ */

    @Override
    public TimedCondition isSimpleMode()
    {
        return and(isAt(), switchModeContainerLocator.element().containsText("Switch to advanced searching"));
    }

    @Override
    public TimedCondition isAdvancedMode()
    {
        return and(isAt(), switchModeContainerLocator.element().containsText("Switch to simple searching"));
    }

    @Override
    public IssueData issueData()
    {
        return issueData;
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected SeleniumLocator detector()
    {
        return detector;
    }


    @Override
    protected SeleniumLocator linkLocator()
    {
        return linkLocator;
    }

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    @Override
    public IssueTable results()
    {
        return results;
    }

    @Override
    public SimpleSearchFilter simpleSearch()
    {
        return simpleSearch;
    }

    @Override
    public <D extends IssueActionDialog<D>> D dialog(Class<D> dialogType)
    {
        throw new UnsupportedOperationException("yet");
    }

    @Override
    public DotDialog dotDialog()
    {
        return dotDialog;
    }


    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    @Override
    public IssueNavigator toSimpleMode()
    {
        if (!isSimpleMode().now())
        {
            switchModeLinkLocator.element().click();
            waitFor().pageLoad();
        }
        return this;
    }

    @Override
    public IssueNavigator toAdvancedMode()
    {
        if (!isAdvancedMode().now())
        {
            switchModeLinkLocator.element().click();
            waitFor().pageLoad();
        }
        return this;
    }

    @Override
    public DotDialog openDotDialog()
    {
        context().ui().pressInBody(Shortcuts.DOT_DIALOG);
        return dotDialog;
    }


    @Override
    public <D extends IssueActionDialog<D>> DialogOpenMode<D> openDialog(Class<D> dialogType)
    {
        throw new UnsupportedOperationException("yet");
    }


    /* ------------------------------------------------ STUFF ------------------------------------------------------- */

    private class IssueNavDD extends SeleniumDotDialog implements DotDialog
    {
        public IssueNavDD(SeleniumContext context)
        {
            super(context);
        }

        @Override
        public IssueData issueData()
        {
            return results().selectedRow().now().issueData();
        }

        @Override
        public IssueActionsParent parentPage()
        {
            return SeleniumIssueNav.this;
        }

    }

    private class INIssueData implements IssueData
    {
        @Override
        public long id()
        {
            return results().selectedRow().now().issueData().id();
        }

        @Override
        public String key()
        {
            return results().selectedRow().now().issueData().key();
        }
    }

}
