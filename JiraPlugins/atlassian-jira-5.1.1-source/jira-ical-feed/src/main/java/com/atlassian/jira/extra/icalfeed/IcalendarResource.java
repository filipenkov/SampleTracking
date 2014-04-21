package com.atlassian.jira.extra.icalfeed;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.extra.icalfeed.service.EntityAsEventService;
import com.atlassian.jira.extra.icalfeed.util.QueryUtil;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.impl.DateCFType;
import com.atlassian.jira.issue.customfields.impl.DateTimeCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.component.jql.AutoCompleteJsonGenerator;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.extensions.property.WrCalDesc;
import net.fortuna.ical4j.extensions.property.WrCalName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.XProperty;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Path("ical")
@AnonymousAllowed
public class IcalendarResource
{
    private static final Logger LOG = LoggerFactory.getLogger(IcalendarResource.class);

    private static final Collection<String> BUILT_IN_SYSTEM_FIELD_KEYS = Collections.unmodifiableSet(
            new HashSet<String>(
                    Arrays.asList(
                            IssueFieldConstants.CREATED,
                            IssueFieldConstants.UPDATED,
                            IssueFieldConstants.DUE_DATE,
                            IssueFieldConstants.RESOLUTION
                    )
            )
    );

    private String CALENDAR_PRODUCT_ID = "-//Atlassian JIRA//iCalendar Plugin 1.0//EN";

    private static final String X_PROP_ISSUE_DATE_FIELD_NAME = "X-JIRA-ISSUE-DATE-FIELD";

    private static final String X_PARAM_ISSUE_DATE_FIELD_KEY = "X-JIRA-ISSUE-DATE-FIELD-KEY";

    private static final String X_PARAM_UID_STATIC = "X-JIRA-UID-STATIC";

    private static final String X_PROP_ISSUE_KEY = "X-JIRA-ISSUE-KEY";

    private static final String X_PROP_ISSUE_TYPE = "X-JIRA-TYPE";

    private static final String X_PROP_ISSUE_TYPE_ICON = "X-JIRA-TYPE-ICON-URL";

    private static final String X_PROP_ISSUE_STATUS = "X-JIRA-STATUS";

    private static final String X_PROP_ISSUE_STATUS_ICON = "X-JIRA-STATUS-ICON-URL";

    private static final String X_PROP_ISSUE_ASSIGNEE = "X-JIRA-ASSIGNEE";

    private static final String X_PROP_ISSUE_ASSIGNEE_ID = "X-JIRA-ASSIGNEE-ID";

    private static final String X_PROP_VERSION_RELEASED = "X-JIRA-VERSION-RELEASED";

    private static final String X_PROP_VERSION_ICON_URL = "X-JIRA-VERSION-ICON-URL";

    private static final String X_PROP_VERSION_SUMMARY_URL = "X-JIRA-VERSION-SUMMARY-URL";

    private static final String X_PROP_VERSION_ISSUES_URL = "X-JIRA-ISSUES-URL";

    private static final String X_PROP_VERSION_RELEASE_NOTES_URL = "X-JIRA-VERSION-RELEASE-NOTES-URL";

    private static final String X_PROP_VERSION_PROJECT_NAME = "X-JIRA-PROJECT-NAME";

    private final ApplicationProperties applicationProperties;

    private final JiraAuthenticationContext jiraAuthenticationContext;

    private final EntityAsEventService entityAsEventService;

    private final SearchService searchService;

    private final TimeZoneRegistry timeZoneRegistry;

    private final FieldManager fieldManager;

    private final CustomFieldManager customFieldManager;

    private final QueryUtil queryUtil;

    private final ProjectManager projectManager;

    private final PermissionManager permissionManager;

    private final SearchRequestService searchRequestService;

    private final AutoCompleteJsonGenerator autoCompleteJsonGenerator;

