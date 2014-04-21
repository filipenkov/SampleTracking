package com.sysbliss.jira.plugins.workflow.jgraph;

import com.mxgraph.canvas.mxGraphics2DCanvas;
import com.mxgraph.shape.mxIMarker;
import com.mxgraph.shape.mxMarkerRegistry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.*;

import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 3/2/11
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class JGraphxWorkflowStyles {

    public static mxEdgeStyle.mxEdgeStyleFunction JIRA_EDGE_LOOP = new mxEdgeStyle.mxEdgeStyleFunction()
	{

		/* (non-Javadoc)
		 * @see com.mxgraph.view.mxEdgeStyle.mxEdgeStyleFunction#apply(com.mxgraph.view.mxCellState, com.mxgraph.view.mxCellState, com.mxgraph.view.mxCellState, java.util.List, java.util.List)
		 */
		public void apply(mxCellState state, mxCellState source,
				mxCellState target, java.util.List<mxPoint> points, java.util.List<mxPoint> result)
		{
			if (source != null)
			{
				mxGraphView view = state.getView();
				mxGraph graph = view.getGraph();


                mxRectangle startRect = source.getBoundingBox();

                    mxPoint startPoint = new mxPoint(startRect.getCenterX(), startRect.getCenterY());
                    mxPoint endPoint = new mxPoint(startPoint.getX(), startPoint.getY());

				mxPoint p1 = new mxPoint(startPoint.getX() - ((startPoint.getX() - startRect.getX()) + 15), startPoint.getY());
                    mxPoint p2 = new mxPoint(p1.getX(), ((startPoint.getY() + startRect.getHeight()) + 15));
                    mxPoint p3 = new mxPoint(endPoint.getX(), p2.getY());

				result.add(p1);
				result.add(p2);
                result.add(p3);
			}
		}
	};
    
    public static final String KEY_LAYER = "JIRALAYER";
    public static final Hashtable STYLE_LAYER = initLayerStyle();

    public static final String KEY_JIRA_STEP = "JIRASTEP";
    public static final Hashtable STYLE_JIRA_STEP = initJiraStepStyle();

    public static final String KEY_JIRA_STEP_SELECTED = "SELECTEDJIRASTEP";
    public static final Hashtable STYLE_JIRA_STEP_SELECTED = initJiraStepSelectedStyle();

    public static final String KEY_JIRA_ACTION = "JIRAACTION";
    public static final Hashtable STYLE_JIRA_ACTION = initJiraActionStyle();

    public static final String KEY_JIRA_ARROW_STYLE = "JIRAARROW";


    private static Hashtable initLayerStyle() {
        Hashtable<String, Object> layerStyle = new Hashtable<String, Object>();
        layerStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
        layerStyle.put(mxConstants.STYLE_OPACITY, 0);

        return layerStyle;
    }

    private static Hashtable initJiraStepSelectedStyle() {
        Hashtable<String, Object> stepStyle = new Hashtable<String, Object>();
        stepStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LABEL);
        stepStyle.put(mxConstants.STYLE_OPACITY, 100);
        stepStyle.put(mxConstants.STYLE_FONTCOLOR, "#111111");
        stepStyle.put(mxConstants.STYLE_FONTSIZE, "14");
        stepStyle.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        stepStyle.put(mxConstants.STYLE_FONTFAMILY, "Arial,arial,FreeSans,Helvetica,sans-serif,_sans");
        stepStyle.put(mxConstants.STYLE_ROUNDED, "true");
        stepStyle.put(mxConstants.STYLE_FILLCOLOR, "#ffff99");
        stepStyle.put(mxConstants.STYLE_STROKECOLOR, "#bbbbbb");
        stepStyle.put(mxConstants.STYLE_STROKEWIDTH, "1");
        stepStyle.put(mxConstants.STYLE_SPACING_TOP, "-5");
        stepStyle.put(mxConstants.STYLE_SPACING_BOTTOM, "-5");
        stepStyle.put(mxConstants.STYLE_SPACING_LEFT, "18");
        stepStyle.put(mxConstants.STYLE_SPACING_RIGHT, "2");
        stepStyle.put(mxConstants.STYLE_IMAGE_WIDTH, 27);
        stepStyle.put(mxConstants.STYLE_IMAGE_HEIGHT, 35);
        stepStyle.put(mxConstants.STYLE_IMAGE_ALIGN, mxConstants.ALIGN_LEFT);

        return stepStyle;
    }

    private static Hashtable initJiraStepStyle() {
        Hashtable<String, Object> stepStyle = new Hashtable<String, Object>();
        stepStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_LABEL);
        stepStyle.put(mxConstants.STYLE_OPACITY, 100);
        stepStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        stepStyle.put(mxConstants.STYLE_FONTSIZE, "12");
        stepStyle.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);
        stepStyle.put(mxConstants.STYLE_FONTFAMILY, "arial,FreeSans,Helvetica,sans-serif,_sans");
        stepStyle.put(mxConstants.STYLE_ROUNDED, "true");
        stepStyle.put(mxConstants.STYLE_FILLCOLOR, "#f0f0f0");
        stepStyle.put(mxConstants.STYLE_STROKECOLOR, "#bbbbbb");
        stepStyle.put(mxConstants.STYLE_STROKEWIDTH, "1");
        stepStyle.put(mxConstants.STYLE_SPACING_TOP, "-5");
        stepStyle.put(mxConstants.STYLE_SPACING_BOTTOM, "-5");
        stepStyle.put(mxConstants.STYLE_SPACING_LEFT, "18");
        stepStyle.put(mxConstants.STYLE_SPACING_RIGHT, "2");
        stepStyle.put(mxConstants.STYLE_IMAGE_WIDTH, 27);
        stepStyle.put(mxConstants.STYLE_IMAGE_HEIGHT, 35);
        stepStyle.put(mxConstants.STYLE_IMAGE_ALIGN, mxConstants.ALIGN_LEFT);

        return stepStyle;
    }

    private static Hashtable initJiraActionStyle() {
        Hashtable<String, Object> edgeStyle = new Hashtable<String, Object>();
        edgeStyle.put(mxConstants.STYLE_FONTCOLOR, "#003366");
        edgeStyle.put(mxConstants.STYLE_FONTSIZE, "11");
        edgeStyle.put(mxConstants.STYLE_FONTFAMILY, "arial,FreeSans,Helvetica,sans-serif,_sans");
        edgeStyle.put(mxConstants.STYLE_EDGE, mxConstants.STYLE_NOEDGESTYLE);
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#BBBBBB");
        edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, "1");
        edgeStyle.put(mxConstants.STYLE_ENDARROW, KEY_JIRA_ARROW_STYLE);
        edgeStyle.put(mxConstants.STYLE_ENDSIZE, "7");
        edgeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#D0DFEE");
        edgeStyle.put(mxConstants.STYLE_LABEL_BORDERCOLOR,"#3c78b5");
        edgeStyle.put(mxConstants.EDGESTYLE_LOOP, JGraphxWorkflowStyles.JIRA_EDGE_LOOP);
        //mxConstants.STYLE_LOOP

        return edgeStyle;
    }



    static {
        mxIMarker tmp = new mxIMarker() {
            public mxPoint paintMarker(mxGraphics2DCanvas canvas,
                                       mxCellState state, String type, mxPoint pe, double nx,
                                       double ny, double size) {
                Polygon poly = new Polygon();
                poly.addPoint((int) Math.round(pe.getX()), (int) Math.round(pe.getY()));
                poly.addPoint((int) Math.round(pe.getX() - nx * 2 - ny / 2), (int) Math.round(pe.getY() - ny * 2 + nx / 2));

                poly.addPoint((int) Math.round(pe.getX() + ny / 2 - nx * 2), (int) Math.round(pe.getY() - ny * 2 - nx / 2));

                canvas.fillShape(poly);
                canvas.getGraphics().draw(poly);

                return new mxPoint(-nx, -ny);
            }
        };

        mxMarkerRegistry.registerMarker(KEY_JIRA_ARROW_STYLE, tmp);
    }
}
