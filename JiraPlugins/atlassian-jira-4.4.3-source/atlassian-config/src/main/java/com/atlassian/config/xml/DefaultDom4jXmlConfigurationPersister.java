package com.atlassian.config.xml;

import com.atlassian.config.ConfigurationException;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class DefaultDom4jXmlConfigurationPersister extends AbstractDom4jXmlConfigurationPersister
{
    private static final Logger log = Logger.getLogger(DefaultDom4jXmlConfigurationPersister.class);

    public String getRootName()
    {
        return "application-configuration";
    }

    public synchronized void save(String configPath, String configFile) throws ConfigurationException
    {
        saveDocument(configPath, configFile);
    }

    public Object load(InputStream is) throws ConfigurationException
    {
        try
        {
            loadDocument(is);
        }
        catch (DocumentException e)
        {
            throw new ConfigurationException("Failed to parse config file: " + e.getMessage(), e);
        }

        return null;
    }

    public String getStringConfigElement(String elementName)
    {
        String val = null;
        try
        {
            val = (String) getConfigElement(String.class, elementName);
        }
        catch (ConfigurationException e)
        {
            log.fatal("Could not load text from " + elementName +" element: " + e.getMessage());
        }

        return val;
    }

    public Object load(String configPath, String configFile) throws ConfigurationException
    {
        if (configPath == null)
        {
            configPath = ".";
        }

        try
        {
            return load(new FileInputStream(new File(configPath + "/" + configFile)));
        }
        catch (FileNotFoundException e)
        {
            throw new ConfigurationException("failed to find config at: " + configPath + "/" + configFile, e);
        }
    }
}