    public IcalendarResource(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext, EntityAsEventService entityAsEventService, SearchService searchService, FieldManager fieldManager, CustomFieldManager customFieldManager, QueryUtil queryUtil, ProjectManager projectManager, PermissionManager permissionManager, SearchRequestService searchRequestService)
    {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.entityAsEventService = entityAsEventService;
        this.searchService = searchService;
        this.fieldManager = fieldManager;
        this.customFieldManager = customFieldManager;
        this.queryUtil = queryUtil;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
        this.searchRequestService = searchRequestService;
        this.autoCompleteJsonGenerator = ComponentManager.getComponentInstanceOfType(AutoCompleteJsonGenerator.class);

        timeZoneRegistry = TimeZoneRegistryFactory.getInstance().createRegistry();
    }

    @Path("project/{projectKey}/events.ics")
    @GET
    @Produces("text/calendar")
    public Response getIcalendarByProject(@PathParam("projectKey") String projectKey, @QueryParam("dateFieldName") List<String> dateFieldNames, @QueryParam("includeFixVersions") boolean includeFixVersions)
    {
        try
        {
            if (StringUtils.isBlank(projectKey))
                return Response.status(Response.Status.BAD_REQUEST).build();

            return createIcalendarResponse(
                    search(JqlQueryBuilder.newBuilder().where().project(projectKey).buildQuery(), dateFieldNames, includeFixVersions, jiraAuthenticationContext.getLoggedInUser())
            );
        }
        catch (Exception generalError)
        {
            LOG.error("Unable to export issues to iCalendar", generalError);
            return Response.serverError().build();
        }
    }

    @Path("search/jql/events.ics")
    @GET
    @Produces("text/calendar")
    public Response getIcalendarByJql(@QueryParam("jql") String jql, @QueryParam("dateFieldName") List<String> dateFieldNames, @QueryParam("includeFixVersions") boolean includeFixVersions)
    {
        try
        {
            if (StringUtils.isBlank(jql))
                return Response.status(Response.Status.BAD_REQUEST).build();

            User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
            SearchService.ParseResult jqlParseResult = searchService.parseQuery(loggedInUser, StringUtils.defaultString(jql));

            return jqlParseResult.isValid()
                    ? createIcalendarResponse(search(jqlParseResult.getQuery(), dateFieldNames, includeFixVersions, loggedInUser))
                    : Response.status(Response.Status.BAD_REQUEST).build();
        }
        catch (Exception generalError)
        {
            LOG.error("Unable to export issues to iCalendar", generalError);
            return Response.serverError().build();
        }
    }
    
    @Path("search/filter/events.ics")
    @GET
    @Produces("text/calendar")
    public Response getIcalendarByJql(@QueryParam("searchFilterId") long searchFilterId, @QueryParam("dateFieldName") List<String> dateFieldNames, @QueryParam("includeFixVersions") boolean includeFixVersions)
    {
        try
        {
            User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
            SearchRequest searchRequest = searchRequestService.getFilter(new JiraServiceContextImpl(loggedInUser), searchFilterId);

            if (null == searchRequest)
                return Response.status(Response.Status.BAD_REQUEST).build();

            return createIcalendarResponse(search(searchRequest.getQuery(), dateFieldNames, includeFixVersions, loggedInUser));
        }
        catch (Exception generalError)
        {
            LOG.error("Unable to export issues to iCalendar", generalError);
            return Response.serverError().build();
        }
    }

    private Response createIcalendarResponse(Calendar calendar) throws IOException, ValidationException
    {
        Writer iCalendarWriter = new StringWriter();
        new CalendarOutputter(false).output(calendar, iCalendarWriter);

        return Response.ok(iCalendarWriter.toString()).build();
    }

    private Calendar search(Query query, Collection<String> dateFieldNames, boolean includeFixVersions, User user) throws SearchException, ParseException, MalformedURLException, URISyntaxException
    {
        return toIcalendar(entityAsEventService.search(query, new HashSet<String>(null == dateFieldNames ? Collections.<String>emptySet() : dateFieldNames), includeFixVersions, user));
    }

