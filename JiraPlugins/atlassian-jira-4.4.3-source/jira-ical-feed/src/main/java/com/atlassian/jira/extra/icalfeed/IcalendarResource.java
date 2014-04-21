package com.atlassian.jira.extra.icalfeed;


import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.extra.icalfeed.service.EntityAsEventService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.query.Query;
import com.atlassian.sal.api.ApplicationProperties;
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
import net.fortuna.ical4j.util.HostInfo;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

@Path("ical")
@AnonymousAllowed
public class IcalendarResource
{
    private static final Logger LOG = LoggerFactory.getLogger(IcalendarResource.class);

    private String CALENDAR_PRODUCT_ID = "-//Atlassian JIRA//iCalendar Plugin 1.0//EN";

    private static final String X_PROP_ISSUE_KEY = "X-JIRA-ISSUE-KEY";

    private static final String X_PROP_ISSUE_TYPE = "X-JIRA-TYPE";
    
    private static final String X_PROP_ISSUE_TYPE_ICON = "X-JIRA-TYPE-ICON-URL";

    private static final String X_PROP_ISSUE_STATUS = "X-JIRA-STATUS";

    private static final String X_PROP_ISSUE_STATUS_ICON = "X-JIRA-STATUS-ICON-URL";

    private static final String X_PROP_ISSUE_ASSIGNEE = "X-JIRA-ASSIGNEE";

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

    private final UidGenerator uidGenerator;

    private final TimeZoneRegistry timeZoneRegistry;

    public IcalendarResource(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext, EntityAsEventService entityAsEventService, SearchService searchService)
    {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.entityAsEventService = entityAsEventService;
        this.searchService = searchService;

        uidGenerator = new UidGenerator(new BaseUrlHostInfo(applicationProperties), String.valueOf(new Random().nextInt()));
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

    @Path("search/events.ics")
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

        VTimeZone vTimeZone = timeZoneRegistry.getTimeZone(TimeZone.getDefault().getID()).getVTimeZone();
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
            iCalendarComponents.add(toVEvent(allDayDateValueFormatter, urlBuilder, issueDateResult));

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

        vEventProps.add(new Uid(uidGenerator.generateUid().getValue()));
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
                        getJiraUrl(urlBuilder, "/browse/", projectKey, "/fixforversion/", String.valueOf(version.getId()),  "#selectedTab=com.atlassian.jira.plugin.system.project%3Aversion-summary-panel")
                )
        );
        vEventProps.add(
                new XProperty(
                        X_PROP_VERSION_ISSUES_URL,
                        getJiraUrl(urlBuilder, "/browse/", projectKey, "/fixforversion/", String.valueOf(version.getId()),  "#selectedTab=com.atlassian.jira.plugin.system.project%3Aversion-issues-panel")
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

    private String getJiraUrl(StringBuilder builder, String ... parts)
    {
        builder.setLength(0);
        builder.append(applicationProperties.getBaseUrl());

        for (String part : parts)
            builder.append(part);

        return builder.toString();
    }

    private VEvent toVEvent(DateTimeFormatter allDayDateValueFormatter, StringBuilder urlBuilder, EntityAsEventService.IssueDateResult issueDateResult)
            throws ParseException, URISyntaxException, MalformedURLException
    {
        PropertyList vEventProps = new PropertyList();

        vEventProps.add(new Uid(uidGenerator.generateUid().getValue()));
        vEventProps.add(new Summary(issueDateResult.issueSummary));

        if (issueDateResult.allDay)
        {
            vEventProps.add(new DtStart(new net.fortuna.ical4j.model.Date(allDayDateValueFormatter.print(issueDateResult.start))));
            vEventProps.add(new DtEnd(new net.fortuna.ical4j.model.Date(allDayDateValueFormatter.print(issueDateResult.end))));
        }
        else
        {
            vEventProps.add(new DtStart(new net.fortuna.ical4j.model.DateTime(allDayDateValueFormatter.print(issueDateResult.start))));
            vEventProps.add(new DtEnd(new net.fortuna.ical4j.model.DateTime(allDayDateValueFormatter.print(issueDateResult.end))));
        }

        if (StringUtils.isNotBlank(issueDateResult.issueDescription))
            vEventProps.add(new Description(issueDateResult.issueDescription));

        if (null != issueDateResult.assignee)
            vEventProps.add(new XProperty(X_PROP_ISSUE_ASSIGNEE, issueDateResult.assignee.getDisplayName()));

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

    private static class BaseUrlHostInfo implements HostInfo
    {
        private static final String LOOPBACK_HOST_NAME = "127.0.0.1";

        private final ApplicationProperties applicationProperties;

        private BaseUrlHostInfo(ApplicationProperties applicationProperties)
        {
            this.applicationProperties = applicationProperties;
        }

        @Override
        public String getHostName()
        {
            try
            {
                return new URL(applicationProperties.getBaseUrl()).getHost();
            }
            catch (MalformedURLException e)
            {
                LOG.error("Unable to get host name from base URL", e);
                return LOOPBACK_HOST_NAME;
            }
        }
    }
}
