package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.Check;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.4
 */
public class UserBrowserPage extends AbstractJiraPage
{
    private static final String URI = "/secure/admin/user/UserBrowser.jspa";

    private String MAX = "1000000";
    private String TEN = "10";
    private String TWENTY = "20";
    private String FIFTY = "50";
    private String ONE_HUNDRED = "100";

    private final Map<String, User> users;

    @FindBy(id="filter_link")
    private WebElement filterSubmit;

    @ElementBy (id = "add_user")
    private PageElement addUserLink;

    @ElementBy (id = "results-count-total")
    private PageElement numUsers;

    @FindBy (name = "max")
    private WebElement usersPerPageDropdown;

    @FindBy (id = "user_browser_table")
    private WebElement userTable;

    public UserBrowserPage()
    {
        users = new HashMap<String, User>();
    }

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("user_browser_table")).timed().isPresent();
    }

    @Init
    public void init()
    {
        setUserFilterToShowAllUsers();
        getUsers();
    }

    public boolean hasUser(User user)
    {
        return users.containsKey(user.getUserName());
    }

    /**
     * When editing a users groups from this page, EditUserGroups always returns back to
     * UserBrowser unless there was an error.
     * @param user
     * @return
     */
    public EditUserGroupsPage editUserGroups(User user)
    {

        if (hasUser(user))
        {
            String editGroupsId = "editgroups_" + user.getUserName();

            driver.findElement(By.id(editGroupsId)).click();

            return pageBinder.bind(EditUserGroupsPage.class);
        }
        else
        {
            throw new IllegalStateException("User: " + user.getUserName() + " was not found.");
        }

    }

    public Set<String> getUsersGroups(User user)
    {

        if (hasUser(user))
        {
            Set<String> groups = new HashSet<String>();
            WebElement groupCol = userTable.findElements(ByJquery.$("#" + user.getUserName()).parents("tr.vcard").find("td")).get(4);

            for (WebElement groupEl : groupCol.findElements(By.tagName("a")))
            {
                groups.add(groupEl.getText());
            }

            return groups;
        }
        else
        {
            throw new IllegalStateException("User: " + user.getUserName() + " was not found.");
        }
    }

    public ViewUserPage gotoViewUserPage(User user)
    {
        if (hasUser(user))
        {
            User actualUser = users.get(user.getUserName());
            WebElement userEmailLink = driver.findElement(By.id(actualUser.getUserName()));
            userEmailLink.click();

            return pageBinder.bind(ViewUserPage.class);
        }
        else
        {
            throw new IllegalStateException("The user: " + user + " was not found on the page");
        }
    }

    public int getNumberOfUsers()
    {
        return Integer.valueOf(numUsers.getText());
    }

    public UserBrowserPage filterByUserName(String username)
    {
        driver.findElement(By.name("userNameFilter")).sendKeys(username);
        filterSubmit.click();

        return pageBinder.bind(UserBrowserPage.class);
    }

    /**
     * navigates to the addUserPage by activating the add User link
     * @return
     */
    public AddUserPage gotoAddUserPage()
    {
        addUserLink.click();

        return pageBinder.bind(AddUserPage.class);
    }

    /**
     * Takes User object and fills out the addUserPage form and creates the user.
     * @param user the user to create
     * @param sendPasswordEmail sets the send email tick box to on or off
     * @return the user browser page which should have the new user added to the count.
     */
    public ViewUserPage addUser(User user, boolean sendPasswordEmail)
    {
        AddUserPage addUserPage = gotoAddUserPage();
        return addUserPage.addUser(user.getUserName(), user.getPassword(), user.getFullName(), user.getEmail(), sendPasswordEmail).createUser();
    }

    private void setUserFilterToShowAllUsers()
    {
        usersPerPageDropdown.findElement(By.cssSelector("option[value=\"" + MAX + "\"]")).setSelected();
        filterSubmit.click();
    }

    private void getUsers()
    {
        users.clear();

        List<WebElement> rows = userTable.findElements(By.tagName("tr"));

        for (WebElement row : rows)
        {
            // Check it's not the headings (th) tags.
            if (Check.elementExists(By.tagName("td"), row))
            {
                List<WebElement> cols = row.findElements(By.tagName("td"));

                String username = cols.get(0).getText();
                String email = cols.get(1).getText();
                String fullName = cols.get(2).getText();

//                Set<Group> groups = new HashSet<Group>();
//
//                for (WebElement group : cols.get(4).findElements(By.tagName("a")))
//                {
//                    groups.add(new Group(group.getText()));
//                }

                users.put(username, new User(username, fullName, email, null));
            }
        }

    }
}
