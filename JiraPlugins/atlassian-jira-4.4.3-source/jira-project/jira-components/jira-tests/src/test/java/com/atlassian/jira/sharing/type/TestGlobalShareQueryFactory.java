package com.atlassian.jira.sharing.type;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.sharing.search.GlobalShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

/**
 * A test for GlobalShareQueryFactory
 *
 * @since v3.13
 */
public class TestGlobalShareQueryFactory extends ListeningTestCase
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
        GlobalShareQueryFactory queryFactory = new GlobalShareQueryFactory();
        Term[] terms = queryFactory.getTerms(null);
        assertNotNull(terms);
        assertEquals(1, terms.length);
        assertEquals("shareTypeGlobal", terms[0].field());
        assertEquals("true", terms[0].text());

        terms = queryFactory.getTerms(user);
        assertEquals(1, terms.length);
        assertEquals("shareTypeGlobal", terms[0].field());
        assertEquals("true", terms[0].text());
    }

    @Test
    public void testGetQuery()
    {
        ShareTypeSearchParameter searchParameter = GlobalShareTypeSearchParameter.GLOBAL_PARAMETER;

        GlobalShareQueryFactory queryFactory = new GlobalShareQueryFactory();
        Query query = queryFactory.getQuery(searchParameter);
        assertNotNull(query);
        assertEquals("shareTypeGlobal:true", query.toString());

        // no sematic difference in these this call with a user
        query = queryFactory.getQuery(searchParameter, user);
        assertNotNull(query);
        assertEquals("shareTypeGlobal:true", query.toString());
    }

    @Test
    public void testGetField()
    {
        GlobalShareQueryFactory queryFactory = new GlobalShareQueryFactory();
        // none of the parameters matter for global
        Field field = queryFactory.getField(null, null);
        assertNotNull(field);
        assertEquals("shareTypeGlobal", field.name());
        assertTrue(field.isStored());
        assertTrue(field.isIndexed());
        assertEquals("true",field.stringValue());
    }
}
