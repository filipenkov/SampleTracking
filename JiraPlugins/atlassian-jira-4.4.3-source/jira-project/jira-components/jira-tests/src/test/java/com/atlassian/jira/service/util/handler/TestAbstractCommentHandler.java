package com.atlassian.jira.service.util.handler;

import com.atlassian.jira.service.util.handler.AbstractCommentHandler;
import com.atlassian.jira.event.type.EventType;

import javax.mail.MessagingException;

import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.Collections;
import java.util.ArrayList;

public class TestAbstractCommentHandler extends AbstractUsersTestCase
{
    public TestAbstractCommentHandler(String s)
    {
        super(s);
    }

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
