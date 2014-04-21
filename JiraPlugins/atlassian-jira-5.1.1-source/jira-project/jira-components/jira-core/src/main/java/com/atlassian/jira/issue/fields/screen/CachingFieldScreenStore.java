package com.atlassian.jira.issue.fields.screen;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.issue.field.screen.AbstractFieldScreenLayoutItemEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004 All rights reserved.
 */
@EventComponent
public class CachingFieldScreenStore implements FieldScreenStore
{
    private static final Logger log = LoggerFactory.getLogger(CachingFieldScreenStore.class);
    private final FieldScreenStore decoratedStore;
    private final ResettableLazyReference<FieldScreenCache> cache = new ResettableLazyReference<FieldScreenCache>()
    {
        @Override
        protected FieldScreenCache create() throws Exception
        {
            return new FieldScreenCache();
        }
    };

    public CachingFieldScreenStore(FieldScreenStore decoratedStore)
    {
        this.decoratedStore = decoratedStore;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public void setFieldScreenManager(FieldScreenManager fieldScreenManager)
    {
        decoratedStore.setFieldScreenManager(fieldScreenManager);
    }

    public FieldScreen getFieldScreen(Long id)
    {
        return cache.get().getFieldScreen(id);
    }

    public List<FieldScreen> getFieldScreens()
    {
        return cache.get().getFieldScreens();
    }

    public void createFieldScreen(FieldScreen fieldScreen)
    {
        decoratedStore.createFieldScreen(fieldScreen);
        refresh();
    }

    public void removeFieldScreen(Long id)
    {
        decoratedStore.removeFieldScreen(id);
        refresh();
    }

    public void updateFieldScreen(FieldScreen fieldScreen)
    {
        decoratedStore.updateFieldScreen(fieldScreen);
        refresh();
    }

    public void createFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        decoratedStore.createFieldScreenTab(fieldScreenTab);
    }

    public void updateFieldScreenTab(FieldScreenTab fieldScreenTab)
    {
        decoratedStore.updateFieldScreenTab(fieldScreenTab);
    }

    public List<FieldScreenTab> getFieldScreenTabs(FieldScreen fieldScreen)
    {
        return decoratedStore.getFieldScreenTabs(fieldScreen);
    }

