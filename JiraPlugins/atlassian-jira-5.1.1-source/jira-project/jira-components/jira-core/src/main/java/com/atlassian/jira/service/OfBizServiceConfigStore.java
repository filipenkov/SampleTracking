package com.atlassian.jira.service;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.util.JiraUtils;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OfBiz implementation of the ServiceConfigStore.
 */
public class OfBizServiceConfigStore implements ServiceConfigStore
{
    private static final Logger log = Logger.getLogger(OfBizServiceConfigStore.class);

    private static final String ENTITY_NAME = "ServiceConfig";
    private static final String SERVICE_CONFIG_ID = "id";
    private static final String SERVICE_CONFIG_NAME = "name";
    private static final String SERVICE_CONFIG_TIME = "time";
    private static final String SERVICE_CONFIG_CLAZZ = "clazz";

    private final DelegatorInterface genericDelegator;
    private final ComponentClassManager componentClassManager;
    private static final String CANT_CREATE_SERVICE_MSG = "Unable to create a service config for service with the name : %s ";
    private static final String UNABLE_TO_FIND_CLASS_MSG = "The class '%s' could not be found.  This can happen when a plugin is uninstalled or disabled";
    private static final String A_NO_OP_SERVICE_HAS_BEEN_RETURNED_MSG = "A NoOp Service has been returned and hence '%s' will not do anything until fixed.";

    public OfBizServiceConfigStore(final DelegatorInterface genericDelegator, final ComponentClassManager componentClassManager)
    {
        this.genericDelegator = genericDelegator;
        this.componentClassManager = componentClassManager;
    }

    public JiraServiceContainer addServiceConfig(final String serviceName, final Class<? extends JiraService> serviceClass, final long serviceDelay)
            throws ServiceException, GenericEntityException
    {
        validateServiceConfigDetails(serviceName, serviceClass, serviceDelay);
        String serviceClassName = serviceClass.getName();
        // we create a dummy service so we can get to the object configurable under the covers
        final JiraService jiraService = JiraUtils.loadComponent(serviceClass);
        // Create the generic value for the service
        GenericValue serviceConfig = EntityUtils.createValue(ENTITY_NAME, EasyMap.build(SERVICE_CONFIG_NAME, serviceName, SERVICE_CONFIG_CLAZZ, serviceClassName, SERVICE_CONFIG_TIME, new Long(serviceDelay)));
        // At this stage we have inserted a row in the DB for the ServiceConfig entity, but we have more work to do.
        // Really we should be using a DB transaction, but because we are retarded, we will instead delete this entity if the following code screws up.
        boolean success = false;
        try
        {
            // always add a propertySet for this service, even though we don't have properties yet
            // Create a service container wrapper and set it up for this service config
            final PropertySet ps = createPropertySet(serviceConfig, jiraService, new HashMap<String, String[]>());
            JiraServiceContainer serviceContainer = instantiateServiceContainer(jiraService, serviceConfig, ps);
            success = true;
            return serviceContainer;
        }
        finally
        {
            if (!success)
            {
                // Retarded version of a transaction rollback.
                serviceConfig.remove();
            }
        }
    }

    public void editServiceConfig(final JiraServiceContainer config, final long delay, final Map<String, String[]> params)
            throws GenericEntityException, ServiceException
    {
        final GenericValue serviceConfigGV = getGenericValueForConfig(config);
        if (log.isDebugEnabled())
        {
            log.debug("Editing service  with id '" + serviceConfigGV.getLong(SERVICE_CONFIG_ID) + "'.");
        }

        // store the updated properties
        final PropertySet ps = createPropertySet(serviceConfigGV, config, params);

        // store the updated delay
        serviceConfigGV.set(SERVICE_CONFIG_TIME, delay);
        serviceConfigGV.store();

        // update the object to reflect the changes in the db
        config.setDelay(serviceConfigGV.getLong(SERVICE_CONFIG_TIME));
        try
        {
            config.init(ps);
        }
        catch (ObjectConfigurationException ex)
        {
            throw new ServiceException("An error occurred when initialising Service '" + config.getName() + "'.", ex);
        }
    }

    public void removeServiceConfig(final JiraServiceContainer config) throws Exception
    {
        final GenericValue serviceConfigGV = getGenericValueForConfig(config);
        removePropertySet(serviceConfigGV);
        serviceConfigGV.remove();
    }

