package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.HtmlPage;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.util.AsynchronousTasks;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Component;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.ComponentClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.ProjectClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Version;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.VersionClient;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of Project interface
 *
 * @since v3.13
 */
public class ProjectImpl extends AbstractFuncTestUtil implements Project
{
    private final static Pattern PROJECT_ID_PATTERN = Pattern.compile("projectId=(\\d+)");

    private final Navigation navigation;
    private final Assertions assertions;
    private final AsynchronousTasks asynchronousTasks;
    private final HtmlPage htmlPage;

    public ProjectImpl(WebTester tester, JIRAEnvironmentData environmentData, Navigation navigation, Assertions assertions,
            AsynchronousTasks asynchronousTasks)
    {
        super(tester, environmentData, 2);
        this.navigation = navigation;
        this.assertions = assertions;
        this.asynchronousTasks = asynchronousTasks;
        this.htmlPage = new HtmlPage(tester);
    }

    public long addProject(String name, String key, String lead)
    {
        if (projectExists(name))
        {
            log("Project " + name + " exists ");
            navigation.gotoPage("/plugins/servlet/project-config/" + key.toUpperCase() + "/summary");
        }
        else
        {
            log("Adding project " + name);

            //Doing this as a URL hack because the lead is a SS-Frother select list which may not have the user
            //listed as a possible options (it is populated via JavaScript).
            StringBuilder builder = new StringBuilder("/secure/admin/AddProject.jspa");
            addUrlParameter(builder, "name", name);
            addUrlParameter(builder, "key", key);
            addUrlParameter(builder, "lead", lead);
            addUrlParameter(builder, "permissionScheme", 0);
            addUrlParameter(builder, "assigneeType", 2);

            tester.gotoPage(htmlPage.addXsrfToken(builder.toString()));
            assertions.getURLAssertions().assertCurrentURLPathEndsWith(getEnvironmentData().getContext() + "/plugins/servlet/project-config/" + key.toUpperCase() + "/summary");
        }

        try
        {
            final String text = tester.getDialog().getResponse().getText();
            final Matcher matcher = PROJECT_ID_PATTERN.matcher(text);
            if (!matcher.find())
            {
                Assert.fail("Could not find projectId on result page.");
            }
            return Long.parseLong(matcher.group(1));
        }
        catch (IOException e)
        {
            Assert.fail("Unable to retrieve issue key" + e.getMessage());
            return -1;
        }
    }

    @Override
    public void viewProject(String project)
    {
        navigation.gotoAdmin();
        tester.clickLink("view_projects");
        if (htmlPage.isLinkPresentWithExactText(project))
        {
            navigation.clickLinkWithExactText(project);
        }
        else
        {
            Assert.fail("Cannot find link to project with name '" + project + "'");
        }
    }

    @Override
    public void editProject(long projectId, String name, String description, String url)
    {
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);
        tester.assertTextPresent("action=\"EditProject.jspa\"");

        if (name != null)
        {
            tester.setFormElement("name", name);
        }

        if (description != null)
        {
            tester.setFormElement("description", description);
        }

        if (url != null)
        {
            tester.setFormElement("url", url);
        }

        tester.submit();

        if (name != null)
        {
            assertions.getTextAssertions().assertTextPresent(locators.id("project-config-header-name"), name);
        }

        if (description != null)
        {
            assertions.getTextAssertions().assertTextPresent(locators.id("project-config-description"), description);
        }

