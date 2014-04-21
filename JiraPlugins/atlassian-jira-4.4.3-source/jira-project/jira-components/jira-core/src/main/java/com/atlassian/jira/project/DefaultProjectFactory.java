package com.atlassian.jira.project;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Null;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * Implementation that generates project objects.
 * Note that this class cannot use constructor injection because of cyclic dependencies.
 */
public class DefaultProjectFactory implements ProjectFactory
{
    final Function<GenericValue, Project> gvToProjectTransformer = new Function<GenericValue, Project>()
    {
        public Project get(final GenericValue input)
        {
            return getProject(input);
        }
    };

    public Project getProject(GenericValue projectGV)
    {
        if (projectGV == null)
        {
            return null;
        }
        return new ProjectImpl(projectGV);
    }

    public List<Project> getProjects(Collection<GenericValue> projectGVs)
    {
        Null.not("projectGVs", projectGVs);
        return CollectionUtil.transform(projectGVs, gvToProjectTransformer);
    }
}
