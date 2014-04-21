package com.atlassian.applinks.core.rest.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement (name = "applicationLinkInfo")
public class ApplicationLinkInfoEntity
{
    @XmlElement (name = "configuredAuthProviders")
    List<String> configuredAuthProviders;

    @XmlElement (name = "hostEntityTypes")
    List<String> hostEntityTypes;

    @XmlElement (name = "remoteEntityTypes")
    ArrayList<String> remoteEntityTypes;

    @XmlElement (name = "numConfiguredEntities")
    int numConfiguredEntities;

    public ApplicationLinkInfoEntity()
    {

    }

    public ApplicationLinkInfoEntity(final List<String> configuredAuthProviders, final int numConfiguredEntities, final List<String> hostEntityTypes, final ArrayList<String> remoteEntityTypes)
    {
        this.configuredAuthProviders = configuredAuthProviders;
        this.numConfiguredEntities = numConfiguredEntities;
        this.hostEntityTypes = hostEntityTypes;
        this.remoteEntityTypes = remoteEntityTypes;
    }

    public List<String> getConfiguredAuthProviders()
    {
        return configuredAuthProviders;
    }

    public List<String> getHostEntityTypes()
    {
        return hostEntityTypes;
    }

    public int getNumConfiguredEntities()
    {
        return numConfiguredEntities;
    }

    public ArrayList<String> getRemoteEntityTypes()
    {
        return remoteEntityTypes;
    }
}
