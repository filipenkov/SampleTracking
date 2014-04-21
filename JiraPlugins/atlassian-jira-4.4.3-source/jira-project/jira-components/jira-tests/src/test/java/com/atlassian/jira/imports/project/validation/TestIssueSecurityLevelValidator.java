package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestIssueSecurityLevelValidator extends ListeningTestCase
{
    @Test
    public void testValidateMappingsNoProject()
    {
        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(null);
        mockProjectManagerControl.replay();

        IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(mockProjectManager);

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList());
        MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, backupProject, new MockI18nBean());
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The issue security level 'Who cares' is required for the import. Please create a project with key 'TST', and configure its issue security scheme.", messageSet.getErrorMessages().iterator().next());

        mockProjectManagerControl.verify();
    }

    @Test
    public void testValidateMappingsProjectExists()
    {
        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(new MockProject(12, "TST"));
        mockProjectManagerControl.replay();

        IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(mockProjectManager);

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList());
        MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, backupProject, new MockI18nBean());
        assertTrue(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("The issue security level 'Who cares' is required for the import but does not exist in the configured issue security scheme for this project.", messageSet.getErrorMessages().iterator().next());

        mockProjectManagerControl.verify();
    }

    @Test
    public void testHappyPath()
    {
        IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(null);

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");
        simpleProjectImportIdMapper.flagValueAsRequired("14");
        simpleProjectImportIdMapper.registerOldValue("12", "Real Important");
        simpleProjectImportIdMapper.registerOldValue("14", "Who cares");
        simpleProjectImportIdMapper.mapValue("12", "623");
        simpleProjectImportIdMapper.mapValue("14", "626");

        MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, null, new MockI18nBean());
        assertFalse(messageSet.hasAnyErrors());
        assertTrue(messageSet.getWarningMessages().isEmpty());
    }

    @Test
    public void testOrphanSecurityLevel() throws Exception
    {
        IssueSecurityLevelValidator issueSecurityLevelValidator = new IssueSecurityLevelValidator(null);

        SimpleProjectImportIdMapper simpleProjectImportIdMapper = new SimpleProjectImportIdMapperImpl();
        simpleProjectImportIdMapper.flagValueAsRequired("12");

        MessageSet messageSet = issueSecurityLevelValidator.validateMappings(simpleProjectImportIdMapper, null, new MockI18nBean());
        assertTrue(messageSet.hasAnyWarnings());
        assertTrue(messageSet.getErrorMessages().isEmpty());
        assertEquals(1, messageSet.getWarningMessages().size());
        assertEquals("The issue security level with id '12' can not be resolved into an actual security level in the backup file. Any issues that were protected by this security level will no longer have an issue security level. After performing the import see the logs for details of which issues were affected.", messageSet.getWarningMessages().iterator().next());
    }
}
