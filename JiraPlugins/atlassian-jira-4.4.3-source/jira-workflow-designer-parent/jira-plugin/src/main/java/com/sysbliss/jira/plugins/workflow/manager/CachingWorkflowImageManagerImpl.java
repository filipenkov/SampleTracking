package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;

/**
 * Author: jdoklovic
 */
public class CachingWorkflowImageManagerImpl implements WorkflowImageManager {

    public static Logger log = Logger.getLogger(CachingWorkflowImageManagerImpl.class);
    public static String BASE_CACHE_FOLDER = "workflowimages";

    private WorkflowImageManager delegateImageManager;
    private JiraHome jiraHome;

    public CachingWorkflowImageManagerImpl(WorkflowImageManager delegateImageManager, JiraHome jiraHome) {
        this.delegateImageManager = delegateImageManager;
        this.jiraHome = jiraHome;
    }

    public BufferedImage getThumbnailImage(WorkflowThumbnailParams params) throws Exception {
        String widthName = Integer.toString(params.getWidth());
        String heightName = Integer.toString(params.getHeight());
        BufferedImage image = null;

        if (cacheHasValidImage(params.getWorkflow(), params.getStepId(), widthName, heightName, params.showLabels(), params.maintainAspect())) {
            image = ImageIO.read(getCacheFile(params.getWorkflow(), params.getStepId(), widthName, heightName, params.showLabels(), params.maintainAspect()));
        } else {
            image = delegateImageManager.getThumbnailImage(params);
        }

        return image;
    }

    public BufferedImage getFullImage(WorkflowImageParams params) throws Exception {
        BufferedImage image = null;

        if (cacheHasValidImage(params.getWorkflow(), params.getStepId(), "full", "full", params.showLabels(), true)) {
            image = ImageIO.read(getCacheFile(params.getWorkflow(), params.getStepId(), "full", "full", params.showLabels(), true));
        } else {
            image = delegateImageManager.getFullImage(params);
        }

        return image;
    }

    public InputStream getThumbnailStream(WorkflowThumbnailParams params) throws Exception {

        String widthName = Integer.toString(params.getWidth());
        String heightName = Integer.toString(params.getHeight());
        InputStream is = getStreamFromCache(params.getWorkflow(), params.getStepId(), widthName, heightName, params.showLabels(), params.maintainAspect());

        if (is == null) {
            BufferedImage image = delegateImageManager.getThumbnailImage(params);
            if (image != null) {
                File cacheFile = addImageToCache(image, params.getWorkflow(), params.getStepId(), widthName, heightName, params.showLabels(), params.maintainAspect());
                try {
                    is = new FileInputStream(cacheFile);
                } catch (Exception e) {
                    log.error("Workflow Thumbnail File Missing!", e);
                }
            }
        }

        return is;
    }

    public InputStream getFullImageStream(WorkflowImageParams params) throws Exception {
        String widthName = "full";
        String heightName = "full";
        InputStream is = getStreamFromCache(params.getWorkflow(), params.getStepId(), widthName, heightName, params.showLabels(), true);

        if (is == null) {
            BufferedImage image = delegateImageManager.getFullImage(params);
            if (image != null) {
                File cacheFile = addImageToCache(image, params.getWorkflow(), params.getStepId(), widthName, heightName, params.showLabels(), true);
                try {
                    is = new FileInputStream(cacheFile);
                } catch (Exception e) {
                    log.error("Workflow Thumbnail File Missing!", e);
                }
            }
        }

        return is;
    }

    public InputStream getStreamFromCache(JiraWorkflow workflow, int stepId, String width, String height, boolean showLabels, boolean maintainAspect) {
        InputStream is = null;

        if (cacheHasValidImage(workflow, stepId, width, height, showLabels, maintainAspect)) {
            File file = getCacheFile(workflow, stepId, width, height, showLabels, maintainAspect);
            try {
                is = new FileInputStream(file);
            } catch (Exception e) {
                log.error("Workflow Thumbnail File Missing!", e);
            }
        }

        return is;
    }

    public File addImageToCache(BufferedImage image, JiraWorkflow workflow, int stepId, String width, String height, boolean showLabels, boolean maintainAspect) throws Exception {
        File file = getCacheFile(workflow, stepId, width, height, showLabels, maintainAspect);

        File baseDir = new File(file.getParent());
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
        FileOutputStream outputStream = new FileOutputStream(file);
        try {
            mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
            encoder.encode(image);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return file;
    }

    public boolean cacheHasValidImage(JiraWorkflow workflow, int stepId, String width, String height, boolean showLabels, boolean maintainAspect) {
        boolean cacheIsValid = false;

        File file = getCacheFile(workflow, stepId, width, height, showLabels, maintainAspect);

        if (file.exists()) {
            //check to see if we need to re-generate the image
            Date workflowUpdated = workflow.getUpdatedDate();
            Date imageUpdated = new Date(file.lastModified());

            /*
            currently workflow.isDefault() negates workflowUpdated != null, however, if we ever ship other workflows
            with JIRA, they may have a null updated date like the default workflow does now
             */
            if (workflow.isDefault() || (workflowUpdated != null && !imageUpdated.before(workflowUpdated))) {
                //we have a valid cache image
                cacheIsValid = true;
            }
        }

        return cacheIsValid;
    }

    public File getCacheFile(JiraWorkflow workflow, int stepId, String width, String height, boolean showLabels, boolean maintainAspect) {

        return new File(getCacheFolder(workflow), createFilename(workflow, stepId, width, height, showLabels, maintainAspect));
    }

    public File getCacheFolder(JiraWorkflow workflow) {

        return getCacheFolder(workflow.getName());
    }

    public File getCacheFolder(String workflowName) {

        //This should probably be replaced. I'm sure a util in JIRA already has a better way to sanitize.
        String folderName = workflowName.replaceAll("[:\\\\/*?|<> _]", "-");
        File baseFolder = new File(jiraHome.getCachesDirectory(), BASE_CACHE_FOLDER);

        return new File(baseFolder, folderName);
    }

    public void clearCacheForWorkflow(JiraWorkflow workflow) {
        clearCacheForWorkflow(workflow.getName());
    }

    public void clearCacheForWorkflow(String workflowName) {
        File cacheFolder = getCacheFolder(workflowName);
        if(cacheFolder.exists() && cacheFolder.canWrite()) {
            try {
                FileUtils.cleanDirectory(cacheFolder);
            } catch (IOException e) {
                log.error("Unable to remove cached workflow images", e);
            }
        }

    }

    //This is a pretty cheap filename generator, but it works.
    public String createFilename(JiraWorkflow workflow, int stepId, String width, String height, boolean showLabels, boolean maintainAspect) {

        StringBuffer buff = new StringBuffer();

        if (workflow.isDraftWorkflow()) {
            buff.append("D");
        }

        if (stepId > -1) {
            buff.append("S").append(Integer.toString(stepId));
        }

        buff.append("W").append(width).append("H").append(height);

        if (showLabels) {
            buff.append("L1");
        } else {
            buff.append("L0");
        }

        if (maintainAspect) {
            buff.append("A1");
        } else {
            buff.append("A0");
        }

        buff.append(".png");

        return buff.toString().replaceAll("[:\\\\/*?|<> _]", "-");
    }
}
