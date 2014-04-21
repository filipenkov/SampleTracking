package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.plugin.webfragment.CacheableContextProviderDecorator;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a wrapper to enable you use multiple {@link ContextProvider}s to provide context to a {@link
 * com.atlassian.plugin.web.model.WebPanel} or {@link com.atlassian.plugin.web.model.WebLabel} or simalar web fragment.
 * Usage:
 *<context-provider class="com.atlassian.jira.plugin.webfragment.contextproviders.MultiContextProvider">
 *    <param name="ctxProvider-1">com.yourorg.jira.web.contextproviders.yourfirstcontextprovider</param>
 *    <param name="ctxProvider-1:firstKey">A value for the 1st ContextProvider</param>
 *    <param name="ctxProvider-2">com.yourorg.jira.web.contextproviders.yourfirstcontextprovider</param>
 *    <param name="ctxProvider-2:anotherKey">A value for the 2nd ContextProvider</param>
 *</context-provider>     
 *
 * Note. If you are using this in a plugin, you will need to extend this class (no need to override anything) in order
 * for it to be able to get the right classloader.
 *
 * @since v4.4
 */
public abstract class MultiContextProvider implements ContextProvider
{
    private final List<ContextProvider> ctxProviders = new ArrayList<ContextProvider>();

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {

        int classIndex = 1;
        while (params.containsKey("ctxProvider-" + classIndex))
        {
            final String classKey = "ctxProvider-" + classIndex;
            final ContextProvider contextProvider = getContextProvider(params.get(classKey));

            final MapBuilder<String, String> subParamBuilder = MapBuilder.newBuilder();

            final Set<Map.Entry<String, String>> allParams = params.entrySet();

            for (Map.Entry<String, String> paramEntry : allParams)
            {
                final String key = paramEntry.getKey();
                if (key.startsWith(classKey + ":"))
                {
                    subParamBuilder.add(key.substring(key.indexOf(":") + 1), paramEntry.getValue());
                }
            }
            contextProvider.init(subParamBuilder.toMap());
            ctxProviders.add(contextProvider);
            classIndex++;

        }
    }

    protected ContextProvider getContextProvider(String clazz)
    {
        try
        {

            final ClassLoader classLoader = this.getClass().getClassLoader();

            final Class<ContextProvider> aClass = (Class<ContextProvider>) classLoader.loadClass(clazz);
            final ContextProvider contextProvider = JiraUtils.loadComponent(aClass);
            if(contextProvider instanceof CacheableContextProvider)
            {
                return new CacheableContextProviderDecorator((CacheableContextProvider) contextProvider);
            }
            else
            {
                return contextProvider;
            }

        }
        catch (ClassNotFoundException e)
        {
            throw new PluginParseException("Unable to load the Context Provider: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        Map<String, Object> returnContext = MapBuilder.newBuilder(context).toMutableMap();

        for (ContextProvider provider : ctxProviders)
        {
            returnContext = CompositeMap.of(returnContext, provider.getContextMap(context));
        }
        return returnContext;
    }

    public List<ContextProvider> getCtxProviders()
    {
        return ctxProviders;
    }
}
