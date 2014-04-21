package com.atlassian.jira.webtests.ztests.comment;


import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.COMMENTS })
public class TestAddComment extends JIRAWebTest
{
    private static final String smartQuoteOpen = "\u201C";
    private static final String smartQuoteClose = "\u201D";
    private static final String htmlEscQuote = "&quot;";
    private static final String chineseChars = "\u963F\u725B\u54E5"; // UTF code for 3 chinese characters
    private static final String fiveHTMLquotes = htmlEscQuote + htmlEscQuote + htmlEscQuote + htmlEscQuote + htmlEscQuote;
    private static final String TEST_ADD_COMMENT_XML = "TestAddComment.xml";

    public TestAddComment(String name)
    {
        super(name);
    }

    public void testRemoveMe() throws Exception
    {
        //This is just here to ensure there is a test a test bamboo does not report and error.
    }

    //    Needs to be moved to selenium see: JRADEV-1844
    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testCannotAddCommentWithoutIssue()
    {
        gotoPage(page.addXsrfToken("/secure/AddComment.jspa"));
        tester.assertTitleEquals("Error - jWebTest JIRA installation");
        text.assertTextPresent(new CssLocator(tester, "#content .error"), "The issue no longer exists.");
    }

//    /**
//     * Test that the Smart quotes are escaped properly - JRA-4330
//     */
//   Commenting out with the filter that does this - tentative
//    public void testSmartQuoteRemoval()
//    {
//        gotoIssue(addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "summary"));
//        clickLink("comment-issue");
//        setFormElement("comment", smartQuoteOpen + chineseChars + smartQuoteClose);
//        submit();
//        assertTextNotPresent(fiveHTMLquotes);
//        assertTextPresent(htmlEscQuote);
////        assertTextPresent(htmlEscQuote + "???" + htmlEscQuote); // commented out as it *shouldn't* be '???'
//        assertTextPresentAfterText(htmlEscQuote, htmlEscQuote); //check that there are two quotes after each other
//    }

    public void testCommentVisiblityOrdering()
    {
        restoreData(TEST_ADD_COMMENT_XML);
        final String FIRST_GROUP_NAME = "a group";
        final String LAST_GROUP_NAME = "z group";
        createGroup(FIRST_GROUP_NAME);
        createGroup(LAST_GROUP_NAME);
        addUserToGroup(ADMIN_USERNAME, FIRST_GROUP_NAME);
        addUserToGroup(ADMIN_USERNAME, LAST_GROUP_NAME);
        gotoIssue("HSP-1");
        clickLink("comment-issue");
        setWorkingForm("comment-add");
        String[] commentLevels = getDialog().getOptionsFor("commentLevel");
        assertEquals(9, commentLevels.length);
        assertEquals("All Users", commentLevels[0]);
        assertEquals("Administrators", commentLevels[1]);
        assertEquals("Developers", commentLevels[2]);
        assertEquals("Users", commentLevels[3]);
        assertEquals(FIRST_GROUP_NAME, commentLevels[4]);
        assertEquals("jira-administrators", commentLevels[5]);
        assertEquals("jira-developers", commentLevels[6]);
        assertEquals("jira-users", commentLevels[7]);
        assertEquals(LAST_GROUP_NAME, commentLevels[8]);
    }

    public void testAddInvalidComment()
    {
        restoreData(TEST_ADD_COMMENT_XML);
        gotoIssue("HSP-1");

        // empty is not ok for AddComment action
        addComment("All Users", "");
        assertTextPresent("Comment body can not be empty!");

        // all spaces on the other hand is not considered kosher!
        gotoIssue("HSP-1");
        addComment("All Users", "     ");
        assertTextPresent("Comment body can not be empty!");
    }

