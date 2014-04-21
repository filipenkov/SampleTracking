package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.link.MockIssueLinkType;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.permission.LiteralSanitiser;
import com.atlassian.jira.jql.permission.MockLiteralSanitiser;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import org.easymock.classextension.EasyMock;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since v4.0
 */
public class TestLinkedIssuesFunction extends MockControllerTestCase
{
    private JqlIssueSupport jqlIssueSupport;
    private IssueLinkTypeManager issueLinkTypeManager;
    private IssueLinkManager issueLinkManager;
    private I18nHelper i18nHelper;
    private String functionName = "linkedIssues";
    private PermissionManager permissionManager;
    private com.opensymphony.user.User theUser = null;
    private QueryCreationContext queryCreationContext;

    @Before
    public void setUp() throws Exception
    {
        jqlIssueSupport = mockController.getMock(JqlIssueSupport.class);
        issueLinkTypeManager = mockController.getMock(IssueLinkTypeManager.class);
        issueLinkManager = mockController.getMock(IssueLinkManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        i18nHelper = new MockI18nHelper();
        queryCreationContext = new QueryCreationContextImpl(theUser);
    }

    @Test
    public void testDataType() throws Exception
    {
        LinkedIssuesFunction handler = mockController.instantiate(LinkedIssuesFunction.class);
        assertEquals(JiraDataTypes.ISSUE, handler.getDataType());        
    }

    @Test
    public void testGetIssueLinkTypesForArgIsInward() throws Exception
    {
        final MockIssueLinkType type = new MockIssueLinkType(1L, "link", "", "", "");

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByInwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>singleton(type));

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>emptySet());

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> result = function.getIssueLinkTypesForArg("test");

        assertEquals(1, result.size());
        final Collection<IssueLinkType> inLinkTypes = result.get(LinkedIssuesFunction.Direction.IN);
        final Collection<IssueLinkType> outLinkTypes = result.get(LinkedIssuesFunction.Direction.OUT);

        assertEquals(1, inLinkTypes.size());
        assertTrue(inLinkTypes.contains(type));

