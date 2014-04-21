package com.atlassian.jira.projectconfig.tab;

import com.atlassian.util.concurrent.CopyOnWriteMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A tab manager that gets all the {@link ProjectConfigTab}s out of the {@link BeanFactory} associated with the plugin.
 *
 * @since v4.4
 */
public class DefaultProjectConfigTabManager implements ProjectConfigTabManager, BeanFactoryAware
{
    private final Map<String, ProjectConfigTab> tabs = CopyOnWriteMap.newHashMap();

    private volatile ListableBeanFactory factory;

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        if (!(beanFactory instanceof ListableBeanFactory))
        {
            throw new BeanInitializationException("Expecting a ListableBeanFactory.");
        }
        factory = (ListableBeanFactory) beanFactory;
    }

    public ProjectConfigTab getTabForId(String id)
    {
        if (tabs.isEmpty())
        {
            final Map<String, ProjectConfigTab> tabs = new HashMap<String, ProjectConfigTab>();
            for (ProjectConfigTab tab : getTabs(ProjectConfigTab.class))
            {
                tabs.put(tab.getId(), tab);
            }
            this.tabs.putAll(tabs);
        }
        return tabs.get(id);
    }

    @SuppressWarnings ( { "unchecked" })
    private <T> Collection<? extends T> getTabs(Class<T> k)
    {
        return factory.getBeansOfType(k).values();
    }
}
