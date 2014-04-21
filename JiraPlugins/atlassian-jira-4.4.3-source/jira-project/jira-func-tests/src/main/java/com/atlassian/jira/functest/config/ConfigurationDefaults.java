package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.config.crowd.CrowdApplicationCheck;
import com.atlassian.jira.functest.config.crowd.PlaintextEncoderChecker;
import com.atlassian.jira.functest.config.dashboard.DashboardConfigurationCheck;
import com.atlassian.jira.functest.config.mail.MailChecker;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DelegateFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Holds the default configuration values for the XML checker and fixer.
 */
public final class ConfigurationDefaults
{
    private ConfigurationDefaults()
    {
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Why are you calling me? I'm static.");
    }

    public static File getDefaultXmlDataLocation()
    {
        LocalTestEnvironmentData environmentData = new LocalTestEnvironmentData();
        final File xmlDataLocation = environmentData.getXMLDataLocation();
        if (xmlDataLocation == null)
        {
            throw new RuntimeException("Func Test XML directory has not been configured.");
        }

        if (!xmlDataLocation.exists())
        {
            throw new RuntimeException(String.format("Func Test XML directory '%s' does not exist.", xmlDataLocation));
        }

        return xmlDataLocation;
    }

    public static List<IOFileFilter> getDefaultExcludedFilters()
    {
        /*
            NOTE: DO NOT JUST ADD YOUR FILE HERE IF THE UNIT TEST FAILS. Have a look at
            https://extranet.atlassian.com/x/GAW7b for information on disabling individual checks for a file.
            By adding a file to this list you could be hiding problems in your XML that can cause the
            func tests to fail intermittently.
         */

        List<IOFileFilter> files = new ArrayList<IOFileFilter>();

        //START: NON-backup files.
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-issue-links.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-timetracking.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-due-date-hidden.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-subtasks.xml"));
        files.add(createNameFilter("TestXmlIssueView-HSP-1-no-custom-fields.xml"));
        files.add(createNameFilter("check-cache.xml"));
        files.add(createNameFilter("fixerBroken.zip"));
        files.add(createParentFilter("TestDownloadZipAttachmentEntries/attachments"));
        files.add(createParentFilter("TestAttachmentsBlockSortingOnViewIssue/attachments"));
        files.add(createParentFilter("TestBrowseZipAttachmentEntries/attachments"));
        //END: NON-backup files.

        //Generated file.
        files.add(createNameFilter("TestEmptyStringDataRestore_out.xml"));
        /*
            NOTE: DO NOT JUST ADD YOUR FILE HERE IF THE UNIT TEST FAILS. Have a look at
            https://extranet.atlassian.com/x/GAW7b for information on disabling individual checks for a file.
            By adding a file to this list you could be hiding problems in your XML that can cause the
            func/selenium tests to fail intermittently.
         */

        return files;
    }

    public static List<ConfigurationCheck> createDefaultConfigurationChecks()
    {
        List<ConfigurationCheck> checkers = new ArrayList<ConfigurationCheck>();
        checkers.add(new AttachmentDirectoryChecker());
        checkers.add(new IndexDirectoryChecker());
        checkers.add(new BackupChecker());
        checkers.add(new MailChecker());
        checkers.add(new DashboardConfigurationCheck());
        checkers.add(new ServiceChecker());
        checkers.add(new CrowdApplicationCheck());
        checkers.add(new PlaintextEncoderChecker());

        return checkers;
    }

    private static IOFileFilter createNameFilter(final String name)
    {
        return new NameFileFilter(name, IOCase.INSENSITIVE);
    }

    private static IOFileFilter createParentFilter(final String name)
    {
        //Bit of a hack but it should work well enough.
        final String s = Pattern.quote(FilenameUtils.separatorsToSystem(name));
        return new DelegateFileFilter(new RegexPathFilter(s));
    }

    private static class RegexPathFilter implements FileFilter
    {
        private final Pattern patten;

        public RegexPathFilter(final String patten)
        {
            this.patten = Pattern.compile(patten, Pattern.CASE_INSENSITIVE);
        }

        public boolean accept(final File pathname)
        {
            return patten.matcher(pathname.getPath()).find();
        }
    }
}
