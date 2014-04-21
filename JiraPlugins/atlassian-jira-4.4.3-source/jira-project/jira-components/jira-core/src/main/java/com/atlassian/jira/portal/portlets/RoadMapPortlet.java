package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.portal.ProjectAndProjectCategoryValuesGenerator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PercentageGraphModel;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Portlet implementation for the RoadMapPortlet.
 * This portlet displays the upcoming Versions for selected projects.
 * It is restricted to Versions with a release date X days from date run.
 *
 * @since v3.11
 */
public class RoadMapPortlet extends PortletImpl
{
    private static final Logger log = Logger.getLogger(StatsPortlet.class);

    private static final class Property
    {
        private static final String DAYS = "days";
        private static final String MAX_RESULTS = "maxresults";
        private static final String PROJECTS = "projects";
        private static final String PROJECTS_ENT = "projectsEnt";
    }

    private static final int DEFAULT_DAYS = 30;

    private final SearchProvider searchProvider;
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;
    private final VersionManager versionManager;
    private final SearchService searchService;

    public RoadMapPortlet(JiraAuthenticationContext authCtx, PermissionManager permissionManager,
            ApplicationProperties appProps, SearchProvider searchProvider, ConstantsManager constantsManager,
            ProjectManager projectManager, VersionManager versionManager,
            final SearchService searchService)
    {
        super(authCtx, permissionManager, appProps);
        this.searchProvider = searchProvider;
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
        this.versionManager = versionManager;
        this.searchService = searchService;
    }

    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = null;
        try
        {
            params = super.getVelocityParams(portletConfiguration);

            final Long daysProperty = portletConfiguration.getLongProperty(Property.DAYS);
            final int days;
            if (daysProperty != null)
            {
                days = daysProperty.intValue();
            }
            else
            {
                log.debug("Road Map portlet does not have 'days' property set or value is invalid. Dafaulting to " + DEFAULT_DAYS);
                days = DEFAULT_DAYS;
            }

            final List projectsOrCategories = getListFromMultiSelectValue(getProjectsProperty(portletConfiguration));

            Set projectIds = getProjectIds(projectsOrCategories);

            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, days);
            List /* <Version> */ versions = getVersions(projectIds, cal);

            final Long maxResultsProperty = portletConfiguration.getLongProperty(Property.MAX_RESULTS);
            if (maxResultsProperty != null)
            {
                // limit the number of versions in the list
                int maxResults = maxResultsProperty.intValue();
                if (maxResults <= 0)
                {
                    log.warn("Road Map portlet 'maxresults' property value '" + maxResults + "' is invalid, should be a positive number");
                }
                else if (versions.size() > maxResults)
                {
                    versions = versions.subList(0, maxResults);
                }
            }

            List /* <Version> */ overdueVersions = new ArrayList();
            for (Iterator i = versions.iterator(); i.hasNext();)
            {
                Version version = (Version) i.next();
                if (isOverdue(version))
                {
                    overdueVersions.add(version);
                    i.remove();
                }
            }

