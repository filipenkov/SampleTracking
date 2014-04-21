package com.atlassian.labs.jira4compat.spring;

import com.atlassian.labs.jira4compat.spi.CompatProjectTabPanelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class CompatProjectTabPanelConverterFactoryBean extends AbstractCompatFactoryBean
{
    @Autowired
    public CompatProjectTabPanelConverterFactoryBean(AutowireCapableBeanFactory beanFactory)
    {
        super(CompatProjectTabPanelFactory.class, beanFactory);
    }
}
