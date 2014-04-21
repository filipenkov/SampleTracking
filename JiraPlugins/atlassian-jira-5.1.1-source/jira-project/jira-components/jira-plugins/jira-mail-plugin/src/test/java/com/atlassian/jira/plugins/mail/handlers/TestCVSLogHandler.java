package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comments.CommentManager;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore("This test is ignored and may be removed if CVSLogHandler is removed")
public class TestCVSLogHandler extends AbstractTestMessageHandler
{

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        handler = new CVSLogHandler();
    }

    @Test
    public void testHandleMessageDispatchesEvent() throws MessagingException
    {
        setupIssueAndMessage();
        message.setText("Log Message:\nThis fixes PRJ-1 bug.\n===================================================================\n");

        final Mock mock = new Mock(CommentManager.class);
        final Constraint[] constraints = new Constraint[]{
                P.IS_ANYTHING, // issue
                P.IS_ANYTHING, // author
                P.IS_ANYTHING, // body
                P.IS_ANYTHING, // groupLevel
                P.IS_ANYTHING, // roleLevelId
                P.IS_TRUE      // this must be TRUE, as we test that the event gets dispatched
        };
        mock.expectAndReturn("create", constraints, null);

        CVSLogHandler logHandler = new CVSLogHandler() {

            protected boolean canHandleMessage(Message message)
            {
                return true;
            }

            protected boolean hasUserPermissionToComment(GenericValue issue, User reporter)
            {
                return true;
            }


        };

        boolean result = logHandler.handleMessage(message, context);

        assertTrue(result);
    }

    @Test
    public void testHandleMessageUpdated() throws IOException
    {
        String fileName = "updated.txt";
        String message = getMessage(fileName);

        assertEquals("no message\n\n", new CVSLogHandler().getCommentArea(message));
    }

    @Test
    public void testHandleMessageAdded() throws IOException
    {
        String fileName = "added.txt";
        String message = getMessage(fileName);

        assertEquals("Move dashboard out of config/\n\n", new CVSLogHandler().getCommentArea(message));
    }

    @Test
    public void testHandleMessageDeleted() throws IOException
    {
        String fileName = "deleted.txt";
        String message = getMessage(fileName);

        assertEquals("Move dashboard out of config/\n\n", new CVSLogHandler().getCommentArea(message));
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

    private String getMessage(String fileName) throws IOException
    {
//        InputStream in = ClassLoaderUtils.getResourceAsStream(JiraTestUtil.TESTS_BASE + "/service/handler/" + fileName, this.getClass());
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//
//        CharArrayWriter writer = new CharArrayWriter();
//
//        int i;
//        while ((i = reader.read()) != -1)
//        {
//            writer.write(i);
//        }
//
//        return writer.toString();
        return null;
    }
}
