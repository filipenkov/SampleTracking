package com.atlassian.jira.imports.project.validation;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.action.issue.customfields.MockProjectImportableCustomFieldType;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.managers.MockCustomFieldManager;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.MockConstantsManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetAssert;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
@RunWith(ListeningMockitoRunner.class)
public class TestCustomFieldMapperValidatorImpl
{
    @org.mockito.Mock
    private CustomFieldDescription customFieldDescription;
    @org.mockito.Mock
    private I18nHelper.BeanFactory i18nFactory;

    @Test
    public void testCustomFieldTypeIsImportable_TypeDoesNotExist()
    {
        // Create a mock CustomFieldManager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);
        mockCustomFieldManager.expectAndReturn("getCustomFieldType", P.args(P.eq("A")), null);
        final CustomFieldManager customFieldManager = (CustomFieldManager) mockCustomFieldManager.proxy();

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(customFieldManager, null, null);
        assertFalse(customFieldMapperValidator.customFieldTypeIsImportable("A"));

        mockCustomFieldManager.verify();
    }

    @Test
    public void testCustomFieldTypeIsImportable_TypeNotImportable()
    {
        // Create a mock CustomFieldManager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);
        mockCustomFieldManager.expectAndReturn("getCustomFieldType", P.args(P.eq("A")), new MockCustomFieldType());
        final CustomFieldManager customFieldManager = (CustomFieldManager) mockCustomFieldManager.proxy();

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(customFieldManager, null, null);
        assertFalse(customFieldMapperValidator.customFieldTypeIsImportable("A"));

        mockCustomFieldManager.verify();
    }

    @Test
    public void testCustomFieldTypeIsImportable_TypeImportable()
    {
        // Create a mock CustomFieldManager
        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);
        mockCustomFieldManager.expectAndReturn("getCustomFieldType", P.args(P.eq("A")), new MockProjectImportableCustomFieldType(null));
        final CustomFieldManager customFieldManager = (CustomFieldManager) mockCustomFieldManager.proxy();

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(customFieldManager, null, null);
        assertTrue(customFieldMapperValidator.customFieldTypeIsImportable("A"));

