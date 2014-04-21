package com.atlassian.labs.jira4compat.spring;

import com.atlassian.labs.jira4compat.spi.CompatViewProfilePanelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class CompatViewProfileConverterFactoryBean extends AbstractCompatFactoryBean
{
    @Autowired
    public CompatViewProfileConverterFactoryBean(AutowireCapableBeanFactory beanFactory)
    {
        super(CompatViewProfilePanelFactory.class, beanFactory);
    }
}
