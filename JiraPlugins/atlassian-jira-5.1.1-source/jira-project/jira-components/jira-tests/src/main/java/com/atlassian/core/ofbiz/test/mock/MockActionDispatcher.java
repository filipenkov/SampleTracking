package com.atlassian.core.ofbiz.test.mock;

import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.action.DefaultActionDispatcher;
import webwork.action.Action;
import webwork.dispatcher.ActionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This was taken from atlassian-ofbiz and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class MockActionDispatcher implements ActionDispatcher
{
    private String result;
    private List resultActions;

    private final List actionsCalled = new ArrayList();
    private final List parametersCalled = new ArrayList();
    private final Collection calls = new ArrayList();
    private final boolean delegating;
    private final ActionDispatcher decoratedDispatcher;

    public MockActionDispatcher(final boolean delegating)
    {
        this.delegating = delegating;
        decoratedDispatcher = delegating ? new DefaultActionDispatcher() : this;
        result = Action.SUCCESS;
    }

    public ActionResult execute(final String actionName) throws Exception
    {
        return execute(actionName, null);
    }

    public ActionResult execute(final String actionName, final Map parameters) throws Exception
    {
        calls.add(new Object[] { "execute", actionName, parameters });
        actionsCalled.add(actionName);
        parametersCalled.add(parameters);

        return (delegating) ? decoratedDispatcher.execute(actionName, parameters) : new ActionResult(result, null, resultActions, null);
    }

    public List getActionsCalled()
    {
        return actionsCalled;
    }

    public List getParametersCalled()
    {
        return parametersCalled;
    }

    public void setResult(final String result)
    {
        this.result = result;
    }

    public void setResultAction(final Action action)
    {
        resultActions = new ArrayList();
        resultActions.add(action);
    }

    public Collection getCalls()
    {
        return calls;
    }
}
