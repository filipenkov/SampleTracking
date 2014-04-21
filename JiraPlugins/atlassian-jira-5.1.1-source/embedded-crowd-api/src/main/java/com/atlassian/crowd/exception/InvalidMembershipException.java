package com.atlassian.crowd.exception;

/**
 * Thrown when a user tries to create a Nested Group membership that is not valid.
 * Reasons could include:
 * <ul>
 * <li>The parent group and child group are the same.</li>
 * <li>The parent group and child group are of different types (one is a Group and the other is a Role).</li>
 * <li>The new membership would cause a circular reference in the given application.</li>
 * </ul>
 */
public class InvalidMembershipException extends CrowdException
{
    public InvalidMembershipException(final String message)
    {
        super(message);
    }

    public InvalidMembershipException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public InvalidMembershipException(final Throwable cause)
    {
        super(cause);
    }
}
