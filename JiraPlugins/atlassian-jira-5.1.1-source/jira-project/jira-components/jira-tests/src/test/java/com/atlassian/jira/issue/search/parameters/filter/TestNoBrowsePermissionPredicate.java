package com.atlassian.jira.issue.search.parameters.filter;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * @since v4.0
 */
public class TestNoBrowsePermissionPredicate extends ListeningTestCase
{
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
    }

    @Test
    public void testEvaluate() throws Exception
    {
        final PermissionManager permissionManager = createMock(PermissionManager.class);
        final GenericValue issue = new MockGenericValue("Issue");

        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, theUser)).andReturn(false);

        replay(permissionManager);

        NoBrowsePermissionPredicate predicate = new NoBrowsePermissionPredicate(theUser, permissionManager);
        assertTrue(predicate.evaluate(issue));

        verify(permissionManager);
    }
}
