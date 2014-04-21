package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.plugins.rest.common.Link;
import com.google.common.collect.Sets;

import java.util.EnumSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a DirectoryMapping entity.
 *
 * @since 2.2
 */
@XmlRootElement (name = "directory-mapping")
@XmlAccessorType (XmlAccessType.FIELD)
public class DirectoryMappingEntity
{
    @XmlAttribute (name = "id")
    private final Long directoryId;

    @XmlElement (name = "authenticate-all")
    private final Boolean authenticateAll;

    @XmlElementWrapper (name = "allowed-operations")
    @XmlElements (@XmlElement (name = "allowed-operation"))
    private final Set<String> allowedOperations;

    @XmlElement (name = "link")
    private final Link link;

    /**
     * JAXB requires a no-arg constructor
     */
    private DirectoryMappingEntity()
    {
        directoryId = null;
        authenticateAll = null;
        allowedOperations = Sets.newHashSet();
        link = null;
    }

    public DirectoryMappingEntity(final Long directoryId, final Boolean authenticateAll, final Set<String> allowedOperations, final Link link)
    {
        this.directoryId = directoryId;
        this.authenticateAll = authenticateAll;
        this.allowedOperations = allowedOperations;
        this.link = link;
    }

    public Long getDirectoryId()
    {
        return directoryId;
    }

    public Boolean isAuthenticateAll()
    {
        return authenticateAll;
    }

    public Set<String> getAllowedOperations()
    {
        return allowedOperations;
    }

    public Link getLink()
    {
        return link;
    }
}
