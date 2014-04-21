package com.atlassian.jira.rest.v1.projectcategories;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v1.model.ValueEntry;
import com.atlassian.jira.rest.v1.model.errors.ErrorCollection;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST endpoint for setting the active Project Category.
 */
@Path("/project-categories")
@AnonymousAllowed
@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_FORM_URLENCODED })
@Produces({MediaType.APPLICATION_JSON})
public class ProjectCategoriesResource
{
    private static final Logger log = Logger.getLogger(ProjectCategoriesResource.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final VelocityRequestContextFactory contextFactory;
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private static final String ALL = "all";
    private static final String NONE = "none";
    private static final String RECENT = "recent";

    public ProjectCategoriesResource(JiraAuthenticationContext jiraAuthenticationContext, VelocityRequestContextFactory contextFactory,
                                     ProjectManager projectManager, PermissionManager permissionManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.contextFactory = contextFactory;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    /**
     * Returns the active Project Category.
     *
     * @return the active Project Category
     */
    @GET
    @Path("/active")
    public Response getCurrent()
    {
        String current = (String) getSession().getAttribute(SessionKeys.BROWSE_PROJECTS_CURRENT_TAB);

        if (validateCategory(current).hasAnyErrors())
        {
            current = null;
        }

        return Response.ok(new ValueEntry(current, "value")).cacheControl(CacheControl.NO_CACHE).build();
    }


    /**
     * Sets the active project category.
     *
     * @param current the active project category
     * @return a 200 response.
     */
    @POST
    @Path("/active")
    public Response setCurrent(String current)
    {
        final ErrorCollection errorCollection = validateCategory(current);

        if (errorCollection.hasAnyErrors())
        {
            return Response.status(400).entity(errorCollection).type(MediaType.APPLICATION_JSON_TYPE).cacheControl(CacheControl.NO_CACHE).build();
        }

        getSession().setAttribute(SessionKeys.BROWSE_PROJECTS_CURRENT_TAB, current);

        return Response.ok().cacheControl(CacheControl.NO_CACHE).build();
    }

    private VelocityRequestSession getSession()
    {
        final VelocityRequestContext requestContext = contextFactory.getJiraVelocityRequestContext();
        return requestContext.getSession();
    }

    /*
     * Validates the given category id.  ensures it is a valid category id, "none", "all" or "recent" and that the user
     * can see at least one project in the category.
     */
    private ErrorCollection validateCategory(String current)
    {
        final ErrorCollection.Builder errorBuilder = ErrorCollection.Builder.newBuilder();

        if (StringUtils.isBlank(current))
        {
            errorBuilder.addError("current", "project.category.active.blank");
        }

        if (!(ALL.equals(current) || NONE.equals(current) || RECENT.equals(current)))
        {
            // Check if the user can see a project in that category and that the category exists
            final GenericValue category = getProjectCategory(current);
            if (category == null || !canSeeCategory(jiraAuthenticationContext.getUser(), category))
            {
                errorBuilder.addError("current", "project.category.active.no.permission.or.not.exist", current);
            }
        }

        return errorBuilder.build();
    }

    /*
     * Get the project category with given id or null if id is invalid.
     */
    private GenericValue getProjectCategory(String categoryIdStr)
    {
        try
        {
            final long categoryId = Long.parseLong(categoryIdStr);
            return projectManager.getProjectCategory(categoryId);
        }
        catch (DataAccessException dae)
        {
            log.warn("Error thrown trying to retrieve category", dae);
        }
        catch (NumberFormatException nfe)
        {
            log.warn("Category was not a number - " + categoryIdStr, nfe);
        }
        return null;
    }

    /*
     * Can the user see any projects in the given category.
     */
    private boolean canSeeCategory(final User user, GenericValue category)
    {
        final Collection projects = permissionManager.getProjects(Permissions.BROWSE, user, category);
        return projects != null && !projects.isEmpty();
    }

}
