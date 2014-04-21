/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.event;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

public class TestFireIssueEventFunction extends AbstractUsersTestCase
{
    private Mock mockEventPublisher;
    private static final String BASE_URL = "www.jira.atlassian.com";
    private EventPublisher oldEventPublisher;

    public TestFireIssueEventFunction(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        PropertiesManager.getInstance().getPropertySet().setString(APKeys.JIRA_BASEURL, BASE_URL);

        mockEventPublisher = new Mock(EventPublisher.class);
        oldEventPublisher = (EventPublisher) ManagerFactory.addService(EventPublisher.class, (EventPublisher) mockEventPublisher.proxy()).getComponentInstance();
    }

    @Override
    protected void tearDown()
    {
        ManagerFactory.addService(EventPublisher.class, oldEventPublisher);
    }

    public void testExecute() throws GenericEntityException
    {
        final FireIssueEventFunction function = new FireIssueEventFunction();

        final Map transientVars = new HashMap();
        final GenericValue issue = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1), "assignee", "Test Assignee", "key", "Test-1",
            "resolution", "Test Resolution"));

        // Create the issue object
        final IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        final Issue issueObject = issueFactory.getIssue(issue);

        final GenericValue changegroup = EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", new Long(1), "author", "bob"));

        final CommentManager commentManager = ComponentManager.getComponentInstanceOfType(CommentManager.class);
        final Comment comment = commentManager.create(issueObject, "bob", "Test Body", "Test Level", null, false);

        transientVars.put("issue", issueObject);
        transientVars.put("commentValue", comment);
        transientVars.put("changeGroup", changegroup);

        final Map args = EasyMap.build("eventTypeId", EventType.ISSUE_CREATED_ID);

        final Mock mockWFContext = new Mock(WorkflowContext.class);
        transientVars.put("context", mockWFContext.proxy());

        final User bob = UtilsForTests.getTestUser("bob");
        mockWFContext.setupResult("getCaller", "bob");

        final Map params = new HashMap();
        params.put("baseurl", BASE_URL);
        params.put("eventsource", IssueEventSource.WORKFLOW);

        mockEventPublisher.expectVoid("publish", P.ANY_ARGS);

        function.execute(transientVars, args, null);

        mockEventPublisher.verify();
    }

    public void testExecuteWithNoEventType()
    {
        final FireIssueEventFunction function = new FireIssueEventFunction();
        function.execute(new HashMap(), new HashMap(), null);
        mockEventPublisher.verify();
    }

    public void testMakeDescriptor()
    {
        final FunctionDescriptor descriptor = FireIssueEventFunction.makeDescriptor(EventType.ISSUE_CREATED_ID);
        assertEquals(EventType.ISSUE_CREATED_ID, descriptor.getArgs().get("eventTypeId"));
    }
}
