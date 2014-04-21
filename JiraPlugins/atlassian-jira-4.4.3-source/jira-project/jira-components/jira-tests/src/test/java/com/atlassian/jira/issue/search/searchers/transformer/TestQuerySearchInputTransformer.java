package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.issue.search.searchers.util.DefaultQuerySearcherInputHelper;
import com.atlassian.jira.issue.search.searchers.util.QuerySearcherInputHelper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.validator.FreeTextFieldValidator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestQuerySearchInputTransformer extends MockControllerTestCase
{
    private JiraAuthenticationContext authenticationContext = null;
    private final User theUser = null;
    private QueryParser queryParser;
    private SearchContext searchContext;

    @Before
    public void setUp() throws Exception
    {
        queryParser = mockController.getMock(QueryParser.class);
        authenticationContext = mockController.getMock(JiraAuthenticationContext.class);
        authenticationContext.getUser();
        mockController.setDefaultReturnValue(theUser);
        searchContext = mockController.getMock(SearchContext.class);
    }

    @After
    public void tearDown() throws Exception
    {
        authenticationContext = null;
    }

    @Test
    public void testValidateParmsBlankQuery() throws Exception
    {
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        mockController.replay();

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();

        fvh.put(QuerySearcher.QUERY_URL_PARAM, "");

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver
        );
        transformer.validateParams(null, null, fvh, i18n, errors);

        assertFalse(errors.hasAnyErrors());
        mockController.verify();
    }

    @Test
    public void testValidateParmsHappyPath() throws Exception
    {
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);

        queryParser.parse("test");
        mockController.setReturnValue(new BooleanQuery());

        mockController.replay();

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();

        fvh.put(QuerySearcher.QUERY_URL_PARAM, "test");

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver
        )
        {
            @Override
            QueryParser createQueryParser()
            {
                return queryParser;
            }
        };

        transformer.validateParams(null, null, fvh, i18n, errors);

        assertFalse(errors.hasAnyErrors());
        mockController.verify();
    }

    @Test
    public void testValidateParmsDoesntParse() throws Exception
    {
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);

        queryParser.parse("test");
        mockController.setThrowable(new ParseException());

        mockController.replay();

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();

        fvh.put(QuerySearcher.QUERY_URL_PARAM, "test");

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver
        )
        {
            @Override
            QueryParser createQueryParser()
            {
                return queryParser;
            }
        };

        transformer.validateParams(null, null, fvh, i18n, errors);

        assertTrue(errors.hasAnyErrors());
        assertEquals("navigator.error.parse", errors.getErrors().get("query"));
        mockController.verify();
    }

    @Test
    public void testValidateParmsInvalidChars() throws Exception
    {
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        mockController.replay();

        final SimpleErrorCollection errors = new SimpleErrorCollection();
        final MockI18nHelper i18n = new MockI18nHelper();
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver
        );

        for (final String invalidChar : FreeTextFieldValidator.INVALID_FIRST_CHAR_LIST)
        {
            fvh.put(QuerySearcher.QUERY_URL_PARAM, invalidChar + "test");
            transformer.validateParams(null, null, fvh, i18n, errors);
            assertTrue(errors.hasAnyErrors());
        }
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestNullQuery() throws Exception
    {
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final QuerySearcherInputHelper inputHelper = mockController.getMock(QuerySearcherInputHelper.class);
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties,
                operandResolver)
        {
            @Override
            QuerySearcherInputHelper createQuerySearcherInputHelper()
            {
                return inputHelper;
            }
        };

        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, fvh, null, searchContext);
        assertTrue(fvh.isEmpty());
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestNoWhereClause() throws Exception
    {
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final QuerySearcherInputHelper inputHelper = mockController.getMock(QuerySearcherInputHelper.class);
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties,
                operandResolver)
        {
            @Override
            QuerySearcherInputHelper createQuerySearcherInputHelper()
            {
                return inputHelper;
            }
        };

        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, fvh, new QueryImpl(), searchContext);
        assertTrue(fvh.isEmpty());
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestHappyPath() throws Exception
    {
        final String searchText = "search text";
        final Operand operand = new SingleValueOperand(searchText);
        final TerminalClause clause = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, operand);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final QuerySearcherInputHelper inputHelper = mockController.getMock(QuerySearcherInputHelper.class);
        inputHelper.convertClause(clause, null);
        mockController.setReturnValue(MapBuilder.newBuilder().add(QuerySearcher.QUERY_URL_PARAM, searchText).add(IssueFieldConstants.DESCRIPTION, "true").toMap());
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties,
                operandResolver)
        {
            @Override
            QuerySearcherInputHelper createQuerySearcherInputHelper()
            {
                return inputHelper;
            }
        };

        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, fvh, new QueryImpl(clause), searchContext);
        assertEquals("true", fvh.get(IssueFieldConstants.DESCRIPTION));
        assertEquals(searchText, fvh.get(QuerySearcher.QUERY_URL_PARAM));
        assertEquals(CollectionBuilder.newBuilder(IssueFieldConstants.DESCRIPTION).asList(), fvh.get(QuerySearcher.QUERY_FIELDS_ID));
        mockController.verify();
    }

    @Test
    public void testPopulateFromSearchRequestNullResults() throws Exception
    {
        final String searchText = "search text";
        final Operand operand = new SingleValueOperand(searchText);
        final TerminalClause clause = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, operand);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        final QuerySearcherInputHelper inputHelper = mockController.getMock(QuerySearcherInputHelper.class);
        inputHelper.convertClause(clause, null);
        mockController.setReturnValue(null);
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties,
                operandResolver)
        {
            @Override
            QuerySearcherInputHelper createQuerySearcherInputHelper()
            {
                return inputHelper;
            }
        };

        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        transformer.populateFromQuery(null, fvh, new QueryImpl(clause), searchContext);
        assertTrue(fvh.isEmpty());
        mockController.verify();
    }

    @Test
    public void testPopulateFromParams()
    {
        final String searchText = "search text";

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver
        );
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        final Map<String, String> map = MapBuilder.<String, String> newBuilder().add("query", searchText).add(
            SystemSearchConstants.forSummary().getUrlParameter(), "true").add(SystemSearchConstants.forDescription().getUrlParameter(),
            "true").add(SystemSearchConstants.forEnvironment().getUrlParameter(), "true").add(
            SystemSearchConstants.forComments().getUrlParameter(), "true").toMap();
        transformer.populateFromParams(null, fvh, new MockActionParams(map));

        // a few legit values
        assertEquals("true", fvh.get(SystemSearchConstants.forSummary().getUrlParameter()));
        assertEquals("true", fvh.get(SystemSearchConstants.forDescription().getUrlParameter()));
        assertEquals("true", fvh.get(SystemSearchConstants.forEnvironment().getUrlParameter()));
        assertEquals("true", fvh.get(SystemSearchConstants.forComments().getUrlParameter()));
        assertEquals(searchText, fvh.get(QuerySearcher.QUERY_URL_PARAM));

        final List<String> queryFields = (List<String>) fvh.get(QuerySearcher.QUERY_FIELDS_ID);

        assertTrue(queryFields.contains(SystemSearchConstants.forSummary().getUrlParameter()));
        assertTrue(queryFields.contains(SystemSearchConstants.forDescription().getUrlParameter()));
        assertTrue(queryFields.contains(SystemSearchConstants.forEnvironment().getUrlParameter()));
        assertTrue(queryFields.contains(SystemSearchConstants.forComments().getUrlParameter()));

        mockController.verify();
    }

    @Test
    public void testGetSearchQueryOneClause() throws Exception
    {
        final String searchText = "search text";
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        fvh.put(SystemSearchConstants.forComments().getUrlParameter(), "true");
        fvh.put(QuerySearcher.QUERY_URL_PARAM, searchText);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver);

        final Clause result = transformer.getSearchClause(null, fvh);
        final TerminalClause expected = new TerminalClauseImpl(IssueFieldConstants.COMMENT, Operator.LIKE, searchText);
        assertEquals(expected, result);
        mockController.verify();
    }

    @Test
    public void testGetSearchQueryMoreThanOneClause() throws Exception
    {
        final String searchText = "search text";
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();
        fvh.put(IssueFieldConstants.ENVIRONMENT, "true");
        fvh.put(IssueFieldConstants.SUMMARY, "true");
        fvh.put(IssueFieldConstants.DESCRIPTION, "true");
        fvh.put(QuerySearcher.QUERY_URL_PARAM, searchText);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver);

        final Clause result = transformer.getSearchClause(null, fvh);
        final TerminalClause description = new TerminalClauseImpl(IssueFieldConstants.DESCRIPTION, Operator.LIKE, searchText);
        final TerminalClause environment = new TerminalClauseImpl(IssueFieldConstants.ENVIRONMENT, Operator.LIKE, searchText);
        final TerminalClause summary = new TerminalClauseImpl(IssueFieldConstants.SUMMARY, Operator.LIKE, searchText);

        final OrClause expected = new OrClause(summary, description, environment);

        assertEquals(expected, result);
        mockController.verify();
    }

    @Test
    public void testGetSearchQueryNoClause() throws Exception
    {
        final FieldValuesHolder fvh = new FieldValuesHolderImpl();

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        final ApplicationProperties applicationProperties = mockController.getMock(ApplicationProperties.class);
        mockController.replay();

        final QuerySearchInputTransformer transformer = new QuerySearchInputTransformer(applicationProperties, operandResolver);

        assertNull(transformer.getSearchClause(null, fvh));

        mockController.verify();
    }

    @Test
    public void testValidForNavigatorNoWhereClause() throws Exception
    {
        final QuerySearchInputTransformer transformer = createTransformerForValidateForNavigatorTests(null);
        mockController.replay();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(), searchContext));
        mockController.verify();
    }

    @Test
    public void testValidForNavigatorFailedClauseTest() throws Exception
    {
        final String id = "fieldName";
        final TerminalClause clause = new TerminalClauseImpl(id, Operator.EQUALS, "value");

        final QuerySearchInputTransformer transformer = createTransformerForValidateForNavigatorTests(null);
        mockController.replay();

        assertFalse(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    @Test
    public void testValidForNavigatorHappyPath() throws Exception
    {
        final String id = "fieldName";
        final TerminalClause clause = new TerminalClauseImpl(id, Operator.EQUALS, "value");

        final QuerySearchInputTransformer transformer = createTransformerForValidateForNavigatorTests(Collections.<String, String> emptyMap());
        mockController.replay();

        assertTrue(transformer.doRelevantClausesFitFilterForm(null, new QueryImpl(clause), searchContext));
        mockController.verify();
    }

    private QuerySearchInputTransformer createTransformerForValidateForNavigatorTests(final Map<String, String> helperResult)
    {
        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        return new QuerySearchInputTransformer(mockController.getMock(ApplicationProperties.class), operandResolver)
        {
            @Override
            QuerySearcherInputHelper createQuerySearcherInputHelper()
            {
                return new DefaultQuerySearcherInputHelper("searcherId", operandResolver)
                {
                    @Override
                    public Map<String, String> convertClause(final Clause clause, final User user)
                    {
                        return helperResult;
                    }
                };
            }
        };
    }

    private static class MockActionParams implements ActionParams
    {
        private Map<String, String> params = new HashMap<String, String>();

        private MockActionParams(final Map<String, String> params)
        {
            this.params = params;
        }

        public String[] getAllValues()
        {
            throw new UnsupportedOperationException();
        }

        public String[] getValuesForNullKey()
        {
            throw new UnsupportedOperationException();
        }

        public String[] getValuesForKey(final String key)
        {
            throw new UnsupportedOperationException();
        }

        public String getFirstValueForNullKey()
        {
            throw new UnsupportedOperationException();
        }

        public String getFirstValueForKey(final String key)
        {
            return params.get(key);
        }

        public void put(final String id, final String[] values)
        {
            throw new UnsupportedOperationException();
        }

        public Set getAllKeys()
        {
            throw new UnsupportedOperationException();
        }

        public Map getKeysAndValues()
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(final String key)
        {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty()
        {
            throw new UnsupportedOperationException();
        }
    }
}
