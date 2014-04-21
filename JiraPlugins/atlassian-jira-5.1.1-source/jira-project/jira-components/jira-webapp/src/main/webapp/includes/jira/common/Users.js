/**
 * A namespace containing functions to handle users,
 * groups and roles in JIRA.
 *
 * @namespace JIRA.Users
 */
JIRA.Users = {};

/**
 * Represents the user that is currently logged in JIRA.
 *
 * @namespace JIRA.Users.LoggedInUser
 */
JIRA.Users.LoggedInUser = {};

/**
 * Retrieves the user name the user that is currently logged in JIRA.
 *
 * @return A {String} containing the user name.
 */
JIRA.Users.LoggedInUser.userName = function() {
    return AJS.Meta.get("remote-user");
};

/**
 * Whether the user that is currently logged in JIRA is anonymous or not.
 *
 * @return {Boolean} true if the currently logged in user is anonymous; otherwise false.
 */
JIRA.Users.LoggedInUser.isAnonymous = function() {
    return AJS.Meta.get("remote-user") === "";
};