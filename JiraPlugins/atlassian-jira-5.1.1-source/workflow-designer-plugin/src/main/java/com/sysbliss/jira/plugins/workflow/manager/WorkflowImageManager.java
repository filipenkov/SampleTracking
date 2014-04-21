package com.sysbliss.jira.plugins.workflow.manager;

import com.atlassian.jira.workflow.JiraWorkflow;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: jdoklovic
 */
public interface WorkflowImageManager {

    BufferedImage getThumbnailImage(WorkflowThumbnailParams workflowThumbailParams) throws Exception;
    BufferedImage getFullImage(WorkflowImageParams workflowImageParams) throws Exception;

    InputStream getThumbnailStream(WorkflowThumbnailParams workflowThumbailParams) throws Exception;
    InputStream getFullImageStream(WorkflowImageParams workflowImageParams) throws Exception;
}
