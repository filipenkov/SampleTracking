package com.atlassian.jira.jql.context;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.opensymphony.user.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.SimpleAllTextCustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import org.easymock.classextension.EasyMock;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since v4.0
 */
public class TestAllTextClauseContextFactory extends MockControllerTestCase
{
    private CustomFieldManager customFieldManager;
    private SearchHandlerManager searchHandlerManager;
    private ContextSetUtil contextSetUtil;
    private AllTextClauseContextFactory factory;
    private User theUser = null;

    @Before
    public void setUp() throws Exception
    {
        customFieldManager = getMock(CustomFieldManager.class);
        searchHandlerManager = getMock(SearchHandlerManager.class);
        contextSetUtil = getMock(ContextSetUtil.class);
        factory = new AllTextClauseContextFactory(customFieldManager, searchHandlerManager, contextSetUtil);
    }

    @After
    public void tearDown() throws Exception
    {
        customFieldManager = null;
        searchHandlerManager = null;
        contextSetUtil = null;
        factory = null;
    }

    @Test
    public void testGetClauseContextMergesContextsFromFactories() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("test");
        final TerminalClauseImpl clause = new TerminalClauseImpl("text", Operator.LIKE, operand);

        final ClauseContext subContext1 = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(10L), AllIssueTypesContext.INSTANCE)));
        final ClauseContext subContext2 = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>singleton(new ProjectIssueTypeContextImpl(new ProjectContextImpl(50L), AllIssueTypesContext.INSTANCE)));

        final Set<ClauseContext> expectedSet = CollectionBuilder.newBuilder(subContext1, subContext2).asSet();

        final ClauseContextFactory subFactory1 = EasyMock.createMock(ClauseContextFactory.class);
        EasyMock.expect(subFactory1.getClauseContext(theUser, clause))
                .andReturn(subContext1);

        final ClauseContextFactory subFactory2 = EasyMock.createMock(ClauseContextFactory.class);
        EasyMock.expect(subFactory2.getClauseContext(theUser, clause))
                .andReturn(subContext2);

        EasyMock.expect(contextSetUtil.union(expectedSet))
                .andReturn(null);
        
        factory = new AllTextClauseContextFactory(customFieldManager, searchHandlerManager, contextSetUtil)
        {
            @Override
            List<ClauseContextFactory> getFactories(final User searcher)
            {
                return CollectionBuilder.list(subFactory1, subFactory2);
            }
        };
        replay(subFactory1, subFactory2);

        factory.getClauseContext(theUser, clause);

        verify(subFactory1, subFactory2);
    }

    @Test
    public void testGetAllSystemFieldFactories() throws Exception
    {
        final ClauseHandler commentHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseContextFactory commentFactory = EasyMock.createMock(ClauseContextFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forComments().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(commentHandler));
        EasyMock.expect(commentHandler.getClauseContextFactory())
                .andReturn(commentFactory);

        final ClauseHandler descriptionHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseContextFactory descriptionFactory = EasyMock.createMock(ClauseContextFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(descriptionHandler));
        EasyMock.expect(descriptionHandler.getClauseContextFactory())
                .andReturn(descriptionFactory);

        final ClauseHandler environmentHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseContextFactory environmentFactory = EasyMock.createMock(ClauseContextFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forEnvironment().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(environmentHandler));
        EasyMock.expect(environmentHandler.getClauseContextFactory())
                .andReturn(environmentFactory);

        final ClauseHandler summaryHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseContextFactory summaryFactory = EasyMock.createMock(ClauseContextFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(theUser, SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName()))
                .andReturn(Collections.singletonList(summaryHandler));
        EasyMock.expect(summaryHandler.getClauseContextFactory())
                .andReturn(summaryFactory);

        replay(commentHandler, commentFactory, descriptionHandler, descriptionFactory, environmentHandler, environmentFactory, summaryHandler, summaryFactory);

        final List<ClauseContextFactory> result = factory.getAllSystemFieldFactories(theUser);
        assertEquals(4, result.size());
        assertTrue(result.contains(commentFactory));
        assertTrue(result.contains(descriptionFactory));
        assertTrue(result.contains(environmentFactory));
        assertTrue(result.contains(summaryFactory));

        verify(commentHandler, commentFactory, descriptionHandler, descriptionFactory, environmentHandler, environmentFactory, summaryHandler, summaryFactory);
    }

    @Test
    public void testGetAllCustomFieldFactories() throws Exception
    {
        final CustomField customFieldNullSearcher = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldNullSearcher.getCustomFieldSearcher())
                .andReturn(null);

        final CustomFieldSearcher numberSearcher = EasyMock.createMock(CustomFieldSearcher.class);
        final CustomField customFieldNonText = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldNonText.getCustomFieldSearcher())
                .andReturn(numberSearcher);

        final ClauseHandler textHandler = EasyMock.createMock(ClauseHandler.class);
        final ClauseContextFactory textFactory = EasyMock.createMock(ClauseContextFactory.class);
        EasyMock.expect(searchHandlerManager.getClauseHandler(theUser, "customfield_10000"))
                .andReturn(Collections.singletonList(textHandler));
        EasyMock.expect(textHandler.getClauseContextFactory())
                .andReturn(textFactory);

        final CustomFieldSearcher freeTextSearcher = EasyMock.createMock(CustomFieldSearcher.class);
        
        SimpleAllTextCustomFieldSearcherClauseHandler cfSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.LIKE).asSet(), null);
        EasyMock.expect(freeTextSearcher.getCustomFieldSearcherClauseHandler()).andReturn(cfSupportsAllText);
        
        final CustomField customField = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customField.getCustomFieldSearcher())
                .andReturn(freeTextSearcher);
        EasyMock.expect(customField.getClauseNames())
                .andReturn(new ClauseNames("customfield_10000"));

        EasyMock.expect(customFieldManager.getCustomFieldObjects())
                .andReturn(CollectionBuilder.list(customFieldNullSearcher, customFieldNonText, customField));
        
        replay(customFieldNullSearcher, numberSearcher, textHandler, textFactory, customField, freeTextSearcher);

        final List<ClauseContextFactory> result = factory.getAllCustomFieldFactories(theUser);
        assertEquals(1, result.size());
        assertTrue(result.contains(textFactory));
        
        verify(customFieldNullSearcher, numberSearcher, textHandler, textFactory, customField, freeTextSearcher);
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
        final ClauseContextFactory textFactory = EasyMock.createMock(ClauseContextFactory.class);

        final CustomFieldSearcher customFieldSearcher1 = EasyMock.createMock(CustomFieldSearcher.class);
        final CustomField customFieldDoesNotSupportLIKEOoperator = EasyMock.createMock(CustomField.class);
        EasyMock.expect(customFieldDoesNotSupportLIKEOoperator.getCustomFieldSearcher())
                .andReturn(customFieldSearcher1);
        SimpleAllTextCustomFieldSearcherClauseHandler cfDoesNotSupportsAllText  = new SimpleAllTextCustomFieldSearcherClauseHandler(null, null, CollectionBuilder.newBuilder(Operator.GREATER_THAN_EQUALS).asSet(), null);
        EasyMock.expect(customFieldSearcher1.getCustomFieldSearcherClauseHandler()).andReturn(cfDoesNotSupportsAllText);

        EasyMock.expect(customFieldManager.getCustomFieldObjects())
                .andReturn(CollectionBuilder.list(customFieldSupportsLIKEOoperator, customFieldDoesNotSupportLIKEOoperator));

        EasyMock.expect(searchHandlerManager.getClauseHandler(theUser, "customfield_10000"))
                .andReturn(Collections.singletonList(textHandler));
        EasyMock.expect(textHandler.getClauseContextFactory())
                .andReturn(textFactory);

        replay(customFieldSearcher, customFieldSupportsLIKEOoperator, textHandler, textFactory, customFieldSearcher1, customFieldDoesNotSupportLIKEOoperator);

        final List<ClauseContextFactory> result = factory.getAllCustomFieldFactories(theUser);
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

        factory = new AllTextClauseContextFactory(customFieldManager, searchHandlerManager, contextSetUtil)
        {
            @Override
            List<ClauseContextFactory> getAllSystemFieldFactories(final User searcher)
            {
                systemCalled.set(true);
                return Collections.emptyList();
            }

            @Override
            List<ClauseContextFactory> getAllCustomFieldFactories(final User user)
            {
                customCalled.set(true);
                return Collections.emptyList();
            }
        };

        factory.getFactories(theUser);

        assertTrue(systemCalled.get());
        assertTrue(customCalled.get());
    }
}