    /**
     * Do not call this method directly. It is not private only so we can unit test the method above.
     *
     * @param serviceConfigGV service config GV
     */
    void removePropertySet(final GenericValue serviceConfigGV)
    {
        OFBizPropertyUtils.removePropertySet(serviceConfigGV);
    }

    public JiraServiceContainer getServiceConfigForName(final String name) throws GenericEntityException, ServiceException
    {
        @SuppressWarnings ("unchecked")
        final List<GenericValue> services = getGenericDelegator().findByAnd("ServiceConfig", EasyMap.build(SERVICE_CONFIG_NAME, name));
        if ((services == null) || services.isEmpty())
        {
            return null;
        }
        else if (services.size() > 1)
        {
            throw new IllegalArgumentException("Multiple services with name '" + name + "' exist.");
        }
        return getServiceContainer((services.get(0)));
    }

    public JiraServiceContainer getServiceConfigForId(final Long id) throws Exception
    {
        final GenericValue serviceConfigGV = getGenericDelegator().findByPrimaryKey("ServiceConfig", EasyMap.build(SERVICE_CONFIG_ID, id));
        if (serviceConfigGV != null)
        {
            return getServiceContainer(serviceConfigGV);
        }
        else
        {
            return null;
        }
    }

    public Collection<JiraServiceContainer> getAllServiceConfigs() throws GenericEntityException
    {
        final Collection<JiraServiceContainer> serviceConfigs = new ArrayList<JiraServiceContainer>();
        @SuppressWarnings ("unchecked")
        final Collection<GenericValue> serivceConfigGVs = getGenericDelegator().findAll(ENTITY_NAME);
        for (final GenericValue element : serivceConfigGVs)
        {
            serviceConfigs.add(getServiceContainer(element));
        }
        return serviceConfigs;
    }

    private DelegatorInterface getGenericDelegator()
    {
        return genericDelegator;
    }

    GenericValue getGenericValueForConfig(final JiraServiceContainer config) throws GenericEntityException
    {
        return getGenericDelegator().findByPrimaryKey("ServiceConfig", EasyMap.build(SERVICE_CONFIG_ID, config.getId()));
    }

    private JiraServiceContainer getServiceContainer(final GenericValue serviceConfigGV)
    {
        final PropertySet ps = OFBizPropertyUtils.getPropertySet(serviceConfigGV);
        try
        {
            return this.instantiateServiceContainer(serviceConfigGV, ps);
        }
        catch (final ClassNotFoundServiceException cnfse)
        {
            final String serviceName = serviceConfigGV.getString(SERVICE_CONFIG_NAME);
            // Log message with stacktrace - this can be due to user error like removing a plugin,
            // but is also likely to point to a plugin that is requesting Services too early.
            log.error(String.format(CANT_CREATE_SERVICE_MSG, serviceName), cnfse);
            log.error(String.format(UNABLE_TO_FIND_CLASS_MSG, cnfse.getClazzName()));
            log.error(String.format(A_NO_OP_SERVICE_HAS_BEEN_RETURNED_MSG, serviceName));
            //fall through
        }
        catch (ServiceException se)
        {
            log.error(String.format(CANT_CREATE_SERVICE_MSG, serviceConfigGV.getString(SERVICE_CONFIG_NAME)), se);
            //fall through
        }
        catch (Exception e)
        {
            log.error(String.format(CANT_CREATE_SERVICE_MSG, serviceConfigGV.getString(SERVICE_CONFIG_NAME)), e);
            //fall through
        }

        // We need a place holder so that someone can delete a service that is un-loadable. Lets create one.
        return new UnloadableJiraServiceContainer(serviceConfigGV.getLong(SERVICE_CONFIG_ID),
                serviceConfigGV.getString(SERVICE_CONFIG_NAME), serviceConfigGV.getString(SERVICE_CONFIG_CLAZZ), serviceConfigGV.getLong(SERVICE_CONFIG_TIME));
    }

    private JiraServiceContainer instantiateServiceContainer(final GenericValue serviceConfigGV, final PropertySet ps) throws ServiceException
    {
        final String className = serviceConfigGV.getString(SERVICE_CONFIG_CLAZZ);
        try
        {
            final JiraService service = loadServiceClass(className);
            return instantiateServiceContainer(service, serviceConfigGV, ps);
        }
        catch (ClassNotFoundException e)
        {
            throw new ClassNotFoundServiceException("Could not find class: " + className, className,  e);
        }
    }

