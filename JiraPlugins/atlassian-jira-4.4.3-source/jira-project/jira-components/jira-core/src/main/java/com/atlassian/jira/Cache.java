/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira;

/**
 * @deprecated Not used anywhere in JIRA. Since v4.4.
 */
public interface Cache
{
    long getSize();

    long getCapacity();

    void setCapacity(long maxSize);

    long getHitsCount();

    long getMissCount();

    void resetCacheStats();
}
