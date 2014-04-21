package com.atlassian.rpc.jsonrpc;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.voorhees.I18nAdapter;
import com.atlassian.voorhees.JsonRpcHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 */
public abstract class JsonRpcFilter<T extends ModuleDescriptor> implements Filter
{
    protected final PluginEventManager pluginEventManager;
    protected final PluginAccessor pluginAccessor;
    protected final I18nAdapter i18nAdapter;
    private ConcurrentMap<String, MappedHandler> handlers = new ConcurrentHashMap<String, MappedHandler>();

    public JsonRpcFilter(PluginEventManager pluginEventManager, PluginAccessor pluginAccessor, I18nResolver i18nResolver)
    {
        this.pluginEventManager = pluginEventManager;
        this.pluginAccessor = pluginAccessor;
        this.i18nAdapter = new SALI18nAdapter(i18nResolver);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        pluginEventManager.register(this);

        List<T> soapModules = pluginAccessor.getEnabledModuleDescriptorsByClass(getModuleDescriptorClass());

        for (T soapModule : soapModules)
        {
            register(soapModule);
        }
    }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) rq;
        HttpServletResponse response = (HttpServletResponse) rs;

        if ("POST".equals(request.getMethod()))
        {
            String uri = request.getRequestURI();
            int contextIndex = uri.indexOf("/rpc/json-rpc/");
            if (contextIndex + 14 < uri.length())
            {
                String pathPart = uri.substring(contextIndex + 14);

                for (MappedHandler mappedHandler : handlers.values())
                {

                    if (pathPart.equals(mappedHandler.servicePath))
                    {
                        mappedHandler.handler.process(request, response);
                        return;
                    }
                    else if (pathPart.startsWith(mappedHandler.servicePath + "/") && pathPart.length() > mappedHandler.servicePath.length() + 2)
                    {
                        String methodName = pathPart.substring(mappedHandler.servicePath.length() + 1);
                        mappedHandler.handler.process(methodName, request, response);
                        return;
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }


    @PluginEventListener
    public void onPluginModuleEnabled(PluginModuleEnabledEvent event)
    {
        if (getModuleDescriptorClass().isAssignableFrom(event.getModule().getClass()))
            register((T) event.getModule());
    }

    @PluginEventListener
    public void onPluginModuleDisabled(PluginModuleDisabledEvent event)
    {
        unregister(event.getModule());
    }

    @PluginEventListener
    public void onPluginDisabled(PluginDisabledEvent event)
    {
        for (ModuleDescriptor<?> moduleDescriptor : event.getPlugin().getModuleDescriptors())
        {
            unregister(moduleDescriptor);
        }
    }

    @Override
    public void destroy()
    {
        pluginEventManager.unregister(this);
    }

    protected abstract Class<T> getModuleDescriptorClass();

    protected abstract void register(T soapModule);

    protected void register(String pluginModuleKey, String servicePath, JsonRpcHandler handler)
    {
        handlers.put(pluginModuleKey, new MappedHandler(handler, servicePath));
    }

    protected void unregister(ModuleDescriptor moduleDescriptor)
    {
        handlers.remove(moduleDescriptor.getCompleteKey());
    }

    private static class MappedHandler
    {
        public final JsonRpcHandler handler;
        public final String servicePath;

        private MappedHandler(JsonRpcHandler handler, String servicePath)
        {
            this.handler = handler;
            this.servicePath = servicePath;
        }
    }
}
