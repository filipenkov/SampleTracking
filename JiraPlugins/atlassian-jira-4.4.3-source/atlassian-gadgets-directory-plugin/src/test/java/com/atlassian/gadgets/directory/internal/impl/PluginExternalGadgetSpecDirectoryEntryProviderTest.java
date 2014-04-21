package com.atlassian.gadgets.directory.internal.impl;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.plugin.Plugin;

import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.atlassian.gadgets.test.ExampleGadgetSpecs.BANANA_GADGET_SPEC_URI;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.MONKEY_GADGET_SPEC_URI;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.monkeyGadgetSpec;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginExternalGadgetSpecDirectoryEntryProviderTest
{
    private static final String MODULE_KEY = "monkey-gadget";
    @Mock GadgetSpecFactory gadgetSpecFactory;
    @Mock ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory;

    PluginGadgetSpec monkeyGadgetSpec;
    PluginExternalGadgetSpecDirectoryEntryProvider pluginExternalGadgetSpecs;

    @Before
    public void setUp() throws GadgetParsingException
    {
        monkeyGadgetSpec = new PluginGadgetSpec(mock(Plugin.class), MODULE_KEY, MONKEY_GADGET_SPEC_URI.toASCIIString(), ImmutableMap.<String, String>of());

        when(threadLocalDelegateExecutorFactory.createExecutorService(Matchers.<ExecutorService>anyObject())).thenReturn(Executors.newSingleThreadExecutor());
        pluginExternalGadgetSpecs = new PluginExternalGadgetSpecDirectoryEntryProvider(gadgetSpecFactory, threadLocalDelegateExecutorFactory);

        GadgetSpec spec = monkeyGadgetSpec();
        when(gadgetSpecFactory.getGadgetSpec(eq(MONKEY_GADGET_SPEC_URI),
                isA(GadgetRequestContext.class)))
            .thenReturn(spec);

        pluginExternalGadgetSpecs.pluginGadgetSpecEnabled(monkeyGadgetSpec);
    }

    @Test
    public void assertThatContainsReturnsFalseWhenUriIsNotStoredInStore()
    {
        assertFalse(pluginExternalGadgetSpecs.contains(BANANA_GADGET_SPEC_URI));
    }

    @Test
    public void assertThatContainsReturnsTrueWhenUriIsStoredInStore()
    {
        assertTrue(pluginExternalGadgetSpecs.contains(MONKEY_GADGET_SPEC_URI));
    }

    @Test
    public void assertThatEntriesContainsUriStoredInStore()
    {
        assertThat(getOnlyElement(entries(pluginExternalGadgetSpecs)).getGadgetSpecUri(),
                   is(equalTo(MONKEY_GADGET_SPEC_URI)));
    }

    @Test
    public void assertThatEntriesDoesNotContainDuplicateUris() throws Exception
    {
        pluginExternalGadgetSpecs.pluginGadgetSpecEnabled(monkeyGadgetSpec);
        pluginExternalGadgetSpecs.pluginGadgetSpecEnabled(monkeyGadgetSpec);

        assertThat(size(entries(pluginExternalGadgetSpecs)), is(equalTo(1)));
    }

    @Test
    public void assertThatContainsReturnsFalseAfterUriIsRemovedFromStore()
    {
        pluginExternalGadgetSpecs.pluginGadgetSpecDisabled(monkeyGadgetSpec);

        assertFalse(pluginExternalGadgetSpecs.contains(MONKEY_GADGET_SPEC_URI));
    }


    @Test
    public void assertThatEntriesDoesNotContainUriRemovedFromStore()
    {
        pluginExternalGadgetSpecs.pluginGadgetSpecDisabled(monkeyGadgetSpec);

        assertTrue(isEmpty(entries(pluginExternalGadgetSpecs)));
    }

    @Test
    public void assertThatEntriesDoesNotContainNonExternalGadgetSpec()
    {
        int sizeBefore = size(entries(pluginExternalGadgetSpecs));

        pluginExternalGadgetSpecs.pluginGadgetSpecEnabled(
            new PluginGadgetSpec(mock(Plugin.class), MODULE_KEY, "path/to/gadget.xml", ImmutableMap.<String, String>of()));

        assertThat(size(entries(pluginExternalGadgetSpecs)), is(equalTo(sizeBefore)));
    }

    @Test(expected = NullPointerException.class)
    public void assertThatConstructingWithNullGadgetSpecFactoryThrowsNullPointerException()
    {
        new PluginExternalGadgetSpecDirectoryEntryProvider(null, null);
    }

    private static Iterable<Directory.Entry> entries(PluginExternalGadgetSpecDirectoryEntryProvider provider)
    {
        return provider.entries(GadgetRequestContext.NO_CURRENT_REQUEST);
    }
}
