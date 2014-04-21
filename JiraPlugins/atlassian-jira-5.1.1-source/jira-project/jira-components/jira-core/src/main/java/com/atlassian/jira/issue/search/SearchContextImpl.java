package com.atlassian.jira.issue.search;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.LazyIssueContext;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ObjectUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.collections.functors.InstanceofPredicate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SearchContextImpl implements SearchContext
{
    private static final Logger log = Logger.getLogger(SearchContextImpl.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    private static final Predicate LONG_PREDICATE = new AndPredicate(InstanceofPredicate.getInstance(Long.class), ObjectUtils.getIsSetPredicate());
    private static final Predicate STRING_PREDICATE = new AndPredicate(InstanceofPredicate.getInstance(String.class), ObjectUtils.getIsSetPredicate());

    // ------------------------------------------------------------------------------------------------- Type Properties
    protected List projectCategoryIds;
    protected List<Long> projectIds;
    protected List<String> issueTypeIds;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public SearchContextImpl()
    {
        constantsManager = ComponentAccessor.getConstantsManager();
        projectManager = ComponentAccessor.getProjectManager();
        permissionManager = ComponentAccessor.getPermissionManager();
        authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
    }

    public SearchContextImpl(List projectCategoryIds, List projectIds, List issueTypeIds)
    {
        this();
        setProjectCategoryIds(projectCategoryIds);
        setProjectIds(projectIds);
        setIssueTypeIds(issueTypeIds);
    }

    public SearchContextImpl(SearchContext searchContext)
    {
        this(searchContext.getProjectCategoryIds(), searchContext.getProjectIds(), searchContext.getIssueTypeIds());
    }


    // -------------------------------------------------------------------------------------------------- Public Methods
    public boolean isForAnyProjects()
    {
        return (projectCategoryIds == null || projectCategoryIds.isEmpty()) &&
               (projectIds == null || projectIds.isEmpty());
    }

    public boolean isForAnyIssueTypes()
    {
        return (issueTypeIds == null || issueTypeIds.isEmpty());
    }

    public boolean isSingleProjectContext()
    {
        return getProjectIds() != null && getProjectIds().size() == 1;
    }

    public List getProjectCategoryIds()
    {
        return projectCategoryIds;
    }

    private void setProjectCategoryIds(List projectCategoryIds)
    {
        this.projectCategoryIds = prepareProjectList(projectCategoryIds);
    }

    public List<Long> getProjectIds()
    {
        return projectIds;
    }

    private void setProjectIds(List projectIds)
    {
        this.projectIds = prepareProjectList(projectIds);
    }

    public GenericValue getOnlyProject()
    {
        if (isSingleProjectContext())
        {
            Long projectId = (Long) getProjectIds().get(0);
            return projectManager.getProject(projectId);
        }
        else
        {
            log.warn("Trying to get the only the project but is not a single project context. Project ids are: " + getProjectIds());
            return null;
        }
    }

    public List<String> getIssueTypeIds()
    {
        return issueTypeIds;
    }

    public List<IssueContext> getAsIssueContexts()
    {
        List<IssueContext> issueContexts = new ArrayList<IssueContext>();
        List projectIds = getProjectIds() != null && !getProjectIds().isEmpty() ? getProjectIds() : EasyList.buildNull();
        for (Iterator iterator = projectIds.iterator(); iterator.hasNext();)
        {
            Long projectId = (Long) iterator.next();
            List issueTypeIds = getIssueTypeIds() != null && !getIssueTypeIds().isEmpty() ? getIssueTypeIds() : EasyList.buildNull();
            for (Iterator iterator1 = issueTypeIds.iterator(); iterator1.hasNext();)
            {
                String issueType = (String) iterator1.next();
                issueContexts.add(new LazyIssueContext(projectId, issueType));
            }
        }

        return issueContexts;
    }

    public void verify()
    {
        if (projectIds != null && !projectIds.isEmpty())
        {
            for (Iterator iterator = projectIds.iterator(); iterator.hasNext();)
            {
                Long projectId = (Long) iterator.next();
                if (projectManager.getProject(projectId) == null)
                {
                    log.warn("Project id " + projectId + " found in searchContext but is not valid. Being removed.");
                    iterator.remove();
                }
            }
        }

        if (issueTypeIds != null && !issueTypeIds.isEmpty())
        {
            for (Iterator iterator = issueTypeIds.iterator(); iterator.hasNext();)
            {
                String issueTypeId = (String) iterator.next();
                if (constantsManager.getIssueType(issueTypeId) == null)
                {
                    log.warn("Issue type id " + issueTypeId + " found in searchContext but is not valid. Being removed.");
                    iterator.remove();
                }
            }
        }
    }

    private void setIssueTypeIds(List issueTypeIds)
    {
        this.issueTypeIds = ListUtils.predicatedList(constantsManager.expandIssueTypeIds(issueTypeIds), STRING_PREDICATE);
    }

    // -------------------------------------------------------------------------------------------------- Helper methods
    private static List<Long> prepareProjectList(List list)
    {
        if (list == null)
        {
            return Collections.emptyList();
        }
        else if (list.size() == 1 && !ObjectUtils.isValueSelected(list.get(0)))
        {
            return Collections.emptyList();
        }
        else
        {
            return ListUtils.predicatedList(list, LONG_PREDICATE);
        }
    }

    public String toString()
    {
        return new ToStringBuilder(this)
                .append("projectCategoryIds", getProjectCategoryIds())
                .append("projectIds", getProjectIds())
                .append("issueTypeIds", getIssueTypeIds())
                .toString();
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SearchContextImpl))
        {
            return false;
        }
        SearchContextImpl rhs = (SearchContextImpl) o;
        return new EqualsBuilder()
                .append(getProjectCategoryIds(), rhs.getProjectCategoryIds())
                .append(getProjectIds(), rhs.getProjectIds())
                .append(getIssueTypeIds(), rhs.getIssueTypeIds())
                .isEquals();
    }

    public int hashCode()
    {
        return new HashCodeBuilder(37, 47)
                .append(getProjectCategoryIds())
                .append(getProjectIds())
                .append(getIssueTypeIds())
                .toHashCode();
    }
}
