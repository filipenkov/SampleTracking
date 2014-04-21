/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * <b>NOTE:</b> This is referred to as Field Configuration in the UI.
 */
public interface FieldLayout
{
    public Long getId();

    public String getName();

    public String getDescription();

    public List<FieldLayoutItem> getFieldLayoutItems();

    public GenericValue getGenericValue();

    public FieldLayoutItem getFieldLayoutItem(OrderableField orderableField);

    public FieldLayoutItem getFieldLayoutItem(String fieldId);

    public List<FieldLayoutItem> getVisibleLayoutItems(User remoteUser, Project project, List<String> issueTypes);

    /**
     * Returns the list of Custom Fields in this Field Layout that are both visible and applicable to the given context (of project and Issue types).
     *
     * @param project The project context
     * @param issueTypes The Issue Types for context
     * @return the list of visible Custom Fields applicable to the given context (of project and Issue types).
     */
    public List<FieldLayoutItem> getVisibleCustomFieldLayoutItems(Project project, List<String> issueTypes);

    /**
     * Returns the list of Custom Fields in this Field Layout that are both visible and applicable to the given context (of project and Issue types).
     *
     * @param remoteUser Ignored
     * @param project The project context
     * @param issueTypes The Issue Types for context
     * @return the list of visible Custom Fields applicable to the given context (of project and Issue types).
     *
     * @deprecated use {@link #getVisibleCustomFieldLayoutItems(Project, List)}. Since v4.3
     */
    public List<FieldLayoutItem> getVisibleCustomFieldLayoutItems(User remoteUser, GenericValue project, List<String> issueTypes);

    public List<Field> getHiddenFields(Project project, List<String> issueTypeIds);

    /** @deprecated Use {@link #getHiddenFields(com.atlassian.jira.project.Project, java.util.List)}. Since v4.3 */
    public List<Field> getHiddenFields(User remoteUser, GenericValue project, List<String> issueTypeIds);

    /** @deprecated Use {@link #getHiddenFields(com.atlassian.jira.project.Project, java.util.List)}. Since v4.3 */
    public List<Field> getHiddenFields(User remoteUser, Project project, List<String> issueTypeIds);

    public List<FieldLayoutItem> getRequiredFieldLayoutItems(Project project, List<String> issueTypes);

    /** @deprecated Use {@link #getRequiredFieldLayoutItems(com.atlassian.jira.project.Project, java.util.List)}. Since v4.3 */
    public List<FieldLayoutItem> getRequiredFieldLayoutItems(User remoteUser, GenericValue project, List<String> issueTypes);

    public boolean isFieldHidden(String fieldId);

    public String getRendererTypeForField(String fieldId);

    /**
     * Returns true if this is the default FieldLayout.
     * This means that the "type" field holds "value".
     * @return true if this is the default FieldLayout.
     */
    boolean isDefault();
}
