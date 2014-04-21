/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bulkedit.operation.BulkOperation;

import java.util.Collection;

@PublicApi
public interface BulkOperationManager
{
    /**
     * Returns all available {@link BulkOperation} objects
     * @return Collection of {@link BulkOperation} objects
     */
    public Collection<BulkOperation> getBulkOperations();

    /**
     * Returns true if the operation name is of an existing registered {@link BulkOperation}
     * @param operationName the operation name
     * @return true if the operation name is of an existing {@link BulkOperation} else false
     */
    public boolean isValidOperation(String operationName);

    /**
     * Returns a {@link BulkOperation} object registered with corresponding name
     *
     * @param operationName the operation name
     * @return {@link BulkOperation} object. Null if doesn't exist
     */
    BulkOperation getOperation(String operationName);

    /**
     * Add a new operation using the given class
     * @param operationName - name to register the loaded class under
     * @param componentClass - class to load
     */
    void addBulkOperation(String operationName, Class<? extends BulkOperation> componentClass);
}
