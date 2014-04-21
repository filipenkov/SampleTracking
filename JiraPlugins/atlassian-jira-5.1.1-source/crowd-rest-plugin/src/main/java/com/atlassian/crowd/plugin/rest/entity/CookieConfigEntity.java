package com.atlassian.crowd.plugin.rest.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST version of a cookie configuration
 */
@XmlRootElement (name = "cookie-config")
@XmlAccessorType (XmlAccessType.FIELD)
public class CookieConfigEntity
{
    @XmlElement (name = "domain")
    private final String domain;

    @XmlElement (name = "secure")
    private final boolean secure;

    @XmlElement (name = "name")
    private final String name;

    private CookieConfigEntity()
    {
        domain = null;
        secure = false;
        name = null;
    }

    public CookieConfigEntity(final String domain, final boolean secure, final String name)
    {
        this.domain = domain;
        this.secure = secure;
        this.name = name;
    }

    public String getDomain()
    {
        return domain;
    }

    public boolean isSecure()
    {
        return secure;
    }

    public String getName()
    {
        return name;
    }
}
