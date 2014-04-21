package com.atlassian.jira.web.component;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.VelocityException;

import java.util.Map;

/**
 * The superclass of all web components, which has some simple helper methods.
 */
public class AbstractWebComponent
{
    private static final Logger log = Logger.getLogger(AbstractWebComponent.class);

    protected final VelocityManager velocityManager;
    protected final ApplicationProperties applicationProperties;

    public AbstractWebComponent(VelocityManager velocityManager, ApplicationProperties applicationProperties)
    {
        this.velocityManager = velocityManager;
        this.applicationProperties = applicationProperties;
    }

    protected String getHtml(String resourceName, Map<String, Object> startingParams)
    {
        if (TextUtils.stringSet(resourceName))
        {
            try
            {
                return velocityManager.getEncodedBody(resourceName, "", applicationProperties.getEncoding(), startingParams);
            }
            catch (VelocityException e)
            {
                log.error("Error while rendering velocity template for '" + resourceName + "'.", e);
            }
        }
        return "";
    }

}
