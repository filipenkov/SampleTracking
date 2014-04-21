package com.atlassian.jira.issue.context.persistence;

import com.atlassian.bandana.BandanaPersister;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;

import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public interface FieldConfigContextPersister extends BandanaPersister
{
    List<JiraContextNode> getAllContextsForCustomField(String key);

    List<JiraContextNode> getAllContextsForConfigScheme(FieldConfigScheme fieldConfigScheme);

    void removeContextsForConfigScheme(Long fieldConfigSchemeId);

    void removeContextsForProject(GenericValue project);

    void removeContextsForProjectCategory(GenericValue projectCategory);
}
