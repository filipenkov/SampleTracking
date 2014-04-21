package com.atlassian.jira.plugins.workflow;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import com.sysbliss.jira.plugins.workflow.manager.*;
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
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Note: this is not really a test. It's a class to help debug image layout problems without having to boot up JIRA
 */
public class LocalImageLayoutRunnerTest {

    private WorkflowDescriptor workflowDescriptor;
    private WorkflowManager workflowManager;
    private WorkflowLayoutManager workflowLayoutManager;
    private JiraHome jiraHome;
    private CachingWorkflowImageManagerImpl cacheManager;
    private String savedLayout;

    @Before
    public void setup() throws IOException, SAXException, InvalidWorkflowDescriptorException {
        //load the workflow
        InputStream workflowXml = this.getClass().getClassLoader().getResourceAsStream("simple-workflow.xml");

        //load the layouyt
        StringWriter writer = new StringWriter();
        IOUtils.copy(this.getClass().getClassLoader().getResourceAsStream("simple-layout-no-edge-spacing.json"), writer);
        savedLayout = writer.toString();

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
            FileUtils.cleanDirectory(thumbsDir);
        }

        ConstantsManager constantsManager = new MockStatusConstantsManager();
        this.workflowLayoutManager = new WorkflowLayoutManagerImpl(constantsManager, new MockVWDPropertySet());
        cacheManager = new CachingWorkflowImageManagerImpl(new WorkflowImageManagerImpl(constantsManager, workflowLayoutManager), jiraHome);
    }

    @Test
    public void createSimpleImage() throws Exception {
        //create a junk image
        mxGraph graph = new mxGraph();
        mxRectangle rect = new mxRectangle(0, 0, 600, 600);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, rect);

        //create a workflow
        JiraWorkflow workflow = new SimpleConfigurableJiraWorkflow("simple", false, workflowDescriptor, workflowManager,true);

        WorkflowImageParams params = new WorkflowImageParams.Builder(workflow).setShowLabels(true).build();
        cacheManager.getFullImageStream(params);

        assertTrue("i always work", true);
    }

    private class MockVWDPropertySet implements WorkflowDesignerPropertySet {

        public void setProperty(String key, String value) {
            //do nothing
        }

        public String getProperty(String key) {
            return savedLayout;
        }

        public void removeProperty(String key) {
            //do nothing;
        }

        public boolean hasProperty(String key) {
            return true;
        }
    }

}
