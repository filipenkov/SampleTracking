package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.gadgets.test.PassThroughTransactionTemplate;
import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.BANANA_GADGET_SPEC_URI;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.MONKEY_GADGET_SPEC_URI;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.monkeyGadgetSpec;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.size;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurableExternalGadgetSpecDirectoryEntryProviderTest
{
    private static final String MONKEY_DIRECTORY_URI = "http://example.org/gadget/directory/monkey";
    private static final Directory.Entry MONKEY_DIRECTORY_ENTRY =
        new GadgetSpecDirectoryEntry(monkeyGadgetSpec(), true, URI.create(MONKEY_DIRECTORY_URI));

    @Mock GadgetSpecFactory gadgetSpecFactory;

    @Mock DirectoryUrlBuilder directoryUrlBuilder;
    @Mock ThreadLocalDelegateExecutorFactory threadLocalDelegateExecutorFactory;
    @Mock EventPublisher eventPublisher;
    TransactionTemplate txTemplate;
    private ConfigurableExternalGadgetSpecDirectoryEntryProvider configuredExternalGadgetSpecs;
    private GadgetRequestContext gadgetRequestContext;

    @Before
    public void setUp() throws GadgetParsingException
    {
        txTemplate = new PassThroughTransactionTemplate();
        when(threadLocalDelegateExecutorFactory.createExecutorService(Matchers.<ExecutorService>anyObject())).thenReturn(Executors.newSingleThreadExecutor());
        configuredExternalGadgetSpecs = new ConfigurableExternalGadgetSpecDirectoryEntryProvider(gadgetSpecFactory,
            new MapBackedExternalGadgetSpecStore(), directoryUrlBuilder, txTemplate, threadLocalDelegateExecutorFactory, eventPublisher);

        GadgetSpec spec = monkeyGadgetSpec();
        when(gadgetSpecFactory.getGadgetSpec(eq(MONKEY_GADGET_SPEC_URI), isA(GadgetRequestContext.class))).thenReturn(spec);
        when(directoryUrlBuilder.buildDirectoryGadgetResourceUrl(Matchers.<ExternalGadgetSpecId>anyObject()))
            .thenReturn(MONKEY_DIRECTORY_URI);

        configuredExternalGadgetSpecs.add(MONKEY_GADGET_SPEC_URI);
        gadgetRequestContext = gadgetRequestContext().build();
    }

    @Test
    public void assertThatContainsReturnsFalseWhenUriIsNotStoredInStore()
    {
        assertFalse(configuredExternalGadgetSpecs.contains(BANANA_GADGET_SPEC_URI));
    }

    @Test
    public void assertThatContainsReturnsTrueWhenUriIsStoredInStore()
    {
        assertTrue(configuredExternalGadgetSpecs.contains(MONKEY_GADGET_SPEC_URI));
    }

    @Test
    public void assertThatEntriesContainsUriStoredInStore()
    {
        assertThat(getOnlyElement(configuredExternalGadgetSpecs.entries(gadgetRequestContext)),
                   is(equalTo(MONKEY_DIRECTORY_ENTRY)));
    }

    @Test
    public void assertThatEntriesDoesNotContainDuplicateUris() throws Exception
    {
        configuredExternalGadgetSpecs.add(MONKEY_GADGET_SPEC_URI);
        configuredExternalGadgetSpecs.add(MONKEY_GADGET_SPEC_URI);

        assertThat(size(configuredExternalGadgetSpecs.entries(gadgetRequestContext)), is(equalTo(1)));
    }
}
