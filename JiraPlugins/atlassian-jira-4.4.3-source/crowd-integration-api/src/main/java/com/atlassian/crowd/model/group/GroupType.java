package com.atlassian.crowd.model.group;

/**
 * Represents the type of a Group:
 * <ul>
 * <li>GROUP: group used to determine authorisation.</li>
 * <li>LEGACY_ROLE: group representing pre-Crowd 2.0 "Role".</li>
 * </ul>
 */
public enum GroupType
{
    GROUP,
    LEGACY_ROLE;
}
