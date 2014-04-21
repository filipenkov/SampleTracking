package com.atlassian.jira.issue.fields.screen;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * @since v4.0
 */
public class TestCachingFieldScreenStore extends ListeningTestCase
{
    private static final FieldScreen ANT = newFieldScreen(1, "Ant");
    private static final FieldScreen BEE = newFieldScreen(2, "Bee");
    private static final FieldScreen CATERPILLAR = newFieldScreen(3, "Caterpillar");
    private static final FieldScreen MAGGOT = newFieldScreen(4, "Maggot");

    @Test
    public void testGetFieldScreens() throws Exception
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);
        delegate.createFieldScreen(CATERPILLAR);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, null);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        // FieldScreens should be ordered alphabetically.
        List<FieldScreen> expected = CollectionBuilder.list(ANT, CATERPILLAR, MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
        // Now add a new Screen
        cachingFieldScreenStore.createFieldScreen(BEE);
        // this should slot into the alphabetical order
        expected = CollectionBuilder.list(ANT, BEE, CATERPILLAR, MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
    }

    @Test
    public void testGetFieldScreensImmutable()
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, null);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        // try to add a new screen
        Collection<FieldScreen> screenList = cachingFieldScreenStore.getFieldScreens();
        try
        {
            screenList.add(BEE);
            fail("Should be immutable");
        }
        catch (UnsupportedOperationException ex)
        {
            // Good!
        }
    }

    @Test
    public void testGetFieldScreensConcurrentModification()
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, null);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        // Get the List of screens.
        Collection<FieldScreen> screenList = cachingFieldScreenStore.getFieldScreens();
        // Now update the store
        cachingFieldScreenStore.createFieldScreen(BEE);
        // Try to iterate over the list - just want to make sure it doesn't throw a Concurrent Modification Exception
        for (FieldScreen fieldScreen : screenList)
        {
            fieldScreen.getId();
        }
    }

    @Test
    public void testRemove() throws Exception
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, null);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        List<FieldScreen> expected;
        expected = CollectionBuilder.list(ANT, MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
        // Remove ANT
        cachingFieldScreenStore.removeFieldScreen(ANT.getId());
        expected = CollectionBuilder.list(MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
    }

    @Test
    public void testUpdate() throws Exception
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, null);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        List<FieldScreen> expected;
        expected = CollectionBuilder.list(ANT, MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
        // Update the Maggot Screen
        FieldScreen maggie = newFieldScreen(4, "Maggie");

        cachingFieldScreenStore.updateFieldScreen(maggie);
        expected = CollectionBuilder.list(ANT, maggie);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
    }

    private static FieldScreen newFieldScreen(int id, final String name)
    {
        final GenericValue gvFieldScreen = new MockGenericValue("FieldScreen");
        gvFieldScreen.set("id", (long) id);
        gvFieldScreen.setString("name", name);
        return new FieldScreenImpl(null, gvFieldScreen);
    }
}
