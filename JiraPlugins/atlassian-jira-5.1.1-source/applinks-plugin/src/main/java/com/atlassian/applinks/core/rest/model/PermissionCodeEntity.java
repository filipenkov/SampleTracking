package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.core.rest.model.adapter.PermissionCodeAdapter;
import com.atlassian.applinks.core.rest.model.adapter.RequiredURIAdapter;
import com.atlassian.applinks.core.rest.permission.PermissionCode;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;

@XmlRootElement
public class PermissionCodeEntity
{
    @XmlJavaTypeAdapter(PermissionCodeAdapter.class)
    private PermissionCode code;
    /**
     * The URL to redirect to when code == CREDENTIALS_REQUIRED
     */
    @XmlJavaTypeAdapter(RequiredURIAdapter.class)
    private URI url;

    @SuppressWarnings("unused")
    private PermissionCodeEntity()
    {
    }

    public PermissionCodeEntity(final PermissionCode code)
    {
        this.code = code;
    }

    public PermissionCodeEntity(final PermissionCode code, final URI url)
    {
        this.code = code;
        this.url = url;
    }

    public PermissionCode getCode()
    {
        return code;
    }

    public URI getUrl()
    {
        return url;
    }
}
