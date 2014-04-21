package com.atlassian.upm.pac;

import com.atlassian.plugins.client.service.AbstractServiceClient;
import com.atlassian.plugins.client.service.ClientContextFactory;
import com.atlassian.plugins.client.service.audit.AuditServiceClientImpl;
import com.atlassian.plugins.client.service.plugin.PluginServiceClientImpl;
import com.atlassian.plugins.client.service.plugin.PluginVersionServiceClientImpl;
import com.atlassian.plugins.client.service.product.ProductServiceClientImpl;
import com.atlassian.plugins.service.audit.AuditService;
import com.atlassian.plugins.service.plugin.PluginService;
import com.atlassian.plugins.service.plugin.PluginVersionService;
import com.atlassian.plugins.service.product.ProductService;
import com.atlassian.upm.Sys;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PacServiceFactoryImpl implements PacServiceFactory
{
    private static final boolean LOG_REQUESTS = Boolean.valueOf(System.getProperty("pac.log.requests"));
    
    private ClientContextFactory clientContextFactory;
    
    public PacServiceFactoryImpl(ClientContextFactory clientContextFactory)
    {
        this.clientContextFactory = checkNotNull(clientContextFactory, "clientContextFactory");
    }

    public PluginVersionService getPluginVersionService()
    {
        return configure(new PluginVersionServiceClientImpl());
    }

    public PluginService getPluginService()
    {
        return configure(new PluginServiceClientImpl());
    }

    public ProductService getProductService()
    {
        return configure(new ProductServiceClientImpl());
    }

    public AuditService getAuditService()
    {
        return configure(new AuditServiceClientImpl());
    }
    
    private <T extends AbstractServiceClient> T configure(T client)
    {
        client.setBaseUrl(Sys.getPacBaseUrl());
        client.setContextFactory(clientContextFactory);
        client.setLogWebResourceRequests(LOG_REQUESTS);
        return client;
    }
}