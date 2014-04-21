package com.atlassian.gadgets.directory.internal.impl;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.directory.internal.DirectoryEntryProvider;

import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static com.atlassian.gadgets.dashboard.util.Iterables.elementsEqual;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.BANANA_GADGET_SPEC_URI;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.MONKEY_GADGET_SPEC_URI;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.TREE_GADGET_SPEC_URI;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.monkeyGadgetSpec;
import static com.atlassian.gadgets.test.ExampleGadgetSpecs.treeGadgetSpec;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryImplTest
{
    @Mock DirectoryEntryProvider monkeyProvider;
    @Mock DirectoryEntryProvider treeProvider;
    @Mock DirectoryEntryProvider emptyProvider;

    private final Directory.Entry monkeyDirectoryEntry = new GadgetSpecDirectoryEntry(monkeyGadgetSpec(), false, null);
    private final Directory.Entry treeDirectoryEntry = new GadgetSpecDirectoryEntry(treeGadgetSpec(), false, null);

    Directory directory;

    @Before
    public void setUp() throws Exception
    {
        when(monkeyProvider.contains(MONKEY_GADGET_SPEC_URI)).thenReturn(true);
        when(monkeyProvider.entries(isA(GadgetRequestContext.class))).thenReturn(singleton(monkeyDirectoryEntry));
        when(treeProvider.contains(TREE_GADGET_SPEC_URI)).thenReturn(true);
        when(treeProvider.entries(isA(GadgetRequestContext.class))).thenReturn(singleton(treeDirectoryEntry));
        when(emptyProvider.entries(isA(GadgetRequestContext.class))).thenReturn(ImmutableSet.<Directory.Entry>of());

        directory =
            new DirectoryImpl(asList(monkeyProvider, emptyProvider, treeProvider));
    }

    @Test
    public void assertThatContainsReturnsFalseWhenGadgetIsNotInAnyProvider() throws Exception
    {
        assertFalse(directory.contains(BANANA_GADGET_SPEC_URI));
    }

    @Test
    public void assertThatContainsReturnsTrueWhenGadgetIsInFirstProvider()
    {
        assertTrue(directory.contains(MONKEY_GADGET_SPEC_URI));
    }

    @Test
    public void assertThatContainsReturnsTrueWhenGadgetIsInLastProvider()
    {
        assertTrue(directory.contains(TREE_GADGET_SPEC_URI));
    }

    @Test
    public void assertThatEntriesIncludesGadgetSpecsFromProviders()
    {
        final Iterable<Directory.Entry> expectedEntries = asList(monkeyDirectoryEntry, treeDirectoryEntry);
        assertTrue(elementsEqual(directory.getEntries(
               gadgetRequestContext().build()), expectedEntries));
    }
}
