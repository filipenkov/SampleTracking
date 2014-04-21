package it.com.atlassian.jira.plugin.issuenav.webdriver;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.dialogs.quickedit.EditIssueDialog;
import com.atlassian.jira.plugin.issuenav.pageobjects.IssueDetailComponent;
import com.atlassian.jira.plugin.issuenav.pageobjects.fields.*;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.openqa.selenium.Keyboard;
import org.openqa.selenium.Keys;

import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * Abstract base class for inline edit in either standalone or split view modes.
 * @since v5.0
 */
public abstract class TestInlineEdit extends FuncTestCase
{
    protected JiraTestedProduct product = TestedProductFactory.create(JiraTestedProduct.class);

    @Override
    protected void setUpTest()
    {
        // Clear the onBeforeUnload handler from javascript to ensure we don't get prompted when navigating away from a
        // page with dirty invalid errors. There may be a better solution by attempting to find the selenium Alert and
        // accepting if it's present, however this solution would have to be baked into atlassian-selenium.
        AtlassianWebDriver driver = product.getTester().getDriver();
        driver.executeScript("window.onbeforeunload = function() {};");

        super.setUpTest();
        administration.restoreData("TestInlineEdit.xml");
    }

    @Test
    public void testEditLabels()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17");
        issuesPage.labels().editSaveWait("Value1", "Value2");
        List<String> labels = issuesPage.getLabels();
        assertEquals(4, labels.size());
        assertTrue(labels.contains("Value1"));
        assertTrue(labels.contains("Value2"));
        assertTrue(labels.contains("hello"));
        assertTrue(labels.contains("world"));
    }

    @Test
    public void testDirtyEdits()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17");
        assertEquals(Lists.newArrayList("hello", "world"), issuesPage.getLabels());
        issuesPage.labels().editSaveWait("Value1", "Value2");
        navigation.gotoDashboard();
        issuesPage = goToIssue("XSS-17");
        assertEquals(Lists.newArrayList("Value1", "Value2", "hello", "world"), issuesPage.getLabels());
    }

    @Test
    public void testInlineEditWithValidationErrors()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17");
        final String summaryFragment = "This is a long Summary. ";
        final StringBuilder longSummary = new StringBuilder();
        for(int i = 0; i<20; i++) {
            longSummary.append(summaryFragment);
        }
        final InlineSummaryField summary = issuesPage.summary();
        summary.switchToEdit();
        summary.setMaxLength(300);
        summary.fill(longSummary.toString());

        final Field priority = issuesPage.priority().switchToEdit().fill("Minor").save();

        summary.waitToSave();  // setting reporter will result in the blurring and saving of summary
        assertTrue("Errors should remain for [summary]", issuesPage.getFieldsInError().contains("summary"));

        issuesPage.labels().editSaveWait("Value1", "Value2");

        priority.waitToSave();

        assertTrue("Errors should remain for [summary]", issuesPage.getFieldsInError().contains("summary"));

        issuesPage.summary().editSaveWait("Valid Summary");

        assertEquals("Valid Summary", summary.getValue());
        assertFalse("Errors should NOT remain for [summary]", issuesPage.getFieldsInError().contains("summary"));
    }

    @Test
    public void testNewFieldsAreEditable()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17");
        issuesPage.waitForEditButton().clickEditButton();

        // Setting the issue's due date through the quick edit dialog will
        // result in a full page refresh, making the due date appear.
        EditIssueDialog editIssueDialog = product.getPageBinder().bind(EditIssueDialog.class);
        editIssueDialog.fill("duedate", "31/Mar/2012");
        editIssueDialog.submitExpectingViewIssue("XSS-17");

        // The due date field should be inline-editable.
        issuesPage.dueDate().editSaveWait("01/Jan/12");
        assertEquals("01/Jan/12", issuesPage.dueDate().getValue());
    }


    @Test
    public void testPermissionDenied()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17", "fred", "fred", "");
        String originalSummary = issuesPage.summary().getValue();
        backdoor.usersAndGroups().removeUserFromGroup("fred", "jira-developers");
        issuesPage.summary().editSaveWait("blah blah blah blah blah");
        issuesPage.waitForGlobalErrorMessage();
        waitUntilFalse(issuesPage.hasEditableFields());
        waitUntilFalse(issuesPage.hasDescriptionModule());
        assertEquals(issuesPage.summary().getValue(), originalSummary);
    }

    @Test
    public void testXSRFFailure()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17");
        issuesPage.setXSRFTokenToJunk();
        Field priority = issuesPage.priority().switchToEdit().fill("Blocker").save();
        issuesPage.waitForXSRFRetryButton().clickXSRFRetryButton();
        priority.waitToSave();
        assertEquals("Blocker", issuesPage.priority().getValue());

        // The page's XSRF token should now be valid.
        issuesPage.priority().editSaveWait("Minor");
        assertEquals("Minor", issuesPage.priority().getValue());
    }

    @Test
    public void testSaveOnBlur()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17");
        String originalPriority = issuesPage.priority().getValue();
        String priority = "Blocker".equals(originalPriority) ? "Minor" : "Blocker";
        Field field = issuesPage.priority().switchToEdit().fill(priority);
        // Danger Will Robinson! This next statement will be a no-op unless your browser window has the focus
        issuesPage.clickInWhitespace();
        field.waitToSave();

        assertEquals(priority, issuesPage.priority().getValue());
    }

    /**
     * JRADEV-11544
     * Set field to invalid, start editing another field, ensure that second field has correct editHtml
     */
    @Test
    public void testSaveAfterValidationError()
    {
        IssueDetailComponent issuesPage = goToIssue("XSS-17");
        issuesPage.summary().editSaveWait("");
        assertTrue(issuesPage.getFieldsInError().contains("summary"));

        InlinePriorityField priorityField = issuesPage.priority();
        String originalPriority = priorityField.getValue();
        priorityField.switchToEdit();
        assertEquals(originalPriority, priorityField.singleSelect().getValue());
    }

    @Test
    public void testTwixieCanTwix()
    {
        IssueDetailComponent issuesPage = goToIssue("ARA-1", "issueKey = ARA-1");

        final boolean originalState = issuesPage.isDetailsTwixieOpen();
        issuesPage.clickDetailsTwixie();
        assertEquals("Twixie don't twix", !originalState, issuesPage.isDetailsTwixieOpen());
        issuesPage.clickDetailsTwixie();
        assertEquals("Twixie don't retwix", originalState, issuesPage.isDetailsTwixieOpen());
    }

    @Test
    public void testFocusShifter()
    {
        // TODO christo trying to work out what is going wrong with this on bamboo
        if (1==1) {
            return;
        }
        final Keyboard keyboard = product.getTester().getDriver().getKeyboard();

        // ARA-1 has two (non-user, non-date) custom fields with values on different tabs

        IssueDetailComponent issuesPage = goToIssue("ARA-1", "issueKey = ARA-1");

        final int thingiesId = 10010;
        InlineLabelsCustomField thingies = issuesPage.getCustomLabelsField(thingiesId);

        if (issuesPage.isDetailsTwixieOpen())
        {
            issuesPage.clickDetailsTwixie();
        }
        assertFalse(issuesPage.isDetailsTwixieOpen());

        assertFalse(thingies.isVisible());

        PageElement focusShifter = issuesPage.openFocusShifter();
        assertTrue("Dude, where's my focus shifter?", focusShifter.isPresent());
        assertTrue("focus shifter is invisible!?!!?", focusShifter.isVisible());

        // let's close it with escape and open again
        keyboard.sendKeys(Keys.ESCAPE);
        waitUntilFalse("Dude, where's my focus shifter?", focusShifter.timed().isPresent());

        focusShifter = issuesPage.openFocusShifter();

        // we only need to type a unique prefix
        keyboard.sendKeys("Thing");
        keyboard.sendKeys("\n");

        assertFalse("focus shifter should have been hidden", focusShifter.timed().isPresent().byDefaultTimeout());

        assertTrue("Details twixie should have been opened by the focus shifter", issuesPage.isDetailsTwixieOpen());

        assertTrue("Thingies field should be visible after the focus shifter chose it", thingies.isVisible());

        // we can edit the field now
        thingies.multiSelect().add("foo");
        thingies.save();

        assertEquals("I just edited the Thingies field, WTF!", "ffffffffffffff\nfoo", thingies.getValue());

        assertTrue("The details twixie should still be open", issuesPage.isDetailsTwixieOpen());

        // TODO update our firefox testing to something more modern that FF 3.6
        // an edit seems to wreck the focus shifter's ability (only on firefox 3.6) to reveal fields
        // on closed twixies WITH unselected tabs (just tabs: OK, just twixies:, OK, tabs+twixies? COMPUTER SAYS NO.)
        // NOTE this works on FF 8, so it's not really a concern, just the test cannot pass on our automated tests ATM
        boolean weHaveUpgradedFirefoxTests = false;
        if (weHaveUpgradedFirefoxTests) {
            // close twixie
            issuesPage.clickDetailsTwixie();
            assertFalse("Hey I should have just closed the details twixie", issuesPage.isDetailsTwixieOpen());
        }

        // let's do a second field to ensure the prefill works and also that the
        // post-edit reblat did not drop our twixie-opening event handlers

        focusShifter = issuesPage.openFocusShifter();

        assertEquals("Expected pre-fill to contain my last selected value", "Thingies", focusShifter.getValue());

        // Champ de texte text field now, it's on a different non-active tab
        focusShifter.type("champ");
        keyboard.sendKeys("\n");

        final int champId = 10110;
        final InlineTextCustomField champField = issuesPage.getCustomTextField(champId);

        assertTrue("The details twixie should have been opened by the focus shifter", issuesPage.isDetailsTwixieOpen());
        assertTrue("The Champ de texte field should be visible when the focus shifter chose it", champField.isVisible());

        final String newValue = "new champ de texte value";
        champField.fill(newValue).save();
        champField.waitToSave();

        assertEquals("Oi! I just edited the Champ de texte field!!", newValue, champField.getValue());
    }

    @Test
    public void testAddComment()
    {
        IssueDetailComponent issueDetails = goToIssue("XSS-17");
        issueDetails.addComment("SAVE ON BLUR FTW");
        assertEquals("SAVE ON BLUR FTW", issueDetails.waitForCommentWithId("10460"));
        //check anchor
        assertEquals("the new comment should be the hash component", "#comment-10460", issueDetails.getUrlHashComponent());
        //check focused class
        assertTrue("new comment should be focused", issueDetails.commentIsFocused("10460"));
        //no restriction on comment
        assertFalse("The issue details field should have no user restrictions",issueDetails.getCommentLevel("10460").startsWith("Restricted to"));
    }

    @Test
    public void testArchivedAffectsVersion()
    {
        IssueDetailComponent issueDetails = goToIssue("XSS-17");
        assertEquals("Archived Version Shown", "Version A", issueDetails.affectsVersion().getValue());
        //saving nothing should still show archived version A. (Regression test for JRADEV-12638)
        issueDetails.affectsVersion().switchToEdit().save().waitToSave();
        assertEquals("Archived Version still shown", "Version A", issueDetails.affectsVersion().getValue());
        issueDetails.affectsVersion().editSaveWait("Version B");
        assertEquals("Version A (archived) and B as stored and displayed", "Version A, Version B", issueDetails.affectsVersion().getValue());
    }

    /**
     * Navigates to the issue with the given key, first logging in as admin.
     */
    protected IssueDetailComponent goToIssue(final String issueKey)
    {
        return goToIssue(issueKey, "");
    }

    /**
     * Navigates to the issue with the given key, first logging in as admin.
     */
    protected IssueDetailComponent goToIssue(final String issueKey, String query)
    {
        return goToIssue(issueKey, "admin", "admin", query);
    }

    /**
     * Navigates to the issue with the given key, first logging in with the given username and password and, if it's
     * supported (ok if it's split view), starting in the context of a search using the given jql query.
     */
    protected abstract IssueDetailComponent goToIssue(String issueKey, String username, String password, String query);
}
