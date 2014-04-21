package com.atlassian.core.action;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.webwork.actions.ActionConfiguration;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.dispatcher.ActionResult;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

public class TestDefaultActionDispatcher extends LegacyJiraMockTestCase
{
    private static final String MOCK_ACTION_CLAZZ = "com.atlassian.core.action.MockAction";
    private Principal principal;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        stubActionConfiguration();
    }

    public TestDefaultActionDispatcher(String s)
    {
        super(s);
        principal = new Principal()
        {
            @Override
            public String getName()
            {
                return "tom";
            }
        };
    }

    public void stubActionConfiguration()
    {
        final ActionConfiguration actionConfiguration = new ActionConfiguration()
        {
            @Override
            public Entry getActionCommand(final String alias)
            {
                return Entry.newBuilder().className(alias).build();
            }
        };
        MockComponentWorker componentWorker = new MockComponentWorker();
        componentWorker.registerMock(ActionConfiguration.class, actionConfiguration);
        ComponentAccessor.initialiseWorker(componentWorker);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        ActionContext.setContext(new ActionContext());
    }

    public void testExecuteWithParams() throws Exception
    {

        ActionContext.setPrincipal(principal);

        Map parameters = new HashMap(1);
        parameters.put("foo", "bar");

        ActionResult result = new DefaultActionDispatcher().execute(MOCK_ACTION_CLAZZ, parameters);

        assertEquals(principal, ActionContext.getContext().getPrincipal());

        // test the result action
        MockAction resultAction = (MockAction) result.getFirstAction();
        assertEquals(Action.SUCCESS, result.getResult());
        assertEquals("bar", resultAction.getFoo());

    }

    public void testExecute() throws Exception
    {
        ActionResult result = new DefaultActionDispatcher().execute(MOCK_ACTION_CLAZZ);

        assertNull(ActionContext.getContext().getPrincipal());

        // test the result action
        MockAction resultAction = (MockAction) result.getFirstAction();
        assertEquals(Action.SUCCESS, result.getResult());
        assertEquals(null, resultAction.getFoo());
    }
}