        assertNull(outLinkTypes);
    }

    @Test
    public void testGetIssueLinkTypesForArgIsBoth() throws Exception
    {
        final MockIssueLinkType type1 = new MockIssueLinkType(1L, "link", "", "", "");
        final MockIssueLinkType type2 = new MockIssueLinkType(2L, "link2", "", "", "");

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByInwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>singleton(type1));

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>singleton(type2));

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> result = function.getIssueLinkTypesForArg("test");

        assertEquals(2, result.size());
        final Collection<IssueLinkType> inLinkTypes = result.get(LinkedIssuesFunction.Direction.IN);
        final Collection<IssueLinkType> outLinkTypes = result.get(LinkedIssuesFunction.Direction.OUT);

        assertEquals(1, inLinkTypes.size());
        assertTrue(inLinkTypes.contains(type1));

        assertEquals(1, outLinkTypes.size());
        assertTrue(outLinkTypes.contains(type2));
    }

    @Test
    public void testGetIssueLinkTypesForArgNotFound() throws Exception
    {
        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByInwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>emptySet());

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>emptySet());

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> result = function.getIssueLinkTypesForArg("test");

        assertNull(result);
    }

    @Test
    public void testGetIssueLinkTypesForArgsHappyPath() throws Exception
    {
        final MockIssueLinkType type1 = new MockIssueLinkType(1L, "type1", "", "", "");
        final MockIssueLinkType type2 = new MockIssueLinkType(2L, "type2", "", "", "");
        final MockIssueLinkType type3 = new MockIssueLinkType(3L, "type3", "", "", "");

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByInwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>singleton(type1));

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>singleton(type2));

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByInwardDescription("test2"))
                .andReturn(Collections.<IssueLinkType>emptySet());

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("test2"))
                .andReturn(Collections.<IssueLinkType>singleton(type3));

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> result = function.getIssueLinkTypesForArgs(
                CollectionBuilder.newBuilder("test", "test2").asList()
        );

        assertEquals(2, result.size());
        final Collection<IssueLinkType> inLinkTypes = result.get(LinkedIssuesFunction.Direction.IN);
        final Collection<IssueLinkType> outLinkTypes = result.get(LinkedIssuesFunction.Direction.OUT);

        assertEquals(1, inLinkTypes.size());
        assertTrue(inLinkTypes.contains(type1));

        assertEquals(2, outLinkTypes.size());
        assertTrue(outLinkTypes.contains(type2));
        assertTrue(outLinkTypes.contains(type3));
    }

    @Test
    public void testGetIssueLinkTypesForArgsOneFailed() throws Exception
    {
        final MockIssueLinkType type1 = new MockIssueLinkType(1L, "type1", "", "", "");
        final MockIssueLinkType type2 = new MockIssueLinkType(2L, "type2", "", "", "");

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByInwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>singleton(type1));

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("test"))
                .andReturn(Collections.<IssueLinkType>singleton(type2));

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByInwardDescription("test2"))
                .andReturn(Collections.<IssueLinkType>emptySet());

        EasyMock.expect(issueLinkTypeManager.getIssueLinkTypesByOutwardDescription("test2"))
                .andReturn(Collections.<IssueLinkType>emptySet());

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> result = function.getIssueLinkTypesForArgs(
                CollectionBuilder.newBuilder("test", "test2").asList()
        );

        assertNull(result);
    }

    @Test
    public void testGetIssuesForArgExistsAsKey() throws Exception
    {
        final MockIssue issue = new MockIssue();

        EasyMock.expect(jqlIssueSupport.getIssues("TST-1", (com.atlassian.crowd.embedded.api.User) theUser))
                .andReturn(Collections.<Issue>singletonList(issue));

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> result = function.getIssuesForArg("TST-1", theUser, false);
        assertEquals(1, result.size());
        assertEquals(issue, result.iterator().next());
    }

    @Test
    public void testGetIssuesForArgExistsAsKeyOverrideSecurity() throws Exception
    {
        final MockIssue issue = new MockIssue();

        EasyMock.expect(jqlIssueSupport.getIssues("TST-1"))
                .andReturn(Collections.<Issue>singletonList(issue));

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> result = function.getIssuesForArg("TST-1", theUser, true);
        assertEquals(1, result.size());
        assertEquals(issue, result.iterator().next());
    }

    @Test
    public void testGetIssuesForArgIsntKeyOrId() throws Exception
    {
        EasyMock.expect(jqlIssueSupport.getIssues("TST-1", (com.atlassian.crowd.embedded.api.User) theUser))
                .andReturn(Collections.<Issue>emptyList());

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> result = function.getIssuesForArg("TST-1", theUser, false);
        assertNull(result);
    }

    @Test
    public void testGetIssuesForArgIsntKeyOrIdOverrideSecurity() throws Exception
    {
        EasyMock.expect(jqlIssueSupport.getIssues("TST-1"))
                .andReturn(Collections.<Issue>emptyList());

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> result = function.getIssuesForArg("TST-1", theUser, true);
        assertNull(result);
    }

    @Test
    public void testGetIssuesForArgExistsAsId() throws Exception
    {
        final MockIssue issue = new MockIssue();

        EasyMock.expect(jqlIssueSupport.getIssues("5555", (com.atlassian.crowd.embedded.api.User) theUser))
                .andReturn(Collections.<Issue>emptyList());

        EasyMock.expect(jqlIssueSupport.getIssue(5555L, (com.atlassian.crowd.embedded.api.User) theUser))
                .andReturn(issue);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> result = function.getIssuesForArg("5555", theUser, false);
        assertEquals(1, result.size());
        assertEquals(issue, result.iterator().next());
    }

    @Test
    public void testGetIssuesForArgExistsAsIdOverrideSecurity() throws Exception
    {
        final MockIssue issue = new MockIssue();

        EasyMock.expect(jqlIssueSupport.getIssues("5555"))
                .andReturn(Collections.<Issue>emptyList());

        EasyMock.expect(jqlIssueSupport.getIssue(5555L))
                .andReturn(issue);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> result = function.getIssuesForArg("5555", theUser, true);
        assertEquals(1, result.size());
        assertEquals(issue, result.iterator().next());
    }

    @Test
    public void testGetLinkedIssuesWithLinkTypeMapping() throws Exception
    {
        final MockIssue rootIssue = new MockIssue();
        final MockIssue linkIssue1 = new MockIssue();
        final MockIssue linkIssue2 = new MockIssue();
        final MockIssue linkIssue3 = new MockIssue();
        final Collection<Issue> rootIssues = Collections.<Issue>singleton(rootIssue);
        final IssueLinkType linkType1 = new MockIssueLinkType(1L, "linkName1", "", "", "");
        final IssueLinkType linkType2 = new MockIssueLinkType(2L, "linkName2", "", "", "");
        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> linkTypeMapping =
                MapBuilder.<LinkedIssuesFunction.Direction, Collection<IssueLinkType>>newBuilder()
                .add(LinkedIssuesFunction.Direction.IN, CollectionBuilder.newBuilder(linkType1, linkType2).asSet())
                .add(LinkedIssuesFunction.Direction.OUT, CollectionBuilder.newBuilder(linkType1, linkType2).asSet())
                .toMap();

        LinkCollection linkCollection = EasyMock.createMock(LinkCollection.class);

        EasyMock.expect(issueLinkManager.getLinkCollection(rootIssue, (User) theUser))
                .andReturn(linkCollection);

        EasyMock.expect(linkCollection.getInwardIssues("linkName1"))
                .andReturn(Collections.<Issue>singletonList(linkIssue1));

        EasyMock.expect(linkCollection.getInwardIssues("linkName2"))
                .andReturn(null);

        EasyMock.expect(linkCollection.getOutwardIssues("linkName1"))
                .andReturn(Collections.<Issue>singletonList(linkIssue2));

        EasyMock.expect(linkCollection.getOutwardIssues("linkName2"))
                .andReturn(Collections.<Issue>singletonList(linkIssue3));

        EasyMock.replay(linkCollection);
        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> linkedIssues = function.getLinkedIssues(theUser, false, rootIssues, linkTypeMapping);

        assertEquals(3, linkedIssues.size());
        assertTrue(linkedIssues.contains(linkIssue1));
        assertTrue(linkedIssues.contains(linkIssue2));
        assertTrue(linkedIssues.contains(linkIssue3));

        EasyMock.verify(linkCollection);
    }

    @Test
    public void testGetLinkedIssuesWithEmptyLinkTypeMapping() throws Exception
    {
        final MockIssue rootIssue = new MockIssue();
        final Collection<Issue> rootIssues = Collections.<Issue>singleton(rootIssue);
        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> linkTypeMapping =
                MapBuilder.<LinkedIssuesFunction.Direction, Collection<IssueLinkType>>newBuilder()
                .toMap();

        LinkCollection linkCollection = EasyMock.createMock(LinkCollection.class);

        EasyMock.expect(issueLinkManager.getLinkCollection(rootIssue, (User) theUser))
                .andReturn(linkCollection);

        EasyMock.replay(linkCollection);
        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> linkedIssues = function.getLinkedIssues(theUser, false, rootIssues, linkTypeMapping);

        assertEquals(0, linkedIssues.size());

        EasyMock.verify(linkCollection);
    }

    @Test
    public void testGetLinkedIssuesWithUnspecifiedMapping() throws Exception
    {
        final MockIssue rootIssue = new MockIssue();
        final MockIssue linkIssue1 = new MockIssue();
        final Collection<Issue> rootIssues = Collections.<Issue>singleton(rootIssue);
        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> linkTypeMapping = null;

        LinkCollection linkCollection = EasyMock.createMock(LinkCollection.class);

        EasyMock.expect(issueLinkManager.getLinkCollection(rootIssue, (User) theUser))
                .andReturn(linkCollection);

        EasyMock.expect(linkCollection.getAllIssues())
                .andReturn(Collections.<Issue>singletonList(linkIssue1));

        EasyMock.replay(linkCollection);
        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> linkedIssues = function.getLinkedIssues(theUser, false, rootIssues, linkTypeMapping);

        assertEquals(1, linkedIssues.size());
        assertTrue(linkedIssues.contains(linkIssue1));

        EasyMock.verify(linkCollection);
    }

    @Test
    public void testGetLinkedIssuesWithUnspecifiedMappingOverrideSecurity() throws Exception
    {
        final MockIssue rootIssue = new MockIssue();
        final MockIssue linkIssue1 = new MockIssue();
        final Collection<Issue> rootIssues = Collections.<Issue>singleton(rootIssue);
        final Map<LinkedIssuesFunction.Direction, Collection<IssueLinkType>> linkTypeMapping = null;

        LinkCollection linkCollection = EasyMock.createMock(LinkCollection.class);

        EasyMock.expect(issueLinkManager.getLinkCollectionOverrideSecurity(rootIssue))
                .andReturn(linkCollection);

        EasyMock.expect(linkCollection.getAllIssues())
                .andReturn(Collections.<Issue>singletonList(linkIssue1));

        EasyMock.replay(linkCollection);
        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final Collection<Issue> linkedIssues = function.getLinkedIssues(theUser, true, rootIssues, linkTypeMapping);

        assertEquals(1, linkedIssues.size());
        assertTrue(linkedIssues.contains(linkIssue1));

        EasyMock.verify(linkCollection);
    }

    @Test
    public void testGetValuesLinkingDisabled() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(functionName);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(false);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final List<QueryLiteral> result = function.getValues(queryCreationContext, operand, clause);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetValuesIncorrectNumberOfArgs() throws Exception
    {
        final FunctionOperand operand1 = new FunctionOperand(functionName);
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("issue", Operator.IN, operand1);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andStubReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final List<QueryLiteral> result1 = function.getValues(queryCreationContext, operand1, clause1);
        assertTrue(result1.isEmpty());
    }

    @Test
    public void testGetValuesNoIssuesResolved() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1");
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return null;
            }
        };

        final List<QueryLiteral> result = function.getValues(queryCreationContext, operand, clause);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetValuesNoLinkTypesResolved() throws Exception
    {
        final MockIssue issue = new MockIssue();
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1", "badlinktype");
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return Collections.<Issue>singleton(issue);
            }

            @Override
            Map<Direction, Collection<IssueLinkType>> getIssueLinkTypesForArgs(final List<String> linkDescArgs)
            {
                assertEquals("badlinktype", linkDescArgs.get(0));
                return null;
            }
        };

        final List<QueryLiteral> result = function.getValues(queryCreationContext, operand, clause);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final MockIssue rootIssue = new MockIssue(123L);
        final MockIssue linkedIssue = new MockIssue(456L);
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1");
        final TerminalClauseImpl clause = new TerminalClauseImpl("rootIssue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return Collections.<Issue>singleton(rootIssue);
            }

            @Override
            Collection<Issue> getLinkedIssues(final User searcher, final boolean overrideSecurity, final Collection<Issue> rootIssues, final Map<Direction, Collection<IssueLinkType>> linkTypeMappings)
            {
                assertEquals(1, rootIssues.size());
                assertTrue(rootIssues.contains(rootIssue));
                assertNull(linkTypeMappings);
                return Collections.<Issue>singleton(linkedIssue);
            }
        };

        final List<QueryLiteral> result = function.getValues(queryCreationContext, operand, clause);

        assertEquals(1, result.size());
        assertEquals(createLiteral(456L), result.get(0));
        assertEquals(operand, result.get(0).getSourceOperand());
    }

    @Test
    public void testValidateLinkingDisabled() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(functionName);
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(false);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return i18nHelper;
            }

            @Override
            public String getFunctionName()
            {
                return functionName;
            }
        };

        final MessageSet result = function.validate(theUser, operand, clause);

        assertEquals(1, result.getErrorMessages().size());
        assertTrue(result.getErrorMessages().contains("jira.jql.function.linked.issues.disabled " + functionName));
    }

    @Test
    public void testValidateIncorrectNumberOfArgs() throws Exception
    {
        final FunctionOperand operand1 = new FunctionOperand(functionName);
        final TerminalClauseImpl clause1 = new TerminalClauseImpl("issue", Operator.IN, operand1);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andStubReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            protected I18nHelper getI18n()
            {
                return i18nHelper;
            }

            @Override
            public String getFunctionName()
            {
                return functionName;
            }
        };

        final MessageSet result1 = function.validate(theUser, operand1, clause1);

        assertEquals(1, result1.getErrorMessages().size());
        assertTrue(result1.getErrorMessages().contains("jira.jql.function.linked.issues.incorrect.usage " + functionName));
    }

    @Test
    public void testValidateNoIssuesResolved() throws Exception
    {
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1");
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return null;
            }

            @Override
            protected I18nHelper getI18n()
            {
                return i18nHelper;
            }

            @Override
            public String getFunctionName()
            {
                return functionName;
            }
        };

        final MessageSet result1 = function.validate(theUser, operand, clause);

        assertEquals(1, result1.getErrorMessages().size());
        assertTrue(result1.getErrorMessages().contains(String.format("jira.jql.function.linked.issues.issue.not.found %s %s", functionName, "TST-1")));
    }

    @Test
    public void testValidateNoLinkTypeResolvedFirstArg() throws Exception
    {
        final MockIssue issue = new MockIssue();
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1", "badlinktype");
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return Collections.<Issue>singleton(issue);
            }

            @Override
            Map<Direction, Collection<IssueLinkType>> getIssueLinkTypesForArg(final String linkTypeArg)
            {
                assertEquals("badlinktype", linkTypeArg);
                return null;
            }

            @Override
            protected I18nHelper getI18n()
            {
                return i18nHelper;
            }

            @Override
            public String getFunctionName()
            {
                return functionName;
            }
        };

        final MessageSet result1 = function.validate(theUser, operand, clause);

        assertEquals(1, result1.getErrorMessages().size());
        assertTrue(result1.getErrorMessages().contains(String.format("jira.jql.function.linked.issues.link.type.not.found %s %s", functionName, "badlinktype")));
    }

    @Test
    public void testValidateNoLinkTypeResolvedSecondArg() throws Exception
    {
        final MockIssue issue = new MockIssue();
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1", "goodlinktype", "badlinktype");
        final TerminalClauseImpl clause = new TerminalClauseImpl("issue", Operator.IN, operand);
        final AtomicInteger callCount = new AtomicInteger(0);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return Collections.<Issue>singleton(issue);
            }

            @Override
            Map<Direction, Collection<IssueLinkType>> getIssueLinkTypesForArg(final String linkTypeArg)
            {
                callCount.incrementAndGet();

                if (callCount.get() == 1)
                {
                    assertEquals("goodlinktype", linkTypeArg);
                    return new LinkedHashMap<Direction, Collection<IssueLinkType>>();
                }
                else
                {
                    assertEquals("badlinktype", linkTypeArg);
                    return null;
                }
            }

            @Override
            protected I18nHelper getI18n()
            {
                return i18nHelper;
            }

            @Override
            public String getFunctionName()
            {
                return functionName;
            }
        };

        final MessageSet result1 = function.validate(theUser, operand, clause);

        assertEquals(1, result1.getErrorMessages().size());
        assertTrue(result1.getErrorMessages().contains(String.format("jira.jql.function.linked.issues.link.type.not.found %s %s", functionName, "badlinktype")));
    }

    @Test
    public void testValidateHappyPathOneArg() throws Exception
    {
        final MockIssue rootIssue = new MockIssue(123L);
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1");
        final TerminalClauseImpl clause = new TerminalClauseImpl("rootIssue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return Collections.<Issue>singleton(rootIssue);
            }
        };

        final MessageSet result = function.validate(theUser, operand, clause);

        assertFalse(result.hasAnyMessages());
    }

    @Test
    public void testValidateHappyPathTwoArgs() throws Exception
    {
        final MockIssue rootIssue = new MockIssue(123L);
        final FunctionOperand operand = new FunctionOperand(functionName, "TST-1", "linktype");
        final TerminalClauseImpl clause = new TerminalClauseImpl("rootIssue", Operator.IN, operand);

        EasyMock.expect(issueLinkManager.isLinkingEnabled())
                .andReturn(true);

        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            Collection<Issue> getIssuesForArg(final String issueArg, final User searcher, final boolean overrideSecurity)
            {
                assertEquals("TST-1", issueArg);
                return Collections.<Issue>singleton(rootIssue);
            }

            @Override
            Map<Direction, Collection<IssueLinkType>> getIssueLinkTypesForArg(final String linkTypeArg)
            {
                assertEquals("linktype", linkTypeArg);
                return new LinkedHashMap<Direction, Collection<IssueLinkType>>();
            }
        };

        final MessageSet result = function.validate(theUser, operand, clause);

        assertFalse(result.hasAnyMessages());
    }

    @Test
    public void testSanitiseEmptyArgs() throws Exception
    {
        replay();

        LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);

        final FunctionOperand inputOperand = new FunctionOperand(functionName);
        final FunctionOperand cleanOperand = function.sanitiseOperand(theUser, inputOperand);
        assertSame(cleanOperand, inputOperand);

        verify();
    }

    @Test
    public void testSanitiseNotModified() throws Exception
    {
        replay();

        final MockLiteralSanitiser sanitiser = new MockLiteralSanitiser(new LiteralSanitiser.Result(false, null), createLiteral("arg1"));
        LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            LiteralSanitiser createLiteralSanitiser(final User user)
            {
                return sanitiser;
            }
        };

        final FunctionOperand inputOperand = new FunctionOperand(functionName, "arg1", "arg2", "arg3");
        final FunctionOperand cleanOperand = function.sanitiseOperand(theUser, inputOperand);
        assertSame(cleanOperand, inputOperand);

        verify();
    }

    @Test
    public void testSanitiseModified() throws Exception
    {
        replay();

        final MockLiteralSanitiser sanitiser = new MockLiteralSanitiser(new LiteralSanitiser.Result(true, CollectionBuilder.newBuilder(createLiteral("clean"), createLiteral("ignored")).asList()), createLiteral("arg1"));
        LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager)
        {
            @Override
            LiteralSanitiser createLiteralSanitiser(final User user)
            {
                return sanitiser;
            }
        };

        final FunctionOperand inputOperand = new FunctionOperand(functionName, "arg1", "arg2", "arg3");
        final FunctionOperand expectedOperand = new FunctionOperand(functionName, "clean", "arg2", "arg3");
        final FunctionOperand cleanOperand = function.sanitiseOperand(theUser, inputOperand);
        assertEquals(expectedOperand, cleanOperand);

        verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        replay();

        final LinkedIssuesFunction function = new LinkedIssuesFunction(jqlIssueSupport, issueLinkTypeManager, issueLinkManager, permissionManager);
        assertEquals(1, function.getMinimumNumberOfExpectedArguments());
    }

}
