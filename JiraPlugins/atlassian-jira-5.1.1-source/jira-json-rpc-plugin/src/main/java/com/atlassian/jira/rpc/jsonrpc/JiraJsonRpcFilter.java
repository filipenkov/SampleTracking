package com.atlassian.jira.rpc.jsonrpc;

import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.rpc.jsonrpc.JsonRpcFilter;
import com.atlassian.rpc.jsonrpc.SoapModuleMethodMapper;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.voorhees.JsonRpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry-point for the JIRAlementation of the JSON-RPC service. The service exposes all existing
 * SOAP endpoints as JSON-RPC at /rpc/json-rpc/soapEndpointName, but does not (yet) support JSON-only
 * RPC services
 *
 * Implemented as a servlet filter so that it can be placed in the URL space next to the other RPC implementations.
 */
public class JiraJsonRpcFilter extends JsonRpcFilter<SoapModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(JiraJsonRpcFilter.class);

    public JiraJsonRpcFilter(PluginEventManager pluginEventManager, PluginAccessor pluginAccessor, I18nResolver i18nResolver)
    {
        super(pluginEventManager, pluginAccessor, i18nResolver);
    }

    @Override
    protected void register(SoapModuleDescriptor soapModule)
    {
        register(soapModule.getCompleteKey(), soapModule.getServicePath(), new JsonRpcHandler(new SoapModuleMethodMapper(soapModule.getModule(), soapModule.getPublishedInterface(), false), i18nAdapter));
    }

    @Override
    protected Class<SoapModuleDescriptor> getModuleDescriptorClass()
    {
        return SoapModuleDescriptor.class;
    }
}
