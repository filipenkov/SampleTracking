package com.atlassian.upm.license.internal;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.checkNotNull;

public class JiraHostLicenseEventReader implements HostLicenseEventReader, InitializingBean, DisposableBean
{
    private Class<?> jiraNewLicenseEventClass = null;

    @Override
    public void destroy() throws Exception
    {
        jiraNewLicenseEventClass = null;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        try
        {
            jiraNewLicenseEventClass = Class.forName("com.atlassian.jira.license.NewLicenseEvent", false, this.getClass().getClassLoader());
        }
        catch (Exception e)
        {
            jiraNewLicenseEventClass = null;
        }
    }

    @Override
    public boolean isHostLicenseUpdated(Object event)
    {
        checkNotNull(event, "event");
        // JIRA 4.3 and 4.4's API did not include NewLicenseEvent
        return jiraNewLicenseEventClass != null && jiraNewLicenseEventClass.isAssignableFrom(event.getClass());
    }
}
