package com.atlassian.jira.plugins.workflow;


import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.workflow.AbstractJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import com.sysbliss.jira.plugins.workflow.manager.CachingWorkflowImageManagerImpl;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowImageManagerImpl;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowLayoutManager;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowLayoutManagerImpl;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowThumbnailParams;
import com.sysbliss.jira.plugins.workflow.util.WorkflowDesignerPropertySet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CachingWorkflowImageManagerTest {

    private WorkflowDescriptor workflowDescriptor;
    private WorkflowManager workflowManager;
    private WorkflowLayoutManager workflowLayoutManager;
    private JiraHome jiraHome;
    private CachingWorkflowImageManagerImpl cacheManager;
    private String defaultWorkflowSavedLayout;

    @Before
    public void setup() throws IOException, SAXException, InvalidWorkflowDescriptorException {
        //load the default workflow
        InputStream workflowXml = this.getClass().getClassLoader().getResourceAsStream("default-jira-workflow.xml");

       // StringWriter writer = new StringWriter();
       // IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("default-layout.json"), writer);
        //defaultWorkflowSavedLayout = writer.toString();

        defaultWorkflowSavedLayout = "";
        
        workflowDescriptor = WorkflowLoader.load(workflowXml, true);
        workflowManager = mock(WorkflowManager.class);

        //mock the jiraHome
        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        final File baseCacheDir = new File(sysTempDir, "caches");

        if (!baseCacheDir.exists()) {
            baseCacheDir.mkdirs();
        }

        jiraHome = mock(JiraHome.class);
        when(jiraHome.getCachesDirectory()).thenReturn(baseCacheDir);

        //clean the cache
        File thumbsDir = new File(jiraHome.getCachesDirectory(), CachingWorkflowImageManagerImpl.BASE_CACHE_FOLDER);
        if (thumbsDir.exists()) {
            FileUtils.deleteDirectory(thumbsDir);
        }

        this.workflowLayoutManager = new WorkflowLayoutManagerImpl(new MockVWDPropertySet());
        cacheManager = new CachingWorkflowImageManagerImpl(new WorkflowImageManagerImpl(workflowLayoutManager), jiraHome);

        MockComponentWorker worker = new MockComponentWorker();
        worker.addMock(ConstantsManager.class, new MockStatusConstantsManager());
        ComponentAccessor.initialiseWorker(worker);
    }

    @Test
    public void createsFirstImageInEmptyCache() throws Exception {
        //create a junk image
        mxGraph graph = new mxGraph();
        mxRectangle rect = new mxRectangle(0, 0, 600, 600);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, rect);

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);

        WorkflowThumbnailParams params = new WorkflowThumbnailParams.Builder(workflow).setWidth(1024).setHeight(600).build();
        cacheManager.getThumbnailStream(params);

        File thumbFile = cacheManager.getCacheFile(params.getWorkflow(), -1, Integer.toString(params.getWidth()), Integer.toString(params.getHeight()), false, false);

        assertTrue("Thumb should have been added to empty cache", thumbFile.exists());

    }

    @Test
    public void scrubInvalidFilename() throws Exception {

        final String invalidName = "!nval1d Name: [yes *$$\\";
        final String validName = "!nval1d-Name--[yes--$$-";

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow(invalidName, false, workflowDescriptor, workflowManager,true);

        File thumbFile = cacheManager.getCacheFile(workflow, -1, "600", "600", true, false);
        File workflowFolder = new File(thumbFile.getParent());

        assertEquals(validName, workflowFolder.getName());

    }

    @Test
    public void cachedFileDateForSameWorkflow() throws Exception {

        mxGraph graph = new mxGraph();
        mxRectangle rect = new mxRectangle(0, 0, 600, 600);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, rect);

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);

        WorkflowThumbnailParams params = new WorkflowThumbnailParams.Builder(workflow).setWidth(600).setHeight(600).build();
        cacheManager.getThumbnailStream(params);

        File thumbFile = cacheManager.getCacheFile(params.getWorkflow(), -1, Integer.toString(params.getWidth()), Integer.toString(params.getHeight()), false, false);

        //create a workflow2
        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);
        File cacheFile = cacheManager.getCacheFile(workflow2, -1, Integer.toString((int) rect.getWidth()), Integer.toString((int)rect.getHeight()), false, false);

        assertTrue("Cache file doesn't exist", cacheFile.exists());
        assertEquals(thumbFile.lastModified(), cacheFile.lastModified());

    }

    @Test
    public void cachedStreamsAreSameForWorkflow() throws Exception {

        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("workflow with content", false, workflowDescriptor, workflowManager,true);

        //add it to the cache
        WorkflowThumbnailParams params = new WorkflowThumbnailParams.Builder(workflow).setWidth(400).setHeight(400).build();
        InputStream firstStream = cacheManager.getThumbnailStream(params);


        //make sure the workflow we're testing against has a time EARLIER than the image
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -5);
        workflowDescriptor.getMetaAttributes().put(AbstractJiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(cal.getTime().getTime()));

        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("workflow with content", false, workflowDescriptor, workflowManager,true);
        WorkflowThumbnailParams params2 = new WorkflowThumbnailParams.Builder(workflow2).setWidth(400).setHeight(400).build();
        InputStream secondStream = cacheManager.getThumbnailStream(params2);

        boolean sameContent = IOUtils.contentEquals(firstStream,secondStream);

        assertTrue("Streams contain different content", sameContent);

    }

    @Test
    public void nullStreamForInvalidCache() throws Exception {
        mxGraph graph = new mxGraph();
        mxRectangle rect = new mxRectangle(0, 0, 600, 600);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, rect);

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);

        WorkflowThumbnailParams params = new WorkflowThumbnailParams.Builder(workflow).setWidth(600).setHeight(600).build();
        cacheManager.getThumbnailStream(params);

        //create a workflow with a date more recent than the image
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        workflowDescriptor.getMetaAttributes().put(AbstractJiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(cal.getTime().getTime()));

        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);
        InputStream cacheStream = cacheManager.getStreamFromCache(workflow2, -1, Integer.toString((int) rect.getWidth()), Integer.toString((int)rect.getHeight()), true, false);

        assertNull("Invalid Cache stream should be null", cacheStream);

    }

    @Test
    public void trueHasValidImageForValidCache() throws Exception {
        mxGraph graph = new mxGraph();
        mxRectangle rect = new mxRectangle(0, 0, 600, 600);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, rect);

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);
        WorkflowThumbnailParams params = new WorkflowThumbnailParams.Builder(workflow).setWidth(600).setHeight(600).build();
        cacheManager.getThumbnailStream(params);

        //create a workflow with a date less recent than the image
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -5);
        workflowDescriptor.getMetaAttributes().put(AbstractJiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(cal.getTime().getTime()));

        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);
        boolean isValid = cacheManager.cacheHasValidImage(workflow2, -1, Integer.toString((int) rect.getWidth()), Integer.toString((int)rect.getHeight()), false, false);

        assertTrue("hasValidImage should be true", isValid);

    }

    @Test
    public void falseHasValidImageForInvalidCache() throws Exception {
        mxGraph graph = new mxGraph();
        mxRectangle rect = new mxRectangle(0, 0, 600, 600);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, rect);

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);

        WorkflowThumbnailParams params = new WorkflowThumbnailParams.Builder(workflow).setWidth(600).setHeight(600).build();
        cacheManager.getThumbnailStream(params);

        //create a workflow with a date more recent than the image
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        workflowDescriptor.getMetaAttributes().put(AbstractJiraWorkflow.JIRA_META_UPDATED_DATE, Long.toString(cal.getTime().getTime()));

        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);
        boolean isValid = cacheManager.cacheHasValidImage(workflow2, -1, Integer.toString((int) rect.getWidth()), Integer.toString((int)rect.getHeight()), true, false);

        assertFalse("hasValidImage should be false", isValid);

    }

    @Test
    public void draftHasDifferentNameThanParent() throws Exception {

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);

        File parentFile = cacheManager.getCacheFile(workflow, -1, "600", "600", true, false);

        //create a draft workflow
        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("emptyWorkflow", true, workflowDescriptor, workflowManager,true);
        File draftFile = cacheManager.getCacheFile(workflow2, -1, "600", "600", true, false);

        assertFalse("Draft and Parent names should be different", parentFile.getName().equals(draftFile.getName()));

    }

    @Test
    public void labelsOnHasDifferentNameThanLabelsOff() throws Exception {

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);

        File onFile = cacheManager.getCacheFile(workflow, -1, "600", "600", true, false);

        //create a draft workflow
        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);
        File offFile = cacheManager.getCacheFile(workflow2, -1, "600", "600", false, false);

        assertFalse("Draft and Parent names should be different", onFile.getName().equals(offFile.getName()));

    }

    @Test
    public void aspectOnHasDifferentNameThanAspectOff() throws Exception {
        mxGraph graph = new mxGraph();
        mxRectangle rect = new mxRectangle(0, 0, 600, 600);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, rect);

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);

        File onFile = cacheManager.getCacheFile(workflow, -1, "600", "600", true, false);

        //create a draft workflow
        JiraWorkflow workflow2 = new SimpleConfigurableJiraWorkflow("emptyWorkflow", false, workflowDescriptor, workflowManager,true);
        File offFile = cacheManager.getCacheFile(workflow2, -1, "600", "600", true, true);

        assertFalse("Draft and Parent names should be different", onFile.getName().equals(offFile.getName()));

    }

    private class MockVWDPropertySet implements WorkflowDesignerPropertySet {

        public void setProperty(String key, String value) {
            //do nothing
        }

        public String getProperty(String key) {
            return defaultWorkflowSavedLayout;
        }

        public void removeProperty(String key) {
            //do nothing;
        }

        public boolean hasProperty(String key) {
            return true;
        }
    }

}


