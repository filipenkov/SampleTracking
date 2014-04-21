package com.atlassian.jira.service.util.handler;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.issue.comments.CommentManager;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestCVSLogHandler extends AbstractTestMessageHandler
{
    public TestCVSLogHandler(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        handler = new CVSLogHandler();
    }

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

            protected CommentManager getCommentManager()
            {
                return (CommentManager) mock.proxy();
            }

        };

        boolean result = logHandler.handleMessage(message);

        assertTrue(result);
    }

    public void testHandleMessageUpdated() throws IOException
    {
        String fileName = "updated.txt";
        String message = getMessage(fileName);

        assertEquals("no message\n\n", new CVSLogHandler().getCommentArea(message));
    }

    public void testHandleMessageAdded() throws IOException
    {
        String fileName = "added.txt";
        String message = getMessage(fileName);

        assertEquals("Move dashboard out of config/\n\n", new CVSLogHandler().getCommentArea(message));
    }

    public void testHandleMessageDeleted() throws IOException
    {
        String fileName = "deleted.txt";
        String message = getMessage(fileName);

        assertEquals("Move dashboard out of config/\n\n", new CVSLogHandler().getCommentArea(message));
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

    private String getMessage(String fileName) throws IOException
    {
        InputStream in = ClassLoaderUtils.getResourceAsStream(JiraTestUtil.TESTS_BASE + "/service/handler/" + fileName, this.getClass());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        CharArrayWriter writer = new CharArrayWriter();

        int i;
        while ((i = reader.read()) != -1)
        {
            writer.write(i);
        }

        return writer.toString();
    }
}
