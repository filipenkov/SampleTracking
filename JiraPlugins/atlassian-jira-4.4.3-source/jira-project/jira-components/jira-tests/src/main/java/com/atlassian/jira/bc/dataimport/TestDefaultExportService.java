package com.atlassian.jira.bc.dataimport;

import com.atlassian.activeobjects.spi.Backup;
import com.atlassian.jira.action.admin.export.EntitiesExporter;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.easymock.MockType;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.model.ModelReader;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.zip.ZipOutputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;

/**
 * @since v4.4
 */
public class TestDefaultExportService
{
    @Mock
    DelegatorInterface delegatorInterface;

    @Mock(MockType.NICE)
    EntitiesExporter entitiesExporter;

    @Mock
    ModelReader modelReader;

    @Before
    public void initMocks()
    {
        EasyMockAnnotations.initMocks(this);

        expect(delegatorInterface.getModelReader()).andReturn(modelReader);
    }

    @Test
    public void happyPathNoActiveObjects() throws Exception
    {
        expect(modelReader.getEntityNames()).andReturn(Collections.<String>emptyList());

        EasyMockAnnotations.replayMocks(this);

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory())
        {
            @Override
            protected Backup getActiveObjectsBackup()
            {
                return null;
            }
        };
        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);
        assertTrue(outcome.isValid());
        assertFalse(outcome.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void ioException() throws Exception
    {
        expect(modelReader.getEntityNames()).andReturn(Collections.<String>emptyList());

        EasyMockAnnotations.replayMocks(this);

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory())
        {
            @Override
            protected ZipOutputStream getZipOutputStream(String filename) throws IOException
            {
                throw new IOException("unit test exception");
            }
        };

        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);
        assertFalse(outcome.isValid());
        assertNull(outcome.getReturnedValue());

        final ErrorCollection errorCollection = outcome.getErrorCollection();
        assertEquals(0, errorCollection.getReasons().size());

        final Collection<String> errorMessages = errorCollection.getErrorMessages();
        assertEquals(1, errorMessages.size());
        final String message = errorMessages.iterator().next();
        assertEquals("Unable to save the backup file 'filename'.", message);
    }

    @Test
    public void invalidXml() throws Exception
    {
        // this special string triggers the "invalid XML" error code to try to give the user a better error message
        // for this common error case
        expect(modelReader.getEntityNames()).andThrow(new GenericEntityException("invalid XML character"));

        EasyMockAnnotations.replayMocks(this);

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory());

        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);
        assertFalse(outcome.isValid());
        assertNull(outcome.getReturnedValue());

        final ErrorCollection errorCollection = outcome.getErrorCollection();
        assertEquals(1, errorCollection.getReasons().size());
        assertEquals(ErrorCollection.Reason.VALIDATION_FAILED, errorCollection.getReasons().iterator().next());

        final Collection<String> errorMessages = errorCollection.getErrorMessages();
        assertEquals(1, errorMessages.size());
        final String message = errorMessages.iterator().next();
        assertEquals("Backup Data: Invalid XML characters", message);
    }

    @Test
    public void entityException() throws Exception
    {
        expect(modelReader.getEntityNames()).andThrow(new GenericEntityException("error message goes here"));
        EasyMockAnnotations.replayMocks(this);

        final DefaultExportService exportService = new DefaultExportService(delegatorInterface, entitiesExporter, new MockI18nBean.MockI18nBeanFactory());

        final ServiceOutcome<Void> outcome = exportService.export(null, "filename", ExportService.Style.NORMAL, TaskProgressSink.NULL_SINK);
        assertFalse(outcome.isValid());
        assertNull(outcome.getReturnedValue());

        final ErrorCollection errorCollection = outcome.getErrorCollection();
        assertEquals(0, errorCollection.getReasons().size());
        final Collection<String> errorMessages = errorCollection.getErrorMessages();
        assertEquals(1, errorMessages.size());
        final String message = errorMessages.iterator().next();
        assertEquals("Error exporting data: org.ofbiz.core.entity.GenericEntityException: error message goes here", message);
    }
}
