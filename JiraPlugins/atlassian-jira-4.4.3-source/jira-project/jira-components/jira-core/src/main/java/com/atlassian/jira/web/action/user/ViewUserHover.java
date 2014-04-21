package com.atlassian.jira.web.action.user;

import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.timezone.TimeZoneInfo;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.query.Query;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.ImmutableList;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import webwork.action.ServletActionContext;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.joda.time.DateTimeConstants.*;

/**
 * Displays the contents of the user hover dialog.
 *
 * @since 4.2
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public class ViewUserHover extends JiraWebActionSupport
{
    static private final ImmutableList<Integer> WEEKEND = ImmutableList.of(SATURDAY, SUNDAY);

    private String username;
    private User user;
    private Long avatarId;

    private final UserUtil userUtil;
    private final EmailFormatter emailFormatter;
    private final SearchService searchService;
    private final PermissionManager permissionManager;
    private final SimpleLinkManager simpleLinkManager;
    private final AvatarManager avatarManager;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final TimeZoneService timeZoneService;
    private final LazyReference<SimpleDateFormat> hourOfDayFormatter = new HourOfDayFormatterRef();

    public ViewUserHover(final UserUtil userUtil, final EmailFormatter emailFormatter,
            final SearchService searchService, final PermissionManager permissionManager,
            final SimpleLinkManager simpleLinkManager, final AvatarManager avatarManager,
            final DateTimeFormatterFactory dateTimeFormatterFactory,
            final TimeZoneService timeZoneService)
    {
        this.userUtil = userUtil;
        this.emailFormatter = emailFormatter;
        this.searchService = searchService;
        this.permissionManager = permissionManager;
        this.simpleLinkManager = simpleLinkManager;
        this.avatarManager = avatarManager;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.timeZoneService = timeZoneService;
    }

    @Override
    public String doDefault() throws Exception
    {
        user = userUtil.getUser(username);
        if (user != null)
        {
            final PropertySet propertySet = user.getPropertySet();
            if (propertySet.exists(AvatarManager.USER_AVATAR_ID_KEY))
            {
                avatarId = propertySet.getLong(AvatarManager.USER_AVATAR_ID_KEY);
            }
        }
        else
        {
            avatarId = avatarManager.getAnonymousAvatarId();
        }

        return SUCCESS;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(final User user)
    {
        this.user = user;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(final String username)
    {
        this.username = username;
    }

    public Long getAvatarId()
    {
        return avatarId;
    }

    public void setAvatarId(final Long avatarId)
    {
        this.avatarId = avatarId;
    }

    public String getAssigneeQuery()
    {
        final Query query = JqlQueryBuilder.newClauseBuilder().assignee().eq(username).
                and().resolution().isEmpty().buildQuery();
        return searchService.getQueryString(getRemoteUser(), query);
    }

    public String getFormattedEmail()
    {
        if (user != null)
        {
            return emailFormatter.formatEmailAsLink(user.getEmail(), getRemoteUser());
        }
        return "";
    }

    public String getTime()
    {
        return dateTimeFormatterFactory.formatter().forLoggedInUser().withStyle(DateTimeStyle.TIME).withZone(getUserTimeZone()).format(new Date());
    }

    public String getDayOfWeek()
    {
        SimpleDateFormat df = new SimpleDateFormat("EEEE", getLocale());
        df.setTimeZone(getUserTimeZone());

        return df.format(new Date());
    }

    /**
     * Returns the hour of day in the current user's time zone, in 24-hour format.
     *
     * @return the hour of day in the current user's time zone
     */
    public String getHourOfDay()
    {
        return hourOfDayFormatter.get().format(new Date());
    }

    public String getTimeZoneCity()
    {
       com.atlassian.crowd.embedded.api.User user = userUtil.getUserObject(this.user.getName());
       TimeZoneInfo userTimeZoneInfo = timeZoneService.getUserTimeZoneInfo(new JiraServiceContextImpl(user));
       return userTimeZoneInfo.getCity();
    }

    @Nullable
    public Boolean getIsWeekend()
    {
        return WEEKEND.contains(new DateTime(new Date(), DateTimeZone.forTimeZone(getUserTimeZone())).getDayOfWeek()) ? Boolean.TRUE : null;
    }

    public boolean hasViewUserPermission()
    {
        //check if the user is allowed to view user profiles and if they can view issues!
        try
        {
            return permissionManager.hasPermission(Permissions.USE, getRemoteUser()) &&
                    permissionManager.hasProjects(Permissions.BROWSE, getRemoteUser());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public SimpleLink getFirstHoverLink()
    {
        final List<SimpleLink> simpleLinkList = getHoverLinks();
        if(!simpleLinkList.isEmpty())
        {
            return simpleLinkList.get(0);
        }
        return null;
    }

    public List<SimpleLink> getRemainingLinks()
    {
        final List<SimpleLink> simpleLinkList = getHoverLinks();
        if(!simpleLinkList.isEmpty())
        {
            return simpleLinkList.subList(1, simpleLinkList.size());
        }
        return Collections.emptyList();
    }

    public List<SimpleLink> getHoverLinks()
    {
        final User remoteUser = getRemoteUser();
        final HttpServletRequest servletRequest = ServletActionContext.getRequest();

        final Map<String, Object> params = MapBuilder.<String, Object>newBuilder().
                add("profileUser", getUsername()).
                add("jqlquery", getAssigneeQuery()).toMap();

        final JiraHelper helper = new JiraHelper(servletRequest, null, params);

        return simpleLinkManager.getLinksForSection("system.user.hover.links", remoteUser, helper);
    }

    public boolean isCurrentUserNoAvatar()
    {
        //if the logged in user is viewing his/her own avatar and they haven't set an avatar yet, return true.
        if(getRemoteUser() != null && user != null && getRemoteUser().equals(user))
        {
            final PropertySet propertySet = user.getPropertySet();
            if (!propertySet.exists(AvatarManager.USER_AVATAR_ID_KEY))
            {
                return true;
            }
        }
        return false;
    }

    protected TimeZone getUserTimeZone()
    {
        com.atlassian.crowd.embedded.api.User user = userUtil.getUserObject(this.user.getName());
        TimeZoneInfo userTimeZoneInfo = timeZoneService.getUserTimeZoneInfo(new JiraServiceContextImpl(user));
        return userTimeZoneInfo.toTimeZone();
    }

    private class HourOfDayFormatterRef extends LazyReference<SimpleDateFormat>
    {
        @Override
        protected SimpleDateFormat create() throws Exception
        {
            TimeZoneInfo userTimeZone = timeZoneService.getUserTimeZoneInfo(new JiraServiceContextImpl(getUser()));

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH");
            dateFormat.setTimeZone(userTimeZone.toTimeZone());
            return dateFormat;
        }
    }
}