package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.IssueConstantInfoResolver;
import com.atlassian.jira.jql.resolver.MockPriorityResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.Lists;
import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test for {@link RawValuesExistValidator}.
 *
 * @since v4.0
 */
public class TestRawValuesExistValidator extends MockControllerTestCase
{
    private User theUser = null;
    private I18nHelper.BeanFactory beanFactory;


    @Before
    public void setUp()
    {
        beanFactory = mockController.getMock(I18nHelper.BeanFactory.class);
    }


    @Test
    public void testLookupFailureStringValue()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("major");

        NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);
        RawValuesExistValidator clauseValidator = new RawValuesExistValidator(MockJqlOperandResolver.createSimpleSupport(), priorityIndexInfoResolver, beanFactory)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);
        replay();
        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals("The value 'major' does not exist for the field 'priority'.", messages.getErrorMessages().iterator().next());
    }

    @Test
    public void testLookupFailureStringValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand("major");
        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause));
        mockController.setReturnValue(Collections.singletonList(createLiteral("major")));
        operandHandler.isFunction();
        mockController.setReturnValue(true);

        mockController.replay();

        final MockJqlOperandResolver mockJqlOperandSupport = new MockJqlOperandResolver().addHandler("SingleValueOperand", operandHandler);

        NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);
        RawValuesExistValidator clauseValidator = new RawValuesExistValidator(mockJqlOperandSupport, priorityIndexInfoResolver, beanFactory)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'priority'.", messages.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testLookupFailureLongValue()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(12345L);

        NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);
        RawValuesExistValidator clauseValidator = new RawValuesExistValidator(MockJqlOperandResolver.createSimpleSupport(), priorityIndexInfoResolver, beanFactory)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };
        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);
        replay();
        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals("A value with ID '12345' does not exist for the field 'priority'.", messages.getErrorMessages().iterator().next());
    }

    @Test
    public void testLookupFailureLongValueFromFunction()
    {
        final SingleValueOperand singleValueOperand = new SingleValueOperand(12345L);
        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.EQUALS, singleValueOperand);

        final OperandHandler operandHandler = mockController.getMock(OperandHandler.class);
        operandHandler.getValues(isA(QueryCreationContext.class), eq(singleValueOperand), eq(priorityClause));
        mockController.setReturnValue(Collections.singletonList(createLiteral(12345L)));
        operandHandler.isFunction();
        mockController.setReturnValue(true);

        mockController.replay();

        final MockJqlOperandResolver mockJqlOperandSupport = new MockJqlOperandResolver().addHandler("SingleValueOperand", operandHandler);

        NameResolver<Priority> lookupFailer = new FailingPriorityResolver();
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(lookupFailer);
        RawValuesExistValidator clauseValidator = new RawValuesExistValidator(mockJqlOperandSupport, priorityIndexInfoResolver, beanFactory)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messages = clauseValidator.validate(theUser, priorityClause);
        assertTrue(messages.hasAnyErrors());
        assertEquals("A value provided by the function 'SingleValueOperand' is invalid for the field 'priority'.", messages.getErrorMessages().iterator().next());
        mockController.verify();
    }

    @Test
    public void testLookupLongAsName()
    {
        final MultiValueOperand operand = new MultiValueOperand(111L, 123L);
        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isValidOperand(operand);
        mockController.setReturnValue(true);
        jqlOperandResolver.getValues(theUser, operand, priorityClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral(111L), createLiteral(123L)).asList());
        mockController.replay();

        LinkedHashMap<String, List<Long>> priorityConfig = new LinkedHashMap<String, List<Long>>();
        priorityConfig.put("111", Lists.newArrayList(1L));
        priorityConfig.put("123", Lists.newArrayList(2L));
        NameResolver<Priority> priorityResolver = new MockPriorityResolver(priorityConfig);
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);
        RawValuesExistValidator clauseValidator = new RawValuesExistValidator(jqlOperandResolver, priorityIndexInfoResolver, beanFactory)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        MessageSet errorCollection = clauseValidator.validate(theUser, priorityClause);

        assertFalse(errorCollection.hasAnyMessages());
    }

    @Test
    public void testNoOperandHandler()
    {
        final JqlOperandResolver jqlOperandResolver = getMock(JqlOperandResolver.class);
        expect(jqlOperandResolver.isValidOperand(this.<Operand>anyObject())).andReturn(false);
        replay();

        LinkedHashMap<String, List<Long>> priorityConfig = new LinkedHashMap<String, List<Long>>();
        priorityConfig.put("major", Lists.newArrayList(1L));
        priorityConfig.put("minor", Lists.newArrayList(2L));
        NameResolver<Priority> priorityResolver = new MockPriorityResolver(priorityConfig);
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);
        RawValuesExistValidator clauseValidator = new RawValuesExistValidator(jqlOperandResolver, priorityIndexInfoResolver, beanFactory)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, new MultiValueOperand("major", "minor"));
        MessageSet errorCollection = clauseValidator.validate(theUser, priorityClause);
        assertFalse(errorCollection.hasAnyMessages());
    }

    @Test
    public void testHappyPath()
    {
        final MultiValueOperand operand = new MultiValueOperand("major", "minor");
        TerminalClause priorityClause = new TerminalClauseImpl(IssueFieldConstants.PRIORITY, Operator.IN, operand);

        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        jqlOperandResolver.isValidOperand(operand);
        mockController.setReturnValue(true);
        jqlOperandResolver.getValues(theUser, operand, priorityClause);
        mockController.setReturnValue(CollectionBuilder.newBuilder(createLiteral("major"), createLiteral("minor")).asList());
        mockController.replay();

        LinkedHashMap<String, List<Long>> priorityConfig = new LinkedHashMap<String, List<Long>>();
        priorityConfig.put("major", Lists.newArrayList(1L));
        priorityConfig.put("minor", Lists.newArrayList(2L));
        NameResolver<Priority> priorityResolver = new MockPriorityResolver(priorityConfig);
        final IssueConstantInfoResolver<Priority> priorityIndexInfoResolver = new IssueConstantInfoResolver<Priority>(priorityResolver);
        RawValuesExistValidator clauseValidator = new RawValuesExistValidator(jqlOperandResolver, priorityIndexInfoResolver, beanFactory)
        {
            protected I18nHelper getI18n(final User user)
            {
                return new MockI18nBean();
            }
        };

        MessageSet errorCollection = clauseValidator.validate(theUser, priorityClause);
        assertFalse(errorCollection.hasAnyMessages());
     }

    @Test
    public void testNullArgs()
    {
        final JqlOperandResolver jqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockController.replay();
        try
        {
            new RawValuesExistValidator(jqlOperandResolver, null, beanFactory);
            fail("expected exception");
        }
        catch (IllegalArgumentException expected)
        {

        }
        try
        {
            final MockControl mockIndexInfoResolverControl = MockControl.createStrictControl(IndexInfoResolver.class);
            final IndexInfoResolver mockIndexInfoResolver = (IndexInfoResolver) mockIndexInfoResolverControl.getMock();
            mockIndexInfoResolverControl.replay();
            new RawValuesExistValidator(null, mockIndexInfoResolver, beanFactory);
            fail("expected exception");
        }
        catch (IllegalArgumentException expected)
        {

        }

        mockController.verify();
    }

    /**
     * A NameResolver<Priority> which always fails lookups.
     */
    private static class FailingPriorityResolver implements NameResolver<Priority>
    {
        public List<String> getIdsFromName(final String name)
        {
            return Collections.emptyList();
        }

        public boolean nameExists(final String name)
        {
            return false;
        }

        public boolean idExists(final Long id)
        {
            return false;
        }

        public Priority get(final Long id)
        {
            return null;
        }

        public Collection<Priority> getAll()
        {
            return Collections.emptyList();
        }

    }
}
