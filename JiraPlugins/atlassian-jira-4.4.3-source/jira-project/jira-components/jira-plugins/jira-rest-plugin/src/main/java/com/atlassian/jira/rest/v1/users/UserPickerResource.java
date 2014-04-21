package com.atlassian.jira.rest.v1.users;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.DelimeterInserter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
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
    private final I18nHelper i18nHelper;

    public UserPickerResource(JiraAuthenticationContext authContext, I18nHelper.BeanFactory i18nBeanFactory,
                              UserPickerSearchService service, ApplicationProperties applicationProperties,
                              AvatarService avatarService)
    {
        this.authContext = authContext;
        this.service = service;
        this.applicationProperties = applicationProperties;
        this.avatarService = avatarService;
        i18nHelper = i18nBeanFactory.getInstance(authContext.getLoggedInUser());
    }

    @GET
    public Response getUsersResponse(@QueryParam("fieldName") final String fieldName,
                                     @QueryParam("query") final String query, @QueryParam("showAvatar") final boolean showAvatar)
    {
        return Response.ok(getUsers(fieldName, query, showAvatar)).cacheControl(NO_CACHE).build();
    }

    public UserPickerResultsWrapper getUsers(final String fieldName, final String query, final boolean showAvatar)
    {
        final JiraServiceContext jiraServiceCtx = getContext();
        final UserPickerResultsWrapper results = new UserPickerResultsWrapper();

        if (service.canPerformAjaxSearch(jiraServiceCtx))
        {
            final boolean canShowEmailAddresses = service.canShowEmailAddresses(jiraServiceCtx);
            final int limit = getLimit();
            int count = 0;
            final Collection<User> users = service.findUsers(jiraServiceCtx, query);
            for (User user : users)
            {
                if (count >= limit)
                {
                    break;
                }

                final String html = formatUser(fieldName, user, query, canShowEmailAddresses);

                results.addUser(new UserPickerUser(user.getName(), user.getDisplayName(), html, showAvatar ? avatarService.getAvatarURL(user, user.getName(), Avatar.Size.SMALL) : null));
                count++;
            }
            results.setFooter(i18nHelper.getText("jira.ajax.autocomplete.user.more.results", String.valueOf(count), String.valueOf(users.size())));
        }
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


        StringBuffer sb = new StringBuffer();
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
        ErrorCollection errorCollection = new SimpleErrorCollection();
        return new JiraServiceContextImpl(user, errorCollection);
    }

    @XmlRootElement
    public static class UserPickerResultsWrapper
    {
        @XmlElement
        private List<UserPickerUser> users;
        @XmlElement
        private String footer;

        @SuppressWarnings({"UnusedDeclaration", "unused"})
        private UserPickerResultsWrapper() {}

        public UserPickerResultsWrapper(List<UserPickerUser> users, String footer)
        {
            this.users = users;
            this.footer = footer;
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
    }
}
