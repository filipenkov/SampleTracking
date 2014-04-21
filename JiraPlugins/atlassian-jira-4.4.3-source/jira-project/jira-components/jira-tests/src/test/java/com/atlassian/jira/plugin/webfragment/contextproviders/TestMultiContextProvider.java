package com.atlassian.jira.plugin.webfragment.contextproviders;

import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestMultiContextProvider
{

    @Test
    public void testNoDefinedCTxProviders()
    {
        final MultiContextProvider contextProvider = getMultiContextProvider(null);
        final Map<String, String> initParams = MapBuilder.emptyMap();

        contextProvider.init(initParams);

        assertEquals(0, contextProvider.getCtxProviders().size());
    }

    @Test
    public void testOneContextWithNoParams()
    {
        final Map<String, String> initParams = MapBuilder.build("ctxProvider-1", "context1");
        final Map<String, Object> expectedContext = MapBuilder.<String, Object>build("k1", "v1");
        ContextProvider onlyCP = new ContextProvider()
        {

            @Override
            public void init(Map<String, String> params) throws PluginParseException
            {
                assertTrue(params.isEmpty());
            }

            @Override
            public Map<String, Object> getContextMap(Map<String, Object> context)
            {
                assertEquals(expectedContext, context);
                return context;
            }
        };

        List<StringCtxProviderTuple> contextProviders = CollectionBuilder.list(new StringCtxProviderTuple("context1", onlyCP));
        final MultiContextProvider contextProvider = getMultiContextProvider(contextProviders);

        contextProvider.init(initParams);
        assertEquals(expectedContext, contextProvider.getContextMap(expectedContext));

        assertEquals(1, contextProvider.getCtxProviders().size());

    }

    @Test
    public void testOneContextWithParams()
    {
        final Map<String, String> initParams = MapBuilder.build("ctxProvider-1", "context1", "ctxProvider-1:pk1", "pk1Value", "ctxProvider-1:pk2", "pk2Value");
        final Map<String, String> expectedInitParams = MapBuilder.build("pk1", "pk1Value", "pk2", "pk2Value");
        final Map<String, Object> expectedContext = MapBuilder.<String, Object>build("k1", "v1");
        ContextProvider onlyCP = new ContextProvider()
        {

            @Override
            public void init(Map<String, String> params) throws PluginParseException
            {
                assertEquals(expectedInitParams, params);

            }

            @Override
            public Map<String, Object> getContextMap(Map<String, Object> context)
            {
                assertEquals(expectedContext, context);

                return MapBuilder.<String, Object>build("pk1", "pk1Value", "pk2", "pk2Value");
            }
        };

        List<StringCtxProviderTuple> contextProviders = CollectionBuilder.list(new StringCtxProviderTuple("context1", onlyCP));
        final MultiContextProvider contextProvider = getMultiContextProvider(contextProviders);

        contextProvider.init(initParams);
        assertEquals(MapBuilder.<String, Object>build("k1", "v1", "pk1", "pk1Value", "pk2", "pk2Value"), contextProvider.getContextMap(expectedContext));

        assertEquals(1, contextProvider.getCtxProviders().size());

    }

    @Test
    public void testMultipleContextsWithParams()
    {
        final Map<String, String> initParams = MapBuilder.newBuilder("ctxProvider-1", "context1", "ctxProvider-2", "context2")
                .add("ctxProvider-1:pk1", "pk1Value").add("ctxProvider-1:pk2", "pk2Value")
                .add("ctxProvider-2:pk3", "pk3Value").add("ctxProvider-2:pk4", "pk4Value").add("ctxProvider-2:pk5", "pk5Value").toMap();

        final Map<String, String> expectedInitParamsForP1 = MapBuilder.build("pk1", "pk1Value", "pk2", "pk2Value");
        final Map<String, String> expectedInitParamsForP2 = MapBuilder.build("pk3", "pk3Value", "pk4", "pk4Value", "pk5", "pk5Value");

        final Map<String, Object> passedInCtx = MapBuilder.<String, Object>build("k1", "v1");
        final Map<String, Object> returnContext1 = MapBuilder.<String, Object>build("rc1", "rcv1");
        final Map<String, Object> returnContext2 = MapBuilder.<String, Object>build("rc2", "rcv2", "rc3", "rcv3");
        ContextProvider firstCP = new ContextProvider()
        {

            @Override
            public void init(Map<String, String> params) throws PluginParseException
            {
                assertEquals(expectedInitParamsForP1, params);

            }

            @Override
            public Map<String, Object> getContextMap(Map<String, Object> context)
            {
                assertEquals(passedInCtx, context);

                return returnContext1;
            }
        };
        ContextProvider secondCP = new ContextProvider()
        {

            @Override
            public void init(Map<String, String> params) throws PluginParseException
            {
                assertEquals(expectedInitParamsForP2, params);

            }

            @Override
            public Map<String, Object> getContextMap(Map<String, Object> context)
            {
                assertEquals(passedInCtx, context);

                return returnContext2;
            }
        };

        List<StringCtxProviderTuple> contextProviders = CollectionBuilder.list(new StringCtxProviderTuple("context1", firstCP), new StringCtxProviderTuple("context2", secondCP));
        final MultiContextProvider contextProvider = getMultiContextProvider(contextProviders);

        contextProvider.init(initParams);
        assertEquals(CompositeMap.of(passedInCtx, CompositeMap.of(returnContext1, returnContext2)), contextProvider.getContextMap(passedInCtx));

        assertEquals(2, contextProvider.getCtxProviders().size());

    }


    private MultiContextProvider getMultiContextProvider(final List<StringCtxProviderTuple> providers)
    {
        return new MultiContextProvider()
        {
            int getCount = 0;

            @Override
            protected ContextProvider getContextProvider(String classKey)
            {
                assertEquals(providers.get(getCount).getClassName(), classKey);
                final ContextProvider ctxProvider = providers.get(getCount).getCtxProvider();

                getCount++;
                return ctxProvider;
            }
        };
    }

    private static class StringCtxProviderTuple
    {
        private final String className;
        private final ContextProvider ctxProvider;

        public StringCtxProviderTuple(String className, ContextProvider ctxProvider)
        {
            this.className = className;
            this.ctxProvider = ctxProvider;
        }

        public String getClassName()
        {
            return className;
        }

        public ContextProvider getCtxProvider()
        {
            return ctxProvider;
        }
    }
}
