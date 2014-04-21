package com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.AbstractSeleniumAuiDialog;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract Selenium implementation of {@link com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog}.
 *
 * @since v4.3
 */
public abstract class AbstractIssueActionDialog<T extends IssueActionDialog<T>> extends AbstractSeleniumAuiDialog<T>
    implements IssueActionDialog<T>
{

    protected final IssueActionDialogOpener opener;
    protected final IssueActionsParent parent;
    private final Class<T> targetType; 

    /**
     * Subclasses must provide unique ID for given dialog type. This id is used as an identifier of the div element serving
     * as a container of the dialog.
     *
     * @param parent parent object of this dialog
     * @param context current selenium test context
     * @param dialogId unique HTML id of the dialog container div
     * @param opener dialog opener
     * @param targetType target dialog type
     */
    protected AbstractIssueActionDialog(IssueActionsParent parent, SeleniumContext context, String dialogId, IssueActionDialogOpener opener, Class<T> targetType)
    {
        super(context, dialogId);
        this.parent = notNull("parent", parent);
        this.opener = notNull("opener", opener);
        this.targetType = notNull("targetType", targetType);
    }


    @Override
    protected TimedCondition isOpenableInContext()
    {
        return parent.isAt();
    }

    @Override
    public IssueData issueData()
    {
        return parent.issueData();
    }

    @Override
    public final T open()
    {
        opener.open(action());
        return targetType.cast(this);
    }
}
