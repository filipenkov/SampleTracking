package com.atlassian.config;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 16/03/2004
 * Time: 10:44:27
 * To change this template use File | Settings | File Templates.
 */
public interface ConfigElement
{
    public String getPropertyName();

    public Class getObjectClass();

    public void setPropertyName(String name);

    public Object getContext();

    public void setContext(Object context);

    public void save(Object object) throws ConfigurationException;

    public Object load() throws ConfigurationException;
}
