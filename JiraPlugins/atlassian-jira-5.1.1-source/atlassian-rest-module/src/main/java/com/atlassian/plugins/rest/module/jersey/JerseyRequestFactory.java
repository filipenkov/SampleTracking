package com.atlassian.plugins.rest.module.jersey;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugins.rest.module.ChainingClassLoader;
import com.atlassian.plugins.rest.module.ContextClassLoaderSwitchingProxy;
import com.atlassian.sal.api.net.NonMarshallingRequestFactory;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import org.osgi.framework.Bundle;

import java.lang.reflect.Proxy;

public class JerseyRequestFactory implements RequestFactory
{
    private final RequestFactory<? extends Request> delegateRequestFactory;
    private final Plugin plugin;
    private final Bundle bundle;

    private volatile JerseyEntityHandler jerseyEntityHandler;

    public JerseyRequestFactory(final NonMarshallingRequestFactory<? extends Request> delegateRequestFactory,
                                final Plugin plugin,
                                final Bundle bundle)
    {
        this.plugin = plugin;
        this.delegateRequestFactory = delegateRequestFactory;
        this.bundle = bundle;
    }

    public Request createRequest(final Request.MethodType methodType, final String s)
    {
        ensureInitalised();
        final Request delegateRequest = delegateRequestFactory.createRequest(methodType, s);
        final JerseyRequest request = new JerseyRequest(delegateRequest, jerseyEntityHandler, plugin);
        return (Request) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Request.class},
                new ContextClassLoaderSwitchingProxy(request, getChainingClassLoader(plugin))
        );
    }

    public boolean supportsHeader()
    {
        ensureInitalised();
        return delegateRequestFactory.supportsHeader();
    }

    private void ensureInitalised()
    {
        if (jerseyEntityHandler == null)
        {
            jerseyEntityHandler = createHandler();
        }
    }

    private JerseyEntityHandler createHandler()
    {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        ChainingClassLoader chainingClassLoader = getChainingClassLoader(plugin);
        try
        {
            Thread.currentThread().setContextClassLoader(chainingClassLoader);
            return new JerseyEntityHandler((AutowireCapablePlugin) plugin, bundle);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    void destroy()
    {
        if (jerseyEntityHandler != null) {
            jerseyEntityHandler.destroy();
        }
    }

    private ChainingClassLoader getChainingClassLoader(final Plugin plugin)
    {
        return new ChainingClassLoader(getClass().getClassLoader(), plugin.getClassLoader());
    }
}
