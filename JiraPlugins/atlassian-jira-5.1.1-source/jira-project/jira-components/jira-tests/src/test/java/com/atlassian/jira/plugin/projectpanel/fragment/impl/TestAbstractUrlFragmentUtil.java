package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

/**
 * @since v4.0
 */
public class TestAbstractUrlFragmentUtil extends MockControllerTestCase
{

    private SearchRequest searchRequest;
    private ApplicationProperties applicationProperties;
    private static final String baseUrl = "base";

    @Before
    public void setUp() throws Exception
    {
        searchRequest = mockController.getMock(SearchRequest.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
    }

    @Test
    public void testCreateAllUrlSearhRequestContainsOrderClause() throws Exception
    {
        final OrderBy orgOrder = new OrderByImpl(new SearchSort("org", SortOrder.ASC));
        final OrderBy newOrder = new OrderByImpl(new SearchSort("new", SortOrder.DESC));
        final TerminalClause orgClause = new TerminalClauseImpl("org", Operator.EQUALS, "org");
        final QueryImpl query = new QueryImpl(orgClause, orgOrder, null);

        final OrderBy expectedOrder = new OrderByImpl(new SearchSort("new", SortOrder.DESC), new SearchSort("org", SortOrder.ASC));

        searchRequest.getQuery();
        mockController.setDefaultReturnValue(query);

        mockController.replay();
        final MyUrlFragmentUtil urlUtil = new MyUrlFragmentUtil(null, newOrder, orgClause, expectedOrder, searchRequest, null, applicationProperties);
        urlUtil.getAllUrl();
        urlUtil.getAllUrl();
        mockController.verify();
    }

    @Test
    public void testCreateAllUrlSearhRequestContainsNoOrderClause() throws Exception
    {
        final OrderBy newOrder = new OrderByImpl(new SearchSort("new", SortOrder.DESC));
        final TerminalClause orgClause = new TerminalClauseImpl("org", Operator.EQUALS, "org");
        final QueryImpl query = new QueryImpl(orgClause);

        searchRequest.getQuery();
        mockController.setDefaultReturnValue(query);

        mockController.replay();
        final MyUrlFragmentUtil urlUtil = new MyUrlFragmentUtil(null, newOrder, orgClause, newOrder, searchRequest, null, applicationProperties);
        urlUtil.getAllUrl();
        urlUtil.getAllUrl();
        mockController.verify();
    }

    @Test
    public void testGetUrlSearhRequestContainsWhereClause() throws Exception
    {
        final OrderBy orgOrder = new OrderByImpl(new SearchSort("org", SortOrder.ASC));
        final TerminalClause orgClause = new TerminalClauseImpl("org", Operator.EQUALS, "org");
        final TerminalClause newClause = new TerminalClauseImpl("new", Operator.EQUALS, "new");
        final QueryImpl query = new QueryImpl(orgClause, orgOrder, null);

        final AndClause expectedClause = new AndClause(orgClause, newClause);

        searchRequest.getQuery();
        mockController.setDefaultReturnValue(query);

        mockController.replay();
        final MyUrlFragmentUtil urlUtil = new MyUrlFragmentUtil(newClause, null, expectedClause, orgOrder, searchRequest, null, applicationProperties);
        urlUtil.getUrl(null);
        mockController.verify();
    }

    @Test
    public void testGetUrlSearhRequestContainsNoWhereClause() throws Exception
    {
        final OrderBy orgOrder = new OrderByImpl(new SearchSort("org", SortOrder.ASC));
        final TerminalClause newClause = new TerminalClauseImpl("new", Operator.EQUALS, "new");
        final QueryImpl query = new QueryImpl(null, orgOrder, null);

        searchRequest.getQuery();
        mockController.setDefaultReturnValue(query);

        mockController.replay();
        final MyUrlFragmentUtil urlUtil = new MyUrlFragmentUtil(newClause, null, newClause, orgOrder, searchRequest, null, applicationProperties);
        urlUtil.getUrl(null);
        mockController.verify();
    }
    
    static private class MyUrlFragmentUtil extends AbstractUrlFragmentUtil<Void>
    {
        private final Clause domainClause;
        private final OrderBy orderBy;
        private final Clause expectedClause;
        private final OrderBy expectedOrderBy;

        public MyUrlFragmentUtil(final Clause domainClause, final OrderBy orderBy, final Clause expectedClause, final OrderBy expectedOrderBy, final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
        {
            super(searchRequest, user, applicationProperties);
            this.domainClause = domainClause;
            this.orderBy = orderBy;
            this.expectedClause = expectedClause;
            this.expectedOrderBy = expectedOrderBy;
        }

        @Override
        String getLink(final Clause whereClause, final OrderBy orderBy)
        {
            assertEquals(expectedClause, whereClause);
            assertEquals(expectedOrderBy, orderBy);
            return "";
        }

        protected Clause getDomainClause(final Void domain)
        {
            return domainClause;
        }

        protected OrderBy getOrderBy()
        {
            return orderBy;
        }

        @Override
        String createBaseUrl(final ApplicationProperties applicationProperties)
        {
            return baseUrl;
        }
    }
}
