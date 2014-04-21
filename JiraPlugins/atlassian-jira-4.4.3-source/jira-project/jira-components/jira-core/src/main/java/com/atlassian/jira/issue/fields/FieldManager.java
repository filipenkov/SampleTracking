/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.jql.context.QueryContext;
import org.ofbiz.core.entity.GenericValue;

import java.util.Set;

public interface FieldManager extends FieldAccessor
{
    //we should remove this!
    public static final String CUSTOM_FIELD_PREFIX = "customfield_";

    /**
     * Get a field by its id.
     * @param id An {@link com.atlassian.jira.issue.IssueFieldConstants} constant, or custom field key (eg. "customfield_10010")
     * @return the Field
     */
    public Field getField(String id);

    public boolean isCustomField(String id);

    public boolean isCustomField(Field field);

    /**
     * Get a CustomField by its text key (eg 'customfield_10000').
     * @param id Eg. 'customfield_10000'
     * @return The {@link CustomField} or null if not found.
     */
    public CustomField getCustomField(String id);

    public boolean isHideableField(String id);

    public boolean isHideableField(Field field);

    public HideableField getHideableField(String id);

    public boolean isOrderableField(String id);

    public boolean isOrderableField(Field field);

    public OrderableField getOrderableField(String id);

    public ConfigurableField getConfigurableField(String id);

    public Set<OrderableField> getOrderableFields();

    public boolean isNavigableField(String id);

    public boolean isNavigableField(Field field);

    public NavigableField getNavigableField(String id);

    public boolean isRequirableField(String id);

    public boolean isRequirableField(Field field);

    public boolean isMandatoryField(String id);

    public boolean isMandatoryField(Field field);

    public boolean isRenderableField(String id);

    public boolean isRenderableField(Field field);

    public boolean isUnscreenableField(String id);

    public boolean isUnscreenableField(Field field);

    public RequirableField getRequiredField(String id);

    /**
     * @return CustomFieldManager
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    public CustomFieldManager getCustomFieldManager();

    /**
     * @return FieldLayoutManager
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    public FieldLayoutManager getFieldLayoutManager();

    /**
     * @return ColumnLayoutManager
     * @deprecated Declare your dependency and let PicoContainer resolve it instead
     */
    @Deprecated
    public ColumnLayoutManager getColumnLayoutManager();

    public void refresh();

    public Set<Field> getUnavailableFields();

    boolean isFieldHidden(User remoteUser, Field field);

    /**
     * Determines whether the given Field is NOT hidden in AT LEAST one {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout} that the user can see
     * (assigned to projects for which the user has the {@link com.atlassian.jira.security.Permissions#BROWSE} permission).
     *
     * @param remoteUser the remote user.
     * @param field The Field
     *
     * @deprecated Use {@link #isFieldHidden(com.atlassian.crowd.embedded.api.User, String)} instead. Since v4.3
     */
    boolean isFieldHidden(com.opensymphony.user.User remoteUser, Field field);

    /**
     * Determines whether the field with id of fieldId is NOT hidden in AT LEAST one {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout} that the user can see
     * (assigned to projects for which the user has the {@link com.atlassian.jira.security.Permissions#BROWSE} permission).
     *
     * @param remoteUser the remote user.
     * @param fieldId The Field ID
     */
    boolean isFieldHidden(User remoteUser, String fieldId);

    /**
     * Determines whether the field with id of fieldId is NOT hidden in AT LEAST one {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout} that the user can see
     * (assigned to projects for which the user has the {@link com.atlassian.jira.security.Permissions#BROWSE} permission).
     *
     * @param remoteUser the remote user.
     * @param fieldId The Field ID
     *
     * @deprecated Use {@link #isFieldHidden(com.atlassian.crowd.embedded.api.User, String)} instead. Since v4.3
     */
    boolean isFieldHidden(com.opensymphony.user.User remoteUser, String fieldId);

    /**
     * Gets all the available fields that the user can see, this is providing no context scope.
     *
     * @param user the remote user.
     * @return a set of NavigableFields that can be show because their visibility/configuration fall within what the
     * user can see.
     *
     * @throws FieldException thrown if there is a problem looking up the fields
     */
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(User user) throws FieldException;

