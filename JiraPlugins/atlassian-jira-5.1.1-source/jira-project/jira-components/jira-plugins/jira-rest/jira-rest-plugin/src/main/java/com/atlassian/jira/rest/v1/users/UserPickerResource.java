package com.atlassian.jira.rest.v1.users;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DelimeterInserter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * REST end point for searching users in the user picker.
 *
 * @since v4.0
 */
@Path("users/picker")
@AnonymousAllowed
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserPickerResource
{
    private static final Logger log = Logger.getLogger(UserPickerResource.class);

    private final JiraAuthenticationContext authContext;
    private final UserPickerSearchService service;
    private final ApplicationProperties applicationProperties;
    private final AvatarService avatarService;
    private final ContextI18n i18nHelper;

    public UserPickerResource(JiraAuthenticationContext authContext, ContextI18n i18nHelper,
            UserPickerSearchService service, ApplicationProperties applicationProperties,
            AvatarService avatarService)
    {
        this.authContext = authContext;
        this.service = service;
        this.applicationProperties = applicationProperties;
        this.avatarService = avatarService;
        this.i18nHelper = i18nHelper;
    }

    @GET
    public Response getUsersResponse(@QueryParam("fieldName") final String fieldName,
                                     @QueryParam("query") final String query,
                                     @QueryParam("showAvatar") final boolean showAvatar,
                                     @QueryParam("exclude") final List<String> excludeUsers)
    {
        return Response.ok(getUsers(fieldName, query, showAvatar, excludeUsers)).cacheControl(NO_CACHE).build();
    }

    UserPickerResultsWrapper getUsers(final String fieldName, final String query, final boolean showAvatar, List<String> excludeUsers)
    {
        final JiraServiceContext jiraServiceCtx = getContext();
        final UserPickerResultsWrapper results = new UserPickerResultsWrapper();

        if (excludeUsers == null) {
            excludeUsers = new ArrayList<String>();
        }
        
        if (!service.canPerformAjaxSearch(jiraServiceCtx))
            return results;

        final boolean canShowEmailAddresses = service.canShowEmailAddresses(jiraServiceCtx);
        final Collection<User> users = service.findUsers(jiraServiceCtx, query);
        final int limit = getLimit();
        int count = 0;
        int total = users.size();

        for (User user : users)
        {
            if (!excludeUsers.contains(user.getName()))
            {
                final String html = formatUser(fieldName, user, query, canShowEmailAddresses);
                results.addUser(new UserPickerUser(user.getName(), user.getDisplayName(), html, showAvatar ? avatarService.getAvatarURL(user, user.getName(), Avatar.Size.SMALL) : null));
                ++count;
            } else {
                --total;
            }


            if (count >= limit)
                break;
        }

        results.setTotal(total);
        results.setFooter(i18nHelper.getText("jira.ajax.autocomplete.user.more.results", String.valueOf(count), String.valueOf(total)));

        return results;
    }

    private String getElementId(String fieldName, String type, String field)
    {
        return " id=\"" + fieldName + "_" + type + "_" + field + "\" ";
    }

    // get the number of items to display.
    private int getLimit()
    {
        //Default limit to 20
        int limit = 20;
        try
        {
            limit = Integer.valueOf(applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        catch (Exception nfe)
        {
            log.error("jira.ajax.autocomplete.limit does not exist or is an invalid number in jira-application.properties.", nfe);
        }
        return limit;
    }


    /*
    * We use direct html instead of velocity to ensure the AJAX lookup is as fast as possible
    */
    private String formatUser(String fieldName, User user, String query, boolean canShoweEmailAddresses)
    {

        DelimeterInserter delimeterInserter = new DelimeterInserter("<b>", "</b>");
        //delimeterInserter.setConsideredWhitespace("-_/\\,.+=&^%$#*@!~`'\":;<>");

        String[] terms = {query};

        String userFullName = delimeterInserter.insert(TextUtils.htmlEncode(user.getDisplayName()), terms);
        String userName = delimeterInserter.insert(TextUtils.htmlEncode(user.getName()), terms);


        StringBuilder sb = new StringBuilder();
        sb.append("<div ");
        if(!StringUtils.isEmpty(fieldName))
        {
            sb.append(getElementId(fieldName, "i", TextUtils.htmlEncode(user.getName())));
        }
        sb.append("class=\"yad\" ");

        sb.append(">");

        sb.append(userFullName);
        if (canShoweEmailAddresses)
        {
            String userEmail = delimeterInserter.insert(TextUtils.htmlEncode(user.getEmailAddress()), terms);
            /*
             We dont mask the email address by design.  We dont think the email bots will be able to easily
             get email addresses from YUI generated divs and also its only an issue if "browse user" is given to group
             anyone.  So here is where we would change this if we change our mind in the future.
             */
            sb.append("&nbsp;-&nbsp;");
            sb.append(userEmail);
        }
        sb.append("&nbsp;(");
        sb.append(userName);
        sb.append(")");

        sb.append("</div>");
        return sb.toString();
    }


    JiraServiceContext getContext()
    {
        User user = authContext.getLoggedInUser();
        return new JiraServiceContextImpl(user);
    }

    @XmlRootElement
    public static class UserPickerResultsWrapper
    {
        @XmlElement
        private List<UserPickerUser> users;
        
        @XmlElement
        private Integer total;
        
        @XmlElement
        private String footer;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private UserPickerResultsWrapper() {}

        public UserPickerResultsWrapper(List<UserPickerUser> users, String footer, Integer total)
        {
            this.users = users;
            this.footer = footer;
            this.total = total;
        }

        public void addUser(final UserPickerUser user)
        {
            if (users == null)
            {
                users = new ArrayList<UserPickerUser>();
            }
            users.add(user);
        }

        public void setFooter(String footer)
        {
            this.footer = footer;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        @Override
        public String toString()
        {
            return "UserPickerResultsWrapper{" +
                    "users=" + users +
                    ", total=" + total +
                    ", footer='" + footer + '\'' +
                    '}';
        }
    }

    @XmlRootElement
    public static class UserPickerUser
    {
        @XmlElement
        private String name;
        @XmlElement
        private String html;
        @XmlElement
        private String displayName;
        @XmlElement
        private URI avatarUrl;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private UserPickerUser() {}

        public UserPickerUser(String name, String displayName, String html, URI avatarUrl)
        {
            this.name = name;
            this.displayName = displayName;
            this.html = html;
            this.avatarUrl = avatarUrl;
        }

        @Override
        public String toString()
        {
            return "UserPickerUser{" +
                    "name='" + name + '\'' +
                    ", html='" + html + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", avatarUrl=" + avatarUrl +
                    '}';
        }
    }
}
