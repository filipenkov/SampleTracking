package com.atlassian.jira.webtest.webdriver.tests.admin.issue.types;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.admin.AdminSummaryPage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddIssueType;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddIssueTypeDialog;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.AddIssueTypePage;
import com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage;
import com.atlassian.jira.pageobjects.util.UserSessionHelper;
import com.atlassian.jira.pageobjects.websudo.JiraSudoFormDialog;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudo;
import com.atlassian.jira.pageobjects.websudo.JiraWebSudoPage;
import com.atlassian.jira.pageobjects.xsrf.Xsrf;
import com.atlassian.jira.pageobjects.xsrf.XsrfDialog;
import com.atlassian.jira.pageobjects.xsrf.XsrfPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.Page;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static com.atlassian.jira.pageobjects.pages.admin.issuetype.ViewIssueTypesPage.IssueType;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for adding issue types by the dialog.
 *
 * @since v5.0.1
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.ISSUE_TYPES })
public class TestAddIssueTypes extends BaseJiraWebTest
{
    public static final String DEFAULT_ICON_URL = "/images/icons/genericissue.gif";
    public static final String FIELD_NAME = "name";
    public static final String ICONURL_FIELD = "iconurl";

    @Test
    public void testCreateIssueTypeDialog()
    {
        backdoor.restoreBlankInstance();
        final ViewIssueTypesPage pages = jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testIssueTypeAdd(new Function<Void, AddIssueTypeDialog>()
        {
            @Override
            public AddIssueTypeDialog apply(@Nullable Void input)
            {
                return pages.addIssueType();
            }
        });
    }

    @Test
    public void testCreateIssueTypePage()
    {
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testIssueTypeAdd(new Function<Void, AddIssueTypePage>()
        {
            @Override
            public AddIssueTypePage apply(@Nullable Void input)
            {
                return jira.goTo(AddIssueTypePage.class);
            }
        });
    }

    private void testIssueTypeAdd(Function<Void, ? extends AddIssueType> opener)
    {
        IssueType issueTypeWithDescription = new IssueType("IssueTypeWithDescription",
            "Here is a description", false, DEFAULT_ICON_URL);

        IssueType withoutDescription = new IssueType("IssueTypeWithoutDescription",
                null, false, DEFAULT_ICON_URL);

        IssueType withIcon = new IssueType("IssueTypeIcon",
                null, false, "/other/example/wait.gif");

        IssueType subtask = new IssueType("IssueTypeSubtask",
                null, true, DEFAULT_ICON_URL);

        backdoor.subtask().disable();

        AddIssueType addIssueType = opener.apply(null);

        //We should not see the subtask selector when subtasks are disabled.
        assertFalse(addIssueType.isSubtasksEnabled());

        //Create an issue type with simple Name and Description.
        
        addIssueType.setName(issueTypeWithDescription.getName())
                .setDescription(issueTypeWithDescription.getDescription()).submit(ViewIssueTypesPage.class);

        //Create an issue type without a description.
        addIssueType = opener.apply(null);
        addIssueType.setName(withoutDescription.getName()).submit(ViewIssueTypesPage.class);

        //Create an issue with a different icon.
        addIssueType = opener.apply(null);
        addIssueType.setName(withIcon.getName()).setIconUrl(withIcon.getIconUrl()).submit(ViewIssueTypesPage.class);

        backdoor.subtask().enable();

        addIssueType = opener.apply(null);

        //We should see the subtask selector.
        assertTrue(addIssueType.isSubtasksEnabled());

        //Create a subtask.
        ViewIssueTypesPage typesPage = addIssueType.setName(subtask.getName()).setSubtask(subtask.isSubtask()).submit(ViewIssueTypesPage.class);

        final List<IssueType> issueTypes = typesPage.getIssueTypes();
        assertThat(issueTypes, Matchers.<IssueType>hasItems(issueType(withoutDescription), issueType(withIcon),
                issueType(issueTypeWithDescription), issueType(subtask)));
    }

