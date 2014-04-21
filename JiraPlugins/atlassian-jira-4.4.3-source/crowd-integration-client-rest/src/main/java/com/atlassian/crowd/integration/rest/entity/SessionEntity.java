package com.atlassian.crowd.integration.rest.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "session")
public class SessionEntity
{
    @XmlElement
    private final String token;

    @XmlElement
    private final UserEntity user;

    private SessionEntity()
    {
        this.token = null;
        this.user = null;
    }

    public SessionEntity(String token, UserEntity user)
    {
        this.token = token;
        this.user = user;
    }

    public String getToken()
    {
        return token;
    }

    public UserEntity getUser()
    {
        return user;
    }
}
