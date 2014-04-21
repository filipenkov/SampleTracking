package com.atlassian.core.action;

import webwork.action.ActionContext;
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
    public ActionResult execute(String actionName) throws Exception
    {
        return execute(actionName, null);
    }

    public ActionResult execute(String actionName, Map parameters) throws Exception
    {
        Principal user = ActionContext.getPrincipal();
        GenericDispatcher gd = new GenericDispatcher(actionName);
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
}