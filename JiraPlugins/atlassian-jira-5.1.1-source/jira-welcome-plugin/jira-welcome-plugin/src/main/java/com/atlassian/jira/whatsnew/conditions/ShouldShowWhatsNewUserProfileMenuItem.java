package com.atlassian.jira.whatsnew.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.whatsnew.access.WhatsNewAccess;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * A {@link Condition} that is used to check whether JIRA should render a menu item for the &quot;What's New&quot;
 * feature in the &quot;user profile&quot; drop-down.
 *
 * @since 1.1
 * @see WhatsNewAccess
 */
public class ShouldShowWhatsNewUserProfileMenuItem implements Condition
{
    private final JiraAuthenticationContext authenticationContext;
    private final WhatsNewAccess whatsNewAccess;

    public ShouldShowWhatsNewUserProfileMenuItem(final JiraAuthenticationContext authenticationContext,
                                                 final WhatsNewAccess whatsNewAccess)
    {
        this.authenticationContext = authenticationContext;
        this.whatsNewAccess = whatsNewAccess;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    /**
     * Whether the what's new menu item should be displayed in the user profile drop-down menu.
     *
     * <p>This will return {@code true} if the user has been granted access to the what's new
     * feature.</p>
     *
     * @param context {@inheritDoc}
     * @return {@code true} if the what's new menu item should be displayed in the user profile drop-down menu;
     * otherwise, {@code false}.
     *
     * @see WhatsNewAccess
     * @see WhatsNewAccess.Constraint
     */
    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        return whatsNewAccess.isGrantedTo(authenticationContext.getLoggedInUser());
    }
}
