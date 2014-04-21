package com.atlassian.jira.user.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Iterables.any;

/**
 * User Profile Tab containing a list of project with assigned and open issues
 *
 * @since v4.1
 */
public class AssignedAndOpenUserProfileFragment extends AbstractUserProfileFragment
{
    private final PermissionManager permissionManager;
    private final SearchService searchService;

    public AssignedAndOpenUserProfileFragment(JiraAuthenticationContext jiraAuthenticationContext,
            VelocityTemplatingEngine templatingEngine, VelocityParamFactory velocityParamFactory, PermissionManager permissionManager,
            final SearchService searchService)
    {
        super(jiraAuthenticationContext, templatingEngine, velocityParamFactory);
        this.permissionManager = permissionManager;
        this.searchService = searchService;
    }

    /**
     * Only show this fragment is the current user can browse any of the projects that the profile user can be assigned to.
     *
     * @param profileUser The user whose profile the current user is looking at
     * @param currentUser The current user
     * @return true if the current user can browse any of the projects that the profile user can be assigned to
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NM_WRONG_PACKAGE", justification="OSUser is deprecated and dying anyway. Plus the method in question is final so we can't override it.")
    public boolean showFragment(User profileUser, User currentUser)
    {
        return any(
                permissionManager.getProjectObjects(Permissions.BROWSE, currentUser),
                in(permissionManager.getProjectObjects(Permissions.ASSIGNABLE_USER, profileUser))
        );
    }

    @Override
    protected Map<String, Object> createVelocityParams(User profileUser, User currentUser)
    {
        final Map<String, Object> params = super.createVelocityParams(profileUser, currentUser);
        params.put("projects", getProjects(profileUser, currentUser));
        params.put("urlBuilder", new UrlBuilder(profileUser, currentUser));

        return params;
    }

    public String getId()
    {
        return "assigned-and-open";
    }

    private StatisticMapWrapper getProjects(final User profileUser, final User currentUser)
    {
        final JqlClauseBuilder builder = getBaseQuery(profileUser, currentUser);

        final SearchRequest request = new SearchRequest(builder.buildQuery());

        try
        {
            final StatisticAccessorBean bean = new StatisticAccessorBean(currentUser, request);
            final StatisticMapWrapper mapWrapper = bean.getAllFilterBy("project");
            return mapWrapper;
        }
        catch (SearchException e)
        {
            throw new RuntimeException(e);
        }

    }

    private JqlClauseBuilder getBaseQuery(User profileUser, User currentUser)
    {
        final JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().unresolved();
        if (profileUser.equals(currentUser))
        {
            builder.and().assigneeIsCurrentUser();
        }
        else
        {
            builder.and().assignee().eq(profileUser.getName());
        }
        return builder;
    }

    /**
     * Utility class for creating url on the fly in the velocity.
     */
    public class UrlBuilder
    {
        private final User profileUser;
        private final User currentUser;

        public UrlBuilder(User profileUser, User currentUser)
        {
            this.profileUser = profileUser;
            this.currentUser = currentUser;
        }

        public String getUrl(GenericValue project)
        {
            final JqlClauseBuilder builder = getBaseQuery(profileUser, currentUser);
            builder.and().project(project.getLong("id"));
            return searchService.getQueryString(currentUser, builder.buildQuery());
        }
    }
}
