package com.atlassian.config;

import com.atlassian.config.db.HibernateConfig;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.spring.filter.FlushingSpringSessionInViewFilter;

public class FlushingSpringSessionInViewFilterForHibernate extends FlushingSpringSessionInViewFilter
{
    protected boolean isDatabaseSetUp()
    {
        HibernateConfig hibernateConfig = ((HibernateConfig) ContainerManager.getComponent("hibernateConfig"));
        return hibernateConfig.isHibernateSetup();
    }
}
