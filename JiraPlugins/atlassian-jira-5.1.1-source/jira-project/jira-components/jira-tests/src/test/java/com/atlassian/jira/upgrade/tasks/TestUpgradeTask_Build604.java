package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.parser.JqlParseErrorMessages;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.upgrade.tasks.jql.ClauseXmlHandler;
import com.atlassian.jira.upgrade.tasks.jql.ClauseXmlHandlerRegistry;
import com.atlassian.jira.upgrade.tasks.jql.OrderByXmlHandler;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestUpgradeTask_Build604 extends MockControllerTestCase
{
    private SearchHandlerManager searchHandlerManager;
    private UserUtil userUtil;
    private ClauseXmlHandlerRegistry clauseXmlHandlerRegistry;
    private OfBizDelegator ofBizDelegator;
    private SearchService searchService;
    private UpgradeTask_Build604 upgradeTask_Build604;
    private OrderByXmlHandler orderByXmlHandler;
    private JqlQueryParser jqlQueryParser;
    private MailQueue mailQueue;
    private Document doc;

    private String xml = "<searchrequest name='PM values'>\n"
            + "   <parameter class='com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter'>\n"
            + "     <projid andQuery='false'>\n"
            + "       <value>10470</value>\n"
            + "     </projid>\n"
            + "   </parameter>\n"
            + "   <parameter class='com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter'>\n"
            + "     <type andQuery='false'>\n"
            + "       <value>2</value>\n"
            + "     </type>\n"
            + "   </parameter>\n"
            + "   <parameter class='com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter'>\n"
            + "     <customfield_10570 name='customfield_10570:relative'>\n"
            + "       <previousOffset>43200000000</previousOffset>\n"
            + "     </customfield_10570>\n"
            + "   </parameter>\n"
            + "</searchrequest>";

    private String inBetweenXml = "<searchrequest name='PM values'>\n"
            + "     <parameter jql='true'>\n"
            + "    <query>type = Bug</query>\n"
            + "  </parameter>"
            + "</searchrequest>";
    private Set<Clause> clausesNotToNamify = Collections.emptySet();

    @Before
    public void setUp() throws Exception
    {
        clauseXmlHandlerRegistry = mockController.getMock(ClauseXmlHandlerRegistry.class);
        searchService = mockController.getMock(SearchService.class);
        ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        orderByXmlHandler = mockController.getMock(OrderByXmlHandler.class);
        jqlQueryParser = mockController.getMock(JqlQueryParser.class);
        mailQueue = mockController.getMock(MailQueue.class);
        searchHandlerManager = getMock(SearchHandlerManager.class);
        userUtil = getMock(UserUtil.class);

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null);
        doc = new Document(xml);
    }

    @Test
    public void testGetWhereClausesHappyPath() throws Exception
    {
        final Elements paramEls = doc.getRoot().getElements("parameter");
        final Element projEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element itEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element relDateEl = ((Element) paramEls.nextElement()).getElements().first();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final ClauseXmlHandler.ConversionResult projectConversionResult = new ClauseXmlHandler.FullConversionResult(projClause);
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final ClauseXmlHandler.ConversionResult issueTypeConversionResult = new ClauseXmlHandler.FullConversionResult(itClause);
        final Clause relDateClause = JqlQueryBuilder.newClauseBuilder().createdAfter("-4d").buildClause();
        final ClauseXmlHandler.ConversionResult relDateConversionResult = new ClauseXmlHandler.FullConversionResult(relDateClause);

        final ClauseXmlHandler handler = mockController.getMock(ClauseXmlHandler.class);
        expect(handler.isSafeToNamifyValue())
                .andStubReturn(true);
        expect(handler.convertXmlToClause(projEl))
                .andReturn(projectConversionResult);
        expect(handler.convertXmlToClause(itEl))
                .andReturn(issueTypeConversionResult);
        expect(handler.convertXmlToClause(relDateEl))
                .andReturn(relDateConversionResult);

        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter", "projid"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter", "type"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "customfield_10570"))
                .andReturn(handler);

        replay();

        final UpgradeTask_Build604.WhereClauseConversionResults conversionResults = upgradeTask_Build604.getWhereClauses(doc.getRoot().getElements("parameter"));

        assertTrue(conversionResults.getConversionMessages().isEmpty());
        assertTrue(conversionResults.getClausesNotToNamify().isEmpty());
        assertEquals(3, conversionResults.getConvertedClauses().size());
        assertTrue(conversionResults.getConvertedClauses().contains(projClause));
        assertTrue(conversionResults.getConvertedClauses().contains(itClause));
        assertTrue(conversionResults.getConvertedClauses().contains(relDateClause));
    }

    @Test
    public void testGetWhereClausesSomeShouldntBeNamified() throws Exception
    {
        final Elements paramEls = doc.getRoot().getElements("parameter");
        final Element projEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element itEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element relDateEl = ((Element) paramEls.nextElement()).getElements().first();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final ClauseXmlHandler.ConversionResult projectConversionResult = new ClauseXmlHandler.FullConversionResult(projClause);
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final ClauseXmlHandler.ConversionResult issueTypeConversionResult = new ClauseXmlHandler.FullConversionResult(itClause);
        final Clause relDateClause = JqlQueryBuilder.newClauseBuilder().createdAfter("-4d").buildClause();
        final ClauseXmlHandler.ConversionResult relDateConversionResult = new ClauseXmlHandler.FullConversionResult(relDateClause);

        final ClauseXmlHandler handler = mockController.getMock(ClauseXmlHandler.class);
        expect(handler.convertXmlToClause(projEl))
                .andReturn(projectConversionResult);
        expect(handler.isSafeToNamifyValue())
                .andReturn(true);
        expect(handler.convertXmlToClause(itEl))
                .andReturn(issueTypeConversionResult);
        expect(handler.isSafeToNamifyValue())
                .andReturn(true);
        expect(handler.convertXmlToClause(relDateEl))
                .andReturn(relDateConversionResult);
        expect(handler.isSafeToNamifyValue())
                .andReturn(false);

        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter", "projid"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter", "type"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "customfield_10570"))
                .andReturn(handler);

        replay();

        final UpgradeTask_Build604.WhereClauseConversionResults conversionResults = upgradeTask_Build604.getWhereClauses(doc.getRoot().getElements("parameter"));

        assertTrue(conversionResults.getConversionMessages().isEmpty());

        assertEquals(1, conversionResults.getClausesNotToNamify().size());
        assertTrue(conversionResults.getClausesNotToNamify().contains(relDateClause));

        assertEquals(3, conversionResults.getConvertedClauses().size());
        assertTrue(conversionResults.getConvertedClauses().contains(projClause));
        assertTrue(conversionResults.getConvertedClauses().contains(itClause));
        assertTrue(conversionResults.getConvertedClauses().contains(relDateClause));
    }

    @Test
    public void testGetWhereClausesInBetween() throws Exception
    {
        doc = new Document(inBetweenXml);
        final Elements paramEls = doc.getRoot().getElements("parameter");
        final Element jqlEl = ((Element) paramEls.nextElement()).getElements().first();

        final Clause jqlClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final ClauseXmlHandler.ConversionResult jqlConversionResult = new ClauseXmlHandler.FullConversionResult(jqlClause);

        final ClauseXmlHandler handler = mockController.getMock(ClauseXmlHandler.class);
        expect(handler.convertXmlToClause(jqlEl))
                .andReturn(jqlConversionResult);
        expect(handler.isSafeToNamifyValue())
                .andReturn(true);

        replay();

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {
            @Override
            ClauseXmlHandler createJqlClauseXmlHandler()
            {
                return handler;
            }
        };

        final UpgradeTask_Build604.WhereClauseConversionResults conversionResults = upgradeTask_Build604.getWhereClauses(doc.getRoot().getElements("parameter"));

        assertTrue(conversionResults.getConversionMessages().isEmpty());
        assertEquals(1, conversionResults.getConvertedClauses().size());
        assertTrue(conversionResults.getConvertedClauses().contains(jqlClause));
    }

    @Test
    public void testGetWhereClausesOneBestGuessConversion() throws Exception
    {
        final Elements paramEls = doc.getRoot().getElements("parameter");
        final Element projEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element itEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element relDateEl = ((Element) paramEls.nextElement()).getElements().first();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final ClauseXmlHandler.ConversionResult projectConversionResult = new ClauseXmlHandler.FullConversionResult(projClause);
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final ClauseXmlHandler.ConversionResult issueTypeConversionResult = new ClauseXmlHandler.FullConversionResult(itClause);
        final Clause relDateClause = JqlQueryBuilder.newClauseBuilder().createdAfter("-4d").buildClause();
        final ClauseXmlHandler.ConversionResult relDateConversionResult = new ClauseXmlHandler.BestGuessConversionResult(relDateClause, "customfield_10570", "cf[12345]");

        final ClauseXmlHandler handler = mockController.getMock(ClauseXmlHandler.class);
        expect(handler.isSafeToNamifyValue())
                .andStubReturn(true);
        expect(handler.convertXmlToClause(projEl))
                .andReturn(projectConversionResult);
        expect(handler.convertXmlToClause(itEl))
                .andReturn(issueTypeConversionResult);
        expect(handler.convertXmlToClause(relDateEl))
                .andReturn(relDateConversionResult);

        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter", "projid"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter", "type"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "customfield_10570"))
                .andReturn(handler);

        replay();

        final UpgradeTask_Build604.WhereClauseConversionResults conversionResults = upgradeTask_Build604.getWhereClauses(doc.getRoot().getElements("parameter"));

        assertEquals(1, conversionResults.getConversionMessages().size());
        assertEquals(relDateConversionResult, conversionResults.getConversionMessages().iterator().next());
        assertEquals(3, conversionResults.getConvertedClauses().size());
        assertTrue(conversionResults.getConvertedClauses().contains(projClause));
        assertTrue(conversionResults.getConvertedClauses().contains(itClause));
        assertTrue(conversionResults.getConvertedClauses().contains(relDateClause));
    }

    @Test
    public void testGetWhereClausesOneNoOpConversion() throws Exception
    {
        final Elements paramEls = doc.getRoot().getElements("parameter");
        final Element projEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element itEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element relDateEl = ((Element) paramEls.nextElement()).getElements().first();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final ClauseXmlHandler.ConversionResult projectConversionResult = new ClauseXmlHandler.FullConversionResult(projClause);
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final ClauseXmlHandler.ConversionResult issueTypeConversionResult = new ClauseXmlHandler.FullConversionResult(itClause);
        final ClauseXmlHandler.ConversionResult relDateConversionResult = new ClauseXmlHandler.NoOpConversionResult();

        final ClauseXmlHandler handler = mockController.getMock(ClauseXmlHandler.class);
        expect(handler.isSafeToNamifyValue())
                .andStubReturn(true);
        expect(handler.convertXmlToClause(projEl))
                .andReturn(projectConversionResult);
        expect(handler.convertXmlToClause(itEl))
                .andReturn(issueTypeConversionResult);
        expect(handler.convertXmlToClause(relDateEl))
                .andReturn(relDateConversionResult);

        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter", "projid"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter", "type"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "customfield_10570"))
                .andReturn(handler);

        mockController.replay();

        final UpgradeTask_Build604.WhereClauseConversionResults conversionResults = upgradeTask_Build604.getWhereClauses(doc.getRoot().getElements("parameter"));

        assertEquals(0, conversionResults.getConversionMessages().size());
        assertEquals(2, conversionResults.getConvertedClauses().size());
        assertTrue(conversionResults.getConvertedClauses().contains(projClause));
        assertTrue(conversionResults.getConvertedClauses().contains(itClause));
    }

    @Test
    public void testGetWhereClausesOneNotFoundConverter() throws Exception
    {
        final Elements paramEls = doc.getRoot().getElements("parameter");
        final Element projEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element itEl = ((Element) paramEls.nextElement()).getElements().first();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final ClauseXmlHandler.ConversionResult projectConversionResult = new ClauseXmlHandler.FullConversionResult(projClause);
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final ClauseXmlHandler.ConversionResult issueTypeConversionResult = new ClauseXmlHandler.FullConversionResult(itClause);

        final ClauseXmlHandler handler = mockController.getMock(ClauseXmlHandler.class);
        expect(handler.isSafeToNamifyValue())
                .andStubReturn(true);
        expect(handler.convertXmlToClause(projEl))
                .andReturn(projectConversionResult);
        expect(handler.convertXmlToClause(itEl))
                .andReturn(issueTypeConversionResult);

        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter", "projid"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter", "type"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "customfield_10570"))
                .andReturn(null);

        replay();

        final UpgradeTask_Build604.WhereClauseConversionResults conversionResults = upgradeTask_Build604.getWhereClauses(doc.getRoot().getElements("parameter"));

        assertEquals(1, conversionResults.getConversionMessages().size());
        assertEquals(new ClauseXmlHandler.FailedConversionResult("customfield_10570"), conversionResults.getConversionMessages().iterator().next());
        assertEquals(2, conversionResults.getConvertedClauses().size());
        assertTrue(conversionResults.getConvertedClauses().contains(projClause));
        assertTrue(conversionResults.getConvertedClauses().contains(itClause));
    }

    @Test
    public void testGetWhereClausesOneConversionFailed() throws Exception
    {
        final Elements paramEls = doc.getRoot().getElements("parameter");
        final Element projEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element itEl = ((Element) paramEls.nextElement()).getElements().first();
        final Element relDateEl = ((Element) paramEls.nextElement()).getElements().first();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final ClauseXmlHandler.ConversionResult projectConversionResult = new ClauseXmlHandler.FullConversionResult(projClause);
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final ClauseXmlHandler.ConversionResult issueTypeConversionResult = new ClauseXmlHandler.FullConversionResult(itClause);
        final ClauseXmlHandler.ConversionResult relDateConversionResult = new ClauseXmlHandler.FailedConversionResult("customfield_10570");

        final ClauseXmlHandler handler = mockController.getMock(ClauseXmlHandler.class);
        expect(handler.isSafeToNamifyValue())
                .andStubReturn(true);
        expect(handler.convertXmlToClause(projEl))
                .andReturn(projectConversionResult);
        expect(handler.convertXmlToClause(itEl))
                .andReturn(issueTypeConversionResult);
        expect(handler.convertXmlToClause(relDateEl))
                .andReturn(relDateConversionResult);

        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter", "projid"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter", "type"))
                .andReturn(handler);
        expect(clauseXmlHandlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "customfield_10570"))
                .andReturn(handler);

        replay();

        final UpgradeTask_Build604.WhereClauseConversionResults conversionResults = upgradeTask_Build604.getWhereClauses(doc.getRoot().getElements("parameter"));

        assertEquals(1, conversionResults.getConversionMessages().size());
        assertEquals(relDateConversionResult, conversionResults.getConversionMessages().iterator().next());
        assertEquals(2, conversionResults.getConvertedClauses().size());
        assertTrue(conversionResults.getConvertedClauses().contains(projClause));
        assertTrue(conversionResults.getConvertedClauses().contains(itClause));
    }

    @Test
    public void testGetQueryFromXmlHappyPathMultipleClauses() throws Exception
    {
        mockController.replay();
        MockGenericValue mockSR = createMockSearchRequestGV();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final Clause relDateClause = JqlQueryBuilder.newClauseBuilder().createdAfter("-4d").buildClause();

        final Query expectedQuery = JqlQueryBuilder.newBuilder().where().defaultAnd()
                .addClause(projClause)
                .addClause(itClause)
                .addClause(relDateClause)
                .endWhere().orderBy().add("dude", SortOrder.ASC).buildQuery();

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {
            @Override
            WhereClauseConversionResults getWhereClauses(final Elements paramEls)
            {
                return new WhereClauseConversionResults(CollectionBuilder.newBuilder(projClause, itClause, relDateClause).asList(), Collections.<ClauseXmlHandler.ConversionResult>emptyList(), clausesNotToNamify);
            }

            @Override
            OrderByXmlHandler.OrderByConversionResults getOrderBy(final Elements sortEls)
            {
                return new OrderByXmlHandler.OrderByConversionResults(new OrderByImpl(new SearchSort("dude", SortOrder.ASC)), Collections.<OrderByXmlHandler.ConversionError>emptyList());
            }

            @Override
            Query getNamifiedQuery(final String ownerUserName, final Query queryFromXml, final Set<Clause> clausesNotToNamify)
            {
                assertEquals("dude", ownerUserName);
                assertEquals(expectedQuery, queryFromXml);
                return queryFromXml;
            }
        };

        final Query query = upgradeTask_Build604.getQueryFromXml(mockSR);
        assertEquals(expectedQuery, query);
        assertTrue(upgradeTask_Build604.userSavedFilterConversionInformationsMap.isEmpty());
    }

    @Test
    public void testGetQueryFromXmlHappyPathSingleClauses() throws Exception
    {
        mockController.replay();
        MockGenericValue mockSR = createMockSearchRequestGV();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();

        final Query expectedQuery = JqlQueryBuilder.newBuilder().where().defaultAnd()
                .addClause(projClause)
                .endWhere().orderBy().add("dude", SortOrder.ASC).buildQuery();

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {
            @Override
            WhereClauseConversionResults getWhereClauses(final Elements paramEls)
            {
                return new WhereClauseConversionResults(CollectionBuilder.newBuilder(projClause).asList(), Collections.<ClauseXmlHandler.ConversionResult>emptyList(), clausesNotToNamify);
            }

            @Override
            OrderByXmlHandler.OrderByConversionResults getOrderBy(final Elements sortEls)
            {
                return new OrderByXmlHandler.OrderByConversionResults(new OrderByImpl(new SearchSort("dude", SortOrder.ASC)), Collections.<OrderByXmlHandler.ConversionError>emptyList());
            }

            @Override
            Query getNamifiedQuery(final String ownerUserName, final Query queryFromXml, final Set<Clause> clausesNotToNamify)
            {
                assertEquals("dude", ownerUserName);
                assertEquals(expectedQuery, queryFromXml);
                return queryFromXml;
            }
        };

        final Query query = upgradeTask_Build604.getQueryFromXml(mockSR);
        assertEquals(expectedQuery, query);
        assertTrue(upgradeTask_Build604.userSavedFilterConversionInformationsMap.isEmpty());
    }

    @Test
    public void testGetQueryFromXmlBadXml() throws Exception
    {
        mockController.replay();
        MockGenericValue mockSR = createMockSearchRequestGV("<");

        assertNull(upgradeTask_Build604.getQueryFromXml(mockSR));
        assertTrue(upgradeTask_Build604.userSavedFilterConversionInformationsMap.isEmpty());
    }

    @Test
    public void testGetQueryFromXmlConvertedClausesEmpty() throws Exception
    {
        mockController.replay();
        MockGenericValue mockSR = createMockSearchRequestGV();

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {
            @Override
            WhereClauseConversionResults getWhereClauses(final Elements paramEls)
            {
                return new WhereClauseConversionResults(Collections.<Clause>emptyList(), Collections.<ClauseXmlHandler.ConversionResult>emptyList(), clausesNotToNamify);
            }

            @Override
            OrderByXmlHandler.OrderByConversionResults getOrderBy(final Elements sortEls)
            {
                return new OrderByXmlHandler.OrderByConversionResults(new OrderByImpl(new SearchSort("dude", SortOrder.ASC)), Collections.<OrderByXmlHandler.ConversionError>emptyList());
            }
        };

        final Query query = upgradeTask_Build604.getQueryFromXml(mockSR);
        final Query expectedQuery = JqlQueryBuilder.newBuilder().orderBy().add("dude", SortOrder.ASC).buildQuery();
        assertEquals(expectedQuery, query);
        assertTrue(upgradeTask_Build604.userSavedFilterConversionInformationsMap.isEmpty());
    }

    // THis is a test for logConversionWarningIfNeeded
    @Test
    public void testGetQueryFromXmlContainsWarning() throws Exception
    {
        mockController.replay();
        MockGenericValue mockSR = createMockSearchRequestGV();

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final Clause relDateClause = JqlQueryBuilder.newClauseBuilder().createdAfter("-4d").buildClause();

        final Query expectedQuery = JqlQueryBuilder.newBuilder().where().defaultAnd()
                .addClause(projClause)
                .addClause(itClause)
                .addClause(relDateClause)
                .endWhere().orderBy().add("dude", SortOrder.ASC).buildQuery();

        final ClauseXmlHandler.FailedConversionResult failedConversionResult = new ClauseXmlHandler.FailedConversionResult("customfield_10570");
        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {
            @Override
            WhereClauseConversionResults getWhereClauses(final Elements paramEls)
            {
                return new WhereClauseConversionResults(CollectionBuilder.newBuilder(projClause, itClause, relDateClause).asList(),
                        CollectionBuilder.<ClauseXmlHandler.ConversionResult>newBuilder(failedConversionResult).asList(), clausesNotToNamify);
            }

            @Override
            OrderByXmlHandler.OrderByConversionResults getOrderBy(final Elements sortEls)
            {
                return new OrderByXmlHandler.OrderByConversionResults(new OrderByImpl(new SearchSort("dude", SortOrder.ASC)), Collections.<OrderByXmlHandler.ConversionError>emptyList());
            }

            @Override
            I18nHelper getI18n()
            {
                return new MockI18nBean();
            }

            @Override
            Query getNamifiedQuery(final String ownerUserName, final Query queryFromXml, final Set<Clause> clausesNotToNamify)
            {
                assertEquals("dude", ownerUserName);
                assertEquals(expectedQuery, queryFromXml);
                return queryFromXml;
            }
        };

        final Query query = upgradeTask_Build604.getQueryFromXml(mockSR);
        assertEquals(expectedQuery, query);
        assertFalse(upgradeTask_Build604.userSavedFilterConversionInformationsMap.isEmpty());
        final UpgradeTask_Build604.UserSavedFilterConversionInformations expectedInformation = new UpgradeTask_Build604.UserSavedFilterConversionInformations("dude");
        expectedInformation.addConversionResult(new UpgradeTask_Build604.SavedFilterConversionInformation("dude", "My Filter", 1L, Collections.<ClauseXmlHandler.ConversionResult>singletonList(failedConversionResult), Collections.<OrderByXmlHandler.ConversionError>emptyList()));
        assertEquals(expectedInformation, upgradeTask_Build604.userSavedFilterConversionInformationsMap.get("dude"));
    }

    @Test
    public void testGetQueryFromXmlContainsWarningNullAuthor() throws Exception
    {
        mockController.replay();
        MockGenericValue mockSR = createMockSearchRequestGV(xml, null);

        final Clause projClause = JqlQueryBuilder.newClauseBuilder().project("TST").buildClause();
        final Clause itClause = JqlQueryBuilder.newClauseBuilder().issueType("Bug").buildClause();
        final Clause relDateClause = JqlQueryBuilder.newClauseBuilder().createdAfter("-4d").buildClause();

        final Query expectedQuery = JqlQueryBuilder.newBuilder().where().defaultAnd()
                .addClause(projClause)
                .addClause(itClause)
                .addClause(relDateClause)
                .endWhere().orderBy().add("dude", SortOrder.ASC).buildQuery();

        final ClauseXmlHandler.FailedConversionResult failedConversionResult = new ClauseXmlHandler.FailedConversionResult("customfield_10570");
        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {
            @Override
            WhereClauseConversionResults getWhereClauses(final Elements paramEls)
            {
                return new WhereClauseConversionResults(CollectionBuilder.newBuilder(projClause, itClause, relDateClause).asList(),
                        CollectionBuilder.<ClauseXmlHandler.ConversionResult>newBuilder(failedConversionResult).asList(), clausesNotToNamify);
            }

            @Override
            OrderByXmlHandler.OrderByConversionResults getOrderBy(final Elements sortEls)
            {
                return new OrderByXmlHandler.OrderByConversionResults(new OrderByImpl(new SearchSort("dude", SortOrder.ASC)), Collections.<OrderByXmlHandler.ConversionError>emptyList());
            }

            @Override
            I18nHelper getI18n()
            {
                return new MockI18nBean();
            }

            @Override
            Query getNamifiedQuery(final String ownerUserName, final Query queryFromXml, final Set<Clause> clausesNotToNamify)
            {
                assertNull(ownerUserName);
                assertEquals(expectedQuery, queryFromXml);
                return queryFromXml;
            }
        };

        final Query query = upgradeTask_Build604.getQueryFromXml(mockSR);
        assertEquals(expectedQuery, query);
        assertTrue(upgradeTask_Build604.userSavedFilterConversionInformationsMap.isEmpty());
    }

    @SuppressWarnings ({ "ThrowableInstanceNeverThrown" })
    @Test
    public void testRequestRequiresUpgrade() throws Exception
    {
        expect(jqlQueryParser.parseQuery(""))
                .andReturn(null);

        expect(jqlQueryParser.parseQuery("bad string"))
                .andThrow(new JqlParseException(JqlParseErrorMessages.genericParseError()));

        replay();

        assertFalse(upgradeTask_Build604.requestRequiresUpgrade(null));
        assertTrue(upgradeTask_Build604.requestRequiresUpgrade("bad string"));
    }

    @Test
    public void testSendEmailNotifications() throws Exception
    {
        final UpgradeTask_Build604.UserSavedFilterConversionInformations info = new UpgradeTask_Build604.UserSavedFilterConversionInformations("dude");

        upgradeTask_Build604.userSavedFilterConversionInformationsMap.put("dude", info);

        mailQueue.addItem(isA(UpgradeTask_Build604MailItem.class));
        EasyMock.expectLastCall();

        replay();

        upgradeTask_Build604.sendEmailNotifications();
    }

    @Test
    public void testDoUpgradeNoSearchRequests() throws Exception
    {
        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {

            @Override
            void sendEmailNotifications()
            {
                // do nothing
            }

            @Override
            List<Long> getSearchRequestIds()
            {
                return Collections.emptyList();
            }
        };

        replay();

        upgradeTask_Build604.doUpgrade(false);
    }

    @Test
    public void testDoUpgradeOneSearchRequestDoesntRequireUpgrade() throws Exception
    {
        final GenericValue searchRequest = createMockSearchRequestGV();

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {

            @Override
            boolean requestRequiresUpgrade(final String string)
            {
                return false;
            }

            @Override
            void sendEmailNotifications()
            {
                // do nothing
            }

            @Override
            List<Long> getSearchRequestIds()
            {
                return Collections.singletonList(1L);
            }

            @Override
            List<GenericValue> getSearchRequestGvsForIds(final List<Long> batchSearchRequestIds)
            {
                return Collections.singletonList(searchRequest);
            }
        };

        replay();

        upgradeTask_Build604.doUpgrade(false);
    }

    @Test
    public void testDoUpgradeOneSearchRequestRequiresUpgradeQueryNotParsable() throws Exception
    {
        final AtomicBoolean storeCalled = new AtomicBoolean(false);
        final GenericValue searchRequest = createMockSearchRequestGV(storeCalled);

        expect(searchService.getGeneratedJqlString(JqlQueryBuilder.newBuilder().where().project().isEmpty().buildQuery()))
                .andReturn("test = test");

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {

            @Override
            boolean requestRequiresUpgrade(final String string)
            {
                return true;
            }

            @Override
            Query getQueryFromXml(final GenericValue searchRequestGv)
            {
                assertSame(searchRequestGv, searchRequest);
                return null;
            }

            @Override
            void sendEmailNotifications()
            {
                // do nothing
            }

            @Override
            List<Long> getSearchRequestIds()
            {
                return Collections.singletonList(1L);
            }

            @Override
            List<GenericValue> getSearchRequestGvsForIds(final List<Long> batchSearchRequestIds)
            {
                return Collections.singletonList(searchRequest);
            }
        };

        replay();

        upgradeTask_Build604.doUpgrade(false);

        assertEquals("test = test", searchRequest.getString("request"));
        assertTrue(storeCalled.get());
        assertEquals(1, upgradeTask_Build604.userSavedFilterConversionInformationsMap.size());
        final UpgradeTask_Build604.UserSavedFilterConversionInformations informations = upgradeTask_Build604.userSavedFilterConversionInformationsMap.get("dude");
        assertEquals(1, informations.getUsersSavedFilterConversionInformation().size());
        final UpgradeTask_Build604.SavedFilterConversionInformation info = informations.getUsersSavedFilterConversionInformation().iterator().next();
        assertEquals(new Long(1), info.getFilterId());
        assertEquals("My Filter", info.getFilterName());
    }

    @Test
    public void testDoUpgradeOneSearchRequestRequiresUpgradeQueryNotParsableNullAuthor() throws Exception
    {
        final AtomicBoolean storeCalled = new AtomicBoolean(false);
        final GenericValue searchRequest = createMockSearchRequestGV(storeCalled, null);

        expect(searchService.getGeneratedJqlString(JqlQueryBuilder.newBuilder().where().project().isEmpty().buildQuery()))
                .andReturn("test = test");

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {

            @Override
            boolean requestRequiresUpgrade(final String string)
            {
                return true;
            }

            @Override
            Query getQueryFromXml(final GenericValue searchRequestGv)
            {
                assertSame(searchRequestGv, searchRequest);
                return null;
            }

            @Override
            void sendEmailNotifications()
            {
                // do nothing
            }

            @Override
            List<Long> getSearchRequestIds()
            {
                return Collections.singletonList(1L);
            }

            @Override
            List<GenericValue> getSearchRequestGvsForIds(final List<Long> batchSearchRequestIds)
            {
                return Collections.singletonList(searchRequest);
            }
        };

        replay();

        upgradeTask_Build604.doUpgrade(false);

        assertEquals("test = test", searchRequest.getString("request"));
        assertTrue(storeCalled.get());
        assertTrue(upgradeTask_Build604.userSavedFilterConversionInformationsMap.isEmpty());
    }

    @Test
    public void testDoUpgradeOneSearchRequestRequiresUpgradeQueryOkay() throws Exception
    {
        final AtomicBoolean storeCalled = new AtomicBoolean(false);
        final GenericValue searchRequest = createMockSearchRequestGV(storeCalled);

        final Query query = JqlQueryBuilder.newBuilder().where().project().eq("TST").buildQuery();
        final String expectedJqlString = "project = TST";
        expect(searchService.getGeneratedJqlString(query))
                .andReturn(expectedJqlString);

        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {

            @Override
            boolean requestRequiresUpgrade(final String string)
            {
                return true;
            }

            @Override
            Query getQueryFromXml(final GenericValue searchRequestGv)
            {
                assertSame(searchRequestGv, searchRequest);
                return query;
            }

            @Override
            void sendEmailNotifications()
            {
                // do nothing
            }

            @Override
            List<Long> getSearchRequestIds()
            {
                return Collections.singletonList(1L);
            }

            @Override
            List<GenericValue> getSearchRequestGvsForIds(final List<Long> batchSearchRequestIds)
            {
                return Collections.singletonList(searchRequest);
            }

            @Override
            Query getNamifiedQuery(final String ownerUserName, final Query queryFromXml, final Set<Clause> clausesNotToNamify)
            {
                assertEquals("dude", ownerUserName);
                assertEquals(query, queryFromXml);
                return queryFromXml;
            }
        };

        replay();

        upgradeTask_Build604.doUpgrade(false);

        assertEquals(expectedJqlString, searchRequest.getString("request"));
        assertTrue(storeCalled.get());
        assertEquals(0, upgradeTask_Build604.userSavedFilterConversionInformationsMap.size());
    }

    @Test
    public void testDoUpgradeBatchingWorks() throws Exception
    {
        final AtomicBoolean storeCalled = new AtomicBoolean(false);
        final GenericValue searchRequest = createMockSearchRequestGV(storeCalled);

        final List<Long> searchRequestIds = new ArrayList<Long>(210);
        for (long i = 0; i < 210; i++)
        {
            searchRequestIds.add(i);
        }

        final Query query = JqlQueryBuilder.newBuilder().where().project().eq("TST").buildQuery();
        final String expectedJqlString = "project = TST";
        expect(searchService.getGeneratedJqlString(query))
                .andReturn(expectedJqlString).times(2);

        final AtomicInteger getSearchRequestGvsCallCount = new AtomicInteger(0);
        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null)
        {

            @Override
            boolean requestRequiresUpgrade(final String string)
            {
                return true;
            }

            @Override
            Query getQueryFromXml(final GenericValue searchRequestGv)
            {
                assertSame(searchRequestGv, searchRequest);
                return query;
            }

            @Override
            void sendEmailNotifications()
            {
                // do nothing
            }

            @Override
            List<Long> getSearchRequestIds()
            {
                return searchRequestIds;
            }

            @Override
            List<GenericValue> getSearchRequestGvsForIds(final List<Long> batchSearchRequestIds)
            {
                getSearchRequestGvsCallCount.incrementAndGet();
                if (getSearchRequestGvsCallCount.get() == 1)
                {
                    assertEquals(200, batchSearchRequestIds.size());
                }
                else
                {
                    assertEquals(10, batchSearchRequestIds.size());
                }
                return Collections.singletonList(searchRequest);
            }

            @Override
            Query getNamifiedQuery(final String ownerUserName, final Query queryFromXml, final Set<Clause> clausesNotToNamify)
            {
                assertEquals("dude", ownerUserName);
                assertEquals(query, queryFromXml);
                return queryFromXml;
            }
        };

        replay();

        upgradeTask_Build604.doUpgrade(false);

        assertEquals(expectedJqlString, searchRequest.getString("request"));
        assertEquals(2, getSearchRequestGvsCallCount.get());
        assertTrue(storeCalled.get());
        assertEquals(0, upgradeTask_Build604.userSavedFilterConversionInformationsMap.size());
    }

    @Test
    public void testUserSavedFilterConversionInformationsEquals() throws Exception
    {
        replay();

        UpgradeTask_Build604.SavedFilterConversionInformation r1 = new UpgradeTask_Build604.SavedFilterConversionInformation("dude", "My Name", 1L);
        UpgradeTask_Build604.SavedFilterConversionInformation r2 = new UpgradeTask_Build604.SavedFilterConversionInformation("dude", "My Name", 1L);
        UpgradeTask_Build604.SavedFilterConversionInformation r3 = new UpgradeTask_Build604.SavedFilterConversionInformation("fred", "My Name", 1L);
        UpgradeTask_Build604.SavedFilterConversionInformation r4 = new UpgradeTask_Build604.SavedFilterConversionInformation("dude", "My Name", 1L, Collections.<ClauseXmlHandler.ConversionResult>emptyList(), Collections.<OrderByXmlHandler.ConversionError>emptyList());

        assertTrue(r1.equals(r2));
        assertFalse(r1.equals(r3));
        assertFalse(r1.equals(r4));

        UpgradeTask_Build604.UserSavedFilterConversionInformations i1 = new UpgradeTask_Build604.UserSavedFilterConversionInformations("dude");
        i1.addConversionResult(r1);

        UpgradeTask_Build604.UserSavedFilterConversionInformations i2 = new UpgradeTask_Build604.UserSavedFilterConversionInformations("dude");
        i2.addConversionResult(r2);

        UpgradeTask_Build604.UserSavedFilterConversionInformations i3 = new UpgradeTask_Build604.UserSavedFilterConversionInformations("fred");
        i3.addConversionResult(r1);

        assertTrue(i1.equals(i2));
        assertFalse(i1.equals(i3));
    }

    @Test
    public void testNamifiedQueryWhereClauseIsNull() throws Exception
    {
        upgradeTask_Build604 = mockController.instantiate(UpgradeTask_Build604.class);

        final QueryImpl query = new QueryImpl();
        final Query namifiedQuery = upgradeTask_Build604.getNamifiedQuery(null, query, null);

        assertSame(query, namifiedQuery);
    }

    @Test
    public void testNamifiedQueryThrownException() throws Exception
    {
        final Query query = createMock(Query.class);
        expect(query.getWhereClause()).andThrow(new RuntimeException());
        expect(query.getQueryString()).andReturn("monkeys ~ bananas");
        upgradeTask_Build604 = mockController.instantiate(UpgradeTask_Build604.class);

        final Query namifiedQuery = upgradeTask_Build604.getNamifiedQuery(null, query, null);

        assertSame(query, namifiedQuery);
        verify(query);
    }

    @Test
    public void testNamifiedQueryUserIsNull() throws Exception
    {
        expect(userUtil.getUserObject(null)).andReturn(null);
        upgradeTask_Build604 = mockController.instantiate(UpgradeTask_Build604.class);

        final QueryImpl query = new QueryImpl(new TerminalClauseImpl("project", Operator.EQUALS, "HSP"));
        final Query namifiedQuery = upgradeTask_Build604.getNamifiedQuery(null, query, null);

        assertSame(query, namifiedQuery);
    }

    @Test
    public void testNamifiedQueryHappyPath() throws Exception
    {
        final User user = new MockUser("dude");
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, "10000");
        final TerminalClauseImpl issueTypeClause = new TerminalClauseImpl("issuetype", Operator.EQUALS, "3");
        final TerminalClauseImpl projectClauseNamified = new TerminalClauseImpl("project", Operator.EQUALS, "HSP");
        final TerminalClauseImpl issueTypeClauseNamified = new TerminalClauseImpl("issuetype", Operator.EQUALS, "Bug");
        final Clause where = new NotClause(new OrClause(new AndClause(projectClause, issueTypeClause)));
        final Clause whereNamified = new NotClause(new OrClause(new AndClause(projectClauseNamified, issueTypeClauseNamified)));

        final QueryImpl query = new QueryImpl(where);
        final QueryImpl queryNamified = new QueryImpl(whereNamified);

        final SearchContext searchContext = getMock(SearchContext.class);

        final IssueSearcher<?> projectSearcher = EasyMock.createMock(IssueSearcher.class);
        final SearchInputTransformer projectInputTransformer = EasyMock.createMock(SearchInputTransformer.class);
        final IssueSearcher<?> issueTypeSearcher = EasyMock.createMock(IssueSearcher.class);
        final SearchInputTransformer issueTypeInputTransformer = EasyMock.createMock(SearchInputTransformer.class);

        expect(userUtil.getUserObject("dude")).andReturn(user);
        expect(searchService.getSearchContext(user, query)).andReturn(searchContext);

        expect(searchHandlerManager.getSearchersByClauseName(user, "project", searchContext)).andReturn(Collections.<IssueSearcher<?>>singletonList(projectSearcher));
        expect(projectSearcher.getSearchInputTransformer()).andReturn(projectInputTransformer);
        projectInputTransformer.populateFromQuery(eq(user), isA(FieldValuesHolder.class), eq(new QueryImpl(projectClause)), eq(searchContext));
        EasyMock.expectLastCall();
        expect(projectInputTransformer.getSearchClause(eq(user), isA(FieldValuesHolder.class)))
                .andReturn(projectClauseNamified);

        expect(searchHandlerManager.getSearchersByClauseName(user, "issuetype", searchContext)).andReturn(Collections.<IssueSearcher<?>>singletonList(issueTypeSearcher));
        expect(issueTypeSearcher.getSearchInputTransformer()).andReturn(issueTypeInputTransformer);
        issueTypeInputTransformer.populateFromQuery(eq(user), isA(FieldValuesHolder.class), eq(new QueryImpl(issueTypeClause)), eq(searchContext));
        EasyMock.expectLastCall();
        expect(issueTypeInputTransformer.getSearchClause(eq(user), isA(FieldValuesHolder.class)))
                .andReturn(issueTypeClauseNamified);

        replay(projectSearcher, projectInputTransformer, issueTypeSearcher, issueTypeInputTransformer);
        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null);

        final Query namifiedQuery = upgradeTask_Build604.getNamifiedQuery("dude", query, Collections.<Clause>emptySet());

        assertEquals(queryNamified, namifiedQuery);

        verify(projectSearcher, projectInputTransformer, issueTypeSearcher, issueTypeInputTransformer);
    }

    @Test
    public void testNamifiedQueryNoSearcherFound() throws Exception
    {
        final User user = new MockUser("dude");
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, "10000");

        final QueryImpl query = new QueryImpl(projectClause);

        final SearchContext searchContext = getMock(SearchContext.class);

        expect(userUtil.getUserObject("dude")).andReturn(user);
        expect(searchService.getSearchContext(user, query)).andReturn(searchContext);

        expect(searchHandlerManager.getSearchersByClauseName(user, "project", searchContext)).andReturn(Collections.<IssueSearcher<?>>emptyList());

        replay();
        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null);

        final Query result = upgradeTask_Build604.getNamifiedQuery("dude", query, Collections.<Clause>emptySet());

        assertEquals(query, result);
    }

    @Test
    public void testNamifiedQueryNullNamifiedClause() throws Exception
    {
        final User user = new MockUser("dude");
        final TerminalClauseImpl projectClause = new TerminalClauseImpl("project", Operator.EQUALS, "10000");

        final QueryImpl query = new QueryImpl(projectClause);

        final SearchContext searchContext = getMock(SearchContext.class);

        final IssueSearcher<?> projectSearcher = EasyMock.createMock(IssueSearcher.class);
        final SearchInputTransformer projectInputTransformer = EasyMock.createMock(SearchInputTransformer.class);

        expect(userUtil.getUserObject("dude")).andReturn(user);
        expect(searchService.getSearchContext(user, query)).andReturn(searchContext);

        expect(searchHandlerManager.getSearchersByClauseName(user, "project", searchContext)).andReturn(Collections.<IssueSearcher<?>>singletonList(projectSearcher));
        expect(projectSearcher.getSearchInputTransformer()).andReturn(projectInputTransformer);
        projectInputTransformer.populateFromQuery(eq(user), isA(FieldValuesHolder.class), eq(new QueryImpl(projectClause)), eq(searchContext));
        EasyMock.expectLastCall();
        expect(projectInputTransformer.getSearchClause(eq(user), isA(FieldValuesHolder.class)))
                .andReturn(null);

        replay(projectSearcher, projectInputTransformer);
        upgradeTask_Build604 = new UpgradeTask_Build604(clauseXmlHandlerRegistry, ofBizDelegator, searchService, orderByXmlHandler, jqlQueryParser, mailQueue, searchHandlerManager, userUtil, null);

        final Query namifiedQuery = upgradeTask_Build604.getNamifiedQuery("dude", query, Collections.<Clause>emptySet());

        assertEquals(query, namifiedQuery);

        verify(projectSearcher, projectInputTransformer);
    }

    private MockGenericValue createMockSearchRequestGV(final AtomicBoolean storeCalled)
    {
        return createMockSearchRequestGV(storeCalled, "dude");
    }

    private MockGenericValue createMockSearchRequestGV(final AtomicBoolean storeCalled, final String author)
    {
        return new MockGenericValue("SearchRequest", EasyMap.build("request", xml, "name", "My Filter", "id", 1L, "author", author))
        {
            @Override
            public void store() throws GenericEntityException
            {
                storeCalled.set(true);
            }
        };
    }

    private MockGenericValue createMockSearchRequestGV()
    {
        return createMockSearchRequestGV(xml, "dude");
    }

    private MockGenericValue createMockSearchRequestGV(final String requestXml)
    {
        return createMockSearchRequestGV(requestXml, "dude");
    }

    private MockGenericValue createMockSearchRequestGV(final String requestXml, final String author)
    {
        return new MockGenericValue("SearchRequest", EasyMap.build("request", requestXml, "name", "My Filter", "id", 1L, "author", author));
    }
}