    private Calendar toIcalendar(EntityAsEventService.Result result)
            throws ParseException, MalformedURLException, URISyntaxException
    {
        ComponentList iCalendarComponents = new ComponentList();

        TimeZone timeZone = timeZoneRegistry.getTimeZone(TimeZone.getDefault().getID());
        VTimeZone vTimeZone = timeZone.getVTimeZone();
        iCalendarComponents.add(vTimeZone);

        PropertyList iCalendarProps = new PropertyList();
        iCalendarProps.add(new ProdId(CALENDAR_PRODUCT_ID));
        iCalendarProps.add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
        iCalendarProps.add(CalScale.GREGORIAN);
        iCalendarProps.add(new WrCalName(new ParameterList(), WrCalName.FACTORY, StringUtils.defaultString(applicationProperties.getDisplayName())));
        iCalendarProps.add(new WrCalDesc(new ParameterList(), WrCalDesc.FACTORY, StringUtils.defaultString(applicationProperties.getDisplayName())));
        iCalendarProps.add(new XProperty("X-WR-TIMEZONE", new ParameterList(), vTimeZone.getTimeZoneId().getValue()));

        DateTimeFormatter allDayDateValueFormatter = DateTimeFormat.forPattern("yyyyMMdd");
        StringBuilder urlBuilder = new StringBuilder();

        for (EntityAsEventService.IssueDateResult issueDateResult : result.issues)
            iCalendarComponents.add(toVEvent(allDayDateValueFormatter, urlBuilder, issueDateResult, timeZone));

        for (Version affectedVersion : result.affectedVersions)
            iCalendarComponents.add(toVEvent(allDayDateValueFormatter, urlBuilder, affectedVersion));

        for (Version fixVersion : result.fixedVersions)
            iCalendarComponents.add(toVEvent(allDayDateValueFormatter, urlBuilder, fixVersion));


        return new Calendar(iCalendarProps, iCalendarComponents);
    }

    private VEvent toVEvent(DateTimeFormatter allDayDateValueFormatter, StringBuilder urlBuilder, Version version)
            throws ParseException, URISyntaxException, MalformedURLException
    {
        PropertyList vEventProps = new PropertyList();

        urlBuilder.setLength(0);
        urlBuilder.append(String.valueOf(version.getId())).append('@').append(getHostFromBaseUrl());
        vEventProps.add(new Uid(
                new ParameterList()
                {
                    {
                        add(new XParameter(X_PARAM_UID_STATIC, Boolean.TRUE.toString()));
                    }
                },
                urlBuilder.toString()
        ));

        vEventProps.add(new Summary(version.getName()));

        DateTime releaseDate = new DateTime(version.getReleaseDate().getTime());
        vEventProps.add(new DtStart(new net.fortuna.ical4j.model.Date(allDayDateValueFormatter.print(releaseDate))));
        vEventProps.add(new DtEnd(new net.fortuna.ical4j.model.Date(allDayDateValueFormatter.print(releaseDate.plusDays(1)))));
        if (StringUtils.isNotBlank(version.getDescription()))
            vEventProps.add(new Description(version.getDescription()));


        Project versionProject = version.getProjectObject();
        String projectKey = versionProject.getKey();
        String versionUrl = getJiraUrl(urlBuilder, "/browse/", projectKey, "/fixforversion/", String.valueOf(version.getId()));
        vEventProps.add(new Url(new URL(versionUrl).toURI()));

        vEventProps.add(new XProperty(X_PROP_VERSION_PROJECT_NAME, versionProject.getName()));
        boolean isReleased = version.isReleased();
        vEventProps.add(new XProperty(X_PROP_VERSION_RELEASED, String.valueOf(isReleased)));
        vEventProps.add(new XProperty(X_PROP_VERSION_ICON_URL, getJiraUrl(urlBuilder, "/images/icons/", isReleased ? "package_16.gif" : "box_16.gif")));

        vEventProps.add(
                new XProperty(
                        X_PROP_VERSION_SUMMARY_URL,
                        getJiraUrl(urlBuilder, "/browse/", projectKey, "/fixforversion/", String.valueOf(version.getId()), "#selectedTab=com.atlassian.jira.plugin.system.project%3Aversion-summary-panel")
                )
        );
        vEventProps.add(
                new XProperty(
                        X_PROP_VERSION_ISSUES_URL,
                        getJiraUrl(urlBuilder, "/browse/", projectKey, "/fixforversion/", String.valueOf(version.getId()), "#selectedTab=com.atlassian.jira.plugin.system.project%3Aversion-issues-panel")
                )
        );
        vEventProps.add(
                new XProperty(
                        X_PROP_VERSION_RELEASE_NOTES_URL,
                        getJiraUrl(urlBuilder, "/secure/ReleaseNote.jspa?projectId=", String.valueOf(versionProject.getId()), "&version=", String.valueOf(version.getId()))
                )
        );

        return new VEvent(vEventProps);
    }

