package com.atlassian.labs.jira4compat.spring;

import com.atlassian.jira.util.BuildUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 *
 */
public abstract class AbstractCompatFactoryBean implements FactoryBean
{
    private final Class serviceClass;
    private final AutowireCapableBeanFactory beanFactory;

    private static final String IDENTIFIER;

    static
    {
        String identifier = "Jira4";
        try
        {
            String buildNumber = BuildUtils.getCurrentBuildNumber();
            if (Integer.parseInt(buildNumber) >= 700)
            {
                identifier = "Jira5";
            }
        }
        catch (Throwable e)
        {
            // not JIRA 5, assume 4
        }
        IDENTIFIER = identifier;
    }

    public AbstractCompatFactoryBean(Class serviceClass, AutowireCapableBeanFactory beanFactory)
    {
        this.serviceClass = serviceClass;
        this.beanFactory = beanFactory;
    }

    public Object getObject() throws Exception
    {
        final Class<?> type = AbstractCompatFactoryBean.class.getClassLoader().loadClass("com.atlassian.labs.jira4compat.impl." + IDENTIFIER + serviceClass.getSimpleName());
        return beanFactory.createBean(type);
    }

    public Class getObjectType()
    {
        return serviceClass;
    }

    public boolean isSingleton()
    {
        return true;
    }
}
