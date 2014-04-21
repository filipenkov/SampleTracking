package com.atlassian.jira.plugin.webwork;

import com.atlassian.jira.ComponentManager;
import com.atlassian.seraph.SecurityService;
import com.atlassian.seraph.config.SecurityConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * WebworkPluginSecurityService configures Seraph based on Webwork plugin module atlassian-plugin.xml
 *
 * This allows for the roles-required attribute to be used within plugins.
 *
 * @since v5.0
 */
public class WebworkPluginSecurityService implements SecurityService
{
     /**
     * Seraph Initable initialisation method.
     * As we rely on plugin events to setup our required roles, we don't do anything here
     *
     * @param params
     * @param config
     */
    public void init(Map<String, String> params, SecurityConfig config)
    {
    }

    /**
     * Seraph Initable cleanup method.
     */
    public void destroy()
    {
    }

    /**
     * This hands off to the Helper, who is able to keep track of plugin module events, and live in the pico container
     *
     * @param request
     * @return
     */
    public Set<String> getRequiredRoles(final HttpServletRequest request)
    {
        return ComponentManager.getComponent(WebworkPluginSecurityServiceHelper.class).getRequiredRoles(request);
    }
}
