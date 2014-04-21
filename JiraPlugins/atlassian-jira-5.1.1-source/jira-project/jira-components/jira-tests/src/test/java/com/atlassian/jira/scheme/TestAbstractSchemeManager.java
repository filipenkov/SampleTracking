package com.atlassian.jira.scheme;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.I18nHelper;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class TestAbstractSchemeManager extends MockControllerTestCase
{
    @Test
    public void testGetDefaultSchemeTranslationKeysExist() throws Exception
    {
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        i18n.getText("default.name.key");
        mockController.setReturnValue("default scheme name");
        i18n.getText("default.desc.key");
        mockController.setReturnValue("this is the default scheme scheme");

        mockController.replay();

        MockAbstractSchemeManager schemeManager = new MockAbstractSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("default scheme name", defaultScheme.getString("name"));
        assertEquals("this is the default scheme scheme", defaultScheme.getString("description"));

        mockController.verify();
    }

    @Test
    public void testGetDefaultSchemeTranslationKeysDontExist() throws Exception
    {
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);

        i18n.getText("default.name.key");
        mockController.setReturnValue("default.name.key");
        i18n.getText("default.desc.key");
        mockController.setReturnValue("default.desc.key");

        mockController.replay();

        MockAbstractSchemeManager schemeManager = new MockAbstractSchemeManager(i18n);
        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default desc Scheme", defaultScheme.getString("name"));
        assertEquals("This is the default desc Scheme. Any new projects that are created will be assigned this scheme", defaultScheme.getString("description"));

        mockController.verify();
    }

    @Test
    public void testGetDefaultSchemeTranslationNullKeys() throws Exception
    {
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        mockController.replay();

        MockAbstractSchemeManager schemeManager = new MockAbstractSchemeManager(i18n)
        {
            @Override
            public String getDefaultDescriptionKey()
            {
                return null;
            }

            @Override
            public String getDefaultNameKey()
            {
                return null;
            }
        };

        final GenericValue defaultScheme = schemeManager.createDefaultScheme();

        assertNotNull(defaultScheme);
        assertEquals("Default desc Scheme", defaultScheme.getString("name"));
        assertEquals("This is the default desc Scheme. Any new projects that are created will be assigned this scheme", defaultScheme.getString("description"));

        mockController.verify();
    }

    private static class MockAbstractSchemeManager extends AbstractSchemeManager
    {
        private I18nHelper i18nHelper;

        MockAbstractSchemeManager(I18nHelper i18nHelper)
        {
            super(null, null, null, null, null, null, null);
            this.i18nHelper = i18nHelper;
        }

        protected GenericValue createSchemeGenericValue(final Map values) throws GenericEntityException
        {
            return new MockGenericValue(getSchemeEntityName(), values);
        }

        public GenericValue getDefaultScheme() throws GenericEntityException
        {
            return null;
        }

        I18nHelper getApplicationI18n()
        {
            return this.i18nHelper;
        }

        public String getSchemeEntityName()
        {
            return "scheme.entity";
        }

        public String getEntityName()
        {
            return "scheme";
        }

        public String getSchemeDesc()
        {
            return "desc";
        }

        public String getDefaultNameKey()
        {
            return "default.name.key";
        }

        public String getDefaultDescriptionKey()
        {
            return "default.desc.key";
        }

        public GenericValue copySchemeEntity(final GenericValue scheme, final GenericValue entity)
                throws GenericEntityException
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId) throws GenericEntityException
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final Long entityTypeId, final String parameter)
                throws GenericEntityException
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final String type, final Long entityTypeId)
                throws GenericEntityException
        {
            return null;
        }

        public List<GenericValue> getEntities(final GenericValue scheme, final String entityTypeId) throws GenericEntityException
        {
            return null;
        }

        public GenericValue createSchemeEntity(final GenericValue scheme, final SchemeEntity entity)
                throws GenericEntityException
        {
            return null;
        }

        public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity)
        {
            return false;
        }

        public boolean hasSchemeAuthority(final Long entityType, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
        {
            return false;
        }
    }
}