    /**
     * Gets all the available fields that the user can see, this is providing no context scope.
     *
     * @param user the remote user.
     * @return a set of NavigableFields that can be show because their visibility/configuration fall within what the
     * user can see.
     *
     * @throws FieldException thrown if there is a problem looking up the fields
     *
     * @deprecated Use {@link #getAvailableNavigableFieldsWithScope(com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(com.opensymphony.user.User user) throws FieldException;

    /**
     * Gets all the available fields within the defined scope of the QueryContext.
     *
     * @param user the user making the request
     * @param queryContext the context of the search request.
     * @return a set of NavigableFields that can be show because their visibility/configuration fall within the specified
     * context
     *
     * @throws FieldException thrown if there is a problem looking up the fields
     */
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(User user, QueryContext queryContext) throws FieldException;

    /**
     * Gets all the available fields within the defined scope of the QueryContext.
     *
     * @param user the user making the request
     * @param queryContext the context of the search request.
     * @return a set of NavigableFields that can be show because their visibility/configuration fall within the specified
     * context
     *
     * @throws FieldException thrown if there is a problem looking up the fields
     *
     * @deprecated Use {@link #getAvailableNavigableFieldsWithScope(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.jql.context.QueryContext)} instead. Since v4.3
     */
    public Set<NavigableField> getAvailableNavigableFieldsWithScope(com.opensymphony.user.User user, QueryContext queryContext) throws FieldException;

    /**
     * Retrieves custom fields in scope for the given issue
     *
     * @param remoteUser Remote User
     * @param issue Issue
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve
     *                        the field layouts for the viewable projects
     * @return custom fields in scope for the given issue
     */
    public Set<CustomField> getAvailableCustomFields(User remoteUser, Issue issue) throws FieldException;

    /**
     * Retrieves custom fields in scope for the given issue
     *
     * @param remoteUser Remote User
     * @param issue Issue
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve
     *                        the field layouts for the viewable projects
     * @return custom fields in scope for the given issue
     *
     * @deprecated Use {@link #getAvailableCustomFields(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue)} instead. Since v4.3
     */
    public Set<CustomField> getAvailableCustomFields(com.opensymphony.user.User remoteUser, Issue issue) throws FieldException;

    /**
     * Retrieves custom fields in scope for the given issue
     *
     * @param remoteUser Remote User
     * @param issue Issue
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve
     *                        the field layouts for the viewable projects
     * @return custom fields in scope for the given issue
     *
     * @deprecated - use {@link #getAvailableCustomFields(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue)} instead. Deprecated since v4.0.
     */
    public Set<CustomField> getAvailableCustomFields(com.opensymphony.user.User remoteUser, GenericValue issue) throws FieldException;

    public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException;

    public Set<NavigableField> getAvailableNavigableFields(User remoteUser) throws FieldException;

    /**
     * @deprecated Use {@link #getAvailableNavigableFields(com.atlassian.crowd.embedded.api.User)} instead. Since v4.3
     */
    public Set<NavigableField> getAvailableNavigableFields(com.opensymphony.user.User remoteUser) throws FieldException;

    /**
     * Return all the searchable fields in the system. This set will included all defined custom fields.
     *
     * @return the set of all searchable fields in the system.
     */
    Set<SearchableField> getAllSearchableFields();

    /**
     * Return all the searchable systems fields. This set will *NOT* include defined custom fields.
     *
     * @return the set of all searchable systems fields defined.
     */
    Set<SearchableField> getSystemSearchableFields();

    // --------------------------------------------------------------------------------------------- Convenience Methods

    /**
     * Retrieve the IssueType system Field.
     * @return the IssueType system Field.
     */
    public IssueTypeField getIssueTypeField();

    /**
     * Retrieve the IssueType system Field.
     * @return the IssueType system Field.
     *
     * @deprecated Use {@link #getIssueTypeField()}. Since v4.3
     */
    public IssueTypeSystemField getIssueTypeSystemField();

    /**
     * Retrieve the Project system Field.
     * @return the Project system Field.
     */
    public ProjectField getProjectField();

    /**
     * Retrieve the Project system Field.
     * @return the Project system Field.
     *
     * @deprecated Use {@link #getProjectField()}. Since v4.3
     */
    public ProjectSystemField getProjectSystemField();

    boolean isTimeTrackingOn();
}
