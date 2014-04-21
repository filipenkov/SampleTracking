package com.atlassian.jira.bc.customfield;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.bc.JiraServiceContext;

/**
 * Service front for the custom field manager.  Implementations of this interface are responsible for
 * carrying out any validation and permission logic required to carry out a certain task.  The actual
 * work required to do a certain task should be delegated to the {@link com.atlassian.jira.issue.CustomFieldManager}.
 *
 * @since v3.13
 */
@PublicApi
public interface CustomFieldService
{

    /**
     * Validates that the custom field with the provided id can be deleted.  This means we check whether
     * or not the custom field is used in any permission or issue level security schemes.  This method will also
     * check that the custom field with the given id exists. The user performing this operation needs to have
     * global admin permission.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who is performing the change and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param customFieldId      the custom field id of the custom field about to be deleted.
     */
    void validateDelete(JiraServiceContext jiraServiceContext, Long customFieldId);

    /**
     * Validates that the custom field with the provided id can be updated.  This means we check whether
     * or not the custom field is used in any permission or issue level security schemes if the custom field's
     * searcher is being set to null.  This method will also check that the custom field with the given id exists
     * and that all its attributes are valid. The user performing this operation needs to have global admin permission.
     *
     * @param jiraServiceContext containing the {@link com.atlassian.crowd.embedded.api.User} who is performing the change and
     *                           the {@link com.atlassian.jira.util.ErrorCollection} that will contain any errors
     *                           in calling the method
     * @param customFieldId      the custom field id of the customfield about to be updated
     * @param name the updated name of the customfield
     * @param description the description of the customfield
     * @param searcherKey the customfield searcher that should be used
     */
    void validateUpdate(JiraServiceContext jiraServiceContext, Long customFieldId, String name, String description, String searcherKey);
}
