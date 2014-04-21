package com.atlassian.crowd.util;

import com.atlassian.core.util.ClassLoaderUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation tied to Spring.
 */
public class SpringContextInstanceFactory implements InstanceFactory, ApplicationContextAware
{
    private ApplicationContext applicationContext;

    public Object getInstance(String className) throws ClassNotFoundException
    {
        return getInstance(className, this.getClass().getClassLoader());
    }

    public Object getInstance(String className, ClassLoader classLoader) throws ClassNotFoundException
    {
        return getInstance(ClassLoaderUtils.loadClass(className, classLoader));
    }

    public <T> T getInstance(Class<T> clazz)
    {
        return clazz.cast(applicationContext.getAutowireCapableBeanFactory().createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false));
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = checkNotNull(applicationContext);
    }
}
