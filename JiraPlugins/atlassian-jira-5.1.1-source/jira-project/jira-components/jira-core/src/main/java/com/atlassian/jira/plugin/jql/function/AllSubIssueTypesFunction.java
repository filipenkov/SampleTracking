package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Function that produces all the sub-task issue types.
 *
 * @since v4.0
 */
public class AllSubIssueTypesFunction extends AbstractIssueTypeFunction
{
    public static final String FUNCTION_SUB_ISSUE_TYPES = "subTaskIssueTypes";
    private final ConstantsManager constantsManager;

    public AllSubIssueTypesFunction(ConstantsManager constantsManager, SubTaskManager subTaskManager)
    {
        super(subTaskManager);
        this.constantsManager = notNull("constantsManager", constantsManager);
    }

    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        Collection<IssueType> subIssueTypes = constantsManager.getSubTaskIssueTypeObjects();
        List<QueryLiteral> literals = new ArrayList<QueryLiteral>();
        for (IssueType subIssueType : subIssueTypes)
        {
            literals.add(new QueryLiteral(operand, subIssueType.getId()));
        }
        return literals;
    }
}
