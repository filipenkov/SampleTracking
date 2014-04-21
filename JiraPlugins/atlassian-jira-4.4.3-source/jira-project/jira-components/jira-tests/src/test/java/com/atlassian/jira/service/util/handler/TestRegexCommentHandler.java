package com.atlassian.jira.service.util.handler;

import org.ofbiz.core.entity.GenericEntityException;

import javax.mail.MessagingException;
import java.util.HashMap;

public class TestRegexCommentHandler extends AbstractTestCommentHandler
{
    private RegexCommentHandler regexCommentHandler;
    private HashMap params;
    private static final String KEY_REGEX = "splitregex";

    public TestRegexCommentHandler(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        regexCommentHandler = (RegexCommentHandler) handler;
        params = new HashMap();
    }

    @Override
    protected AbstractMessageHandler createHandler() {
        return new RegexCommentHandler();
    }

    public void testMailWithPrecedenceBulkHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithPrecedenceBulkHeader();
    }

    public void testMailWithIllegalPrecedenceBulkHeader() throws Exception
    {
        _testMailWithIllegalPrecedenceBulkHeader();
    }    

    public void testMailWithDeliveryStatusHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithDeliveryStatusHeader();
    }

    public void testMailWithAutoSubmittedHeader() throws MessagingException, GenericEntityException
    {
        _testMailWithAutoSubmittedHeader();
    }

    public void testMailWithCatchEmailMiss()
            throws Exception
    {
        _testCatchEmailSettings();
    }

    public void testAddCommentOnly() throws MessagingException, GenericEntityException
    {
        _testAddCommentOnly();
    }

    public void testAddCommentOnlyToMovedIssue() throws MessagingException, GenericEntityException
    {
        setupMovedIssue();
        _testAddCommentOnlyToMovedIssue();
    }

    public void testAddCommentAndAttachment() throws MessagingException,  GenericEntityException
    {
        _testAddCommentAndAttachment();
    }

    public void testAddAttachmentWithInvalidFilename() throws MessagingException,  GenericEntityException
    {
        _testAddMultipleAttachmentsWithInvalidAndValidFilenames();
    }

    public void testAddCommentWithEmptyBodyAndAttachment()
            throws MessagingException, GenericEntityException
    {
        _testAddCommentWithNonMultipartAttachment();
    }

    public void testAddCommentWithInlineAttachment() throws GenericEntityException, MessagingException
    {
        _testAddCommentWithNonMultipartInline();
    }

    public void testInitialisesRegexFromInitParameters()
    {
        // setup
        String expectedRegexExpression = "expectedRegexExpression";
        params.put(KEY_REGEX, expectedRegexExpression);

        // expectations
        assertNull("Regex should be initialised to null", regexCommentHandler.getSplitRegex());

        // execute
        regexCommentHandler.init(params);

        // verify
        assertEquals(expectedRegexExpression, regexCommentHandler.getSplitRegex());
    }

    public void testSplitsAnEmailBodyBasedOnRegexPattern() throws MessagingException
    {
        // setup
        params.put(KEY_REGEX, "==================================");
        regexCommentHandler.init(params);

        // expectations
        String expectedComment = "Comment";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment + "==================================Quote");

        // verify
        assertEquals("\n" + expectedComment + "\n\n", processedComment);
    }

    public void testReturnsOriginalMailBodyIfNoSplitCouldBeMade()
    {
        // setup
        params.put(KEY_REGEX, "================somthing we wont find==================");
        regexCommentHandler.init(params);

        // expectations
        String expectedComment = "Comment with all sorts of stuff but nothing the regex can split on...";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment);

        // verify
        assertEquals(expectedComment, processedComment);
    }

    public void testReturnsOriginalMailBodyIfNoRegexIsSpecified()
    {
        // setup
        regexCommentHandler.init(params);

        // expectations
        String expectedComment = "Doesnt really matter what is in this comment";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment);

        // verify
        assertEquals(expectedComment, processedComment);
    }


    public void testReturnsOriginalMailBodyIfEncountersARegexError()
    {
        // setup
        params.put(KEY_REGEX, "===somthing with an error, like===[this should have a matching close bracket, but it doesnt");
        regexCommentHandler.init(params);

        // expectations
        String expectedComment = "Doesnt really matter what is in this comment";

        // execute
        String processedComment = regexCommentHandler.splitMailBody(expectedComment);

        // verify
        assertEquals(expectedComment, processedComment);
    }

    public void testCorrectlyProcessesThisRealWorldExampleFromALotusNotesEmail()
    {
        // setup
        params.put(KEY_REGEX, "/Extranet\\s*jira.london.echonet/FMB/UK/EUROPE/GROUP@CRAPPYLOTUSNOTES/");
        regexCommentHandler.init(params);

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
        assertEquals(expectedComment, processedComment);
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
