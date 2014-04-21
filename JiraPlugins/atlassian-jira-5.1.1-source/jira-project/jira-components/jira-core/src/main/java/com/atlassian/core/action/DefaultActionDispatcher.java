package com.atlassian.core.action;

import com.atlassian.jira.config.webwork.JiraActionFactory;
import webwork.action.ActionContext;
import webwork.action.factory.ActionFactory;
import webwork.dispatcher.ActionResult;
import webwork.dispatcher.GenericDispatcher;

import java.security.Principal;
import java.util.Map;

/**
 * This was taken from atlassian-webwork1 and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class DefaultActionDispatcher implements ActionDispatcher
{
    public ActionResult execute(final String actionName) throws Exception
    {
        return execute(actionName, null);
    }

    public ActionResult execute(final String actionName, final Map parameters) throws Exception
    {
        final Principal user = ActionContext.getPrincipal();
        final GenericDispatcher gd = new GenericDispatcher(actionName, ActionFactoryInstance.get());
        gd.prepareContext();
        try
        {
            gd.prepareValueStack(); //this is needed when you use Webwork 1.3.1, as the calls have been split out
            ActionContext.setParameters(parameters);
            ActionContext.setPrincipal(user);

            gd.executeAction();
            return gd.finish();
        }
        finally
        {
            gd.finalizeContext();
        }
    }

    /**
     * Holds a lazily initialised {@link ActionFactory} instance.
     *
     * Laziness is needed because when this class is initialised the {@link ActionFactory} is not ready to be
     * initialised yet :-(
     */
    private static class ActionFactoryInstance
    {
        private final static ActionFactory INSTANCE = new JiraActionFactory.NonWebActionFactory();

        private ActionFactoryInstance(){}

        public static ActionFactory get()
        {
            return INSTANCE;
        }
    }
}