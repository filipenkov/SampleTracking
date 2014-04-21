/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit;

import com.atlassian.jira.bulkedit.operation.BulkDeleteOperation;
import com.atlassian.jira.bulkedit.operation.BulkEditOperation;
import com.atlassian.jira.bulkedit.operation.BulkMigrateOperation;
import com.atlassian.jira.bulkedit.operation.BulkOperation;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.util.JiraUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;

public class DefaultBulkOperationManager implements BulkOperationManager
{
    private static final Logger log = Logger.getLogger(DefaultBulkOperationManager.class);

    private Map bulkOperations;

    public DefaultBulkOperationManager()
    {
        bulkOperations = new ListOrderedMap();
        bulkOperations.put(BulkEditOperation.NAME_KEY, JiraUtils.loadComponent(BulkEditOperation.class));
//        bulkOperations.put(BulkMoveOperation.NAME_KEY, JiraUtils.loadComponent(BulkMoveOperationImpl.class));
        BulkMigrateOperation bulkMigrateOperation = (BulkMigrateOperation) JiraUtils.loadComponent(BulkMigrateOperation.class);
        bulkOperations.put(bulkMigrateOperation.getNameKey(), bulkMigrateOperation);
        bulkOperations.put(BulkWorkflowTransitionOperation.NAME_KEY, JiraUtils.loadComponent(BulkWorkflowTransitionOperation.class));
        bulkOperations.put(BulkDeleteOperation.NAME_KEY, new BulkDeleteOperation());
    }

    public Collection getBulkOperations()
    {
        return bulkOperations.values();
    }

    public BulkOperation getOperation(String operationName)
    {
        return (BulkOperation) getBulkOperationsMap().get(operationName);
    }

    public boolean isValidOperation(String operationName)
    {
        return getBulkOperationsMap().containsKey(operationName);
    }

    public void addBulkOperation(String operationName, Class componentClass)
    {
        log.info("Adding Bulk Operation " + operationName + " with class " + componentClass);
        bulkOperations.put(operationName, JiraUtils.loadComponent(componentClass));
    }

    protected Map getBulkOperationsMap()
    {
        return bulkOperations;
    }
}
