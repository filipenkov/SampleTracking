/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A class to manage interactions with issues
 */
public interface IssueManager extends JiraManager
{
    /**
     * Retrieves an issue by its id.
     *
     * @param id Issue ID.
     * @return An issue {@link GenericValue}.
     * @throws DataAccessException if there is an error in the Data Access Layer.
     * @deprecated Use {@link #getIssueObject(Long)} instead.
     */
    @Deprecated
    GenericValue getIssue(Long id) throws DataAccessException;

    /**
     * Retrieves the issue as a {@link GenericValue} with the given key.
     *
     * @param key the issue key.
     * @return the issue as a {@link GenericValue}.
     * @throws GenericEntityException if there is a problem getting the data
     * @deprecated Use {@link #getIssueObject(String)} instead.
     */
    @Deprecated
    GenericValue getIssue(String key) throws GenericEntityException;

    /**
     * Retrieves an issue {@link GenericValue} given a workflow ID.  This is used when transitioning
     * through the various steps of a workflow.
     *
     * @param wfid workflow ID.
     * @return An issue {@link GenericValue}.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     * @see #getIssueObjectByWorkflow(Long)
     */
    GenericValue getIssueByWorkflow(Long wfid) throws GenericEntityException;

    /**
     * Retrieves an issue given a workflow ID.
     * This is used when transitioning through the various steps of a workflow.
     *
     * @param workflowId workflow ID.
     * @return The Issue
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    MutableIssue getIssueObjectByWorkflow(Long workflowId) throws GenericEntityException;

    /**
     * Retrieves an issue by id. This method will always return a new instance of an issue.
     *
     * @param id the id
     * @return A {@link MutableIssue}
     * @throws DataAccessException if there is an error in the Data Access Layer.
     */
    public MutableIssue getIssueObject(Long id) throws DataAccessException;

    /**
     * Retrieves the {@link Issue} that has the given key, or null if no such Issue exists.
     * <p>
     * This method will always return a new instance of an issue object if the issue exists.
     * </p>
     *
     * @param key The Issue key.
     * @return a {@link MutableIssue} with the given key, or null if no such Issue exists.
     * @throws DataAccessException if there is an error in the Data Access Layer.
     */
    MutableIssue getIssueObject(String key) throws DataAccessException;

    /**
     * Get issues with the following ids.  The issues are sorted in the order that
     * the ids were given in.
     *
     * @param ids Issue IDs.
     * @return A collection of issue {@link GenericValue}s
     */
    List<GenericValue> getIssues(Collection<Long> ids);

    /**
     * Get a list of issues that the user has voted on and can see.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    List<Issue> getVotedIssues(com.opensymphony.user.User user) throws GenericEntityException;

    /**
     * Get a list of issues that the user has voted on and can see.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    List<Issue> getVotedIssues(User user) throws GenericEntityException;

    /**
     * Get a list of issues that the user has voted on.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     * @since v4.0
     */
    List<Issue> getVotedIssuesOverrideSecurity(com.opensymphony.user.User user) throws GenericEntityException;

    /**
     * Get a list of issues that the user has voted on.
     *
     * @param user The user.
     * @return A list of {@link Issue} objects the user has voted on.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     * @since v4.0
     */
    List<Issue> getVotedIssuesOverrideSecurity(User user) throws GenericEntityException;

    /**
     * Return a list of watchers for a particular issue.
     *
     * @param issue the Issue.
     * @return A list of {@link com.opensymphony.user.User}s.
     * @deprecated Use {@link #getIssueWatchers(Issue)} instead
     */
    @Deprecated
    List<com.opensymphony.user.User> getIssueWatchers(GenericValue issue);

    /**
     * Return a list of watchers for a particular issue.
     *
     * @param issue the Issue
     * @return A list of {@link com.opensymphony.user.User}s.
     * @deprecated Use {@link #getWatchers(com.atlassian.jira.issue.Issue)} instead
     */
    @Deprecated
    List<com.opensymphony.user.User> getIssueWatchers(Issue issue);

