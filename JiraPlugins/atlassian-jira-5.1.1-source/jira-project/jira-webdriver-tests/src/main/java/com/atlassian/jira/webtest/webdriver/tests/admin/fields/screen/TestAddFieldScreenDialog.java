package com.atlassian.jira.webtest.webdriver.tests.admin.fields.screen;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.AddFieldScreenDialog;
import com.atlassian.jira.pageobjects.pages.admin.ViewFieldScreensPage;
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
public class TestAddFieldScreenDialog extends BaseJiraWebTest
{
    @Test
    public void shouldBeAbleToAddAFieldScreenByOnlyDefiningANameForIt()
    {
        final ViewFieldScreensPage.FieldScreenItem expectedFieldScreenItem =
                new ViewFieldScreensPage.FieldScreenItem("A test field screen", "");

        jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreensPage.class).
                openAddFieldScreenDialog().
                setName(expectedFieldScreenItem.getName()).
                submitSuccess();

        final ViewFieldScreensPage viewFieldScreensPage = jira.goTo(ViewFieldScreensPage.class);

        final Iterable<ViewFieldScreensPage.FieldScreenItem> actualFieldScreens =
                viewFieldScreensPage.getFieldScreens();

        assertTrue(contains(actualFieldScreens, expectedFieldScreenItem));
    }

    @Test
    public void shouldBeAbleToAddAFieldScreenByDefiningBothItsNameAndADescription()
    {
        final ViewFieldScreensPage.FieldScreenItem expectedFieldScreenItem =
                new ViewFieldScreensPage.
                        FieldScreenItem
                        (
                                "A test field screen", "description for the test field screen"
                        );

        jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreensPage.class).
                openAddFieldScreenDialog().
                setName(expectedFieldScreenItem.getName()).
                setDescription(expectedFieldScreenItem.getDescription()).
                submitSuccess();

        final ViewFieldScreensPage viewFieldScreensPage = jira.goTo(ViewFieldScreensPage.class);

        final Iterable<ViewFieldScreensPage.FieldScreenItem> actualFieldScreens =
                viewFieldScreensPage.getFieldScreens();

        assertTrue(contains(actualFieldScreens, expectedFieldScreenItem));
    }

    @Test
    public void shouldNotBeAbleToAddAFieldScreenGivenThatItsNameIsEmpty()
    {
        final ViewFieldScreensPage.FieldScreenItem fieldScreenItemWithAnEmptyName =
                new ViewFieldScreensPage.FieldScreenItem("", "description text");

        final String expectedErrorMessageForAFieldScreenWithAnEmptyName =
                "You must enter a valid name.";

        final AddFieldScreenDialog addFieldScreenDialog = jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreensPage.class).
                openAddFieldScreenDialog();

        addFieldScreenDialog.
                setName(fieldScreenItemWithAnEmptyName.getName()).
                setDescription(fieldScreenItemWithAnEmptyName.getDescription()).
                submit();

        assertTrue(addFieldScreenDialog.isOpen());
        assertTrue(addFieldScreenDialog.hasFormErrors());

        final Map<String,String> addFieldScreenDialogFormErrors =
                addFieldScreenDialog.getFormErrors();

        assertEquals
                (
                        addFieldScreenDialogFormErrors.get("fieldScreenName"),
                        expectedErrorMessageForAFieldScreenWithAnEmptyName
                );
    }

    @Test
    public void shouldNotBeAbleToAddAFieldScreenGivenThatItsNameMatchesAnExistingFieldScreen()
    {
        final ViewFieldScreensPage.FieldScreenItem defaultFieldScreen =
                new ViewFieldScreensPage.
                        FieldScreenItem("Default Screen", "The default field screen");

        final String expectedErrorMessageForAFieldScreenWithDuplicateName =
                "A Screen with this name already exists.";

        final AddFieldScreenDialog addFieldScreenDialog = jira.gotoLoginPage().
                loginAsSysAdmin(ViewFieldScreensPage.class).
                openAddFieldScreenDialog();

        addFieldScreenDialog.
                setName(defaultFieldScreen.getName()).
                setDescription(defaultFieldScreen.getDescription()).
                submit();

        assertTrue(addFieldScreenDialog.isOpen());
        assertTrue(addFieldScreenDialog.hasFormErrors());

        final Map<String,String> addFieldScreenDialogFormErrors =
                addFieldScreenDialog.getFormErrors();

        assertEquals
                (
                        addFieldScreenDialogFormErrors.get("fieldScreenName"),
                        expectedErrorMessageForAFieldScreenWithDuplicateName
                );
    }

    @Test
    public void shouldBeAbleToAddAFieldScreenWhenTheLoggedInUserIsAJiraAdministrator()
    {
        final ViewFieldScreensPage.FieldScreenItem expectedFieldScreenItem =
                new ViewFieldScreensPage.
                        FieldScreenItem("A test field screen", "description text");

        jira.gotoLoginPage().
                login(ADMIN_USERNAME, ADMIN_PASSWORD, ViewFieldScreensPage.class).
                openAddFieldScreenDialog().
                setName(expectedFieldScreenItem.getName()).
                setDescription(expectedFieldScreenItem.getDescription()).
                submitSuccess();

    final ViewFieldScreensPage viewFieldScreensPage = jira.goTo(ViewFieldScreensPage.class);

        final Iterable<ViewFieldScreensPage.FieldScreenItem> actualFieldScreens =
                viewFieldScreensPage.getFieldScreens();

        assertTrue(contains(actualFieldScreens, expectedFieldScreenItem));
    }

    @Test
    public void shouldNotBeAbleToAddAFieldScreenWhenTheLoggedInUserIsNeitherAJiraOrSystemAdministrator()
    {
        jira.gotoLoginPage().loginAndGoToHome(FRED_USERNAME, FRED_PASSWORD);
        jira.visitDelayed(ViewFieldScreensPage.class);
        jira.visit(JiraLoginPage.class);
    }
}
