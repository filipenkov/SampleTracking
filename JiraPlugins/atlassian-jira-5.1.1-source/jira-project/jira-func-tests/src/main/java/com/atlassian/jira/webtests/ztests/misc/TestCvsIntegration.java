package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;

/**
 *
 */
@Ignore("This test is unreliable, and is messing up our CI. Disabled until someone makes it reliable. --lmiranda")
@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestCvsIntegration extends JIRAWebTest
{
    private static final String TEST_CVS_INTEGRATION_LOG = "TestCvsIntegration.log";

    public TestCvsIntegration(String name)
    {
        super(name);
    }
    
    public void testCommitsWithSimilarProjectKeys() throws InterruptedException
    {
        restoreBlankInstance();
        addProject("AHSP", "AHSP", ADMIN_USERNAME);

        //create 2 test issues
        createIssueStep1("homosapien", "Bug");
        setFormElement("summary", "First issue");
        submit("Create");

        createIssueStep1("AHSP", "Bug");
        setFormElement("summary", "Second issue");
        submit("Create");
        addCvsModule("Test", TEST_CVS_INTEGRATION_LOG);

        //add the cvs module to projects:
        addCvsModuleToProject("AHSP", "Test");
        addCvsModuleToProject("HSP", "Test");

        // Run the Vcs service so we can get some data
        administration.utilities().runServiceNow(10010);

        //check that HSP-1 onlys shows the HSP-1 related commit message
        gotoPage("/browse/HSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels:cvs-tabpanel");
        assertTextPresent("re-ordered sourcing the base_build_jira script such that the current directory is set correctly");
        assertTextNotPresent("Hopefully fixed maven2 build by explicitly setting the path");

        //check that AHSP-1 only shows the AHSP-1 related commit message
        gotoPage("/browse/AHSP-1?page=com.atlassian.jira.plugin.system.issuetabpanels:cvs-tabpanel");
        assertTextNotPresent("re-ordered sourcing the base_build_jira script such that the current directory is set correctly");
        assertTextPresent("Hopefully fixed maven2 build by explicitly setting the path");
    }

    //test for JRA-12978

    public void testCvsIntegrationMovedIssue() throws InterruptedException
    {
        //data with cvs modules configured and a moved issue (HSP-2).
        restoreData("TestCvsIntegrationMovedIssue.xml");

        addCvsModule("Test", "TestCvsIntegrationMovedIssue.log");
        addCvsModule("NewRepo", "TestCvsIntegrationMovedIssueOtherRepository.log");

        assertTextPresent("Test");
        assertTextPresent("NewRepo");

        //add the cvs module to projects:
        addCvsModuleToProject("AHSP", "Test");
        addCvsModuleToProject("HSP", "Test");
        addCvsModuleToProject("MKY", "NewRepo");

        // Run the Vcs service so we can get some data
        administration.utilities().runServiceNow(10010);

        //now check out HSP-2.  Used to be HSP-1 -> AHSP-2 -> MKY-1 -> HSP-2
        navigation.issue().viewIssue("HSP-2");
        clickLinkWithText("Version Control");
        //there should be commits from HSP-1, AHSP-2, MKY-1 and HSP-2.  MKY-1 commits were against a different
        //repository that was linked only to the MKY project.  THey should also be present!
        assertTextPresent("HSP-1 Initial revision");
        assertTextPresent("moving to use tmp dir HSP-1 local to build root");
        assertTextPresent("massaging temp dir path AHSP-2 references to use absolute rendition of working directory");
        assertTextPresent("removing verbose flag on tar MKY-1 zxvf to reduce distracting build log bloat");
        assertTextPresent("remove the temp dir deletion if the MKY-1 build failed");
        assertTextPresent("MKY-1 Added releaseInfo flag to the source release");
        assertTextSequence(new String[] { "HSP-2", "Use maven2 to build source release if a pom.xml file exists" });
        assertTextSequence(new String[] { "HSP-2", "Hopefully fixed maven2 build by explicitly setting the path" });
        assertTextSequence(new String[] { "HSP-2", "re-ordered sourcing the base_build_jira script such that the current directory is set correctly" });
    }

    public void testAdminCanNotModify() throws InterruptedException
    {
        try
        {
            restoreData("TestWithSystemAdmin.xml");
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);

            addProject("AHSP", "AHSP", ADMIN_USERNAME);

            //setup a cvs module
            addCvsModule("Test", TEST_CVS_INTEGRATION_LOG);

            assertLinkPresent("edit_10000");
            assertLinkPresent("test_10000");
            assertLinkPresent("delete_10000");
            assertLinkPresent("add_cvs_module");

            // login as an admin
            login(ADMIN_USERNAME, ADMIN_PASSWORD);

            gotoAdmin();
            clickLink("cvs_modules");
            assertLinkNotPresent("edit_10000");
            assertLinkNotPresent("test_10000");
            assertLinkNotPresent("delete_10000");
            assertLinkNotPresent("add_cvs_module");

            assertTextPresent("Only JIRA system administrators can manipulate CVS modules.");

            // Try to jump to the add url and make sure we end up back at the view
            gotoPage("/secure/admin/AddRepository!default.jspa");

            assertTextPresent("Only JIRA system administrators can manipulate CVS modules.");

            // Try to add the repository by hacking the url
            tester.gotoPage(page.addXsrfToken("/secure/admin/AddRepository.jspa?name=AnotherTest&cvsRoot=someroot&moduleName=dummy&fetchLog=false&logFilePath=/tmp"));
            assertTextPresent("You must be a JIRA system administrator to add a CVS repository to JIRA.");
        }
        finally
        {
            logout();
            // go back to sysadmin user
            login(SYS_ADMIN_USERNAME, SYS_ADMIN_PASSWORD);
            restoreBlankInstance();
        }
    }

    //JRA-20275
    public void testTimeoutOverflow() throws Exception
    {
        restoreBlankInstance();

        gotoAdmin();
        clickLink("cvs_modules");
        clickLink("add_cvs_module");
        setFormElement("name", "Test");
        setFormElement("cvsRoot", ":pserver:anonymous@example.com:/");
        setFormElement("moduleName", "hello");
        setFormElement("logFilePath", getCvsLogFile(TEST_CVS_INTEGRATION_LOG));
        checkCheckbox("fetchLog", "false");
        setFormElement("timeout", String.valueOf(3600000));
        submit(" Add ");

        assertTextPresent("CVS Modules");
        text.assertTextSequence(new TableLocator(tester, "cvs_modules_table"), "CVS Timeout", "3600000 seconds");

        //Lets edit the repository and specify the largest timeout.
        clickLink("edit_10000");
        setFormElement("timeout", String.valueOf(Long.MAX_VALUE - 100));

        submit(" Update ");

        //It will actually store Long.MAX_VALUE / 1000 since TimeUnit.SECONDS.toMillis(Long.MAX_VALUE - 100) == Long.MAX_VALUE. This will stop
        //the overflow to negative numbers even for Longs.
        assertTextPresent("CVS Modules");
        text.assertTextSequence(new TableLocator(tester, "cvs_modules_table"), "CVS Timeout", String.format("%d seconds", Long.MAX_VALUE / 1000));
    }

    private void addCvsModuleToProject(String projectName, String repositoryName)
    {
        gotoPage("/plugins/servlet/project-config/" + projectName + "/summary");

        clickLink("project-config-cvs-change");
        selectOption("multipleRepositoryIds", repositoryName);
        submit("Select");
    }

    private void addCvsModule(String name, String logfile)
    {
        //setup a cvs module
        gotoAdmin();
        clickLink("cvs_modules");
        clickLinkWithText("Add");
        setFormElement("name", name);
        setFormElement("cvsRoot", ":pserver:anonymous@example.com:/");
        setFormElement("moduleName", "dummy");
        checkCheckbox("fetchLog", "false");
        setFormElement("logFilePath", getCvsLogFile(logfile));

        submit(" Add ");
        // Ensure the CVS module was created
        assertTextInTable("cvs_modules_table", name);
    }

    private String getCvsLogFile(final String logfile)
    {
        final File dataLocation = new File(getEnvironmentData().getXMLDataLocation(), logfile);
        try
        {
            return dataLocation.getCanonicalPath();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
