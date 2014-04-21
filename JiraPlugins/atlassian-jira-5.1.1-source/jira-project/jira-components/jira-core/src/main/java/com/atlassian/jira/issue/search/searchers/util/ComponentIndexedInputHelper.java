package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.query.operand.SingleValueOperand;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Extension of {@link com.atlassian.jira.issue.search.searchers.util.DefaultIndexedInputHelper} that knows how to create
 * {@link com.atlassian.query.operand.SingleValueOperand}s by resolving ids to Component names.
 *
 * @since v4.0
 */
public class ComponentIndexedInputHelper extends DefaultIndexedInputHelper<ProjectComponent>
{
    private final NameResolver<ProjectComponent> componentResolver;

    public ComponentIndexedInputHelper(IndexInfoResolver<ProjectComponent> componentIndexInfoResolver, JqlOperandResolver operandResolver,
            final FieldFlagOperandRegistry fieldFlagOperandRegistry, final SearchContextVisibilityChecker searchContextVisibilityChecker,
            final NameResolver<ProjectComponent> componentResolver)
    {
        super(componentIndexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        this.componentResolver = notNull("componentResolver", componentResolver);
    }


    @Override
    protected SingleValueOperand createSingleValueOperandFromId(final String stringValue)
    {
        final long projectComponentId;
        try
        {
            projectComponentId = Long.parseLong(stringValue);
        }
        catch (NumberFormatException e)
        {
            return new SingleValueOperand(stringValue);
        }

        final ProjectComponent projectComponent = componentResolver.get(projectComponentId);

        if(projectComponent != null)
        {
            return new SingleValueOperand(projectComponent.getName());
        }
        return new SingleValueOperand(projectComponentId);
    }
}
