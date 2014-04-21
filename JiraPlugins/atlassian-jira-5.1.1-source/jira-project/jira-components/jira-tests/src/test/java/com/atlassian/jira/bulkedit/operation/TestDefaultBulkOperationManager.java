/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.jira.bulkedit.BulkOperationManager;
import com.atlassian.jira.bulkedit.DefaultBulkOperationManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.Collection;

public class TestDefaultBulkOperationManager extends LegacyJiraMockTestCase
{
    public TestDefaultBulkOperationManager(String s)
    {
        super(s);
    }

    public void testGetBulkOperations()
    {
        BulkOperationManager bulkOperationManager = new DefaultBulkOperationManager();
        Collection bulkOperations = bulkOperationManager.getBulkOperations();
        assertNotNull(bulkOperations);
        assertEquals(4, bulkOperations.size());
        bulkOperations.contains(new BulkDeleteOperation());
        bulkOperations.contains(new BulkEditOperation(null, null, null, null, null));
    }
}
