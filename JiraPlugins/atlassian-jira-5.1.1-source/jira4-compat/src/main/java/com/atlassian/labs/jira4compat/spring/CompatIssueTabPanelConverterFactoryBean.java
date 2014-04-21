package com.atlassian.labs.jira4compat.spring;

import com.atlassian.labs.jira4compat.spi.CompatIssueTabPanelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class CompatIssueTabPanelConverterFactoryBean extends AbstractCompatFactoryBean
{
    @Autowired
    public CompatIssueTabPanelConverterFactoryBean(AutowireCapableBeanFactory beanFactory)
    {
        super(CompatIssueTabPanelFactory.class, beanFactory);
    }

}
