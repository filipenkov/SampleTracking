package com.atlassian.crowd.model.group;

import java.util.Set;

/**
 * Details of the direct members of a single group.
 */
public interface Membership
{
    String getGroupName();
    Set<String> getUserNames();
    Set<String> getChildGroupNames();
    
    /**
     * Something went wrong while iterating over a collection of {@link Membership}s.
     */
    public class MembershipIterationException extends RuntimeException
    {
        public MembershipIterationException(Throwable cause)
        {
            super(cause);
        }
    }
}
