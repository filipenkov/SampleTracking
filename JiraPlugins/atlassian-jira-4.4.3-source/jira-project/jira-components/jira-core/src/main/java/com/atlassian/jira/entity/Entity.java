package com.atlassian.jira.entity;

import com.atlassian.jira.project.ProjectCategory;

/**
 * Holds Entity Factory classes.
 *
 * @since v4.4
 */
public interface Entity
{
    public static final EntityFactory<ProjectCategory> PROJECT_CATEGORY = new ProjectCategoryFactory();
}
