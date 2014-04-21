package com.atlassian.jira.web.action.issue;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.session.SessionPagerFilterManager;
import org.apache.log4j.Logger;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Some utility code shared between the searching actions. 
 *
 * @since v4.0
 */
final class SearchActionHelper
{
    private static final Logger log = Logger.getLogger(SearchActionHelper.class);

    private static final int DEFAULT_NUMBER_OF_ISSUES_PER_PAGE = 20;

    private final SessionPagerFilterManager sessionPagerFilterManager;
    private final Preferences userPreferences;

    private int tempMax = -1;

    SearchActionHelper(SessionPagerFilterManager sessionPagerFilterManager, Preferences userPreferences, IssueSearchLimits issueSearchLimits)
    {
        this.sessionPagerFilterManager = notNull("sessionPagerFilterManager", sessionPagerFilterManager);
        this.userPreferences = notNull("userPreferences", userPreferences);
    }

    /**
     * Store the current pager in the session. The pager handles paging through the issue list.
     *
     * @return the page currently in the session.
     */
    PagerFilter getPagerFilter()
    {
        PagerFilter pager = sessionPagerFilterManager.getCurrentObject();

        if (pager == null)
        {
            pager = resetPager();
        }

        if (tempMax >= 0)
        {
            pager.setMax(tempMax);
        }

        return pager;
    }

    /**
     * Restart the pager in the session.
     *
     * @return the new PagerFilter that was created
     */
    PagerFilter resetPager()
    {
        final PagerFilter pager = new PagerFilter();
        try
        {
            pager.setMax((int) userPreferences.getLong(PreferenceKeys.USER_ISSUES_PER_PAGE));
        }
        catch (final NumberFormatException nfe)
        {
            log.error("Unable to find '" + PreferenceKeys.USER_ISSUES_PER_PAGE + "' property setting. Defaulting to " + DEFAULT_NUMBER_OF_ISSUES_PER_PAGE);
            pager.setMax(DEFAULT_NUMBER_OF_ISSUES_PER_PAGE);
        }
        sessionPagerFilterManager.setCurrentObject(pager);
        return pager;
    }


    int getTempMax()
    {
        return tempMax;
    }

    void setTempMax(final int tempMax)
    {
        this.tempMax = tempMax;
    }

    void resetPagerTempMax()
    {
        if (getTempMax() >= 0)
        {
            getPagerFilter().setMax(getTempMax());
        }
    }

    void restartPager()
    {
        getPagerFilter().setStart(0);
    }
}
