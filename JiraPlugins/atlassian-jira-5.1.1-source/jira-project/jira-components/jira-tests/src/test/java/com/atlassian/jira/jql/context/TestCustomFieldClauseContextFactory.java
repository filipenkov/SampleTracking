package com.atlassian.jira.jql.context;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestCustomFieldClauseContextFactory extends MockControllerTestCase
{
    private CustomField customField;
    private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    private ContextSetUtil contextSetUtil;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        fieldConfigSchemeClauseContextUtil = mockController.getMock(FieldConfigSchemeClauseContextUtil.class);
        contextSetUtil = mockController.getMock(ContextSetUtil.class);
    }

    @After
    public void tearDown() throws Exception
    {
        customField = null;
        fieldConfigSchemeClauseContextUtil = null;
        contextSetUtil = null;
    }

    @Test
    public void testGetClauseContextNoGlobalContextSeen() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme1 = mockController.getMock(FieldConfigScheme.class);
        final FieldConfigScheme fieldConfigScheme2 = mockController.getMock(FieldConfigScheme.class);
        final ClauseContext context2 = createForProjects(37);
        final ClauseContext context4 = createForProjects(37383);
        final ClauseContext context5 = createForProjects(37394782, 3483);

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(fieldConfigScheme1, fieldConfigScheme2));

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).andReturn(context2);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme2)).andReturn(context4);
        expect(contextSetUtil.union(CollectionBuilder.newBuilder(context2, context4).asSet())).andReturn(context5);

        mockController.replay();

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);
        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertSame(context5, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextGlobalContextSeen() throws Exception
    {
        final FieldConfigScheme fieldConfigScheme1 = mockController.getMock(FieldConfigScheme.class);
        final FieldConfigScheme fieldConfigScheme2 = mockController.getMock(FieldConfigScheme.class);

        final ClauseContext context2 = createForProjects(37);
        final ClauseContext context4 = ClauseContextImpl.createGlobalClauseContext();

        expect(customField.getConfigurationSchemes()).andReturn(Arrays.asList(fieldConfigScheme1, fieldConfigScheme2));

        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).andReturn(context2);
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme2)).andReturn(context4);

        mockController.replay();

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);
        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertSame(ClauseContextImpl.createGlobalClauseContext(), result);

        mockController.verify();
    }

    @Test
    public void testGetClauseContextNoFieldSchemes() throws Exception
    {
        expect(customField.getConfigurationSchemes()).andReturn(Collections.<FieldConfigScheme>emptyList());

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);

        mockController.replay();

        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        final ClauseContext expectedResult = ClauseContextImpl.createGlobalClauseContext();
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextOneContext() throws Exception
    {
        final ClauseContext context2 = createForProjects(373883);

        final FieldConfigScheme fieldConfigScheme1 = mockController.getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(Collections.<FieldConfigScheme>singletonList(fieldConfigScheme1));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).andReturn(context2);

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);

        mockController.replay();

        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertEquals(context2, result);
        mockController.verify();
    }

    @Test
    public void testGetClauseContextEmptyContext() throws Exception
    {
        final ClauseContext context2 = new ClauseContextImpl(Collections.<ProjectIssueTypeContext>emptySet());

        final FieldConfigScheme fieldConfigScheme1 = mockController.getMock(FieldConfigScheme.class);

        expect(customField.getConfigurationSchemes()).andReturn(Collections.<FieldConfigScheme>singletonList(fieldConfigScheme1));
        expect(fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(null, fieldConfigScheme1)).andReturn(context2);

        final CustomFieldClauseContextFactory clauseContextFactory = new CustomFieldClauseContextFactory(customField, fieldConfigSchemeClauseContextUtil, contextSetUtil);

        mockController.replay();

        final ClauseContext result = clauseContextFactory.getClauseContext(null, new TerminalClauseImpl("blah", Operator.LESS_THAN_EQUALS, "blah"));
        assertEquals(ClauseContextImpl.createGlobalClauseContext(), result);
        
        mockController.verify();
    }

    private static ClauseContext createForProjects(long... ids)
    {
        Set<ProjectIssueTypeContext> ctxs = new HashSet<ProjectIssueTypeContext>();
        for (long id : ids)
        {
            ctxs.add(new ProjectIssueTypeContextImpl(new ProjectContextImpl(id), AllIssueTypesContext.getInstance()));
        }
        return new ClauseContextImpl(ctxs);
    }
}
