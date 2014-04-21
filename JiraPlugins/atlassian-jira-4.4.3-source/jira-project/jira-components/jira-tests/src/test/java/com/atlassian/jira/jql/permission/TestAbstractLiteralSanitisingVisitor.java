package com.atlassian.jira.jql.permission;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;
import com.atlassian.jira.jql.validator.MockJqlOperandResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.easymock.classextension.EasyMock;

import java.util.Collections;
import java.util.List;

/**
 * @since v4.0
 */
public class TestAbstractLiteralSanitisingVisitor extends MockControllerTestCase
{
    private User theUser;

    @Before
    public void setUp() throws Exception
    {
        theUser = new User("fred", new MockProviderAccessor(), new MockCrowdService());
        mockController.addObjectInstance(theUser);
    }

    @Test
    public void testVisitNoValues() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("HSP");
        TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, operand);
        mockController.addObjectInstance(clause);

        final JqlOperandResolver operandResolver = mockController.getMock(JqlOperandResolver.class);
        EasyMock.expect(operandResolver.getValues((com.atlassian.crowd.embedded.api.User) theUser, operand, clause)).andReturn(null);

        mockController.replay();
        
        final AbstractLiteralSanitisingVisitor visitor = new AbstractLiteralSanitisingVisitor(operandResolver, theUser, clause)
        {
            @Override
            protected LiteralSanitiser createLiteralSanitiser()
            {
                return new MockLiteralSanitiser(new LiteralSanitiser.Result(false, Collections.<QueryLiteral>emptyList()));
            }
        };

        assertSame(operand, visitor.visit(operand));

        mockController.verify();
    }

    @Test
    public void testNoModification() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("HSP");
        TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, operand);

        AbstractLiteralSanitisingVisitor visitor = createVisitor(false, null, clause, createLiteral("HSP"));

        assertSame(operand, visitor.visit(operand));
        
        mockController.verify();
    }

    @Test
    public void testModificationWithOneResultantLiteral() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final SingleValueOperand expectedOperand = new SingleValueOperand("NEW HSP");
        TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, inputOperand);

        AbstractLiteralSanitisingVisitor visitor = createVisitor(true, Collections.singletonList(createLiteral("NEW HSP")), clause, createLiteral("HSP"));

        assertEquals(expectedOperand, visitor.visit(inputOperand));
        
        mockController.verify();
    }

    @Test
    public void testModificationWithTwoResultantLiterals() throws Exception
    {
        final SingleValueOperand inputOperand = new SingleValueOperand("HSP");
        final MultiValueOperand expectedOperand = new MultiValueOperand("NEW", "HSP");
        TerminalClause clause = new TerminalClauseImpl("bah", Operator.EQUALS, inputOperand);

        AbstractLiteralSanitisingVisitor visitor = createVisitor(true, CollectionBuilder.newBuilder(createLiteral("NEW"), createLiteral("HSP")).asList(), clause, createLiteral("HSP"));

        assertEquals(expectedOperand, visitor.visit(inputOperand));
        
        mockController.verify();
    }

    private AbstractLiteralSanitisingVisitor createVisitor(final boolean isModified, final List<QueryLiteral> literals, final TerminalClause terminalClause, final QueryLiteral... expectedLiterals)
    {
        final JqlOperandResolver operandResolver = MockJqlOperandResolver.createSimpleSupport();

        final AbstractLiteralSanitisingVisitor visitor = new AbstractLiteralSanitisingVisitor(operandResolver, theUser, terminalClause)
        {
            @Override
            protected LiteralSanitiser createLiteralSanitiser()
            {
                return new MockLiteralSanitiser(new LiteralSanitiser.Result(isModified, literals), expectedLiterals);
            }
        };

        mockController.replay();
        
        return visitor;
    }

}
