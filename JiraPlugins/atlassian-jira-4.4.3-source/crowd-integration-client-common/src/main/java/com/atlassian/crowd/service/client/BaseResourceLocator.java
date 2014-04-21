package com.atlassian.crowd.service.client;

import com.atlassian.crowd.util.Assert;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Abstract class with a few methods that will help locate a given resource
 */
public abstract class BaseResourceLocator implements ResourceLocator
{
    protected final Logger logger = Logger.getLogger(this.getClass());

    private String resourceName;
    protected String propertyFileLocation;

    protected BaseResourceLocator(String resourceName)
    {
        Assert.notNull(resourceName);
        this.resourceName = resourceName;
    }

    /**
     * Returns the location of the property <tt>resourceName</tt> on the filesystem, based off the location
     * of a system property, it is returned as a URL.toExternalForm();
     *
     * @return location of the resource
     */
    protected String getResourceLocationFromSystemProperty()
    {
        String fileLocation = System.getProperty(resourceName);

        return formatFileLocation(fileLocation);

    }

    protected String formatFileLocation(String fileLocation)
    {
        String url = null;
        if (fileLocation != null)
        {
            File file = new File(fileLocation);
            if (file.exists() && file.canRead())
            {
                try
                {
                    url = file.toURI().toURL().toExternalForm();
                }
                catch (MalformedURLException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            else
            {
                logger.info("The file cannot be read or does not exist: " + fileLocation);
            }
        }
        return url;
    }

    protected String getResourceLocationFromClassPath()
    {
        URL resource = getClassLoaderResource();
        return resource != null ? resource.toExternalForm() : null;
    }

    /**
     * Load a given resource.
     * <p/>
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     * <li>From {@link Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     * <li>From {@link Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     * </ul>
     * @return URL of the class loader resource
     */
    protected URL getClassLoaderResource()
    {
        URL url;

        url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        if (url == null)
        {
            url = BaseResourceLocator.class.getClassLoader().getResource(resourceName);
        }

        return url;
    }

    public String getResourceName()
    {
        return resourceName;
    }

    public Properties getProperties()
    {
        Properties properties = null;

        try
        {
            URL url = new URL(getResourceLocation());

            properties = getPropertiesFromStream(url.openStream());
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }

        return properties;
    }

    private Properties getPropertiesFromStream(InputStream is)
    {
        if (is == null)
        {
            return null;
        }

        Properties props = new Properties();
        try
        {
            props.load(is);
        }
        catch (IOException e)
        {
            logger.error("Error loading properties from stream.", e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                logger.error("Failed to close the stream: " + e.getMessage(), e);
            }
        }

        return props;
    }

    public String getResourceLocation()
    {
        return propertyFileLocation;
    }
}