    public void updateFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.updateFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.removeFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public void removeFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        decoratedStore.removeFieldScreenLayoutItems(fieldScreenTab);
    }

    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(FieldScreenTab fieldScreenTab)
    {
        return decoratedStore.getFieldScreenLayoutItems(fieldScreenTab);
    }

    public void refresh()
    {
        cache.reset();
        cache.get();

        decoratedStore.refresh();
        if (log.isTraceEnabled())
        {
            log.trace("Called refresh()", new Throwable());
        }
    }

    /**
     * Refreshes a single FieldScreen when there is a change to any of its constituent FieldScreenLayoutItem's.
     *
     * @param event a AbstractFieldScreenLayoutItemEvent
     */
    @EventListener
    public void onFieldScreenLayoutChange(AbstractFieldScreenLayoutItemEvent event)
    {
        cache.get().refreshSingle(event.getFieldScreenId());
    }

    public void createFieldScreenLayoutItem(FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        decoratedStore.createFieldScreenLayoutItem(fieldScreenLayoutItem);
    }

    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(GenericValue genericValue)
    {
        return decoratedStore.buildNewFieldScreenLayoutItem(genericValue);
    }

    public void removeFieldScreenTabs(FieldScreen fieldScreen)
    {
        decoratedStore.removeFieldScreenTabs(fieldScreen);
    }

    public void removeFieldScreenTab(Long id)
    {
        decoratedStore.removeFieldScreenTab(id);
    }

    public FieldScreenTab getFieldScreenTab(Long tabId)
    {
        // @TODO getting the tab by ID is not cached. Only used once at present
        return decoratedStore.getFieldScreenTab(tabId);
    }

    /**
     * The FieldScreenCache contains every single FieldScreen that exists in the database, so it must be kept up to date
     * whenever there are writes to the underlying database.
     * <p/>
     * This cache has copy-on-write semantics.
     */
    class FieldScreenCache
    {
        private volatile ImmutableFieldScreenCache data;

        FieldScreenCache()
        {
            // initialise the cache data from the database
            List<FieldScreen> dbFieldScreens = decoratedStore.getFieldScreens();
            data = new ImmutableFieldScreenCache(Maps.uniqueIndex(dbFieldScreens, new IndexById()));
        }

        /**
         * Creates a copy of this caches's data and refreshes the value for the screen with the id equal to
         * {@code fieldScreenId} with a new value read from the database.
         *
         * @param fieldScreenId a Long containing the id of the FieldScreen to refresh
         */
        synchronized void refreshSingle(Long fieldScreenId)
        {
            FieldScreen dbFieldScreen = decoratedStore.getFieldScreen(fieldScreenId);
            data = data.makeCopyContaining(dbFieldScreen);
        }

        FieldScreen getFieldScreen(Long id)
        {
            return data.getFieldScreen(id);
        }

        List<FieldScreen> getFieldScreens()
        {
            return data.getFieldScreens();
        }
    }

    /**
     * Immutable data holder used in {@code FieldScreenCache}. This class defensively copies FieldScreens that are
     * passed in or returned in order to guarantee immutability.
     * <p/>
     * Used in {@code FieldScreenCache} to implement copy-on-write semantics, but contains <em>mutable</em> FieldScreen
     * instances, which are returned to callers.
     */
    @Immutable
    class ImmutableFieldScreenCache
    {
        private final ImmutableMap<Long, FieldScreen> fieldScreenById;
        private final ImmutableList<FieldScreen> fieldScreens;

        /**
         * Creates a new ImmutableCacheData. This constructor makes defensive copies of all arguments that are passed
         * in.
         *
         * @param fieldScreenById a Map of FieldScreen by id
         */
        public ImmutableFieldScreenCache(Map<Long, FieldScreen> fieldScreenById)
        {
            this.fieldScreenById = ImmutableMap.copyOf(deepCopyOf(fieldScreenById));
            this.fieldScreens = Ordering.from(new ScreenNameComparator()).immutableSortedCopy(this.fieldScreenById.values());
        }

        /**
         * Returns a single FieldScreen by id.
         *
         * @param id a Long containing a FieldScreen id
         * @return a FieldScreen, or null
         */
        public FieldScreen getFieldScreen(Long id)
        {
            FieldScreen fieldScreen = fieldScreenById.get(id);

            return fieldScreen != null ? deepCopyOf(fieldScreen) : null;
        }

        /**
         * Returns a list of all FieldScreen instances, sorted by name.
         *
         * @return a List of FieldScreen
         */
        public List<FieldScreen> getFieldScreens()
        {
            return deepCopyOf(fieldScreens);
        }

        /**
         * Returns a copy of this ImmutableCacheData containing the given {@code fieldScreen}.
         *
         * @param fieldScreen a FieldScreen
         * @return an updated copy of this ImmutableCacheData
         */
        public ImmutableFieldScreenCache makeCopyContaining(FieldScreen fieldScreen)
        {
            ImmutableMap.Builder<Long, FieldScreen> copyOfFieldScreenById = ImmutableMap.builder();
            for (Map.Entry<Long, FieldScreen> screen : fieldScreenById.entrySet())
            {
                if (!screen.getKey().equals(fieldScreen.getId()))
                {
                    copyOfFieldScreenById.put(screen.getKey(), screen.getValue());
                }
            }

            // put the updated value and return a copy
            copyOfFieldScreenById.put(fieldScreen.getId(), fieldScreen);
            return new ImmutableFieldScreenCache(copyOfFieldScreenById.build());
        }

        /**
         * Creates a deep copy of the given map, copying the map itself and the FieldScreen's contained therein.
         *
         * @param fieldScreenById a Map of FieldScreen by id
         * @return a deep copy of {@code fieldScreenById}
         */
        private Map<Long, FieldScreen> deepCopyOf(Map<Long, FieldScreen> fieldScreenById)
        {
            Map<Long, FieldScreen> copyOfMap = Maps.newHashMapWithExpectedSize(fieldScreenById.size());
            for (Map.Entry<Long, FieldScreen> fieldScreen : fieldScreenById.entrySet())
            {
                copyOfMap.put(fieldScreen.getKey(), deepCopyOf(fieldScreen.getValue()));
            }

            return copyOfMap;
        }

        /**
         * Creates a deep copy of the given collection, copying the collection itself and the FieldScreen's contained
         * therein.
         *
         * @param fieldScreens a Collection of FieldScreen
         * @return a deep copy of {@code fieldScreens}
         */
        private List<FieldScreen> deepCopyOf(Collection<FieldScreen> fieldScreens)
        {
            List<FieldScreen> copyOfList = Lists.newArrayListWithCapacity((fieldScreens.size()));
            for (FieldScreen fieldScreen : fieldScreens)
            {
                copyOfList.add(deepCopyOf(fieldScreen));
            }

            return copyOfList;
        }

        /**
         * Creates a deep copy of a given FieldScreen instance.
         *
         * @param fieldScreen a FieldScreen
         * @return a deep copy of {@code fieldScreen}
         */
        private FieldScreen deepCopyOf(FieldScreen fieldScreen)
        {
//            if (fieldScreen instanceof FieldScreenImpl)
//            {
//                return ((FieldScreenImpl) fieldScreen).deepCopy();
//            }

            // NOTE: we are returning a mutable object from a cache here, which is rubbish. however, this cache has
            // worked like this since JIRA 3.10 so it is not causing any immediate problems. the last time I tried to
            // improve on this I ended up returning an incomplete-copied FieldScreen, which then caused performance
            // problems as everyone who got a FieldScreen from the cache ended up having to read the FieldScreenTabs
            // from the database anyway (JRA-28906).
            return fieldScreen;
        }
    }

    private static class IndexById implements Function<FieldScreen, Long>
    {
        @Override
        public Long apply(@Nullable FieldScreen fieldScreen)
        {
            return fieldScreen != null ? fieldScreen.getId() : null;
        }
    }

    private static class ScreenNameComparator implements Comparator<FieldScreen>
    {
        @Override
        public int compare(FieldScreen fs1, FieldScreen fs2)
        {
            return fs1.getName().compareTo(fs2.getName());
        }
    }
}
