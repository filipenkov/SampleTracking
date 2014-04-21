package com.atlassian.config;

import com.atlassian.core.util.ClassHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractConfigurationPersister implements ConfigurationPersister
{
    private Map configMappings = new HashMap();

    public void addConfigMapping(Class propertyType, Class configType)
    {
        configMappings.put(propertyType, configType);
    }

    public abstract Object getRootContext();

    public void addConfigElement(Object item, String propertyName) throws ConfigurationException
    {
        addConfigElement(item, propertyName, getRootContext());
    }

    public Object getConfigElement(Class propertyType, String propertyName) throws ConfigurationException
    {
        return getConfigElement(propertyType, propertyName, getRootContext());
    }

    public String getStringConfigElement(String propertyName) throws ConfigurationException
    {
        return (String) getConfigElement(String.class, propertyName);
    }

    public void addConfigElement(Object item, String propertyName, Object context) throws ConfigurationException
    {
        Class clazz = null;
        Map.Entry entry;

        if (item == null)
            return;

        for (Iterator iterator = configMappings.entrySet().iterator(); iterator.hasNext();)
        {
            entry = (Map.Entry) iterator.next();
            Class c = (Class) entry.getKey();
            if (c.isAssignableFrom(item.getClass()))
            {
                clazz = (Class) entry.getValue();
                break;
            }
        }
        if (clazz != null)
        {
            try
            {
                ConfigElement config = (ConfigElement) ClassHelper.instantiateClass(clazz, new Object[]{propertyName, context, this});
                config.save(item);
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Failed to create config element: " + clazz.getName(), e);
            }
        }
        else
        {
            throw new ConfigurationException("Failed to find config element for " + item.getClass().getName());
        }
    }

    public Object getConfigElement(Class propertyType, String propertyName, Object context) throws ConfigurationException
    {
        Class clazz = (Class) configMappings.get(propertyType);
        if (clazz != null)
        {
            try
            {
                ConfigElement config = (ConfigElement) ClassHelper.instantiateClass(clazz, new Object[]{propertyName, context, this});
                return config.load();
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Failed to create config element: " + clazz.getName(), e);
            }
        }
        else
        {
            throw new ConfigurationException("Failed to find config element for " + propertyType.getName());
        }
    }
}
