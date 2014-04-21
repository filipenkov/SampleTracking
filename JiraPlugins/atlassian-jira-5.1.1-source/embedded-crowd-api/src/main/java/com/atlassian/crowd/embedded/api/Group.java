package com.atlassian.crowd.embedded.api;

/**
 * Represents a group.
 */
public interface Group extends Comparable<Group>
{
    /**
     * @return name of the group.
     */
    String getName();

    /**
     * Implementations must ensure equality based on case-insensitive getName().
     *
     * @param o object to compare to.
     * @return <code>true</code> if and only if the names in lowercase
     *         of the directory entities match.
     */
    boolean equals(Object o);

    /**
     * Implementations must produce a hash-code based on case-insensitive getName().
     *
     * @return hash-code.
     */
    int hashCode();

    /**
     * CompareTo must be compatible with the equals() and hashCode() methods
     * @param   o the object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     */
    int compareTo(Group o);
}