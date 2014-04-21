package com.atlassian.jira.whatsnew;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.welcome.WelcomeUserPreferenceManager;
import com.atlassian.sal.api.ApplicationProperties;
import com.opensymphony.module.propertyset.PropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhatsNewManager
{
    private static final Logger log = LoggerFactory.getLogger(WhatsNewManager.class);

    // Matches "3.5" in "3.5.1" or "3.5-SNAPSHOT"
    private static Pattern VERSION_PATTERN = Pattern.compile("^(\\d+\\.\\d+).*");
    private static final String PROPERTY_KEY = "jira.user.whats.new.dont.show.version";

    private ApplicationProperties applicationProperties;
    private final UserPropertyManager userPropertyManager;
    private final WelcomeUserPreferenceManager welcomeManager;

    public WhatsNewManager(final ApplicationProperties applicationProperties, final UserPropertyManager userPropertyManager, final WelcomeUserPreferenceManager welcomeManager)
    {
        this.applicationProperties = applicationProperties;
        this.userPropertyManager = userPropertyManager;
        this.welcomeManager = welcomeManager;
    }

    public boolean isShownForUser(User user, boolean fromCheck)
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
        String dontShowVersion = ps.getString(PROPERTY_KEY);
        String currentVersion = getCurrentVersion();

        boolean shown = !currentVersion.equals(dontShowVersion);
        if (shown && fromCheck)
        {
            log.debug("Shown for user '{}' : don't-show version is {} but current version is {}", new Object[] { user.getName(), dontShowVersion, currentVersion });
        }

        // If the welcome manager is being shown, we don't want to display the what's new dialog
        return shown && !welcomeManager.isShownForUser(user);
    }

    public void setShownForUser(User user, boolean shown)
    {
        final PropertySet ps = userPropertyManager.getPropertySet(user);
        if (ps == null)
        {
            log.warn("Unable to set shownForUser preference for user: " + user);
            return;
        }

        // dontShowVersion in Bandana can be null, "UNSET", or a version number
        String dontShowVersion = shown ? "UNSET" : getCurrentVersion();
        ps.setString(PROPERTY_KEY, dontShowVersion);
        log.debug("Preference changed for user '{}' : don't-show version is {}",
                new Object[] { user.getName(), dontShowVersion });
    }

    private String getCurrentVersion()
    {
        String versionNumber = applicationProperties.getVersion();
        Matcher matcher = VERSION_PATTERN.matcher(versionNumber);
        matcher.matches();
        return matcher.group(1);
    }
}
