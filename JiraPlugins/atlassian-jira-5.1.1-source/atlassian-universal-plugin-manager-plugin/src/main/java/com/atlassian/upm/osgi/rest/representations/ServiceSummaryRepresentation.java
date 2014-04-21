package com.atlassian.upm.osgi.rest.representations;

import java.net.URI;
import java.util.Map;

import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.impl.Wrapper;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;

/**
 * A summary representation for OSGi service objects,
 * omitting bundles and properties
 */
public class ServiceSummaryRepresentation
{
    @JsonProperty private final long id;
    @JsonProperty private final Map<String, URI> links;

    @JsonCreator
    public ServiceSummaryRepresentation(@JsonProperty("id") long id,
        @JsonProperty("links") Map<String, URI> links)
    {
        this.id = id;
        this.links = ImmutableMap.copyOf(links);
    }

    public ServiceSummaryRepresentation(Service service, UpmUriBuilder uriBuilder)
    {
        this.id = service.getId();
        this.links = ImmutableMap.of(SELF_REL, uriBuilder.buildOsgiServiceUri(service));
    }

    public long getId()
    {
        return id;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }

    public static Wrapper<Service, ServiceSummaryRepresentation> wrapSummary(final UpmUriBuilder uriBuilder)
    {
        return new Wrapper<Service, ServiceSummaryRepresentation>("serviceSummaryRepresentation")
        {
            public ServiceSummaryRepresentation wrap(Service service)
            {
                return new ServiceSummaryRepresentation(service, uriBuilder);
            }
        };
    }

}