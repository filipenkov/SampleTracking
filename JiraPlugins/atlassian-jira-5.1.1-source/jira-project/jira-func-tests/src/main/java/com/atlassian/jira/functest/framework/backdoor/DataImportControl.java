package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.functest.framework.dump.FuncTestTimer;
import com.atlassian.jira.functest.framework.dump.TestInformationKit;
import com.atlassian.jira.functest.framework.xmlbackup.XmlBackupCopier;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import java.io.File;

/**
 * Use this class from func/selenium/page-object tests that need to import data. Which is all of them.
 *
 * See DataImportBackdoor for the code this plugs into at the back-end.
 *
 * @since v5.0
 */
public class DataImportControl extends BackdoorControl<DataImportControl>
{
    public static final String FS = System.getProperty("file.separator");

    private JIRAEnvironmentData environmentData;
    private XmlBackupCopier xmlBackupCopier;
    /**
     * Evil but necessary static field used for caching the JIRA_HOME during func test runs.
     */
    private static final ThreadLocal<String> JIRA_HOME_DIR = new ThreadLocal<String>();

    public DataImportControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        this.environmentData = environmentData;
        this.xmlBackupCopier = new XmlBackupCopier(environmentData.getBaseUrl());
    }

    /**
     * Restores the instance with the default XML file. A commercial license is used.
     */
    public void restoreBlankInstance()
    {
        restoreData("blankprojects.xml");
    }

    /**
     * Restores the instance with the specified XML file. A commercial license is used.
     * @param xmlFileName the name of the file to import
     */
    public void restoreData(String xmlFileName)
    {
        final FuncTestTimer timer = TestInformationKit.pullTimer("XML Restore");

        // 1. Copy the import file from the test resource directory to the server's import XML directory
        // (i.e. we ain't using no secret sauce).
        // Done at the 'front-end' and not the back-end because of the desire to reuse XmlBackupCopier logic...
        String sourcePath = environmentData.getXMLDataLocation().getAbsolutePath() + FS + xmlFileName;
        String jiraImportPath = getJiraHomePath() + FS + "import" + FS + new File(xmlFileName).getName();
        boolean baseUrlReplaced = xmlBackupCopier.copyXmlBackupTo(sourcePath, jiraImportPath);

        DataImportBean importBean = new DataImportBean();
        importBean.filePath = jiraImportPath;
        importBean.licenseString = LicenseKeys.V2_COMMERCIAL.getLicenseString();
        importBean.useDefaultPaths = false;
        importBean.quickImport = true;
        importBean.isSetup = false;

        if (baseUrlReplaced)
        {
            importBean.baseUrl = environmentData.getBaseUrl().toString();
        }

        post(createResource().path("dataImport"), importBean, String.class);
    }

    public void turnOffDangerMode()
    {
        post(createResource().path("systemproperty").path("jira.dangermode").queryParam("value", "false"));
    }

    public void turnOnDangerMode()
    {
        post(createResource().path("systemproperty").path("jira.dangermode").queryParam("value","true"));
    }

    private String getJiraHomePath()
    {
        String jiraHomeDir = JIRA_HOME_DIR.get();
        if (jiraHomeDir == null)
        {
            jiraHomeDir = get(createResource().path("dataImport/jiraHomePath"));
            JIRA_HOME_DIR.set(jiraHomeDir);
        }
        return jiraHomeDir;
    }

    static class DataImportBean
    {
        public String filePath;
        public String licenseString;
        public boolean quickImport;
        public boolean useDefaultPaths;
        public boolean isSetup;
        public String baseUrl;
    }
}
