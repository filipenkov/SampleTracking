package com.atlassian.jira.pageobjects.project.people;

import com.atlassian.jira.pageobjects.components.MultiSelect;
import com.atlassian.jira.pageobjects.components.NoBrowseUsersUserPicker;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.by;

/**
 * Form for editing a role on the people tab.
 *
 * @since v4.4
 */
public class EditPeopleRoleForm
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    @Inject
    private Timeouts timeouts;

    private final String id;

    private PageElement editableRow;
    private PageElement editableUsersDiv;
    private PageElement groupTextAreaDiv;

    private MultiSelect editableUsers;
    private MultiSelect editableGroups;

    @Init
    public void initialize()
    {
        editableRow = elementFinder.find(By.id("people-" + id + "-row"));

        editableUsersDiv = elementFinder.find(By.id("project-config-people-users-select-textarea"));

        boolean canBrowseUsers = this.elementFinder.find(By.cssSelector("input[title='currentUserCanBrowseUsers']")).getAttribute("value").equals("true");
        if (canBrowseUsers) {
            editableUsers = pageBinder.bind(MultiSelect.class, "project-config-people-users-select");
        } else {
            editableUsers = pageBinder.bind(NoBrowseUsersUserPicker.class, "project-config-people-users-select");
        }
        groupTextAreaDiv = elementFinder.find(By.id("project-config-people-groups-select-textarea"));
        editableGroups = pageBinder.bind(MultiSelect.class, "project-config-people-groups-select");

        Poller.waitUntil(isPresent(), Matchers.is(true), by(timeouts.timeoutFor(TimeoutType.AJAX_ACTION)));
    }

    public TimedQuery<Boolean> isPresent()
    {
        return Conditions.and(
                editableUsersDiv.timed().isPresent(),
                groupTextAreaDiv.timed().isPresent()
        );
    }

    public EditPeopleRoleForm(final String id)
    {
        this.id = id;
    }

    @Nonnull
    public EditPeopleRoleForm addUser(final String user)
    {
        editableUsers.add(user);
        return this;
    }

    public String addUserWithError(final String user)
    {
        editableUsers.addNotWait(user);
        return editableUsers.waitUntilError();
    }

    @Nonnull
    public EditPeopleRoleForm removeUser(final String user)
    {
        editableUsers.remove(user);
        return this;
    }

    @Nonnull
    public EditPeopleRoleForm addGroup(final String group)
    {
        editableGroups.add(group);
        return this;
    }

    @Nonnull
    public EditPeopleRoleForm removeGroup(final String group)
    {
        editableGroups.remove(group);
        return this;
    }

    @Nonnull
    public EditPeopleRoleForm clearGroups()
    {
        editableGroups.clear();
        return this;
    }

    @Nonnull
    public EditPeopleRoleForm clearUsers()
    {
        editableUsers.clear();
        return this;
    }

    public void submit()
    {
        editableRow.find(By.cssSelector(".aui-button")).click();
    }

    public void cancel()
    {
        editableRow.find(By.className("aui-button-cancel")).click();
    }

}
