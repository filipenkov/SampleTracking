package com.atlassian.jira.upgrade;

import com.atlassian.jira.bean.export.IllegalXMLCharactersException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface UpgradeManager
{

    /**
     * <p/>
     * Performs any upgrades that may be needed as a result of the Setup procedure of JIRA
     *
     * <p/>
     * Get the set of setupUpgradeNumbers which are to be performed for this setup.
     *
     * <p/>
     * Iterate over these numbers and if either of the standard, professional or enterprise upgrade maps contains an
     * {@link UpgradeTask} with this number then do the upgrade
     *
     * <p/>
     * If errors are found, it will cancel the upgrade, and return the list of errors.
     *
     * <p/>
     * For each upgrade that happens successfully, it will increment the build number in the database, so that if one
     * fails, you do not have to repeat all the upgrades that have already run.
     *
     * <p/>
     * If there are no errors from the upgrade, the build number in the database is incremented to the current build
     * number.  This is because there may be no upgrades for a particular version & needUpgrade() checks build no in
     * database.
     *
     * @return list of errors that occured during the upgrade process
     */
    Collection<String> doSetupUpgrade();

    /**
     * Performs the upgrade if one is required and the license is not too old to proceed with the upgrade.
     *
     * @param backupPath - a path to the default location of the export, may be <code>null</code>, in which case no auto
     * export will be performed
     * @return a list of errors that occurred during the upgrade
     * @throws com.atlassian.jira.bean.export.IllegalXMLCharactersException if backup was impossible due to invalid XML
     * characters
     */
    Collection<String> doUpgradeIfNeededAndAllowed(@Nullable String backupPath) throws IllegalXMLCharactersException;

    /**
     * Export path of the last backup performed by this manager
     *
     * @return path to the last backup file
     */
    String getExportFilePath();

    /**
     * @return the history of upgrades performed on this instance of JIRA in reverse chronological order
     * @since v4.1
     */
    List<UpgradeHistoryItem> getUpgradeHistory();
}
