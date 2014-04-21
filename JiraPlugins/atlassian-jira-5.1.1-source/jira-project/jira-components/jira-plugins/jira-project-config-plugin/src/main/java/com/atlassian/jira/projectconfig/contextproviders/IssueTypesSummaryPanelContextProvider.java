package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides context for the issue types summary panel, in particular the "issueTypes" object
 * containing the list of a project's {@link IssueType}s.
 *
 * @since v4.4
 */
public class IssueTypesSummaryPanelContextProvider implements CacheableContextProvider
{
    static final String CONTEXT_ISSUE_TYPES_KEY = "issueTypes";
    static final String CONTEXT_ISSUE_TYPE_SCHEME_KEY = "issueTypeScheme";
    static final String CONTEXT_ERRORS_KEY = "errors";

    static final String ISSUE_TYPE_SCHEME_ERROR_I18N_KEY = "admin.project.config.summary.issuetypes.no.issuetypescheme.error";
    static final String MANAGE_URL = "manageUrl";

    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final TabUrlFactory tabUrlFactory;
    private final ComparatorFactory comparatorFactory;

    public IssueTypesSummaryPanelContextProvider(final IssueTypeSchemeManager issueTypeSchemeManager,
            TabUrlFactory tabUrlFactory, ComparatorFactory comparatorFactory)
    {
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.tabUrlFactory = tabUrlFactory;
        this.comparatorFactory = comparatorFactory;
    }

    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(final Map<String, Object> context)
    {
        final Project project = (Project) context.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        final I18nHelper i18nHelper = (I18nHelper) context.get(ContextProviderUtils.CONTEXT_I18N_KEY);

        final Collection<SimpleIssueType> simpleIssueTypes = getSimpleIssueTypes(project);

        final FieldConfigScheme issueTypeScheme = issueTypeSchemeManager.getConfigScheme(project);

        final List<String> errors = Lists.newArrayList();
        if(issueTypeScheme == null)
        {
            errors.add(i18nHelper.getText(ISSUE_TYPE_SCHEME_ERROR_I18N_KEY));
        }

        return MapBuilder.<String, Object>newBuilder()
                .addAll(context)
                .add(CONTEXT_ISSUE_TYPES_KEY, simpleIssueTypes)
                .add(CONTEXT_ISSUE_TYPE_SCHEME_KEY, issueTypeScheme)
                .add(CONTEXT_ERRORS_KEY, errors)
                .add(MANAGE_URL, tabUrlFactory.forIssueTypes())
                .toMap();
    }

    private Collection<SimpleIssueType> getSimpleIssueTypes(final Project project)
    {
        final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(project);

        final IssueType defaultIssueType = issueTypeSchemeManager.getDefaultValue(project.getGenericValue());

        final List<SimpleIssueType> simpleIssueTypes = Lists.newArrayList();

        for (final IssueType issueType : issueTypes)
        {
            if (defaultIssueType != null && defaultIssueType.equals(issueType))
            {
                simpleIssueTypes.add(new SimpleIssueTypeImpl(issueType, true));
            }
            else
            {
                simpleIssueTypes.add(new SimpleIssueTypeImpl(issueType, false));
            }
        }
        Collections.sort(simpleIssueTypes, comparatorFactory.createIssueTypeComparator());

        return simpleIssueTypes;
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }
}
