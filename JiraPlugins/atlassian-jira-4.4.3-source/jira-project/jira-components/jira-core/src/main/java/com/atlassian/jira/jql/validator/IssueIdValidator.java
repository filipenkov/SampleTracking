package com.atlassian.jira.jql.validator;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlIssueKeySupport;
import com.atlassian.jira.jql.util.JqlIssueSupport;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.opensymphony.user.User;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Clause validator for the &quot;IssueKey&quot; clause.
 *
 * @since v4.0
 */
@InjectableComponent
public class IssueIdValidator implements ClauseValidator
{
    private final JqlOperandResolver operandResolver;
    private final SupportedOperatorsValidator supportedOperatorsValidator;
    private final JqlIssueKeySupport issueKeySupport;
    private final JqlIssueSupport issueSupport;
    private final I18nHelper.BeanFactory i18nFactory;

    IssueIdValidator(final JqlOperandResolver operandResolver, final JqlIssueKeySupport issueKeySupport, final JqlIssueSupport issueSupport, final I18nHelper.BeanFactory i18nFactory, final SupportedOperatorsValidator supportedOperatorsValidator)
    {
        this.issueSupport = issueSupport;
        this.issueKeySupport = notNull("issueKeySupport", issueKeySupport);
        this.i18nFactory = notNull("i18nFactory", i18nFactory);
        this.supportedOperatorsValidator = notNull("supportedOperatorsValidator", supportedOperatorsValidator);
        this.operandResolver = notNull("operandResolver", operandResolver);
    }

    public IssueIdValidator(final JqlOperandResolver operandResolver, final JqlIssueKeySupport issueKeySupport, final JqlIssueSupport issueSupport, final I18nHelper.BeanFactory i18nFactory)
    {
        this(operandResolver, issueKeySupport, issueSupport, i18nFactory, new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, OperatorClasses.RELATIONAL_ONLY_OPERATORS));
    }

    public MessageSet validate(final User searcher, final TerminalClause terminalClause)
    {
        notNull("terminalClause", terminalClause);

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
                }
            }
        }

        return messages;
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
