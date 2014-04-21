package com.atlassian.crowd.model;

/**
 * Represents a directory entity.
 */
public interface DirectoryEntity
{
    /**
     * @return id of the directory in which the DirectoryEntity is stored.
     */
    long getDirectoryId();

    /**
     * @return name of the entity.
     */
    String getName();

    /**
     * Implementations must ensure equality based on
     * getDirectoryId() and case-insensitive getName().
     *
     * @param o object to compare to.
     * @return <code>true</code> if and only if the directoryId
     * and the lowercase names of the directory entities match.
     */
    boolean equals(Object o);

    /**
     * Implementations must produce a hashcode based on
     * getDirectoryId() and case-insensitive getName().
     *
     * @return hashcode.
     */
    int hashCode();
}
