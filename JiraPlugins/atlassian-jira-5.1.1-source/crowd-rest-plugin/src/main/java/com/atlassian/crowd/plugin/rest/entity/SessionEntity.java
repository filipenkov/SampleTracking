package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.expand.Expandable;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.*;

/**
 * Represents a Session entity.
 *
 * @since v2.1
 */
@XmlRootElement (name = "session")
@XmlAccessorType(XmlAccessType.FIELD)
public class SessionEntity
{ 
    @SuppressWarnings("unused")
    @XmlAttribute
    private String expand;

    @XmlElement (name = "token")
    private String token;

    @Expandable
    @XmlElement(name = "user")
    private UserEntity user;

    @XmlElement (name = "link")
    private Link link;

    /**
     * JAXB requires a no-arg constructor.
     */
    private SessionEntity()
    {
    }

    public SessionEntity(final String token, final UserEntity user, final Link link)
    {
        this.token = token;
        this.user = user;
        this.link = link;
    }

    public String getToken()
    {
        return token;
    }

    public UserEntity getUser()
    {
        return user;
    }

    public Link getLink()
    {
        return link;
    }

    public String toString()
    {
        return new ToStringBuilder(this).
                append("token", getToken()).
                append("user", getUser()).
                toString();
    }
}
