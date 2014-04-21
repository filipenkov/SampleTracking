package com.atlassian.crowd.manager.permission;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.google.common.collect.Sets;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

/**
 * PermissionManagerImpl Tester.
 */
public class TestPermissionManagerImpl extends MockObjectTestCase
{
    private PermissionManager permissionManager;
    private DirectoryImpl directory = null;
    private ApplicationImpl application = null;

    public void setUp() throws Exception
    {
        super.setUp();


        permissionManager = new PermissionManagerImpl((ApplicationDAO) new Mock(ApplicationDAO.class).proxy(), (DirectoryDao) new Mock(DirectoryDao.class).proxy());

        directory = new DirectoryImpl();

        application = ApplicationImpl.newInstanceWithPassword("Test Application", ApplicationType.GENERIC_APPLICATION, "secret");

        directory.setAllowedOperations(Sets.newHashSet(OperationType.CREATE_USER, OperationType.UPDATE_USER));
    }

    public void tearDown() throws Exception
    {
        permissionManager = null;

        super.tearDown();
    }

    public void testDirectoryHasPermission()
    {
        boolean allowed = permissionManager.hasPermission(directory, OperationType.CREATE_USER);

        assertTrue(allowed);
    }

    public void testDirectoryDoesNotHavePermission()
    {
        boolean allowed = permissionManager.hasPermission(directory, OperationType.DELETE_USER);

        assertFalse(allowed);
    }

    public void testHasPermissionIllegalArguments()
    {
        try
        {
            permissionManager.hasPermission(null, OperationType.DELETE_USER);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here.
        }

        try
        {
            permissionManager.hasPermission(directory, null);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here.
        }
    }

    public void testAddPermissionToApplicationDirectoryMapping() throws Exception
    {
        try
        {
            permissionManager.addPermission(null, directory, OperationType.CREATE_GROUP);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }

        try
        {
            permissionManager.addPermission(application, null, OperationType.CREATE_GROUP);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }
        try
        {
            permissionManager.addPermission(application, directory, null);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }
    }

    public void testHasPermissionForApplicationDirectoryMapping()
    {
        try
        {
            permissionManager.hasPermission(null, directory, OperationType.CREATE_GROUP);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }

        try
        {
            permissionManager.hasPermission(application, null, OperationType.CREATE_GROUP);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }

        try
        {
            permissionManager.hasPermission(application, directory, null);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }
    }

    public void testRemovePermission() throws Exception
    {
        try
        {
            permissionManager.removePermission(null, directory, OperationType.CREATE_GROUP);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }

        try
        {
            permissionManager.removePermission(application, null, OperationType.CREATE_GROUP);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }

        try
        {
            permissionManager.removePermission(application, directory, null);

            fail("An illegal argument exception should of been thrown");
        }
        catch (IllegalArgumentException e)
        {
            // we should be here
        }
    }
}
