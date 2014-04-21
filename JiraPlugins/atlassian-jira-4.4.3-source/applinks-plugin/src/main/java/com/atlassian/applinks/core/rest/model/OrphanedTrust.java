package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.core.auth.OrphanedTrustCertificate;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "orphanedTrust")
public class OrphanedTrust
{

    private String id;
    private String type;

    @SuppressWarnings("unused")
    private OrphanedTrust()
    {
    }

    public OrphanedTrust(final String id, final OrphanedTrustCertificate.Type type)
    {
        this.id = id;
        this.type = type.name();
    }

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

}
