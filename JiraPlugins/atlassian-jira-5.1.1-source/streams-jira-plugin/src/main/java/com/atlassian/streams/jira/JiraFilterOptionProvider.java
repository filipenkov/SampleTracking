package com.atlassian.streams.jira;

import java.util.Map;

import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.spi.ActivityOptions;
import com.atlassian.streams.spi.StreamsFilterOption;
import com.atlassian.streams.spi.StreamsFilterOption.Builder;
import com.atlassian.streams.spi.StreamsFilterOptionProvider;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.ActivityObjectTypes.comment;
import static com.atlassian.streams.api.ActivityObjectTypes.file;
import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.ActivityVerbs.update;
import static com.atlassian.streams.api.StreamsFilterType.SELECT;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.atlassian.streams.jira.JiraActivityVerbs.close;
import static com.atlassian.streams.jira.JiraActivityVerbs.reopen;
import static com.atlassian.streams.jira.JiraActivityVerbs.open;
import static com.atlassian.streams.jira.JiraActivityVerbs.resolve;
import static com.atlassian.streams.jira.JiraActivityVerbs.start;
import static com.atlassian.streams.jira.JiraActivityVerbs.stop;
import static com.atlassian.streams.jira.JiraActivityVerbs.transition;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static com.google.common.collect.Sets.newHashSet;

public class JiraFilterOptionProvider implements StreamsFilterOptionProvider
{
    public static final String ISSUE_TYPE = "issue_type";

    public static final Iterable<Pair<ActivityObjectType, ActivityVerb>> activities =
        ImmutableList.<Pair<ActivityObjectType, ActivityVerb>>builder().
            add(pair(issue(), post())).
            add(pair(issue(), update())).
            add(pair(issue(), transition())).
            add(pair(issue(), reopen())).
            add(pair(issue(), close())).
            add(pair(issue(), open())).
            add(pair(issue(), resolve())).
            add(pair(issue(), start())).
            add(pair(issue(), stop())).
            add(pair(comment(), post())).
            add(pair(file(), post())).
            build();

    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final I18nResolver i18nResolver;
    private final Function<Pair<ActivityObjectType, ActivityVerb>, ActivityOption> toActivityOption;

    public JiraFilterOptionProvider(final PermissionManager permissionManager,
            final JiraAuthenticationContext authenticationContext,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final I18nResolver i18nResolver)
    {
        this.permissionManager = checkNotNull(permissionManager, "permissionManager");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.issueTypeSchemeManager = checkNotNull(issueTypeSchemeManager, "issueTypeSchemeManager");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.toActivityOption = ActivityOptions.toActivityOption(i18nResolver, "streams.filter.jira");
    }

    public Iterable<StreamsFilterOption> getFilterOptions()
    {
        return ImmutableList.of(getIssueTypeFilter());
    }

    public Iterable<ActivityOption> getActivities()
    {
        return transform(activities, toActivityOption);
    }

    private StreamsFilterOption getIssueTypeFilter()
    {
        return new Builder(ISSUE_TYPE, SELECT)
            .displayName(i18nResolver.getText("streams.filter.jira.issue.type"))
            .helpTextI18nKey("streams.filter.help.jira.issue.type")
            .i18nKey("streams.filter.jira.issue.type")
            .unique(true)
            .values(getIssueTypes())
            .build();
    }

    private Map<String, String> getIssueTypes()
    {
        return transformValues(uniqueIndex(newHashSet(concat(transform(getAllProjects(), toIssueTypeIterable))), toIssueTypeKey), toIssueTypeLabel);
    }

    private Iterable<Project> getAllProjects()
    {
        return permissionManager.getProjectObjects(Permissions.BROWSE, authenticationContext.getLoggedInUser());
    }

    private static final Function<IssueType, String> toIssueTypeKey = new Function<IssueType, String>()
    {
        public String apply(IssueType type)
        {
            return type.getId();
        }
    };

    private static final Function<IssueType, String> toIssueTypeLabel = new Function<IssueType, String>()
    {
        public String apply(IssueType type)
        {
            return type.getNameTranslation();
        }
    };

    private final Function<Project, Iterable<IssueType>> toIssueTypeIterable = new Function<Project, Iterable<IssueType>>()
    {
        public Iterable<IssueType> apply(Project project)
        {
            return issueTypeSchemeManager.getIssueTypesForProject(project);
        }
    };
}
