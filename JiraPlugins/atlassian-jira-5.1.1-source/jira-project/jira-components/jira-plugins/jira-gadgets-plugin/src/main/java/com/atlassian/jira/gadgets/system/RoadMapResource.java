package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;

import static com.atlassian.jira.datetime.DateTimeStyle.*;
import static java.util.Arrays.asList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST endpoint to retrieve a list of unreleased versions, according to the input criteria. It also provides a REST
 * endpoint to validate the input criteria to make sure specified projects exist, etc.
 *
 * @since v4.0
 */
@Path ("roadmap")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class RoadMapResource extends AbstractResource
{
    public static final int DEFAULT_DAYS_MAX_INCL = 1000;
    public static final int DEFAULT_NUM_MAX_INCL = 50;

    private static final ToStringStyle TO_STRING_STYLE = ToStringStyle.SHORT_PREFIX_STYLE;

    static final String PROJECT_OR_CATEGORY_IDS = "projectsOrCategories";
    static final String DAYS = "days";
    static final String NUM = "num";

    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final VersionManager versionManager;
    private final SearchProvider searchProvider;
    private final SearchService searchService;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private final DateTimeFormatter dateTimeFormatter;

    public RoadMapResource(final JiraAuthenticationContext authCtx, final PermissionManager permissionManager,
            final ProjectManager projectManager, final VersionManager versionManager,
            final SearchProvider searchProvider, final SearchService searchService, VelocityRequestContextFactory velocityRequestContextFactory,
            DateTimeFormatter dateTimeFormatter)
    {
        this.authenticationContext = authCtx;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.versionManager = versionManager;
        this.searchProvider = searchProvider;
        this.searchService = searchService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser() : null;
    }

    /**
     * Validates the given configuration.
     *
     * @param projectsOrCategories the ids of all selected projects and project categories, concatenated by pipe symbole
     *                             (|). A special id value of &quot;allprojects&quot; indicates all projects are
     *                             selected. Project ids must be numeric; otherwise, the id is ignored. Category ids
     *                             must be numeric and prefixed by &quot;cat&quot;; otherwise, the id is ignored. If
     *                             none is selected or one of the selecte projects/categories does not exist, a validate
     *                             error is returned in the response.
     * @param days                 the number of days to cover. If zero, only overdue versions will be returned. Must
     *                             not be negative. If negative or non-numeric, a validation error is returned
     * @param num                  the maximum number of results. Must be positive. if negative, zero or non-numeric, a
     *                             validation error is returned
     * @return a response with status code 200 if validation passes or with status code 400 if fail, in which case,
     *         validation errors are included in the response
     */
    @GET
    @Path ("/validate")
    public Response validate(@QueryParam (PROJECT_OR_CATEGORY_IDS) final String projectsOrCategories,
            @QueryParam (DAYS) @DefaultValue ("30") final String days,
            @QueryParam (NUM) @DefaultValue ("10") final String num)
    {
        final Collection<ValidationError> errors = new ArrayList<ValidationError>();
        validateProjectsAndCategories(projectsOrCategories, errors);
        validateDays(days, errors);
        validateNum(num, errors);

        return createValidationResponse(errors);
    }

    private void validateProjectsAndCategories(String projectsOrCategories, Collection<ValidationError> errors)
    {
        List<String> projectAndCategoryIds = splitProjectAndCategoryIds(projectsOrCategories);
        if (projectAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_PROJECTS))
        {
            return;
        }
        Set<Long> selectedProjectIds = ProjectsAndProjectCategoriesResource.filterProjectIds(projectAndCategoryIds);
        for (Long projectId : selectedProjectIds)
        {
            if (projectManager.getProjectObj(projectId) == null)
            {
                errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.invalid.project"));
            }
        }
        Set<Long> selectedCategoryIds = ProjectsAndProjectCategoriesResource.filterProjectCategoryIds(projectAndCategoryIds);
        for (Long catId : selectedCategoryIds)
        {
            if (projectManager.getProjectCategory(catId) == null)
            {
                errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.invalid.projectCategory"));
            }
        }
        if (selectedProjectIds.isEmpty() && selectedCategoryIds.isEmpty())
        {
            errors.add(new ValidationError(PROJECT_OR_CATEGORY_IDS, "gadget.common.projects.and.categories.none.selected"));
        }
    }

    private int validateDays(String days, Collection<ValidationError> errors)
    {
        try
        {
            final int numberOfDays = Integer.valueOf(days);
            if (numberOfDays < 0)
            {
                errors.add(new ValidationError(DAYS, "gadget.common.negative.days"));
            }
            else if (numberOfDays > DEFAULT_DAYS_MAX_INCL)
            {
                errors.add(new ValidationError(DAYS, "gadget.common.days.overlimit",
                        String.valueOf(DEFAULT_DAYS_MAX_INCL)));
            }
            return numberOfDays;
        }
        catch (NumberFormatException e)
        {
            errors.add(new ValidationError(DAYS, "gadget.common.days.nan"));
        }

        return -1;
    }

    private int validateNum(String num, Collection<ValidationError> errors)
    {
        try
        {
            final int validatedNum = Integer.valueOf(num);
            if (validatedNum <= 0)
            {
                errors.add(new ValidationError(NUM, "gadget.common.num.negative"));
            }
            else if (validatedNum > DEFAULT_NUM_MAX_INCL)
            {
                errors.add(new ValidationError(NUM, "gadget.common.num.overlimit",
                        String.valueOf(DEFAULT_NUM_MAX_INCL)));
            }
            return validatedNum;
        }
        catch (NumberFormatException e)
        {
            errors.add(new ValidationError(NUM, "gadget.common.num.nan"));
        }

        return -1;
    }

    /**
     * Generates the road map data based on the given parameters.
     *
     * @param projectsOrCategories the ids of all selected projects and project categories, concatenated by pipe symbole
     *                             (|). A special id value of &quot;allprojects&quot; indicates all projects are
     *                             selected. Project ids must be numeric; otherwise, the id is ignored. Category ids
     *                             must be numeric and prefixed by &quot;cat&quot;; otherwise, the id is ignored.
     * @param days                 the number of days to cover. If zero, only overdue versions will be returned. Must
     *                             not be negative.
     * @param num                  the maximum number of results. Must be positive.
     * @return a response with status code 200 containing {@link RoadMapData}. If there is an error in the processing, a
     *         response with status code 500 is returned
     */
    @GET
    @Path ("/generate")
    public Response generate(@QueryParam (PROJECT_OR_CATEGORY_IDS) final String projectsOrCategories,
            @QueryParam (DAYS) @DefaultValue ("30") final int days,
            @QueryParam (NUM) @DefaultValue ("10") final int num)
    {
        final Set<Long> projectIds = getProjectIds(splitProjectAndCategoryIds(projectsOrCategories));
        final Calendar periodEnd = Calendar.getInstance();
        periodEnd.add(Calendar.DAY_OF_YEAR, days);
        List<Version> versions = getVersions(projectIds, periodEnd);
        if (versions.size() > num)
        {
            versions = versions.subList(0, num);
        }
        try
        {
            return Response.ok(transform(versions, days, periodEnd)).cacheControl(CacheControl.NO_CACHE).build();
        }
        catch (SearchUnavailableException e)
        {
            if (!e.isIndexingEnabled())
            {
                return createIndexingUnavailableResponse(createIndexingUnavailableMessage());
            }
            else
            {
                throw e;
            }
        }
        catch (SearchException se)
        {
            return Response.status(500).build();
        }
    }

    private String createIndexingUnavailableMessage()
    {
        final String msg1 = authenticationContext.getI18nHelper().getText("gadget.common.indexing");
        String msg2;
        if (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getLoggedInUser()))
        {
            String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.configure",
                    "<a href=\"" + baseUrl + "/secure/admin/jira/IndexAdmin.jspa\">", "</a>");
        }
        else
        {
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.admin");
        }
        return msg1 + " " + msg2;
    }

    private List<String> splitProjectAndCategoryIds(final String projectsOrCategories)
    {
        if (projectsOrCategories == null)
        {
            return Collections.emptyList();
        }
        return asList(projectsOrCategories.split("\\|"));
    }

    private RoadMapData transform(List<Version> source, int days, Calendar periodEnd) throws SearchException
    {
        List<VersionData> versions = new ArrayList<VersionData>(source.size());
        final OutlookDate outlookDate = authenticationContext.getOutlookDate();
        DateTimeFormatter dateFormatter = dateTimeFormatter.withStyle(DATE);

        for (final Version version : source)
        {
            Project srcProj = version.getProjectObject();
            ProjectData targetProj = new ProjectData(srcProj.getId(), srcProj.getKey(), srcProj.getName());

            Query allIssuesQuery = buildAllIssuesForFixVersionQuery(version);
            int all = (int) searchCount(allIssuesQuery);

            Query unresolvedIssuesQuery = buildUnresolvedIssuesForFixVersionQuery(version);
            int unresolved = (int) searchCount(unresolvedIssuesQuery);

            int resolvedPercent;
            int unresolvedPercent;

            if (all == 0)
            {
                resolvedPercent = 100;
                unresolvedPercent = 0;
            }
            else
            {
            // JRA-18876 percentages are just like a currency: they always have to add up to exactly 100 so we can't use rounding or floats.
                // we'll use integer math, determine if we lost any hundreths along the way, and add it back in to the smallest item.
                // this prevents either one from disappearing entirely by ensuring it will have 1% instead of 0%.
                // e.g. 299 resolved issues and 1 unresolved issue. resolved = (29900 / 300) = 99. unresolved = (100 / 300) = 0.
                // but there is (100 - 99) = 1 leftover which will get added back in to unresolved bumping it up to 1.
                resolvedPercent = ((all - unresolved) * 100) / all;
                unresolvedPercent = (unresolved * 100) / all;
                final int leftovers = 100 - (resolvedPercent + unresolvedPercent);
                if (resolvedPercent < unresolvedPercent)
                {
                    resolvedPercent += leftovers;
                }
                else
                {
                    unresolvedPercent += leftovers;
                }
            }

            ResolutionData resolvedData = new ResolutionData(
                    all - unresolved, resolvedPercent,
                    getQueryString(buildResolvedIssuesForFixVersionQuery(version)));
            ResolutionData unresolvedData = new ResolutionData(
                    unresolved, unresolvedPercent,
                    getQueryString(unresolvedIssuesQuery));
            VersionData ver = new VersionData(version.getId(), version.getName(), version.getDescription(),
                    targetProj, dateFormatter.format(version.getReleaseDate()),
                    outlookDate.formatIso8601(version.getReleaseDate()),
                    isOverdue(version), all, resolvedData, unresolvedData);
            versions.add(ver);
        }

        return new RoadMapData(versions, days, dateFormatter.format(periodEnd.getTime()));
    }

    // The following are copied straight from com.atlassian.jira.portal.portlets.RoadMapPortlet,
    // and java5-ed.

    public boolean isOverdue(Version version)
    {
        return new Date().compareTo(version.getReleaseDate()) > 0;
    }

    Set<Long> getProjectIds(List<String> projectAndCategoryIds)
    {
        Set<Long> projectIds = new HashSet<Long>();
        if (projectAndCategoryIds != null && !projectAndCategoryIds.isEmpty())
        {
            if (projectAndCategoryIds.contains(ProjectsAndProjectCategoriesResource.ALL_PROJECTS))
            {
                // add all projects this user can see
                projectIds.addAll(getAllBrowsableProjects());
            }
            else
            {
                // add projects
                projectIds.addAll(ProjectsAndProjectCategoriesResource.filterProjectIds(projectAndCategoryIds));

                // get categories
                final Set<Long> categoryIds = ProjectsAndProjectCategoriesResource.filterProjectCategoryIds(projectAndCategoryIds);
                for (final Long categoryId : categoryIds)
                {
                    projectIds.addAll(getProjectIdsForCategory(categoryId));
                }

                projectIds = filterProjectsByPermission(projectIds);
            }
        }
        return projectIds;
    }

    private Set<Long> getAllBrowsableProjects()
    {
        return extractProjectIdsFromProjects(permissionManager.getProjectObjects(Permissions.BROWSE, authenticationContext.getLoggedInUser()));
    }

    Set<Long> filterProjectsByPermission(Set<Long> projectIds)
    {
        final Set<Long> filtered = new HashSet<Long>();
        for (final Long projectId : projectIds)
        {
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
        // if project does not exist, simply return false
        return project != null && permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getLoggedInUser());
    }

    /**
     * Collect and return ids of all projects that relate to this project category.
     *
     * @param categoryId category id
     * @return set of project ids, never null
     */
    Set<Long> getProjectIdsForCategory(Long categoryId)
    {
        Collection<Project> projs = projectManager.getProjectObjectsFromProjectCategory(categoryId);
        return extractProjectIdsFromProjects(projs);
    }

    private Set<Long> extractProjectIdsFromProjects(Collection<Project> projs)
    {
        Set<Long> projectIds = new HashSet<Long>();
        for (Project proj : projs)
        {
            projectIds.add(proj.getId());
        }
        return projectIds;
    }

    /**
     * Retrieves a List of Versions for a given set of projects.  Versions must have a release date before that of the
     * passed in date.
     *
     * @param projectIds The projects to retrieve the Versions for.
     * @param cal        The date which the Version must be released before
     * @return Versions that will be released before the given date
     */
    List<Version> getVersions(Set<Long> projectIds, Calendar cal)
    {
        List<Version> versions = new ArrayList<Version>();
        for (Long projectId : projectIds)
        {
            versions.addAll(getVersionsForProject(projectId, cal.getTime()));
        }

        Collections.sort(versions, new Comparator<Version>()
        {
            public int compare(Version v1, Version v2)
            {
                return v1.getReleaseDate().compareTo(v2.getReleaseDate());
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
    Collection<Version> getVersionsForProject(Long projectId, Date releasedBefore)
    {
        Collection<Version> versions = versionManager.getVersionsUnreleased(projectId, false);
        Collection<Version> result = new ArrayList<Version>(versions.size());
        for (Version version : versions)
        {
            final Date releaseDate = version.getReleaseDate();
            if (releaseDate != null && !releaseDate.after(releasedBefore))
            {
                result.add(version);
            }
        }
        return result;
    }

    long searchCount(final Query query) throws SearchException
    {
        return searchProvider.searchCount(query, authenticationContext.getLoggedInUser());
    }

    Query buildAllIssuesForFixVersionQuery(final Version version)
    {
        return JqlQueryBuilder.newBuilder().where()
                .project(version.getProjectObject().getName())
                .and()
                .fixVersion().eq(version.getName()).buildQuery();
    }

    Query buildUnresolvedIssuesForFixVersionQuery(final Version version)
    {
        return JqlQueryBuilder.newBuilder().where().defaultAnd()
                .project(version.getProjectObject().getName())
                .fixVersion().eq(version.getName())
                .resolution().isEmpty()
                .buildQuery();
    }

    Query buildResolvedIssuesForFixVersionQuery(final Version version)
    {
        return JqlQueryBuilder.newBuilder().where().defaultAnd()
                .project(version.getProjectObject().getName())
                .fixVersion().eq(version.getName())
                .resolution().isNotEmpty()
                .buildQuery();
    }

    /**
     * Get the Lucene query string for the given Resolution.
     *
     * @param query that represents the resolution values
     * @return Query string representation of the resolution
     */
    String getQueryString(Query query)
    {
        if (query != null)
        {
            final User user = authenticationContext.getLoggedInUser();
            return searchService.getQueryString(user, query);
        }
        return null;
    }

    ///CLOVER:OFF
    /**
     * The data structure of the road map generated by the {@link com.atlassian.jira.gadgets.system.RoadMapResource}.
     * <p/>
     * It contains the number of days covered in the period, the formatted end date of the period and a collection of
     * unreleased versions, sorted by scheduled release dates.
     */
    @XmlRootElement
    public static class RoadMapData
    {
        @XmlElement
        private int days;

        @XmlElement
        private String dateBefore;

        @XmlElement
        private Collection<VersionData> versions;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private RoadMapData()
        {}

        public RoadMapData(final Collection<VersionData> versions, final int days, final String dateBefore)
        {
            this.versions = versions;
            this.days = days;
            this.dateBefore = dateBefore;
        }

        public int getDays()
        {
            return days;
        }

        public String getDateBefore()
        {
            return dateBefore;
        }

        public Collection<VersionData> getVersions()
        {
            return Collections.unmodifiableCollection(versions);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, TO_STRING_STYLE);
        }
    }

    /**
     * The data structure of a project version contained in the road map data.
     * <p/>
     * It contains:<ul> <li>The id of the version</li> <li>The name of the version</li> <li>The description of the
     * version</li> <li>The project to which the version belongs</li> <li>The scheduled release date formatted according
     * to the general settings</li> <li>The scheduled release date formatted in ISO-8601</li> <li>Whether the version is
     * overdue</li> <li>The count of all issues</li> <li>The data on resolved issues</li> <li>The data on unresolved
     * issues</li> </ul>
     */
    @XmlRootElement
    public static class VersionData
    {
        @XmlElement
        private long id;

        @XmlElement
        private String name;

        @XmlElement
        private String description;

        @XmlElement
        private ProjectData owningProject;

        @XmlElement
        private String releaseDate;

        @XmlElement
        private String releaseDateIso8601;

        @XmlElement
        private boolean isOverdue;

        @XmlElement
        private int allCount;

        @XmlElement
        private ResolutionData resolved;

        @XmlElement
        private ResolutionData unresolved;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private VersionData()
        {}

        VersionData(final long id, final String name, final String description, final ProjectData owningProject,
                final String releaseDate, final String releaseDateIso8601, final boolean overdue, final int allCount,
                final ResolutionData resolved, final ResolutionData unresolved)
        {
            this.id = id;
            this.name = name;
            this.description = description;
            this.owningProject = owningProject;
            this.releaseDate = releaseDate;
            this.releaseDateIso8601 = releaseDateIso8601;
            isOverdue = overdue;
            this.allCount = allCount;
            this.resolved = resolved;
            this.unresolved = unresolved;
        }

        public long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public ProjectData getOwningProject()
        {
            return owningProject;
        }

        public String getReleaseDate()
        {
            return releaseDate;
        }

        public String getReleaseDateIso8601()
        {
            return releaseDateIso8601;
        }

        public boolean isOverdue()
        {
            return isOverdue;
        }

        public int getAllCount()
        {
            return allCount;
        }

        public ResolutionData getResolved()
        {
            return resolved;
        }

        public ResolutionData getUnresolved()
        {
            return unresolved;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, TO_STRING_STYLE);
        }
    }

    /**
     * The data structure of statistics on resolved/unresolved, with jql clause used to retrieve the set of the issues.
     * <p/>
     * It contains:<ul> <li>The count of issues (resolved/unresolved)</li> <li>The rounded percentage of the issues
     * against the count of all issues (in the same version) multiplied by 100</li> <li>The jql query to retrieve the
     * set of the data</li> </ul>
     */
    @XmlRootElement
    public static class ResolutionData
    {
        @XmlElement
        private int count;

        @XmlElement
        private int percentage;

        @XmlElement
        private String issueNavigatorUrl;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private ResolutionData()
        {}

        ResolutionData(final int count, final int percentage, final String jqlQuery)
        {
            this.count = count;
            this.percentage = percentage;
            this.issueNavigatorUrl = "/secure/IssueNavigator.jspa?reset=true" + jqlQuery;
        }

        public int getCount()
        {
            return count;
        }

        public int getPercentage()
        {
            return percentage;
        }

        public String getIssueNavigatorUrl()
        {
            return issueNavigatorUrl;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, TO_STRING_STYLE);
        }
    }

    /**
     * Contains the data of a project a version belongs to, including:<ul> <li>The id of the project</li> <li>The
     * project key</li> <li>The project name</li> </ul>
     */
    @XmlRootElement
    public static class ProjectData
    {

        @XmlElement
        private long id;

        @XmlElement
        private String key;

        @XmlElement
        private String name;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private ProjectData()
        {}

        ProjectData(final long id, String key, String name)
        {
            this.id = id;
            this.key = key;
            this.name = name;
        }

        public long getId()
        {
            return id;
        }

        public String getKey()
        {
            return key;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, TO_STRING_STYLE);
        }
    }
    ///CLOVER:ON
}
