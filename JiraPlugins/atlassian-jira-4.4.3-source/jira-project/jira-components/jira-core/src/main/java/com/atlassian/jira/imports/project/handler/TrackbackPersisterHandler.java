package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalTrackback;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.TrackbackParser;
import com.atlassian.jira.imports.project.parser.TrackbackParserImpl;
import com.atlassian.jira.imports.project.transformer.TrackbackTransformer;
import com.atlassian.jira.imports.project.transformer.TrackbackTransformerImpl;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @since v3.13
 */
public class TrackbackPersisterHandler extends AbstractPersisterHandler implements ImportEntityHandler
{
    private static final Logger log = Logger.getLogger(TrackbackPersisterHandler.class);

    private final ProjectImportPersister projectImportPersister;
    private final ProjectImportMapper projectImportMapper;
    private final ProjectImportResults projectImportResults;
    private final BackupSystemInformation backupSystemInformation;
    private TrackbackParser trackbackParser;
    private TrackbackTransformer trackbackTransformer;

    public TrackbackPersisterHandler(final ProjectImportPersister projectImportPersister, final ProjectImportMapper projectImportMapper, final ProjectImportResults projectImportResults, final BackupSystemInformation backupSystemInformation, final Executor executor)
    {
        super(executor, projectImportResults);

        this.projectImportPersister = projectImportPersister;
        this.projectImportMapper = projectImportMapper;
        this.projectImportResults = projectImportResults;
        this.backupSystemInformation = backupSystemInformation;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException, AbortImportException
    {
        if (TrackbackParser.TRACKBACK_ENTITY_NAME.equals(entityName))
        {
            final ExternalTrackback externalTrackback = getTrackbackParser().parse(attributes);
            final ExternalTrackback transformedTrackback = getTrackbackTransformer().transform(projectImportMapper, externalTrackback);
            // This can be null if the issue was not actually created
            if (transformedTrackback.getIssueId() != null)
            {
                execute(new Runnable()
                {
                    public void run()
                    {
                        final Long trackbackId = projectImportPersister.createEntity(getTrackbackParser().getEntityRepresentation(
                            transformedTrackback));
                        if (trackbackId == null)
                        {
                            final String issueKey = backupSystemInformation.getIssueKeyForId(externalTrackback.getIssueId());
                            projectImportResults.addError(projectImportResults.getI18n().getText("admin.errors.project.import.trackback.error",
                                externalTrackback.getId(), issueKey));
                        }
                    }
                });
            }
            else
            {
                final String issueKey = backupSystemInformation.getIssueKeyForId(externalTrackback.getIssueId());
                log.warn("Not creating trackback with id '" + externalTrackback.getId() + "' for backup issue '" + issueKey + "', the issue has not been mapped in the new system.");
            }
        }
    }

    ///CLOVER:OFF
    TrackbackTransformer getTrackbackTransformer()
    {
        if (trackbackTransformer == null)
        {
            trackbackTransformer = new TrackbackTransformerImpl();
        }
        return trackbackTransformer;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    TrackbackParser getTrackbackParser()
    {
        if (trackbackParser == null)
        {
            trackbackParser = new TrackbackParserImpl();
        }
        return trackbackParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }
    ///CLOVER:ON
}
