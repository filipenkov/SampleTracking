package com.atlassian.jira.welcome;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages user preferences and flags that relate to the welcome screen dialog.
 *
 * @since v5.1
 */
public class WelcomeUserPreferenceManager
{
    private static final Logger log = LoggerFactory.getLogger(WelcomeUserPreferenceManager.class);

    private static final String PROPERTY_KEY = "jira.user.welcome.dismissed";

    private ApplicationProperties applicationProperties;
    private final UserPropertyManager userPropertyManager;

    public WelcomeUserPreferenceManager(final ApplicationProperties applicationProperties, final UserPropertyManager userPropertyManager)
    {
        this.applicationProperties = applicationProperties;
        this.userPropertyManager = userPropertyManager;
    }

    public boolean isShownForUser(User user)
    {
        if(user == null)
        {
            return false;
        }
        final PropertySet ps = userPropertyManager.getPropertySet(user);
        if (ps == null)
        {
            log.warn("Unable to get shownForUser preference for user: " + user);
            return false;
        }
        boolean dismissedByUser = ps.getBoolean(PROPERTY_KEY);
    
        return !dismissedByUser;
    }
    
    public void setShownForUser(User user, boolean value)
    {
        final PropertySet ps = userPropertyManager.getPropertySet(user);
        if (ps == null)
        {
            log.warn("Unable to set shownForUser preference for user: " + user);
            return;
        }

        boolean dismissedByUser = !value;
        ps.setBoolean(PROPERTY_KEY, dismissedByUser);
        log.debug("Preference changed for user '{}' : welcome screen dismissed is {}",
                new Object[] { user.getName(), dismissedByUser });
    }
}
