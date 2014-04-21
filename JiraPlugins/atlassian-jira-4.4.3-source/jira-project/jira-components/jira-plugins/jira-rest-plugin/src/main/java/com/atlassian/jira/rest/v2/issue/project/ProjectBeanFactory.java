package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.project.Project;

import java.net.URI;

/**
 * Class for creating project from projects.
 *
 * @since v4.4
 */
public interface ProjectBeanFactory
{
    ProjectBean shortProject(String key, String name, URI selfUri);
    ProjectBean shortProject(Project project);
    ProjectBean fullProject(Project project);
}
