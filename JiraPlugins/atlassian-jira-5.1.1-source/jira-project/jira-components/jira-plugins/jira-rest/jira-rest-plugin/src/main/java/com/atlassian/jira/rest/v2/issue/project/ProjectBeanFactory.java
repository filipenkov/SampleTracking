package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.project.Project;

/**
 * Class for creating project from projects.
 *
 * @since v4.4
 */
public interface ProjectBeanFactory
{
    ProjectBean fullProject(Project project);
}
