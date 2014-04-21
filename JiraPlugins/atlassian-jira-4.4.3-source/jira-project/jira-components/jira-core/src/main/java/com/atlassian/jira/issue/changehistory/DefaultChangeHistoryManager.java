package com.atlassian.jira.issue.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultChangeHistoryManager implements ChangeHistoryManager
{
    private static final Logger log = Logger.getLogger(DefaultChangeHistoryManager.class);

    private final IssueManager issueManager;
    private final OfBizDelegator ofBizDelegator;
    private final PermissionManager permissionManager;
    private final ComponentLocator componentLocator;

    private static final String FIELD_KEY = "Key";
    private static final String ISSUEID_FIELD = "issueid";
    private static final List<String> FIELDS_TO_SELECT = ImmutableList.of(ISSUEID_FIELD);

    public DefaultChangeHistoryManager(final IssueManager issueManager, final OfBizDelegator ofBizDelegator,
            final PermissionManager permissionManager, final ComponentLocator componentLocator)
    {
        this.issueManager = issueManager;
        this.ofBizDelegator = ofBizDelegator;
        this.permissionManager = permissionManager;
        this.componentLocator = componentLocator;
    }

    public List<ChangeHistory> getChangeHistories(final Issue issue)
    {
        return getAllChangeHistories(issue);
    }

    public List<ChangeHistory> getChangeHistoriesForUser(final Issue issue, final User remoteUser)
    {
        return getAllChangeHistories(issue);
    }

    private List<ChangeHistory> getAllChangeHistories(final Issue issue)
    {
        Assertions.notNull("issue", issue);

        if (issue.getId() == null) { return Collections.emptyList(); }

        final List<GenericValue> allChangeGroups = ofBizDelegator.findByAnd("ChangeGroup", ImmutableMap.of("issue", issue.getId()), ImmutableList.of("created ASC", "id ASC"));

        return Lists.transform(allChangeGroups, new Function<GenericValue, ChangeHistory>()
        {
            @Override
            public ChangeHistory apply(@Nullable GenericValue changeHistoryGroupAsGv)
            {
                return new ChangeHistory(changeHistoryGroupAsGv, issueManager);
            }
        });
    }

    public List<ChangeHistory> getChangeHistoriesForUser(final Issue issue, final com.opensymphony.user.User remoteUser)
    {
        return getChangeHistoriesForUser(issue, (User) remoteUser);
    }

    public List<ChangeItemBean> getChangeItemsForField(final Issue issue, final String changeItemFieldName)
    {
        Assertions.notNull("issue", issue);
        Assertions.notBlank("changeItemFieldName", changeItemFieldName);

        if (issue.getId() == null) { return Collections.emptyList(); }

        final List<GenericValue> changeItemsForFieldGVs = ofBizDelegator.findByAnd("ChangeGroupChangeItemView", ImmutableMap.of("issue", issue.getId(), "field", changeItemFieldName), ImmutableList.of("created ASC", "changeitemid ASC"));
        final List<ChangeItemBean> changeItemsForField = new ArrayList<ChangeItemBean>();
        for (final GenericValue changeItemGV : changeItemsForFieldGVs)
        {
            changeItemsForField.add(new ChangeItemBean(changeItemGV.getString("fieldtype"),
                    changeItemGV.getString("field"), changeItemGV.getString("oldvalue"),
                    changeItemGV.getString("oldstring"), changeItemGV.getString("newvalue"),
                    changeItemGV.getString("newstring"), changeItemGV.getTimestamp("created")));
        }

        return changeItemsForField;
    }

    @Override
    public List<ChangeHistoryItem> getAllChangeItems(final Issue issue)
    {
        Assertions.notNull("issue", issue);

        if (issue.getId() == null) { return Collections.emptyList(); }

        final List<GenericValue> changeItemsGVs = ofBizDelegator.findByAnd("ChangeGroupChangeItemView", ImmutableMap.of("issue", issue.getId()), ImmutableList.of("created ASC", "changeitemid ASC"));
        final Map<Long, Map<String, ChangeHistoryItem.Builder>>  fieldsPerChangeGroup = Maps.newHashMap();
        final List<ChangeHistoryItem.Builder> builders = Lists.newArrayList();

        for (final GenericValue changeItemGV : changeItemsGVs)
        {
            Long changeGroupId = changeItemGV.getLong("changegroupid");
            String fieldName = changeItemGV.getString("field");
            Map<String, ChangeHistoryItem.Builder> buildersPerField = fieldsPerChangeGroup.get(changeGroupId);
            if (buildersPerField == null)
            {
                buildersPerField = new HashMap<String, ChangeHistoryItem.Builder>();
                fieldsPerChangeGroup.put(changeGroupId, buildersPerField);
            }
            if (buildersPerField.containsKey(fieldName)) {
                ChangeHistoryItem.Builder builder = buildersPerField.get(fieldName);
                builder.changedFrom(changeItemGV.getString("oldstring"), changeItemGV.getString("oldvalue"));
                builder.to(changeItemGV.getString("newstring"), changeItemGV.getString("newvalue"));
            }
            else
            {
                ChangeHistoryItem.Builder builder = new ChangeHistoryItem.Builder().withId(changeItemGV.getLong("changeitemid")).inChangeGroup(changeGroupId).
                        inProject(issue.getProjectObject().getId()).forIssue(issue.getId(), issue.getKey()).field(changeItemGV.getString("field")).
                        on(changeItemGV.getTimestamp("created")).changedFrom(changeItemGV.getString("oldstring"), changeItemGV.getString("oldvalue")).
                        to(changeItemGV.getString("newstring"), changeItemGV.getString("newvalue")).byUser(changeItemGV.getString("author"));
                buildersPerField.put(fieldName, builder);
                builders.add(builder);
            }
        }
        return Lists.transform(builders, new Function<ChangeHistoryItem.Builder, ChangeHistoryItem>()
        {
            @Override
            public ChangeHistoryItem apply(@Nullable ChangeHistoryItem.Builder builder)
            {
                return builder.build();
            }
        });
    }

    public Issue findMovedIssue(final String originalKey) throws GenericEntityException
    {
        String key = originalKey.toUpperCase();
        final GenericValue changeItem;
        try
        {
            final List<GenericValue> changeItems = ofBizDelegator.findByLike("ChangeItem", ImmutableMap.of("field", FIELD_KEY, "oldstring", key), ImmutableList.of("group desc"));
            changeItem = getLastChangeItem(changeItems);
        }
        catch (DataAccessException e) //Fixes JRA-5067
        {
            return null;
        }

        if (changeItem == null)
        {
            return null;
        }
        final GenericValue changeGroup = ofBizDelegator.findByPrimaryKey("ChangeGroup", ImmutableMap.of("id", changeItem.getLong("group")));
        if (changeGroup == null)
        {
            return null;
        }
        return issueManager.getIssueObject(changeGroup.getLong("issue"));
    }

    public Collection<String> getPreviousIssueKeys(final Long issueId)
    {
        Null.not("issueId", issueId);
        try
        {
            EntityCondition condition = new EntityFieldMap(
                    ImmutableMap.of("issue", issueId, "field", FIELD_KEY),
                    EntityOperator.AND);
            final List<GenericValue> gvs = ofBizDelegator.findByCondition("ChangeGroupChangeItemView", condition,
                    ImmutableList.of("group", "oldstring", "newstring"), ImmutableList.of("group desc"));

            return collectPreviousIssueKeys(gvs);
        }
        catch (DataAccessException e) //Fixes JRA-5067
        {
            return Collections.emptySet();
        }
    }

    public Collection<String> getPreviousIssueKeys(final String issueKey)
    {
        Null.not("issueKey", issueKey);

        Issue theIssue = issueManager.getIssueObject(issueKey);
        if (theIssue == null)
        {
            return Collections.emptySet();
        }

        return getPreviousIssueKeys(theIssue.getId());
    }

    public Collection<Issue> findUserHistory(final User remoteUser, final Collection<String> usernames, final int maxResults)
    {
        // Only search in projects that we have permission to see
        final Collection<Long> projectIds = new ArrayList<Long>();
        for (Project project : permissionManager.getProjectObjects(Permissions.BROWSE, remoteUser))
        {
            projectIds.add(project.getId());
        }
        return doFindUserHistory(remoteUser, usernames, projectIds, maxResults);
    }

    public Collection<Issue> findUserHistory(final com.opensymphony.user.User remoteUser, final Collection<String> usernames, final int maxResults)
    {
        return findUserHistory((User) remoteUser, usernames, maxResults);
    }

    public Collection<Issue> findUserHistory(final User remoteUser, final Collection<String> usernames, final Collection<Project> projects, final int maxResults)
    {
        // Filter out the projects that we can't see
        final Collection<Long> filteredProjectIds = new ArrayList<Long>();
        for (Project project : projects)
        {
            if (permissionManager.hasPermission(Permissions.BROWSE, project, remoteUser))
            {
                filteredProjectIds.add(project.getId());
            }
        }
        return doFindUserHistory(remoteUser, usernames, filteredProjectIds, maxResults);
    }

    public Collection<Issue> findUserHistory(final com.opensymphony.user.User remoteUser, final Collection<String> usernames, final Collection<Project> projects, final int maxResults)
    {
        return findUserHistory((User) remoteUser, usernames, projects, maxResults);
    }

    @Override
    public Map<String, String> findAllPossibleValues(final String field)
    {
        Null.not("field", field);
        final List<GenericValue> gvs = getAllChangeItems(field);
        return collectFieldValues(gvs);
    }

    private List<GenericValue> getAllChangeItems(String field)
    {
        OfBizListIterator iterator = null;
        final EntityCondition condition = new EntityFieldMap(ImmutableMap.of("field", field), EntityOperator.AND);

        try
        {
            iterator = ofBizDelegator.findListIteratorByCondition("ChangeItem", condition, null,
                    ImmutableList.of("oldstring", "oldvalue", "newstring", "newvalue"),
                    ImmutableList.of("asc"), null);
            return iterator.getCompleteList();
        }
        catch (DataAccessException e)
        {
            log.error("Unable to retrieve values for " + field, e);
            return Lists.newArrayList();
        }
        finally
        {
            if (iterator != null)
            {
                iterator.close();
            }
        }
    }

    public void removeAllChangeItems(final Issue issue)
    {
        final Map<String, ?> params = ImmutableMap.of("issue", issue.getId());
        final List<GenericValue> changeGroups = ofBizDelegator.findByAnd("ChangeGroup", params);
        for (GenericValue changeGroup : changeGroups)
        {
            // remove all changeItems associated with the changeGroup
            ofBizDelegator.removeByAnd("ChangeItem", ImmutableMap.of("group", changeGroup.getLong("id")));
        }
        // remove all changeGroups at once
        ofBizDelegator.removeByAnd("ChangeGroup", params);
    }

    private Map<String,String> collectFieldValues(final List<GenericValue> genericValues)
    {
        MapBuilder<String, String> builder =  MapBuilder.newBuilder();
        for (GenericValue gv : genericValues)
        {
            if (StringUtils.isNotBlank(gv.getString("oldstring")) && StringUtils.isNotBlank(gv.getString("oldvalue")))
            {
                builder.add(gv.getString("oldstring").toLowerCase(), gv.getString("oldvalue").toLowerCase());
            }
            if (StringUtils.isNotBlank(gv.getString("newstring")) && StringUtils.isNotBlank(gv.getString("newvalue")))
            {
                builder.add(gv.getString("newstring").toLowerCase(), gv.getString("newvalue").toLowerCase());
            }
        }
        return builder.toMap();
    }

    Collection<Issue> doFindUserHistory(final User remoteUser, final Collection<String> usernames, final Collection<Long> projects, int maxResults)
    {
        // If we can't see any projects, don't do a search
        if (projects.isEmpty())
        {
            return Collections.emptyList();
        }
        // Create all the conditions now so we can reuse them
        final EntityCondition entityCondition = new EntityConditionList(Arrays.asList(new EntityExpr("project",
                EntityOperator.IN, projects), new EntityExpr("author", EntityOperator.IN, usernames)), EntityOperator.AND);

        // LinkedHashSet will maintain order while removing duplicates
        final Collection<Long> issueIds = new LinkedHashSet<Long>();
        //Get changegroup history.
        issueIds.addAll(extractIssueIds(maxResults, ofBizDelegator.findListIteratorByCondition("ChangeGroupIssueView", entityCondition, null, FIELDS_TO_SELECT, ImmutableList.of("created DESC"), null)));

        //Get comment history.
        issueIds.addAll(extractIssueIds(maxResults, ofBizDelegator.findListIteratorByCondition("ActionIssueView", entityCondition, null, FIELDS_TO_SELECT, ImmutableList.of("created DESC"), null)));

        if (!issueIds.isEmpty())
        {
            //running a search for the issueids which will do all the permission checks quickly and retrieve all
            //issue information without hitting the database!
            final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
            builder.issue().in(issueIds.toArray(new Long[issueIds.size()])).endWhere().orderBy().createdDate(SortOrder.DESC);
            final Query query = builder.buildQuery();

            final SearchResults searchResults;
            //Breaking circular dependency introduced by search change history
            final SearchProvider searchProvider = componentLocator.getComponentInstanceOfType(SearchProvider.class);
            try
            {
                searchResults = searchProvider.search(query, remoteUser, PagerFilter.getUnlimitedFilter());
            }
            catch (SearchException e)
            {
                log.error("Error running query '" + query + "'");
                return Collections.emptyList();
            }

            return Collections.unmodifiableList(searchResults.getIssues());
        }

        return Collections.emptyList();
    }

    private Set<Long> extractIssueIds(final int maxResults, final OfBizListIterator iterator)
    {
        try
        {
            final Set<Long> issueIds = new LinkedHashSet<Long>();
            int issuesLeft = maxResults;
            GenericValue issueIdGV = iterator.next();
            while (issueIdGV != null && issuesLeft > 0)
            {
                issueIds.add(issueIdGV.getLong(ISSUEID_FIELD));
                issueIdGV = iterator.next();
                issuesLeft--;
            }
            return issueIds;
        }
        finally
        {
            iterator.close();
        }
    }

    private Collection<String> collectPreviousIssueKeys(List<GenericValue> issueChangeItemIteratorGVs)
    {
        final Collection<String> result = new LinkedHashSet<String>();
        for (final GenericValue issueChangeItemIteratorGV : issueChangeItemIteratorGVs)
        {
            final String oldString = issueChangeItemIteratorGV.getString("oldstring");
            if (StringUtils.isNotEmpty(oldString))
            {
                result.add(oldString);
            }
        }
        return result;
    }

    private GenericValue getLastChangeItem(List<GenericValue> changeItems)
    {
        GenericValue lastChangeItem = null;

        for (GenericValue changeItem : changeItems)
        {
            if (lastChangeItem == null || lastChangeItem.getLong("id").compareTo(changeItem.getLong("id")) < 0)
            {
                lastChangeItem = changeItem;
            }
        }
        return lastChangeItem;
    }
}
