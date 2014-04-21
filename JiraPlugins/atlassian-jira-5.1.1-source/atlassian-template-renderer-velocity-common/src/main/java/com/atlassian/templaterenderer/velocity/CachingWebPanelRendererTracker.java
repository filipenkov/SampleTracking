package com.atlassian.templaterenderer.velocity;

import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Tracks the creation of {@link AbstractCachingWebPanelRenderer} and invokes
 * {@link com.atlassian.templaterenderer.velocity.AbstractCachingWebPanelRenderer#destroy()} method when the
 * application context is closed.
 *
 * The subclasses of {@link AbstractCachingWebPanelRenderer} are defined in atlassian-plugin.xml as web-panel-renderer
 * instead of component. So they are not defined as a bean in the generated
 * META-INF/spring/atlassian-plugins-components.xml. They are instantiated by the WebPanelRendererModuleDescriptor
 * invoking the createModule() method of a ModuleFactory, which in a Spring-backed plugin system calls the Spring
 * IoC container programmatically to instantiate an instance of the given module class and autowire it. Beans created
 * this way are not defined as part of the Spring application context; thus, when the Spring application context is
 * closed, the destroy() method of these beans are not called even if they implement the {@link DisposableBean}
 * interface. Therefore, we need to track {@link AbstractCachingWebPanelRenderer} programmatically.
 *
 * @since v1.3.4
 */
public class CachingWebPanelRendererTracker implements BeanPostProcessor, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(CachingWebPanelRendererTracker.class);
    private static final Object identityMapValue = new Object();

    // Used as an IdentitySet
    private final Map<AbstractCachingWebPanelRenderer, Object> tracked =
            new IdentityHashMap<AbstractCachingWebPanelRenderer, Object>();

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
    {
        if (bean instanceof AbstractCachingWebPanelRenderer)
        {
            log.debug("Tracking a WebPanelRenderer {}", bean);
            synchronized (tracked)
            {
                tracked.put((AbstractCachingWebPanelRenderer) bean, identityMapValue);
            }
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
    {
        return bean;
    }

    public void destroy() throws Exception
    {
        synchronized (tracked)
        {
            for (AbstractCachingWebPanelRenderer render : tracked.keySet())
            {
                destroy(render);
            }
            tracked.clear();
        }
    }

    private void destroy(AbstractCachingWebPanelRenderer render)
    {
        try
        {
            render.destroy();
        }
        catch (Exception e)
        {
            log.warn("Exception trying to destroy " + render, e );
        }
    }

    /*
     * Intended to be only used by unit tests.
     */
    int numberOfTracked()
    {
        synchronized (tracked)
        {
            return tracked.size();
        }
    }
}
