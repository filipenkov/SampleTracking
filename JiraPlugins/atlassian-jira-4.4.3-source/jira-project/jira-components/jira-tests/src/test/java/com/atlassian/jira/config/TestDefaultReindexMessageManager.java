package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.web.util.MockOutlookDate;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.opensymphony.module.propertyset.PropertySet;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestDefaultReindexMessageManager extends MockControllerTestCase
{
    private JiraPropertySetFactory jiraPropertySetFactory;
    private UserFormatManager userFormatManager;
    private I18nHelper.BeanFactory i18nFactory;
    private OutlookDateManager outlookDateManager;
    private PropertySet propertySet;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private User user = null;

    @Before
    public void setUp() throws Exception
    {
        jiraPropertySetFactory = mockController.getMock(JiraPropertySetFactory.class);
        userFormatManager = mockController.getMock(UserFormatManager.class);
        i18nFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        outlookDateManager = mockController.getMock(OutlookDateManager.class);
        propertySet = mockController.getMock(PropertySet.class);
        velocityRequestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);

        EasyMock.expect(jiraPropertySetFactory.buildCachingDefaultPropertySet(DefaultReindexMessageManager.PS_KEY, true))
                .andReturn(propertySet);
    }

    @Test
    public void testPushMessageNullUser() throws Exception
    {
        final Date currentDate = new Date(System.currentTimeMillis());

        propertySet.setString("user", "");
        EasyMock.expectLastCall();
        propertySet.setString("task", "i18n");
        EasyMock.expectLastCall();
        propertySet.setDate("time", currentDate);
        EasyMock.expectLastCall();

        replay();

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory, null)
        {
            @Override
            Date getCurrentDate()
            {
                return currentDate;
            }
        };

        manager.pushMessage(user, "i18n");
    }

    @Test
    public void testPushMessageNonNullUser() throws Exception
    {
        final Date currentDate = new Date(System.currentTimeMillis());
        user = new MockUser("bill");

        propertySet.setString("user", "bill");
        EasyMock.expectLastCall();
        propertySet.setString("task", "i18n");
        EasyMock.expectLastCall();
        propertySet.setDate("time", currentDate);
        EasyMock.expectLastCall();

        replay();

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory, null)
        {
            @Override
            Date getCurrentDate()
            {
                return currentDate;
            }
        };

        manager.pushMessage(user, "i18n");
    }

    @Test
    public void testClearNoExistingMessage() throws Exception
    {
        EasyMock.expect(propertySet.exists("user"))
                .andReturn(false);

        replay();

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory, null);

        manager.clear();
    }

    @Test
    public void testClearExistingMessage() throws Exception
    {
        EasyMock.expect(propertySet.exists("user"))
                .andReturn(true);
        propertySet.remove("time");
        EasyMock.expectLastCall();
        propertySet.remove("task");
        EasyMock.expectLastCall();
        propertySet.remove("user");
        EasyMock.expectLastCall();

        replay();

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory, null);

        manager.clear();
    }

    @Test
    public void testGetMessageNoExistingMessage() throws Exception
    {
        EasyMock.expect(propertySet.exists("user"))
                .andReturn(false);

        replay();

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory, null);

        assertNull(manager.getMessage(user));
    }

    @Test
    public void testGetMessage() throws Exception
    {
        Date date = new Date(1248402833700L);
        EasyMock.expect(propertySet.exists("user")).andReturn(true);
        EasyMock.expect(propertySet.getString("user")).andReturn("bill");
        EasyMock.expect(propertySet.getString("task")).andReturn("admin.bla.bla");
        EasyMock.expect(propertySet.getDate("time")).andReturn(date);

        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        EasyMock.expect(i18nFactory.getInstance(user)).andReturn(i18n);
        final MockOutlookDate mockOutlookDate = mockOutlookDate();
        EasyMock.expect(outlookDateManager.getOutlookDate(Locale.getDefault())).andReturn(mockOutlookDate);
        EasyMock.expect(userFormatManager.formatUser("bill", FullNameUserFormat.TYPE, "fullName")).andReturn("Full Name of Bill");
        replay();
        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory, null)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };
        final String result = manager.getMessage(user);
        assertEquals("<p>admin.notifications.task.requires.reindex Full Name of Bill admin.bla.bla THE DATE<p>admin.notifications.reindex.now <a href='/jira/secure/admin/jira/IndexAdmin.jspa'> </a><p>admin.notifications.complete.all.changes", result);
    }

    @Test
    public void testGetMessageNullUser() throws Exception
    {
        Date date = new Date(1248402833700L);
        EasyMock.expect(propertySet.exists("user")).andReturn(true);
        EasyMock.expect(propertySet.getString("user")).andReturn("not-existing");
        EasyMock.expect(propertySet.getString("task")).andReturn("admin.bla.bla");
        EasyMock.expect(propertySet.getDate("time")).andReturn(date);
        I18nHelper i18n = new MockI18nHelper(Locale.getDefault());
        EasyMock.expect(i18nFactory.getInstance(user)).andReturn(i18n);

        final MockOutlookDate mockOutlookDate = mockOutlookDate();
        EasyMock.expect(outlookDateManager.getOutlookDate(Locale.getDefault())).andReturn(mockOutlookDate);
        EasyMock.expect(userFormatManager.formatUser("not-existing", FullNameUserFormat.TYPE, "fullName")).andReturn(null);
        replay();

        DefaultReindexMessageManager manager = new DefaultReindexMessageManager(jiraPropertySetFactory, userFormatManager, i18nFactory, outlookDateManager, velocityRequestContextFactory, null)
        {
            @Override
            String getContextPath()
            {
                return "/jira";
            }
        };

        final String result = manager.getMessage(user);
        assertEquals("<p>admin.notifications.task.requires.reindex.nouser admin.bla.bla THE DATE<p>admin.notifications.reindex.now <a href='/jira/secure/admin/jira/IndexAdmin.jspa'> </a><p>admin.notifications.complete.all.changes", result);
    }

    private MockOutlookDate mockOutlookDate() {
        return new MockOutlookDate(Locale.getDefault())
        {
            @Override
            public String formatDMYHMS(final Date date)
            {
                return "THE DATE";
            }
        };
    }
}
