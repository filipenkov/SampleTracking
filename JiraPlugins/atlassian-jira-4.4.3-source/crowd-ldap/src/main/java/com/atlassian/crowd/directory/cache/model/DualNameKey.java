package com.atlassian.crowd.directory.cache.model;

import java.io.Serializable;

/**
 * A cache key formed by two Strings.
 */
public class DualNameKey implements Serializable
{
    private final String name1;
    private final String name2;

    public DualNameKey(String name1, String name2)
    {
        this.name1 = name1;
        this.name2 = name2;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DualNameKey that = (DualNameKey) o;

        if (name1 != null ? !name1.equals(that.name1) : that.name1 != null) return false;
        if (name2 != null ? !name2.equals(that.name2) : that.name2 != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name1 != null ? name1.hashCode() : 0;
        result = 31 * result + (name2 != null ? name2.hashCode() : 0);
        return result;
    }
}
