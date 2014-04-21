package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalTrackback;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.TrackbackParser;
import com.atlassian.jira.imports.project.transformer.TrackbackTransformer;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestTrackbackPersisterHandler extends ListeningTestCase
{
    @Test
    public void testHandle() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalTrackback externalTrackback = new ExternalTrackback("12", "100", "http://whatever", "BlogMe", "Why it sux", "It does", new Date(0));

        final MockControl mockTrackbackParserControl = MockControl.createStrictControl(TrackbackParser.class);
        final TrackbackParser mockTrackbackParser = (TrackbackParser) mockTrackbackParserControl.getMock();
        mockTrackbackParser.parse(null);
        mockTrackbackParserControl.setReturnValue(externalTrackback);
        mockTrackbackParser.getEntityRepresentation(externalTrackback);
        mockTrackbackParserControl.setReturnValue(null);
        mockTrackbackParserControl.replay();

        final MockControl mockTrackbackTransformerControl = MockControl.createStrictControl(TrackbackTransformer.class);
        final TrackbackTransformer mockTrackbackTransformer = (TrackbackTransformer) mockTrackbackTransformerControl.getMock();
        mockTrackbackTransformer.transform(projectImportMapper, externalTrackback);
        mockTrackbackTransformerControl.setReturnValue(externalTrackback);
        mockTrackbackTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(new Long(12));
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        TrackbackPersisterHandler trackbackPersisterHandler = new TrackbackPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            TrackbackParser getTrackbackParser()
            {
                return mockTrackbackParser;
            }

            TrackbackTransformer getTrackbackTransformer()
            {
                return mockTrackbackTransformer;
            }
        };

        trackbackPersisterHandler.handleEntity(TrackbackParser.TRACKBACK_ENTITY_NAME, null);
        trackbackPersisterHandler.handleEntity("NOTTrackback", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockTrackbackParserControl.verify();
        mockTrackbackTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleNullTransformedTrackback() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalTrackback externalTrackback = new ExternalTrackback("12", "34", "http://whatever", "BlogMe", "Why it sux", "It does", new Date(0));
        ExternalTrackback transformedExternalTrackback = new ExternalTrackback("12", null, "http://whatever", "BlogMe", "Why it sux", "It does", new Date(0));

        final MockControl mockTrackbackParserControl = MockControl.createStrictControl(TrackbackParser.class);
        final TrackbackParser mockTrackbackParser = (TrackbackParser) mockTrackbackParserControl.getMock();
        mockTrackbackParser.parse(null);
        mockTrackbackParserControl.setReturnValue(externalTrackback);
        mockTrackbackParserControl.replay();

        final MockControl mockTrackbackTransformerControl = MockControl.createStrictControl(TrackbackTransformer.class);
        final TrackbackTransformer mockTrackbackTransformer = (TrackbackTransformer) mockTrackbackTransformerControl.getMock();
        mockTrackbackTransformer.transform(projectImportMapper, externalTrackback);
        mockTrackbackTransformerControl.setReturnValue(transformedExternalTrackback);
        mockTrackbackTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        TrackbackPersisterHandler trackbackPersisterHandler = new TrackbackPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            TrackbackParser getTrackbackParser()
            {
                return mockTrackbackParser;
            }

            TrackbackTransformer getTrackbackTransformer()
            {
                return mockTrackbackTransformer;
            }
        };

        trackbackPersisterHandler.handleEntity(TrackbackParser.TRACKBACK_ENTITY_NAME, null);
        trackbackPersisterHandler.handleEntity("NOTTrackback", null);

        mockTrackbackParserControl.verify();
        mockTrackbackTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleErrorAddingTrackback() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalTrackback externalTrackback = new ExternalTrackback("12", "34", "http://whatever", "BlogMe", "Why it sux", "It does", new Date(0));

        final MockControl mockTrackbackParserControl = MockControl.createStrictControl(TrackbackParser.class);
        final TrackbackParser mockTrackbackParser = (TrackbackParser) mockTrackbackParserControl.getMock();
        mockTrackbackParser.parse(null);
        mockTrackbackParserControl.setReturnValue(externalTrackback);
        mockTrackbackParser.getEntityRepresentation(externalTrackback);
        mockTrackbackParserControl.setReturnValue(null);
        mockTrackbackParserControl.replay();

        final MockControl mockTrackbackTransformerControl = MockControl.createStrictControl(TrackbackTransformer.class);
        final TrackbackTransformer mockTrackbackTransformer = (TrackbackTransformer) mockTrackbackTransformerControl.getMock();
        mockTrackbackTransformer.transform(projectImportMapper, externalTrackback);
        mockTrackbackTransformerControl.setReturnValue(externalTrackback);
        mockTrackbackTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(null);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        TrackbackPersisterHandler trackbackPersisterHandler = new TrackbackPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            TrackbackParser getTrackbackParser()
            {
                return mockTrackbackParser;
            }

            TrackbackTransformer getTrackbackTransformer()
            {
                return mockTrackbackTransformer;
            }
        };

        trackbackPersisterHandler.handleEntity(TrackbackParser.TRACKBACK_ENTITY_NAME, null);
        trackbackPersisterHandler.handleEntity("NOTTrackback", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving trackback with id '12' for issue 'TST-1'."));
        mockTrackbackParserControl.verify();
        mockTrackbackTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }
    
}
