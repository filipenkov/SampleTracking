package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public interface IssueTypeScreenSchemeManager
{
    String ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME = "IssueTypeScreenScheme";
    String ISSUE_TYPE_SCREEN_SCHEME_ENTITY_ENTITY_NAME = "IssueTypeScreenSchemeEntity";

    public Collection getIssueTypeScreenSchemes();

    public IssueTypeScreenScheme getIssueTypeScreenScheme(Long id);

    IssueTypeScreenScheme getIssueTypeScreenScheme(GenericValue project);

    FieldScreenScheme getFieldScreenScheme(Issue issue);

    Collection getIssueTypeScreenSchemeEntities(IssueTypeScreenScheme issueTypeScreenScheme);

    void createIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    void updateIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    void removeIssueTypeSchemeEntities(IssueTypeScreenScheme issueTypeScreenScheme);

    void removeIssueTypeScreenScheme(IssueTypeScreenScheme issueTypeScreenScheme);

    void createIssueTypeScreenSchemeEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    void updateIssueTypeScreenSchemeEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    void removeIssueTypeScreenSchemeEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    Collection getIssueTypeScreenSchemes(FieldScreenScheme fieldScreenScheme);

    public void addSchemeAssociation(GenericValue project, IssueTypeScreenScheme issueTypeScreenScheme);

    public void removeSchemeAssociation(GenericValue project, IssueTypeScreenScheme issueTypeScreenScheme);

    Collection getProjects(IssueTypeScreenScheme issueTypeScreenScheme);

    void associateWithDefaultScheme(GenericValue project);

    void addSchemeAssociation(GenericValue project, FieldScreenScheme fieldScreenScheme);

    Collection getProjects(FieldScreenScheme fieldScreenScheme);

    IssueTypeScreenScheme getDefaultScheme();

    void refresh();
}