            params.put("overdueVersions", overdueVersions);
            params.put("versions", versions);
            params.put("days", new Integer(days));
            params.put("dateBefore", cal.getTime());
            params.put("portlet", this);
            params.put("action", this);
            params.put("outlookDate", authenticationContext.getOutlookDate());
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }
        return params;
    }

    private String getProjectsProperty(PortletConfiguration portletConfiguration) throws ObjectConfigurationException
    {
        final String property = portletConfiguration.getProperty(Property.PROJECTS_ENT);
        return StringUtils.isNotBlank(property) ? property : portletConfiguration.getProperty(Property.PROJECTS);
    }

    public boolean isOverdue(Version version)
    {
        return new Date().compareTo(version.getReleaseDate()) > 0;
    }

    /**
     * Converts the list of Project and Category Ids into a Set of Project Ids.
     *
     * @param projectAndCategoryIds A list of project and category ids
     * @return A Set of project Ids.
     */
    Set /* <Long> */ getProjectIds(List /* <String> */ projectAndCategoryIds)
    {
        Set projectIds = new HashSet();
        if (projectAndCategoryIds != null && !projectAndCategoryIds.isEmpty())
        {
            if (projectAndCategoryIds.contains(ProjectAndProjectCategoryValuesGenerator.Values.ALL_PROJECTS))
            {
                // add all projects this user can see
                projectIds.addAll(getAllBrowsableProjects());
            }
            else
            {
                // add projects
                projectIds.addAll(ProjectAndProjectCategoryValuesGenerator.filterProjectIds(projectAndCategoryIds));

                // narrow down to project categories
                final Collection possibleCategoryIds = new ArrayList(projectAndCategoryIds);
                possibleCategoryIds.removeAll(projectIds);

                // get categories
                final Set categoryIds = ProjectAndProjectCategoryValuesGenerator.filterProjectCategoryIds(possibleCategoryIds);
                for (Iterator catIt = categoryIds.iterator(); catIt.hasNext();)
                {
                    Long categoryId = (Long) catIt.next();
                    projectIds.addAll(getProjectIdsForCategory(categoryId));
                }

                projectIds = filterProjectsByPermission(projectIds);

            }
        }
        return projectIds;
    }

    private Set /* <Long> */ getAllBrowsableProjects()
    {
        return extractProjectIdsFromProjectGVs(permissionManager.getProjects(Permissions.BROWSE, authenticationContext.getUser()));
    }

    Set /* <Long> */ filterProjectsByPermission(Set /* <Long> */ projectIds)
    {
        final Set filtered = new HashSet();
        for (Iterator i = projectIds.iterator(); i.hasNext();)
        {
            final Long projectId = (Long) i.next();
            if (canBrowseProject(projectId))
            {
                filtered.add(projectId);
            }
        }
        return filtered;
    }

    boolean canBrowseProject(Long projectId)
    {
        final Project project = projectManager.getProjectObj(projectId);
        return permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getUser());
    }


    /**
     * Collect and return ids of all projects that relate to this project category.
     *
     * @param categoryId category id
     * @return set of project ids, never null
     */
    Set /* <Long> */ getProjectIdsForCategory(Long categoryId)
    {
        Collection projGVs = projectManager.getProjectsFromProjectCategory(projectManager.getProjectCategory(categoryId));
        return extractProjectIdsFromProjectGVs(projGVs);
    }

    private Set extractProjectIdsFromProjectGVs(Collection projGVs)
    {
        Set projectIds = new HashSet();
        for (Iterator projIt = projGVs.iterator(); projIt.hasNext();)
        {
            Long projectId = ((GenericValue) projIt.next()).getLong("id");
            projectIds.add(projectId);
        }
        return projectIds;
    }

    /**
     * Retrieves a List of Versions for a given set of projects.  Versions must have a release
     * date before that of the passed in date.
     *
     * @param projectIds The projects to retrieve the Versions for.
     * @param cal        The date which the Version must be released before
     * @return Versions that will be released before the given date
     */
    List /* <Version> */ getVersions(Set /* <Long> */ projectIds, Calendar cal)
    {
        List versions = new ArrayList();
        for (Iterator i = projectIds.iterator(); i.hasNext();)
        {
            Long projectId = (Long) i.next();
            versions.addAll(getVersionsForProject(projectId, cal.getTime()));
        }

        Collections.sort(versions, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Version) o1).getReleaseDate().compareTo(((Version) o2).getReleaseDate());
            }
        });
        return versions;
    }

    /**
     * Retrieves the collection of all unreleased versions for given project and narrows it down to only versions that
     * have a release date set to a date that is before releaseDate.
     *
     * @param projectId      project id
     * @param releasedBefore released before date
     * @return collection of unreleased versions for project, never null
     */
    Collection /* <Version> */ getVersionsForProject(Long projectId, Date releasedBefore)
    {
        Collection versions = versionManager.getVersionsUnreleased(projectId, false);
        for (Iterator it = versions.iterator(); it.hasNext();)
        {
            Version version = (Version) it.next();
            final Date releaseDate = version.getReleaseDate();
            if (releaseDate == null || releaseDate.after(releasedBefore))
            {
                it.remove();
            }
        }
        return versions;
    }

    /**
     * Calculate the number of issues for a given Version FixFor.
     *
     * @param version The Version to calculate for.
     * @return The number of Issues with a FixFor of this Version
     * @throws SearchException when underlying search throws an exception
     */
    long getAllIssueCount(Version version) throws SearchException
    {
        Query query = JqlQueryBuilder.newBuilder().where()
                .project(version.getProjectObject().getId())
                .and()
                .fixVersion().eq(version.getId()).buildQuery();

        return searchProvider.searchCount(query, authenticationContext.getUser());

    }

    /**
     * Calculate the number of Unresolved issues for a given Version FixFor.
     *
     * @param version The Version to calculate for.
     * @return The number of Unresolved with a FixFor of this Version
     * @throws SearchException when underlying search throws an exception
     */
    long getUnresolvedIssueCount(Version version) throws SearchException
    {
        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd()
                    .project(version.getProjectObject().getId())
                    .unresolved()
                    .fixVersion().eq(version.getId());
        return searchProvider.searchCount(builder.buildQuery(), authenticationContext.getUser());

    }

    /**
     * Get the GraphModel for a given Version.  This will calculate the percentage of issues resolved.
     * This method gets called from rendering template
     *
     * @param version The Version to calculate percentages for.
     * @return model representing a resolution bar graph.
     * @throws SearchException when search throws an error.
     */
    public PercentageGraphModel getGraphModelForVersion(Version version) throws SearchException
    {
        final long allIssuesSize = getAllIssueCount(version);

        if (allIssuesSize == 0)
        {
            return new PercentageGraphModel();
        }

        final long openIssues = getUnresolvedIssueCount(version);

        PercentageGraphModel model = new PercentageGraphModel();
        model.addRow("#009900", allIssuesSize - openIssues, getText("common.concepts.resolved.issues"), getNavigatorUrl(version.getProjectObject(), version, false));
        model.addRow("#cc0000", openIssues, getText("common.concepts.unresolved.issues"), getNavigatorUrl(version.getProjectObject(), version, true));

        return model;

    }

    public String getNavigatorUrl(final Project project, final Version version, final boolean unresolved)
    {
        final JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder().defaultAnd();
        if (project != null)
        {
            builder.project(project.getKey());
        }
        if (version != null)
        {
            builder.fixVersion(version.getName());
        }
        if (!unresolved)
        {
            List<String> resolutions = new ArrayList<String>();
            for (Resolution resolution : constantsManager.getResolutionObjects())
            {
                resolutions.add(resolution.getName());
            }
            if (!resolutions.isEmpty())
            {
                builder.resolution().in(resolutions.toArray(new String[resolutions.size()])).buildClause();
            }
        }
        else
        {
            builder.unresolved();
        }
        return searchService.getQueryString(authenticationContext.getUser(), builder.buildQuery());
    }

    /**
     * Create a ResolutionParameter that retreives all issues with a resolution.
     *
     * @return ResolutionPrameter containing all resolutions
     */
    TerminalClause createResolutionClause()
    {
        List<Operand> resolutionOperands = new ArrayList<Operand>();
        for (Iterator longIterator = constantsManager.getResolutionObjects().iterator(); longIterator.hasNext();)
        {
            Resolution resolution = (Resolution) longIterator.next();
            resolutionOperands.add(new SingleValueOperand(new Long(resolution.getId())));
        }
        if (resolutionOperands.size() == 1)
        {
            return new TerminalClauseImpl(DocumentConstants.ISSUE_RESOLUTION, Operator.EQUALS, resolutionOperands.get(0));
        }
        else if (resolutionOperands.size() > 1)
        {
            return new TerminalClauseImpl(DocumentConstants.ISSUE_RESOLUTION, Operator.IN, new MultiValueOperand(resolutionOperands));
        }

        return null;
    }

    /**
     * Get the Lucene query string for the given Resolution.
     *
     * @param resolutionClause that represents the resolution values
     * @return Query string representation of the resolution
     */
    String getResolutionQueryString(TerminalClause resolutionClause)
    {
        if (resolutionClause != null)
        {
            final User user = authenticationContext.getUser();
            final Query query = new QueryImpl(resolutionClause);
            return searchService.getQueryString(user, query);
        }
        return null;
    }

    /**
     * Get i18n text for key.
     *
     * @param key key to retrieve property for
     * @return i18n string
     */
    public String getText(String key)
    {
        return new I18nBean().getText(key);
    }

}
