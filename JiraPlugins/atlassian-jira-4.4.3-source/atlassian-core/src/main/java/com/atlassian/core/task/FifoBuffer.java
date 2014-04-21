package com.atlassian.core.task;

import java.util.Collection;

public interface FifoBuffer
{
    /**
     * Get the oldest object from the buffer
     * @return the oldest Object, or null if the queue is empty
     */
    Object remove();

    /**
     * Add an Object to the buffer
     * @param o the Object to add
     */
    void add(Object o);

    /**
     * The number of buffer in the queue
     */
    int size();

    /**
     * The buffer in the queue
     */
    Collection getItems();

    /**
     * Clear all the objects from the buffer
     */
    void clear();
}