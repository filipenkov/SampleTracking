package com.atlassian.jira.project;

import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @since v3.13
 */
public class MockProjectFactory implements ProjectFactory
{
    public MockProjectFactory()
    {
    }

    public Project getProject(final GenericValue projectGV)
    {
        return new ProjectImpl(projectGV);
    }

    public List<Project> getProjects(Collection projectGVs)
    {
        List<Project> projects = new ArrayList<Project>();
        for (Iterator iterator = projectGVs.iterator(); iterator.hasNext();)
        {
            GenericValue projectGV = (GenericValue) iterator.next();
            projects.add(getProject(projectGV));
        }
        return projects;
    }
}