    private String getHostFromBaseUrl() throws MalformedURLException
    {
        return new URL(applicationProperties.getBaseUrl()).getHost();
    }

    private String getJiraUrl(StringBuilder builder, String... parts)
    {
        builder.setLength(0);
        builder.append(applicationProperties.getBaseUrl());

        for (String part : parts)
            builder.append(part);

        return builder.toString();
    }

    private VEvent toVEvent(DateTimeFormatter allDayDateValueFormatter, StringBuilder urlBuilder, final EntityAsEventService.IssueDateResult issueDateResult, TimeZone systemTimeZoneAsCalendarTimeZone)
            throws ParseException, URISyntaxException, MalformedURLException
    {
        PropertyList vEventProps = new PropertyList();

        urlBuilder.setLength(0);
        urlBuilder.append(issueDateResult.issueKey).append('-').append(issueDateResult.dateFieldKey).append('@').append(getHostFromBaseUrl());
        vEventProps.add(new Uid(
                new ParameterList()
                {
                    {
                        add(new XParameter(X_PARAM_UID_STATIC, Boolean.TRUE.toString()));
                    }
                },
                urlBuilder.toString()
        ));

        vEventProps.add(new Summary(issueDateResult.issueSummary));
        vEventProps.add(new XProperty(
                X_PROP_ISSUE_DATE_FIELD_NAME,
                new ParameterList()
                {
                    {
                        add(new XParameter(X_PARAM_ISSUE_DATE_FIELD_KEY, issueDateResult.dateFieldKey));
                    }
                },
                issueDateResult.dateFieldName));

        if (issueDateResult.allDay)
        {
            vEventProps.add(new DtStart(new net.fortuna.ical4j.model.Date(allDayDateValueFormatter.print(issueDateResult.start))));
            vEventProps.add(new DtEnd(new net.fortuna.ical4j.model.Date(allDayDateValueFormatter.print(issueDateResult.end))));
        }
        else
        {
            DtStart dtStart = new DtStart(new net.fortuna.ical4j.model.DateTime(issueDateResult.start.toDate()));
            dtStart.setTimeZone(systemTimeZoneAsCalendarTimeZone);
            vEventProps.add(dtStart);
            
            DtEnd dtEnd = new DtEnd(new net.fortuna.ical4j.model.DateTime(issueDateResult.end.toDate()));
            dtEnd.setTimeZone(systemTimeZoneAsCalendarTimeZone);
            vEventProps.add(dtEnd);
        }

        if (StringUtils.isNotBlank(issueDateResult.issueDescription))
            vEventProps.add(new Description(issueDateResult.issueDescription));

        if (null != issueDateResult.assignee)
        {
            vEventProps.add(new XProperty(X_PROP_ISSUE_ASSIGNEE, issueDateResult.assignee.getDisplayName()));
            vEventProps.add(new XProperty(X_PROP_ISSUE_ASSIGNEE_ID, issueDateResult.assignee.getName()));
        }

        vEventProps.add(new Url(new URL(getJiraUrl(urlBuilder, "/browse/", issueDateResult.issueKey)).toURI()));
        vEventProps.add(new DtStamp(new net.fortuna.ical4j.model.DateTime(issueDateResult.issueCreated.toDate())));
        vEventProps.add(new Created(new net.fortuna.ical4j.model.DateTime(issueDateResult.issueCreated.toDate())));
        vEventProps.add(new LastModified(new net.fortuna.ical4j.model.DateTime(issueDateResult.issueUpdated.toDate())));

        // JIRA X-properties
        vEventProps.add(new XProperty(X_PROP_ISSUE_KEY, issueDateResult.issueKey));
        vEventProps.add(new XProperty(X_PROP_ISSUE_TYPE, issueDateResult.type));
        vEventProps.add(new XProperty(X_PROP_ISSUE_TYPE_ICON, getJiraUrl(urlBuilder, issueDateResult.typeIconUrl)));
        vEventProps.add(new XProperty(X_PROP_ISSUE_STATUS, issueDateResult.status));
        vEventProps.add(new XProperty(X_PROP_ISSUE_STATUS_ICON, getJiraUrl(urlBuilder, issueDateResult.statusIconUrl)));

        return new VEvent(vEventProps);
    }

