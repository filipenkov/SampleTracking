/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd. All rights reserved.
 */
package com.atlassian.spring.container;

import com.atlassian.event.Event;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

/**
 * Implementation  of ContainerContext to allow compoennts to be retrieved from a spring container
 */
public class SpringContainerContext implements ContainerContext
{
    private static final Logger log = Logger.getLogger(SpringContainerContext.class);

    private volatile ServletContext servletContext;
    private volatile ApplicationContext applicationContext;
    // @TODO Why are we inited this here like this? The only thing is maybe if Confluence needs to creteComponent before Spring is setup
    private volatile AtlassianBeanFactory beanFactory = new AtlassianBeanFactory(null);

    public void setServletContext(ServletContext context)
    {
        servletContext = context;
        setApplicationContext(WebApplicationContextUtils.getWebApplicationContext(context));
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    /* (non-Javadoc)
     * @see com.atlassian.confluence.setup.container.ContainerContext#getComponent(java.lang.Object)
     */
    public Object getComponent(Object key) throws ComponentNotFoundException
    {
        if (applicationContext == null)
        {
            log.fatal("Spring Application context has not been set");
            throw new IllegalStateException("Spring Application context has not been set");
        }

        if (key == null)
        {
            log.error("The component key cannot be null");
            throw new ComponentNotFoundException("The component key cannot be null");
        }
        if (key instanceof Class)
        {
            //We will assume that there should only be one object of
            //this class in the container for now
            String[] names = beanFactory.getBeanNamesForType((Class)key);
            if (names == null || names.length == 0 || names.length > 1)
            {
                throw new ComponentNotFoundException("The container is unable to resolve single instance of "
                        + ((Class) key).getName()
                        + " number of instances found was: "
                        + names.length);
            }
            else
            {
                key = names[0];
            }
        }
        try
        {
            return beanFactory.getBean(key.toString());
        }
        catch (BeansException e)
        {
            throw new ComponentNotFoundException("Failed to find component: " + e.getMessage(), e);
        }
    }

    public Object createComponent(Class clazz)
    {
        return beanFactory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }

    public Object createCompleteComponent(Class clazz)
    {
        return beanFactory.createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }

    /**
     * autowire the dependences a bean that has already been created
     *
     * @param bean
     */
    public void autowireComponent(Object bean)
    {
        if (beanFactory != null)
        {
            beanFactory.autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
        }
        else
        {
            log.debug("ApplicationContext is null or has not been set. Cannot proceed with autowiring of component: " + bean);
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
    public void setApplicationContext(ApplicationContext appContext)
            throws ApplicationContextException
    {
        if (appContext != null)
        {
            applicationContext = appContext;
            beanFactory = new AtlassianBeanFactory(appContext.getAutowireCapableBeanFactory());
        }
        else
        {
            applicationContext = null;
            beanFactory = null;
        }
    }

    /*
	 * @see com.atlassian.confluence.setup.container.ContainerContext#refresh()
	 */
    public synchronized void refresh()
    {
        ContextLoader loader = new ContextLoader();

        // if we have an existing spring context, ensure we close it properly
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        if (ctx != null)
        {
            loader.closeWebApplicationContext(servletContext);
        }

        loader.initWebApplicationContext(servletContext);

        if (applicationContext == null)
        {
            setApplicationContext(WebApplicationContextUtils.getWebApplicationContext(servletContext));
        }

        contextReloaded();
    }

    public boolean isSetup()
    {
        return applicationContext != null;
    }

    protected void contextReloaded()
    {
        if (applicationContext != null)
        {
            applicationContext.publishEvent(new ContainerContextLoadedEvent(applicationContext));
        }
    }

    protected ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    public void publishEvent(Event e)
    {
        applicationContext.publishEvent(e);
    }
}
