/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.IssueUtils;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.search.parameters.filter.NoBrowsePermissionPredicate;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.InvalidInputException;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityListIterator;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.entityengine.FindOptions.findOptions;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultIssueManager implements IssueManager
{
    private static final Logger log = Logger.getLogger(DefaultIssueManager.class);

    private final OfBizDelegator ofBizDelegator;
    private final WorkflowManager workflowManager;
    private final AssociationManager associationManager;
    private final UserAssociationStore userAssociationStore;
    private final IssueUpdater issueUpdater;
    private IssueDeleteHelper issueDeleteHelper;
    private FieldManager fieldManager;
    private FieldLayoutManager fieldLayoutManager;
    private final PermissionManager permissionManager;
    private final Map<String, Object> unassignedCondition = Collections.singletonMap("assignee", null);

    public DefaultIssueManager(OfBizDelegator ofBizDelegator, final WorkflowManager workflowManager, final AssociationManager associationManager,
            UserAssociationStore userAssociationStore, final IssueUpdater issueUpdater, final PermissionManager permissionManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.workflowManager = workflowManager;
        this.associationManager = associationManager;
        this.userAssociationStore = userAssociationStore;
        this.issueUpdater = issueUpdater;
        this.permissionManager = permissionManager;
    }

    // Get / Finder Methods --------------------------------------------------------------------------------------------
    public GenericValue getIssue(final Long id) throws DataAccessException
    {
        if (id == null)
        {
            return null; // JRA-17080
        }
        return ofBizDelegator.findById("Issue", id);
    }

    public GenericValue getIssue(final String key) throws GenericEntityException
    {
        if (key == null)
        {
            return null; // JRA-17080
        }
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("Issue", EasyMap.build("key", key)));
    }

    public List<GenericValue> getIssues(final Collection<Long> ids)
    {
        // Long SQL queries cause databases to blow up as they run out of resources
        // So retrieve issues in batches
        List<EntityExpr> entityExpressions = new ArrayList<EntityExpr>();
        List<GenericValue> unsortedIssues = null;
        final int batchSize = DefaultOfBizDelegator.getQueryBatchSize();
        int i = 0;
        for (final Long issueId : ids)
        {
            i++;
            entityExpressions.add(new EntityExpr("id", EntityOperator.EQUALS, issueId));

            if (i >= batchSize)
            {
                // Get the batch from the database
                if (unsortedIssues == null)
                {
                    // Save a call to addAll() if we can so that we do not iterate over the returned list unless we have to
                    unsortedIssues = ofBizDelegator.findByOr("Issue", entityExpressions, null);
                }
                else
                {
                    unsortedIssues.addAll(ofBizDelegator.findByOr("Issue", entityExpressions, null));
                }

                // Reset the query and the counter
                entityExpressions = new ArrayList<EntityExpr>();
                i = 0;
            }
        }

        // If we have some more issues to retrieve then do it
        if (!entityExpressions.isEmpty())
        {
            if (unsortedIssues == null)
            {
                // Save a call to addAll() if we can so that we do not iterate over the returned list unless we have to
                unsortedIssues = ofBizDelegator.findByOr("Issue", entityExpressions, null);
            }
            else
            {
                unsortedIssues.addAll(ofBizDelegator.findByOr("Issue", entityExpressions, null));
            }
        }

        return getIssuesSortedByIds(unsortedIssues, ids);
    }

    @Override
    public List<Issue> getIssueObjects(Collection<Long> ids)
    {
        return Lists.transform(getIssues(ids), new Function<GenericValue, Issue>()
        {
            @Override
            public Issue apply(@Nullable GenericValue from)
            {
                return getIssueFactory().getIssue(from);
            }
        });
    }

    /**
     * Retrieve a collection of all issue ids that belong to a given project.
     *
     * @param projectId the id of the project for which to retrieve all issue ids
     */
    public Collection<Long> getIssueIdsForProject(final Long projectId) throws GenericEntityException
    {
        if (projectId == null)
        {
            throw new NullPointerException("Project Id cannot be null.");
        }

        // JRA-6987 - do not retrieve all issues at once - use iterator to iterate over each issue id
        EntityListIterator issueIterator = null;

        final Collection<Long> issueIds = new ArrayList<Long>();

        try
        {
            issueIterator = ComponentAccessor.getComponent(DelegatorInterface.class).findListIteratorByCondition("Issue",
                    new EntityFieldMap(EasyMap.build("project", projectId), EntityOperator.AND), EasyList.build("id"), null);
            GenericValue issueIdGV = (GenericValue) issueIterator.next();
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            while (issueIdGV != null)
            {
                // record the issue id
                issueIds.add(issueIdGV.getLong("id"));
                // See if we have another issue
                issueIdGV = (GenericValue) issueIterator.next();
            }
        }
        finally
        {
            if (issueIterator != null)
            {
                issueIterator.close();
            }
        }

        return issueIds;
    }

    public long getIssueCountForProject(final Long projectId)
    {
        notNull("projectId", projectId);

        long count;
        final EntityCondition condition = new EntityFieldMap(EasyMap.build("project", projectId), EntityOperator.AND);
        final GenericValue countGV = EntityUtil.getOnly(ofBizDelegator.findByCondition("IssueCount", condition,
                Collections.singletonList("count"), Collections.<String>emptyList()));
        count = countGV.getLong("count");

        return count;
    }

    @Override
    public boolean hasUnassignedIssues()
    {
        OfBizListIterator unassignedIssuesIt = ofBizDelegator.findListIteratorByCondition("Issue", new EntityFieldMap(unassignedCondition, EntityOperator.EQUALS), null, ImmutableList.of("id"), null, findOptions().maxResults(1));
        try
        {
            return unassignedIssuesIt.next() != null;
        }
        finally
        {
            unassignedIssuesIt.close();
        }
    }

    @Override
    public long getUnassignedIssueCount()
    {
        List<GenericValue> unassignedCount = ofBizDelegator.findByCondition("IssueCountByAssignee", new EntityFieldMap(unassignedCondition, EntityOperator.EQUALS), ImmutableList.of("count"), null);
        return EntityUtil.getOnly(unassignedCount).getLong("count");
    }

    /**
     * Return the issues sorted in the order that the ids are in.
     * @param unsortedIssues unsorted list of Issue GVs
     * @param ids Ordered list of Issue ID's
     * @return The Issues in the same order
     */
    private List<GenericValue> getIssuesSortedByIds(final Collection<GenericValue> unsortedIssues, final Collection<Long> ids)
    {
        final Map<Long, GenericValue> idToIssue = new HashMap<Long, GenericValue>();
        for (final GenericValue issue : unsortedIssues)
        {
            idToIssue.put(issue.getLong("id"), issue);
        }
        final List<GenericValue> sortedIssues = new ArrayList<GenericValue>();
        for (final Long id : ids)
        {
            sortedIssues.add(idToIssue.get(id));
        }
        return sortedIssues;
    }

    public GenericValue getIssueByWorkflow(final Long wfid) throws GenericEntityException
    {
        return EntityUtil.getOnly(ofBizDelegator.findByAnd("Issue", EasyMap.build("workflowId", wfid)));
    }

    public MutableIssue getIssueObjectByWorkflow(Long workflowId) throws GenericEntityException
    {
        return getIssueObject(getIssueByWorkflow(workflowId));
    }

    public MutableIssue getIssueObject(final Long id) throws DataAccessException
    {
        final GenericValue issueGV = getIssue(id);
        // return null if the issue does not exist JRA-11464
        if (issueGV == null)
        {
            return null;
        }
        return getIssueObject(issueGV);
    }

    public MutableIssue getIssueObject(final String key) throws DataAccessException
    {
        try
        {
            final GenericValue issueGV = getIssue(key);
            // return null if the issue does not exist JRA-11464
            if (issueGV == null)
            {
                return null;
            }
            return getIssueObject(issueGV);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public List<GenericValue> getEntitiesByIssue(final String relationName, final GenericValue issue)
            throws GenericEntityException
    {
        if (relationName.equals(IssueRelationConstants.COMPONENT))
        {
            return associationManager.getSinkFromSource(issue, "Component", relationName, false);
        }
        else if (relationName.equals(IssueRelationConstants.FIX_VERSION))
        {
            return associationManager.getSinkFromSource(issue, "Version", relationName, false);
        }
        else if (relationName.equals(IssueRelationConstants.VERSION))
        {
            return associationManager.getSinkFromSource(issue, "Version", relationName, false);
        }
        else if (relationName.equals(IssueRelationConstants.CHANGE_GROUPS))
        {
            return issue.getRelatedCache("ChildChangeGroup");
        }
        else if (relationName.equals(IssueRelationConstants.WORKFLOW_HISTORY))
        {
            //noinspection unchecked
            return workflowManager.makeWorkflow((String) null).getHistorySteps(issue.getLong("workflowId"));
        }
        else if (relationName.equals(IssueRelationConstants.COMMENTS))
        {
            return issue.getRelatedByAnd("ChildAction", MapBuilder.build("type", ActionConstants.TYPE_COMMENT));
        }
        else if (relationName.equals(IssueRelationConstants.TYPE_WORKLOG))
        {
            return issue.getRelated("ChildWorklog");
        }
        else if (relationName.equals(IssueRelationConstants.LINKS_INWARD))
        {
            return ofBizDelegator.findByAnd("IssueLink", MapBuilder.build("destination", issue.getLong("id")));
        }
        else if (relationName.equals(IssueRelationConstants.LINKS_OUTWARD))
        {
            return ofBizDelegator.findByAnd("IssueLink", MapBuilder.build("source", issue.getLong("id")));
        }
        else if (relationName.equals(IssueRelationConstants.CUSTOM_FIELDS_VALUES))
        {
            return ofBizDelegator.findByAnd("CustomFieldValue", MapBuilder.build("issue", issue.getLong("id")));
        }
        return Collections.emptyList();
    }

    public List<GenericValue> getEntitiesByIssueObject(final String relationName, final Issue issue)
            throws GenericEntityException
    {
        return getEntitiesByIssue(relationName, issue.getGenericValue());

    }

    public List<GenericValue> getIssuesByEntity(final String relationName, final GenericValue entity)
            throws GenericEntityException
    {
        return associationManager.getSourceFromSink(entity, "Issue", relationName, false);
    }

    @Override
    public List<Issue> getIssueObjectsByEntity(String relationName, GenericValue entity)
            throws GenericEntityException
    {
        return Lists.transform(getIssuesByEntity(relationName, entity), new Function<GenericValue, Issue>()
        {
            @Override
            public Issue apply(@Nullable GenericValue from)
            {
                return getIssueFactory().getIssue(from);
            }
        });
    }

    /**
     * This function creates an issue in Jira.  Read the javadoc under the fields parameter to see what object need
     * to be passed to create an issue.
     *
     * @param remoteUser User that is creating this issue
     * @param fields     see below
     *                   <h4>Required Fields</h4>
     *                   projectId:    A Long value representing the id of the project<br>
     *                   issueType:    The String id of an issueType<br>
     *                   summary:      A String describing the issue (max 255 chars)<br>
     *                   <h4>Recomended Fields</h4>
     *                   assignee:     A String representing the username of the assignee<br>
     *                   reporter:     A String representing the username of the reporter<br>
     *                   priority:     The String id of a priority<br>
     *                   <h4>Optional Fields</h4>
     *                   description:  A String description of the issue<br>
     *                   environment:  A String description of the environment the issue is in. e.g W2K<br>
     *                   fixVersions:  A List of Long values representing fixVersion ids<br>
     *                   components:   A List of Long values representing component ids<br>
     *                   timeOriginalEstimate: A Long value representing the number of seconds this tast should take<br>
     *                   timeEstimate: A Long value representing the number of seconds allocated for this issue<br>
     *                   versions: =   A List of Long value representing version ids<br>
     *                   customFields: A Map with the CustomField as the key and Transport Object of the CF as the value <br>
     *                   created:      The date which the issue was created.  If not specified, defaults to {@link System#currentTimeMillis() }<br>
     *                   updated:      The date which the issue was updated.  If not specified, defaults to {@link System#currentTimeMillis() }<br>
     * @return A generic value representing the issue created
     * @throws CreateException
     * @see com.atlassian.jira.workflow.function.issue.IssueCreateFunction
     */
    @Override
    public GenericValue createIssue(final User remoteUser, final Map<String, Object> fields) throws CreateException
    {
        return createIssue(remoteUser != null ? remoteUser.getName() : null, fields);
    }

    @Override
    public GenericValue createIssue(final User remoteUser, final Issue issue) throws CreateException
    {
        try
        {
            final Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("issue", issue);
            // TODO: Why do we call ComponentAccessor.getIssueManager()? Are we trying to break out of DefaultIssueManager, and use CachingIssueManager?
            // If so, maybe the two issue managers should share a common abstract parent instead?
            final MutableIssue originalIssueGV = ComponentAccessor.getIssueManager().getIssueObject(issue.getId());
            fields.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssueGV);
            final GenericValue issueGV = workflowManager.createIssue(remoteUser != null ? remoteUser.getName() : null, fields);
            return issueGV;
        }
        catch (final WorkflowException workflowException)
        {

            final Throwable cause = workflowException.getCause();
            if (cause instanceof InvalidInputException)
            {
                throw new CreateException("Error occurred while creating issue through workflow: " + cause.getMessage(), (InvalidInputException) cause);
            }
            throw new CreateException(workflowException);
        }
    }

    @Override
    public List<GenericValue> getProjectIssues(final GenericValue project) throws GenericEntityException
    {
        return project.getRelated("ChildIssue");
    }

    public boolean isEditable(final Issue issue)
    {
        return workflowManager.isEditable(issue);
    }

    @Override
    public boolean isEditable(final Issue issue, final User user)
    {
        return isEditable(issue) && permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user);
    }

    /**
     * This method is here because this is a logical place for the "createIssue" method to be. As the issues are
     * actually created using workflow, the current implementation of this method uses the {@link WorkflowManager}
     * to create the issue
     *
     * @param remoteUserName the user who is creating the issue
     * @param fields issue's attribute's
     * @return the created issue
     * @see #createIssue(User, java.util.Map)
     */
    @Override
    public GenericValue createIssue(final String remoteUserName, final Map<String, Object> fields)
            throws CreateException
    {
        try
        {
            final GenericValue issue = workflowManager.createIssue(remoteUserName, fields);
            return issue;
        }
        catch (final WorkflowException e)
        {
            final Throwable cause = e.getCause();
            if ((cause != null) && (cause instanceof InvalidInputException))
            {
                throw new CreateException("Error occurred while creating issue through workflow: " + cause.getMessage(), (InvalidInputException) cause);
            }
            throw new CreateException(e.getMessage(), e);
        }
    }

    @Override
    public Issue createIssueObject(String remoteUserName, Map<String, Object> fields) throws CreateException
    {
        return getIssueFactory().getIssue(createIssue(remoteUserName, fields));
    }

    @Override
    public Issue createIssueObject(User remoteUser, Map<String, Object> fields) throws CreateException
    {
        return getIssueFactory().getIssue(createIssue(remoteUser, fields));
    }

    @Override
    public Issue createIssueObject(User remoteUser, Issue issue) throws CreateException
    {
        return getIssueFactory().getIssue(createIssue(remoteUser, issue));
    }

    @Override
    public List<Issue> getVotedIssues(final User user) throws GenericEntityException
    {
        final List<GenericValue> issueGVs = userAssociationStore.getSinksFromUser("VoteIssue", user, "Issue");
        IssueUtils.filterIssues(issueGVs, new NoBrowsePermissionPredicate(user));
        return getIssueObjectsFromGVs(issueGVs);
    }

    @Override
    public List<Issue> getVotedIssuesOverrideSecurity(final User user) throws GenericEntityException
    {
        final List<GenericValue> issueGVs = userAssociationStore.getSinksFromUser("VoteIssue", user, "Issue");
        return getIssueObjectsFromGVs(issueGVs);
    }

    private List<Issue> getIssueObjectsFromGVs(final List<GenericValue> issueGVs)
    {
        Collections.sort(issueGVs, IssueKeyComparator.COMPARATOR);
        final List<Issue> issues = new ArrayList<Issue>();
        for (final GenericValue issue : issueGVs)
        {
            issues.add(getIssueObject(issue));
        }
        return issues;
    }

    @Override
    public List<User> getWatchers(Issue issue)
    {
        return userAssociationStore.getUsersFromSink("WatchIssue", issue.getGenericValue());
    }

    @Override
    public List<Issue> getWatchedIssues(final User user)
    {
        final List<GenericValue> issueGVs = userAssociationStore.getSinksFromUser("WatchIssue", user, "Issue");
        IssueUtils.filterIssues(issueGVs, new NoBrowsePermissionPredicate(user));
        return getIssueObjectsFromGVs(issueGVs);
    }

    @Override
    public List<Issue> getWatchedIssuesOverrideSecurity(final User user)
    {
        final List<GenericValue> issueGVs = userAssociationStore.getSinksFromUser("WatchIssue", user, "Issue");
        return getIssueObjectsFromGVs(issueGVs);
    }


    @Override
    public Issue updateIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
    {
        // Get the original issue before we store the new data
        GenericValue originalIssueGV = issue.getGenericValue();

        StringBuffer modifiedText = new StringBuffer();

        // Generate all of our change items and give the fields a chance to store their changes if needed + build
        // up the modified text to analyze
        DefaultIssueChangeHolder issueChangeHolder = updateFieldValues(issue, modifiedText);

        // Reset the fields as they all have been persisted to the db.
        issue.resetModifiedFields();

        // Perform the update which will also fire the event and create the change group/items
        doUpdate(issue, originalIssueGV, eventDispatchOption, user, sendMail, issueChangeHolder);

        return issue;
    }

    protected void doUpdate(MutableIssue issue, GenericValue originalIssueGV, EventDispatchOption eventDispatchOption, User user, boolean sendMail,
            DefaultIssueChangeHolder issueChangeHolder)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue.getGenericValue(), originalIssueGV,
                eventDispatchOption.getEventTypeId(), user, sendMail, true);
        issueUpdateBean.setComment(issueChangeHolder.getComment());
        issueUpdateBean.setChangeItems(issueChangeHolder.getChangeItems());
        issueUpdateBean.setDispatchEvent(eventDispatchOption.isEventBeingSent());
        issueUpdateBean.setParams(MapBuilder.newBuilder("eventsource", IssueEventSource.ACTION).toMutableMap());
        issueUpdater.doUpdate(issueUpdateBean, false);
    }

    protected DefaultIssueChangeHolder updateFieldValues(MutableIssue issue, StringBuffer modifiedText)
    {
        DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
        Map<String, ModifiedValue> modifiedFields = issue.getModifiedFields();

        for (final String fieldId : modifiedFields.keySet())
        {
            if (getFieldManager().isOrderableField(fieldId))
            {
                OrderableField field = getFieldManager().getOrderableField(fieldId);
                FieldLayoutItem fieldLayoutItem = getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(field);
                final ModifiedValue modifiedValue = modifiedFields.get(fieldId);
                field.updateValue(fieldLayoutItem, issue, modifiedValue, issueChangeHolder);
                if (IssueFieldConstants.DESCRIPTION.equals(fieldId) || IssueFieldConstants.ENVIRONMENT.equals(fieldId))
                {
                    modifiedText.append(modifiedValue != null ? modifiedValue.getNewValue() : "").append(" ");
                }
            }
        }
        return issueChangeHolder;
    }

    @Override
    public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        getIssueDeleteHelper().deleteIssue(user, issue, eventDispatchOption, sendMail);
    }

    @Override
    public void deleteIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        getIssueDeleteHelper().deleteIssue(user, issue, eventDispatchOption, sendMail);
    }

    @Override
    public void deleteIssueNoEvent(Issue issue) throws RemoveException
    {
        getIssueDeleteHelper().deleteIssueNoEvent(issue);
    }

    @Override
    public void deleteIssueNoEvent(MutableIssue issue) throws RemoveException
    {
        getIssueDeleteHelper().deleteIssueNoEvent(issue);
    }

    /**
     * Creates a MutableIssue object from an Issue GenericValue.
     *
     * <p> If a null GenericValue is passed, then null is returned.
     *
     * @param issueGV the Issue GenericValue.
     * @return the MutableIssue Object (will be null if issueGV is null).
     */
    private MutableIssue getIssueObject(final GenericValue issueGV)
    {
        if (issueGV == null)
        {
            return null;
        }
        return getIssueFactory().getIssue(issueGV);
    }

    private IssueFactory getIssueFactory()
    {
        // We can't have IssueFactory injected as we would get a circular dependency.
        return ComponentAccessor.getIssueFactory();
    }

    FieldLayoutManager getFieldLayoutManager()
    {
        if (fieldLayoutManager == null)
        {
            fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        }
        return fieldLayoutManager;
    }

    FieldManager getFieldManager()
    {
        if (fieldManager == null)
        {
            fieldManager = ComponentManager.getInstance().getFieldManager();
        }
        return fieldManager;
    }

    IssueDeleteHelper getIssueDeleteHelper()
    {
        if (issueDeleteHelper == null)
        {
            issueDeleteHelper = ComponentManager.getComponentInstanceOfType(IssueDeleteHelper.class);
        }
        return issueDeleteHelper;
    }
}
