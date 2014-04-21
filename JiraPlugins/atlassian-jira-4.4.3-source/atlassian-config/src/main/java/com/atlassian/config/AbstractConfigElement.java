package com.atlassian.config;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 16/03/2004
 * Time: 13:07:59
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractConfigElement implements ConfigElement
{
    private String propertyName;
    private AbstractConfigurationPersister config;

    public AbstractConfigElement(String name, Object context, AbstractConfigurationPersister config)
    {
        this.propertyName = name;
        setContext(context);
        this.config = config;
    }

    public final void save(Object object) throws ConfigurationException
    {
        checkSaveObject(object);
        saveConfig(object);
    }

    public final Object load() throws ConfigurationException
    {
        return loadConfig();
    }


    protected void checkSaveObject(Object object) throws ConfigurationException
    {
        if (object == null)
        {
            throw new ConfigurationException("Object to save cannot be null");
        }

        if (!getObjectClass().isAssignableFrom(object.getClass()))
        {
            throw new ConfigurationException("Object to save was not of expected type. Expected type was: " + getObjectClass() +
                    ", actual type is: " + object.getClass().getName());
        }
    }

    public AbstractConfigurationPersister getConfiguration()
    {
        return config;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public void setPropertyName(String name)
    {
        propertyName = name;
    }

    protected abstract Object loadConfig() throws ConfigurationException;

    protected abstract void saveConfig(Object object) throws ConfigurationException;
}
