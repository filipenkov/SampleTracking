package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

import java.util.List;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.or;


/**
 * Page for moving users between groups
 *
 * @since v4.4
 */
public class MoveUserToGroupPage extends AbstractJiraPage
{
    private static final String URI = "/secure/admin/user/EditUserGroups!default.jspa?name=%s";

    private final String username;

    @ElementBy(id = "userGroupPicker")
    private PageElement userGroupPickerContainer;

    @ElementBy(name = "groupsToLeave")
    private SelectElement joinedGroups;

    @ElementBy(name = "groupsToJoin")
    private SelectElement availablegroups;

    @ElementBy(name = "join")
    private PageElement join;

    @ElementBy(name = "leave")
    private PageElement leave;

    public MoveUserToGroupPage(final String username)
    {
        this.username = username;
    }

    @Override
    public String getUrl()
    {
        return String.format(URI, username);
    }

    @Override
    public TimedCondition isAt()
    {
        return and(userGroupPickerContainer.timed().isPresent(), hasAvailableGroupsSelectOrInfoMessage(),
                hasGroupsToLeaveSelectOrInfoMessage());
    }

    private TimedCondition hasAvailableGroupsSelectOrInfoMessage()
    {
        return or(availablegroups.timed().isPresent(),
                messageInUserPickerContainer().timed().hasText("User is a member of all groups."));
    }

    private TimedCondition hasGroupsToLeaveSelectOrInfoMessage()
    {
        return or(joinedGroups.timed().isPresent(),
                messageInUserPickerContainer().timed().hasText("User isn't a member of any groups."));
    }

    private PageElement messageInUserPickerContainer()
    {
        return userGroupPickerContainer.find(By.cssSelector(".aui-message.info"));
    }

    public void addTo(final List<String> groups)
    {
        for (String group : groups)
        {
            availablegroups.select(Options.text(group));
        }
        join.click();
    }

    public void removeFrom(final List<String> groups)
    {
        for (String group : groups)
        {
            joinedGroups.select(Options.text(group));
        }
        leave.click();
    }
}
