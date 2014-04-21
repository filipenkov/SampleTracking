package com.atlassian.crowd.embedded.ofbiz.db;

import org.ofbiz.core.entity.GenericEntityException;

/**
 * A RuntimeException that wraps an OfBiz GenericEntityException and indicates that an error occurred while trying to
 * access the database.
 */
public class DataAccessException extends RuntimeException
{
    public DataAccessException(GenericEntityException ex)
    {
        super(ex);
    }

    public DataAccessException(String message, GenericEntityException ex)
    {
        super(message, ex);
    }
}
