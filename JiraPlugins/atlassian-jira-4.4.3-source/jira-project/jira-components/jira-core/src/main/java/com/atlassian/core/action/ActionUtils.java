package com.atlassian.core.action;

import com.atlassian.core.AtlassianCoreException;
import webwork.action.Action;
import webwork.action.ActionSupport;
import webwork.dispatcher.ActionResult;

import java.util.Iterator;

/**
 * This was taken from atlassian-webwork1 and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class ActionUtils
{
    /**
     * A useful method to check a result for errors.
     * <p/>
     * It will try to construct a useful error message and log it to log.ERROR, as well as throwing
     * an ActionException containing all of the error data.
     *
     * @throws java.lang.Exception if any errors are detected in the result
     */
    public static void checkForErrors(ActionResult aResult) throws Exception
    {
        if (aResult.getActionException() != null)
        {
            throw aResult.getActionException();
        }

        if (!Action.SUCCESS.equals(aResult.getResult()))
        {
            String errorMessage = "Error in action: " + aResult.getFirstAction() + ", result: " + aResult.getResult();

            ActionSupport aSupport = (ActionSupport) aResult.getFirstAction();

            if (aSupport != null && aSupport.getHasErrorMessages())
            {
                for (Iterator iterator = aSupport.getErrorMessages().iterator(); iterator.hasNext();)
                {
                    String actionErrorMessage = (String) iterator.next();
                    errorMessage = errorMessage + "\n" + actionErrorMessage;
                }
            }
            throw new AtlassianCoreException(errorMessage);
        }
    }
}