    @Test
    public void testIconPickerOnDialog()
    {
        backdoor.restoreBlankInstance();
        final ViewIssueTypesPage pages = jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testIconPicker(new Function<Void, AddIssueTypeDialog>()
        {
            @Override
            public AddIssueTypeDialog apply(@Nullable Void input)
            {
                return pages.addIssueType();
            }
        });
    }

    @Test
    public void testIconPickerOnPage()
    {
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testIconPicker(new Function<Void, AddIssueTypePage>()
        {
            @Override
            public AddIssueTypePage apply(@Nullable Void input)
            {
                return jira.goTo(AddIssueTypePage.class);
            }
        });
    }

    private void testIconPicker(Function<Void, ? extends AddIssueType> opener)
    {
        AddIssueType addIssueType = opener.apply(null);

        final IconPicker.IconPickerPopup iconPickerPopup = addIssueType.openIconPickerPopup();

        //Does clicking an icon work?
        assertTrue(iconPickerPopup.selectIcon("health.gif"));
        assertThat(addIssueType.getIconUrl(), endsWith("health.gif"));

        //Does typing in a value work?
        addIssueType.openIconPickerPopup();
        final String url = "/other/ur/itworks.png";
        iconPickerPopup.submitIconUrl(url);
        assertThat(addIssueType.getIconUrl(), equalTo(url));

        addIssueType.cancel(ViewIssueTypesPage.class);
    }
    
    @Test
    public void testErrorsPage()
    {
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testErrors(new Function<Void, AddIssueTypePage>()
        {
            @Override
            public AddIssueTypePage apply(@Nullable Void input)
            {
                return jira.goTo(AddIssueTypePage.class);
            }
        });
    }
    
    @Test
    public void testErrorsDialog()
    {
        backdoor.restoreBlankInstance();
        final ViewIssueTypesPage page = jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testErrors(new Function<Void, AddIssueTypeDialog>()
        {
            @Override
            public AddIssueTypeDialog apply(@Nullable Void input)
            {
                return page.addIssueType();
            }
        });
    }

    private void testErrors(Function<Void, ? extends AddIssueType> opener)
    {
        AddIssueType addIssueType = opener.apply(null);

        addIssueType.setName(null).submitFail();
        
        //Issue type with no name.
        assertThat(addIssueType.getFormErrors(), hasEntry(FIELD_NAME, "You must specify a name."));

        //Issue type with no IconUrl.
        addIssueType.setName("AnotherName").setIconUrl(null).submitFail();
        assertThat(addIssueType.getFormErrors(),
                hasEntry(ICONURL_FIELD, "You must specify a URL for the icon of this new issue type."));

        //Issue type with duplicate name.
        addIssueType.setName("Bug").setIconUrl(DEFAULT_ICON_URL).submitFail();
        assertThat(addIssueType.getFormErrors(),
                hasEntry(FIELD_NAME, "An issue type with this name already exists."));

        //Make sure things can succeed.
        ViewIssueTypesPage typesPage = addIssueType.setName("Bug2").submit(ViewIssueTypesPage.class);
        assertThat(typesPage.getIssueTypes(), Matchers.<IssueType>hasItem(
                issueType(new IssueType("Bug2", null, false, DEFAULT_ICON_URL))));
    }
    
