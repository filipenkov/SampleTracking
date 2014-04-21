package com.atlassian.jira.jql.validator;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.OperandHandler;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.NotClause;
import com.atlassian.query.clause.OrClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.SingleValueOperand;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.0
 */
public class TestValidatorVisitor extends MockControllerTestCase
{
    @Test
    public void testVisitAndClauseHappyPath() throws Exception
    {
        ValidatorVisitor validatorVisitor = new ValidatorVisitor(null, null, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(validatorVisitor);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseControl.setReturnValue(set1);
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(validatorVisitor);
        final MessageSetImpl set2 = new MessageSetImpl();
        set2.addErrorMessage("Error 2");
        mockClauseControl2.setReturnValue(set2);
        mockClauseControl2.replay();

        final MockControl mockAndClauseControl = MockClassControl.createControl(AndClause.class);
        final AndClause mockAndClause = (AndClause) mockAndClauseControl.getMock();
        mockAndClause.getClauses();
        mockAndClauseControl.setReturnValue(EasyList.build(mockClause, mockClause2));
        mockAndClauseControl.replay();

        final MessageSet messageSet = validatorVisitor.visit(mockAndClause);

        assertMessageSetErrors(messageSet, "Error 1", "Error 2");
        assertMessageSetWarnings(messageSet);

        mockClauseControl.verify();
        mockClauseControl2.verify();
        mockAndClauseControl.verify();
    }

    @Test
    public void testVisitAndClauseOneNullQuery() throws Exception
    {
        ValidatorVisitor validatorVisitor = new ValidatorVisitor(null, null, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(validatorVisitor);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseControl.setReturnValue(set1);
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(validatorVisitor);
        mockClauseControl2.setReturnValue(null);
        mockClauseControl2.replay();

        final MockControl mockAndClauseControl = MockClassControl.createControl(AndClause.class);
        final AndClause mockAndClause = (AndClause) mockAndClauseControl.getMock();
        mockAndClause.getClauses();
        mockAndClauseControl.setReturnValue(EasyList.build(mockClause, mockClause2));
        mockAndClauseControl.replay();

        final MessageSet messageSet = validatorVisitor.visit(mockAndClause);

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);

        mockClauseControl.verify();
        mockClauseControl2.verify();
        mockAndClauseControl.verify();
    }

    @Test
    public void testVisitOrClauseHappyPath() throws Exception
    {
        ValidatorVisitor validatorVisitor = new ValidatorVisitor(null, null, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(validatorVisitor);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseControl.setReturnValue(set1);
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(validatorVisitor);
        final MessageSetImpl set2 = new MessageSetImpl();
        set2.addErrorMessage("Error 2");
        mockClauseControl2.setReturnValue(set2);
        mockClauseControl2.replay();

        final MockControl mockOrClauseControl = MockClassControl.createControl(OrClause.class);
        final OrClause mockOrClause = (OrClause) mockOrClauseControl.getMock();
        mockOrClause.getClauses();
        mockOrClauseControl.setReturnValue(EasyList.build(mockClause, mockClause2));
        mockOrClauseControl.replay();

        final MessageSet messageSet = validatorVisitor.visit(mockOrClause);

        assertMessageSetErrors(messageSet, "Error 1", "Error 2");
        assertMessageSetWarnings(messageSet);

        mockClauseControl.verify();
        mockClauseControl2.verify();
        mockOrClauseControl.verify();
    }

    @Test
    public void testVisitOrClauseOneNullQuery() throws Exception
    {
        ValidatorVisitor validatorVisitor = new ValidatorVisitor(null, null, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(validatorVisitor);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseControl.setReturnValue(set1);
        mockClauseControl.replay();

        final MockControl mockClauseControl2 = MockControl.createStrictControl(Clause.class);
        final Clause mockClause2 = (Clause) mockClauseControl2.getMock();
        mockClause2.accept(validatorVisitor);
        mockClauseControl2.setReturnValue(null);
        mockClauseControl2.replay();

        final MockControl mockOrClauseControl = MockClassControl.createControl(OrClause.class);
        final OrClause mockOrClause = (OrClause) mockOrClauseControl.getMock();
        mockOrClause.getClauses();
        mockOrClauseControl.setReturnValue(EasyList.build(mockClause, mockClause2));
        mockOrClauseControl.replay();

        final MessageSet messageSet = validatorVisitor.visit(mockOrClause);

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);

        mockClauseControl.verify();
        mockClauseControl2.verify();
        mockOrClauseControl.verify();
    }

    @Test
    public void testVisitNotClauseHappyPath() throws Exception
    {
        ValidatorVisitor validatorVisitor = new ValidatorVisitor(null, null, null, null);

        final MockControl mockClauseControl = MockControl.createStrictControl(Clause.class);
        final Clause mockClause = (Clause) mockClauseControl.getMock();
        mockClause.accept(validatorVisitor);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseControl.setReturnValue(set1);
        mockClauseControl.replay();

        final MockControl mockNotClauseControl = MockClassControl.createStrictControl(NotClause.class);
        final NotClause mockNotClause = (NotClause) mockNotClauseControl.getMock();
        mockNotClause.getSubClause();
        mockNotClauseControl.setReturnValue(mockClause);
        mockNotClauseControl.replay();

        final MessageSet messageSet = validatorVisitor.visit(mockNotClause);

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet);

        mockClauseControl.verify();
        mockNotClauseControl.verify();
    }

    @Test
    public void testVisitTerminalClauseOperandHasError() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("Test");

        final MockControl mockTerminalClauseControl = MockClassControl.createControl(TerminalClause.class);
        final TerminalClause mockTerminalClause = (TerminalClause) mockTerminalClauseControl.getMock();
        mockTerminalClause.getOperand();
        mockTerminalClauseControl.setReturnValue(operand);
        mockTerminalClauseControl.replay();

        final MockControl mockClauseValidatorControl = MockControl.createStrictControl(ClauseValidator.class);
        final ClauseValidator mockClauseValidator = (ClauseValidator) mockClauseValidatorControl.getMock();

        final MockControl mockValidatorRegistryControl = MockControl.createStrictControl(ValidatorRegistry.class);
        final ValidatorRegistry mockValidatorRegistry = (ValidatorRegistry) mockValidatorRegistryControl.getMock();
        mockValidatorRegistry.getClauseValidator(null, mockTerminalClause);
        mockValidatorRegistryControl.setReturnValue(Collections.singletonList(mockClauseValidator));
        mockValidatorRegistryControl.replay();

        final MockControl mockOperandHandlerControl = MockControl.createStrictControl(OperandHandler.class);
        final OperandHandler mockOperandHandler = (OperandHandler) mockOperandHandlerControl.getMock();
        mockOperandHandler.validate(null, operand, mockTerminalClause);
        final MessageSetImpl set2 = new MessageSetImpl();
        set2.addErrorMessage("Operand Error 1");
        mockOperandHandlerControl.setReturnValue(set2);
        mockOperandHandlerControl.replay();

        JqlOperandResolver resolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, mockOperandHandler);

