package com.atlassian.jira.webtest.webdriver.tests.admin.fields.configuration.schemes;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.ViewFieldConfigurationSchemesPage;
import com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes.configure.ConfigureFieldConfigurationSchemePage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.atlassian.jira.functest.framework.FunctTestConstants.ADMIN_PASSWORD;
import static com.atlassian.jira.functest.framework.FunctTestConstants.ADMIN_USERNAME;
import static com.atlassian.jira.functest.framework.FunctTestConstants.FRED_PASSWORD;
import static com.atlassian.jira.functest.framework.FunctTestConstants.FRED_USERNAME;
import static com.google.common.collect.Iterables.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p>Responsible for testing the core functionality of the &quot;add issue type to field configuration association
 * dialog&quot;.</p>
 *
 * <p>This dialog adds an association between an issue type and a field configuration for a given field configuration
 * scheme.</p>
 *
 * <p>Initial data: - A field configuration scheme named &quot;A test field config. scheme&quot;</p> All tests in this
 * class work against this scheme.
 *
 * @since v5.0.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.FIELDS })
@Restore ("xml/TestAddIssueTypeToFieldConfigurationAssociationDialog/existing-field-configuration-scheme.xml")
public class TestAddIssueTypeToFieldConfigurationAssociationDialog extends BaseJiraWebTest
{
    @Test
    public void shouldBeAbleAddAnIssueTypeToFieldConfigurationAssociationWhenThereAreStillUnmappedIssueTypes()
    {
        final ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                expectedIssueTypeToFieldConfigurationAssociation =
                new ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                        (
                                "Bug", "Default Field Configuration"
                        );

        final ConfigureFieldConfigurationSchemePage configureFieldConfigurationSchemePage =
                jira.gotoLoginPage().loginAsSysAdmin(ViewFieldConfigurationSchemesPage.class).
                        configure("A test field config. scheme").
                        openAddIssueTypeToFieldConfigurationDialog().
                            setIssueType(expectedIssueTypeToFieldConfigurationAssociation.getIssueType()).
                            setFieldConfiguration(expectedIssueTypeToFieldConfigurationAssociation.getFieldConfiguration()).
                        submitSuccess();

        final Iterable<ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem>
                actualIssueTypeToFieldConfigurationAssociationItems =
                    configureFieldConfigurationSchemePage.getIssueTypeToFieldConfigurationAssociations();

        assertTrue
                (
                        contains
                                (
                                        actualIssueTypeToFieldConfigurationAssociationItems,
                                        expectedIssueTypeToFieldConfigurationAssociation
                                )
                );
    }

    @Test
    public void shouldNotBeAbleToAssociateAnIssueTypeToAFieldConfigurationWhenTheIssueTypeHasAlreadyBeenMapped()
    {

        final ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                existingIssueTypeToFieldConfigurationAssociation =
                new ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                        (
                                "Task", "Default Field Configuration"
                        );

        final ConfigureFieldConfigurationSchemePage configureFieldConfigurationSchemePage =
                jira.gotoLoginPage().loginAsSysAdmin(ViewFieldConfigurationSchemesPage.class).
                        configure("A test field config. scheme").
                        openAddIssueTypeToFieldConfigurationDialog().
                        setIssueType(existingIssueTypeToFieldConfigurationAssociation.getIssueType()).
                        setFieldConfiguration(existingIssueTypeToFieldConfigurationAssociation.getFieldConfiguration()).
                        submitSuccess();

        final Iterable<String> selectableIssueTypes = configureFieldConfigurationSchemePage.
                openAddIssueTypeToFieldConfigurationDialog().
                getSelectableIssueTypes();

        assertFalse
                (
                        contains
                                (
                                        selectableIssueTypes,
                                        "Task"
                                )
                );

    }

    @Test
    public void shouldNotBeAbleToAddAnIssueTypeToFieldConfigurationAssociationWhenAllIssueTypesHaveAlreadyBeenMapped()
    {
        final Iterable<ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem>
                associationItemsForAllExistingIssueTypes =
                ImmutableList.of(
                        new ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                                (
                                        "Bug", "Default Field Configuration"
                                ),
                        new ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                                (
                                        "Task", "Default Field Configuration"
                                ),
                        new ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                                (
                                        "Improvement", "Default Field Configuration"
                                ),
                        new ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                                (
                                        "New Feature", "Default Field Configuration"
                                )
                );

        for (final ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                        issueTypeToFieldConfigurationAssociationItem : associationItemsForAllExistingIssueTypes)
        {
            jira.gotoLoginPage().loginAsSysAdmin(ViewFieldConfigurationSchemesPage.class).
                    configure("A test field config. scheme").
                    openAddIssueTypeToFieldConfigurationDialog().
                    setIssueType(issueTypeToFieldConfigurationAssociationItem.getIssueType()).
                    setFieldConfiguration(issueTypeToFieldConfigurationAssociationItem.getFieldConfiguration()).
                    submitSuccess();
        }

        assertTrue
                (
                        jira.gotoLoginPage().
                                loginAsSysAdmin(ViewFieldConfigurationSchemesPage.class).
                                configure("A test field config. scheme").
                                isAddingAnIssueTypeToFieldConfigurationAssociationDisabled()
                );

    }

    @Test
    public void shouldBeAbleToAddAFieldConfigurationSchemeWhenTheLoggedInUserIsAJiraAdministrator()
    {
        final ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                expectedIssueTypeToFieldConfigurationAssociation =
                new ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem
                        (
                                "Bug", "Default Field Configuration"
                        );

        final ConfigureFieldConfigurationSchemePage configureFieldConfigurationSchemePage =
                jira.gotoLoginPage().login(ADMIN_USERNAME, ADMIN_PASSWORD, ViewFieldConfigurationSchemesPage.class).
                        configure("A test field config. scheme").
                        openAddIssueTypeToFieldConfigurationDialog().
                        setIssueType(expectedIssueTypeToFieldConfigurationAssociation.getIssueType()).
                        setFieldConfiguration(expectedIssueTypeToFieldConfigurationAssociation.getFieldConfiguration()).
                        submitSuccess();

        final Iterable<ConfigureFieldConfigurationSchemePage.IssueTypeToFieldConfigurationAssociationItem>
                actualIssueTypeToFieldConfigurationAssociationItems =
                configureFieldConfigurationSchemePage.getIssueTypeToFieldConfigurationAssociations();

        assertTrue
                (
                        contains
                                (
                                        actualIssueTypeToFieldConfigurationAssociationItems,
                                        expectedIssueTypeToFieldConfigurationAssociation
                                )
                );
    }

    @Test
    public void shouldNotBeAbleToAddAFieldConfigurationSchemeWhenTheLoggedInUserIsNeitherAJiraOrSystemAdministrator()
    {
        jira.gotoLoginPage().loginAndGoToHome(FRED_USERNAME, FRED_PASSWORD);
        jira.visitDelayed(ConfigureFieldConfigurationSchemePage.class, "10000");
        jira.visit(JiraLoginPage.class);
    }
}
