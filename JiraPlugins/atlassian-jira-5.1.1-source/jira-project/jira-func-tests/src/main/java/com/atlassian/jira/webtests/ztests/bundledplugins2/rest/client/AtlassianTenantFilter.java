package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Adds the X-Atlassian-Tenant header to all requests
 *
 * @since v4.3
 */
public class AtlassianTenantFilter extends ClientFilter
{
    private final String tenant;

    public AtlassianTenantFilter(String tenant)
    {
        this.tenant = tenant;
    }

    @Override
    public ClientResponse handle(ClientRequest cr) throws ClientHandlerException
    {
        if (!cr.getMetadata().containsKey("X-Atlassian-Tenant"))
        {
            cr.getMetadata().add("X-Atlassian-Tenant", tenant);
        }
        return getNext().handle(cr);
    }
}
