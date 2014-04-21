package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.User;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.UserClient;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the user resource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestUserResource extends RestFuncTest
{
    private static final String USER_PATH = "user";
    private static final String REST_PATH = "rest/api/2";
    private static final String REST_USER_URL = REST_PATH + "/" + USER_PATH;

    private UserClient userClient;

    public void testUserResourceNoUsername() throws Exception
    {
        Response response = userClient.getUserResponse(null);
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("The username query parameter was not provided"));
    }

    public void testUserResourceForUserThatDoesntExist() throws Exception
    {
        Response response = userClient.getUserResponse("bofh");
        assertEquals(404, response.statusCode);
        assertTrue(response.entity.errorMessages.contains("The user named 'bofh' does not exist"));
    }

    public void testUserResourceTimeZone() throws Exception
    {
       User user = userClient.get(ADMIN_USERNAME);
       assertEquals("Australia/Sydney", user.timeZone);
    }

    public void testSearchUsers()
    {
        List<User> users = userClient.search("fre", "0", null);

        assertEquals(1, users.size());
        User user = users.get(0);
        assertEquals("fred", user.name);
        assertEquals("Fred Normal", user.displayName);

        //empty search should return nothing
        users = userClient.search("", "0", null);
        assertEquals(0, users.size());

        //try searching via e-mail
        users = userClient.search("love", "0", null);
        assertEquals(1, users.size());
        assertEquals("\u611b", users.get(0).name);

        //searching for a should have 3 results
        users = userClient.search("a", null, null);
        assertEquals(3, users.size());
        assertEquals("a\\b", users.get(0).name);
        assertEquals("admin", users.get(1).name);
        assertEquals("sp ace", users.get(2).name);

        //now try paging
        users = userClient.search("a", "0", "1");
        assertEquals(1, users.size());
        assertEquals("a\\b", users.get(0).name);

        users = userClient.search("a", "1", "30");
        assertEquals(2, users.size());
        assertEquals("admin", users.get(0).name);
        assertEquals("sp ace", users.get(1).name);
    }

    public void testAssignableAndViewableUsers()
    {
        //only admins will be assignable and only devs can browse issues.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGNABLE_USER, "jira-developers");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGNABLE_USER, "jira-administrators");
        administration.permissionSchemes().defaultScheme().removePermission(BROWSE, "jira-users");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(BROWSE, "jira-developers");

        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        //searching for a should have 3 results
        List<User> users = userClient.search("a", null, null);
        assertEquals(3, users.size());
        assertEquals("a\\b", users.get(0).name);
        assertEquals("admin", users.get(1).name);
        assertEquals("sp ace", users.get(2).name);

        //only admin is an admin and can there for be assigned issues
        users = userClient.searchAssignable("a", issueKey, null, null);
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).name);

        //these are the only devs which can browse issues
        users = userClient.searchViewableIssue("a", issueKey, null, null);
        assertEquals(2, users.size());
        assertEquals("a\\b", users.get(0).name);
        assertEquals("admin", users.get(1).name);
    }

    // JRA-27212
    public void testUnprivilegedAccessToBrowseUsersYieldsNoResults()
    {
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        // only admins may browse users.
        backdoor.permissions().removeGlobalPermission(USER_PICKER, "jira-users");
        backdoor.permissions().removeGlobalPermission(USER_PICKER, "jira-developers");
        backdoor.permissions().addGlobalPermission(USER_PICKER, "jira-administrators");

        List<User> users = userClient.searchViewableIssue("a", issueKey, null, null);
        assertEquals(3, users.size());

        userClient.loginAs("fred");

        users = userClient.searchViewableIssue("a", issueKey, null, null);
        assertEquals(0, users.size());
    }

    // JRA-27212
    public void testUnprivilegedAccessToAssignableUsersIsDenied()
    {
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        // nobody is allowed to assign issues.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-developers");
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-administrators");

        Response response = userClient.getResponse(userClient.getSearchAssignableResource("a", issueKey, null, null));
        assertEquals(401, response.statusCode);
    }
    
    // JRA-27212
    public void testDoNotRequireBrowseUserPermissionToListAssignableUsers()
    {
        String issueKey = navigation.issue().createIssue("homosapien", "Bug", "Sample Issue");

        // only developers are allowed to assign issues.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-users");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGN_ISSUE, "jira-developers");
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGN_ISSUE, "jira-administrators");
        // only admins may be assigned to.
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGNABLE_USER, "jira-users");
        administration.permissionSchemes().defaultScheme().removePermission(ASSIGNABLE_USER, "jira-developers");
        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(ASSIGNABLE_USER, "jira-administrators");
        // neither group is allowed to browse users.
        administration.permissionSchemes().defaultScheme().removePermission(USER_PICKER, "jira-users");
        administration.permissionSchemes().defaultScheme().removePermission(USER_PICKER, "jira-developers");
        administration.permissionSchemes().defaultScheme().removePermission(USER_PICKER, "jira-administrators");

        // log in as a developer.
        userClient.loginAs("c/d", "c/d");

        // ask for a list of assignable people.
        List<User> users = userClient.searchAssignable("a", issueKey, null, null);
        // admin should be returned, because he is the only person who is assignable.
        // "a\\b" should not be returned, as he is a developer.
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).name);
    }
    
    public void testUserResourceGroupsNotExpanded() throws Exception
    {
        final String username = FRED_USERNAME;
        final String userPath = getPathFor(username);

        User user = userClient.get(username);
        assertEquals(getBaseUrlPlus(userPath), user.self);

        // verify that groups are not expanded
        assertEquals("groups", user.expand);

        assertNotNull(user.groups.size);
        assertEquals(1, user.groups.size);

        assertNotNull(user.groups.items);
        assertTrue(user.groups.items.isEmpty());
    }

    public void testUserResourceGroupsExpanded() throws Exception
    {
        final String username = FRED_USERNAME;
        final String userPath = getPathFor(username);

        User user = userClient.get(username, User.Expand.groups);
        assertEquals(getBaseUrlPlus(userPath), user.self);

        assertNotNull(user.groups);
        assertEquals(1, user.groups.size);

        assertNotNull(user.groups.items);
        assertEquals(1, user.groups.items.size());
        assertEquals("jira-users", user.groups.items.get(0).name());
    }

    public void testGetAnonymouslyUserResource() throws Exception
    {
        Response response = userClient.anonymous().getUserResponse("fred");
        assertEquals(401, response.statusCode);
    }

    public void testUnicodeCharacters() throws Exception
    {
        // Unicode 611B = chinese symbol for love
        final String username = "\u611B";
        final String userPath = getPathFor("%E6%84%9B");

        User user = userClient.get(username);
        assertEquals(getBaseUrlPlus(userPath), user.self);

        // Name, id and display name should have UTF-8 encoded Chinese symbols
        assertEquals("\u611b", user.name);
        assertEquals("\u611b \u6237", user.displayName);
        // URLs should have the Unicode character's UTF-8 encoded then the bytes are Percent-encoded
        assertEquals(getBaseUrlPlus(REST_USER_URL + "?username=%E6%84%9B"), user.self);
    }

    public void testAvatarUrls() throws Exception
    {
        final String username = "fred";
        User user = userClient.get(username);

        // Note that the avatar urls no longer contain the username, and therefore do not have any url encoding concerns
        final Map<String, String> avatarUrls = MapBuilder.<String, String>newBuilder()
                .add("16x16", getBaseUrlPlus("secure/useravatar?size=small&avatarId=10062"))
                .add("48x48", getBaseUrlPlus("secure/useravatar?avatarId=10062"))
                .toMap();
        assertEquals(avatarUrls, user.avatarUrls);
    }

    /*
     * These encoding-related tests are all in the same method for performance reasons.
     */
    public void testUsernamesWithInterestingCharacters() throws Exception
    {
        assertUserRepresentationIsOK("a\\b", "a%5Cb");          // backslash
        assertUserRepresentationIsOK("c/d", "c/d");             // slash
        assertUserRepresentationIsOK("sp ace", "sp+ace");       // space
        assertUserRepresentationIsOK("pl+us", "pl%2Bus");       // +
        assertUserRepresentationIsOK("per%cent", "per%25cent"); // %
        assertUserRepresentationIsOK("\u611B", "%E6%84%9B"); // %
    }

    public void testUserResourceShouldMaskEmailAddresses() throws Exception
    {
        administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.MASKED);
        User user = userClient.get("fred");
        assertThat(user.emailAddress, equalTo("fred at example dot com"));
    }

    public void testUserResourceShouldHideEmailAddresses() throws Exception
    {
        administration.generalConfiguration().setUserEmailVisibility(GeneralConfiguration.EmailVisibility.HIDDEN);
        User user = userClient.get("fred");
        assertNull(user.emailAddress);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        userClient = new UserClient(getEnvironmentData());
        administration.restoreData("TestUserResource.xml");
    }

    /**
     * Creates the path for the user resource.
     *
     * @param username a String containing the user name
     * @return the path to the user
     */
    protected String getPathFor(String username)
    {
        return getPathFor(username, emptyMap());
    }

    /**
     * Tests that the user representation is being constructed correctly.
     *
     * @param username the username
     * @param encodedUsername the encoded username (if encoding is necessary)
     */
    private void assertUserRepresentationIsOK(String username, String encodedUsername)
    {
        final String userPath = getPathFor(encodedUsername);

        User user = userClient.get(username);
        assertEquals(username, user.name);
        // explicitly assert that the self URL has the encoded username
        assertEquals("The username is not encoded in the self link", getBaseUrlPlus(REST_USER_URL + "?username=" + encodedUsername), user.self);
    }

    /**
     * Creates the path for the user resource, optionally appending any additional query parameters.
     *
     * @param username a String containing the user name
     * @param queryParams a Map containing query parameters
     * @return the path to the user
     */
    protected String getPathFor(String username, Map<?, ?> queryParams)
    {
        // append the query params in "&key=value" format
        return REST_USER_URL + "?username=" + username + StringUtils.join(Collections2.transform(queryParams.entrySet(), new Function<Map.Entry, Object>()
        {
            public Object apply(Map.Entry from)
            {
                return String.format("&%s=%s", from.getKey(), from.getValue());
            }
        }), "");
    }
}
