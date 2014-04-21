package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;


/**
 * @since 4.4
 */
@Path ("websudo")
public class WebSudoBackdoor
{
    /**
     * Invoked like this "/jira/rest/func-test/1.0/websudo?enabled=true"
     */
    @GET
    @AnonymousAllowed
    public Response changeWebSudoStatus(@QueryParam ("enabled") String enabled)
    {
        boolean isDisabled = "false".equals(enabled);

        System.setProperty(SystemPropertyKeys.WEBSUDO_IS_DISABLED, Boolean.toString(isDisabled));

        ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();

        applicationProperties.setOption(APKeys.WebSudo.IS_DISABLED, isDisabled);

        return Response.ok(null).build();
    }

    static private void disableWebSudo() {
        ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();

        applicationProperties.setOption(APKeys.WebSudo.IS_DISABLED, true);
    }

}
