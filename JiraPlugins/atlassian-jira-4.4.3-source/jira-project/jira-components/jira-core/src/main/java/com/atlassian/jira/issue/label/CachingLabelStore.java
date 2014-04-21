package com.atlassian.jira.issue.label;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.ManagedLock;
import com.atlassian.util.concurrent.ManagedLocks;
import com.atlassian.util.concurrent.Supplier;

import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.collect.LRUMap.synchronizedLRUMap;

/**
 * Caching implementation of the Label store, that caches labels in a weakhashmap based on the issue/field combination.
 *
 * @since v4.2
 */
public class CachingLabelStore implements LabelStore, Startable
{
    private final OfBizLabelStore delegateStore;
    private final EventPublisher eventPublisher;
    private final Function<CacheKey, ManagedLock.ReadWrite> lockFactory = ManagedLocks.weakReadWriteManagedLockFactory();
    private final Map<CacheKey, Set<Label>> cache = synchronizedLRUMap(1000);

    public CachingLabelStore(final OfBizLabelStore delegateStore, final EventPublisher eventPublisher)
    {
        this.delegateStore = delegateStore;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cache.clear();
    }

    public Set<Label> getLabels(final Long issueId, final Long customFieldId)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);
        return lockFactory.get(key).read().withLock(new Supplier<Set<Label>>()
        {
            public Set<Label> get()
            {
                Set<Label> labels = cache.get(key);
                if (labels == null)
                {
                    labels = delegateStore.getLabels(issueId, customFieldId);
                    cache.put(key, labels);
                }
                return labels;
            }
        });
    }

    public Set<Label> setLabels(final Long issueId, final Long customFieldId, final Set<String> labels)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);
        return lockFactory.get(key).write().withLock(new Supplier<Set<Label>>()
        {
            public Set<Label> get()
            {
                final Set<Label> newLabels = delegateStore.setLabels(issueId, customFieldId, labels);
                cache.put(key, newLabels);

                return newLabels;
            }
        });
    }

    public Label addLabel(final Long issueId, final Long customFieldId, final String label)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);
        return lockFactory.get(key).write().withLock(new Supplier<Label>()
        {
            public Label get()
            {
                final Label newLabel = delegateStore.addLabel(issueId, customFieldId, label);
                //clear the cache so that the next read will fix up the cache again.
                cache.remove(key);
                return newLabel;
            }
        });
    }

    public void removeLabel(final Long labelId, final Long issueId, final Long customFieldId)
    {
        final CacheKey key = new CacheKey(issueId, customFieldId);
        lockFactory.get(key).write().withLock(new Runnable()
        {
            public void run()
            {
                delegateStore.removeLabel(labelId, issueId, customFieldId);
                //clear the cache so that the next read will fix up the cache again.
                cache.remove(key);
            }
        });
    }

    public Set<Long> removeLabelsForCustomField(final Long customFieldId)
    {
        try
        {
            return delegateStore.removeLabelsForCustomField(customFieldId);
        }
        finally
        {
            //not properly synchronized, but this should only be called very rarely!  Clear the entire
            //cache to ensure no stale entries for custom fields are left.
            cache.clear();
        }
    }

    static final class CacheKey
    {
        private final Long issueId;
        private final Long fieldId;

        CacheKey(final Long issueId, final Long fieldId)
        {
            this.issueId = issueId;
            this.fieldId = fieldId;
        }

        public Long getFieldId()
        {
            return fieldId;
        }

        public Long getIssueId()
        {
            return issueId;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final CacheKey cacheKey = (CacheKey) o;

            if (fieldId != null ? !fieldId.equals(cacheKey.fieldId) : cacheKey.fieldId != null)
            {
                return false;
            }
            if (!issueId.equals(cacheKey.issueId))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = issueId.hashCode();
            result = 31 * result + (fieldId != null ? fieldId.hashCode() : 0);
            return result;
        }
    }

}
