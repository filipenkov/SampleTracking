package com.atlassian.jira.webtest.selenium.framework.model;

import com.atlassian.jira.webtest.framework.model.WorkflowIssueAction;

/**
 * Represents workflow transitions available from view issue / issue navigator pages. Not an enumeration as it
 * it not fixed.
 *
 * @since v4.2
 */
public class WorkflowTransition
{
    public static final WorkflowTransition CLOSE = new WorkflowTransition(2, "Close Issue", ActionType.AJAX);
    public static final WorkflowTransition REOPEN = new WorkflowTransition(3, "Reopen Issue", ActionType.AJAX);
    public static final WorkflowTransition RESOLVE = new WorkflowTransition(5, "Resolve Issue", ActionType.AJAX);

    private static final String ACTIONS_MENU_LOCATOR_FORMAT = "jquery=div#actions_%d_drop a:contains('%s')";

    private final int id;
    private final String name;
    private final ActionType actionType;

    public WorkflowTransition(final int id, final String name, final ActionType actionType)
    {
        this.id = id;
        this.name = name;
        this.actionType = actionType;
    }

    public WorkflowTransition(final int id, final String name)
    {
        this(id, name, ActionType.AJAX);
    }

    public int id()
    {
        return id;
    }

    public String actionName()
    {
        return name;
    }

    public ActionType actionType()
    {
        return actionType;
    }

    public String viewIssueLinkLocator()
    {
        return "id=action_id_" + id;
    }

    public String cogActionLocator(int issueId)
    {
        return String.format(ACTIONS_MENU_LOCATOR_FORMAT, issueId, name);
    }

    public LegacyIssueOperation toOperation()
    {
        return new LegacyIssueOperation(new WorkflowIssueAction(id, name));
    }
}
