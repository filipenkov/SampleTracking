package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Path("project")
@AnonymousAllowed
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class ProjectBackdoorResource {

    private final ProjectRoleManager projectRoleManager;
    private final ProjectManager projectManager;
    private final UserUtil userUtil;

    public ProjectBackdoorResource(ProjectRoleManager projectRoleManager, ProjectManager projectManager, UserUtil userUtil) {
        this.projectRoleManager = projectRoleManager;
        this.projectManager = projectManager;
        this.userUtil = userUtil;
    }

    @GET
    @Path("{projectKey}/roles/{username}")
    public Response userRoles(@PathParam("projectKey") String projectKey, @PathParam("username") String username) {
        final User user = userUtil.getUser(username);
        final Project project = projectManager.getProjectObjByKey(projectKey);
        return Response.ok(ImmutableList.copyOf(Iterables.transform(projectRoleManager.getProjectRoles(user, project), new Function<ProjectRole, String>() {
            @Override
            public String apply(@Nullable ProjectRole input) {
                return input.getName();
            }
        }))).cacheControl(never()).build();
    }

}
