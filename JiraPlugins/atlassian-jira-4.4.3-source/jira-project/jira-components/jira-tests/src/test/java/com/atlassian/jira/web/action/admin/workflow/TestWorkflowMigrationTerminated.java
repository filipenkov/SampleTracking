/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.admin.workflow;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Map;
import java.util.Collections;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.ErrorCollection;

public class TestWorkflowMigrationTerminated extends ListeningTestCase
{
    @Test
    public void testTerminatedState()
    {
        Map failedMap = EasyMap.build(new Long(1), "abc", new Long(200), "some thing");
        WorkflowMigrationResult migrationResult = new WorkflowMigrationTerminated(failedMap);
        assertEquals(WorkflowMigrationResult.TERMINATED, migrationResult.getResult());
        assertEquals(2, migrationResult.getNumberOfFailedIssues());
        assertFalse(migrationResult.getErrorCollection().hasAnyErrors());
        assertEquals(failedMap, migrationResult.getFailedIssues());

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        String errorMessage = "some error message";
        errorCollection.addErrorMessage(errorMessage);
        String errorName = "error name";
        String errorValue = "error value";
        errorCollection.addError(errorName, errorValue);
        migrationResult = new WorkflowMigrationTerminated(errorCollection);
        assertEquals(WorkflowMigrationResult.TERMINATED, migrationResult.getResult());
        assertEquals(0, migrationResult.getNumberOfFailedIssues());
        assertTrue(migrationResult.getErrorCollection().hasAnyErrors());
        assertEquals(errorMessage, migrationResult.getErrorCollection().getErrorMessages().iterator().next());
        assertEquals(errorValue, migrationResult.getErrorCollection().getErrors().get(errorName));


        try
        {
            new WorkflowMigrationTerminated((Map) null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain failures.", e.getMessage());
        }

        try
        {
            new WorkflowMigrationTerminated(Collections.EMPTY_MAP);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain failures.", e.getMessage());
        }

        try
        {
            new WorkflowMigrationTerminated((ErrorCollection) null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain errors.", e.getMessage());
        }

        try
        {
            new WorkflowMigrationTerminated(new SimpleErrorCollection());
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Must contain errors.", e.getMessage());
        }
    }
}
