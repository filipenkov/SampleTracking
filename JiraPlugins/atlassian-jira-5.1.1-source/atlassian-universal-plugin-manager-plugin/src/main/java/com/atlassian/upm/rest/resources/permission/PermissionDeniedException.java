package com.atlassian.upm.rest.resources.permission;

import com.atlassian.upm.AccessDeniedException;

/**
 * Thrown if a user does not have permission to access a resource.
 * There's no way to recover when this happens, so throw a RuntimeException.
 * Is basically a Runtime version of {@link AccessDeniedException}.
 */
public class PermissionDeniedException extends RuntimeException
{
}
