package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.plugin.rest.util.LinkUriHelper;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.Expander;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;
import java.net.URI;

/**
 * Represents a Group entity.
 *
 * @since v2.1
 */
@XmlRootElement (name = "group")
@XmlAccessorType(XmlAccessType.FIELD)
@Expander(GroupEntityExpander.class)
public class GroupEntity implements NamedEntity
{
    @SuppressWarnings("unused")
    @XmlAttribute
    private String expand;

    @XmlElement (name = "link")
    private Link link;

    @XmlAttribute (name = "name")
    private String name;

    @XmlElement (name = "description")
    private String description;

    @XmlElement
    private final GroupType type;

    @XmlElement (name = "active")
    private Boolean active;

    @Expandable
    @XmlElement(name = "attributes")
    private MultiValuedAttributeEntityList attributes;

    /**
     * Only used when creating a minimal GroupEntity.
     *
     * @see {@link #newMinimalGroupEntity(String, String, URI)}
     */
    @XmlTransient
    private String applicationName;

    /**
     * JAXB requires a no-arg constructor.
     */
    private GroupEntity()
    {
        this.name = null;
        this.description = null;
        this.type = null;
        this.active = null;
        this.link = null;
    }

    public GroupEntity(final String name, final String description, final GroupType type, final Boolean active, final Link link)
    {
        this.name = name;
        this.description = description;
        this.type = type;
        this.active = active;
        this.link = link;
    }

    public String getDescription()
    {
        return description;
    }

    public GroupType getType()
    {
        return type;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getName()
    {
        return name;
    }

    public void setAttributes(final MultiValuedAttributeEntityList attributes)
    {
        this.attributes = attributes;
    }

    public MultiValuedAttributeEntityList getAttributes()
    {
        return attributes;
    }

    /**
     * Returns the application name. Should only be used by
     * {@link com.atlassian.crowd.plugin.rest.entity.GroupEntityExpander} to expand the GroupEntity.
     *
     * @return application name
     */
    String getApplicationName()
    {
        return applicationName;
    }

    /**
     * Creates a <tt>GroupEntity</tt> with the minimal amount of information required.
     *
     * @param name group name.
     * @param baseURI base URI
     * @return GroupEntity
     */
    public static GroupEntity newMinimalGroupEntity(final String name, final String applicationName, final URI baseURI)
    {
        GroupEntity group = new GroupEntity(name, null, null, null, LinkUriHelper.buildGroupLink(baseURI, name));
        group.applicationName = applicationName;
        return group;
    }

    /**
     * Does this object represent an expanded group, or does it only contain a group name.
     *
     * @return true if this object represents an expanded group
     */
    public boolean isExpanded()
    {
        return applicationName == null;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("name", getName()).
                append("active", isActive()).
                append("description", getDescription()).
                append("type", getType()).
                toString();
    }

    public Link getLink()
    {
        return link;
    }
}
