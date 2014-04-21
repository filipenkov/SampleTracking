package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.util.ProjectConfigRequestCache;
import com.atlassian.jira.projectconfig.util.UrlEncoder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides common utilities aimed at context providers, such as retrieving the current project.
 *
 * @since v4.4
 */
public class ContextProviderUtils
{
    // Keys for context items provided in the default context
    static final String CONTEXT_PROJECT_KEY = "project";
    static final String CONTEXT_IS_ADMIN_KEY = "isAdmin";
    static final String CONTEXT_IS_PROJECT_ADMIN_KEY = "isProjectAdmin";
    static final String CONTEXT_I18N_KEY = "i18n";
    static final String CONTEXT_PROJECT_KEY_ENCODED = "projectKeyEncoded";

    private final VelocityRequestContextFactory requestContextFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final UrlEncoder encoder;
    private final ProjectConfigRequestCache cache;
    private final ComparatorFactory comparatorFactory;

    public ContextProviderUtils(final VelocityRequestContextFactory requestContextFactory,
            final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager,
            final UrlEncoder encoder, final ProjectConfigRequestCache cache, final ComparatorFactory comparatorFactory)
    {
        this.requestContextFactory = requestContextFactory;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.encoder = encoder;
        this.cache = cache;
        this.comparatorFactory = comparatorFactory;
    }

    public Project getProject()
    {
        return cache.getProject();
    }

    public Comparator<String> getStringComparator()
    {
        return comparatorFactory.createStringComparator();
    }

    /**
     * Provides a default context that should be provided to all context providers.
     *
     * In particular, contains:
     *
     * <dl>
     *     <dt>project</dt><dd>The current project, null if no project was selected</dd>
     *     <dt>isAdmin</dt><dd>True if current user is a system admin</dd>
     *     <dt>isProjectAdmin</dt><dd>True if current user is a project admin</dd>
     *     <dt>i18n</dt><dd>An i18nHelper object</dd>
     * </dl>
     *
     * @return
     */
    public Map<String, Object> getDefaultContext()
    {
        Project project = getProject();
        return MapBuilder.<String, Object>newBuilder()
                .add(CONTEXT_PROJECT_KEY, project)
                .add(CONTEXT_PROJECT_KEY_ENCODED, encode(project.getKey()))
                .add(CONTEXT_IS_ADMIN_KEY, hasAdminPermission())
                .add(CONTEXT_IS_PROJECT_ADMIN_KEY, hasProjectAdminPermission())
                .add(CONTEXT_I18N_KEY, authenticationContext.getI18nHelper())
                .toMap();
    }

    public Set<String> flattenErrors(ErrorCollection collection)
    {
        Assertions.notNull("collection", collection);
        Set<String> errors = new LinkedHashSet<String>();
        errors.addAll(collection.getErrorMessages());
        errors.addAll(collection.getErrors().values());
        return errors;
    }

    boolean hasProjectAdminPermission()
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, getProject(), authenticationContext.getLoggedInUser());
    }

    boolean hasAdminPermission()
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser());
    }

    String encode(String value)
    {
        return encoder.encode(value);
    }

    public UrlBuilder createUrlBuilder(String basename)
    {
        return new UrlBuilder(getBaseUrl() + StringUtils.stripToEmpty(basename));
    }

    public String getBaseUrl()
    {
        return requestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
    }
}
