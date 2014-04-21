package com.atlassian.jira.pageobjects.project.people;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.jira.pageobjects.project.EditProjectLeadAndDefaultAssigneeDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilEquals;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Represents the people panel in the project configuration page.
 *
 * @since v4.4
 */
public class PeoplePage extends AbstractJiraPage
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "project-config-panel-people-list")
    private PageElement peopleList;

    @ElementBy(id = "project-config-panel-people-project-lead")
    private PageElement projectLead;

    @ElementBy(id = "project-config-panel-people-default-assignee")
    private PageElement defaultAssignee;

    @ElementBy(id = "edit_project_lead")
    private PageElement editProjectLead;

    @ElementBy(id = "edit_default_assignee")
    private PageElement editDefaultAssignee;

    @ElementBy(id = "project-config-panel-people-project-lead-avatar")
    private PageElement projectLeadAvatar;

    @ElementBy(id = "project-config-people-table", timeoutType = TimeoutType.AJAX_ACTION)
    private PageElement table;

    private final String projectKey;

    @Inject
    private PageElementFinder elementFinder;

    public PeoplePage(String projectKey)
    {
        this.projectKey = projectKey;
    }


    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey + "/people";
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(
                    table.timed().isPresent(),
                    Conditions.not(isTableLoading())
                );
    }

    public String getProjectLead()
    {
        return projectLead.getText();
    }

    public String getDefaultAssignee()
    {
        return defaultAssignee.getText();
    }

    public boolean isProjectLeadNonExistentIndicated()
    {
        return projectLead.find(By.className("errLabel")).isPresent();
    }

    public boolean isProjectLeadNotAssignableIndicated()
    {
        return peopleList.find(By.className("project-config-invalid")).isPresent();
    }

    public boolean isDefaultAssigneeUserHoverEnabled()
    {
        final PageElement a = projectLead.find(By.tagName("a"));
        return a.isPresent() && a.hasClass("user-hover");
    }

    public EditProjectLeadAndDefaultAssigneeDialog openEditProjectLeadDialog()
    {
        editProjectLead.click();
        return pageBinder.bind(EditProjectLeadAndDefaultAssigneeDialog.class);
    }

    public EditProjectLeadAndDefaultAssigneeDialog openEditDefaultAssigneeDialog()
    {
        editDefaultAssignee.click();
        return pageBinder.bind(EditProjectLeadAndDefaultAssigneeDialog.class);
    }

    public boolean isProjectLeadAvatarPresent()
    {
        return projectLeadAvatar.isPresent();
    }

    public String getProjectLeadAvatarSrc()
    {
        return projectLeadAvatar.getAttribute("src");
    }

    public List<PeopleRole> getRoles()
    {
        waitUntilFalse(isTableLoading());
        //for some reason the previous check isn't good enough!
        waitUntilTrue(table.find(By.className("jira-restfultable-row")).timed().isPresent());

        final List<PeopleRole> roles = Lists.newArrayList();
        final List<PageElement> roleRows = table.findAll(By.className("jira-restfultable-row"));
        for (final PageElement roleRow : roleRows)
        {
            roles.add(pageBinder.bind(PeopleRoleImpl.class, roleRow.getAttribute("data-id")));
        }
        return roles;
    }

    public TimedCondition isTableLoading()
    {
        return table.timed().hasClass("loading");
    }

    public PeopleRole getRoleByName(final String name)
    {
        final List<PeopleRole> roles = getRoles();
        for (final PeopleRole role : roles)
        {
            if(role.getName().equals(name))
            {
                return role;
            }
        }
        return null;
    }

    public String getServerError()
    {
        final PageElement dialog = elementFinder.find(By.cssSelector("#server-error-dialog.aui-dialog-content-ready"));
        waitUntilTrue(dialog.timed().isPresent());
        return dialog.find(By.className("aui-message")).getText();
    }

    public static class PeopleRoleImpl implements PeopleRole
    {
        @Inject
        private PageElementFinder elementFinder;

        @Inject
        private PageBinder pageBinder;

        private PageElement row;

        private final String id;

        @Init
        public void initialize()
        {
            this.row = elementFinder.find(By.id("people-" + id + "-row"));
        }

        public PeopleRoleImpl(final String id)
        {
            this.id = id;
        }

        public List<User> getUsers()
        {
            final List<User> users = Lists.newArrayList();
            final List<PageElement> userElements = row.findAll(By.cssSelector(".project-config-role-users li"));
            for (final PageElement userElement : userElements)
            {
                users.add(pageBinder.bind(UserImpl.class, userElement));
            }
            return users;
        }

        public List<Group> getGroups()
        {
            final List<Group> groups = Lists.newArrayList();
            final List<PageElement> groupElements = row.findAll(By.cssSelector(".project-config-role-groups li"));
            for (final PageElement groupElement : groupElements)
            {
                groups.add(pageBinder.bind(GroupImpl.class, groupElement));
            }
            return groups;
        }

        public String getName()
        {
            return row.find(By.className("project-config-role-name")).getText();
        }

        @Override
        public EditPeopleRoleForm edit(final String dataFieldName)
        {
            waitForEventsToBeEnabled();
            row.find(By.cssSelector(".jira-restfultable-editable[data-field-name=\"" + dataFieldName + "\"]")).click();

            return pageBinder.bind(EditPeopleRoleForm.class, row.getAttribute("data-id"));
        }

        public void waitForEventsToBeEnabled()
        {
            waitUntilEquals("", row.find(By.className("project-config-role-name")).timed().getAttribute("style"));
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder()
                    .append(getName())
                    .append(getUsers())
                    .append(getGroups())
                    .hashCode();
        }

        @Override
        public boolean equals(final Object other)
        {
            if(!(other instanceof PeopleRole))
            {
                return false;
            }
            final PeopleRole rhs = (PeopleRole) other;
            return new EqualsBuilder()
                    .append(getName(), rhs.getName())
                    .append(getUsers(), rhs.getUsers())
                    .append(getGroups(), rhs.getGroups())
                    .isEquals();
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append(getName())
                    .append(getUsers())
                    .append(getGroups())
                    .toString();
        }

        public static class UserImpl implements User
        {
            private PageElement userElement;

            public UserImpl(final PageElement userElement)
            {
                this.userElement = userElement;
            }

            public String getName()
            {
                return userElement.getText();
            }

//            public String getAvatarSrc()
//            {
//                return userElement.find(By.className("project-config-icon-avatar")).getAttribute("src");
//            }

            @Override
            public int hashCode()
            {
                return new HashCodeBuilder()
                        .append(getName())
//                        .append(getAvatarSrc())
                        .hashCode();
            }

            @Override
            public boolean equals(final Object other)
            {
                if(!(other instanceof User))
                {
                    return false;
                }
                final User rhs = (User) other;
                return new EqualsBuilder()
                        .append(getName(), rhs.getName())
//                        .append(getAvatarSrc(), rhs.getAvatarSrc())
                        .isEquals();
            }

            @Override
            public String toString()
            {
                return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .append(getName())
//                        .append(getAvatarSrc())
                        .toString();
            }
        }

        public static class GroupImpl implements Group
        {
            private PageElement groupElement;

            public GroupImpl(final PageElement groupElement)
            {
                this.groupElement = groupElement;
            }

            public String getName()
            {
                return groupElement.getText();
            }

            public boolean hasGroupIcon()
            {
                return groupElement.find(By.className("project-config-icon-projectlead")).isPresent();
            }

            @Override
            public int hashCode()
            {
                return new HashCodeBuilder()
                        .append(getName())
                        .append(hasGroupIcon())
                        .hashCode();
            }

            @Override
            public boolean equals(final Object other)
            {
                if(!(other instanceof Group))
                {
                    return false;
                }
                final Group rhs = (Group) other;
                return new EqualsBuilder()
                        .append(getName(), rhs.getName())
                        .append(hasGroupIcon(), rhs.hasGroupIcon())
                        .isEquals();
            }

            @Override
            public String toString()
            {
                return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                        .append(getName())
                        .append(hasGroupIcon())
                        .toString();
            }

        }

    }
}
