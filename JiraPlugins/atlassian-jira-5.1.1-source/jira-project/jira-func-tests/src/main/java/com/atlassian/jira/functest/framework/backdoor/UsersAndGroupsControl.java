package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.WebResource;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate Users and
 * Groups.
 *
 * Replaces many uses of {@link com.atlassian.jira.functest.framework.admin.UsersAndGroups}.
 *
 * See com.atlassian.jira.dev.backdoor.UsersAndGroupsBackdoor in jira-func-test-plugin for backend.
 *
 * @since v5.0
 */
public class UsersAndGroupsControl extends BackdoorControl<UsersAndGroupsControl>
{
    public UsersAndGroupsControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public UsersAndGroupsControl addUser(String username)
    {
        addUser(username, username, username, username + "@example.com");
        return this;
    }

    public UsersAndGroupsControl addUser(String username, String password, String displayName, String email) {
        addUser(username, password, displayName, email, false);
        return this;
    }

    public UsersAndGroupsControl addUser(String username, String password, String displayName, String email, boolean sendEmail)
    {
        get(createResource().path("user").path("add")
                .queryParam("userName", username)
                .queryParam("password", password)
                .queryParam("displayName", displayName)
                .queryParam("email", email)
                .queryParam("sendEmail", "" + sendEmail)
        );
        return this;
    }

    public UsersAndGroupsControl addUserEvenIfUserExists(String username)
    {
        addUserEvenIfUserExists(username, username, username, username + "@example.com");
        return this;
    }

    public UsersAndGroupsControl addUserEvenIfUserExists(String username, String password, String displayName, String email) {
        addUserEvenIfUserExists(username, password, displayName, email, false);
        return this;
    }

    public UsersAndGroupsControl addUserEvenIfUserExists(String username, String password, String displayName, String email, boolean sendEmail)
    {
        get(createResource().path("user").path("addEvenIfUserExists")
                .queryParam("userName", username)
                .queryParam("password", password)
                .queryParam("displayName", displayName)
                .queryParam("email", email)
                .queryParam("sendEmail", "" + sendEmail)
        );
        return this;
    }

    /**
     * Makes a lot of new users, fast, and adds them to jira-users.
     *
     * The created user will have a password matching their username and their email address will be:
     *
     *   {usernamePrefix} + index + "@example.com"
     *
     * @param usernamePrefix prefix before each new username, e.g. "testuser" becomes "testuser0"
     * @param displayNamePrefix prefix before each new username, e.g. "Test User " becomes "Test User 0"
     * @param numberOfNewUsers number of users to add
     */
    public UsersAndGroupsControl addUsers(String usernamePrefix, String displayNamePrefix, int numberOfNewUsers)
    {
        get(createResource().path("user").path("addMany")
                .queryParam("usernamePrefix", usernamePrefix)
                .queryParam("displayNamePrefix", displayNamePrefix)
                .queryParam("numberOfNewUsers", "" + numberOfNewUsers)
        );
        return this;
    }

    /**
     * Makes a lot of new users, fast, and adds them to the group specified (as well as jira-users).
     *
     * The created user will have a password matching their username and their email address will be:
     *
     *   {usernamePrefix} + index + "@example.com"
     *
     * @param usernamePrefix prefix before each new username, e.g. "testuser" becomes "testuser0"
     * @param displayNamePrefix prefix before each new username, e.g. "Test User " becomes "Test User 0"
     * @param numberOfNewUsers number of users to add
     * @param groupName name of group to add all users to
     */
    public void addUsersWithGroup(String usernamePrefix, String displayNamePrefix, int numberOfNewUsers, String groupName)
    {
        get(createResource().path("user").path("addMany")
                .queryParam("usernamePrefix", usernamePrefix)
                .queryParam("displayNamePrefix", displayNamePrefix)
                .queryParam("numberOfNewUsers", "" + numberOfNewUsers)
                .queryParam("groupName", groupName)
        );
    }

    public void deleteUser(String username)
    {
        get(createResource().path("user").path("delete").queryParam("userName", username));
    }

    public void addUserToGroup(String username, String groupName)
    {
        get(createResource().path("user").path("addToGroup").queryParam("userName", username).queryParam("groupName", groupName));
    }

    public void removeUserFromGroup(String username, String groupName)
    {
        get(createResource().path("user").path("removeFromGroup").queryParam("userName", username).queryParam("groupName", groupName));
    }

    public void addGroup(String groupName)
    {
        get(createResource().path("group").path("add").queryParam("groupName", groupName));
    }

    public UsersAndGroupsControl resetLoginCount(String username)
    {
        get(createResource().path("user").path("resetLoginCount").queryParam("user", username));
        return this;
    }

    public long getNumberOfUsers()
    {
        return getId(createResource().path("user").path("count"));
    }

    public long getNumberOfGroups()
    {
        return getId(createResource().path("group").path("count"));
    }

    @Override
    protected WebResource createResource()
    {
        return super.createResource().path("usersAndGroups");
    }
}
