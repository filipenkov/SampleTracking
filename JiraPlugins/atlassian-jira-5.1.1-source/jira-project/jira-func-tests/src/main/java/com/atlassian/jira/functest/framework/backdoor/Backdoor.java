package com.atlassian.jira.functest.framework.backdoor;


import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.SearchClient;

/**
 * Top-level of Backdoor control hierarchy. Use components of this class to
 * manipulate the back-end in func tests without hitting the UI.
 *
 * @since v5.0
 */
public class Backdoor
{
    private final UsersAndGroupsControl usersAndGroups;
    private final IssuesControl issues;
    private final I18nControl i18n;
    private final DarkFeaturesControl darkFeatures;
    private final DataImportControl dataImport;
    private final PermissionsControl permissions;
    private final ApplicationPropertiesControl applicationProperties;
    private final ProjectControl project;
    private final PermissionSchemesControl permissionSchemes;
    private final MailServersControl mailServers;
    private final SearchRequestControl searchRequests;
    private final UserProfileControl userProfile;
    private final ServicesControl services;
    private final TestRunnerControl testRunner;
    private final FieldConfigurationControl fieldConfigurationControl;
    private final IssueTypeControl issueTypeControl;
    private final SubtaskControl subtaskCotnrol;
    private final IssueLinkingControl issueLinkingControl;
    private final IssueTypeScreenSchemesControl issueTypeScreenSchemes;
    private final WebSudoControl webSudoControl;
    private final DashboardControl dashboardControl;
    private final PluginsControl plugins;
    private final GeneralConfigurationControl generalConfigurationControl;
    private final WorkflowsControl workflowsControl;
    private final WorkflowSchemesControl workflowSchemesControl;
    private final AdvancedSettingsControl advancedSettingsControl;
    private final SearchClient searchClient;

    public Backdoor(JIRAEnvironmentData environmentData)
    {
        this.plugins = new PluginsControl(environmentData);
        this.usersAndGroups = new UsersAndGroupsControl(environmentData);
        this.issues = new IssuesControl(environmentData);
        this.i18n = new I18nControl(environmentData);
        this.darkFeatures = new DarkFeaturesControl(environmentData);
        this.permissions = new PermissionsControl(environmentData);
        this.applicationProperties = new ApplicationPropertiesControl(environmentData);
        this.project = new ProjectControl(environmentData);
        this.permissionSchemes = new PermissionSchemesControl(environmentData);
        this.issueTypeScreenSchemes = new IssueTypeScreenSchemesControl(environmentData);
        this.mailServers = new MailServersControl(environmentData);
        this.searchRequests = new SearchRequestControl(environmentData);
        this.userProfile = new UserProfileControl(environmentData);
        this.dataImport = new DataImportControl(environmentData);
        this.services = new ServicesControl(environmentData);
        this.testRunner = new TestRunnerControl(environmentData);
        this.fieldConfigurationControl = new FieldConfigurationControl(environmentData);
        this.issueTypeControl = new IssueTypeControl(environmentData);
        this.subtaskCotnrol = new SubtaskControl(environmentData);
        this.webSudoControl = new WebSudoControl(environmentData);
        this.issueLinkingControl = new IssueLinkingControl(environmentData);
        this.dashboardControl = new DashboardControl(environmentData);
        this.generalConfigurationControl = new GeneralConfigurationControl(environmentData);
        this.workflowsControl = new WorkflowsControl(environmentData);
        this.workflowSchemesControl = new WorkflowSchemesControl(environmentData);
        this.advancedSettingsControl = new AdvancedSettingsControl(environmentData);
        this.searchClient = new SearchClient(environmentData);
    }

    public UsersAndGroupsControl usersAndGroups()
    {
        return usersAndGroups;
    }

    public IssuesControl issues()
    {
        return issues;
    }

    public I18nControl i18n()
    {
        return i18n;
    }

    public DarkFeaturesControl darkFeatures()
    {
        return darkFeatures;
    }

    public PluginsControl plugins()
    {
        return plugins;
    }

    public PermissionsControl permissions()
    {
        return permissions;
    }

    public ApplicationPropertiesControl applicationProperties()
    {
        return applicationProperties;
    }

    public ProjectControl project()
    {
        return project;
    }

    public PermissionSchemesControl permissionSchemes()
    {
        return permissionSchemes;
    }

    public IssueTypeScreenSchemesControl issueTypeScreenSchemes()
    {
        return issueTypeScreenSchemes;
    }

    public MailServersControl mailServers()
    {
        return mailServers;
    }

    public SearchRequestControl searchRequests()
    {
        return searchRequests;
    }

    public UserProfileControl userProfile()
    {
        return userProfile;
    }

    public ServicesControl services()
    {
        return services;
    }

    public DataImportControl dataImport()
    {
        return dataImport;
    }

    public TestRunnerControl testRunner()
    {
        return testRunner;
    }

    public FieldConfigurationControl fieldConfiguration()
    {
        return fieldConfigurationControl;
    }

    public IssueTypeControl issueType()
    {
        return issueTypeControl;
    }

    public SubtaskControl subtask()
    {
        return subtaskCotnrol;
    }

    public WebSudoControl websudo()
    {
        return webSudoControl;
    }

    public DashboardControl dashboard()
    {
        return dashboardControl;
    }

    public GeneralConfigurationControl generalConfiguration()
    {
        return generalConfigurationControl;
    }

    public AdvancedSettingsControl advancedSettings()
    {
        return advancedSettingsControl;
    }

    public void restoreData(String xmlFileName)
    {
        dataImport().restoreData(xmlFileName);
    }

    public void restoreBlankInstance()
    {
        dataImport().restoreBlankInstance();
    }

    public IssueLinkingControl issueLinking()
    {
        return issueLinkingControl;
    }

    public WorkflowsControl workflow()
    {
        return workflowsControl;
    }

    public WorkflowSchemesControl workflowSchemes()
    {
        return workflowSchemesControl;
    }

    public SearchClient search()
    {
        return searchClient;
    }
}
