package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.plugin.rest.entity.CookieConfigEntity;
import com.atlassian.crowd.plugin.rest.service.controller.CookieConfigController;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.Validate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Path("config/cookie")
@AnonymousAllowed
@Produces ({APPLICATION_XML, APPLICATION_JSON})
public class CookieConfigResource
{
    private final CookieConfigController controller;

    public CookieConfigResource(CookieConfigController controller)
    {
        Validate.notNull(controller);
        this.controller = controller;
    }

    /**
     * Returns the Cookie configuration information.
     *
     * @return
     */
    @GET
    public Response getConfig()
    {
        return Response.ok(new CookieConfigEntity(controller.getDomain(), controller.isSecureCookie(), controller.getName())).build();
    }
}
