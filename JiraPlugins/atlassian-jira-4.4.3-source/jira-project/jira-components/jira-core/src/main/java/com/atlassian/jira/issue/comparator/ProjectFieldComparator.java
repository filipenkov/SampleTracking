package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.project.ProjectManager;
import org.ofbiz.core.entity.GenericValue;

import java.util.Comparator;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class ProjectFieldComparator implements Comparator
{
    Comparator projectComparator;

    public ProjectFieldComparator()
    {
        projectComparator = OfBizComparators.NAME_COMPARATOR;
    }

    public int compare(Object o1, Object o2)
    {
        if (!(o1 instanceof GenericValue) || !(o2 instanceof GenericValue))
        {
            throw new IllegalArgumentException("ProjectFieldComparator can only be used to compare two issues.  Instead got " + o1 + " " + o2);
        }

        GenericValue issue1 = (GenericValue) o1;
        GenericValue issue2 = (GenericValue) o2;

        GenericValue project1 = getProjectManager().getProject(issue1);
        GenericValue project2 = getProjectManager().getProject(issue2);

        return projectComparator.compare(project1, project2);
    }

    private ProjectManager getProjectManager()
    {
        return ManagerFactory.getProjectManager();
    }
}
