package com.atlassian.jira.webtest.framework.impl.selenium.page.issuenavigator;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.Queries;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.AttributesMatchCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPageSection;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.model.SimpleIssueData;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueNavigator;
import com.atlassian.jira.webtest.framework.page.issuenavigator.IssueTable;
import com.atlassian.webtest.ui.keys.SpecialKeys;

import static com.atlassian.jira.webtest.framework.core.QueryAssertions.isEqual;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.webtest.ui.keys.Sequences.chars;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issuenavigator.IssueTable} for the issue navigator.
 *
 * @since v4.3
 */
class SeleniumIssueTable extends AbstractSeleniumPageSection<IssueNavigator> implements IssueTable
{
    private static final String DETECTOR = "issuetable";
    private static final String SELECTED_ROW = "tr.issuerow.focused";

    private static final String ID_HOLDER = "rel";
    private static final String ISSUE_KEY_HOLDER = "data-issuekey";

    private final SeleniumLocator detector;
    private final SeleniumLocator selectedRowLocator;

    private final IssueRow currentRow;

    SeleniumIssueTable(IssueNavigator page, SeleniumContext context)
    {
        super(page, context);
        detector = id(DETECTOR);
        selectedRowLocator = detector.combine(jQuery(SELECTED_ROW));
        currentRow = new SeleniumIssueRow();
    }

    /* ----------------------------------------------- LOCATORS ----------------------------------------------------- */

    @Override
    public Locator selectedRowLocator()
    {
        return selectedRowLocator;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return detector;
    }

    /* ------------------------------------------------ QUERIES ----------------------------------------------------- */
    
    @Override
    public TimedCondition hasSelectedRow()
    {
        return selectedRowLocator.element().isPresent();
    }

    @Override
    public TimedCondition isSelected(long issueId)
    {
        return AttributesMatchCondition.forContext(context).locator(selectedRowLocator)
                .expected(ID_HOLDER, Long.toString(issueId)).build();
    }

    @Override
    public TimedCondition isSelected(String issueKey)
    {
        return AttributesMatchCondition.forContext(context).locator(selectedRowLocator)
                .expected(ISSUE_KEY_HOLDER, issueKey).build();
    }

    @Override
    public TimedQuery<IssueRow> selectedRow()
    {
        return Queries.conditionalQuery(currentRow, hasSelectedRow()).expirationHandler(ExpirationHandler.THROW_ILLEGAL_STATE)
                .build();
    }

    private IssueData currentIssueData()
    {
        String id = selectedRowLocator.element().attribute(ID_HOLDER).byDefaultTimeout();
        String key = selectedRowLocator.element().attribute(ISSUE_KEY_HOLDER).byDefaultTimeout();
        return new SimpleIssueData(Long.parseLong(id), key);
    }

    /* ------------------------------------------------ ACTIONS ----------------------------------------------------- */

    @Override
    public IssueTable up()
    {
        body().element().type(SpecialKeys.ARROW_UP);
        return this;
    }

    @Override
    public IssueTable down()
    {
        body().element().type(SpecialKeys.ARROW_DOWN);
        return this;
    }

    /* ------------------------------------------------ STUFF ------------------------------------------------------- */

    /**
     * Proxies IssueTable's current issue data logic so that we don't need new instance per
     * each call to <tt>selectedRow</tt>.
     *
     */
    private class SeleniumIssueRow implements IssueTable.IssueRow
    {

        @Override
        public IssueData issueData()
        {
            return currentIssueData();
        }

        @Override
        public void execute(IssueOperation issueOperation)
        {
            // uses dot dialog
            DotDialog dd = page().openDotDialog().input().type(chars(issueOperation.uiName())).parent();
            assertThat(dd.dropDown().itemCount(), isEqual(1).byDefaultTimeout());
            dd.close().byEnter();
        }
    }
}
