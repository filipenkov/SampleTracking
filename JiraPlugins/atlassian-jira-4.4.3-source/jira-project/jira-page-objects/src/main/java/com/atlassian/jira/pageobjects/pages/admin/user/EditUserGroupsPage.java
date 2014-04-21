package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.MultiSelectElement;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.Check;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashSet;
import java.util.Set;

import static com.atlassian.pageobjects.elements.query.Conditions.and;

/**
 * @since 4.4
 */
public class EditUserGroupsPage extends AbstractJiraPage
{

    private static final String URI = "/secure/admin/user/EditUserGroups.jspa";
    private static String ERROR_SELECTOR = ".aui-message.error ul li";

    private Set<String> errors = new HashSet<String>();

    // TODO how is it different than MoveUserToGroupPage???

    @ElementBy(id = "userGroupPicker")
    private PageElement userGroupPickerContainer;

    @FindBy (id = "return_link")
    private WebElement returnLink;

    @FindBy (name = "join")
    private WebElement joinButton;

    @FindBy (name = "leave")
    private WebElement leaveButton;

    @ElementBy (name = "groupsToJoin")
    private MultiSelectElement groupsToJoinSelect;

    @ElementBy (name = "groupsToLeave")
    private MultiSelectElement groupsToLeaveSelect;

    @FindBy (name = "jiraform")
    private WebElement editGroupsForm;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return and(userGroupPickerContainer.timed().isPresent(), groupsToJoinSelect.timed().isPresent(),
                groupsToLeaveSelect.timed().isPresent());
    }

    @Init
    public void parsePage()
    {
        if (Check.elementExists(ByJquery.$(ERROR_SELECTOR), driver))
        {
            for (WebElement el : driver.findElements(ByJquery.$(ERROR_SELECTOR)))
            {
                errors.add(el.getText());
            }
        }
    }

    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    public boolean hasError(String errorStr)
    {
        return errors.contains(errorStr);
    }

    public ViewUserPage returnToUserView()
    {
        returnLink.click();

        return pageBinder.bind(ViewUserPage.class);
    }

    /**
     * Add to groups either redirects the user to another page or returns the user to
     * the EditUserGroupsPage.
     * @param groups
     * @return
     */
    public <T extends Page> T addToGroupsAndReturnToPage(Class<T> pageClass, String ... groups)
    {
        selectGroups(groupsToJoinSelect, groups);

        joinButton.click();

        return pageBinder.bind(pageClass);
    }

    public EditUserGroupsPage addToGroupsExpectingError(String ... groups)
    {
        return addToGroupsAndReturnToPage(EditUserGroupsPage.class, groups);
    }

    public <T extends Page> T removeFromGroupsAndReturnToPage(Class<T> pageClass, String ... groups)
    {
        selectGroups(groupsToLeaveSelect, groups);

        leaveButton.click();

        return pageBinder.bind(pageClass);
    }

    public EditUserGroupsPage removeFromGroupsExpectingError(String ... groups)
    {
        return removeFromGroupsAndReturnToPage(EditUserGroupsPage.class, groups);
    }

    private void selectGroups(MultiSelectElement select, String ... groups)
    {
        for (String group : groups)
        {
            select.select(Options.value(group));
        }
    }
}
