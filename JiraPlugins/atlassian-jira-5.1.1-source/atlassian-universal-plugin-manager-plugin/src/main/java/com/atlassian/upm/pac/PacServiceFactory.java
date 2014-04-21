package com.atlassian.upm.pac;

import com.atlassian.plugins.service.audit.AuditService;
import com.atlassian.plugins.service.plugin.PluginService;
import com.atlassian.plugins.service.plugin.PluginVersionService;
import com.atlassian.plugins.service.product.ProductService;

/**
 * A factory for the getting services that connect to PAC.
 */
public interface PacServiceFactory
{
    /**
     * Get an instance of the plugin version service.
     *
     * @return an instance of the plugin version service
     */
    PluginVersionService getPluginVersionService();

    /**
     * Get an instance of the plugin service.
     *
     * @return an instance of the plugin service
     */
    PluginService getPluginService();

    /**
     * Get an instance of the product service.
     *
     * @return an instance of the product service
     */
    ProductService getProductService();

    /**
     * Get an instance of the audit service.
     * 
     * @return an instance of the audit server
     */
    AuditService getAuditService();
}
