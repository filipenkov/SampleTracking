package com.atlassian.jira.action.admin;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.JiraNonWebActionSupport;
import org.ofbiz.core.entity.GenericValue;

public class ListenerCreate extends JiraNonWebActionSupport
{
    String clazz;
    String name;
    GenericValue listenerConfig;

    public String execute() throws Exception
    {
        listenerConfig = EntityUtils.createValue("ListenerConfig", EasyMap.build("name", getName(), "clazz", getClazz()));
        ManagerFactory.getListenerManager().refresh();

        return getResult();
    }

    public String getClazz()
    {
        return clazz;
    }

    public void setClazz(String clazz)
    {
        this.clazz = clazz;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public GenericValue getListenerConfig()
    {
        return listenerConfig;
    }
}
