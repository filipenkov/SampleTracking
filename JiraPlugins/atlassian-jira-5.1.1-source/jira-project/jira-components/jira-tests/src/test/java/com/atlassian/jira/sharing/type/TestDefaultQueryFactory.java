package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.sharing.index.DefaultQueryFactory;
import com.atlassian.jira.sharing.index.PermissionQueryFactory;
import com.atlassian.jira.sharing.index.QueryFactory;
import com.atlassian.jira.sharing.index.SharedEntitySearchContextToQueryFactoryMap;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.user.MockUser;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A test case for DefaultQueryFactory
 *
 * @since v3.13
 */
public class TestDefaultQueryFactory extends ListeningTestCase
{
    private static final ShareTypeSearchParameter SHARE_TYPE_PARAMETER = new ShareTypeSearchParameter()
    {
        public ShareType.Name getType()
        {
            return new ShareType.Name("ShareTypeName");
        }
    };

    private User admin;

    @Before
    public void setUp() throws Exception
    {
        admin = new MockUser("admin");
    }

    @Test
    public void testCreate_WithUser()
    {
        final SharedEntitySearchParameters expectedSearchParameters =
                new SharedEntitySearchParametersBuilder().
                        setShareTypeParameter(SHARE_TYPE_PARAMETER).
                        toSearchParameters();

        final PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(null, null)
        {
            @Override
            public Query create(final SharedEntitySearchParameters searchParameters, final User user)
            {
                assertSame(expectedSearchParameters, searchParameters);
                assertSame(admin, user);
                return new TermQuery(new Term("PermissionQueryFactory", "create"));
            }
        };

        final Query shareTypeQuery = new TermQuery(new Term("ShareQueryFactory", "create"));
        final ShareQueryFactory shareQueryFactory = mock(ShareQueryFactory.class);
        when(shareQueryFactory.getQuery(SHARE_TYPE_PARAMETER, admin)).thenReturn(shareTypeQuery);

        final ShareType shareType = mock(ShareType.class);
        when(shareType.getQueryFactory()).thenReturn(shareQueryFactory);

        final ShareTypeFactory shareTypeFactory = mock(ShareTypeFactory.class);
        when(shareTypeFactory.getShareType(SHARE_TYPE_PARAMETER.getType())).thenReturn(shareType);

        final DefaultQueryFactory queryFactory = new DefaultQueryFactory(shareTypeFactory,
                createSearchContextToQueryFactoryMap(permissionQueryFactory));

        final Query query = queryFactory.create(expectedSearchParameters, admin);
        assertNotNull(query);
        assertEquals("+ShareQueryFactory:create +PermissionQueryFactory:create", query.toString());
    }

    @Test
    public void testCreate_WithNullShareTypeSearchParameter()
    {
        final SharedEntitySearchParameters expectedSearchParameters =
                new SharedEntitySearchParametersBuilder().setShareTypeParameter(null).toSearchParameters();

        final PermissionQueryFactory permissionQueryFactory = new PermissionQueryFactory(null, null)
        {
            @Override
            public Query create(final SharedEntitySearchParameters searchParameters, final User user)
            {
                assertSame(expectedSearchParameters, searchParameters);
                assertSame(null, user);
                return new TermQuery(new Term("PermissionQueryFactory", "create"));
            }
        };

        final DefaultQueryFactory queryFactory =
                new DefaultQueryFactory(null, createSearchContextToQueryFactoryMap(permissionQueryFactory));

        final Query query = queryFactory.create(expectedSearchParameters, null);
        assertNotNull(query);
        assertEquals("+PermissionQueryFactory:create", query.toString());
    }

    private SharedEntitySearchContextToQueryFactoryMap createSearchContextToQueryFactoryMap(final QueryFactory queryFactory)
    {
        final SharedEntitySearchContextToQueryFactoryMap searchContextToQueryFactoryMap =
                mock(SharedEntitySearchContextToQueryFactoryMap.class);

        when(searchContextToQueryFactoryMap.get(any(SharedEntitySearchContext.class))).thenReturn(queryFactory);
        return searchContextToQueryFactoryMap;
    }

    @Test
    public void testCreate_NoPermissionsNeeded()
    {
        final SharedEntitySearchParameters expectedSearchParameters =
                new SharedEntitySearchParametersBuilder().
                        setShareTypeParameter(SHARE_TYPE_PARAMETER).
                        toSearchParameters();

        final Query shareTypeQuery = new TermQuery(new Term("ShareQueryFactory", "create"));
        final ShareQueryFactory shareQueryFactory = mock(ShareQueryFactory.class);
        when(shareQueryFactory.getQuery(SHARE_TYPE_PARAMETER)).thenReturn(shareTypeQuery);

        final ShareType shareType = mock(ShareType.class);
        when(shareType.getQueryFactory()).thenReturn(shareQueryFactory);

        final ShareTypeFactory shareTypeFactory = mock(ShareTypeFactory.class);
        when(shareTypeFactory.getShareType(SHARE_TYPE_PARAMETER.getType())).thenReturn(shareType);

        final DefaultQueryFactory queryFactory = new DefaultQueryFactory(shareTypeFactory, null);

        final Query query = queryFactory.create(expectedSearchParameters);
        assertNotNull(query);
        assertEquals("+ShareQueryFactory:create", query.toString());
    }

    @Test
    public void testCreate_NoPermissionsNeeded_WithNullShareTypeSearchParameter()
    {
        final SharedEntitySearchParameters expectedSearchParameters =
                new SharedEntitySearchParametersBuilder().setShareTypeParameter(null).toSearchParameters();

        final DefaultQueryFactory queryFactory = new DefaultQueryFactory(null, null);

        final Query query = queryFactory.create(expectedSearchParameters);
        assertNotNull(query);
        assertEquals("*:*", query.toString());
    }
}
