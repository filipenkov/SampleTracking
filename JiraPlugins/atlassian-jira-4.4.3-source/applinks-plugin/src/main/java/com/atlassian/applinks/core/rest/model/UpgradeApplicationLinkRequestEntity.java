package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.core.rest.model.adapter.OptionalURIAdapter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

/**
 *
 * @since   3.0
 */
@XmlRootElement(name = "upgradeApplicationLink")
public class UpgradeApplicationLinkRequestEntity
{
    private String username;
    private String password;
    private boolean createTwoWayLink;
    private boolean reciprocateEntityLinks;
    @XmlJavaTypeAdapter(OptionalURIAdapter.class)
    private URI rpcUrl;
    private ConfigurationFormValuesEntity configFormValues;

    @SuppressWarnings("unused")
    public UpgradeApplicationLinkRequestEntity()
    {
    }

    public UpgradeApplicationLinkRequestEntity(final ConfigurationFormValuesEntity configFormValues,
                                        final boolean createTwoWayLink,
                                        final String password,
                                        final boolean reciprocateEntityLinks,
                                        final String username,
                                        final URI rpcUrl)
    {
        this.configFormValues = configFormValues;
        this.createTwoWayLink = createTwoWayLink;
        this.password = password;
        this.reciprocateEntityLinks = reciprocateEntityLinks;
        this.username = username;
        this.rpcUrl = rpcUrl;
    }

    public ConfigurationFormValuesEntity getConfigFormValues()
    {
        return configFormValues;
    }

    public boolean isCreateTwoWayLink() {
        return createTwoWayLink;
    }

    public String getPassword() {
        return password;
    }

    public boolean isReciprocateEntityLinks() {
        return reciprocateEntityLinks;
    }

    public String getUsername() {
        return username;
    }

    public URI getRpcUrl() {
        return rpcUrl;
    }
}
