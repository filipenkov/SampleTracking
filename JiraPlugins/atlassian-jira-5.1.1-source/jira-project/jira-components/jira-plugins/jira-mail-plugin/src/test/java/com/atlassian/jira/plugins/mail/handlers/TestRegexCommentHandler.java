package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.jira.service.util.handler.MessageUserProcessorImpl;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import javax.mail.MessagingException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class TestRegexCommentHandler extends AbstractTestCommentHandler
{
    private RegexCommentHandler regexCommentHandler;
    private HashMap<String, String> params;
    private static final String KEY_REGEX = "splitregex";



    @Before
    public void setUpRegex() throws Exception
    {
        regexCommentHandler = (RegexCommentHandler) handler;
        params = new HashMap<String, String>();
    }

    @Override
    protected AbstractMessageHandler createHandler() {
        return new RegexCommentHandler(permissionManager, issueUpdater, userManager, applicationProperties, jiraApplicationContext, mailLoggingManager,
                new MessageUserProcessorImpl(userManager));
    }

    @Test
    public void testMailWithPrecedenceBulkHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithPrecedenceBulkHeader();
    }

    @Test
    public void testMailWithIllegalPrecedenceBulkHeader() throws Exception
    {
        _testMailWithIllegalPrecedenceBulkHeader();
    }    

    @Test
    public void testMailWithDeliveryStatusHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithDeliveryStatusHeader();
    }

    @Test
    public void testMailWithAutoSubmittedHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithAutoSubmittedHeader();
    }

    @Test
    public void testMailWithCatchEmailMiss()
            throws Exception
    {
        _testCatchEmailSettings();
    }

    @Test
    public void testAddCommentOnly() throws MessagingException, GenericEntityException
    {
        _testAddCommentOnly();
    }

    @Test
    public void testAddCommentOnlyToMovedIssue() throws MessagingException, GenericEntityException
    {
        setupMovedIssue();
        _testAddCommentOnlyToMovedIssue();
    }

    @Test
    public void testAddCommentAndAttachment() throws MessagingException,  GenericEntityException
    {
        _testAddCommentAndAttachment();
    }

    @Test
    public void testAddAttachmentWithInvalidFilename() throws MessagingException,  GenericEntityException
    {
        _testAddMultipleAttachmentsWithInvalidAndValidFilenames();
    }

    @Test
    public void testAddCommentWithEmptyBodyAndAttachment()
            throws MessagingException, GenericEntityException
    {
        _testAddCommentWithNonMultipartAttachment();
    }

    @Test
    public void testAddCommentWithInlineAttachment() throws GenericEntityException, MessagingException
    {
        _testAddCommentWithNonMultipartInline();
    }

    @Test
    public void testInitialisesRegexFromInitParameters()
    {
        // setup
        String expectedRegexExpression = "expectedRegexExpression";
        params.put(KEY_REGEX, expectedRegexExpression);

        // expectations
        Assert.assertNull("Regex should be initialised to null", regexCommentHandler.getSplitRegex());

        // execute
        regexCommentHandler.init(params, monitor);

        // verify
        assertEquals(expectedRegexExpression, regexCommentHandler.getSplitRegex());
    }

    @Test
    public void testSplitsAnEmailBodyBasedOnRegexPattern() throws MessagingException
    {
        // setup
        params.put(KEY_REGEX, "==================================");
        regexCommentHandler.init(params, monitor);

        // expectations
        String expectedComment = "Comment";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment + "==================================Quote");

        // verify
        Assert.assertEquals("\n" + expectedComment + "\n\n", processedComment);
    }

    @Test
    public void testReturnsOriginalMailBodyIfNoSplitCouldBeMade()
    {
        // setup
        params.put(KEY_REGEX, "================somthing we wont find==================");
        regexCommentHandler.init(params, monitor);

        // expectations
        String expectedComment = "Comment with all sorts of stuff but nothing the regex can split on...";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment);

        // verify
        Assert.assertEquals(expectedComment, processedComment);
    }

    @Test
    public void testReturnsOriginalMailBodyIfNoRegexIsSpecified()
    {
        // setup
        regexCommentHandler.init(params, monitor);

        // expectations
        String expectedComment = "Doesnt really matter what is in this comment";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment);

        // verify
        Assert.assertEquals(expectedComment, processedComment);
    }

    @Test
    public void testReturnsOriginalMailBodyIfEncountersARegexError()
    {
        // setup
        params.put(KEY_REGEX, "===somthing with an error, like===[this should have a matching close bracket, but it doesnt");
        regexCommentHandler.init(params, monitor);

        // expectations
        String expectedComment = "Doesnt really matter what is in this comment";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment);

        // verify
        Assert.assertEquals(expectedComment, processedComment);
    }

    @Test
    public void testCorrectlyProcessesThisRealWorldExampleFromALotusNotesEmail()
    {
        // setup
        params.put(KEY_REGEX, "/Extranet\\s*jira.london.echonet/FMB/UK/EUROPE/GROUP@CRAPPYLOTUSNOTES/");
        regexCommentHandler.init(params, monitor);

        // expectations
        String expectedComment = "\nLets try again !\n" +
                "This is a comment\n" +
                "\n" +
                "With a few lines.\n" +
                "\n" +
                "But hopefully, below here, we have bugger all......\n\n";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(rawEmailbody);

        // verify
        Assert.assertEquals(expectedComment, processedComment);
    }

    private static String rawEmailbody = "\n" +
            "\n" +
            "Lets try again !\n" +
            "This is a comment\n" +
            "\n" +
            "With a few lines.\n" +
            "\n" +
            "But hopefully, below here, we have bugger all......\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Extranet\n" +
            "jira.london.echonet/FMB/UK/EUROPE/GROUP@CRAPPYLOTUSNOTES - 23/10/2005 18:58\n" +
            "\n" +
            "\n" +
            "\n" +
            "To:    Nick MINUTELLO\n" +
            "\n" +
            "cc:\n" +
            "\n" +
            "\n" +
            "Subject:    [JIRA] Commented: (DEMO-35) Testing 123\n" +
            "\n" +
            "\n" +
            "    [ http://jira.london.echonet/browse/DEMO-35?page=comments#comment-99154\n" +
            "    ]\n" +
            "\n" +
            "Nick Minutello commented on DEMO-35:\n" +
            "------------------------------------\n" +
            "\n" +
            "Here is my jam-spoon here is my pi?a colada\n" +
            "\n" +
            "> Testing 123\n" +
            "> -----------\n" +
            ">\n" +
            ">          Key: DEMO-35\n" +
            ">          URL: http://jira.london.echonet/browse/DEMO-35\n" +
            ">      Project: Demo Project\n" +
            ">         Type: Bug\n" +
            ">     Reporter: Nick Minutello\n" +
            ">     Priority: Blocker\n" +
            ">      Fix For: 5.4\n" +
            "\n" +
            ">\n" +
            ">\n" +
            "> hello\n" +
            "\n" +
            "--\n" +
            "This message is automatically generated by JIRA.\n" +
            "-\n" +
            "If you think it was sent incorrectly contact one of the administrators:\n" +
            "   http://jira.london.echonet/secure/Administrators.jspa\n" +
            "-\n" +
            "For more information on JIRA, see:\n" +
            "   http://www.atlassian.com/software/jira\n" +
            "\n" +
            "\n" +
            "";
}