    @Path("config/fields")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDateFields(@QueryParam("jql") String jql, @QueryParam("searchRequestId") long searchRequestId)
    {
        User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        Query searchQuery;
        
        if (0 == searchRequestId)
        {
            if (StringUtils.isBlank(jql))
                return Response.status(Response.Status.BAD_REQUEST).build();

            SearchService.ParseResult jqlParseResult = searchService.parseQuery(loggedInUser, StringUtils.defaultString(jql));

            if (!jqlParseResult.isValid())
                return Response.status(Response.Status.BAD_REQUEST).build();

            searchQuery = jqlParseResult.getQuery();
        }
        else
        {
            SearchRequest searchRequest = searchRequestService.getFilter(new JiraServiceContextImpl(loggedInUser), searchRequestId);
            if (null == searchRequest)
                return Response.status(Response.Status.BAD_REQUEST).build();

            searchQuery = searchRequest.getQuery();
        }

        Set<String> fieldKeys = new HashSet<String>(BUILT_IN_SYSTEM_FIELD_KEYS);

        Collection<CustomField> globalCustomFields = customFieldManager.getGlobalCustomFieldObjects();
        if (null != globalCustomFields)
            for (CustomField globalCustomField : Collections2.filter(globalCustomFields, new IsDateCustomFieldPredicate()))
                fieldKeys.add(globalCustomField.getId());

        Collection<Project> browseableProjectsInQuery = queryUtil.getBrowseableProjectsFromQuery(jiraAuthenticationContext.getLoggedInUser(), searchQuery);
        // Only show project level custom fields if there is one project in the query.
        if (browseableProjectsInQuery.size() == 1)
            for (Project browseableProject : browseableProjectsInQuery)
            {
                Collection<CustomField> customFieldsInProject = customFieldManager.getCustomFieldObjects(browseableProject.getId(), ConstantsManager.ALL_ISSUE_TYPES);
                if (null != customFieldsInProject)
                    for (CustomField customField : Collections2.filter(customFieldsInProject, new IsDateCustomFieldPredicate()))
                        fieldKeys.add(customField.getId());
            }

        return Response.ok(new ArrayList<DateField>(Lists.transform(
                new ArrayList<String>(fieldKeys),
                new Function<String, DateField>()
                {
                    @Override
                    public DateField apply(String fieldKey)
                    {
                        Field aField = fieldManager.getField(fieldKey);

                        DateField aDateField = new DateField();
                        aDateField.name = aField.getName();
                        aDateField.key = aField.getId();

                        return aDateField;
                    }
                }
        ))).build();
    }

