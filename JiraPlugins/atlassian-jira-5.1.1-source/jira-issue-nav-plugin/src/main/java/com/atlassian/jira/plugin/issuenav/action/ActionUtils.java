package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.crowd.embedded.api.User;
import org.apache.commons.httpclient.HttpStatus;
import webwork.action.ActionContext;

/**
 * Utilies for actions
 * @since v5.1
 */
public class ActionUtils
{
    public static final String JSON = "json";

    public static void setErrorReturnCode(User user)
    {
        if (user == null)
        {
            ActionContext.getResponse().setStatus(HttpStatus.SC_UNAUTHORIZED);
        }
        else
        {
            ActionContext.getResponse().setStatus(HttpStatus.SC_BAD_REQUEST);
        }
    }
}
