package com.atlassian.jira.util;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * Provides version information about the currently running build of JIRA.
 *
 * @since v4.0
 */
public interface BuildUtilsInfo
{
    /**
     * Gets the current version of JIRA
     *
     * @return the current version of JIRA
     */
    String getVersion();

    /**
     * Gets the current build number of JIRA.
     * <p>
     * This is the same value as {@link #getApplicationBuildNumber()} except it is returned as a String.
     *
     * @return the current build number of JIRA
     *
     * @see #getDatabaseBuildNumber()
     * @see #getApplicationBuildNumber()
     */
    String getCurrentBuildNumber();

    /**
     * Gets the build number of this JIRA install.
     * <p>
     * That is, the build number of the currently running installation files.
     * The DB also stores a build number.
     *
     * @return the build number of this JIRA install.
     *
     * @see #getDatabaseBuildNumber()
     */
    int getApplicationBuildNumber();

    /**
     * Gets the build number of the database that this JIRA instance points to.
     * <p>
     * Under normal circumstances this will be the same as the build number of the JIRA installation.
     * However, when you first upgrade a JIRA database or import data exported from an older JIRA, the existing database
     * will start on the previous build number. JIRA will then run "Upgrade Tasks" to update the data in the DB.
     *
     * @return the build number of the database that JIRA points to.
     *
     * @see #getCurrentBuildNumber()
     */
    int getDatabaseBuildNumber();

    /**
     * Gets the minimal build number that JIRA can upgrade from
     *
     * @return the minimal build number that JIRA can upgrade from
     */
    String getMinimumUpgradableBuildNumber();

    /**
     * Gets the date this version of JIRA was built on.
     *
     * @return the date this version of JIRA was built on
     */
    Date getCurrentBuildDate();

    /**
     * Gets the partner name of this JIRA build
     *
     * @return the partner name of this JIRA build.
     */
    String getBuildPartnerName();

    /**
     * Gets a build information summary as a String.
     *
     * @return a build information summary
     */
    String getBuildInformation();

    /**
     * Get the SVN revision of the build as a String. Will be empty if building from source and it wasn't checked out from SVN
     * @return the SVN revision of this build of JIRA
     */
    String getSvnRevision();

    /**
     * Get the minimum version of JIRA that can be upgraded to this instance version.
     *
     * @return the minimum version that can be upgraded.
     */
    String getMinimumUpgradableVersion();

    Collection<Locale> getUnavailableLocales();

    /**
     * Returns the version of Atlassian SAL that JIRA exports into OSGI-land.
     *
     * @return the version of Atlassian SAL that JIRA exports
     */
    String getSalVersion();

    /**
     * Returns the version of AppLinks that JIRA ships with.
     *
     * @return the version of AppLinks that JIRA ships with
     */
    String getApplinksVersion();
}
