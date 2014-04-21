package com.atlassian.jira.issue.context.persistence;

import com.atlassian.bandana.BandanaPersister;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public interface FieldConfigContextPersister extends BandanaPersister
{
    List<JiraContextNode> getAllContextsForCustomField(String key);

    List<JiraContextNode> getAllContextsForConfigScheme(FieldConfigScheme fieldConfigScheme);

    void removeContextsForConfigScheme(Long fieldConfigSchemeId);

    /**
     * @deprecated Use {@link #removeContextsForProject(com.atlassian.jira.project.Project)} instead. Since v5.1.
     * @param project the project
     */
    void removeContextsForProject(GenericValue project);

    void removeContextsForProject(Project project);

    void removeContextsForProjectCategory(ProjectCategory projectCategory);
}
