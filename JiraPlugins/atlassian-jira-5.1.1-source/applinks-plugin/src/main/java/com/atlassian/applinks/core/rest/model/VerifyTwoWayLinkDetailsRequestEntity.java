package com.atlassian.applinks.core.rest.model;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This object is used when verifying two way link details using the add application link wizard.
 *
 * @since 3.7.1
 */
@XmlRootElement(name = "verifyTwoWayLinkDetailsRequest")
public class VerifyTwoWayLinkDetailsRequestEntity
{
    private String username;
    private String password;
    private URI remoteUrl;
    private URI rpcUrl;

    @SuppressWarnings("unused")
    private VerifyTwoWayLinkDetailsRequestEntity()
    {
    }

    public VerifyTwoWayLinkDetailsRequestEntity(
            final String username,
            final String password,
            final URI remoteUrl,
            final URI rpcUrl)
    {
        this.username = username;
        this.password = password;
        this.remoteUrl = remoteUrl;
        this.rpcUrl = rpcUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public URI getRemoteUrl()
    {
        return remoteUrl;
    }

    public URI getRpcUrl()
    {
        return rpcUrl;
    }
}