    /**
     * Return a list of watchers for a particular issue.
     *
     * @param issue the Issue
     * @return A list of {@link User}s.
     */
    List<User> getWatchers(Issue issue);

    /**
     * Get a list of issues that the user is watching and can see.
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     */
    List<Issue> getWatchedIssues(com.opensymphony.user.User user);

    /**
     * Get a list of issues that the user is watching and can see.
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     */
    List<Issue> getWatchedIssues(User user);

    /**
     * Get a list of issues that the user is watching
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     * @since v4.0
     */
    List<Issue> getWatchedIssuesOverrideSecurity(com.opensymphony.user.User user);

    /**
     * Get a list of issues that the user is watching
     *
     * @param user the User.
     * @return A list of {@link Issue} objects
     * @since v4.0
     */
    List<Issue> getWatchedIssuesOverrideSecurity(User user);

    /**
     * Get a list of entities (versions, components etc) related to this issue.
     *
     * @param relationName A {@link IssueRelationConstants} string indicating some issue relation
     * @param issue        Issue to consider
     * @return A list of entity {@link GenericValue}s associated with the issue.
     * @deprecated Use {@link #getEntitiesByIssueObject(String, Issue)} instead.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    @Deprecated
    List<GenericValue> getEntitiesByIssue(String relationName, GenericValue issue) throws GenericEntityException;

    /**
     * Get a list of entities (versions, components etc) related to this issue.
     *
     * @param relationName A {@link IssueRelationConstants} string indicating some issue relation
     * @param issue        Issue to consider
     * @return A list of entity {@link GenericValue}s associated with the issue.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    List<GenericValue> getEntitiesByIssueObject(String relationName, Issue issue) throws GenericEntityException;

    /**
     * Get a list of issues related to an entity (version, component etc).
     *
     * @param relationName A {@link IssueRelationConstants} string indicating some relation of entity
     * @param entity       The entity related to the issues we're after
     * @return A list of issue {@link GenericValue}s associated with an entity.
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    List<GenericValue> getIssuesByEntity(String relationName, GenericValue entity) throws GenericEntityException;

    /**
     * Creates an issue.
     *
     * @param remoteUserName Issue creator
     * @param fields         A map of the issue's immediate field values. See the Issue definition in entitymodel.xml for values.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    GenericValue createIssue(String remoteUserName, Map<String, Object> fields) throws CreateException;

    /**
     * Creates an issue.
     *
     * @param remoteUser Issue creator
     * @param fields     A map of the issue's immediate field values. See the Issue definition in entitymodel.xml for values.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    GenericValue createIssue(com.opensymphony.user.User remoteUser, Map<String, Object> fields) throws CreateException;

    /**
     * Creates an issue.
     *
     * @param remoteUser Issue creator
     * @param fields     A map of the issue's immediate field values. See the Issue definition in entitymodel.xml for values.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    GenericValue createIssue(User remoteUser, Map<String, Object> fields) throws CreateException;

    /**
     * Persists a new issue.
     *
     * @param remoteUser Issue creator
     * @param issue      The new issue.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    GenericValue createIssue(com.opensymphony.user.User remoteUser, Issue issue) throws CreateException;

    /**
     * Persists a new issue.
     *
     * @param remoteUser Issue creator
     * @param issue      The new issue.
     * @return GenericValue representing the new issue.
     * @throws CreateException If JIRA is unable to create the issue.
     */
    GenericValue createIssue(User remoteUser, Issue issue) throws CreateException;