    public void testAddCommentWithVisibility()
    {
        String allUsersComment = "This is a comment assigned to all users";
        String jiraUsersGroupComment = "this comment visible to jira-users group";
        String jiraUsersRoleComment = "this is a comment visible to Users role"; //role Users
        String jiraDevelopersGroupComment = "this is a comment visible to jira-developers group";
        String jiraDevelopersRoleComment = "this is a comment visible to Developers role"; //role Developers
        String jiraAdminsGroupComment = "this is a comment visible to jira-admin group";
        String jiraAdminsRoleComment = "this is a comment visible to Administrators role"; //Administrators role

        restoreData("TestBlankInstancePlusAFewUsers.xml");

        displayAllIssues();
        clickLinkWithText("test bug");

        //create comment visible to all users
        addComment("All Users", allUsersComment);

        //create comments visible to all jira users
        addComment("jira-users", jiraUsersGroupComment);
        addComment("Users", jiraUsersRoleComment);

        //create comments visible to jira developers
        addComment("jira-developers", jiraDevelopersGroupComment);
        addComment("Developers", jiraDevelopersRoleComment);

        //create comments visible to all admins
        addComment("jira-administrators", jiraAdminsGroupComment);
        addComment("Administrators", jiraAdminsRoleComment);

        List userComments = EasyList.build(allUsersComment, jiraUsersRoleComment, jiraUsersGroupComment);
        List devComments = EasyList.build(jiraDevelopersGroupComment, jiraDevelopersRoleComment);
        List adminComments = EasyList.build(jiraAdminsGroupComment, jiraAdminsRoleComment);

        // verify that Fred can see general comment but not others as he is not in the visibility groups
        checkCommentVisibility(FRED_USERNAME, "HSP-5", userComments, EasyList.mergeLists(devComments, adminComments, null));

        // verify that Admin can see all comments as he is not in all visibility groups
        //list userComments now becomes all comments as we have added two lists together
        checkCommentVisibility(ADMIN_USERNAME, "HSP-5", EasyList.mergeLists(userComments, devComments, adminComments), null);

        //verify that developer only user can only view developer comments
        checkCommentVisibility("devman", "HSP-5", EasyList.mergeLists(devComments, userComments, null), adminComments);

        //veryify that onlyadmin guy can only see admin stuff
        checkCommentVisibility("onlyadmin", "HSP-5", EasyList.mergeLists(adminComments, userComments, null), devComments);
    }

    public void testAddCommentErrorWhenLoggedOut()
    {
        restoreData(TEST_ADD_COMMENT_XML);
        logout();
        String theComment = "comment with html <input type=\"input\" id=\"invalid\"/>";
        String theCommentEscaped = "comment with html &lt;input type=&quot;input&quot; id=&quot;invalid&quot;/&gt;";
        page.getFreshXsrfToken();
        gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=10000&comment=" + theComment));
        assertTextPresent("It seems that you have tried to perform an operation which you are not permitted to perform.");
    }

    public void testAddCommentErrorWhenNoPermission()
    {
        restoreData(TEST_ADD_COMMENT_XML);
        removeGroupPermission(DEFAULT_PERM_SCHEME, COMMENT_ISSUE, Groups.USERS);
        String theComment = "comment with html <input type=\"input\" id=\"invalid\"/>";
        String theCommentEscaped = "comment with html &lt;input type=&quot;input&quot; id=&quot;invalid&quot;/&gt;";
        gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=10000&comment=" + theComment));
        assertTextPresent(ADMIN_FULLNAME + ", you do not have the permission to comment on this issue.");
    }

    public void testAddCommentErrorWhenIssueDoesNotExist()
    {
        restoreData(TEST_ADD_COMMENT_XML);
        deleteIssue("HSP-1");
        String theComment = "comment with html <input type=\"input\" id=\"invalid\"/>";
        String theCommentEscaped = "comment with html &lt;input type=&quot;input&quot; id=&quot;invalid&quot;/&gt;";
        gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=10000&comment=" + theComment));
        assertTextPresent("The issue no longer exists.");
        assertTextNotPresent(theComment); //should have been escaped
        assertTextPresent("The issue no longer exists.");
    }

