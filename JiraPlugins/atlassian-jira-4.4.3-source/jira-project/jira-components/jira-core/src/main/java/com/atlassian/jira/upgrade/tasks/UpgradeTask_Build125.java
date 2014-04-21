/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.user.util.GlobalUserPreferencesUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.util.JiraLocaleUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

// Check default and user pref locales for any *incompatible* locales
// Used for regional licensing

public class UpgradeTask_Build125 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build106.class);

    private final GlobalUserPreferencesUtil userUtil;
    private final BuildUtilsInfo buildUtilsInfo;


    public UpgradeTask_Build125(GlobalUserPreferencesUtil userUtil, final BuildUtilsInfo buildUtilsInfo)
    {
        this.userUtil = notNull("userUtil", userUtil);
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public String getBuildNumber()
    {
        return "125";
    }

    public String getShortDescription()
    {
        return "Check that default and user preference locales are compatible with those available for this JIRA installation.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        JiraLocaleUtils localeUtils = ComponentManager.getInstance().getJiraLocaleUtils();
        ApplicationProperties applicationProperites = ComponentAccessor.getApplicationProperties();
        final Collection<Locale> unavailableLocales = buildUtilsInfo.getUnavailableLocales();

        // Check the default locale as set in JIRA data only - not the system locale
        Locale locale = localeUtils.getLocale(applicationProperites.getDefaultBackedString(APKeys.JIRA_I18N_DEFAULT_LOCALE));

        for (Iterator<Locale> iterator = unavailableLocales.iterator(); iterator.hasNext();)
        {
            Locale unavailableLocale = iterator.next();

            if (locale != null && locale.equals(unavailableLocale))
            {
                log.warn("The default locale - " + locale.getDisplayName() + " - configured in the import data is not " +
                        "available in this installation of JIRA. The translation is available from an Atlassian partner. Please contact Atlassian for further details.");
            }

            // Check user preference locales
            String localeCode = unavailableLocale.toString();
            long userCount = userUtil.getUserLocalePreferenceCount(localeCode);

            if (userCount != 0)
            {
                // More than 20 users - display number only
                if (userCount > 20)
                {
                    log.warn(userCount + " JIRA users have selected the " + unavailableLocale.getDisplayName() + " locale as their " +
                            "user preference locale.");
                }
                else
                {
                    // Display list of names
                    StringBuffer userNames = new StringBuffer();
                    for (Iterator iterator1 = (userUtil.getUserLocalePreferenceList(localeCode)).iterator(); iterator1.hasNext();)
                    {
                        String userName = (String) iterator1.next();
                        if (iterator.hasNext())
                            userNames.append(userName).append(", ");
                        else
                            userNames.append(userName);
                    }
                    log.warn("The following JIRA users have selected the " + unavailableLocale.getDisplayName() + " locale as their " +
                        "user preference locale - " + userNames.toString() + ".");
                }
                log.warn("The '" + unavailableLocale.getDisplayName() + "' locale is not available in this installation of JIRA. The translation is available from an Atlassian partner. Please contact Atlassian for further details.");
            }
        }
    }
}