        mockCustomFieldManager.verify();
    }

    @Test
    public void testGetIssueTypeDisplayNames()
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getIssueTypeObject("2");
        mockConstantsManagerControl.setReturnValue(new MockIssueType("2", "Bug"));
        mockConstantsManager.getIssueTypeObject("4");
        mockConstantsManagerControl.setReturnValue(new MockIssueType("4", "Improvement"));
        mockConstantsManagerControl.replay();
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, mockConstantsManager, null);

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");
        issueTypeMapper.mapValue("3", "4");

        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");
        assertEquals("Bug, Improvement", customFieldMapperValidator.getIssueTypeDisplayNames(externalCustomFieldConfiguration, EasyList.build("1", "3"), issueTypeMapper, new MockI18nHelper()));
        mockConstantsManagerControl.verify();
    }

    @Test
    public void testGetIssueTypeDisplayNamesNullList()
    {
        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");
        issueTypeMapper.mapValue("3", "4");

        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, null, null);

        assertEquals("common.words.none", customFieldMapperValidator.getIssueTypeDisplayNames(externalCustomFieldConfiguration, null, issueTypeMapper, new MockI18nHelper()));
    }

    @Test
    public void testCustomFieldIsValidForRequiredContexts_CustomFieldNotUsed()
    {
        final MockProjectManager projectManager = new MockProjectManager();
        projectManager.addProject(new MockGenericValue("Project", EasyMap.build("key", "PIG")));

        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, null, projectManager);

        final CustomField newCustomField = null;
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();

        final IssueTypeMapper issueTypeMapper = null;

        assertEquals(true, customFieldMapperValidator.customFieldIsValidForRequiredContexts(externalCustomFieldConfiguration, newCustomField, "10", customFieldMapper, issueTypeMapper, "PIG"));
    }

    @Test
    public void testCustomFieldIsValidForRequiredContexts_InvalidContext()
    {
        final MockProjectManager projectManager = new MockProjectManager();
        projectManager.addProject(new MockProject(500, "PIG"));

        final MockConstantsManager constantsManager = new MockConstantsManager();
        constantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "11", "name", "Bug")));
        constantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "12", "name", "Improvement")));

        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, constantsManager, projectManager);

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        // Flag that old CustomField 10 is used by Issue Type 1
        customFieldMapper.flagValueAsRequired("10", "1200");
        customFieldMapper.flagIssueTypeInUse("1200", "1");
        // Flag that old CustomField 10 is used by Issue Type 2
        customFieldMapper.flagValueAsRequired("10", "1201");
        customFieldMapper.flagIssueTypeInUse("1201", "2");
        customFieldMapper.registerIssueTypesInUse();

        // Map the old Issue Type ID's to new Issue Type IDs
        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "11");
        issueTypeMapper.mapValue("2", "12");

        // Create a custom field.
        final CustomField newCustomField = new CustomFieldImpl(new MockGenericValue("CustomField"), null, null, null, null, null, null, null, customFieldDescription, i18nFactory)
        {
            public boolean firstCall = true;

            public FieldConfig getRelevantConfig(final IssueContext issueContext)
            {
                // We want this method to return a FieldConfig the first time
                if (firstCall)
                {
                    firstCall = false;
                    return null;
                }
                // but return null on the second call.
                return null;
            }
        };

        assertEquals(false, customFieldMapperValidator.customFieldIsValidForRequiredContexts(externalCustomFieldConfiguration, newCustomField, "10", customFieldMapper, issueTypeMapper, "PIG"));
    }

    @Test
    public void testCustomFieldIsValidForRequiredContexts_HappyPath()
    {
        final MockProjectManager projectManager = new MockProjectManager();
        projectManager.addProject(new MockProject(500, "PIG"));

        final MockConstantsManager constantsManager = new MockConstantsManager();
        constantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "11", "name", "Bug")));
        constantsManager.addIssueType(new MockGenericValue("IssueType", EasyMap.build("id", "12", "name", "Improvement")));

        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, constantsManager, projectManager);

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        // Flag that old CustomField 10 is used by Issue Type 1
        customFieldMapper.flagValueAsRequired("10", "1200");
        customFieldMapper.flagIssueTypeInUse("1200", "1");
        // Flag that old CustomField 10 is used by Issue Type 2
        customFieldMapper.flagValueAsRequired("10", "1201");
        customFieldMapper.flagIssueTypeInUse("1201", "2");

        // Map the old Issue Type ID's to new Issue Type IDs
        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "11");
        issueTypeMapper.mapValue("2", "12");

        // Create a custom field.
        final CustomField newCustomField = new CustomFieldImpl(new MockGenericValue("CustomField"), null, null, null, null, null, null, null, customFieldDescription, i18nFactory)
        {
            public FieldConfig getRelevantConfig(final IssueContext issueContext)
            {
                return new FieldConfigImpl(null, null, null, Collections.EMPTY_LIST, null);
            }
        };

        assertEquals(true, customFieldMapperValidator.customFieldIsValidForRequiredContexts(externalCustomFieldConfiguration, newCustomField, "10", customFieldMapper, issueTypeMapper, "PIG"));
    }

    @Test
    public void testValidateMappings_CustomFieldDoesNotExist()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Flavour", new MockProjectImportableCustomFieldType("com.atlassian.cf:Flavour", "Flavour"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Spin", new MockProjectImportableCustomFieldType("com.atlassian.cf:Spin", "Spin"));

        final ExternalCustomField customFieldMyStuff = new ExternalCustomField("12", "My Stuff", "com.atlassian.cf:Flavour");
        ExternalCustomFieldConfiguration customFieldMyStuffConfig = new ExternalCustomFieldConfiguration(null, null, customFieldMyStuff, "34");
        final ExternalCustomField customFieldMadStuff = new ExternalCustomField("14", "Mad Stuff", "com.atlassian.cf:Spin");
        ExternalCustomFieldConfiguration customFieldMadStuffConfig = new ExternalCustomFieldConfiguration(null, null, customFieldMadStuff, "34");
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(customFieldMyStuffConfig, customFieldMadStuffConfig), Collections.EMPTY_LIST);

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.registerOldValue("14", "Mad Stuff");

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, null, null);

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nBean(), backupProject, null, customFieldMapper);
        MessageSetAssert.assertNoWarnings(messageSet);
        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getErrorMessages().contains("The custom field 'My Stuff' of type 'Flavour' is required for the import but does not exist in the current JIRA instance."));
        assertTrue(messageSet.getErrorMessages().contains("The custom field 'Mad Stuff' of type 'Spin' is required for the import but does not exist in the current JIRA instance."));
    }

    @Test
    public void testValidateMappings_CustomFieldNotMappedBecauseWrongType()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Text"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Text"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Date", new MockProjectImportableCustomFieldType("com.atlassian.cf:Date", "Date"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Numeric", new MockProjectImportableCustomFieldType("com.atlassian.cf:Numeric", "Numeric"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, null, null);
        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "com.atlassian.cf:Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "com.atlassian.cf:Numeric"), "321"));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigs, Collections.EMPTY_LIST);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        // customFieldMapper.mapValue("12", "22");
        // customFieldMapper.mapValue("14", "24");

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nBean(), backupProject, null, customFieldMapper);
        MessageSetAssert.assertNoWarnings(messageSet);
        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getErrorMessages().contains("The custom field 'Mad Stuff' in the backup project is of type 'Numeric' but the field with the same name in the current JIRA instance is of a different type."));
        assertTrue(messageSet.getErrorMessages().contains("The custom field 'My Stuff' in the backup project is of type 'Date' but the field with the same name in the current JIRA instance is of a different type."));
    }

    @Test
    public void testValidateMappings_CustomFieldMappedButWrongType()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Text"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Text"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, null, null)
        {
            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };
        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"), new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "Numeric"), "321"));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigs, Collections.EMPTY_LIST);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.mapValue("14", "24");

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, null, customFieldMapper);
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getErrorMessages().contains("admin.errors.project.import.custom.field.wrong.type Mad Stuff Numeric"));
        assertTrue(messageSet.getErrorMessages().contains("admin.errors.project.import.custom.field.wrong.type My Stuff Date"));
    }

    @Test
    public void testValidateMappings_CustomFieldNotMappedBecauseNotValidForSomeIssueTypes()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "com.atlassian.cf:Numeric"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Numeric", new MockProjectImportableCustomFieldType("com.atlassian.cf:Numeric", "Numeric"));

        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "com.atlassian.cf:Numeric"), "321"));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigs, Collections.EMPTY_LIST);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.registerIssueTypesInUse();

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getIssueTypeObject", P.args(P.eq("2")), new MockIssueType("2", "FUBAR"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, (ConstantsManager) mockConstantsManager.proxy(), null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for 12, but not 14
                return oldCustomFieldId.equals("12");
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueTypeMapper, customFieldMapper);
        MessageSetAssert.assertNoWarnings(messageSet);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The custom field 'Mad Stuff' in the backup project is used by issue types 'FUBAR' but the field with the same name in the current JIRA instance is not available to those issue types in this project.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateMappings_CustomFieldNotMappedBecauseNotProjectImportable()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));
        mockCustomFieldManager.addCustomFieldType("Numeric", new MockCustomFieldType("Numeric", "Numeric"));

        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"), new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "Mad Stuff", "Numeric"), "321"));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigs, Collections.EMPTY_LIST);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getIssueTypeObject", P.args(P.eq("2")), new MockIssueType("2", "FUBAR"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, (ConstantsManager) mockConstantsManager.proxy(), null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                return true;
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return false;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueTypeMapper, customFieldMapper);
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Unable to import custom field 'Mad Stuff'. The custom field type does not support project imports.", messageSet.getWarningMessages().iterator().next());
        assertEquals("Unable to import custom field 'My Stuff'. The custom field type does not support project imports.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateMappings_CustomFieldMappedButNotValidForSomeIssueTypes()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));

        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"), new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "Numeric"), "321"));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigs, Collections.EMPTY_LIST);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.mapValue("14", "24");
        customFieldMapper.registerIssueTypesInUse();

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getIssueTypeObject", P.args(P.eq("2")), new MockIssueType("2", "FUBAR"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, (ConstantsManager) mockConstantsManager.proxy(), null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for 12, but not 14
                return oldCustomFieldId.equals("12");
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueTypeMapper, customFieldMapper);
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("admin.errors.project.import.custom.field.wrong.context Mad Stuff FUBAR", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateMappings_HappyPath()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));

        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"), new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "Numeric"), "321"));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigs, Collections.EMPTY_LIST);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.mapValue("14", "24");

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getIssueTypeObject", P.args(P.eq("2")), new MockIssueType("2", "FUBAR"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, (ConstantsManager) mockConstantsManager.proxy(), null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for all
                return true;
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueTypeMapper, customFieldMapper);
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testCustomFieldTypePluginMissing() throws Exception
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));

        final List customFieldConfigs = EasyList.build(new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, customFieldConfigs, Collections.EMPTY_LIST);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        // Throw in some orphan data for coverage.
        customFieldMapper.flagValueAsRequired("14", "1000");

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final Mock mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockConstantsManager.expectAndReturn("getIssueTypeObject", P.args(P.eq("2")), new MockIssueType("2", "FUBAR"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, (ConstantsManager) mockConstantsManager.proxy(), null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for all
                return true;
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueTypeMapper, customFieldMapper);
        MessageSetAssert.assert1Warning(messageSet, "The custom field 'My Stuff' will not be imported because the custom field type 'Date' is not installed.");
        MessageSetAssert.assertNoErrors(messageSet);
    }

    private CustomField createCustomField(final long id, final String name, final String typeKey)
    {
        return new CustomFieldImpl(new MockGenericValue("CustomField", EasyMap.build("id", new Long(id), "name", name)), null, null, null, null, null, null, null, customFieldDescription, i18nFactory)
        {
            public CustomFieldType getCustomFieldType()
            {
                final Mock mockCustomFieldType = new Mock(CustomFieldType.class);
                mockCustomFieldType.expectAndReturn("getKey", typeKey);
                return (CustomFieldType) mockCustomFieldType.proxy();
            }
        };
    }
}