    /**
     * This method will store the provided issue to the JIRA datastore. The issue will be saved and re-indexed.
     * This method performs no permission checks.
     * <p/>
     *
     * This method should be used if you want to exert more control over what happens when JIRA updates an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the update.
     *
     * @param user who is performing the operation
     * @param issue the issue to update
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @return the updated issue.
     * @throws com.atlassian.jira.exception.UpdateException thrown if something goes horribly wrong when updating the issue.
     * @since v4.0
     */
    public Issue updateIssue(com.opensymphony.user.User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws UpdateException;

    /**
     * This method will store the provided issue to the JIRA datastore. The issue will be saved and re-indexed.
     * This method performs no permission checks.
     * <p/>
     *
     * This method should be used if you want to exert more control over what happens when JIRA updates an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the update.
     *
     * @param user who is performing the operation
     * @param issue the issue to update
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @return the updated issue.
     * @throws com.atlassian.jira.exception.UpdateException thrown if something goes horribly wrong when updating the issue.
     * @since v4.0
     */
    public Issue updateIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws UpdateException;

    /**
     * This method will delete an issue from JIRA. This will clean up all issue associations in JIRA and will de-index
     * the issue.
     * <p/>
     *
     * This method should be used if you want to exert more control over what happens when JIRA deletes an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the deletion.
     *
     * @param user who is performing the operation
     * @param issue the issue to delete.
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @throws com.atlassian.jira.exception.RemoveException throw if something goes horribly wrong when deleting the issue.
     * @since v4.0
     */
    public void deleteIssue(com.opensymphony.user.User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException;

    /**
     * This method will delete an issue from JIRA. This will clean up all issue associations in JIRA and will de-index
     * the issue.
     * <p/>
     *
     * This method should be used if you want to exert more control over what happens when JIRA deletes an issue. This
     * method will allow you to specify if an event is dispatched and if so which event is dispatched, see
     * {@link com.atlassian.jira.event.type.EventDispatchOption}. This method also allows you to specify if email
     * notifications should be send to notify users of the deletion.
     *
     * @param user who is performing the operation
     * @param issue the issue to delete.
     * @param eventDispatchOption specifies if an event should be sent and if so which should be sent.
     * @param sendMail if true mail notifications will be sent, otherwise mail notifications will be suppressed.
     * @throws com.atlassian.jira.exception.RemoveException throw if something goes horribly wrong when deleting the issue.
     * @since v4.0
     */
    public void deleteIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException;
    
    /**
     * Get all the issues for a given project.
     *
     * @param project the Project
     * @return a List of {@link GenericValue} objects
     * @deprecated only used in test cases, should use a search provider instead.
     * @throws GenericEntityException If there are errors in the persistence layer.
     */
    @Deprecated
    List<GenericValue> getProjectIssues(GenericValue project) throws GenericEntityException;

    /**
     * Returns <code>true</code> if the issue can be edited. This is determined by looking at the workflow step the issue is in.
     * @param issue the Issue.
     * @return <code>true</code> if the issue can be edited. This is determined by looking at the workflow step the issue is in.
     */
    boolean isEditable(Issue issue);

    /**
     * Returns <code>true</code> if the issue can be edited by the current user. This is determined by looking at both the
     * user's permissions and the workflow step the issue is in.
     * @param issue the issue you want to edit
     * @param user the user who will be performing the edit
     * @return <code>true</code> if the user has permission and the issue is in an editable workflow step
     */
    boolean isEditable(Issue issue, com.opensymphony.user.User user);

    /**
     * Returns <code>true</code> if the issue can be edited by the current user. This is determined by looking at both the
     * user's permissions and the workflow step the issue is in.
     * @param issue the issue you want to edit
     * @param user the user who will be performing the edit
     * @return <code>true</code> if the user has permission and the issue is in an editable workflow step
     */
    boolean isEditable(Issue issue, User user);

    /**
     * Returns all issue ids for a particular project. This uses constant memory (i.e. doesn't load
     * all issues into memory.
     *
     * @param projectId Project ID.
     * @return A collection of issue IDs
     * @throws GenericEntityException An exception in the ofbiz Entity Engine.
     */
    Collection<Long> getIssueIdsForProject(Long projectId) throws GenericEntityException;

    /**
     * Returns the number of issues that exist for the provided project id.
     *
     * @param projectId identifies the project which the issues are associated with
     * @return a count of how many issues exist in the project
     */
    long getIssueCountForProject(Long projectId);
}
