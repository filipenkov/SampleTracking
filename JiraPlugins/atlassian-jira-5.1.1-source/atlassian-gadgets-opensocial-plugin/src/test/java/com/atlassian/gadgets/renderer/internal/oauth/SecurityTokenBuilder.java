package com.atlassian.gadgets.renderer.internal.oauth;

import org.apache.shindig.auth.BasicSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.crypto.BlobCrypterException;

public class SecurityTokenBuilder
{
    private String owner = "";
    private String viewer = "";
    private String app = "";
    private String domain = "";
    private String appUrl = "";
    private String moduleId = "";
    private String container = "";
    private String activeUrl = "";
    
    public SecurityTokenBuilder owner(String owner)
    {
        this.owner = owner;
        return this;
    }
    
    public SecurityTokenBuilder viewer(String viewer)
    {
        this.viewer = viewer;
        return this;
    }
    
    public SecurityTokenBuilder app(String app)
    {
        this.app = app;
        return this;
    }
    
    public SecurityTokenBuilder domain(String domain)
    {
        this.domain = domain;
        return this;
    }
    
    public SecurityTokenBuilder appUrl(String appUrl)
    {
        this.appUrl = appUrl;
        return this;
    }
    
    public SecurityTokenBuilder moduleId(String moduleId)
    {
        this.moduleId = moduleId;
        return this;
    }
    
    public SecurityTokenBuilder container(String container)
    {
        this.container = container;
        return this;
    }
    
    public SecurityTokenBuilder activeUrl(String activeUrl)
    {
        this.activeUrl = activeUrl;
        return this;
    }
    
    public SecurityToken build()
    {
        try
        {
            return new BasicSecurityToken(owner, viewer, app, domain, appUrl, moduleId, container, activeUrl);
        }
        catch (BlobCrypterException e)
        {
            throw new RuntimeException(e);
        }
    }
}
