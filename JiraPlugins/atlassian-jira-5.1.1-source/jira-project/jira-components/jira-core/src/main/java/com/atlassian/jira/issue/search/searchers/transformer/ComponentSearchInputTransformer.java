package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.ComponentIndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.query.clause.Clause;

import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A component-specific {@link com.atlassian.jira.issue.search.searchers.transformer.IdIndexedSearchInputTransformer}.
 *
 * @since v4.0
 */
public class ComponentSearchInputTransformer extends IdIndexedSearchInputTransformer<ProjectComponent>
{
    private final NameResolver<ProjectComponent> componentResolver;
    private final ClauseNames clauseNames;

    public ComponentSearchInputTransformer(ClauseNames clauseNames, String urlParameterName, IndexInfoResolver<ProjectComponent> componentIndexInfoResolver,
            JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry, final SearchContextVisibilityChecker searchContextVisibilityChecker,
            final NameResolver<ProjectComponent> componentResolver)
    {
        super(clauseNames, urlParameterName, componentIndexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        this.clauseNames = notNull("clauseNames", clauseNames);
        this.componentResolver = notNull("componentResolver", componentResolver);
    }

    @Override
    public Clause getSearchClause(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        Set<String> componentIdsFromHolder = getValuesFromHolder(fieldValuesHolder);
        if (componentIdsFromHolder != null && componentIdsFromHolder.size() > 0)
        {
            final IndexedInputHelper inputHelper;

            // if the components selected do not belong to the project selected, then we cannot reliably
            // use the name of the components for the clause, since it might match some components in another
            // project. hence, we will use the DefaultIndexInputHelper in that case to generate a clause
            // with ids for the operands
            if (isComponentsNotRelatedToProjects(componentIdsFromHolder, fieldValuesHolder))
            {
                inputHelper = getDefaultIndexedInputHelper();
            }
            else
            {
                inputHelper = getIndexedInputHelper();
            }
            return inputHelper.getClauseForNavigatorValues(clauseNames.getPrimaryName(), componentIdsFromHolder);
        }

        return null;
    }

    @Override
    IndexedInputHelper createIndexedInputHelper()
    {
        return new ComponentIndexedInputHelper(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, componentResolver);
    }

    /**
     * Check that the selected component will fit with the selected project.
     *
     * @param componentsIdsFromHolder component navigator values from the holder
     * @param fieldValuesHolder the general field values holder
     * @return true if at least one selected component does not match the projects specified
     */
    private boolean isComponentsNotRelatedToProjects(final Set<String> componentsIdsFromHolder, final FieldValuesHolder fieldValuesHolder)
    {
        final List<String> projects = (List<String>) fieldValuesHolder.get(SystemSearchConstants.forProject().getUrlParameter());
        if (projects == null || projects.isEmpty())
        {
            return false;
        }
        else if (projects.size() == 1 && projects.contains("-1"))
        {
            return false;
        }
        else if (projects.size() > 1)
        {
            return true;
        }

        for (String componentIdString : componentsIdsFromHolder)
        {
            final ProjectComponent component = getComponentFromNavigatorValue(componentIdString);
            if (component != null)
            {
                if (!projects.contains(component.getProjectId().toString()))
                {
                    // return as soon as we find one offending component
                    return true;
                }
            }
        }

        return false;
    }

    private ProjectComponent getComponentFromNavigatorValue(final String componentIdString)
    {
        try
        {
            final Long compId = new Long(componentIdString);
            return componentResolver.get(compId);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