    private void addComment(String visibleTo, String comment)
    {
        clickLink("comment-issue");
        selectOption("commentLevel", visibleTo);
        setFormElement("comment", comment);
        submit();
    }

    public void testAddCommentWithGroupButNotLoggedIn() throws Exception
    {
        String key = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "Test Issue");
        gotoIssue(key);
        assertTextPresent("Test Issue");

        final String id = getIssueIdWithIssueKey(key);

        gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=" + id + "&comment=Hello"));
        assertTextPresent("Test Issue");
        assertTextPresent("Hello");
        assertTextNotPresent("Ahoj");

        gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=" + id + "&comment=Ahoj&commentLevel=group%3Ajira-users"));
        assertTextPresent("Test Issue");
        assertTextPresent("Hello");
        assertTextPresent("Ahoj");

        /// Make HSP project visible to anonymous users
        addBrowseProjectPermissionToAnonymous();
        addCreateCommentPermissionToAnonymous();
        activateTimeTracking();
        logout();

        page.getFreshXsrfToken();
        gotoPage(page.addXsrfToken("/secure/AddComment.jspa?id=" + id + "&comment=Hola&commentLevel=group%3Ajira-users"));
        assertTextNotPresent("NullPointerException");
        assertTextPresent("You cannot add a comment for specific groups or roles, as your session has expired. Please log in and try again.");

        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testAddCommentCheckUpdatedDate() throws ParseException
    {
        restoreData(TEST_ADD_COMMENT_XML);
        gotoIssue("HSP-1");
        final String startTagNoQuote = "<span class=date>";
        text.assertTextPresent(new IdLocator(tester, "create-date"), "14/Aug/06 4:26 PM");
        text.assertTextPresent(new IdLocator(tester, "updated-date"), "14/Aug/06 4:26 PM");
        final String commentText = "This is my first test comment!";
        assertTextNotPresent(commentText);

        clickLink("comment-issue");
        setFormElement("comment", commentText);
        submit();

        assertTextPresent(commentText);

        //now check the updated time is the same as the one of the comment (kinda, since the updated time is pretty formatted while the comments is not, but we can assert the time)
        String dateString = locator.css("span.date").getNodes()[0].getNodeValue();
        SimpleDateFormat format = new SimpleDateFormat("dd/MMM/yy h:mm a");
        final Date date = format.parse(dateString);

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        String timeString = timeFormat.format(date);
        text.assertTextPresent(new IdLocator(tester, "create-date"), "14/Aug/06 4:26 PM");
        text.assertTextPresent(new IdLocator(tester, "updated-date"), timeString);
    }

    public void testAddTooLongComment()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        getBackdoor().advancedSettings().setTextFieldCharacterLengthLimit(10);
        navigation.issue().gotoIssue("HSP-1");

        addComment("All Users", "This is too long comment");
        assertTextPresent("The entered text is too long. It exceeds the allowed limit of 10 characters.");
    }

    public void testAddCommentWithTextLengthLimitOn()
    {
        administration.restoreData(TEST_ADD_COMMENT_XML);
        getBackdoor().advancedSettings().setTextFieldCharacterLengthLimit(10);
        navigation.issue().gotoIssue("HSP-1");

        final String correctCommentBody = "AllGood";
        addComment("All Users", correctCommentBody);
        assertions.comments(Collections.singletonList(correctCommentBody)).areVisibleTo("admin", "HSP-1");
    }

    private void addBrowseProjectPermissionToAnonymous()
    {
        goToDefaultPermissionScheme();
        clickLink("add_perm_10");
        checkCheckbox("type", "group");
        submit(" Add ");
    }

    private void addCreateCommentPermissionToAnonymous()
    {
        goToDefaultPermissionScheme();
        clickLink("add_perm_15");
        checkCheckbox("type", "group");
        submit(" Add ");
    }

    private void goToDefaultPermissionScheme()
    {
        navigation.gotoAdmin();
        clickLink("permission_schemes");
        clickLinkWithText("Default Permission Scheme");
    }

}
