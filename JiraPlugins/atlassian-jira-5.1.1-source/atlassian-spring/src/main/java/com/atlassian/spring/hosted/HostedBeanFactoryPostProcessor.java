package com.atlassian.spring.hosted;

import java.lang.annotation.Annotation;
import java.net.URL;

import com.atlassian.spring.extension.HostedOverrideBeanDefinitionDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.Ordered;
import org.springframework.core.io.UrlResource;

/**
 * Hosted bean factory post processor to allow Hosted to override components in Spring.
 * <p/>
 * Bean factory post processors are invoked after all the bean definitions have been registered (that is, after the XML
 * files have all been parsed), but before any beans actually get instantiated.
 * <p/>
 * There are limitations to this, namely from Spring bean definition decorators that are applied before this post
 * processor gets applied:
 * <p/>
 * <ol> <li>A number of bean definition decorators/processors will rename a bean, and create a new bean definition in
 * its place.  Examples of this are the spring auto proxying decorators, and the multitenant decorators.  Overriding
 * these beans may have weird consequences.</li> <li>Decorators such as the PluginAvailableBeanDefinitionDecorator may
 * get confused, especially if both the old and the new bean definition have the attribute on it.  If overriding a
 * plugin:available bean definition, it is probably wise to not annotate the overriding bean with plugin:available, as
 * the old bean name will still be referenced by the host component provider.</li> </ol>
 *
 * @since 2.0
 */
public class HostedBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered
{
    private static final Logger log = LoggerFactory.getLogger(HostedBeanFactoryPostProcessor.class);

    private String resource = "META-INF/hosted-application-context-overrides.xml";

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
        // Check that its of right type first, so Confluence will notice is broken before Hosted tries to use it
        if (!(beanFactory instanceof BeanDefinitionRegistry))
        {
            throw new IllegalArgumentException("Bean factory must be an instance of " + BeanDefinitionRegistry.class.getName() +
                    ", otherwise this post processor can't do its job.");
        }
        URL url = getHostedOverrides();
        if (url != null)
        {
            log.info("Overriding application context with " + url.toString());

            validateOverridingContext(beanFactory, url);

            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader((BeanDefinitionRegistry) beanFactory);
            // This will load all the bean definitions from the override file into the registry.  It will override any
            // existing bean definitions, by name.
            reader.loadBeanDefinitions(new UrlResource(url));
        }
    }

    /**
     * Validate whether the context file pointed to the URL is only overriding beans that are allowed to be overridden
     *
     * @param beanFactory The bean factory that beans will be overridden in
     * @param url The URL of the context file
     * @throws HostedOverrideNotAllowedException If a bean is overridden that is not allowed to be overridden
     */
    private void validateOverridingContext(ConfigurableListableBeanFactory beanFactory, URL url)
            throws HostedOverrideNotAllowedException
    {
        BeanDefinitionRegistry validatingRegistry = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader validationReader = new XmlBeanDefinitionReader(validatingRegistry);
        validationReader.loadBeanDefinitions(new UrlResource(url));

        for (String name : validatingRegistry.getBeanDefinitionNames())
        {
            if (beanFactory.containsBeanDefinition(name))
            {
                try
                {
                    BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name);
                    if (!allowedToOverride(beanDefinition))
                    {
                        throw new HostedOverrideNotAllowedException(name, url);
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Overriding bean: " + name);
                        }
                    }
                }
                catch (NoSuchBeanDefinitionException e)
                {
                    // It's not overriding, that's ok
                    if (log.isDebugEnabled())
                    {
                        log.debug("Allowing non overriding bean: " + name);
                    }
                }
            }
        }
    }

    public int getOrder()
    {
        // We want Hosted to get in there and override beans before things like property placeholders have got in and
        // updated properties
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Set the resource to load, if it exists.  By default, this is <code>META-INF/hosted-application-context-overrides.xml</code>.
     *
     * @param resource The resource to load.
     */
    public void setResource(String resource)
    {
        this.resource = resource;
    }

    private URL getHostedOverrides()
    {
        // We're not interested in a list of all the resources with this name, because there is only one Hosted,
        // and multiple plugins trying to override behaviour will just get messy.  We also don't want to encourage
        // plugin developers to use this, which supporting multiple files would encourage.
        return getClass().getClassLoader().getResource(resource);
    }

    /**
     * Check whether the given beanDefinition is allowed to be overridden
     *
     * @param beanDefinition The bean definition
     * @return True if it is allowed to be overridden
     */
    private boolean allowedToOverride(BeanDefinition beanDefinition)
    {
        if (beanDefinition.hasAttribute(HostedOverrideBeanDefinitionDecorator.OVERRIDE))
        {
            return Boolean.parseBoolean(beanDefinition.getAttribute(HostedOverrideBeanDefinitionDecorator.OVERRIDE).toString());
        }

        String className = beanDefinition.getBeanClassName();
        if (className != null)
        {
            try
            {
                Class clazz = Class.forName(className);
                if (hasAnnotation(clazz, AllowHostedOverride.class))
                {
                    return true;
                }
            }
            catch (ClassNotFoundException e)
            {
                // Ignore, I don't think we should reach this point but if we do, we're probably not allowed to ovirride
                // it anyway. Log at error level because this will cause the app to fail in coming up.
                log.error("Could not find class for potential override", e);
            }
        }
        return false;
    }

    private boolean hasAnnotation(Class clazz, Class<? extends Annotation> annotation)
    {
        if (clazz.getAnnotation(annotation) != null)
        {
            return true;
        }
        else
        {
            // Try all interfaces
            for (Class inter : clazz.getInterfaces())
            {
                if (hasAnnotation(inter, annotation))
                {
                    return true;
                }
            }
            // Try super class
            if (clazz.getSuperclass() != null)
            {
                return hasAnnotation(clazz.getSuperclass(), annotation);
            }
            return false;
        }
    }
}