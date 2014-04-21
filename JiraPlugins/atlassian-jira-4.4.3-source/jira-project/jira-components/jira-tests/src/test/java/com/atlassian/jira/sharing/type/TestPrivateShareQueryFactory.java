package com.atlassian.jira.sharing.type;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.search.GlobalShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.opensymphony.user.User;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

/**
 * A test for PrivateShareQueryFactory
 *
 * @since v3.13
 */
public class TestPrivateShareQueryFactory extends ListeningTestCase
{
    User user;

    @Before
    public void setUp() throws Exception
    {
        user = new User("admin", new MockProviderAccessor("admin", EasyList.build("group1", "group2")), new MockCrowdService());
    }

    @Test
    public void testGetTerms()
    {
        PrivateShareQueryFactory queryFactory = new PrivateShareQueryFactory();
        Term[] terms = queryFactory.getTerms(null);
        assertNotNull(terms);
        assertEquals(0, terms.length);

        terms = queryFactory.getTerms(user);
        assertEquals(1, terms.length);
        assertEquals("owner", terms[0].field());
        assertEquals("admin", terms[0].text());
    }

    @Test
    public void testGetQuery()
    {
        ShareTypeSearchParameter searchParameter = GlobalShareTypeSearchParameter.GLOBAL_PARAMETER;

        PrivateShareQueryFactory queryFactory = new PrivateShareQueryFactory();
        Query query = null;
        try
        {
            query = queryFactory.getQuery(searchParameter);
            fail("We need a user in order to search");
        }
        catch (UnsupportedOperationException ignored)
        {
        }

        query = queryFactory.getQuery(searchParameter, user);
        assertNotNull(query);
        assertEquals("owner:admin", query.toString());

        query = queryFactory.getQuery(searchParameter, null);
        assertNull(query);
    }

    @Test
    public void testGetField()
    {
        PrivateShareQueryFactory queryFactory = new PrivateShareQueryFactory();
        final SharedEntity.Identifier entity = new SharedEntity.Identifier(new Long(123), PortalPage.ENTITY_TYPE, user)
        {
            public SharePermissions getPermissions()
            {
                return SharePermissions.PRIVATE;
            }
        };
        Field field = queryFactory.getField(entity, null);
        assertNotNull(field);
        assertEquals("owner", field.name());
        assertTrue(field.isStored());
        assertTrue(field.isIndexed());
        assertEquals("admin",field.stringValue());
    }
}
