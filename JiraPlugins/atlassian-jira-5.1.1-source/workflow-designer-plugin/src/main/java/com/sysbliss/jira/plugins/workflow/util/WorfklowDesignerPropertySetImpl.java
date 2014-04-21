package com.sysbliss.jira.plugins.workflow.util;

import com.atlassian.jira.propertyset.DefaultJiraPropertySetFactory;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.opensymphony.module.propertyset.PropertySet;
import com.sysbliss.jira.plugins.workflow.BuildInfo;

public class WorfklowDesignerPropertySetImpl implements WorkflowDesignerPropertySet
{
    private PropertySet jwdProps;
    JiraPropertySetFactory propertyFactory;

    public WorfklowDesignerPropertySetImpl()
    {
        propertyFactory = new DefaultJiraPropertySetFactory();
        jwdProps = propertyFactory.buildCachingDefaultPropertySet(BuildInfo.PLUGIN_KEY, true);
    }

    public void setProperty(String key, String value)
    {
        jwdProps.setText(key, value);
    }

    public String getProperty(String key)
    {
        return jwdProps.getText(key);
    }

    public boolean hasProperty(String key)
    {
        return jwdProps.exists(key);
    }

    public void removeProperty(String key)
    {
        jwdProps.remove(key);
    }
}
