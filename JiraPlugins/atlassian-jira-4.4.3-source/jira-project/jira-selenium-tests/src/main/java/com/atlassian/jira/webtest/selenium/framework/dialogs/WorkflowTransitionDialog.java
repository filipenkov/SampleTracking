package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.fields.DateFieldWithCalendar;
import com.atlassian.jira.webtest.selenium.framework.model.ActionType;
import com.atlassian.jira.webtest.selenium.framework.model.WorkflowTransition;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generic workflow transition dialog implementation. Needs workflow acton id to open up from the View Isssue
 * page and workflow action name to open up from the Issue Navigator and actions dialog.
 *
 * @since v4.2
 */
public class WorkflowTransitionDialog extends AbstractIssueDialog<WorkflowTransitionDialog> implements IssueActionDialog
{
    private final WorkflowTransition workflow;

    public WorkflowTransitionDialog(SeleniumContext ctx, final WorkflowTransition workflow)
    {
        super(notNull("workflow",workflow).toOperation(), WorkflowTransitionDialog.class, ActionType.NEW_PAGE, ctx);
        this.workflow = workflow;
    }

    public DateFieldWithCalendar getDateTimePicker(long fieldId)
    {
        return DateFieldWithCalendar.createForDateTimeCustomField(context, fieldId);
    }

    public DateFieldWithCalendar getDatePicker(long fieldId)
    {
        return DateFieldWithCalendar.createForDateCustomField(context, fieldId);
    }
}
