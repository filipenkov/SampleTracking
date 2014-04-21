package com.sysbliss.jira.workflow.layout
{

import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.ToolTypes;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.diagram.data.Node;
import com.sysbliss.diagram.ui.DefaultUIEdge;
import com.sysbliss.diagram.ui.StickyNote;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.diagram.ui.UINode;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexWorkflowObject;
import com.sysbliss.jira.plugins.workflow.model.layout.AnnotationLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.EdgeLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPoint;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayout;
import com.sysbliss.util.PointUtils;

import flash.geom.Point;
import flash.geom.Rectangle;

import mx.collections.ArrayCollection;
import mx.core.UIComponent;

public class LayoutImporter
{
    //private var _nodeLayoutMap:HashMap;
    //private var _edgeLayoutMap:HashMap;
    private var _edgeLayoutsToProcess:Array;
    private var _processedEdges:ArrayCollection;
    private var _processedNodes:Array;
    private var _diagram:Diagram;
    private var _initialLayout:Boolean;
    private var _nodeOffsetX:Number;

    public function LayoutImporter()
    {
    }

    public function applyLayout(diagram:Diagram, layout:JWDLayout, initialLayout:Boolean = false):void
    {
        resetLayout(diagram);
        //_nodeLayoutMap = new HashMap();
        //_edgeLayoutMap = new HashMap();
        _processedNodes = new Array();
        _edgeLayoutsToProcess = new Array();
        _processedEdges = new ArrayCollection();

        _diagram = diagram;
        _initialLayout = initialLayout;

        if(layout.graphBounds && layout.graphBounds.width > 0) {
            var graphCenterX:Number = (layout.graphBounds.width / 2);
            var diagramCenterX:Number = (diagram.contentLayer.width / 2);

            //_nodeOffsetX = diagramCenterX - graphCenterX;
            _nodeOffsetX = 0;
        }
        var i:int;
        var layoutRoot:NodeLayout;

        for (i = 0; i < layout.roots.length; i++)
        {
            layoutRoot = layout.roots[i] as NodeLayout;

            applyNodeLayout(layoutRoot);
        }

        processEdges();
        processMissingRecursiveEdges();

        processAnnotations(layout.annotations);

        diagram.contentLayer.validateNow();
        UIComponent(diagram).invalidateDisplayList();
        UIComponent(diagram).validateNow();
    }

    private function processAnnotations(annotations:ArrayCollection):void {
        var i:int;
        for(i = 0;i<annotations.length;i++) {
            var layout:AnnotationLayout = annotations.getItemAt(i) as AnnotationLayout;
            trace("ann layout id:" + layout.id);
            var sticky:StickyNote = _diagram.getStickyByAnnotationId(layout.id);
            trace("sticky: " + sticky);

            if(sticky != null) {
                var rect:Rectangle = new Rectangle(layout.rect.x, layout.rect.y, layout.rect.width,layout.rect.height);
                _diagram.updateStickyDisplay(sticky,rect);
            }
        }
    }

    public function resetLayout(diagram:Diagram):void
    {
        _nodeOffsetX = 0;
        var center:Point = diagram.centerPoint;
        var uinodes:Vector.<UINode> = diagram.getUINodes();
        var uiedges:Vector.<UIEdge> = diagram.getUIEdges();
        var uinode:UINode;
        var uiedge:UIEdge;
        var n:int;
        var e:int;
        var cpoints:Vector.<Point>;
        for (e = 0; e < uiedges.length; e++)
        {
            uiedge = uiedges[e] as UIEdge;
            uiedge.controlPoints = null;
            uiedge.lineType = ToolTypes.LINK_STRAIGHT.name;
            uiedge.uiEdgeLabel.x = -200;
            uiedge.uiEdgeLabel.y = -200;
            UIComponent(uiedge).validateNow();
        }
        for (n = 0; n < uinodes.length; n++)
        {
            uinode = uinodes[n] as UINode;
            //uinode.move(center.x, center.y);
        }
    }


    public function applyNodeLayout(nodeLayout:NodeLayout):void
    {
        var uiNode:UINode = findMatchingDiagramNode(nodeLayout);
        if (uiNode != null && _processedNodes.indexOf(uiNode) < 0)
        {
            _processedNodes.push(uiNode);
            /*if(nodeLayout.isInitialAction) {
                var newX:Number = (_diagram.contentLayer.width / 2) - (uiNode.width / 2);
                trace("got initial action: " + uiNode.node.data.name + " moving x to " + newX);
                trace("could have moved x to " + newX + _nodeOffsetX);
                uiNode.move(newX, 30);
                uiNode.x = newX;
                uiNode.y = 30;
                UIComponent(uiNode).callLater(uiNode.move,[newX,30]);
                UIComponent(uiNode).validateNow();
            } else {*/
                uiNode.move(nodeLayout.rect.x + _nodeOffsetX, nodeLayout.rect.y);
           // }


            UIComponent(uiNode).validateNow();
        }
        crawlEdges(nodeLayout);
    }

    private function crawlEdges(nodeLayout:NodeLayout):void
    {
        var edgeLayouts:ArrayCollection = nodeLayout.outLinks;
        var edgeLayout:EdgeLayout;
        var i:int;
        var isRecursive:Boolean = false;
        for (i = 0; i < edgeLayouts.length; i++)
        {
            edgeLayout = edgeLayouts[i] as EdgeLayout;

            if (_edgeLayoutsToProcess.indexOf(edgeLayout) < 0)
            {
                _edgeLayoutsToProcess.push(edgeLayout);
                applyNodeLayout(edgeLayout.endNode);
            }
        }
    }

    public function processEdges():void
    {
        var edgeLayout:EdgeLayout;
        var uiEdge:UIEdge;
        var i:int;
        for (i = 0; i < _edgeLayoutsToProcess.length; i++)
        {
            edgeLayout = _edgeLayoutsToProcess[i] as EdgeLayout;
            uiEdge = findMatchingDiagramEdge(edgeLayout);
            if (uiEdge == null)
            {
                continue;
            }

            var newLineType:String = ToolTypes.LINK_STRAIGHT.name;
            if (edgeLayout.lineType != null && edgeLayout.lineType != "")
            {
                newLineType = edgeLayout.lineType;
            }
            var p:int;

            uiEdge.moveStartPoint(edgeLayout.startPoint.x + _nodeOffsetX, edgeLayout.startPoint.y);
            uiEdge.moveEndPoint(edgeLayout.endPoint.x + _nodeOffsetX, edgeLayout.endPoint.y);
            if(uiEdge.edge.startNode.data.id == uiEdge.edge.endNode.data.id && edgeLayout.controlPoints.length < 1) {
                //recursive transition
                uiEdge.lineType = ToolTypes.LINK_STRAIGHT.name;
                uiEdge.controlPoints = null;
                uiEdge.renderAsRecursive();
                UIComponent(uiEdge).validateNow();

            }else if (edgeLayout.controlPoints.length > 0)
            {
                uiEdge.lineType = ToolTypes.LINK_STRAIGHT.name;
                UIComponent(uiEdge).validateNow();
                var c:int;
                var layoutPoint:LayoutPoint;

                for (c = 0; c < edgeLayout.controlPoints.length; c++)
                {
                    layoutPoint = edgeLayout.controlPoints[c] as LayoutPoint;
                    var localPoint:Point = new Point((layoutPoint.x + _nodeOffsetX), layoutPoint.y)
                    //var diagramPoint:Point = PointUtils.convertCoordinates(localPoint,uiEdge,_diagram.edgeLayer);
                    var diagramPoint:Point = localPoint;
                    uiEdge.pushControlPoint(diagramPoint);
                }
                uiEdge.lineType = newLineType;
                UIComponent(uiEdge).validateNow();

                /*
                var posPoint:LayoutPoint;
                var edgePosPoint:Point;
                for (p = 0; p < edgeLayout.controlPoints.length; p++)
                {
                    layoutPoint = edgeLayout.controlPoints[p] as LayoutPoint;
                    posPoint = layoutPoint.positiveController;
                    if (posPoint != null)
                    {
                        edgePosPoint = uiEdge.getPositiveControllerAt(p + 1);
                        if (edgePosPoint != null && (Math.floor(edgePosPoint.x) != Math.floor(posPoint.x) || Math.floor(edgePosPoint.y) != Math.floor(posPoint.y)))
                        {
                            uiEdge.movePositiveController(uiEdge.controlPoints[p], posPoint.x, posPoint.y);
                        }
                    }
                }*/

            } else
            {
                uiEdge.lineType = ToolTypes.LINK_STRAIGHT.name;

                /*
                var pEdges:Array = getParallelEdges(uiEdge);
                var pEdge:Edge;
                var angle:Number = getEdgeAngle(uiEdge);
                var spacing:Number = 0;
                for (p = 0; p < pEdges.length; p++)
                {
                    pEdge = pEdges[p];
                    if (angle == 0 || angle == 180)
                    {
                        //horizontal line
                        pEdge.uiEdge.moveStartPoint(pEdge.uiEdge.startPoint.x, pEdge.uiEdge.startPoint.y + (spacing * (p + 1)));
                        pEdge.uiEdge.moveEndPoint(pEdge.uiEdge.endPoint.x, pEdge.uiEdge.endPoint.y + (spacing * (p + 1)));
                    } else if (angle == 90 || angle == -90)
                    {
                        //vertical line
                        pEdge.uiEdge.moveStartPoint(pEdge.uiEdge.startPoint.x + (spacing * (p + 1)), pEdge.uiEdge.startPoint.y);
                        pEdge.uiEdge.moveEndPoint(pEdge.uiEdge.endPoint.x + (spacing * (p + 1)), pEdge.uiEdge.endPoint.y);
                    } else if ((angle > 90 && angle < 180) || (angle < -90 && angle > -180))
                    {
                        //left
                        pEdge.uiEdge.moveStartPoint(pEdge.uiEdge.startPoint.x + (spacing * (p + 1)), pEdge.uiEdge.startPoint.y);
                        pEdge.uiEdge.moveEndPoint(pEdge.uiEdge.endPoint.x + (spacing * (p + 1)), pEdge.uiEdge.endPoint.y);
                    } else
                    {
                        //assume it's right
                        pEdge.uiEdge.moveStartPoint(pEdge.uiEdge.startPoint.x - (spacing * (p + 1)), pEdge.uiEdge.startPoint.y);
                        pEdge.uiEdge.moveEndPoint(pEdge.uiEdge.endPoint.x - (spacing * (p + 1)), pEdge.uiEdge.endPoint.y);
                    }
                }
                */
            }


            if(_initialLayout) {
                uiEdge.uiEdgeLabel.move(edgeLayout.labelPoint.x + _nodeOffsetX, edgeLayout.labelPoint.y);
                DefaultUIEdge(uiEdge).adjustLabelCollision();
            } else {
                uiEdge.uiEdgeLabel.move(edgeLayout.labelPoint.x, edgeLayout.labelPoint.y);
            }

            _processedEdges.addItem(uiEdge);


        }
    }

    public function processMissingRecursiveEdges():void
    {
        var uiEdges:Vector.<UIEdge> = _diagram.getUIEdges();
        var i:int;
        var uiEdge:UIEdge;
        var isRecursive:Boolean;

        for(i=0;i<uiEdges.length; i++) {
            uiEdge = uiEdges[i];
            isRecursive = (uiEdge.edge.startNode.data.id == uiEdge.edge.endNode.data.id && uiEdge.controlPoints.length < 1);
            if(!_processedEdges.contains(uiEdge) && isRecursive) {
                uiEdge.lineType = ToolTypes.LINK_STRAIGHT.name;
                uiEdge.controlPoints = null;
                uiEdge.renderAsRecursive();
                UIComponent(uiEdge).validateNow();
            }
        }
    }

    private function getParallelEdges(edge:UIEdge):Array
    {
        var pEdges:Array = new Array();
        var sNode:Node = edge.edge.startNode;
        var eNode:Node = edge.edge.endNode;
        var inLinks:Vector.<Edge> = sNode.inLinks;
        var inEdge:Edge;
        var i:int;
        for (i = 0; i < inLinks.length; i++)
        {
            inEdge = inLinks[i];
            if (Point.distance(edge.startPoint, inEdge.uiEdge.endPoint) < 5)
            {
                if (Point.distance(edge.endPoint, inEdge.uiEdge.startPoint) < 5)
                {
                    pEdges.push(inEdge);
                }
            }
        }
        return pEdges;
    }

    private function getEdgeAngle(uiEdge:UIEdge):Number
    {

        var deltaX:Number = Math.round((uiEdge.endPoint.x - uiEdge.startPoint.x));
        var deltaY:Number = Math.round((uiEdge.endPoint.y - uiEdge.startPoint.y));
        var edgeAngle:Number = Math.atan2(deltaY, deltaX);

        return edgeAngle * 180 / Math.PI;
    }

    private function findMatchingDiagramNode(layoutNode:NodeLayout):UINode
    {

        var i:int;
        var uiNode:UINode;
        var uiNodes:Vector.<UINode> = _diagram.getUINodes();
        var workflowObject:FlexWorkflowObject;

        for (i = 0; i < uiNodes.length; i++)
        {
            uiNode = uiNodes[i] as UINode;
            workflowObject = uiNode.node.data as FlexWorkflowObject;


            if (layoutNode.isInitialAction && (workflowObject is FlexJiraAction))
            {
                break;
            } else if (!layoutNode.isInitialAction && (workflowObject is FlexJiraStep) && workflowObject.id == layoutNode.stepId)
            {
                break;
            } else
            {
                uiNode = null;
            }
        }
        return uiNode;
    }

    private function findMatchingDiagramEdge(layoutEdge:EdgeLayout):UIEdge
    {
        var i:int;
        var uiEdge:UIEdge;
        var uiEdges:Vector.<UIEdge> = _diagram.getUIEdges();
        for (i = 0; i < uiEdges.length; i++)
        {
            uiEdge = uiEdges[i] as UIEdge;
            var action:FlexJiraAction = uiEdge.edge.data as FlexJiraAction;
            if (action.id == layoutEdge.actionId && uiEdge.edge.startNode.data.id == layoutEdge.startStepId && uiEdge.edge.endNode.data.id == layoutEdge.endStepId)
            {
                break;
            } else
            {
                uiEdge = null;
            }
        }
        return uiEdge;
    }


}
}