    @Test
    public void testWebSudoDialog() throws IOException
    {
        backdoor.restoreBlankInstance();
        final ViewIssueTypesPage page = jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testWebsudo(new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraSudoFormDialog apply(@Nullable Void input)
            {
                return page.addIssueTypeAndBind(JiraSudoFormDialog.class, AddIssueTypeDialog.ID);
            }
        }, AddIssueTypeDialog.class, ViewIssueTypesPage.class);
    }

    @Test
    public void testWebSudoPage() throws IOException
    {
        backdoor.restoreBlankInstance();
        jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testWebsudo(new Function<Void, JiraWebSudo>()
        {
            @Override
            public JiraWebSudoPage apply(@Nullable Void input)
            {
                jira.visitDelayed(AddIssueTypePage.class);
                return pageBinder.bind(JiraWebSudoPage.class);
            }
        }, AddIssueTypePage.class, AdminSummaryPage.class);
    }

    private void testWebsudo(Function<Void, JiraWebSudo> opener,
            Class<? extends AddIssueType> nextPage, Class<?> cancelPage)
    {
        final IssueType newIssueType = new IssueType("AnotherTest", null, false, DEFAULT_ICON_URL);

        backdoor.websudo().enable();

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.clearWebSudo();

        JiraWebSudo formDialog = opener.apply(null);
        formDialog = formDialog.authenticateFail("otherpassword");
        formDialog.cancel(cancelPage);

        formDialog = opener.apply(null);
        final AddIssueType issueTypeDialog = formDialog.authenticate(FunctTestConstants.ADMIN_PASSWORD, nextPage);
        ViewIssueTypesPage typesPage = issueTypeDialog.setName(newIssueType.getName()).submit(ViewIssueTypesPage.class);
        assertThat(typesPage.getIssueTypes(), Matchers.<IssueType>hasItem(issueType(newIssueType)));

        backdoor.websudo().disable();
    }

    @Test
    public void testSubmitSessionTimeoutDialog()
    {
        final ViewIssueTypesPage page = jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testSessionTimeout(ViewIssueTypesPage.class, new TimeoutHelper() {
            @Override
            public AddIssueType openAddIssueType()
            {
                return page.addIssueType();
            }

            @Override
            public Xsrf submitExpired(AddIssueType addIssueType)
            {
                return addIssueType.submitFail(XsrfDialog.class, AddIssueTypeDialog.ID);
            }
        });
    }

    @Test
    public void testSubmitSessionTimeoutPage()
    {
        jira.gotoLoginPage().loginAsSysAdmin(ViewIssueTypesPage.class);
        testSessionTimeout(AddIssueTypePage.class, new TimeoutHelper() {
            @Override
            public AddIssueType openAddIssueType()
            {
                return pageBinder.navigateToAndBind(AddIssueTypePage.class);
            }

            @Override
            public Xsrf submitExpired(AddIssueType addIssueType)
            {
                return addIssueType.submitFail(XsrfPage.class);
            }
        });
    }

    /**
     * Callback for abstracting page/dialog differences.
     */
    interface TimeoutHelper
    {
        AddIssueType openAddIssueType();
        Xsrf submitExpired(AddIssueType addIssueType);
    }
    
    private <P extends Page> void testSessionTimeout(Class<P> nextPage, TimeoutHelper timeoutHelper)
    {
        final AddIssueType addIssueType = timeoutHelper.openAddIssueType();
        addIssueType.setName("name");

        final UserSessionHelper userSessionHelper = pageBinder.bind(UserSessionHelper.class);
        userSessionHelper.invalidateSession();

        // check that we get the "session expired" page
        Xsrf xsrf = timeoutHelper.submitExpired(addIssueType);
        assertTrue(xsrf.isSessionExpired());
        assertTrue(xsrf.hasParamaters());

        final JiraLoginPage jiraLoginPage = xsrf.login();
        jiraLoginPage.loginAsSystemAdminAndFollowRedirect(nextPage);
    }

    private static IssueTypeMatcher issueType(IssueType expected)
    {
        return new IssueTypeMatcher(expected);
    }

    private static class IssueTypeMatcher extends BaseMatcher<ViewIssueTypesPage.IssueType>
    {
        private final IssueType expected;

        private IssueTypeMatcher(IssueType expected)
        {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object o)
        {
            if (o instanceof IssueType)
            {
                IssueType actual = (IssueType) o;
                return StringUtils.equals(actual.getName(), expected.getName()) &&
                        StringUtils.equals(actual.getDescription(), expected.getDescription()) &&
                        StringUtils.endsWith(actual.getIconUrl(), expected.getIconUrl()) &&
                        actual.isSubtask() == expected.isSubtask();
            }
            else
            {
                return false;
            }
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendValue(expected);
        }
    }
}
