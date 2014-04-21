package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.fields.FieldsPanel;
import com.atlassian.jira.pageobjects.project.summary.fields.FieldsPanel.FieldConfigListItem;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Web test for the project configuration summary page's Fields panel.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@Restore("xml/TestProjectConfigSummaryFieldsPanel.xml")

public class TestSummaryFieldsPanel extends BaseJiraWebTest
{
    private static final String HSP_KEY = "HSP";
    private static final String MKY_KEY = "MKY";
    private static final String XSS_KEY = "XSS";

    private static List<FieldConfigListItem> DEFAULT_FIELD_CONFIGS;
    private static List<FieldConfigListItem> CUSTOM_AND_SYSTEM_FIELD_CONFIGS;
    private static List<FieldConfigListItem> CUSTOM_ONLY_FIELD_CONFIGS_AND_XSS;
    private static List<FieldConfigListItem> FIELD_CONFIGS_FOR_PROJECT_ADMIN;

    private static final String DEFAULT_FIELD_CONFIG_SCHEME_NAME = "System Default Field Configuration";
    private static final String CUSTOM_AND_SYSTEM_FIELD_CONFIG_SCHEME_NAME = "Custom And System Field Config Scheme";
    private static final String CUSTOM_ONLY_FIELD_CONFIG_SCHEME_NAME = "Custom Only Field Config Scheme <script>alert('lolcat');</script>";

    private static final String PROJECT_ADMIN = "project_admin";

    private static String baseUrl;

    @Before
    public void setUp()
    {

        baseUrl = jira.getProductInstance().getBaseUrl();

        DEFAULT_FIELD_CONFIGS = Lists.newArrayList(
            createListItem("Default Field Configuration", "/secure/admin/ViewIssueFields.jspa", true)
        );
        CUSTOM_AND_SYSTEM_FIELD_CONFIGS = Lists.newArrayList(
            createListItem("Default Field Configuration", "/secure/admin/ConfigureFieldLayout!default.jspa?id=10100", true),
            createListItem("Custom Field Config 1", "/secure/admin/ConfigureFieldLayout!default.jspa?id=10000", false)
        );
        CUSTOM_ONLY_FIELD_CONFIGS_AND_XSS = Lists.newArrayList(


            createListItem("<script>alert('oh noes');</script>", "/secure/admin/ConfigureFieldLayout!default.jspa?id=10002", false),
            createListItem("Custom Field Config 1", "/secure/admin/ConfigureFieldLayout!default.jspa?id=10000", false),
            createListItem("Custom Field Config 2", "/secure/admin/ConfigureFieldLayout!default.jspa?id=10001", false)
        );
        FIELD_CONFIGS_FOR_PROJECT_ADMIN = Lists.newArrayList(
            createListItem("Default Field Configuration", null, true)
        );

    }

    @Test
    public void testSystemDefaultFieldConfigScheme()
    {
        // for a project with no field config scheme selected
        // - ensure that the system default field config scheme is shown, and the links are right
        // - ensure that the system default field config is shown, and the link is right
        // - ensure that (default) is shown

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final FieldsPanel fieldsPanel = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(FieldsPanel.class);
        assertEquals(DEFAULT_FIELD_CONFIG_SCHEME_NAME, fieldsPanel.fieldConfigSchemeEditLinkText());
        assertEquals(DEFAULT_FIELD_CONFIGS, fieldsPanel.fieldConfigs());
        assertEquals(baseUrl + "/plugins/servlet/project-config/HSP/fields", fieldsPanel.fieldConfigSchemeEditLinkUrl());
    }

    @Restore("xml/TestProjectConfigSummaryFieldsPanelWithCustomFieldConfigSchemes.xml")
    @Test
    public void testShowChangeFieldConfigSchemeLinkOnlyIfSystemAdmin()
    {
        // for System Admin:
        // - link to edit the Field Config Scheme
        // - link to change the Field Config Scheme
        // - links to individual Field Configs
        // for Project Admin:
        // - no links on the Field Config Scheme or the Field Configs and no link to change the Field Config Scheme

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final FieldsPanel fieldsPanel = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(FieldsPanel.class);

        // edit link for Field Config Scheme
        assertEquals(DEFAULT_FIELD_CONFIG_SCHEME_NAME, fieldsPanel.fieldConfigSchemeEditLinkText());
        assertEquals(baseUrl + "/plugins/servlet/project-config/HSP/fields", fieldsPanel.fieldConfigSchemeEditLinkUrl());
        //links to individual Field Configs
        assertEquals(DEFAULT_FIELD_CONFIGS, fieldsPanel.fieldConfigs());

        jira.gotoLoginPage().login(PROJECT_ADMIN, PROJECT_ADMIN, DashboardPage.class);

        final FieldsPanel fieldsPanelForProjectAdmin = navigateToSummaryPageFor(HSP_KEY)
                .openPanel(FieldsPanel.class);

        // edit link for Field Config Scheme
        assertEquals(DEFAULT_FIELD_CONFIG_SCHEME_NAME, fieldsPanelForProjectAdmin.fieldConfigSchemeEditLinkText());
        assertEquals(baseUrl + "/plugins/servlet/project-config/HSP/fields", fieldsPanel.fieldConfigSchemeEditLinkUrl());
        //links to individual Field Configs
        assertEquals(FIELD_CONFIGS_FOR_PROJECT_ADMIN, fieldsPanelForProjectAdmin.fieldConfigs());

    }

    @Restore("xml/TestProjectConfigSummaryFieldsPanelWithCustomFieldConfigSchemes.xml")
    @Test
    public void testFieldConfigListHasNoDuplicates()
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final FieldsPanel fieldsPanel = navigateToSummaryPageFor(MKY_KEY)
                .openPanel(FieldsPanel.class);
        assertEquals(CUSTOM_AND_SYSTEM_FIELD_CONFIG_SCHEME_NAME, fieldsPanel.fieldConfigSchemeEditLinkText());
        assertEquals(CUSTOM_AND_SYSTEM_FIELD_CONFIGS, fieldsPanel.fieldConfigs());
    }

    @Restore("xml/TestProjectConfigSummaryFieldsPanelWithCustomFieldConfigSchemes.xml")
    @Test
    public void testXssAndNoDefaultFieldConfig()
    {

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        final FieldsPanel fieldsPanel = navigateToSummaryPageFor(XSS_KEY)
                .openPanel(FieldsPanel.class);
        assertEquals(CUSTOM_ONLY_FIELD_CONFIG_SCHEME_NAME, fieldsPanel.fieldConfigSchemeEditLinkText());
        assertEquals(CUSTOM_ONLY_FIELD_CONFIGS_AND_XSS, fieldsPanel.fieldConfigs());
    }


    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

    private FieldConfigListItem createListItem(final String configName, final String configUrl, final boolean isDefault)
    {
        final String url = (configUrl == null) ?
                null : baseUrl + configUrl;
        return new FieldConfigListItem(configName, url, isDefault);
    }

}
