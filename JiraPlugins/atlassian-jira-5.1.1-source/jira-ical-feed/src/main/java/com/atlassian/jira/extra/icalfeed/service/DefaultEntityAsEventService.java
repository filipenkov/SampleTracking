package com.atlassian.jira.extra.icalfeed.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.extra.icalfeed.dateprovider.DateProvider;
import com.atlassian.jira.extra.icalfeed.dateprovider.DateProviderModuleDescriptor;
import com.atlassian.jira.extra.icalfeed.util.QueryUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.query.Query;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DefaultEntityAsEventService implements EntityAsEventService
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultEntityAsEventService.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;

    private final FieldManager fieldManager;

    private final Collection<DateProvider> builtInProviders;

    private final DateProvider customFieldDateProvider;

    private final PluginAccessor pluginAccessor;

    private final SearchService searchService;

    private final QueryUtil queryUtil;

    public DefaultEntityAsEventService(JiraAuthenticationContext jiraAuthenticationContext, FieldManager fieldManager, Collection<DateProvider> builtInProviders , DateProvider customFieldDateProvider, PluginAccessor pluginAccessor, SearchService searchService, QueryUtil queryUtil)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.fieldManager = fieldManager;
        this.builtInProviders = builtInProviders;
        this.customFieldDateProvider = customFieldDateProvider;
        this.pluginAccessor = pluginAccessor;
        this.searchService = searchService;
        this.queryUtil = queryUtil;
    }

    @Override
    public Result search(Query query, Set<String> dateFieldNames, boolean includeFixVersions, User user) throws SearchException, ParseException
    {
        Collection<IssueDateResult> issueDateResults = new ArrayList<IssueDateResult>();

        for (String dateFieldName : dateFieldNames)
        {
            Field dateField = fieldManager.getField(dateFieldName);

            if (null == dateField)
                continue;

            Collection<DateProvider> dateProviders = getDateProvider(dateField);

            if (!dateProviders.isEmpty())
            {
                I18nHelper i18nHelper = jiraAuthenticationContext.getI18nHelper();
                Collection<Issue> issueResults = searchService.search(user, query, PagerFilter.getUnlimitedFilter()).getIssues();

                if (null != issueResults)
                {
                    for (final DateProvider dateProvider : dateProviders)
                        for (Issue issue : issueResults)
                        {
                            try
                            {
                                DateTime startDate = dateProvider.getStart(issue, dateField);
                                DateTime endDate = dateProvider.getEnd(issue, dateField, startDate);

                                if (null != startDate && null != endDate)
                                {
                                    issueDateResults.add(
                                            new IssueDateResult(
                                                    issue.getAssigneeUser(),
                                                    dateField.getId(),
                                                    dateField.getName(),
                                                    startDate,
                                                    endDate,
                                                    dateProvider.isAllDay(issue, dateField),
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
                            catch (RuntimeException dateProviderError)
                            {
                                LOG.error(String.format("Unable to process field %s of %s with %s", dateField.getNameKey(), issue.getKey(), dateProvider.getClass().getName()), dateProviderError);
                            }
                        }
                }
            }
        }

        Collection<Version> versions = new ArrayList<Version>();
        if (includeFixVersions)
            for (Project project : queryUtil.getBrowseableProjectsFromQuery(user, query))
                versions.addAll(Collections2.filter(
                        project.getVersions(),
                        Predicates.and(
                                Predicates.<Object>notNull(),
                                new Predicate<Version>()
                                {
                                    @Override
                                    public boolean apply(Version version)
                                    {
                                        return null != version.getReleaseDate();
                                    }
                                }
                        )
                ));

        return new Result(
                issueDateResults,
                Collections.<Version>emptySet(),
                versions
        );
    }


    /* Wider visibility scope for tests */
    Collection<DateProvider> getDateProvider(final Field field)
    {
        List<DateProvider> dateProviders = new ArrayList<DateProvider>();

        dateProviders.addAll(
                Collections2.filter(
                        builtInProviders,
                        new Predicate<DateProvider>()
                        {
                            @Override
                            public boolean apply(DateProvider builtInDateProvider)
                            {
                                return builtInDateProvider.handles(field);
                            }
                        }
                )
        );

        if (field instanceof CustomField)
        {
            List<DateProvider> customFieldDateOverrideProviders = new ArrayList<DateProvider>();
            List<DateProviderModuleDescriptor> dictionaryModuleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(DateProviderModuleDescriptor.class);
            if (null != dictionaryModuleDescriptors)
                for (DateProviderModuleDescriptor dateProviderModuleDescriptor : dictionaryModuleDescriptors)
                {
                    DateProvider dateProvider = dateProviderModuleDescriptor.getModule();
                    if (dateProvider.handles(field))
                        customFieldDateOverrideProviders.add(dateProvider);
                }

            if (customFieldDateOverrideProviders.isEmpty() && customFieldDateProvider.handles(field))
            {
                dateProviders.add(customFieldDateProvider);
            }
            else
            {
                dateProviders.addAll(customFieldDateOverrideProviders);
            }
        }

        return dateProviders;
    }
}
