package com.atlassian.jira.imports.project.validation;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.IssueLinkTypeMapper;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetAssert;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;

/**
 * @since v3.13
 */
public class TestIssueLinkTypeMapperValidatorImpl extends ListeningTestCase
{
    @Test
    public void testValidateMappingsNoneRequired()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(null, null, null);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueLinkTypeMapper);
        assertFalse(messageSet.hasAnyMessages());
    }

    @Test
    public void testValidateMappingsNotMappedMissingLinkType()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Related To");
        mockIssueLinkTypeManagerControl.setReturnValue(Collections.EMPTY_LIST);
        mockIssueLinkTypeManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);
        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, null, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' is required for the import but does not exist in the current JIRA instance.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
    }

    @Test
    public void testValidateMappingsLinksNotEnabled()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Related To");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(createIssueLinkType("56", "Related To", null)));
        mockIssueLinkTypeManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(false);
        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, null, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "Issue Linking must be enabled because there are issue links in the project to import.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
    }

    @Test
    public void testValidateMappingsNotMappedSubtasksNotEnabled()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Related To");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(createIssueLinkType("56", "Related To", "jira_subtask")));
        mockIssueLinkTypeManagerControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(false);
        mockSubTaskManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);
        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, mockSubTaskManager, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The project to import includes subtasks, but subtasks are disabled in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testValidateMappingsNotMappedOldTypeHasStyleSubtaskNotNew()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Related To");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(createIssueLinkType("56", "Related To", null)));
        mockIssueLinkTypeManagerControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(true);
        mockSubTaskManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, mockSubTaskManager, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'jira_subtask' in the backup, but has no style in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testValidateMappingsNotMappedOldTypeHasNoStyleNewDoes()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Related To");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(createIssueLinkType("56", "Related To", "Styling")));
        mockIssueLinkTypeManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, null, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has no style value in the backup, but has style 'Styling' in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
    }

    @Test
    public void testValidateMappingsNotMappedDifferentStyles()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "Rum n Raisin");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Related To");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(createIssueLinkType("56", "Related To", "Cookies n Cream")));
        mockIssueLinkTypeManagerControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, mockSubTaskManager, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'Rum n Raisin' in the backup, but has style 'Cookies n Cream' in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testValidateMappingsNotMappedMappingMissing()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkTypesByName("Related To");
        mockIssueLinkTypeManagerControl.setReturnValue(EasyList.build(createIssueLinkType("56", "Related To", null)));
        mockIssueLinkTypeManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, null, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' is required for the import but has not been mapped.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
    }

    @Test
    public void testValidateMappingsMappedToNonexistantLinkType()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);
        // We make an invalid mapping - this link type does not exist in the current system.
        issueLinkTypeMapper.mapValue("12", "78");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(78));
        mockIssueLinkTypeManagerControl.setReturnValue(null);
        mockIssueLinkTypeManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, null, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' was mapped to an IssuelinkType ID (78) that does not exist.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
    }

    @Test
    public void testValidateMappingsMappedSubtasksNotEnabled()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");
        issueLinkTypeMapper.mapValue("12", "56");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(56));
        mockIssueLinkTypeManagerControl.setReturnValue(createIssueLinkType("56", "Related To", "jira_subtask"));
        mockIssueLinkTypeManagerControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(false);
        mockSubTaskManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, mockSubTaskManager, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The project to import includes subtasks, but subtasks are disabled in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testValidateMappingsMappedOldTypeHasStyleSubtaskNotNew()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "jira_subtask");
        issueLinkTypeMapper.mapValue("12", "56");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(56));
        mockIssueLinkTypeManagerControl.setReturnValue(createIssueLinkType("56", "Related To", null));
        mockIssueLinkTypeManagerControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(true);
        mockSubTaskManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, mockSubTaskManager, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'jira_subtask' in the backup, but has no style in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testValidateMappingsMappedOldTypeHasNoStyleNewDoes()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);
        issueLinkTypeMapper.mapValue("12", "56");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(56));
        mockIssueLinkTypeManagerControl.setReturnValue(createIssueLinkType("56", "Related To", "Styling"));
        mockIssueLinkTypeManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, null, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has no style value in the backup, but has style 'Styling' in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
    }

    @Test
    public void testValidateMappingsMappedDifferentStyles()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", "Rum n Raisin");
        issueLinkTypeMapper.mapValue("12", "56");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(56));
        mockIssueLinkTypeManagerControl.setReturnValue(createIssueLinkType("56", "Related To", "Cookies n Cream"));
        mockIssueLinkTypeManagerControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);

        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, mockSubTaskManager, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assert1ErrorNoWarnings(messageSet, "The Issue Link Type 'Related To' has style 'Rum n Raisin' in the backup, but has style 'Cookies n Cream' in the current system.");

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testValidateMappingsHappyPath()
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        issueLinkTypeMapper.flagValueAsRequired("12");
        issueLinkTypeMapper.registerOldValue("12", "Related To", null);
        issueLinkTypeMapper.mapValue("12", "56");

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(56));
        mockIssueLinkTypeManagerControl.setReturnValue(createIssueLinkType("56", "Related To", null));
        mockIssueLinkTypeManagerControl.replay();

        // Mock ApplicationProperties
        final MockControl mockApplicationPropertiesControl = MockControl.createStrictControl(ApplicationProperties.class);
        final ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockApplicationPropertiesControl.getMock();
        mockApplicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
        mockApplicationPropertiesControl.setReturnValue(true);
        
        mockApplicationPropertiesControl.replay();

        IssueLinkTypeMapperValidatorImpl issueLinkTypeMapperValidator = new IssueLinkTypeMapperValidatorImpl(mockIssueLinkTypeManager, null, mockApplicationProperties);
        MessageSet messageSet = issueLinkTypeMapperValidator.validateMappings(new MockI18nBean(), backupProject, issueLinkTypeMapper);
        MessageSetAssert.assertNoMessages(messageSet);

        // Verify Mock ApplicationProperties
        mockApplicationPropertiesControl.verify();
        mockIssueLinkTypeManagerControl.verify();
    }

    private IssueLinkType createIssueLinkType(final String id, final String linkname, final String style)
    {
        final Map fields = new HashMap();
        fields.put("id", id);
        fields.put("linkname", linkname);
        fields.put("style", style);
        return new IssueLinkType(new MockGenericValue("IssueLinkType", fields));
    }
}
