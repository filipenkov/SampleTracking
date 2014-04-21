package com.atlassian.jira.issue.comments;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.user.MockUser;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import mock.MockComment;

public class TestDefaultCommentPermissionManager extends ListeningTestCase
{
    @Test
    public void testIsUserCommentAuthor() throws Exception
    {
        Comment anonComment = new MockComment(null, "Blah");
        Comment fredComment = new MockComment("fred", "Blah");
        User fred = new MockUser("fred");
        User dude = new MockUser("dude");
        // Class under test
        DefaultCommentPermissionManager defaultCommentPermissionManager = new DefaultCommentPermissionManager(null, null, null);
        // User is anonymous therefore is NEVER the author
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor(null, fredComment));
        // Comment is anonymous therefore no-one is the author
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor(fred, anonComment));
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor(null, anonComment));
        // Different User
        assertFalse(defaultCommentPermissionManager.isUserCommentAuthor(dude, fredComment));
        // Same User
        assertTrue(defaultCommentPermissionManager.isUserCommentAuthor(fred, fredComment));
    }

    @Test
    public void testHasEditAllPermission()
    {
        User user = new MockUser("Bob");
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
                P.args(new IsEqual(new Integer(Permissions.COMMENT_EDIT_ALL)), new IsAnything(), new IsEqual(user)), Boolean.TRUE);
        PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, permissionManager, null);
        assertTrue(manager.hasEditAllPermission(user, null));

        mockPermissionManager.verify();
    }

    @Test
    public void testHasEditAllPermissionFalse()
    {
        User user = new MockUser("Bob");
        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
                P.args(new IsEqual(new Integer(Permissions.COMMENT_EDIT_ALL)), new IsAnything(), new IsEqual(user)), Boolean.FALSE);
        PermissionManager permissionManager = (PermissionManager) mockPermissionManager.proxy();

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, permissionManager, null);
        assertFalse(manager.hasEditAllPermission(user, null));

        mockPermissionManager.verify();
    }

    @Test
    public void testHasBrowsePermissionGloballyVisible()
    {
        User user = new MockUser("Bob");
        Comment comment = new MockComment("dude", "comment body");

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, null, null);

        assertTrue(manager.hasBrowsePermission(user, comment));
    }

    @Test
    public void testHasBrowsePermissionGroup()
    {
        User user = new MockUser("Bob");
        Comment comment = new MockComment("dude", "comment body", "dudes", null);
        MockGroupManager mockGroupManager = new MockGroupManager();
        mockGroupManager.addGroup("dudes");

        DefaultCommentPermissionManager manager = new DefaultCommentPermissionManager(null, null, mockGroupManager);

        // user not in the group, so bad luck
        assertFalse(manager.hasBrowsePermission(user, comment));
        // Now add him to group
        mockGroupManager.addMember("dudes", "Bob");
        assertTrue(manager.hasBrowsePermission(user, comment));
    }

    //TODO: Write test for Project Role Permission.

}
