package com.atlassian.jira.notification;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.util.I18nHelper;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * @since v4.0
 */
public class TestDefaultNotificationSchemeManager extends MockControllerTestCase
{
    @Test
    public void testGetDefaultSchemeTranslationKeysExist() throws Exception
    {
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        i18n.getText("admin.schemes.notifications.default");
        mockController.setReturnValue("Default Notification Scheme");

        mockController.replay();

        MockDefaultNotificationSchemeManager schemeManager = new MockDefaultNotificationSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default Notification Scheme", defaultScheme.getString("name"));
        assertNull(defaultScheme.getString("description"));

        mockController.verify();
    }

    @Test
    public void testGetDefaultSchemeTranslationKeysDontExist() throws Exception
    {
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        i18n.getText("admin.schemes.notifications.default");
        mockController.setReturnValue("admin.schemes.notifications.default");

        mockController.replay();

        MockDefaultNotificationSchemeManager schemeManager = new MockDefaultNotificationSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default Notification Scheme", defaultScheme.getString("name"));
        assertNull(defaultScheme.getString("description"));

        mockController.verify();
    }

    private static class MockDefaultNotificationSchemeManager extends DefaultNotificationSchemeManager
    {
        private final I18nHelper i18n;

        public MockDefaultNotificationSchemeManager(I18nHelper i18n)
        {
            super(null, null, null, null, null, null, null, null, null);
            this.i18n = i18n;
        }

        I18nHelper getApplicationI18n()
        {
            return i18n;
        }

        protected GenericValue createSchemeGenericValue(final Map values) throws GenericEntityException
        {
            return new MockGenericValue(getSchemeEntityName(), values);
        }

        public boolean schemeExists(final String name) throws GenericEntityException
        {
            return false;
        }

        @Override
        public GenericValue getDefaultScheme() throws GenericEntityException
        {
            return null;
        }

        protected void flushProjectSchemes()
        {

        }
    }
}
