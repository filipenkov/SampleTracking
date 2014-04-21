package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.jira.plugins.importer.managers.CreateProjectHandlerProvider;
import com.atlassian.jira.plugins.importer.managers.CreateProjectManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Path("createProjectHandler")
@AnonymousAllowed
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class CreateProjectHandlerBackdoorResource {

    private final CreateProjectHandlerProvider createProjectHandlerProvider;
    private final CreateProjectManager createProjectManager;

    public CreateProjectHandlerBackdoorResource(CreateProjectHandlerProvider createProjectHandlerProvider, CreateProjectManager createProjectManager) {
        this.createProjectHandlerProvider = createProjectHandlerProvider;
        this.createProjectManager = createProjectManager;
    }

    @POST
    public Response canCreateProjects(boolean enabled) {
        if (enabled) {
            createProjectHandlerProvider.setHandler(createProjectManager.getDefaultHandler());
        } else {
            createProjectHandlerProvider.setHandler(new BackdoorCreateProjectHandler());
        }
        return Response.ok(!enabled).cacheControl(never()).build();
    }

}