    @XmlRootElement
    public static class DateField
    {
        @XmlElement
        public String key;

        @XmlElement
        public String name;
    }

    private static class IsDateCustomFieldPredicate implements Predicate<CustomField>
    {
        @Override
        public boolean apply(CustomField field)
        {
            return null != field && (
                    field.getCustomFieldType() instanceof DateTimeCFType
                            || field.getCustomFieldType() instanceof DateCFType
            );
        }
    }

    @Path("config/query/options")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueryOptions() throws JSONException
    {
        final User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        List<SimpleProject> simpleProjects = Lists.newArrayList(
                Collections2.transform(
                        Collections2.filter(
                                projectManager.getProjectObjects(),
                                Predicates.and(
                                        Predicates.notNull(),
                                        new Predicate<Project>()
                                        {
                                            @Override
                                            public boolean apply(Project project)
                                            {
                                                return permissionManager.hasPermission(Permissions.BROWSE, project, loggedInUser);
                                            }
                                        }
                                )
                        ),
                        new Function<Project, SimpleProject>()
                        {
                            @Override
                            public SimpleProject apply(Project project)
                            {
                                SimpleProject simpleProject = new SimpleProject();
                                simpleProject.key = project.getKey();
                                simpleProject.name = project.getName();

                                return simpleProject;
                            }
                        }
                )
        );
        Collections.sort(simpleProjects);

        List<SearchFilter> searchFilters = Lists.newArrayList(
                Collections2.transform(
                        getSearchFiltersForUser(loggedInUser),
                        new Function<SearchRequest, SearchFilter>()
                        {
                            @Override
                            public SearchFilter apply(SearchRequest searchRequest)
                            {
                                SearchFilter searchFilter = new SearchFilter();
                                searchFilter.id = searchRequest.getId();
                                searchFilter.name = searchRequest.getName();
                                searchFilter.description = searchRequest.getDescription();

                                return searchFilter;
                            }
                        }
                )
        );
        Collections.sort(searchFilters);

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.projects = simpleProjects;
        queryOptions.searchFilters = searchFilters;

        Locale userLocale = jiraAuthenticationContext.getLocale();
        queryOptions.visibleFieldNames = autoCompleteJsonGenerator.getVisibleFieldNamesJson(loggedInUser, userLocale);
        queryOptions.visibleFunctionNamesJson = autoCompleteJsonGenerator.getVisibleFunctionNamesJson(loggedInUser, userLocale);
        queryOptions.jqlReservedWordsJson = autoCompleteJsonGenerator.getJqlReservedWordsJson();

        return Response.ok(queryOptions).build();
    }

