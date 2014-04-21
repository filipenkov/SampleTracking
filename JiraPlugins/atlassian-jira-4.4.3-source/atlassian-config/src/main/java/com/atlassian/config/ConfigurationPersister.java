package com.atlassian.config;

import java.io.InputStream;

public interface ConfigurationPersister
{
    void addConfigMapping(Class propertyType, Class configType);

    void save(String configPath, String configFile) throws ConfigurationException;

    Object load(String configPath, String configFile) throws ConfigurationException;

    Object load(InputStream is) throws ConfigurationException;

    void addConfigElement(Object item, String propertyName) throws ConfigurationException;

    Object getConfigElement(Class propertyType, String propertyName) throws ConfigurationException;

    String getStringConfigElement(String propertyName) throws ConfigurationException;

    void clear();
}
