package com.atlassian.jira.jql.context;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.containsNoNulls;

/**
 * Performs set utilities on {@link com.atlassian.jira.jql.context.ClauseContext}'s
 *
 * @since v4.0
 */
public class ContextSetUtil
{
    private static final ContextSetUtil INSTANCE = new ContextSetUtil();

    public static ContextSetUtil getInstance()
    {
        return INSTANCE;
    }
    
    /**
     * Performs an itersection of the ClauseContext's passed in.
     *
     * NOTE: When {@link com.atlassian.jira.jql.context.ProjectIssueTypeContext}'s are compared they are considered
     * equivilent if the id values are the same, we do not compare if they are Explicit or Implicit. When combined
     * an Explicit flag will always replace an Implicit flag.
     *
     * @param childClauseContexts the child clause contexts to intersect, must never be null or contain null elements
     * @return the intersection of ClauseContext's that were passed in.
     */
    public ClauseContext intersect(final Set<? extends ClauseContext> childClauseContexts)
    {
        containsNoNulls("childClauseContexts", childClauseContexts);

        if (childClauseContexts.isEmpty())
        {
            return new ClauseContextImpl();
        }
        
        Iterator<? extends ClauseContext> iter = childClauseContexts.iterator();

        // Our initial result set is the first set of ProjectIssueTypeContext's in out childClauseContexts
        Set<ProjectIssueTypeContext> intersection = iter.next().getContexts();

        while(iter.hasNext())
        {
            // We are always comparing our current intersection results to the current ProjectIssueTypeContext in or childClauseContexts
            Set<ProjectIssueTypeContext> toIntersect = iter.next().getContexts();
            final Set<ProjectIssueTypeContext> intersectionResults = new HashSet<ProjectIssueTypeContext>();

            // We need to compare every element in the current ProjectIssueTypeContext against every element in the
            // intersection results to see if they are equivalent and make sure if they are that the higher order
            // type becomes the type of the new result.
            for (ProjectIssueTypeContext toIntersectContext : toIntersect)
            {
                for (ProjectIssueTypeContext intersectionContext : intersection)
                {
                    if (contextsEquiliaventForIntersection(toIntersectContext, intersectionContext))
                    {
                        intersectionResults.add(combineContexts(toIntersectContext, intersectionContext));
                    }
                }
            }

            // After generating the results we want our next iteration to compare against the already generated
            // results
            intersection = intersectionResults;
        }

        return new ClauseContextImpl(intersection);
    }

    /**
     * Performs a union of the ClauseContext's passed in.
     *
     * NOTE: When {@link com.atlassian.jira.jql.context.ProjectIssueTypeContext}'s are compared they are considered
     * equivilent if the id values are the same, we do not compare if they are Explicit or Implicit. When combined
     * an Explicit flag will always replace an Implicit flag.
     *
     * @param childClauseContexts the child clause contexts to union, must never be null or contain null elements
     * @return the union of the ClauseContext's that were passed in.
     */
    public ClauseContext union(final Set<? extends ClauseContext> childClauseContexts)
    {
        containsNoNulls("childClauseContexts", childClauseContexts);

        if (childClauseContexts.isEmpty())
        {
            return new ClauseContextImpl();
        }

        Iterator<? extends ClauseContext> iter = childClauseContexts.iterator();

        // Our initial result set is the first set of ProjectIssueTypeContext's in out childClauseContexts
        Set<ProjectIssueTypeContext> union = new HashSet<ProjectIssueTypeContext>(iter.next().getContexts());

        while(iter.hasNext())
        {
            Set<ProjectIssueTypeContext> toUnion = iter.next().getContexts();
            final Set<ProjectIssueTypeContext> unionResults = new HashSet<ProjectIssueTypeContext>();

            for (ProjectIssueTypeContext toUnionContext : toUnion)
            {
                boolean matched = false;
                for (Iterator<ProjectIssueTypeContext> iterator = union.iterator(); iterator.hasNext();)
                {
                    ProjectIssueTypeContext unionContext = iterator.next();
                    if (contextsEquiliaventForUnion(toUnionContext, unionContext))
                    {
                        matched = true;
                        iterator.remove();
                        unionResults.add(combineContexts(toUnionContext, unionContext));
                    }
                }
                if (!matched)
                {
                    unionResults.add(toUnionContext);
                }
            }

            union.addAll(unionResults);
        }

        return new ClauseContextImpl(union);
    }

    private boolean contextsEquiliaventForIntersection(ProjectIssueTypeContext one, ProjectIssueTypeContext two)
    {
        // If either the issue type contexts are all OR ids are the same
        if ((one.getIssueTypeContext().isAll() || two.getIssueTypeContext().isAll()) || one.getIssueTypeContext().getIssueTypeId().equals(two.getIssueTypeContext().getIssueTypeId()))
        {
            // If either the project contexts are all OR ids are the same
            if ((one.getProjectContext().isAll() || two.getProjectContext().isAll()) || one.getProjectContext().getProjectId().equals(two.getProjectContext().getProjectId()))
            {
                return true;
            }
        }
        return false;
    }

    private boolean contextsEquiliaventForUnion(ProjectIssueTypeContext one, ProjectIssueTypeContext two)
    {
        // If either the issue type contexts are all OR ids are the same
        return issueTypeValuesEqualForUnion(one.getIssueTypeContext(), two.getIssueTypeContext()) &&
               projectValuesEqualForUnion(one.getProjectContext(), two.getProjectContext());
    }

    private boolean projectValuesEqualForUnion(final ProjectContext one, final ProjectContext two)
    {
        if (one.isAll() && two.isAll())
        {
            return true;
        }
        else if (one.isAll() || two.isAll())
        {
            return false;
        }
        else
        {
            return one.getProjectId().equals(two.getProjectId());
        }
    }

    private boolean issueTypeValuesEqualForUnion(final IssueTypeContext one, final IssueTypeContext two)
    {
        if (one.isAll() && two.isAll())
        {
            return true;
        }
        else if (one.isAll() || two.isAll())
        {
            return false;
        }
        else
        {
            return one.getIssueTypeId().equals(two.getIssueTypeId());
        }
    }

    private ProjectIssueTypeContext combineContexts(ProjectIssueTypeContext one, ProjectIssueTypeContext two)
    {
        final IssueTypeContext issueTypeContext;
        if ((one.getIssueTypeContext().isAll() && two.getIssueTypeContext().isAll()))
        {
            issueTypeContext = AllIssueTypesContext.INSTANCE;
        }
        else if (one.getIssueTypeContext().isAll())
        {
            issueTypeContext = two.getIssueTypeContext();
        }
        else if (two.getIssueTypeContext().isAll())
        {
            issueTypeContext = one.getIssueTypeContext();
        }
        else
        {
            issueTypeContext = new IssueTypeContextImpl(one.getIssueTypeContext().getIssueTypeId());
        }

        final ProjectContext projectContext;
        if ((one.getProjectContext().isAll() && two.getProjectContext().isAll()))
        {
            projectContext = AllProjectsContext.INSTANCE;
        }
        else if (one.getProjectContext().isAll())
        {
            projectContext = two.getProjectContext();
        }
        else if (two.getProjectContext().isAll())
        {
            projectContext = one.getProjectContext();
        }
        else
        {
            projectContext = new ProjectContextImpl(one.getProjectContext().getProjectId());
        }

        return new ProjectIssueTypeContextImpl(projectContext, issueTypeContext);
    }
}
