package com.atlassian.jira.event;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.internal.AsynchronousAbleEventDispatcher;
import com.atlassian.event.internal.EventExecutorFactoryImpl;
import com.atlassian.event.internal.EventPublisherImpl;
import com.atlassian.event.internal.EventThreadPoolConfigurationImpl;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventListener;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.easymock.classextension.EasyMock;

/**
 * @since v4.1
 */
public class TestAtlassianEvents extends LegacyJiraMockTestCase
{

    private EventPublisher eventPublisher;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        eventPublisher = new EventPublisherImpl(new AsynchronousAbleEventDispatcher(new EventExecutorFactoryImpl(new EventThreadPoolConfigurationImpl())), new JiraListenerHandlerConfigurationImpl());
    }

    public void testIssueEventCorrectlyInvoked() throws Exception
    {
        IssueEvent issueEvent = new IssueEvent(null, null, null, 1L);
        final IssueEventListener listener = EasyMock.createMock(IssueEventListener.class);
        listener.workflowEvent(issueEvent);
        EasyMock.expectLastCall();
        EasyMock.replay(listener);

        eventPublisher.register(listener);
        eventPublisher.publish(issueEvent);
        EasyMock.verify(listener);
    }

    public void testUserSignupEventCorrectlyInvoked() throws Exception
    {
        UserEvent userEvent = new UserEvent(null, UserEventType.USER_SIGNUP);
        final UserEventListener listener = EasyMock.createMock(UserEventListener.class);
        listener.userSignup(userEvent);
        EasyMock.expectLastCall();
        EasyMock.replay(listener);

        eventPublisher.register(listener);
        eventPublisher.publish(userEvent);
        EasyMock.verify(listener);
    }

    public void testUserCreatedEventCorrectlyInvoked() throws Exception
    {
        UserEvent userEvent = new UserEvent(null, UserEventType.USER_CREATED);
        final UserEventListener listener = EasyMock.createMock(UserEventListener.class);
        listener.userCreated(userEvent);
        EasyMock.expectLastCall();
        EasyMock.replay(listener);

        eventPublisher.register(listener);
        eventPublisher.publish(userEvent);
        EasyMock.verify(listener);
    }

    public void testUserForgetPasswordEventCorrectlyInvoked() throws Exception
    {
        UserEvent userEvent = new UserEvent(null, UserEventType.USER_FORGOTPASSWORD);
        final UserEventListener listener = EasyMock.createMock(UserEventListener.class);
        listener.userForgotPassword(userEvent);
        EasyMock.expectLastCall();
        EasyMock.replay(listener);

        eventPublisher.register(listener);
        eventPublisher.publish(userEvent);
        EasyMock.verify(listener);
    }

    public void testUserForgotUsernameEventCorrectlyInvoked() throws Exception
    {
        UserEvent userEvent = new UserEvent(null, UserEventType.USER_FORGOTUSERNAME);
        final UserEventListener listener = EasyMock.createMock(UserEventListener.class);
        listener.userForgotUsername(userEvent);
        EasyMock.expectLastCall();
        EasyMock.replay(listener);

        eventPublisher.register(listener);
        eventPublisher.publish(userEvent);
        EasyMock.verify(listener);
    }

}
