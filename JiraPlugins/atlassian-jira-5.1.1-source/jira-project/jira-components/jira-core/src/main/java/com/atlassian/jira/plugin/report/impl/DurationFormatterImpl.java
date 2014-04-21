package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.bean.I18nBean;

/**
 * Duration formatter used by reports.
 *
 * @since v3.11
 */
class DurationFormatterImpl implements DurationFormatter
{
    private final JiraDurationUtils jiraDurationUtils;
    private final I18nBean i18nBean;

    DurationFormatterImpl(I18nBean i18nBean, JiraDurationUtils jiraDurationUtils)
    {
        this.i18nBean = i18nBean;
        this.jiraDurationUtils = jiraDurationUtils;
    }

    /**
     * Formats the duration. If duration is null, returns a dash..
     *
     * @param duration duration
     * @return formatted duration String or i18ned "unknown" if null.
     */
    public String format(Long duration)
    {
        if (duration == null)
        {
            return "-";
        }
        duration = new Long(Math.abs(duration.longValue()));
        return jiraDurationUtils.getFormattedDuration(duration, i18nBean.getLocale());
    }

    public String shortFormat(Long duration)
    {
        if (duration == null)
        {
            return "-";
        }
        duration = new Long(Math.abs(duration.longValue()));
        return jiraDurationUtils.getShortFormattedDuration(duration);
    }
}