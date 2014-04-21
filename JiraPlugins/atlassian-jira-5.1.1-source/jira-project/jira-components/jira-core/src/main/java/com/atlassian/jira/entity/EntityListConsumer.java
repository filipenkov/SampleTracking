package com.atlassian.jira.entity;

/**
 * @since v5.2
 */
public interface EntityListConsumer<E, R>
{
    void consume(E entity);

    R result();
}
