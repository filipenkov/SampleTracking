package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.rest.model.adapter.OptionalURIAdapter;
import com.atlassian.applinks.core.rest.model.adapter.RequiredBaseURIAdapter;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.core.rest.model.adapter.ApplicationIdAdapter;
import com.atlassian.applinks.core.rest.model.adapter.TypeIdAdapter;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugins.rest.common.Link;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

@XmlRootElement(name = "applicationLink")
public class ApplicationLinkEntity extends LinkedEntity
{
    @XmlJavaTypeAdapter(ApplicationIdAdapter.class)
    private ApplicationId id;
    @XmlJavaTypeAdapter(TypeIdAdapter.class)
    private TypeId typeId;
    private String name;
    @XmlJavaTypeAdapter(RequiredBaseURIAdapter.class)
    private URI displayUrl;
    @XmlJavaTypeAdapter(OptionalURIAdapter.class)
    private URI iconUrl;
    @XmlJavaTypeAdapter(RequiredBaseURIAdapter.class)
    private URI rpcUrl;
    private Boolean isPrimary;

    @SuppressWarnings("unused")
    private ApplicationLinkEntity()
    {
    }

    public ApplicationLinkEntity(final ApplicationLink applicationLink, final Link self)
    {
        this(applicationLink.getId(), TypeId.getTypeId(applicationLink.getType()), applicationLink.getName(),
                applicationLink.getDisplayUrl(), applicationLink.getType().getIconUrl(), applicationLink.getRpcUrl(),
                applicationLink.isPrimary(), self);
    }

    public ApplicationLinkEntity(final ApplicationId id,
                                 final TypeId typeId,
                                 final String name,
                                 final URI displayUrl,
                                 final URI iconUrl,
                                 final URI rpcUrl,
                                 final Boolean primary,
                                 final Link self)
    {
        this.id = id;
        this.typeId = typeId;
        this.name = name;
        this.displayUrl = displayUrl;
        this.iconUrl = iconUrl;
        this.rpcUrl = rpcUrl;
        this.isPrimary = primary;
        addLink(self);
    }

    public ApplicationId getId()
    {
        return id;
    }

    public TypeId getTypeId()
    {
        return typeId;
    }

    public String getName()
    {
        return name;
    }

    public URI getDisplayUrl()
    {
        return displayUrl;
    }

    public URI getIconUrl()
    {
        return iconUrl;
    }

    public URI getRpcUrl()
    {
        return rpcUrl;
    }

    public boolean isPrimary()
    {
        return isPrimary != null && isPrimary;
    }

    public ApplicationLinkDetails getDetails()
    {
        return ApplicationLinkDetails
                .builder()
                .name(name)
                .displayUrl(displayUrl)
                .rpcUrl(rpcUrl)
                .isPrimary(isPrimary)
                .build();
    }
}
