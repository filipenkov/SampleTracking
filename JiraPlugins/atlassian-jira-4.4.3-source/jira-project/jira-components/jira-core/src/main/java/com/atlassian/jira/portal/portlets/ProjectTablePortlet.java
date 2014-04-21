package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.core.bean.MathBean;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.seraph.util.RedirectUtils;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Portlet with a list of projects or project categories in a table with a configured list of columns.
 *
 * @since v3.13
 */
public class ProjectTablePortlet extends PortletImpl
{
    private static final String PARAM_CATEGORY_ID = "projectcategoryid";

    private static final Logger log = Logger.getLogger(ProjectTablePortlet.class);

    private final ProjectManager projectManager;
    private static final String VALUE_ALL_CATEGORIES = "allCategories";

    public ProjectTablePortlet(final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ApplicationProperties applicationProperties, final ProjectManager projectManager)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.projectManager = projectManager;
    }

    protected Map /* <String, Object> */getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        final Map params = new HashMap(super.getVelocityParams(portletConfiguration));
        params.put("enterprise", Boolean.TRUE);
        params.put("projectManager", projectManager);
        params.put("redirectUtils", new RedirectUtils());
        params.put("math", new MathBean());
        params.put("projectsExist", Boolean.valueOf(!projectManager.getProjects().isEmpty()));
        params.put("textutils", new TextUtils());
        params.put("allCategories", isAllCategories(portletConfiguration));

        return params;
    }

    /**
     * Returns true only if the config parameter {@link ProjectTablePortlet#PARAM_CATEGORY_ID} specified is the
     * special value {@link ProjectTablePortlet#VALUE_ALL_CATEGORIES} that indicates that all categories should be
     * shown. This should only be used in Enterprise edition.
     *
     * @param portletConfiguration the configuration.
     * @return true only if all categories have been requested.
     */
    private Boolean isAllCategories(final PortletConfiguration portletConfiguration)
    {
        String projectCategoryId = null;
        try
        {
            projectCategoryId = portletConfiguration.getProperty(PARAM_CATEGORY_ID);
        }
        catch (final ObjectConfigurationException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("missing configuration parameter projectcategoryid");
            }
        }
        return Boolean.valueOf(VALUE_ALL_CATEGORIES.equals(projectCategoryId));
    }

    /**
     * Retrieves a list of projects belonging to the specified category that the user has permission to see
     *
     * @param category specify the category or null to retrieve a list of browseable projects that are not associated with any category
     * @return collection of project generic values
     * @throws org.ofbiz.core.entity.GenericEntityException if cannot retrieve projects
     */
    public Collection getBrowseableProjectsInCategory(final GenericValue category) throws GenericEntityException
    {
        return permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser(), category);
    }

    public Collection getBrowseableProjectsWithNoCategory() throws GenericEntityException
    {
        return permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser(), null);
    }

    public Collection getBrowseableProjects()
    {
        return permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser());
    }

    /**
     * Retrieves the single select project category as a generic value. If allCategories are requested, this will
     * return null or if the projectCategoryId doesn't match an existing project category, this will return null.
     * TODO: Replace GV with ProjectCategory domain object when it is created
     * @param portletConfiguration contains the projectCategoryId parameter.
     * @return the project category or null if no single project category is configured in the portletConfiguration.
     */
    public GenericValue getProjectCategoryGv(final PortletConfiguration portletConfiguration)
    {
        try
        {
            return projectManager.getProjectCategory(portletConfiguration.getLongProperty(PARAM_CATEGORY_ID));
        }
        catch (final ObjectConfigurationException e)
        {
            return null;
        }
        catch (final RuntimeException e)
        {
            return null;
        }
    }

    /**
     * Tells whether to show signup link, true only if exteneral user management is off and JIRA is in public mode.
     *
     * @return true only if the link to signup should be shown given current JIRA config.
     */
    public boolean showSignup()
    {
        return !applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT) && JiraUtils.isPublicMode();
    }

}
