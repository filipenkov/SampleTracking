package com.atlassian.jira.config.webwork;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.action.SafeAction;
import com.atlassian.jira.action.component.ComponentEdit;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.security.xsrf.XsrfFailureException;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import junit.framework.Assert;
import mock.servlet.MockHttpServletRequest;
import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.action.CommandDriven;
import webwork.action.ResultException;

import java.util.List;
import java.util.Map;

/**
 * A integration test for JiraActionFactory.
 *
 * @since v3.13.2
 */
public class TestJiraActionFactory extends LegacyJiraMockTestCase
{
    private static final String COMPONENT_LEAD = "Component Lead";
    private static final String COMPONENT_NAME = "Component Name";
    private static final String SHES_A_MANIAC_CLASS = "com.atlassian.jira.config.webwork.ShesAManiac";

    public static class WebActionImpl implements Action
    {
        private Long id;

        public String execute() throws Exception
        {
            return "webaction";
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        /** @noinspection UnusedDeclaration*/
        public void setUnSafeParameter(List anyoldJunk)
        {
            Assert.fail("this should not have invoked");
        }
    }

    public static class SafeActionImpl implements Action, SafeAction
    {
        boolean setCalled = false;

        public String execute() throws Exception
        {
            return "backendaction";
        }

        /** @noinspection UnusedDeclaration*/
        public void setUnSafeParameter(List anyoldJunk)
        {
            setCalled = true;
        }
    }

    public static class XsrfAction extends JiraWebActionSupport implements CommandDriven
    {
        @RequiresXsrfCheck
        public String doExecute()
        {
            return "ok";
        }
    }

    public void testActionDistinction() throws Exception
    {

        // set up the action context
        setupActionContext(EasyMap.build("unSafeParameter", EasyList.build(), "id", "1234"));


        JiraActionFactory factory = new JiraActionFactory();
        WebActionImpl webAction = (WebActionImpl) factory.getActionImpl(WebActionImpl.class.getName());
        assertNotNull(webAction);
        assertEquals(new Long(1234), webAction.id);

        SafeActionImpl backendAction = (SafeActionImpl) factory.getActionImpl(SafeActionImpl.class.getName());
        assertNotNull(backendAction);
        assertTrue(backendAction.setCalled);
    }

    public void testBadBackendActionParameters() throws Exception
    {
        // set up the action context
        setupActionContext(EasyMap.build("unSafeParameter", "This is not a List"));

        JiraActionFactory factory = new JiraActionFactory();
        try
        {
            factory.getActionImpl(SafeActionImpl.class.getName());
            fail("It should have barfed on the parameter type mismatch");
        }
        catch (ResultException expected)
        {
        }
    }

    public void testActionFailsXsrfCheck() throws Exception
    {
        // set up the action context
        setupActionContext(EasyMap.build("unSafeParameter", "This is not a List"));

        JiraActionFactory factory = new JiraActionFactory();
        try
        {
            factory.getActionImpl(XsrfAction.class.getName());
            fail("It should have barfed on XSRF check");
        }
        catch (XsrfFailureException expected)
        {
        }
    }

    public void testBadWebActionParameters() throws Exception
    {
        // set up the action context
        setupActionContext(EasyMap.build("id", "This is not a Long"));

        JiraActionFactory factory = new JiraActionFactory();
        try
        {
            factory.getActionImpl(WebActionImpl.class.getName());
            fail("It should have barfed on the parameter type mismatch");
        }
        catch (ResultException expected)
        {
        }
    }

    public void testInvokedRealBackendAction() throws Exception
    {
        final MockGenericValue projectGV = new MockGenericValue("project");
        setupActionContext(EasyMap.build("lead", COMPONENT_LEAD, "project", projectGV));

        JiraActionFactory factory = new JiraActionFactory();
        Action action = factory.getActionImpl(ActionNames.COMPONENT_EDIT);
        assertNotNull(action);
        assertTrue(action instanceof ComponentEdit);

        ComponentEdit componentEdit = (ComponentEdit) action;
        assertEquals(COMPONENT_LEAD, componentEdit.getLead());
        assertEquals(projectGV, componentEdit.getProject());
    }

    public void testRubbishActionName() throws Exception
    {
        JiraActionFactory factory = new JiraActionFactory();
        try
        {
            factory.getActionImpl("SomeRubbishName");
            fail("Should have barfed on this action name");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public static class ThisIsNotALoveSong
    {
        public ThisIsNotALoveSong()
        {
            Assert.fail("This should not have been invoked!");
        }
    }

    public void testValidClassButNotAnAction() throws Exception
    {
        JiraActionFactory factory = new JiraActionFactory();
        try
        {
            factory.getActionImpl(ThisIsNotALoveSong.class.getName());
            fail("Should have barfed on this action name");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public static class PrivateDancer implements Action
    {
        private PrivateDancer()
        {
        }

        public String execute() throws Exception
        {
            return null;
        }
    }

    public void testPrivateConstructor() throws Exception
    {
        JiraActionFactory factory = new JiraActionFactory();
        try
        {
            factory.getActionImpl(PrivateDancer.class.getName());
            fail("Should have barfed on this action");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    public void testClassDefErrorIsHandled() throws Exception
    {
        // reference the class by name first to cause a problem!
        try
        {
            Class badClass = Class.forName(SHES_A_MANIAC_CLASS);
        }
        catch (ExceptionInInitializerError expected)
        {
            Class badClass = getClass().getClassLoader().loadClass(SHES_A_MANIAC_CLASS);
            try
            {
                badClass.newInstance();
                fail("this should be uncool!");
            }
            catch (NoClassDefFoundError expectedAgain)
            {

            }
        }

        JiraActionFactory factory = new JiraActionFactory();
        try
        {
            factory.getActionImpl(SHES_A_MANIAC_CLASS);
            fail("Should have barfed on this action");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }


    private void setupActionContext(final Map parameterMap)
    {
        setupActionContext(parameterMap, null);
    }

    private void setupActionContext(final Map parameterMap, final Map<String, String> cookieMap)
    {
        ActionContext actionContext = new ActionContext();
        actionContext.put(ActionContext.PARAMETERS, parameterMap);
        ActionContext.setContext(actionContext);

        final MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setCookies(servletRequest.toCookies(cookieMap));
        ActionContext.setRequest(servletRequest);
    }
}
