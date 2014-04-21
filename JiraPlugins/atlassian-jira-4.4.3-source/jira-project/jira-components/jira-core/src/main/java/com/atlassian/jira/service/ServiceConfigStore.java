package com.atlassian.jira.service;

import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.Map;

/**
 * A store interface for the ServiceConfigs
 */
public interface ServiceConfigStore
{
    /**
     * Adds a new Service of the given class with the the given configuration.
     *
     * @param serviceName The service name.
     * @param serviceClass The JiraService class that we wish to add as a service.
     * @param serviceDelay the service delay.
     *
     * @return JiraServiceContainer for this service.
     *
     * @throws ServiceException If there is any errors trying to add this Service.
     * @throws GenericEntityException A DB error.
     */
    public JiraServiceContainer addServiceConfig(String serviceName, Class<? extends JiraService> serviceClass, long serviceDelay)
            throws ServiceException, GenericEntityException;

    public void editServiceConfig(JiraServiceContainer config, long delay, Map<String, String[]> params) 
            throws ServiceException, GenericEntityException;

    public void removeServiceConfig(JiraServiceContainer config) throws Exception;

    public JiraServiceContainer getServiceConfigForId(Long id) throws Exception;

    public JiraServiceContainer getServiceConfigForName(String name) throws Exception;

    public Collection<JiraServiceContainer> getAllServiceConfigs() throws Exception;
}
