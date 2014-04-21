package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.context.QueryContext;

import java.util.Set;

/**
 * Similar to the FieldManager, but without exposing implementation classes.
 *
 * @since v4.3
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public interface FieldAccessor
{

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

    public Set<Field> getUnavailableFields();

    boolean isFieldHidden(User remoteUser, Field field);

    /**
     * Determines whether the field with id of fieldId is NOT hidden in AT LEAST one {@link com.atlassian.jira.issue.fields.layout.field.FieldLayout} that the user can see
     * (assigned to projects for which the user has the {@link com.atlassian.jira.security.Permissions#BROWSE} permission).
     *
     * @param remoteUser the remote user.
     * @param fieldId The Field ID
     *
     * @return true if this field is hidden from the given user
     */
    boolean isFieldHidden(User remoteUser, String fieldId);

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
     * Retrieves custom fields in scope for the given issue
     *
     * @param remoteUser Remote User
     * @param issue Issue
     * @throws FieldException if cannot retrieve the projects the user can see, or if cannot retrieve
     *                        the field layouts for the viewable projects
     * @return custom fields in scope for the given issue
     */
    public Set<CustomField> getAvailableCustomFields(User remoteUser, Issue issue) throws FieldException;

    public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException;

    public Set<NavigableField> getAvailableNavigableFields(User remoteUser) throws FieldException;

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
     * Retrieve the Project system Field.
     * @return the Project system Field.
     */
    public ProjectField getProjectField();

    boolean isTimeTrackingOn();
}
