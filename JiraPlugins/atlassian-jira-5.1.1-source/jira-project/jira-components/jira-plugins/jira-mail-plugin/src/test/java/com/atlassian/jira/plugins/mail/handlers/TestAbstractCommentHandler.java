package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.jira.event.type.EventType;
import org.junit.Test;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TestAbstractCommentHandler
{
    @Test
    public void testCorrectEventTypeDispatched() throws MessagingException
    {
      //check ISSUE_COMMENTED event is dispatched
        //for null
        assertEquals(EventType.ISSUE_COMMENTED_ID, AbstractCommentHandler.getEventTypeId(null));
        //for emptylist
        assertEquals(EventType.ISSUE_COMMENTED_ID, AbstractCommentHandler.getEventTypeId(Collections.EMPTY_LIST));
        //for new empty list
        ArrayList attachmentsChangeItems = new ArrayList();
        assertEquals(EventType.ISSUE_COMMENTED_ID, AbstractCommentHandler.getEventTypeId(attachmentsChangeItems));

      //check ISSUE_UPDATED event is dispatched
        //for non-empty list
        attachmentsChangeItems.add("non-empty list");
        assertEquals(EventType.ISSUE_UPDATED_ID, AbstractCommentHandler.getEventTypeId(attachmentsChangeItems));
    }
}
