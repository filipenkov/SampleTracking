package com.atlassian.jira.collector.plugin.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static java.lang.Math.max;
import static java.lang.Math.min;


@Path("project")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class ProjectResource {

	public static final int DEFAULT_USERS_RETURNED = 50;
	public static final int MAX_USERS_RETURNED = 1000;

	private final PermissionManager permissionManager;
	private final ProjectManager projectManager;

	private final UserPickerSearchService userPickerSearchService;
	private final EmailFormatter emailFormatter;
	private final JiraAuthenticationContext authContext;
	private final TimeZoneManager timeZoneManager;

	public ProjectResource(final PermissionManager permissionManager, final ProjectManager projectManager, final UserPickerSearchService userPickerSearchService, final EmailFormatter emailFormatter, final JiraAuthenticationContext authContext, final TimeZoneManager timeZoneManager) {
		this.permissionManager = permissionManager;
		this.projectManager = projectManager;
		this.userPickerSearchService = userPickerSearchService;
		this.emailFormatter = emailFormatter;
		this.authContext = authContext;
		this.timeZoneManager = timeZoneManager;
	}

	@GET
	@Path("reporter/search")
	public Response getRepoterForProject(@QueryParam("username") final String username, @QueryParam("projectKeys") final String projectKey,
			@QueryParam("maxResults") final Integer maxResults, @QueryParam("startAt") final Integer startAt, final @Context UriInfo uriInfo)
    {

        final Project project = projectManager.getProjectObjByKey(projectKey);
		final List<User> users = userPickerSearchService.findUsers(getContext(), username);

		final ImmutableList<User> filteredUsers = ImmutableList.copyOf(Iterables.limit(Iterables.filter(users, new Predicate<User>() {
			@Override
			public boolean apply(final User user) {
				return permissionManager.hasPermission(Permissions.CREATE_ISSUE, project, user);
			}
		}),maxResults));

		return Response.ok(makeUserBeans(filteredUsers, uriInfo)).cacheControl(never()).build();
	}

	private List<User> limitUserSearch(final Integer startAt, final Integer maxResults, final List<User> users) {
		final int start = startAt != null ? max(0, startAt) : 0;
		final int end = (maxResults != null ? min(MAX_USERS_RETURNED, maxResults) : DEFAULT_USERS_RETURNED) + start;

		return users.subList(start, min(users.size(), end));
	}

	JiraServiceContext getContext() {
		final User user = authContext.getLoggedInUser();
		final com.atlassian.jira.util.ErrorCollection errorCollection = new SimpleErrorCollection();
		return new JiraServiceContextImpl(user, errorCollection);
	}

	private List<UserBean> makeUserBeans(final Collection<User> users, final UriInfo uriInfo) {
		final List<UserBean> beans = new ArrayList<UserBean>();
		for (final User user : users) {
			final UserBeanBuilder builder = new UserBeanBuilder().user(user).context(uriInfo);
			builder.loggedInUser(authContext.getLoggedInUser());
			builder.emailFormatter(emailFormatter);
			builder.timeZone(timeZoneManager.getLoggedInUserTimeZone());
			beans.add(builder.buildMid());
		}
		return beans;
	}

}
