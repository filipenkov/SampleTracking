package com.atlassian.jira.extra.icalfeed.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.extra.icalfeed.dateprovider.DateProvider;
import com.atlassian.jira.extra.icalfeed.dateprovider.DateProviderModuleDescriptor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.Query;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultEntityAsEventService implements EntityAsEventService
{
    private final JiraAuthenticationContext jiraAuthenticationContext;

    private final PluginAccessor pluginAccessor;

    private final SearchService searchService;

    private final ProjectManager projectManager;

    private final PermissionManager permissionManager;

    public DefaultEntityAsEventService(JiraAuthenticationContext jiraAuthenticationContext, PluginAccessor pluginAccessor, SearchService searchService, ProjectManager projectManager, PermissionManager permissionManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.pluginAccessor = pluginAccessor;
        this.searchService = searchService;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    @Override
    public Result search(Query query, Set<String> dateFieldNames, boolean includeFixVersions, User user) throws SearchException, ParseException
    {
        Collection<IssueDateResult> issueDateResults = new ArrayList<IssueDateResult>();
        
        for (String dateFieldName : dateFieldNames)
        {
            DateProvider dateProvider = getDateProvider(dateFieldName);
            I18nHelper i18nHelper = jiraAuthenticationContext.getI18nHelper();

            if (null != dateProvider)
            {
                Collection<Issue> issueResults = searchService.search(user, query, PagerFilter.getUnlimitedFilter()).getIssues();

                if (null != issueResults)
                {
                    for (Issue issue : issueResults)
                    {
                        DateTime startDate = dateProvider.getStart(issue, dateFieldName);
                        DateTime endDate = dateProvider.getEnd(issue, dateFieldName);

                        if (null != startDate && null != endDate)
                        {
                            issueDateResults.add(
                                    new IssueDateResult(
                                            issue.getAssigneeUser(),
                                            startDate,
                                            endDate,
                                            dateProvider.isAllDay(issue, dateFieldName),
                                            issue.getKey(),
                                            issue.getSummary(),
                                            issue.getDescription(),
                                            issue.getIssueTypeObject().getNameTranslation(i18nHelper),
                                            issue.getIssueTypeObject().getIconUrl(),
                                            issue.getStatusObject().getNameTranslation(i18nHelper),
                                            issue.getStatusObject().getIconUrl(),
                                            new DateTime(issue.getCreated().getTime()),
                                            new DateTime(issue.getUpdated().getTime())
                                        )
                            );
                        }
                    }
                }
            }
        }

        Collection<Version> versions = new ArrayList<Version>();
        if (includeFixVersions)
            for (Project project : getProjects(user, query))
                versions.addAll(Collections2.filter(
                        project.getVersions(),
                        new Predicate<Version>()
                        {
                            @Override
                            public boolean apply(@Nullable Version version)
                            {
                                return null != version.getReleaseDate();
                            }
                        }
                ));

        return new Result(
                issueDateResults,
                Collections.<Version>emptySet(),
                versions
        );
    }


    /* Wider visibility scope for tests */
    DateProvider getDateProvider(String dateFieldName)
    {
        List<DateProviderModuleDescriptor> dictionaryModuleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(DateProviderModuleDescriptor.class);
        if (null != dictionaryModuleDescriptors)
        {
            for (DateProviderModuleDescriptor dateProviderModuleDescriptor : dictionaryModuleDescriptors)
            {
                DateProvider dateProvider = dateProviderModuleDescriptor.getModule();
                if (dateProvider.handles(dateFieldName))
                    return dateProvider;
            }
        }

        return null;
    }

    private Set<Project> getProjects(final User user, Query searchQuery)
    {
        QueryContext simpleQueryContext = searchService.getSimpleQueryContext(user, searchQuery);
        Collection queryProjects = simpleQueryContext.getProjectIssueTypeContexts();
        Set<Project> projectsInQuery = new HashSet<Project>();

        for (Object contextObject: queryProjects)
        {
            projectsInQuery.addAll(
                    Collections2.filter(
                            Collections2.transform(
                                    ((QueryContext.ProjectIssueTypeContexts) contextObject).getProjectIdInList(),
                                    new Function<Long, Project>()
                                    {
                                        public Project apply(Long projectId)
                                        {
                                            return projectManager.getProjectObj(projectId);
                                        }
                                    }
                            ),
                            Predicates.and(Predicates.<Project>notNull(), new Predicate<Project>()
                            {
                                public boolean apply(Project aProject)
                                {
                                    return permissionManager.hasPermission(Permissions.BROWSE, aProject, user);
                                }
                            })
                    )
            );
        }

        return projectsInQuery;
    }
}
