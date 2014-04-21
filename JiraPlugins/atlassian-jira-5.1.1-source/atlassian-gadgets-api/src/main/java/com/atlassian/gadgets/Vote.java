package com.atlassian.gadgets;

/**
 * {@code Vote}s are used in the gadget system to determine whether a user is allowed to perform certain actions, such
 * as seeing a gadget in the gadget browser or being allowed to render a gadget on their dashboard.
 */
public enum Vote
{
    /**
     * The user is allowed to perform the requested action.
     */
    ALLOW,

    /**
     * The user is forbidden to perform the requested action.
     */
    DENY,

    /**
     * The implementation has no opinion on whether the user may perform the requested action.
     */
    PASS
}