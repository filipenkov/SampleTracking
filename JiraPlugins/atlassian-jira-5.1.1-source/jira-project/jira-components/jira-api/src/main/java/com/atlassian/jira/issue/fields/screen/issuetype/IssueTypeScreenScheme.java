package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.annotations.PublicApi;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface IssueTypeScreenScheme
{
    Long DEFAULT_SCHEME_ID = new Long(1);

    Long getId();

    void setId(Long id);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    GenericValue getGenericValue();

    void setGenericValue(GenericValue genericValue);

    void store();

    void remove();

    Collection<IssueTypeScreenSchemeEntity> getEntities();

    IssueTypeScreenSchemeEntity getEntity(String issueTypeId);

    void addEntity(IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity);

    void removeEntity(String issueTypeId);

    boolean containsEntity(String issueTypeId);

    Collection<GenericValue> getProjects();

    boolean isDefault();
}
