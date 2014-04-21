package com.atlassian.streams.spi;

import com.google.common.base.Function;

/**
 * An optional {@link Function} for evicting instances from the application's Hibernate session.
 */
public interface Evictor<T> extends Function<T, Void>
{
}
