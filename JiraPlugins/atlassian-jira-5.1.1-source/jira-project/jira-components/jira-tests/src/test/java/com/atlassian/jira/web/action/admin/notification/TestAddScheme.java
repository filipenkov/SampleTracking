package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import org.junit.Test;
import webwork.action.Action;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static webwork.action.ServletActionContext.setResponse;

public class TestAddScheme extends ListeningTestCase
{
    private NotificationSchemeManager notificationSchemeManager = mock(NotificationSchemeManager.class);

    @Test
    public void shouldReturnAnErrorIfTheSchemeNameWasNotSet() throws Exception
    {
        final AddScheme addSchemeAction = new AddScheme(notificationSchemeManager)
        {
            @Override
            protected I18nHelper getI18nHelper()
            {
                return new MockI18nHelper();
            }
        };
        final String actionResult = addSchemeAction.execute();

        assertEquals(Action.INPUT, actionResult);
        assertEquals(1, addSchemeAction.getErrors().size());
        assertEquals("admin.errors.specify.a.name.for.this.scheme", addSchemeAction.getErrors().get("name"));
    }

    @Test
    public void addingASchemeShouldRedirectToTheEditPageForThatScheme() throws Exception
    {
        final Scheme expectedNotificationScheme = new Scheme
                (
                        1L, "NotificationScheme", "A Test Notification Scheme",
                        Collections.<SchemeEntity>emptyList()
                );

        final HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        setResponse(servletResponse);

        when(notificationSchemeManager.createSchemeObject(anyString(), anyString())).thenReturn(expectedNotificationScheme);

        AddScheme addSchemeAction = new AddScheme(notificationSchemeManager);
        addSchemeAction.setName("This scheme");

        String result = addSchemeAction.execute();

        assertEquals(Action.NONE, result);
        verify(servletResponse).sendRedirect("EditNotifications!default.jspa?schemeId=" + expectedNotificationScheme.getId());
    }
}
