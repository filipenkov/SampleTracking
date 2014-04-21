package com.atlassian.config;

import com.atlassian.plugin.util.ClassLoaderUtils;
import org.apache.log4j.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * A simple strategy bean to locate the application home directory.
 */
public class DefaultHomeLocator implements HomeLocator
{
    private static final Logger log = Logger.getLogger(DefaultHomeLocator.class);
    private String initPropertyName;
    private String propertiesFile;
    private String configFileName;
    private String servletHomeProperty;

    public DefaultHomeLocator()
    {
    }

    /**
     * Use this method to try to get the home variable.
     * <p/>
     * This method looks for the variable in the following places (in order):
     * <ul>
     * <li>System property
     * <li>properties file
     * <li>Servlet context init param</li>
     * </ul>
     *
     * @return
     */
    public String getHomePath()
    {
        // Allow configured home to be overridden with system property.
        String home = getHomeFromSystemProperty();
        // If we could not get the home location from the system properties, try the config file
        if (home == null)
        {
            home = getHomeFromConfigFile();
        }
        // As a fall-back, try getting the location from the servlet context parameters (CONF-4054)
        if (home == null)
        {
            home = servletHomeProperty;
        }
        if (log.isDebugEnabled())
        {
            log.debug("Found " + initPropertyName + "  property with value: " + home);
        }
        return home;
    }

    public String getConfigFileName()
    {
        return configFileName;
    }

    public void setConfigFileName(String configFileName)
    {
        this.configFileName = configFileName;
    }

    private String getHomeFromSystemProperty()
    {
        log.debug("Trying to load " + initPropertyName + " from System property parameter... ");
        String sysProperty = System.getProperty(initPropertyName);
        if (sysProperty == null)
        {
            log.debug("Could not find " + initPropertyName + " property as a System property.");
        }
        return sysProperty;
    }

    private String getHomeFromConfigFile()
    {
        log.debug("Trying to load " + initPropertyName + " from properties file... ");
        String confHome = null;
        try
        {
            Properties props = new Properties();
            URL url = ClassLoaderUtils.getResource(getPropertiesFile(), DefaultHomeLocator.class);
            if (url != null)
            {
                InputStream inputStream = null;
                try
                {
                    inputStream = url.openStream();
                    props.load(inputStream);
                }
                finally
                {
                    IOUtils.closeQuietly(inputStream);
                }
            }
            if (props.getProperty(initPropertyName) != null)
            {
                confHome = props.getProperty(initPropertyName);
            }
            else
            {
                log.debug("Could not find " + initPropertyName + " property in the " + getPropertiesFile() + " file. trying other methods.");
            }
        }
        catch (IOException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Could not find " + getPropertiesFile() + " in the classpath, trying other methods.");
            }
        }
        return confHome;
    }

    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

    public void setInitPropertyName(String initPropertyName)
    {
        this.initPropertyName = initPropertyName;
    }

    public void lookupServletHomeProperty(ServletContext context)
    {
        log.debug("Trying to load " + initPropertyName + " from servlet context parameter... ");
        if ((context != null) && (context.getInitParameter(initPropertyName) != null))
        {
            servletHomeProperty = context.getInitParameter(initPropertyName);
        }
        else
        {
            log.debug("Could not find " + initPropertyName + " property in the servlet context. Trying other methods.");
        }
    }
}
