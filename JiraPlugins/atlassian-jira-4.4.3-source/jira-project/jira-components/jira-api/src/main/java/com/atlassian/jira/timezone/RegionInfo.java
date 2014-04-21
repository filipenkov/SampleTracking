package com.atlassian.jira.timezone;

/**
 * TimeZones are grouped by region.
 *
 * @since v4.4
 */
public interface RegionInfo extends Comparable<RegionInfo>
{
    /**
     * @return The key for this region.
     */
    String getKey();

    /**
     * @return the i18n'ed display name for this region.
     */
    String getDisplayName();
}