    private JiraServiceContainer instantiateServiceContainer(final JiraService service, final GenericValue serviceConfigGV, final PropertySet ps) throws ServiceException
    {
        final Long id = serviceConfigGV.getLong(SERVICE_CONFIG_ID);

        try
        {
            final JiraServiceContainer jiraServiceContainer = new JiraServiceContainerImpl(service, id);
            jiraServiceContainer.init(ps);
            jiraServiceContainer.setDelay(serviceConfigGV.getLong(SERVICE_CONFIG_TIME));
            jiraServiceContainer.setName(serviceConfigGV.getString(SERVICE_CONFIG_NAME));

            return jiraServiceContainer;
        }
        catch (ObjectConfigurationException e)
        {
            throw new ServiceException("Could not initialize service '" + service.getName() + "'.", e);
        }
        catch (RuntimeException e)
        {
            throw new ServiceException("Error creating service '" + service.getName() + "'.", e);
        }
    }

    /**
     * This will create / update the ObjectConfigurable related property set that is associated with the service
     *
     * @param serviceGV  the serviceGV in play
     * @param service    the service itself
     * @param properties the map of properties
     *
     * @return a property set
     *
     * @throws ServiceException If an error occurs while getting the Service's ObjectConfigurationException
     */
    private PropertySet createPropertySet(final GenericValue serviceGV, final JiraService service, final Map<String, String[]> properties)
            throws ServiceException
    {
        final PropertySet ps = OFBizPropertyUtils.getPropertySet(serviceGV);

        // set all the possible accepted parameters - blank or missing params are set to null
        String[] fieldKeys = new String[0];
        try
        {
            fieldKeys = service.getObjectConfiguration().getFieldKeys();
        }
        catch (ObjectConfigurationException ex)
        {
            throw new ServiceException("Unable to get ObjectConfiguration for Service '" + service.getName() + "'.", ex);
        }
        for (final String paramName : fieldKeys)
        {
            if (properties.containsKey(paramName))
            {
                String paramValue = (properties.get(paramName))[0];
                if (!TextUtils.stringSet(paramValue))
                {
                    // TODO: Why change empty string to null and then remove? Now we don't see empty fields in the read-only view.
                    paramValue = null;
                }

                if (paramValue != null)
                {
                    ps.setString(paramName, paramValue);
                }
                else if (ps.exists(paramName))
                {
                    ps.remove(paramName);
                }
            }
            else
            {
                // JRA-16949 This was added so the we can uncheck a checkbox in config.
                // The fact that the property is removed rather then set to "false" or "" or whatever is a bit ugly
                // because then that property is not displayed in the read-only view, however this is current behaviour
                // that I don't intend to address now.
                if (ps.exists(paramName))
                {
                    ps.remove(paramName);
                }
            }
        }
        return ps;
    }

    private static class ClassNotFoundServiceException extends ServiceException
    {

        private final String clazzName;

        private ClassNotFoundServiceException(final String msg, final String clazzName, final Exception e)
        {
            super(msg, e);
            this.clazzName = clazzName;
        }

        public String getClazzName()
        {
            return clazzName;
        }

    }

    /**
     * The web UI layer does validation but we should do some more since plugins can programmtically add service
     * configs.
     *
     * @param serviceName      the name of the service
     * @param serviceClass     the JiraService class
     * @param serviceDelay     the delay
     *
     * @throws ServiceException if its not valid
     */
    private void validateServiceConfigDetails(final String serviceName, final Class<? extends JiraService> serviceClass, final long serviceDelay) throws ServiceException
    {
        if (StringUtils.isBlank(serviceName))
        {
            throw new ServiceException("The service name must not be blank.");
        }
        if (serviceClass == null)
        {
            throw new ServiceException("The service class must not be null.");
        }
        if (serviceDelay <= 0)
        {
            throw new ServiceException("The service delay must be greater than 0.");
        }
    }

    private JiraService loadServiceClass(final String serviceClassName) throws ClassNotFoundException
    {
        return componentClassManager.newInstance(serviceClassName);
    }
}