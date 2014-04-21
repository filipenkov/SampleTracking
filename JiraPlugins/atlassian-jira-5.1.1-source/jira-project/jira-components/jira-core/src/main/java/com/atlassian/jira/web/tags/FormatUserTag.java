package com.atlassian.jira.web.tags;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import org.apache.log4j.Logger;
import webwork.view.taglib.WebWorkBodyTagSupport;

import javax.servlet.jsp.JspException;
import java.util.Map;

/**
 * Formats a user given the username (or user object, a type and id) using the {@link UserFormatManager}.
 *
 * @since v3.13
 */
public class FormatUserTag extends WebWorkBodyTagSupport
{
    private static final Logger log = Logger.getLogger(FormatUserTag.class);
    private String user;
    private String type;
    private String id;
    private String params;

    public void setUser(String user)
    {
        this.user = user;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setParams(String params)
    {
        this.params = params;
    }

    public int doEndTag() throws JspException
    {
        String username = null;

        try
        {
            final Object value = findValue(user);
            if(value instanceof String)
            {
                //passed as string.  Must be the user's username.
                username = (String) value;
            }
            else if(value instanceof User)
            {
                //passed a User object, lets use its username.
                username = ((User) value).getName();
            }
            else
            {
                //passed an invalid object type.
                username = user;
            }

            String idVal = findString(id);
            String typeVal = findString(type);

            final UserFormatManager userFormatManager = ComponentManager.getComponentInstanceOfType(UserFormatManager.class);
            String formattedUser;
            if (params == null)
            {
                formattedUser = userFormatManager.formatUser(username, typeVal, idVal);
            }
            else
            {
                formattedUser = userFormatManager.formatUser(username, typeVal, idVal, (Map)findValue(params));
            }

            pageContext.getOut().write(formattedUser);
        }
        catch (Exception e)
        {
            log.error("Unexpected error occurred formatting user '" + username + "'", e);
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

}
