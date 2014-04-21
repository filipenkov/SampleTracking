package com.atlassian.jira.issue.tabpanels;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.Date;
import java.util.Map;

/**
 * A simple action that can be used to display generic messages.
 */
public class GenericMessageAction implements IssueAction
{
    private static final Logger log = Logger.getLogger(GenericMessageAction.class);
    private static final String PLUGIN_TEMPLATES = "templates/plugins/";

    private final String message;

    public GenericMessageAction(String message)
    {
        this.message = message;
    }

    public Date getTimePerformed()
    {
        throw new UnsupportedOperationException();
    }

    public String getHtml()
    {
        String templateName = "jira/issuetabpanels/generic-message.vm";
        VelocityManager velocityManager = ManagerFactory.getVelocityManager();
        try
        {
            Map params = EasyMap.build("message", message);
            return velocityManager.getBody(PLUGIN_TEMPLATES, templateName, params);
        }
        catch (VelocityException e)
        {
            log.error("Error while rendering velocity template for '" + templateName + "'.", e);
            return "Velocity template generation failed.";
        }
    }

    public boolean isDisplayActionAllTab()
    {
        return false;
    }
}
