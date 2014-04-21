package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import org.apache.log4j.Logger;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Clause validator for the &quot;Issue Parent&quot; clause.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueParentValidator implements ClauseValidator
{
    private static final Logger log = Logger.getLogger(IssueParentValidator.class);

    private final JqlOperandResolver operandResolver;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlIssueKeySupport issueKeySupport;
    private final JqlIssueSupport issueSupport;
    private final I18nHelper.BeanFactory i18nFactory;
    private final SubTaskManager subTaskManager;

    IssueParentValidator(final JqlOperandResolver operandResolver, final JqlIssueKeySupport issueKeySupport, final JqlIssueSupport issueSupport, final I18nHelper.BeanFactory i18nFactory, final SupportedOperatorsValidator supportedOperatorsValidator, final SubTaskManager subTaskManager)
    {
        this.issueSupport = issueSupport;
        this.issueKeySupport = notNull("issueKeySupport", issueKeySupport);
        this.i18nFactory = notNull("i18nFactory", i18nFactory);
        this.supportedOperatorsValidator = notNull("supportedOperatorsValidator", supportedOperatorsValidator);
        this.operandResolver = notNull("operandResolver", operandResolver);
        this.subTaskManager = notNull("subTaskManager", subTaskManager);
    }

    public IssueParentValidator(final JqlOperandResolver operandResolver, final JqlIssueKeySupport issueKeySupport, final JqlIssueSupport issueSupport, final I18nHelper.BeanFactory i18nFactory, final SubTaskManager subTaskManager)
    {
        this(operandResolver, issueKeySupport, issueSupport, i18nFactory, new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS), subTaskManager);
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        notNull("terminalClause", terminalClause);

        if (!subTaskManager.isSubTasksEnabled())
        {
            MessageSet messageSet = new MessageSetImpl();
            final I18nHelper i18n = i18nFactory.getInstance(searcher);
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.issue.parent.subtasks.disabled", terminalClause.getName()));
            return messageSet;
        }

        MessageSet messages = supportedOperatorsValidator.validate(searcher, terminalClause);
        if (!messages.hasAnyErrors())
        {
            final Operand operand = terminalClause.getOperand();

            //Thus should not return null since the outside validation makes sure the operand is valid before
            //calling this method.
            final List<QueryLiteral> values = operandResolver.getValues(searcher, operand, terminalClause);
            if (values != null)
            {
                for (QueryLiteral value : values)
                {
                    if (value.getLongValue() != null)
                    {
                        validateIssueId(messages, value.getLongValue(), searcher, terminalClause, value.getSourceOperand());
                    }
                    else if (value.getStringValue() != null)
                    {
                        validateIssueKey(messages, value.getStringValue(), searcher, terminalClause, value.getSourceOperand());
                    }
                    else if (value.isEmpty())
                    {
                        validateEmptyOperand(messages, searcher, terminalClause, value.getSourceOperand());
                    }
                    else
                    {
                        log.error("Unknown QueryLiteral: " + value.toString());
                    }
                }
            }
        }

        return messages;
    }

    private MessageSet validateEmptyOperand(final MessageSet messageSet, final User searcher, final TerminalClause clause, final Operand operand)
    {
        final I18nHelper i18n = i18nFactory.getInstance(searcher);
        if (!operandResolver.isFunctionOperand(operand))
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.field.does.not.support.empty", clause.getName()));
        }
        else
        {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.field.does.not.support.empty.from.func", clause.getName(), operand.getName()));
        }
        return messageSet;
    }

    private boolean validateIssueId(final MessageSet messages, final Long issueId, final User searcher, final TerminalClause clause, final Operand operand)
    {
        final Issue issue = issueSupport.getIssue(issueId, searcher);
        if (issue == null)
        {
            final I18nHelper i18n = i18nFactory.getInstance(searcher);
            if (!operandResolver.isFunctionOperand(operand))
            {
                messages.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.id", clause.getName(), issueId.toString()));
            }
            else
            {
                messages.addErrorMessage(i18n.getText("jira.jql.clause.no.value.for.name.from.function", operand.getName(), clause.getName()));
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean validateIssueKey(final MessageSet messages, final String key, final User searcher, final TerminalClause clause, final Operand operand)
    {
        final List<Issue> issues = issueSupport.getIssues(key, searcher);
        if (issues.isEmpty())
        {
            final I18nHelper i18n = i18nFactory.getInstance(searcher);
            final boolean validIssueKey = issueKeySupport.isValidIssueKey(key);
            if (!operandResolver.isFunctionOperand(operand))
            {
                if (validIssueKey)
                {
                    messages.addErrorMessage(i18n.getText("jira.jql.clause.issuekey.noissue", key, clause.getName()));
                }
                else
                {
                    messages.addErrorMessage(i18n.getText("jira.jql.clause.issuekey.invalidissuekey", key, clause.getName()));
                }
            }
            else
            {
                if (validIssueKey)
                {
                    messages.addErrorMessage(i18n.getText("jira.jql.clause.issuekey.noissue.from.func", operand.getName(), clause.getName()));
                }
                else
                {
                    messages.addErrorMessage(i18n.getText("jira.jql.clause.issuekey.invalidissuekey.from.func", operand.getName(), clause.getName()));
                }
            }
            return false;
        }
        else
        {
            return true;
        }
    }
}
