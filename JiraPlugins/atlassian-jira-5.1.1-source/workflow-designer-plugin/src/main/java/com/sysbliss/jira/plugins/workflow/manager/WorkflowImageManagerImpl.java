package com.sysbliss.jira.plugins.workflow.manager;

import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.png.mxPngEncodeParam;
import com.mxgraph.util.png.mxPngImageEncoder;
import com.mxgraph.view.mxGraph;
import com.sysbliss.jira.plugins.workflow.jgraph.WorkflowToJGraphxTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Author: jdoklovic
 */
public class WorkflowImageManagerImpl implements WorkflowImageManager {

    public static Logger log = Logger.getLogger(WorkflowImageManagerImpl.class);
    public static int MAX_WIDTH = 3000;
    public static int MAX_HEIGHT = 3000;
    public static int MIN_WIDTH = 20;
    public static int MIN_HEIGHT = 20;

    private WorkflowLayoutManager workflowLayoutManager;

    public WorkflowImageManagerImpl(WorkflowLayoutManager workflowLayoutManager) {
        this.workflowLayoutManager = workflowLayoutManager;

        System.setProperty("java.awt.headless", "true");

        //JRA-28648:  MetalLookAndFeel keeps a static cache of the sun.awt.AppContext which eventually has a reference
        // to a java.awt.EventQueue. The java.awt.EventQueue caches the TCC of the thread that created it which
        //Spring DM has set to JWD's classloader by the time this method is called. This is a problem because
        //MetalLookAndFeel comes from the system classloader and is only initialised once which means that it
        //keeps a reference to the Bundle Classloader of the first JWD around until JVM restart.
        //
        //We get around around this by setting the TCC to null while we call this code.
        final Thread currentThread = Thread.currentThread();
        final ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(null);
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            UIManager.put("JTextField", javax.swing.plaf.metal.MetalTextFieldUI.class.getName());
        } catch (final Exception e) {
            log.error("Error loading MetalLookAndFeel in headless mode!", e);
        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }
    }

    public BufferedImage getThumbnailImage(WorkflowThumbnailParams params) throws Exception {

        if ((params.getWidth() > MAX_WIDTH)) {
            throw new IllegalArgumentException("width must be <= " + MAX_WIDTH);
        }

        if (params.getHeight() > MAX_HEIGHT) {
            throw new IllegalArgumentException("height must be <= " + MAX_HEIGHT);
        }

        if ((params.getWidth() < MIN_WIDTH)) {
            throw new IllegalArgumentException("width must be >= " + MIN_WIDTH);
        }

        if (params.getHeight() < MIN_HEIGHT) {
            throw new IllegalArgumentException("height must be >= " + MIN_HEIGHT);
        }

        WorkflowToJGraphxTransformer transformer = new WorkflowToJGraphxTransformer(workflowLayoutManager);
        mxGraph graph = transformer.transform(params.getWorkflow(), params.getStepId(), params.showLabels());

        mxRectangle graphBounds = graph.getGraphBounds();

        graph.getGraphBounds().grow(10.0);
        BufferedImage image = mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, graphBounds);

        double xScale = params.getWidth() / graphBounds.getWidth();
        double yScale = params.getHeight() / graphBounds.getHeight();

        double scaleX = Math.min(xScale, yScale);
        double scaleY = scaleX;

        int scaledWidth = (int) Math.ceil(graphBounds.getWidth() * scaleX);
        int scaledHeight = (int) Math.ceil(graphBounds.getHeight() * scaleY);
        BufferedImage scaledImage = mxUtils.createBufferedImage(scaledWidth, scaledHeight, Color.WHITE);

        AffineTransform xform = null;
        Graphics2D graphics2D = scaledImage.createGraphics();

        if (scaledWidth < graphBounds.getWidth() && scaledHeight < graphBounds.getHeight()) {
            xform = AffineTransform.getScaleInstance(scaleX, scaleY);
        }

        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(image, xform, null);
        graphics2D.dispose();

        BufferedImage finalImage = scaledImage;

        if (!params.maintainAspect()) {
            finalImage = mxUtils.createBufferedImage(params.getWidth(), params.getHeight(), Color.WHITE);
            Graphics2D g = finalImage.createGraphics();
            g.drawImage(scaledImage, null, 0, 0);

        }

        return finalImage;
    }

    public InputStream getThumbnailStream(WorkflowThumbnailParams params) throws Exception {
        InputStream is = null;

        BufferedImage finalImage = getThumbnailImage(params);

        mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(finalImage);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
            encoder.encode(finalImage);
            is = new ByteArrayInputStream(outputStream.toByteArray());

        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return is;
    }

    public BufferedImage getFullImage(WorkflowImageParams params) throws Exception {
        WorkflowToJGraphxTransformer transformer = new WorkflowToJGraphxTransformer(workflowLayoutManager);
        mxGraph graph = transformer.transform(params.getWorkflow(), params.getStepId(), params.showLabels());

        mxRectangle graphBounds = graph.getGraphBounds();

        graph.getGraphBounds().grow(10.0);

        return mxCellRenderer.createBufferedImage(graph, null, 1, Color.WHITE, true, graphBounds);

    }

    public InputStream getFullImageStream(WorkflowImageParams params) throws Exception {
        InputStream is = null;

        BufferedImage image = getFullImage(params);

        mxPngEncodeParam param = mxPngEncodeParam.getDefaultEncodeParam(image);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            mxPngImageEncoder encoder = new mxPngImageEncoder(outputStream, param);
            encoder.encode(image);
            is = new ByteArrayInputStream(outputStream.toByteArray());

        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return is;

    }

}
