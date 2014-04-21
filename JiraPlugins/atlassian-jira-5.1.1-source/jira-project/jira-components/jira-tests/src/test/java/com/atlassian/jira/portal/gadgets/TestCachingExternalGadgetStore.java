package com.atlassian.jira.portal.gadgets;

import com.atlassian.gadgets.directory.spi.ExternalGadgetSpec;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.net.URI;
import java.util.Set;

public class TestCachingExternalGadgetStore extends MockControllerTestCase
{
    @Test
    public void testCRUD()
    {
        final ExternalGadgetStore mockExternalGadgetStore = mockController.getMock(ExternalGadgetStore.class);

        ExternalGadgetSpec spec1 = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("1"), URI.create("http://www.igoogle.com/frogger.xml"));
        ExternalGadgetSpec spec2 = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("2"), URI.create("http://jira.atlassian.com/pie.xml"));
        ExternalGadgetSpec spec3 = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("3"), URI.create("http://confluence.atlassian.com/stuff.xml"));
        ExternalGadgetSpec addedSpec = new ExternalGadgetSpec(ExternalGadgetSpecId.valueOf("4"), URI.create("http://jira.atlassian.com/timesince.xml"));

        final Set<ExternalGadgetSpec> originalSet = CollectionBuilder.newBuilder(spec1, spec2, spec3).asSet();
        expect(mockExternalGadgetStore.getAllGadgetSpecUris()).andReturn(originalSet);
        expect(mockExternalGadgetStore.addGadgetSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"))).andReturn(addedSpec);
        mockExternalGadgetStore.removeGadgetSpecUri(ExternalGadgetSpecId.valueOf("4"));

        final CachingExternalGadgetStore externalGadgetStore = mockController.instantiate(CachingExternalGadgetStore.class);

        //none of these calls should hit the delegate store.
        boolean contains = externalGadgetStore.containsSpecUri(URI.create("http://www.igoogle.com/frogger.xml"));
        assertTrue(contains);
        contains = externalGadgetStore.containsSpecUri(URI.create("http://example.com/invalid.xml"));
        assertFalse(contains);
        contains = externalGadgetStore.containsSpecUri(URI.create("http://jira.atlassian.com/pie.xml"));
        assertTrue(contains);
        contains = externalGadgetStore.containsSpecUri(URI.create("http://confluence.atlassian.com/stuff.xml"));
        assertTrue(contains);
        contains = externalGadgetStore.containsSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"));
        assertFalse(contains);

        externalGadgetStore.addGadgetSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"));
        contains = externalGadgetStore.containsSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"));
        assertTrue(contains);

        externalGadgetStore.removeGadgetSpecUri(ExternalGadgetSpecId.valueOf("4"));
        contains = externalGadgetStore.containsSpecUri(URI.create("http://jira.atlassian.com/timesince.xml"));
        assertFalse(contains);

        final Set<ExternalGadgetSpec> gadgetSpecUris = externalGadgetStore.getAllGadgetSpecUris();
        assertEquals(originalSet, gadgetSpecUris);
    }
}
