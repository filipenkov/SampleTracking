package com.atlassian.jira.webtest.webdriver.tests.admin.fields.screen.scheme;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.AddFieldScreenSchemeDialog;
import com.atlassian.jira.pageobjects.pages.admin.ViewFieldScreenSchemesPage;
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
 * Responsible for testing the core functionality of the &quot;add field screen dialog&quot;.
 *
 * @since v5.0.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.SCHEMES, Category.FIELDS })
@Restore ("xml/blankprojects.xml")
public class TestAddFieldScreenSchemeDialog extends BaseJiraWebTest
{
    @Test
    public void shouldBeAbleToAddAFieldScreenSchemeByOnlyDefiningANameForIt()
    {
        final ViewFieldScreenSchemesPage.FieldScreenSchemeItem expectedFieldScreenSchemeItem =
                new ViewFieldScreenSchemesPage.FieldScreenSchemeItem("A test field screen scheme", "");

        jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreenSchemesPage.class).
                openAddFieldScreenSchemeDialog().
                setName(expectedFieldScreenSchemeItem.getName()).
                submitSuccess();

        final ViewFieldScreenSchemesPage viewFieldScreenSchemesPage = jira.goTo(ViewFieldScreenSchemesPage.class);

        final Iterable<ViewFieldScreenSchemesPage.FieldScreenSchemeItem> actualFieldScreenSchemes =
                viewFieldScreenSchemesPage.getFieldScreenSchemes();

        assertTrue(contains(actualFieldScreenSchemes, expectedFieldScreenSchemeItem));
    }

    @Test
    public void shouldBeAbleToAddAFieldScreenSchemeByDefiningBothItsNameAndADescription()
    {
        final ViewFieldScreenSchemesPage.FieldScreenSchemeItem expectedFieldScreenSchemeItem =
                new ViewFieldScreenSchemesPage.
                        FieldScreenSchemeItem
                        (
                                "A test field screen scheme", "description for the test field screen scheme"
                        );

        jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreenSchemesPage.class).
                openAddFieldScreenSchemeDialog().
                setName(expectedFieldScreenSchemeItem.getName()).
                setDescription(expectedFieldScreenSchemeItem.getDescription()).
                submitSuccess();

        final ViewFieldScreenSchemesPage viewFieldScreenSchemesPage = jira.goTo(ViewFieldScreenSchemesPage.class);

        final Iterable<ViewFieldScreenSchemesPage.FieldScreenSchemeItem> actualFieldScreenSchemes =
                viewFieldScreenSchemesPage.getFieldScreenSchemes();

        assertTrue(contains(actualFieldScreenSchemes, expectedFieldScreenSchemeItem));
    }

    @Test
    public void shouldNotBeAbleToAddAFieldScreenSchemeGivenThatItsNameIsEmpty()
    {
        final ViewFieldScreenSchemesPage.FieldScreenSchemeItem fieldScreenSchemeItemWithAnEmptyName =
                new ViewFieldScreenSchemesPage.FieldScreenSchemeItem("", "description text");

        final String expectedErrorMessageForAFieldScreenSchemeWithAnEmptyName =
                "You must enter a valid name.";

        final AddFieldScreenSchemeDialog addFieldScreenSchemeDialog = jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreenSchemesPage.class).
                openAddFieldScreenSchemeDialog();

        addFieldScreenSchemeDialog.
                setName(fieldScreenSchemeItemWithAnEmptyName.getName()).
                setDescription(fieldScreenSchemeItemWithAnEmptyName.getDescription()).
                submit();

        assertTrue(addFieldScreenSchemeDialog.isOpen());
        assertTrue(addFieldScreenSchemeDialog.hasFormErrors());

        final Map<String,String> addFieldScreenSchemeDialogFormErrors =
                addFieldScreenSchemeDialog.getFormErrors();

        assertEquals
                (
                        addFieldScreenSchemeDialogFormErrors.get("fieldScreenSchemeName"),
                        expectedErrorMessageForAFieldScreenSchemeWithAnEmptyName
                );
    }

    @Test
    public void shouldNotBeAbleToAddAFieldScreenSchemeGivenThatItsNameMatchesAnExistingFieldScreenScheme()
    {
        final ViewFieldScreenSchemesPage.FieldScreenSchemeItem defaultFieldScreenScheme =
                new ViewFieldScreenSchemesPage.
                        FieldScreenSchemeItem("Default Screen Scheme", "The default field screen scheme");

        final String expectedErrorMessageForAFieldScreenSchemeWithDuplicateName =
                "A Screen Scheme with this name already exists.";

        final AddFieldScreenSchemeDialog addFieldScreenSchemeDialog = jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreenSchemesPage.class).
                openAddFieldScreenSchemeDialog();

        addFieldScreenSchemeDialog.
                setName(defaultFieldScreenScheme.getName()).
                setDescription(defaultFieldScreenScheme.getDescription()).
                submit();

        assertTrue(addFieldScreenSchemeDialog.isOpen());
        assertTrue(addFieldScreenSchemeDialog.hasFormErrors());

        final Map<String,String> addFieldScreenSchemeDialogFormErrors =
                addFieldScreenSchemeDialog.getFormErrors();

        assertEquals
                (
                        addFieldScreenSchemeDialogFormErrors.get("fieldScreenSchemeName"),
                        expectedErrorMessageForAFieldScreenSchemeWithDuplicateName
                );
    }

    @Test
    public void shouldBeAbleToAddAFieldScreenSchemeWhenTheLoggedInUserIsAJiraAdministrator()
    {
        final ViewFieldScreenSchemesPage.FieldScreenSchemeItem expectedFieldScreenSchemeItem =
                new ViewFieldScreenSchemesPage.
                        FieldScreenSchemeItem("A test field screen scheme", "description text");

        jira.gotoLoginPage().
                login(ADMIN_USERNAME, ADMIN_PASSWORD, ViewFieldScreenSchemesPage.class).
                openAddFieldScreenSchemeDialog().
                setName(expectedFieldScreenSchemeItem.getName()).
                setDescription(expectedFieldScreenSchemeItem.getDescription()).
                submitSuccess();

        final ViewFieldScreenSchemesPage viewFieldScreenSchemesPage = jira.goTo(ViewFieldScreenSchemesPage.class);

        final Iterable<ViewFieldScreenSchemesPage.FieldScreenSchemeItem> actualFieldScreenSchemes =
                viewFieldScreenSchemesPage.getFieldScreenSchemes();

        assertTrue(contains(actualFieldScreenSchemes, expectedFieldScreenSchemeItem));
    }

    @Test
    public void shouldNotBeAbleToAddAFieldScreenSchemeWhenTheLoggedInUserIsNeitherAJiraOrSystemAdministrator()
    {
        jira.gotoLoginPage().loginAndGoToHome(FRED_USERNAME, FRED_PASSWORD);
        jira.visitDelayed(ViewFieldScreenSchemesPage.class);
        jira.visit(JiraLoginPage.class);
    }
}
