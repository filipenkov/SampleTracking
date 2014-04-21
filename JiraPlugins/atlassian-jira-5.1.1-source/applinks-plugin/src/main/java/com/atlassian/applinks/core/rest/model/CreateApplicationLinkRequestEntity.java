package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.core.rest.model.adapter.RequiredBaseURIAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

/**
 * This object is used when creating a new application link using the add application link wizard.
 *
 *
 * @since 3.0
 */
@XmlRootElement(name = "createApplicationLinkRequest")
public class CreateApplicationLinkRequestEntity
{
    private ApplicationLinkEntity applicationLink;
    private String username;
    private String password;

    //Determines the URL used by the remote application to access this application,
    //only used when creating 2-way links.
    private boolean customRpcURL;
    @XmlJavaTypeAdapter(RequiredBaseURIAdapter.class)
    private URI rpcUrl;
    private boolean createTwoWayLink;
    private ConfigurationFormValuesEntity configFormValues;
    private OrphanedTrust orphanedTrust;

    @SuppressWarnings("unused")
    private CreateApplicationLinkRequestEntity()
    {
    }

    public CreateApplicationLinkRequestEntity(
            final ApplicationLinkEntity applicationLink,
            final String username,
            final String password,
            final boolean customRpcURL,
            final URI rpcUrl,
            final boolean createTwoWayLink,
            final ConfigurationFormValuesEntity configFormValues)
    {
        this.applicationLink = applicationLink;
        this.username = username;
        this.password = password;
        this.customRpcURL = customRpcURL;
        this.rpcUrl = rpcUrl;
        this.createTwoWayLink = createTwoWayLink;
        this.configFormValues = configFormValues;
    }

    public ApplicationLinkEntity getApplicationLink()
    {
        return applicationLink;
    }

    public boolean createTwoWayLink()
    {
        return createTwoWayLink;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public boolean isCustomRpcURL()
    {
        return customRpcURL;
    }

    public URI getRpcUrl()
    {
        return rpcUrl;
    }

    public ConfigurationFormValuesEntity getConfigFormValues()
    {
        return configFormValues;
    }

    public OrphanedTrust getOrphanedTrust()
    {
        return orphanedTrust;
    }
}
