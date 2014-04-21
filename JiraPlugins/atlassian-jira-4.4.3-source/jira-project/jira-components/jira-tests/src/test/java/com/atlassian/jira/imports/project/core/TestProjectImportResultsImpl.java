package com.atlassian.jira.imports.project.core;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v3.13
 */
public class TestProjectImportResultsImpl extends ListeningTestCase
{
    @Test
    public void testAbortExceptionThrownWhenErrorLimitHit() throws Exception
    {
        ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);

        for (int i = 0; i < projectImportResults.getErrorCountLimit() - 1; i++)
        {
            projectImportResults.addError("error " + i);
            assertFalse(projectImportResults.abortImport());
        }

        // Now add the error that pushes it over the edge
        projectImportResults.addError("last errror");
        assertTrue(projectImportResults.abortImport());
    }

    @Test
    public void testGetProjectRoles() throws Exception
    {
        ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);

        projectImportResults.incrementRoleGroupCreatedCount("Role 1");
        projectImportResults.incrementRoleUserCreatedCount("Role 2");

        assertEquals(2, projectImportResults.getRoles().size());
        assertTrue(projectImportResults.getRoles().contains("Role 1"));
        assertTrue(projectImportResults.getRoles().contains("Role 2"));
        assertEquals(1, projectImportResults.getGroupsCreatedCountForRole("Role 1"));
        assertEquals(1, projectImportResults.getUsersCreatedCountForRole("Role 2"));
    }
}
