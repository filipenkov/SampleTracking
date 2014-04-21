package com.atlassian.jira.pageobjects.project.fields;

import com.atlassian.jira.pageobjects.pages.admin.EditFieldConfigPage;

import java.util.List;

/**
 * Represents a Field Configuration on the Project Configuration Field tab
 *
 * @since v4.4
 */
public interface FieldConfiguration
{
    String getName();

    List<Field> getFields();

    boolean isDefault();

    List<IssueType> getIssueTypes();

    SharedProjectsDialog openSharedProjects();

    EditFieldConfigPage gotoEditFieldConfigPage();

    boolean hasSharedProjects();

    String getSharedProjectsText();

    boolean hasEditLink();

    public interface IssueType
    {
        String getName();

        String getIconSrc();
    }

}
