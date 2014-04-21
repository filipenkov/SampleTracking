package com.atlassian.jira.plugin.issuenav;

import com.atlassian.core.filters.AbstractHttpFilter;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.log4j.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Filter that maps to /secure/IssueNavigator.jspa.  If the user has chosen to use kickass then they'll get redirected
 * to kickass when trying to go to the old navigator.
 *
 * @since v5.1
 */
public class KickassRedirectFilter extends AbstractHttpFilter
{
    private static final Logger log = Logger.getLogger(KickassRedirectFilter.class);
    public static final String TRY_KICKASS_PROPERTY = "try.kickass";
    public static final String OPT_OUT_TIME = "kickass.opt.out.time";
    public static final String ATLASSIAN_STAFF = "atlassian-staff";

    private final JiraAuthenticationContext authenticationContext;
    private final UserPropertyManager userPropertyManager;
    private final FeatureManager featureManager;
    private final GroupManager groupManager;

    public KickassRedirectFilter(final JiraAuthenticationContext authenticationContext,
            final UserPropertyManager userPropertyManager, FeatureManager featureManager,
            final GroupManager groupManager)
    {
        this.authenticationContext = authenticationContext;
        this.userPropertyManager = userPropertyManager;
        this.featureManager = featureManager;
        this.groupManager = groupManager;
    }

    @Override
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException
    {
        final User loggedInUser = authenticationContext.getLoggedInUser();
        final Group group = groupManager.getGroup(ATLASSIAN_STAFF);
        final boolean isAtlassianStaff = group != null && loggedInUser != null && groupManager.isUserInGroup(loggedInUser, group);
        //if you're an atlassian the default is to turn kickass on!
        if(isAtlassianStaff)
        {
            final PropertySet ps = userPropertyManager.getPropertySet(loggedInUser);
            final Date optOutDate = ps.getDate(OPT_OUT_TIME);
            //if they've never tried kickass or they have but haven't got an opt out date opt Atlassians in!
            if(!ps.exists(TRY_KICKASS_PROPERTY) || optOutDate == null)
            {
                ps.setBoolean(TRY_KICKASS_PROPERTY, true);
            }
            //otherwise if they've opted out opt them back in if it's been longer than 7 days
            else if(!ps.getBoolean(TRY_KICKASS_PROPERTY))
            {
                int dayInMillis = 24*60*60*1000; 
                int daysSinceOptOut = (int) ((System.currentTimeMillis() - optOutDate.getTime())/dayInMillis);
                if(daysSinceOptOut >=7)
                {
                    ps.setBoolean(TRY_KICKASS_PROPERTY, true);
                }
            }
        }



        if(!featureManager.isEnabled("jira.search.kickass"))
        {
            filterChain.doFilter(request, response);
        }
        else
        {
            boolean tryKickass = false;
            if (loggedInUser != null)
            {
                PropertySet ps = userPropertyManager.getPropertySet(loggedInUser);
                tryKickass = ps.getBoolean(TRY_KICKASS_PROPERTY);
            }

            if (tryKickass)
            {
                String redirectUrl = request.getContextPath() + "/issues/";

                //check if we were looking at a filter or jql search. We should redirect to the correct url in kickass
                //if that was the case.
                final String filterId = request.getParameter("requestId");
                if(filterId != null)
                {
                    redirectUrl += "advanced/?filter=" + filterId;
                }
                final String jqlQuery = request.getParameter("jqlQuery");
                if(jqlQuery != null)
                {
                    redirectUrl += "advanced/?jql=" + jqlQuery;
                }

                response.sendRedirect(redirectUrl);
            }
            else
            {
                filterChain.doFilter(request, response);
            }
        }
    }
}
