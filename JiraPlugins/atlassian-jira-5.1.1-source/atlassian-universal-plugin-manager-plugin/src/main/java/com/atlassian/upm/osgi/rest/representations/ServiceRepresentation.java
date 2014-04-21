package com.atlassian.upm.osgi.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.impl.Wrapper;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.collect.ImmutableList;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A representation for OSGi service objects
 */
public final class ServiceRepresentation extends ServiceSummaryRepresentation
{
    @JsonProperty private final BundleSummaryRepresentation bundle;
    @JsonProperty private final Collection<BundleSummaryRepresentation> usingBundles;
    @JsonProperty private final Collection<String> objectClasses;
    @JsonProperty private final @Nullable String description;
    @JsonProperty private final Collection<String> pid;
    @JsonProperty private final int ranking;
    @JsonProperty private final @Nullable String vendor;

    @JsonCreator
    ServiceRepresentation(@JsonProperty("bundle") BundleSummaryRepresentation bundle,
        @JsonProperty("usingBundles") Collection<BundleSummaryRepresentation> usingBundles,
        @JsonProperty("objectClasses") Collection<String> objectClasses,
        @JsonProperty("description") @Nullable String description,
        @JsonProperty("id") long id,
        @JsonProperty("pid") Collection<String> pid,
        @JsonProperty("ranking") int ranking,
        @JsonProperty("vendor") @Nullable String vendor,
        @JsonProperty("links") Map<String, URI> links)
    {
        super(id, links);
        this.bundle = checkNotNull(bundle, "bundle");
        this.usingBundles = ImmutableList.copyOf(usingBundles);
        this.objectClasses = ImmutableList.copyOf(objectClasses);
        this.description = description;
        this.pid = ImmutableList.copyOf(pid);
        this.ranking = ranking;
        this.vendor = vendor;
    }

    public ServiceRepresentation(Service service, UpmUriBuilder uriBuilder)
    {
        super(service, uriBuilder);
        Wrapper<Bundle, BundleSummaryRepresentation> wrap = BundleSummaryRepresentation.wrapSummary(uriBuilder);
        this.bundle = checkNotNull(wrap.fromSingleton(service.getBundle()), "bundle");
        this.usingBundles = checkNotNull(wrap.fromIterable(service.getUsingBundles()), "usingBundles");
        this.objectClasses = ImmutableList.copyOf(service.getObjectClasses());
        this.description = service.getDescription();
        this.pid = ImmutableList.copyOf(service.getPid());
        this.ranking = service.getRanking();
        this.vendor = service.getVendor();
    }

    public BundleSummaryRepresentation getBundle()
    {
        return bundle;
    }

    public Collection<BundleSummaryRepresentation> getUsingBundles()
    {
        return usingBundles;
    }

    public Collection<String> getObjectClasses()
    {
        return objectClasses;
    }

    @Nullable
    public String getDescription()
    {
        return description;
    }

    public Collection<String> getPid()
    {
        return pid;
    }

    public int getRanking()
    {
        return ranking;
    }

    @Nullable
    public String getVendor()
    {
        return vendor;
    }

    public static Wrapper<Service, ServiceRepresentation> wrap(final UpmUriBuilder uriBuilder)
    {
        return new Wrapper<Service, ServiceRepresentation>("serviceRepresentation")
        {
            public ServiceRepresentation wrap(Service service)
            {
                return new ServiceRepresentation(service, uriBuilder);
            }
        };
    }

}
