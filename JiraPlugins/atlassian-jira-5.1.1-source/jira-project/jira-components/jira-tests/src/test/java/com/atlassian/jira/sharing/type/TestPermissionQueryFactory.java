package com.atlassian.jira.sharing.type;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.groups.MockGroupManager;
import com.atlassian.jira.sharing.index.PermissionQueryFactory;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

/**
 * A test case for PermissionQueryFactory
 *
 * @since v3.13
 */
public class TestPermissionQueryFactory extends ListeningTestCase
{
    private User admin;
    final MockGroupManager mockGroupManager = new MockGroupManager();

    @Before
    public void setUp() throws Exception
    {
        admin = new MockUser("admin");
        // Set up the mock GroupManager
        mockGroupManager.addMember("group1", "admin");
        mockGroupManager.addMember("group2", "admin");
    }

    @Test
    public void testCreate_WithoutUser()
    {
        final SharedEntitySearchParameters expectedSearchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();

        PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(new MockProjectShareQueryFactory(), null);
        try
        {
            final Query query = permissionQueryFactory.create(expectedSearchParameters);
            fail("UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
    }

    @Test
    public void testCreate()
    {
        final SharedEntitySearchParameters expectedSearchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();

        PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(new MockProjectShareQueryFactory(), mockGroupManager);
        final Query query = permissionQueryFactory.create(expectedSearchParameters, admin);
        assertNotNull(query);
        assertEquals("owner:admin shareTypeGlobal:true shareTypeGroup:group1 shareTypeGroup:group2 MockProjectShareQueryFactory:admin", query.toString());
    }

    @Test
    public void testCreate_withNullUser()
    {
        final SharedEntitySearchParameters expectedSearchParameters = new SharedEntitySearchParametersBuilder().toSearchParameters();

        PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(new MockProjectShareQueryFactory(), mockGroupManager);
        final Query query = permissionQueryFactory.create(expectedSearchParameters, null);
        assertNotNull(query);
        assertEquals("shareTypeGlobal:true MockProjectShareQueryFactory:null", query.toString());
    }

    private class MockProjectShareQueryFactory extends ProjectShareQueryFactory
    {
        private MockProjectShareQueryFactory()
        {
            super(null);
        }

        @Override
        public Term[] getTerms(final User user)
        {
            return new Term[] { new Term("MockProjectShareQueryFactory", user != null ? user.getName() : "null") };
        }
    }
}
