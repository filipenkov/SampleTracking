package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.core.rest.model.adapter.ApplicationIdAdapter;
import com.atlassian.applinks.core.rest.model.adapter.OptionalURIAdapter;
import com.atlassian.applinks.core.rest.model.adapter.RequiredURIAdapter;
import com.atlassian.applinks.core.rest.model.adapter.TypeIdAdapter;
import com.atlassian.applinks.spi.application.TypeId;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: should extend LinkedEntity
@XmlRootElement(name = "entityLink")
public class EntityLinkEntity
{
    @XmlJavaTypeAdapter(ApplicationIdAdapter.class)
    private ApplicationId applicationId;
    @XmlJavaTypeAdapter(TypeIdAdapter.class)
    private TypeId typeId;
    private String key;
    private String name;
    @XmlJavaTypeAdapter(OptionalURIAdapter.class)
    private URI displayUrl;
    @XmlJavaTypeAdapter(OptionalURIAdapter.class)
    private URI iconUrl;
    private Boolean isPrimary;

    @SuppressWarnings("unused")
    private EntityLinkEntity()
    {
    }

    public EntityLinkEntity(final EntityLink entity)
    {
        this(entity.getApplicationLink().getId(), entity.getKey(), TypeId.getTypeId(entity.getType()),
                entity.getName(), entity.getDisplayUrl(), entity.getType().getIconUrl(), entity.isPrimary());
    }

    public EntityLinkEntity(final ApplicationId applicationId, final String key, final TypeId typeId,
                            final String name, final URI displayUrl, final URI iconUrl, final Boolean isPrimary)
    {
        this.applicationId = checkNotNull(applicationId);
        this.typeId = checkNotNull(typeId);
        this.key = checkNotNull(key);
        this.name = name != null ? name : key;
        this.displayUrl = displayUrl;
        this.iconUrl = iconUrl;
        this.isPrimary = isPrimary;
    }

    public ApplicationId getApplicationId()
    {
        return applicationId;
    }

    public TypeId getTypeId()
    {
        return typeId;
    }

    public URI getDisplayUrl()
    {
        return displayUrl;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public URI getIconUrl()
    {
        return iconUrl;
    }

    public Boolean isPrimary()
    {
        return isPrimary;
    }
}