        if (url != null)
        {
            assertions.getTextAssertions().assertTextPresent(locators.id("project-config-details-project-url"), url);
        }
    }

    public void deleteProject(long projectId)
    {
        tester.gotoPage("/secure/admin/DeleteProject!default.jspa?pid=" + projectId);
        tester.submit("Delete");
    }

    public void deleteProject(String project)
    {
        final String key = getProjectKeyFromName(project);

        tester.gotoPage("/plugins/servlet/project-config/" + key + "/summary");
        tester.clickLink("delete_project");
        tester.assertTextPresent("Delete Project: " + project);
        tester.submit("Delete");
    }

    public String addComponent(final String projectKey, final String componentName, final String description, final String leadUserName)
    {

        ComponentClient componentClient = new ComponentClient(environmentData);

        final Component component = componentClient.create(new Component().project(projectKey).name(componentName).description(description).leadUserName(leadUserName));

        return "" + component.id;
    }

    public String addVersion(final String projectKey, final String versionName, final String description, final String releaseDate)
    {
        VersionClient versionClient = new VersionClient(environmentData);
        final Version version = new Version();
        version.project(projectKey).name(versionName).description(description).userReleaseDate(releaseDate);
        final Version newVersion = versionClient.create(version);

        return "" + newVersion.id;
    }

    private String getProjectKeyFromName(String projectName)
    {
        ProjectClient projectClient = new ProjectClient(environmentData);
        final List<com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Project> projects = projectClient.getProjects();

        for (com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Project project : projects)
        {
            if (project.name.equals(projectName))
            {
                return project.key;
            }
        }
        return null;
    }


    private Version getVersionByName(String projectKey, String versionName)
    {
        ProjectClient projectClient = new ProjectClient(environmentData);
        final List<com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Project> projects = projectClient.getProjects();

        final List<Version> versions = projectClient.getVersions(projectKey);

        for (Version version : versions)
        {
            if (version.name.equals(versionName))
            {
                return version;
            }
        }

        return null;

    }

    private Component getComponentByname(String projectKey, String componentName)
    {
        ProjectClient projectClient = new ProjectClient(environmentData);
        final List<Component> components = projectClient.getComponents(projectKey);
        for (Component component : components)
        {
            if (component.name.equals(componentName))
            {
                return component;
            }
        }

        return null;

    }

    public void archiveVersion(final String projectKey, final String versionName)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName.archived(true));

    }

    public void releaseVersion(final String projectKey, final String versionName, final String releaseDate)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName.released(true).userReleaseDate(releaseDate));
    }

    public void unreleaseVersion(final String projectKey, final String versionName, final String releaseDate)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName.released(false).userReleaseDate(releaseDate));
    }

    public void deleteVersion(final String projectKey, final String versionName)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.delete("" + versionByName.id);
    }

    public void editVersionDetails(final String projectKey, final String versionName, final String name, final String description, final String releaseDate)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);
        if (name != null)
        {
            versionByName.name(name);
        }
        if (description != null)
        {
            versionByName.description(description);
        }
        if (releaseDate != null)
        {
            versionByName.userReleaseDate(releaseDate);
        }

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName);
    }

    public void editComponent(final String projectKey, final String componentName, final String name, final String description, final String leadUserName)
    {
        ComponentClient componentClient = new ComponentClient(environmentData);

        final Component component = getComponentByname(projectKey, componentName);

        if (name != null)
        {
            component.name(name);
        }
        if (description != null)
        {
            component.description(description);
        }
        if (leadUserName != null)
        {
            component.leadUserName(leadUserName);
        }
        componentClient.putResponse(component);
    }

    public void associateFieldConfigurationScheme(final String projectName, String newFieldConfigurationSchemeName)
    {
        final String projectKey = getProjectKeyFromName(projectName);

        tester.gotoPage("/plugins/servlet/project-config/" + projectKey + "/fields");
        tester.clickLink("project-config-fields-scheme-change");
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        if (newFieldConfigurationSchemeName == null)
        {
            newFieldConfigurationSchemeName = "System Default Field Configuration";
        }
        tester.selectOption("schemeId", newFieldConfigurationSchemeName);
        tester.submit("Associate");
        assertions.assertNodeByIdHasText("project-config-fields-scheme-name", newFieldConfigurationSchemeName);
    }

    public void associateWorkflowScheme(String projectName, String workflowSchemeName, Map<String, String> statusMapping, boolean wait)
    {
        final String projectKey = getProjectKeyFromName(projectName);

        tester.gotoPage("/plugins/servlet/project-config/" + projectKey + "/workflows");
        tester.clickLink("project-config-workflows-scheme-change");

        // We want to select the default workflow scheme for a project but it is already selected
        // and there are no workflow schemes available.
        if (workflowSchemeName.equals("Default") && tester.getDialog().getElement("schemeId_select") == null)
        {
            return;
        }
        
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.selectOption("schemeId", workflowSchemeName);
        tester.submit("Associate");
        boolean thereAreIssuesToMigrate = !locators.css(".jiraform").getText().
                contains("There are no issues to migrate.");

        if (statusMapping != null && !statusMapping.isEmpty())
        {
            // Select status mappings
            for (Map.Entry<String, String> entry : statusMapping.entrySet())
            {
                tester.selectOption(entry.getKey(), entry.getValue());
            }
        }
        // We may get back to the workflow configuration page if there was no need
        // for confirmation
        if (tester.getDialog().getElement("project-config-panel-workflows") != null)
        {
            return;
        }
        tester.submit("Associate");

        if (thereAreIssuesToMigrate && wait)
        {
            waitForWorkflowMigration(1000, 100);
        }

    }

    @Override
    public void associateWorkflowScheme(String projectName, String workflowSchemeName)
    {
        associateWorkflowScheme(projectName, workflowSchemeName, null, true);
    }

    @Override
    public void associateNotificationScheme(String projectName, String notificationSchemeName)
    {
        tester.gotoPage("/plugins/servlet/project-config/" + projectName + "/notifications");
        tester.clickLink("project-config-notification-scheme-change");
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.selectOption("schemeIds", notificationSchemeName);
        tester.submit("Associate");
    }

    private void waitForWorkflowMigration(long sleepTime, int retryCount)
    {
        asynchronousTasks.waitForSuccessfulCompletion(sleepTime, retryCount, "Workflow Migration");
    }

    public void setProjectLead(final String projectName, final String userName)
    {
        final String keyFromName = getProjectKeyFromName(projectName);

        tester.gotoPage("/plugins/servlet/project-config/" + keyFromName + "/people");
        tester.clickLink("edit_project_lead");

        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, "project-edit-lead-and-default-assignee", "Update");
        formParameterUtil.addOptionToHtmlSelect("lead", new String[] { userName });
        formParameterUtil.setFormElement("lead", userName);
        formParameterUtil.submitForm();
    }

    public boolean projectExists(final String project)
    {
        log("Checking if project '" + project + "' exists");
        navigation.gotoAdmin();
        tester.clickLink("view_projects");
        return htmlPage.isLinkPresentWithExactText(project);
    }


    private String encode(String name)
    {
        try
        {
            return URLEncoder.encode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private StringBuilder addUrlParameter(StringBuilder builder, final String name, Object value)
    {
        if (builder.indexOf("?") >= 0)
        {
            builder.append("&");
        }
        else
        {
            builder.append("?");
        }

        return builder.append(encode(name)).append("=").append(encode(String.valueOf(value)));
    }
}
