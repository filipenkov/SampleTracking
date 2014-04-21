package com.atlassian.jira.jql.query;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.SimpleAllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.easymock.classextension.EasyMock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestAllTextClauseQueryFactory extends MockControllerTestCase
{
    private CustomFieldManager customFieldManager;
    private SearchHandlerManager searchHandlerManager;
    private AllTextClauseQueryFactory factory;
    private QueryCreationContext queryCreationContext;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        customFieldManager = getMock(CustomFieldManager.class);
        searchHandlerManager = getMock(SearchHandlerManager.class);
        factory = new AllTextClauseQueryFactory(customFieldManager, searchHandlerManager);
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testGetClauseContextUnsupportedOperators() throws Exception
    {
        replay();

        for (Operator operator : Operator.values())
        {
            if (operator == Operator.LIKE)
            {
                continue;
            }
            assertFalseResultForUnsupportedOperator(operator);
        }
    }

    @Test
    public void testGetQueryMergesResults() throws Exception
    {
        final TerminalClause clause = new TerminalClauseImpl("text", Operator.LIKE, "test");

        final ClauseQueryFactory factory1 = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(factory1.getQuery(queryCreationContext, clause))
                .andReturn(QueryFactoryResult.createFalseResult());

        final ClauseQueryFactory factory2 = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(factory2.getQuery(queryCreationContext, clause))
                .andReturn(QueryFactoryResult.createFalseResult());

        replay(factory1, factory2);

        factory = new AllTextClauseQueryFactory(customFieldManager, searchHandlerManager)
        {
            @Override
            List<ClauseQueryFactory> getFactories(final QueryCreationContext queryCreationContext)
            {
                return CollectionBuilder.list(factory1, factory2);
            }
        };

        assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, clause));

        verify(factory1, factory2);
    }

    @Test
    public void testGetAllSystemFieldFactoriesNoOverride() throws Exception
    {
        final ClauseHandler commentHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory commentFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(commentHandler));
        EasyMock.expect(commentHandler.getFactory())
                .andReturn(commentFactory);

        final ClauseHandler descriptionHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory descriptionFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(descriptionHandler));
        EasyMock.expect(descriptionHandler.getFactory())
                .andReturn(descriptionFactory);

        final ClauseHandler environmentHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory environmentFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(environmentHandler));
        EasyMock.expect(environmentHandler.getFactory())
                .andReturn(environmentFactory);

        final ClauseHandler summaryHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory summaryFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(summaryHandler));
        EasyMock.expect(summaryHandler.getFactory())
                .andReturn(summaryFactory);

        replay(commentHandler, commentFactory, descriptionHandler, descriptionFactory, environmentHandler, environmentFactory, summaryHandler, summaryFactory);

        final List<ClauseQueryFactory> result = factory.getAllSystemFieldFactories(queryCreationContext);
        assertEquals(4, result.size());
        assertTrue(result.contains(commentFactory));
        assertTrue(result.contains(descriptionFactory));
        assertTrue(result.contains(environmentFactory));
        assertTrue(result.contains(summaryFactory));

        verify(commentHandler, commentFactory, descriptionHandler, descriptionFactory, environmentHandler, environmentFactory, summaryHandler, summaryFactory);
    }

    @Test
    public void testGetAllSystemFieldFactoriesOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final ClauseHandler commentHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory commentFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(commentHandler));
        EasyMock.expect(commentHandler.getFactory())
                .andReturn(commentFactory);

        final ClauseHandler descriptionHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory descriptionFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(descriptionHandler));
        EasyMock.expect(descriptionHandler.getFactory())
                .andReturn(descriptionFactory);

        final ClauseHandler environmentHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory environmentFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(environmentHandler));
        EasyMock.expect(environmentHandler.getFactory())
                .andReturn(environmentFactory);

        final ClauseHandler summaryHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory summaryFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(summaryHandler));
        EasyMock.expect(summaryHandler.getFactory())
                .andReturn(summaryFactory);

        replay(commentHandler, commentFactory, descriptionHandler, descriptionFactory, environmentHandler, environmentFactory, summaryHandler, summaryFactory);

        final List<ClauseQueryFactory> result = factory.getAllSystemFieldFactories(queryCreationContext);
        assertEquals(4, result.size());
        assertTrue(result.contains(commentFactory));
        assertTrue(result.contains(descriptionFactory));
        assertTrue(result.contains(environmentFactory));
        assertTrue(result.contains(summaryFactory));

        verify(commentHandler, commentFactory, descriptionHandler, descriptionFactory, environmentHandler, environmentFactory, summaryHandler, summaryFactory);
    }

    @Test
    public void testGetAllCustomFieldFactoriesNoOverride() throws Exception
    {
        final CustomField customFieldNullSearcher = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldNullSearcher.getCustomFieldSearcher())
                .andReturn(null);

        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);

        final CustomFieldSearcher numberSearcher = EasyMock.createMock(CustomFieldSearcher.class);
        final CustomField customFieldNonTextSearcher = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldNonTextSearcher.getCustomFieldSearcher())
                .andReturn(numberSearcher);

        final ClauseHandler textHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory textFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, "customfield_10000"))
                .andReturn(Collections.singletonList(textHandler));
        EasyMock.expect(textHandler.getFactory())
                .andReturn(textFactory);
        final CustomFieldSearcher freeTextSearcher = EasyMock.createMock(CustomFieldSearcher.class);
        final CustomField customFieldTextSearcher = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldTextSearcher.getCustomFieldSearcher())
                .andReturn(freeTextSearcher);
        EasyMock.expect(freeTextSearcher.getCustomFieldSearcherClauseHandler()).andReturn(cfSupportsAllText);
        EasyMock.expect(customFieldTextSearcher.getClauseNames())
                .andReturn(new ClauseNames("customfield_10000"));

        EasyMock.expect(customFieldManager.getCustomFieldObjects())
                .andReturn(CollectionBuilder.list(customFieldNullSearcher, customFieldNonTextSearcher, customFieldTextSearcher));

        replay(customFieldNullSearcher, numberSearcher, textHandler, textFactory, customFieldTextSearcher, freeTextSearcher);

        final List<ClauseQueryFactory> result = factory.getAllCustomFieldFactories(queryCreationContext);
        assertEquals(1, result.size());
        assertTrue(result.contains(textFactory));

        verify(customFieldNullSearcher, numberSearcher, textHandler, textFactory, customFieldTextSearcher, freeTextSearcher);
    }

    @Test
    public void testGetAllCustomFieldFactoriesOverrideSecurity() throws Exception
    {
        queryCreationContext = new QueryCreationContextImpl(theUser, true);

        final CustomField customFieldNull = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldNull.getCustomFieldSearcher())
                .andReturn(null);

        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);

        final ClauseHandler textHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory textFactory = EasyMock.createMock(ClauseQueryFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler("customfield_10000"))
                .andReturn(Collections.singletonList(textHandler));
        EasyMock.expect(textHandler.getFactory())
                .andReturn(textFactory);

        final CustomFieldSearcher freeTextSearcher = EasyMock.createMock(CustomFieldSearcher.class);

        final CustomField customFieldText = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldText.getCustomFieldSearcher())
                .andReturn(freeTextSearcher);
        EasyMock.expect(freeTextSearcher.getCustomFieldSearcherClauseHandler()).andReturn(cfSupportsAllText);
        EasyMock.expect(customFieldText.getClauseNames())
                .andReturn(new ClauseNames("customfield_10000"));

        final CustomFieldSearcher numberSearcher = EasyMock.createMock(CustomFieldSearcher.class);
        EasyMock.expect(numberSearcher.getCustomFieldSearcherClauseHandler()).andReturn(null);
        final CustomField customFieldNonText = EasyMock.createMock(CustomField.class);

        EasyMock.expect(customFieldNonText.getCustomFieldSearcher())
                .andReturn(numberSearcher);

        EasyMock.expect(customFieldManager.getCustomFieldObjects())
                .andReturn(CollectionBuilder.list(customFieldNull, customFieldNonText, customFieldText));

        replay(customFieldNull, numberSearcher, textHandler, textFactory, customFieldText, customFieldNonText, freeTextSearcher);

        final List<ClauseQueryFactory> result = factory.getAllCustomFieldFactories(queryCreationContext);
        assertEquals(1, result.size());
        assertTrue(result.contains(textFactory));

        verify(customFieldNull, numberSearcher, textHandler, textFactory, customFieldText, customFieldNonText, freeTextSearcher);
    }

    @Test
    public void testGetAllCustumFieldFactoriesSupportedOperators() throws Exception
    {
        final CustomFieldSearcher customFieldSearcher = EasyMock.createMock(CustomFieldSearcher.class);
        final CustomField customFieldSupportsLIKEOoperator = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldSupportsLIKEOoperator.getCustomFieldSearcher())
                .andReturn(customFieldSearcher);
        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);
        EasyMock.expect(customFieldSearcher.getCustomFieldSearcherClauseHandler()).andReturn(cfSupportsAllText);
        EasyMock.expect(customFieldSupportsLIKEOoperator.getClauseNames())
                .andReturn(new ClauseNames("customfield_10000"));
        final ClauseHandler textHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseQueryFactory textFactory = EasyMock.createMock(ClauseQueryFactory.class);

        final CustomFieldSearcher customFieldSearcher1 = EasyMock.createMock(CustomFieldSearcher.class);
        final CustomField customFieldDoesNotSupportLIKEOoperator = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldDoesNotSupportLIKEOoperator.getCustomFieldSearcher())
                .andReturn(customFieldSearcher1);
        SimpleAllTextCustomFieldSearcherClauseHandler cfDoesNotSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.GREATER_THAN_EQUALS).asSet(), null);
        EasyMock.expect(customFieldSearcher1.getCustomFieldSearcherClauseHandler()).andReturn(cfDoesNotSupportsAllText);

        EasyMock.expect(customFieldManager.getCustomFieldObjects())
                .andReturn(CollectionBuilder.list(customFieldSupportsLIKEOoperator, customFieldDoesNotSupportLIKEOoperator));

        EasyMock.expect(searchHandlerManager.getClauseHandler((com.atlassian.crowd.embedded.api.User) theUser, "customfield_10000"))
                .andReturn(Collections.singletonList(textHandler));
        EasyMock.expect(textHandler.getFactory())
                .andReturn(textFactory);

        replay(customFieldSearcher, customFieldSupportsLIKEOoperator, textHandler, textFactory, customFieldSearcher1, customFieldDoesNotSupportLIKEOoperator);

        final List<ClauseQueryFactory> result = factory.getAllCustomFieldFactories(queryCreationContext);
        assertEquals(1, result.size());
        assertTrue(result.contains(textFactory));

        verify(customFieldSearcher, customFieldSupportsLIKEOoperator, textHandler, textFactory, customFieldSearcher1, customFieldDoesNotSupportLIKEOoperator);
    }

    @Test
    public void testGetFactoriesHappyPath() throws Exception
    {
        final AtomicBoolean systemCalled = new AtomicBoolean(false);
        final AtomicBoolean customCalled = new AtomicBoolean(false);

        replay();

        factory = new AllTextClauseQueryFactory(customFieldManager, searchHandlerManager)
        {
            @Override
            List<ClauseQueryFactory> getAllSystemFieldFactories(final QueryCreationContext searcher)
            {
                systemCalled.set(true);
                return Collections.emptyList();
            }

            @Override
            List<ClauseQueryFactory> getAllCustomFieldFactories(final QueryCreationContext user)
            {
                customCalled.set(true);
                return Collections.emptyList();
            }
        };

        factory.getFactories(queryCreationContext);

        assertTrue(systemCalled.get());
        assertTrue(customCalled.get());
    }

    private void assertFalseResultForUnsupportedOperator(final Operator operator)
    {
        assertEquals(QueryFactoryResult.createFalseResult(), factory.getQuery(queryCreationContext, new TerminalClauseImpl("text", operator, "test")));
    }


}
