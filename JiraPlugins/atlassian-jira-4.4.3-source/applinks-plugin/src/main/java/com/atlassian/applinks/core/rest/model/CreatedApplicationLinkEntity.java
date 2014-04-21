package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @since 3.0
 */
@XmlRootElement (name = "createdApplicationLink")
public class CreatedApplicationLinkEntity
{
    @XmlElement (name = "applicationLink")
    private final ApplicationLinkEntity applicationLinkEntity;

    private boolean autoConfigurationSuccessful;

    @SuppressWarnings("unused") // needed by JAXB
    public CreatedApplicationLinkEntity()
    {
        this (null, true);
    }

    public CreatedApplicationLinkEntity(ApplicationLinkEntity applicationLinkEntity, final boolean autoconfigurationsuccessful)
    {
        this.applicationLinkEntity = applicationLinkEntity;
        this.autoConfigurationSuccessful = autoconfigurationsuccessful;
    }

    public ApplicationLinkEntity getApplicationLinkEntity()
    {
        return applicationLinkEntity;
    }

    public boolean isAutoConfigurationSuccessful()
    {
        return autoConfigurationSuccessful;
    }
}
