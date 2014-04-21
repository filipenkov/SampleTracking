package com.atlassian.jira.webtest.webdriver.tests.admin.fields.configuration;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.AddFieldConfigurationDialog;
import com.atlassian.jira.pageobjects.pages.admin.ViewFieldConfigurationsPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.Map;

import static com.atlassian.jira.functest.framework.FunctTestConstants.ADMIN_PASSWORD;
import static com.atlassian.jira.functest.framework.FunctTestConstants.ADMIN_USERNAME;
import static com.atlassian.jira.functest.framework.FunctTestConstants.FRED_PASSWORD;
import static com.atlassian.jira.functest.framework.FunctTestConstants.FRED_USERNAME;
import static com.google.common.collect.Iterables.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Responsible for testing the core functionality of the &quot;add field configuration dialog&quot;.
 *
 * @since v5.0.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.FIELDS })
@Restore ("xml/blankprojects.xml")
public class TestAddFieldConfigurationDialog extends BaseJiraWebTest
{
    @Test
    public void shouldBeAbleToAddAFieldConfigurationByOnlyDefiningANameForIt()
    {
        final ViewFieldConfigurationsPage.FieldConfigurationItem expectedFieldConfigurationItem =
                new ViewFieldConfigurationsPage.FieldConfigurationItem("A test field configuration", "");

        jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldConfigurationsPage.class).
                openAddFieldConfigurationDialog().
                setName(expectedFieldConfigurationItem.getName()).
                submitSuccess();

        final ViewFieldConfigurationsPage viewFieldConfigurationsPage = jira.goTo(ViewFieldConfigurationsPage.class);

        final Iterable<ViewFieldConfigurationsPage.FieldConfigurationItem> actualFieldConfigurations =
                viewFieldConfigurationsPage.getFieldConfigurations();

        assertTrue(contains(actualFieldConfigurations, expectedFieldConfigurationItem));
    }

    @Test
    public void shouldBeAbleToAddAFieldConfigurationByDefiningBothItsNameAndADescription()
    {
        final ViewFieldConfigurationsPage.FieldConfigurationItem expectedFieldConfigurationItem =
                new ViewFieldConfigurationsPage.
                        FieldConfigurationItem
                        (
                                "A test field configuration", "description for the test field configuration"
                        );

        jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldConfigurationsPage.class).
                openAddFieldConfigurationDialog().
                setName(expectedFieldConfigurationItem.getName()).
                setDescription(expectedFieldConfigurationItem.getDescription()).
                submitSuccess();

        final ViewFieldConfigurationsPage viewFieldConfigurationsPage = jira.goTo(ViewFieldConfigurationsPage.class);

        final Iterable<ViewFieldConfigurationsPage.FieldConfigurationItem> actualFieldConfigurations =
                viewFieldConfigurationsPage.getFieldConfigurations();

        assertTrue(contains(actualFieldConfigurations, expectedFieldConfigurationItem));
    }

    @Test
    public void shouldNotBeAbleToAddAFieldConfigurationGivenThatItsNameIsEmpty()
    {
        final ViewFieldConfigurationsPage.FieldConfigurationItem fieldConfigurationItemWithAnEmptyName =
                new ViewFieldConfigurationsPage.FieldConfigurationItem("", "description text");

        final String expectedErrorMessageForAFieldConfigurationWithAnEmptyName =
                "The field configuration name must not be empty.";

        final AddFieldConfigurationDialog addFieldConfigurationDialog = jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldConfigurationsPage.class).
                openAddFieldConfigurationDialog();

        addFieldConfigurationDialog.
                setName(fieldConfigurationItemWithAnEmptyName.getName()).
                setDescription(fieldConfigurationItemWithAnEmptyName.getDescription()).
                submit();

        assertTrue(addFieldConfigurationDialog.isOpen());
        assertTrue(addFieldConfigurationDialog.hasFormErrors());

        final Map<String,String> addFieldConfigurationDialogFormErrors =
                addFieldConfigurationDialog.getFormErrors();

        assertEquals
                (
                        addFieldConfigurationDialogFormErrors.get("fieldLayoutName"),
                        expectedErrorMessageForAFieldConfigurationWithAnEmptyName
                );
    }

    @Test
    public void shouldNotBeAbleToAddAFieldConfigurationGivenThatItsNameMatchesAnExistingFieldConfiguration()
    {
        final ViewFieldConfigurationsPage.FieldConfigurationItem defaultFieldConfiguration =
                new ViewFieldConfigurationsPage.
                        FieldConfigurationItem("Default Field Configuration", "The default field configuration");

        final String expectedErrorMessageForAFieldConfigurationWithDuplicateName =
                "A field configuration with this name already exists.";

        final AddFieldConfigurationDialog addFieldConfigurationDialog = jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldConfigurationsPage.class).
                openAddFieldConfigurationDialog();

        addFieldConfigurationDialog.
                setName(defaultFieldConfiguration.getName()).
                setDescription(defaultFieldConfiguration.getDescription()).
                submit();

        assertTrue(addFieldConfigurationDialog.isOpen());
        assertTrue(addFieldConfigurationDialog.hasFormErrors());

        final Map<String,String> addFieldConfigurationDialogFormErrors =
                addFieldConfigurationDialog.getFormErrors();

        assertEquals
                (
                        addFieldConfigurationDialogFormErrors.get("fieldLayoutName"),
                        expectedErrorMessageForAFieldConfigurationWithDuplicateName
                );
    }

    @Test
    public void shouldBeAbleToAddAFieldConfigurationWhenTheLoggedInUserIsAJiraAdministrator()
    {
        final ViewFieldConfigurationsPage.FieldConfigurationItem expectedFieldConfigurationItem =
                new ViewFieldConfigurationsPage.
                        FieldConfigurationItem("A test field configuration", "description text");

        jira.gotoLoginPage().
                login(ADMIN_USERNAME, ADMIN_PASSWORD, ViewFieldConfigurationsPage.class).
                openAddFieldConfigurationDialog().
                setName(expectedFieldConfigurationItem.getName()).
                setDescription(expectedFieldConfigurationItem.getDescription()).
                submitSuccess();

        final ViewFieldConfigurationsPage viewFieldConfigurationsPage = jira.goTo(ViewFieldConfigurationsPage.class);
        final Iterable<ViewFieldConfigurationsPage.FieldConfigurationItem> actualFieldConfigurations =
                viewFieldConfigurationsPage.getFieldConfigurations();

        assertTrue(contains(actualFieldConfigurations, expectedFieldConfigurationItem));
    }

    @Test
    public void shouldNotBeAbleToAddAFieldConfigurationWhenTheLoggedInUserIsNeitherAJiraOrSystemAdministrator()
    {
        jira.gotoLoginPage().loginAndGoToHome(FRED_USERNAME, FRED_PASSWORD);
        jira.visitDelayed(ViewFieldConfigurationsPage.class);
        jira.visit(JiraLoginPage.class);
    }
}
