package com.atlassian.plugins.rest.module;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A class loader that delegates to a list of class loaders. The order is important as classes and resources will be
 * loaded from the first classloader that can load them.
 */
public class ChainingClassLoader extends ClassLoader
{
    private static final Logger log = LoggerFactory.getLogger(ChainingClassLoader.class);

    private static final String SERVICES_PREFIX = "META-INF/services";
    private static final String ALTERNATE_SERVICES_PREFIX = "META-INF/alternate-services";
    private static final Set<String> ALTERNATE_SERVICES = ImmutableSet.of(
            SERVICES_PREFIX + "/com.sun.jersey.server.impl.model.method.dispatch.ResourceMethodDispatchProvider",
            SERVICES_PREFIX + "/com.sun.jersey.spi.container.ContainerProvider",
            SERVICES_PREFIX + "/com.sun.jersey.spi.container.ContainerRequestFilter",
            SERVICES_PREFIX + "/com.sun.jersey.spi.container.WebApplicationProvider",
            SERVICES_PREFIX + "/com.sun.jersey.spi.HeaderDelegateProvider",
            SERVICES_PREFIX + "/com.sun.jersey.spi.StringReaderProvider",
            SERVICES_PREFIX + "/javax.ws.rs.ext.MessageBodyReader",
            SERVICES_PREFIX + "/javax.ws.rs.ext.MessageBodyWriter",
            SERVICES_PREFIX + "/javax.ws.rs.ext.RuntimeDelegate");

    /**
     * The list of classloader to delegate to.
     */
    private final List<ClassLoader> classLoaders;

    public ChainingClassLoader(ClassLoader... classLoaders)
    {
        Validate.noNullElements(classLoaders, "ClassLoader arguments cannot be null");
        this.classLoaders = Arrays.asList(classLoaders);
    }

    @Override
    public Class loadClass(String name) throws ClassNotFoundException
    {
        for (ClassLoader classloader : classLoaders)
        {
            try
            {
                return classloader.loadClass(name);
            }
            catch (ClassNotFoundException e)
            {
                // ignoring until we reach the end of the list since we are chaining
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException
    {
        return new ResourcesEnumeration(getAlternativeResourceName(name), classLoaders);
    }

    @Override
    public URL getResource(String name)
    {
        final String realResourceName = getAlternativeResourceName(name);
        for (ClassLoader classloader : classLoaders)
        {
            final URL url = classloader.getResource(realResourceName);
            if (url != null)
            {
                return url;
            }
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name)
    {
        final String realResourceName = getAlternativeResourceName(name);
        for (ClassLoader classloader : classLoaders)
        {
            final InputStream inputStream = classloader.getResourceAsStream(realResourceName);
            if (inputStream != null)
            {
                return inputStream;
            }
        }

        if (!name.equals(realResourceName))
        {
            //looks like we didn't find anything with the alternate resourcename.  Lets fall back to the
            //original resource name!
            log.debug("No resource found with alternate resourceName '{}'. Falling back to original name '{}'.", realResourceName, name);
            for (ClassLoader classloader : classLoaders)
            {
                final InputStream inputStream = classloader.getResourceAsStream(name);
                if (inputStream != null)
                {
                    return inputStream;
                }
            }
        }
        return null;
    }

    private String getAlternativeResourceName(String name)
    {
        if (ALTERNATE_SERVICES.contains(name))
        {
            log.debug("Service '{}' is registered as an alternate service.", name);
            return StringUtils.replace(name, SERVICES_PREFIX, ALTERNATE_SERVICES_PREFIX, 1);
        }
        else
        {
            return name;
        }
    }

    @Override
    public synchronized void setDefaultAssertionStatus(boolean enabled)
    {
        for (ClassLoader classloader : classLoaders)
        {
            classloader.setDefaultAssertionStatus(enabled);
        }
    }

    @Override
    public synchronized void setPackageAssertionStatus(String packageName, boolean enabled)
    {
        for (ClassLoader classloader : classLoaders)
        {
            classloader.setPackageAssertionStatus(packageName, enabled);
        }
    }

    @Override
    public synchronized void setClassAssertionStatus(String className, boolean enabled)
    {
        for (ClassLoader classloader : classLoaders)
        {
            classloader.setClassAssertionStatus(className, enabled);
        }
    }

    @Override
    public synchronized void clearAssertionStatus()
    {
        for (ClassLoader classloader : classLoaders)
        {
            classloader.clearAssertionStatus();
        }
    }

    private static final class ResourcesEnumeration implements Enumeration<URL>
    {
        private final List<Enumeration<URL>> resources;
        private final String resourceName;

        ResourcesEnumeration(String resourceName, List<ClassLoader> classLoaders) throws IOException
        {
            this.resourceName = resourceName;
            this.resources = new LinkedList<Enumeration<URL>>();
            for (ClassLoader classLoader : classLoaders)
            {
                resources.add(classLoader.getResources(resourceName));
            }
        }

        public boolean hasMoreElements()
        {
            for (Enumeration<URL> resource : resources)
            {
                if (resource.hasMoreElements())
                {
                    return true;
                }
            }

            return false;
        }

        public URL nextElement()
        {
            for (Enumeration<URL> resource : resources)
            {
                if (resource.hasMoreElements())
                {
                    return resource.nextElement();
                }
            }
            throw new NoSuchElementException(resourceName);
        }
    }
}