        final MockControl mockOperatorUsageValidatorControl = MockControl.createStrictControl(OperatorUsageValidator.class);
        final OperatorUsageValidator mockOperatorUsageValidator = (OperatorUsageValidator) mockOperatorUsageValidatorControl.getMock();
        mockOperatorUsageValidator.validate(null, mockTerminalClause);
        mockOperatorUsageValidatorControl.setReturnValue(new MessageSetImpl());
        mockOperatorUsageValidatorControl.replay();

        ValidatorVisitor validatorVisitor = new ValidatorVisitor(mockValidatorRegistry, resolver, mockOperatorUsageValidator, null);

        final MessageSet messageSet = validatorVisitor.visit(mockTerminalClause);

        assertMessageSetErrors(messageSet, "Operand Error 1");
        assertMessageSetWarnings(messageSet);

        mockOperatorUsageValidatorControl.verify();
        mockOperandHandlerControl.verify();
        mockTerminalClauseControl.verify();
        mockValidatorRegistryControl.verify();
    }

    @Test
    public void testVisitTerminalClauseOperatorValidationFailure() throws Exception
    {
        final MockControl mockTerminalClauseControl = MockClassControl.createControl(TerminalClause.class);
        final TerminalClause mockTerminalClause = (TerminalClause) mockTerminalClauseControl.getMock();
        mockTerminalClauseControl.replay();

        final MockControl mockClauseValidatorControl = MockControl.createStrictControl(ClauseValidator.class);
        final ClauseValidator mockClauseValidator = (ClauseValidator) mockClauseValidatorControl.getMock();

        final MockControl mockValidatorRegistryControl = MockControl.createStrictControl(ValidatorRegistry.class);
        final ValidatorRegistry mockValidatorRegistry = (ValidatorRegistry) mockValidatorRegistryControl.getMock();
        mockValidatorRegistry.getClauseValidator(null, mockTerminalClause);
        mockValidatorRegistryControl.setReturnValue(Collections.singletonList(mockClauseValidator));
        mockValidatorRegistryControl.replay();

        final MockControl mockOperatorUsageValidatorControl = MockControl.createStrictControl(OperatorUsageValidator.class);
        final OperatorUsageValidator mockOperatorUsageValidator = (OperatorUsageValidator) mockOperatorUsageValidatorControl.getMock();
        mockOperatorUsageValidator.validate(null, mockTerminalClause);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Bad Operator Error 1");
        mockOperatorUsageValidatorControl.setReturnValue(set1);
        mockOperatorUsageValidatorControl.replay();

        ValidatorVisitor validatorVisitor = new ValidatorVisitor(mockValidatorRegistry, MockJqlOperandResolver.createSimpleSupport(), mockOperatorUsageValidator, null);

        final MessageSet messageSet = validatorVisitor.visit(mockTerminalClause);

        assertMessageSetErrors(messageSet, "Bad Operator Error 1");
        assertMessageSetWarnings(messageSet);

        mockTerminalClauseControl.verify();
        mockValidatorRegistryControl.verify();
    }

    @Test
    public void testVisitTerminalClauseHappyPathWithOperatorWarning() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("Test");

        final MockControl mockTerminalClauseControl = MockClassControl.createControl(TerminalClause.class);
        final TerminalClause mockTerminalClause = (TerminalClause) mockTerminalClauseControl.getMock();
        mockTerminalClause.getOperand();
        mockTerminalClauseControl.setReturnValue(operand);
        mockTerminalClauseControl.replay();

        final MockControl mockClauseValidatorControl = MockControl.createStrictControl(ClauseValidator.class);
        final ClauseValidator mockClauseValidator = (ClauseValidator) mockClauseValidatorControl.getMock();
        mockClauseValidator.validate(null, mockTerminalClause);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseValidatorControl.setReturnValue(set1);
        mockClauseValidatorControl.replay();

        final MockControl mockValidatorRegistryControl = MockControl.createStrictControl(ValidatorRegistry.class);
        final ValidatorRegistry mockValidatorRegistry = (ValidatorRegistry) mockValidatorRegistryControl.getMock();
        mockValidatorRegistry.getClauseValidator(null, mockTerminalClause);
        mockValidatorRegistryControl.setReturnValue(Collections.singletonList(mockClauseValidator));
        mockValidatorRegistryControl.replay();

        final MockControl mockOperandHandlerControl = MockControl.createStrictControl(OperandHandler.class);
        final OperandHandler mockOperandHandler = (OperandHandler) mockOperandHandlerControl.getMock();
        mockOperandHandler.validate(null, operand, mockTerminalClause);
        final MessageSetImpl set2 = new MessageSetImpl();
        set2.addWarningMessage("Operand Warning 1");
        mockOperandHandlerControl.setReturnValue(set2);
        mockOperandHandlerControl.replay();

        final JqlOperandResolver resolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, mockOperandHandler);

        final MockControl mockOperatorUsageValidatorControl = MockControl.createStrictControl(OperatorUsageValidator.class);
        final OperatorUsageValidator mockOperatorUsageValidator = (OperatorUsageValidator) mockOperatorUsageValidatorControl.getMock();
        mockOperatorUsageValidator.validate(null, mockTerminalClause);
        final MessageSetImpl set3 = new MessageSetImpl();
        set3.addWarningMessage("Operator Warning 1");
        mockOperatorUsageValidatorControl.setReturnValue(set3);
        mockOperatorUsageValidatorControl.replay();

        ValidatorVisitor validatorVisitor = new ValidatorVisitor(mockValidatorRegistry, resolver, mockOperatorUsageValidator, null);

        final MessageSet messageSet = validatorVisitor.visit(mockTerminalClause);

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet, "Operator Warning 1", "Operand Warning 1");

        mockOperandHandlerControl.verify();
        mockTerminalClauseControl.verify();
        mockClauseValidatorControl.verify();
        mockValidatorRegistryControl.verify();
    }

    @Test
    public void testVisitTerminalClauseHappyPath() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("Test");

        final MockControl mockTerminalClauseControl = MockClassControl.createControl(TerminalClause.class);
        final TerminalClause mockTerminalClause = (TerminalClause) mockTerminalClauseControl.getMock();
        mockTerminalClause.getOperand();
        mockTerminalClauseControl.setReturnValue(operand);
        mockTerminalClauseControl.replay();

        final MockControl mockClauseValidatorControl = MockControl.createStrictControl(ClauseValidator.class);
        final ClauseValidator mockClauseValidator = (ClauseValidator) mockClauseValidatorControl.getMock();
        mockClauseValidator.validate(null, mockTerminalClause);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseValidatorControl.setReturnValue(set1);
        mockClauseValidatorControl.replay();

        final MockControl mockValidatorRegistryControl = MockControl.createStrictControl(ValidatorRegistry.class);
        final ValidatorRegistry mockValidatorRegistry = (ValidatorRegistry) mockValidatorRegistryControl.getMock();
        mockValidatorRegistry.getClauseValidator(null, mockTerminalClause);
        mockValidatorRegistryControl.setReturnValue(Collections.singletonList(mockClauseValidator));
        mockValidatorRegistryControl.replay();

        final MockControl mockOperandHandlerControl = MockControl.createStrictControl(OperandHandler.class);
        final OperandHandler mockOperandHandler = (OperandHandler) mockOperandHandlerControl.getMock();
        mockOperandHandler.validate(null, operand, mockTerminalClause);
        final MessageSetImpl set2 = new MessageSetImpl();
        set2.addWarningMessage("Operand Warning 1");
        mockOperandHandlerControl.setReturnValue(set2);
        mockOperandHandlerControl.replay();

        final JqlOperandResolver resolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, mockOperandHandler);

        final MockControl mockOperatorUsageValidatorControl = MockControl.createStrictControl(OperatorUsageValidator.class);
        final OperatorUsageValidator mockOperatorUsageValidator = (OperatorUsageValidator) mockOperatorUsageValidatorControl.getMock();
        mockOperatorUsageValidator.validate(null, mockTerminalClause);
        mockOperatorUsageValidatorControl.setReturnValue(new MessageSetImpl());
        mockOperatorUsageValidatorControl.replay();

        ValidatorVisitor validatorVisitor = new ValidatorVisitor(mockValidatorRegistry, resolver, mockOperatorUsageValidator, null);

        final MessageSet messageSet = validatorVisitor.visit(mockTerminalClause);

        assertMessageSetErrors(messageSet, "Error 1");
        assertMessageSetWarnings(messageSet, "Operand Warning 1");

        mockOperandHandlerControl.verify();
        mockTerminalClauseControl.verify();
        mockClauseValidatorControl.verify();
        mockValidatorRegistryControl.verify();
    }

    @Test
    public void testVisitTerminalClauseHappyPathWithMultipleValidators() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("Test");

        final MockControl mockTerminalClauseControl = MockClassControl.createControl(TerminalClause.class);
        final TerminalClause mockTerminalClause = (TerminalClause) mockTerminalClauseControl.getMock();
        mockTerminalClause.getOperand();
        mockTerminalClauseControl.setReturnValue(operand);
        mockTerminalClauseControl.replay();

        final MockControl mockClauseValidatorControl = MockControl.createStrictControl(ClauseValidator.class);
        final ClauseValidator mockClauseValidator = (ClauseValidator) mockClauseValidatorControl.getMock();
        mockClauseValidator.validate(null, mockTerminalClause);
        final MessageSetImpl set1 = new MessageSetImpl();
        set1.addErrorMessage("Error 1");
        mockClauseValidatorControl.setReturnValue(set1);
        mockClauseValidatorControl.replay();

        final MockControl mockClauseValidatorControl2 = MockControl.createStrictControl(ClauseValidator.class);
        final ClauseValidator mockClauseValidator2 = (ClauseValidator) mockClauseValidatorControl2.getMock();
        mockClauseValidator2.validate(null, mockTerminalClause);
        final MessageSetImpl set3 = new MessageSetImpl();
        set3.addErrorMessage("Error 2");
        mockClauseValidatorControl2.setReturnValue(set3);
        mockClauseValidatorControl2.replay();

        final MockControl mockValidatorRegistryControl = MockControl.createStrictControl(ValidatorRegistry.class);
        final ValidatorRegistry mockValidatorRegistry = (ValidatorRegistry) mockValidatorRegistryControl.getMock();
        mockValidatorRegistry.getClauseValidator(null, mockTerminalClause);
        mockValidatorRegistryControl.setReturnValue(CollectionBuilder.newBuilder(mockClauseValidator, mockClauseValidator2).asList());
        mockValidatorRegistryControl.replay();

        final MockControl mockOperandHandlerControl = MockControl.createStrictControl(OperandHandler.class);
        final OperandHandler mockOperandHandler = (OperandHandler) mockOperandHandlerControl.getMock();
        mockOperandHandler.validate(null, operand, mockTerminalClause);
        final MessageSetImpl set2 = new MessageSetImpl();
        set2.addWarningMessage("Operand Warning 1");
        mockOperandHandlerControl.setReturnValue(set2);
        mockOperandHandlerControl.replay();

        final JqlOperandResolver resolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, mockOperandHandler);

        final MockControl mockOperatorUsageValidatorControl = MockControl.createStrictControl(OperatorUsageValidator.class);
        final OperatorUsageValidator mockOperatorUsageValidator = (OperatorUsageValidator) mockOperatorUsageValidatorControl.getMock();
        mockOperatorUsageValidator.validate(null, mockTerminalClause);
        mockOperatorUsageValidatorControl.setReturnValue(new MessageSetImpl());
        mockOperatorUsageValidatorControl.replay();

        ValidatorVisitor validatorVisitor = new ValidatorVisitor(mockValidatorRegistry, resolver, mockOperatorUsageValidator, null);

        final MessageSet messageSet = validatorVisitor.visit(mockTerminalClause);

        assertMessageSetErrors(messageSet, "Error 1", "Error 2");
        assertMessageSetWarnings(messageSet, "Operand Warning 1");

        mockOperandHandlerControl.verify();
        mockTerminalClauseControl.verify();
        mockClauseValidatorControl.verify();
        mockClauseValidatorControl2.verify();
        mockValidatorRegistryControl.verify();
        mockValidatorRegistryControl.verify();
    }

    @Test
    public void testVisitTerminalClauseNoOperandHandler() throws Exception
    {
        final SingleValueOperand operand = new SingleValueOperand("Test");

        final TerminalClause mockTerminalClause = mockController.getMock(TerminalClause.class);
        mockTerminalClause.getOperand();
        mockController.setReturnValue(operand);

        final ClauseValidator mockClauseValidator = mockController.getMock(ClauseValidator.class);

        final ValidatorRegistry mockValidatorRegistry = mockController.getMock(ValidatorRegistry.class);
        mockValidatorRegistry.getClauseValidator(null, mockTerminalClause);
        mockController.setReturnValue(Collections.singletonList(mockClauseValidator));        

        final OperatorUsageValidator mockOperatorUsageValidator = mockController.getMock(OperatorUsageValidator.class);
        mockOperatorUsageValidator.validate(null, mockTerminalClause);
        mockController.setReturnValue(new MessageSetImpl());

        final JqlOperandResolver mockJqlOperandResolver = mockController.getMock(JqlOperandResolver.class);
        mockJqlOperandResolver.validate(null, operand, mockTerminalClause);
        final MessageSet errors = new MessageSetImpl();
        errors.addErrorMessage("Unable to handle 'SingleValueOperand'.");
        mockController.setReturnValue(errors);

        mockController.replay();

        ValidatorVisitor validatorVisitor = new ValidatorVisitor(mockValidatorRegistry, mockJqlOperandResolver, mockOperatorUsageValidator, null)
        {
            @Override
            I18nBean getI18n()
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messageSet = validatorVisitor.visit(mockTerminalClause);

        assertMessageSetErrors(messageSet, "Unable to handle 'SingleValueOperand'.");
        assertMessageSetWarnings(messageSet);

        mockController.verify();
    }

    @Test
    public void testVisitTerminalClauseNoValidator() throws Exception
    {
        final MockControl mockTerminalClauseControl = MockClassControl.createControl(TerminalClause.class);
        final TerminalClause mockTerminalClause = (TerminalClause) mockTerminalClauseControl.getMock();
        mockTerminalClause.getName();
        mockTerminalClauseControl.setReturnValue("nameDoesNotExist");
        mockTerminalClauseControl.replay();

        final MockControl mockValidatorRegistryControl = MockControl.createStrictControl(ValidatorRegistry.class);
        final ValidatorRegistry mockValidatorRegistry = (ValidatorRegistry) mockValidatorRegistryControl.getMock();
        mockValidatorRegistry.getClauseValidator(null, mockTerminalClause);
        mockValidatorRegistryControl.setReturnValue(Collections.emptyList());
        mockValidatorRegistryControl.replay();

        final MockControl mockOperandHandlerControl = MockControl.createStrictControl(OperandHandler.class);
        final OperandHandler mockOperandHandler = (OperandHandler) mockOperandHandlerControl.getMock();
        mockOperandHandlerControl.replay();

        final JqlOperandResolver resolver = new MockJqlOperandResolver().addHandler(SingleValueOperand.OPERAND_NAME, mockOperandHandler);

        final MockControl mockOperatorUsageValidatorControl = MockControl.createStrictControl(OperatorUsageValidator.class);
        final OperatorUsageValidator mockOperatorUsageValidator = (OperatorUsageValidator) mockOperatorUsageValidatorControl.getMock();
        mockOperatorUsageValidatorControl.replay();
        
        ValidatorVisitor validatorVisitor = new ValidatorVisitor(mockValidatorRegistry, resolver, mockOperatorUsageValidator, null)
        {
            @Override
            I18nBean getI18n()
            {
                return new MockI18nBean();
            }
        };

        final MessageSet messageSet = validatorVisitor.visit(mockTerminalClause);

        assertMessageSetErrors(messageSet, "Field 'nameDoesNotExist' does not exist or this field cannot be viewed by anonymous users.");

        mockOperandHandlerControl.verify();
        mockTerminalClauseControl.verify();
        mockValidatorRegistryControl.verify();
    }

    private void assertMessageSetErrors(MessageSet set, String...errors)
    {
        assertSet(set.getErrorMessages(), errors);
    }

    private void assertMessageSetWarnings(MessageSet set, String...warnings)
    {
        assertSet(set.getWarningMessages(), warnings);
    }

    private <T> void assertSet(Set<T> actualValues, T...expectedValues)
    {
        assertEquals(actualValues.size(), expectedValues.length);

        final Iterator<T> actualIter = actualValues.iterator();
        for (T expected : expectedValues)
        {
            assertEquals(expected, actualIter.next());
        }
    }
}
