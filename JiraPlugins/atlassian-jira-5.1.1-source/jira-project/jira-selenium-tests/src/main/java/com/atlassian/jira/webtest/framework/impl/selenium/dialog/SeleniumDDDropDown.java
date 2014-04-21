package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.component.AbstractSeleniumDropdown;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.model.IssueOperation;

/**
 * Implementation of {@link com.atlassian.jira.webtest.framework.component.AjsDropdown} for the
 * {@link SeleniumDotDialog}.
 *
 * @since v4.3
 */
public class SeleniumDDDropDown extends AbstractSeleniumDropdown<DotDialog> implements DotDialog.DDDropDown
{
    private static final String ID = "issueactions-suggestions";

    /**
     * Requires a unique id of the dropdown list used to evaluate its presence on the page.
     *
     * @param parent parent component of this dropdown
     * @param ctx Selenium context
     */
    protected SeleniumDDDropDown(DotDialog parent, SeleniumContext ctx)
    {
        super(ID, parent, ctx);
    }

    @Override
    protected TimedCondition isOpenableByContext()
    {
        return parent().isOpen();
    }

    @Override
    public TimedQuery<Item<DotDialog>> findFor(IssueOperation issueOperation)
    {
        return item(issueOperation.uiName());
    }

    
    @Override
    public AjsDropdown open()
    {
        if (isOpen().byDefaultTimeout())
        {
            throw new IllegalStateException("Already open");
        }
        return parent().input().arrowDown();
    }
}
