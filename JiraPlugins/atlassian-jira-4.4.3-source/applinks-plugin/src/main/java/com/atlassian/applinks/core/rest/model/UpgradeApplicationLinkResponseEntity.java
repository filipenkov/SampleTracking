package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Returned by the upgrade methods in
 * {@link com.atlassian.applinks.core.rest.ui.UpgradeApplicationLinkUIResource}.
 *
 * @since   3.0
 */
@XmlRootElement(name = "upgradedApplicationLink")
public class UpgradeApplicationLinkResponseEntity
{
    @XmlElement(name = "applicationLink")
    private final ApplicationLinkEntity applicationLinkEntity;

    @XmlElement(name = "message")
    private final List<String> messages;

    public UpgradeApplicationLinkResponseEntity(final ApplicationLinkEntity applicationLinkEntity, final List<String> messages)
    {
        this.applicationLinkEntity = applicationLinkEntity;
        this.messages = messages;
    }

    public ApplicationLinkEntity getApplicationLinkEntity()
    {
        return applicationLinkEntity;
    }

    public List<String> getMessages()
    {
        return messages;
    }
}
