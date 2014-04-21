package com.atlassian.jira.bc.project.version;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import org.easymock.MockControl;

/**
 * Tests the {@link DeleteVersionValidator} construction. Note that the actual validation is already being tested in
 * the {@link TestDefaultVersionService}.
 */
public class TestDeleteVersionValidator extends ListeningTestCase
{
    @Test
    public void testConstruction()
    {
        final JiraServiceContext context = new MockJiraServiceContext();
        final MockControl mockVersionManagerControl = MockControl.createControl(VersionManager.class);
        final VersionManager mockVersionManager = (VersionManager) mockVersionManagerControl.getMock();
        final PermissionManager permissionManager = MyPermissionManager.createPermissionManager(false);
        mockVersionManagerControl.replay();

        try
        {
            new DeleteVersionValidator(null, mockVersionManager, permissionManager);
            fail("Cannot construct a DeleteVersionValidator with a null JiraServiceContext");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        try
        {
            new DeleteVersionValidator(context, null, permissionManager);
            fail("Cannot construct a DeleteVersionValidator with a null VersionManager");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        try
        {
            new DeleteVersionValidator(context, mockVersionManager, null);
            fail("Cannot construct a DeleteVersionValidator with a null PermissionManager");
        }
        catch (IllegalArgumentException expected)
        {
            // do nothing
        }

        new DeleteVersionValidator(context, mockVersionManager, permissionManager); // exception = fail
    }


}

