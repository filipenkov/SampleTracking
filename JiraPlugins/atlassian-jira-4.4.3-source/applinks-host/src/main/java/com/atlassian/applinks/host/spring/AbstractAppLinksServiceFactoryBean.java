package com.atlassian.applinks.host.spring;

import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import org.springframework.beans.factory.FactoryBean;

/**
 * A service factory bean for all applink services.  Sets the default service call timeout as 10 seconds.
 *
 * @since 3.0
 */
abstract class AbstractAppLinksServiceFactoryBean implements FactoryBean
{
    private final OsgiServiceProxyFactory applinksApiProxyFactory;
    private final Class apiClass;
    private long timeoutInMillis;

    public AbstractAppLinksServiceFactoryBean(final OsgiContainerManager osgiContainerManager, final Class apiClass)
    {
        this.applinksApiProxyFactory = new OsgiServiceProxyFactory(osgiContainerManager);
        this.apiClass = apiClass;
        this.timeoutInMillis = 10000;
    }

    @SuppressWarnings("unchecked")
    public Object getObject() throws Exception
    {
        return applinksApiProxyFactory.createProxy(apiClass, timeoutInMillis);
    }

    public Class getObjectType()
    {
        return apiClass;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public long getTimeoutInMillis()
    {
        return timeoutInMillis;
    }

    public void setTimeoutInMillis(final long timeoutInMillis)
    {
        this.timeoutInMillis = timeoutInMillis;
    }
}
