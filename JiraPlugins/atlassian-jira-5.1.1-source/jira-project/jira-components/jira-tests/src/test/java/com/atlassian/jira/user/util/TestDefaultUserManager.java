package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.jira.user.MockCrowdDirectoryService;
import com.atlassian.jira.user.MockUser;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * @since v4.4
 */
public class TestDefaultUserManager extends TestCase
{
    public void testCanUpdateUser_readOnlyDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), null, false);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null);
        assertFalse(defaultUserManager.canUpdateUser(mockUser));
    }

    public void testCanUpdateUser_writableDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), null, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null);
        assertTrue(defaultUserManager.canUpdateUser(mockUser));
    }

    public void testCanUpdateUserPassword_readOnlyDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), null, false);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null);
        assertFalse(defaultUserManager.canUpdateUserPassword(mockUser));
    }

    public void testCanUpdateUserPassword_writableNonDelegatedLdapDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.CONNECTOR, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null);
        assertTrue(defaultUserManager.canUpdateUserPassword(mockUser));
    }

    public void testCanUpdateUserPassword_writableDelegatedLdapDirectory() throws Exception
    {
        final User mockUser = createMockUser();
        final Directory mockDirectory = createMockDirectory(mockUser.getDirectoryId(), DirectoryType.DELEGATING, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(mockDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null);
        assertFalse(defaultUserManager.canUpdateUserPassword(mockUser));
    }

    public void testHasPasswordWritableDirectory_none() throws Exception
    {
        final Directory readOnlyDirectory = createMockDirectory(1, null, false);
        final Directory writableDelegatedDirectory = createMockDirectory(2, DirectoryType.DELEGATING, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(readOnlyDirectory);
        crowdDirectoryService.addDirectory(writableDelegatedDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null);
        assertFalse(defaultUserManager.hasPasswordWritableDirectory());
    }

    public void testHasPasswordWritableDirectory_some() throws Exception
    {
        final Directory readOnlyDirectory = createMockDirectory(1, null, false);
        final Directory writableDelegatedDirectory = createMockDirectory(2, DirectoryType.DELEGATING, true);
        final Directory writableDirectory = createMockDirectory(3, DirectoryType.CONNECTOR, true);

        final CrowdDirectoryService crowdDirectoryService = new MockCrowdDirectoryService();
        crowdDirectoryService.addDirectory(readOnlyDirectory);
        crowdDirectoryService.addDirectory(writableDelegatedDirectory);
        crowdDirectoryService.addDirectory(writableDirectory);

        final DefaultUserManager defaultUserManager = new DefaultUserManager(null, crowdDirectoryService, null);
        assertTrue(defaultUserManager.hasPasswordWritableDirectory());
    }

    private User createMockUser()
    {
        return new MockUser("user1");
    }

    private Directory createMockDirectory(final long id, final DirectoryType type, final boolean writable)
    {
        final ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder();
        builder.setId(id);

        if (writable)
        {
            final Set<OperationType> allowedOperations = new HashSet<OperationType>();
            allowedOperations.add(OperationType.CREATE_USER);
            allowedOperations.add(OperationType.UPDATE_USER);
            builder.setAllowedOperations(allowedOperations);
        }

        builder.setType(type);

        return builder.toDirectory();
    }
}
