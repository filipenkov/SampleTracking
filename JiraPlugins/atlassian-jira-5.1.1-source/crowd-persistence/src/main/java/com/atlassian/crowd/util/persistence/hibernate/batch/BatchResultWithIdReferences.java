package com.atlassian.crowd.util.persistence.hibernate.batch;

import com.atlassian.crowd.model.InternalDirectoryEntity;
import com.atlassian.crowd.util.BatchResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Batch result that stores ID references of all the directory entities that were
 * successfully processed by the batch.
 */
public class BatchResultWithIdReferences<T> extends BatchResult<T>
{
    private final Map<Pair<Long, String>, Long> entityToId = new HashMap<Pair<Long, String>, Long>();

    public BatchResultWithIdReferences(final int totalEntities)
    {
        super(totalEntities);
    }

    public void addIdReference(InternalDirectoryEntity entity)
    {
        entityToId.put(new Pair<Long, String>(entity.getDirectoryId(), entity.getName()), entity.getId());
    }

    public Long getIdReference(Long directoryId, String name)
    {
        return entityToId.get(new Pair<Long, String>(directoryId, name));
    }
}

/**
 * Holder object.
 *
 * @param <K> type of first element.
 * @param <V> type of second element.
 */
class Pair<K, V>
{
    private final K first;
    private final V second;

    public Pair(final K first, final V second)
    {
        this.first = first;
        this.second = second;
    }

    public K getFirst()
    {
        return first;
    }

    public V getSecond()
    {
        return second;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        if (second != null ? !second.equals(pair.second) : pair.second != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
