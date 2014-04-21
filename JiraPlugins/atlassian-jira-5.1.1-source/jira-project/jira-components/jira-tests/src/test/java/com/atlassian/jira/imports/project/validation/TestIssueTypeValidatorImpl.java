package com.atlassian.jira.imports.project.validation;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.util.IssueTypeImportHelper;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestIssueTypeValidatorImpl extends ListeningTestCase
{
    @Test
    public void testIssueTypeDoesNotExist()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.getIssueTypeForName("Task");
        mockIssueTypeImportHelperControl.setReturnValue(null);
        mockIssueTypeImportHelperControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Task' is required for the import but does not exist in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testSubTaskIssueTypeDoesNotExist()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.getIssueTypeForName("Sub-Task");
        mockIssueTypeImportHelperControl.setReturnValue(null);
        mockIssueTypeImportHelperControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createStrictControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(true);
        mockSubTaskManagerControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, mockIssueTypeImportHelper, mockSubTaskManager);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Sub-Task", true);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The sub-task issue type 'Sub-Task' is required for the import but does not exist in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
        mockIssueTypeImportHelperControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testSubTaskIssueTypeDoesNotExistSubTasksNotEnabled()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.getIssueTypeForName("Sub-Task");
        mockIssueTypeImportHelperControl.setReturnValue(null);
        mockIssueTypeImportHelperControl.replay();

        final MockControl mockSubTaskManagerControl = MockControl.createStrictControl(SubTaskManager.class);
        final SubTaskManager mockSubTaskManager = (SubTaskManager) mockSubTaskManagerControl.getMock();
        mockSubTaskManager.isSubTasksEnabled();
        mockSubTaskManagerControl.setReturnValue(false);
        mockSubTaskManagerControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, mockIssueTypeImportHelper, mockSubTaskManager);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Sub-Task", true);

        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("Sub-tasks are currently disabled in JIRA, please enable sub-tasks. The sub-task issue type 'Sub-Task' is required for the import but does not exist in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
        mockIssueTypeImportHelperControl.verify();
        mockSubTaskManagerControl.verify();
    }

    @Test
    public void testIssueTypeNotInSchemeNotMapped()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.getIssueTypeForName("Task");
        mockIssueTypeImportHelperControl.setReturnValue(new MockIssueType("987", "Task"));
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(false);
        mockIssueTypeImportHelperControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();
        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Task' exists in the system but is not valid for the projects issue type scheme.", messageSet.getErrorMessages().iterator().next());
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testIssueTypeNotInScheme()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(false);
        mockIssueTypeImportHelperControl.replay();

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getIssueTypeObject("987");
        mockConstantsManagerControl.setReturnValue(new MockIssueType("987", "Task"));
        mockConstantsManagerControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(mockConstantsManager, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Task", false);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();
        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Task' exists in the system but is not valid for the projects issue type scheme.", messageSet.getErrorMessages().iterator().next());
        mockConstantsManagerControl.verify();
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testIssueTypeWasSubtaskNotNowNotMapped()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.getIssueTypeForName("Bug");
        mockIssueTypeImportHelperControl.setReturnValue(new MockIssueType("987", "Bug", false));
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(true);
        mockIssueTypeImportHelperControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", true);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Bug' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testIssueTypeWasSubtaskNotNow()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(true);
        mockIssueTypeImportHelperControl.replay();

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getIssueTypeObject("987");
        mockConstantsManagerControl.setReturnValue(new MockIssueType("987", "Bug", false));
        mockConstantsManagerControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(mockConstantsManager, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", true);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Bug' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
        mockConstantsManagerControl.verify();
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testIssueTypeWasSubtaskNotNowAndNotRelevantForProject()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(false);
        mockIssueTypeImportHelperControl.replay();

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getIssueTypeObject("987");
        mockConstantsManagerControl.setReturnValue(new MockIssueType("987", "Bug", false));
        mockConstantsManagerControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(mockConstantsManager, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", true);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(2, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertTrue( messageSet.getErrorMessages().contains("The issue type 'Bug' is defined as a sub-task in the backup project, but it is a normal issue type in the current JIRA instance."));
        assertTrue( messageSet.getErrorMessages().contains("The issue type 'Bug' exists in the system but is not valid for the projects issue type scheme."));
        mockConstantsManagerControl.verify();
        mockIssueTypeImportHelperControl.verify();
    }


    @Test
    public void testIssueTypeNormalNowSubtaskNotMapped()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.getIssueTypeForName("Bug");
        mockIssueTypeImportHelperControl.setReturnValue(new MockIssueType("987", "Bug", true));
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(true);
        mockIssueTypeImportHelperControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Bug' is defined as a normal issue type in the backup project, but it is a sub-task issue type in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testIssueTypeNormalNowSubtask()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(true);
        mockIssueTypeImportHelperControl.replay();

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getIssueTypeObject("987");
        mockConstantsManagerControl.setReturnValue(new MockIssueType("987", "Bug", true));
        mockConstantsManagerControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(mockConstantsManager, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Bug' is defined as a normal issue type in the backup project, but it is a sub-task issue type in the current JIRA instance.", messageSet.getErrorMessages().iterator().next());
        mockConstantsManagerControl.verify();
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testIssueTypeNotMappedButSeemsFineError()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.getIssueTypeForName("Bug");
        mockIssueTypeImportHelperControl.setReturnValue(new MockIssueType("987", "Bug", false));
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(true);
        mockIssueTypeImportHelperControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(null, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals("The issue type 'Bug' is required for the import but it is not mapped.", messageSet.getErrorMessages().iterator().next());
        mockIssueTypeImportHelperControl.verify();
    }

    @Test
    public void testNoProjectFoundUsingDefaultIssueTypeSchemeAndHappyPath()
    {
        final MockControl mockIssueTypeImportHelperControl = MockClassControl.createStrictControl(IssueTypeImportHelper.class);
        final IssueTypeImportHelper mockIssueTypeImportHelper = (IssueTypeImportHelper) mockIssueTypeImportHelperControl.getMock();
        mockIssueTypeImportHelper.isIssueTypeValidForProject("TST", "987");
        mockIssueTypeImportHelperControl.setReturnValue(true);
        mockIssueTypeImportHelperControl.replay();

        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getIssueTypeObject("987");
        mockConstantsManagerControl.setReturnValue(new MockIssueType("987", "Bug", false));
        mockConstantsManagerControl.replay();

        IssueTypeMapperValidatorImpl issueTypeValidator = new IssueTypeMapperValidatorImpl(mockConstantsManager, mockIssueTypeImportHelper, null);

        // Create an issue type mapper with an issue type that is not above
        IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.flagValueAsRequired("678");
        issueTypeMapper.registerOldValue("678", "Bug", false);
        issueTypeMapper.mapValue("678", "987");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12), new Long(14)));

        MockI18nBean mockI18nBean = new MockI18nBean();

        final MessageSet messageSet = issueTypeValidator.validateMappings(mockI18nBean, backupProject, issueTypeMapper);
        assertFalse(messageSet.hasAnyErrors());
        mockConstantsManagerControl.verify();
        mockIssueTypeImportHelperControl.verify();
    }

}