    @Path("util/jql/validate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateJql(@FormParam("jql") final String jql)
    {
        User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        SearchService.ParseResult jqlParseResult = searchService.parseQuery(loggedInUser, StringUtils.defaultString(jql));
        JqlValidationErrors jqlValidationErrors = new JqlValidationErrors();

        if (jqlParseResult.isValid())
        {
            return Response.ok(jqlValidationErrors).build();
        }
        else
        {
            MessageSet validationMessageSet = jqlParseResult.getErrors();

            jqlValidationErrors.errorMessages = validationMessageSet.getErrorMessages();
            jqlValidationErrors.warningMessages = validationMessageSet.getWarningMessages();

            return Response.ok(jqlValidationErrors).build();
        }
    }

    private Collection<SearchRequest> getSearchFiltersForUser(User loggedInUser)
    {
        return searchRequestService.search(
                new JiraServiceContextImpl(loggedInUser),
                prepareSharedEntitySearchParametersBuilder(new SharedEntitySearchParametersBuilder()).toSearchParameters(),
                0,
                Integer.MAX_VALUE
        ).getResults();
    }

    /**
     * This method is only required for backwards compatibility with JIRA 4.3, in which {@code SharedEntitySearchParametersBuilder.setEntitySearchContext} could not be found.
     *
     * @param sharedEntitySearchParametersBuilder
     * The instance of {@code SharedEntitySearchParametersBuilder} to set a search context to. Cannot be {@code null}
     * @return
     * The same instance of {@code SharedEntitySearchParametersBuilder} passed into this method.
     */
    private SharedEntitySearchParametersBuilder prepareSharedEntitySearchParametersBuilder(SharedEntitySearchParametersBuilder sharedEntitySearchParametersBuilder)
    {
        try
        {
            Class sharedEntitySearchContextClazz = getClass().getClassLoader().loadClass("com.atlassian.jira.sharing.search.SharedEntitySearchContext");
            java.lang.reflect.Field useField = sharedEntitySearchContextClazz.getField("USE");

            if (Modifier.isStatic(useField.getModifiers()))
            {
                Method setEntitySearchContextMethod = sharedEntitySearchParametersBuilder.getClass().getMethod("setEntitySearchContext", sharedEntitySearchContextClazz);
                setEntitySearchContextMethod.invoke(sharedEntitySearchParametersBuilder, useField.get(null));
            }
        }
        catch (ClassNotFoundException sharedEntitySearchContextDoesNotExist)
        {
            LOG.debug("Unable to find class SharedEntitySearchContext", sharedEntitySearchContextDoesNotExist);
        }
        catch (NoSuchFieldException useFieldNotFound)
        {
            LOG.debug("Unable to find static field \"USE\" in SharedEntitySearchContext", useFieldNotFound);
        }
        catch (NoSuchMethodException setEntitySearchContextMethodNotFound)
        {
            LOG.debug("Unable to find SharedEntitySearchParametersBuilder.setEntitySearchContext()", setEntitySearchContextMethodNotFound);
        }
        catch (IllegalAccessException unableToSetSearchContext)
        {
            LOG.debug("Invalid access to SharedEntitySearchParametersBuilder.setEntitySearchContext()", unableToSetSearchContext);
        }
        catch (InvocationTargetException setSearchContextError)
        {
            LOG.debug("Error invoking SharedEntitySearchParametersBuilder.setEntitySearchContext()", setSearchContextError);
        }

        return sharedEntitySearchParametersBuilder;
    }

    @XmlRootElement
    public static class JqlValidationErrors
    {
        @XmlElement
        public Set<String> errorMessages;

        @XmlElement
        public Set<String> warningMessages;
    }

    @XmlRootElement
    public static class QueryOptions
    {
        @XmlElement
        public List<SimpleProject> projects;

        @XmlElement
        public List<SearchFilter> searchFilters;
        
        @XmlElement
        public String visibleFieldNames;
        
        @XmlElement
        public String visibleFunctionNamesJson;
        
        @XmlElement
        public String jqlReservedWordsJson;
    }

    @XmlRootElement
    public static class SimpleProject implements Comparable<SimpleProject>
    {
        @XmlElement
        public String key;

        @XmlElement
        public String name;

        @Override
        public int compareTo(SimpleProject simpleProject)
        {
            int result = StringUtils.defaultString(name).compareTo(StringUtils.defaultString(simpleProject.name));
            return 0 == result
                    ? key.compareTo(simpleProject.key)
                    : result;
        }
    }

    @XmlRootElement
    public static class SearchFilter implements Comparable<SearchFilter>
    {
        @XmlElement
        public long id;

        @XmlElement
        public String name;
        
        @XmlElement
        public String description;

        @Override
        public int compareTo(SearchFilter searchFilter)
        {
            int result = StringUtils.defaultString(name).compareTo(StringUtils.defaultString(searchFilter.name));
            return 0 == result
                    ? (int) (id - searchFilter.id)
                    : result;
        }
    }
}
