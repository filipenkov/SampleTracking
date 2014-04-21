package com.atlassian.jira.user;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

/**
 * Copyright 2007 Atlassian Software.
 * All rights reserved.
 */
public class TestCachingExternalEntityStore extends ListeningTestCase
{
    @Test
    public void testCreateIfDoesNotExist()
    {
        String name = "username";

        // Create a mock backing store for the cache that will only expect to be invoked one time, this
        // allows us to verify that the second time we call the method we are getting the value from the
        // cache and not the backing store.
        MockControl ctrlExternalEntityStore = MockClassControl.createControl(ExternalEntityStore.class);
        ExternalEntityStore mockExternalEntityStore = (ExternalEntityStore)ctrlExternalEntityStore.getMock();
        mockExternalEntityStore.createIfDoesNotExist(name);
        ctrlExternalEntityStore.setReturnValue(new Long(1), 1); // is meant to be called only once
        ctrlExternalEntityStore.replay();

        CachingExternalEntityStore entityStore = new CachingExternalEntityStore(mockExternalEntityStore, null);

        Long id = entityStore.createIfDoesNotExist(name);
        assertEquals(new Long(1), id);

        // Do it a second time to make sure we get the value from the cache
        id = entityStore.createIfDoesNotExist(name);
        assertEquals(new Long(1), id);

        ctrlExternalEntityStore.verify();
    }

    @Test
    public void testIllegalName()
    {
        CachingExternalEntityStore entityStore = new CachingExternalEntityStore(null, null);
        try
        {
            entityStore.createIfDoesNotExist(null);
            fail("Name cannot be null");
        }
        catch (IllegalArgumentException e)
        {
            // this is expected
        }
    }

}
