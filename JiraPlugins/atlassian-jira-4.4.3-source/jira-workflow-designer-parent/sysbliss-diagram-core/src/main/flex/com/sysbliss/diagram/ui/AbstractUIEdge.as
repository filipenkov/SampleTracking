package com.sysbliss.diagram.ui {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.ToolTypes;
import com.sysbliss.diagram.data.DefaultEdgeLabel;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.diagram.data.EdgeLabel;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.diagram.event.InteractiveDiagramObjectEvent;
import com.sysbliss.diagram.geom.Line;
import com.sysbliss.diagram.manager.EdgeControlPointManager;
import com.sysbliss.diagram.renderer.AbstractEdgeRenderer;
import com.sysbliss.diagram.renderer.EdgeLabelRenderer;
import com.sysbliss.diagram.renderer.EdgeRenderer;
import com.sysbliss.diagram.ui.selectable.Selectable;
import com.sysbliss.diagram.ui.selectable.SelectionManager;
import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
import com.sysbliss.diagram.util.VectorUtils;
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
import com.sysbliss.util.AbstractClassEnforcer;
import com.sysbliss.util.PointUtils;

import flash.display.BitmapData;
import flash.display.IGraphicsData;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.geom.Rectangle;
import flash.ui.Keyboard;

import mx.core.UIComponent;
import mx.core.mx_internal;
import mx.events.FlexEvent;

use namespace mx_internal;

public class AbstractUIEdge extends AbstractDiagramUIObject implements UIEdge {
    protected var _line:Line;
    protected var _oldLineColor:Number;
    protected var _edge:Edge;
    protected var _knotRendererClass:Class;
    protected var _lineType:String;
    protected var _startPoint:Point;
    protected var _endPoint:Point;
    protected var _controlPoints:Vector.<Point>;

    protected var _edgeChanged:Boolean;
    protected var _startPointChanged:Boolean;
    protected var _startPointMoved:Boolean;
    protected var _endPointChanged:Boolean;
    protected var _endPointMoved:Boolean;
    protected var _endPointPreviewMoved:Boolean;
    protected var _lineTypeChanged:Boolean;
    protected var _controlPointsChanged:Boolean;
    protected var _controlPointMoved:Boolean;
    protected var _controlPointAdded:Boolean;
    protected var _controlPointInserted:Boolean;
    protected var _controlPointRemoved:Boolean;
    protected var _editedControlPointIndex:int;

    protected var _localStartPoint:Point;
    protected var _localEndPoint:Point;
    protected var _localControlPoints:Vector.<Point>;

    protected var _controlPointManager:EdgeControlPointManager;
    protected var _pointSelectionManager:SelectionManager;

    protected var _uiEdgeLabel:UIEdgeLabel = null;

    private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

    protected var _edgeLabelRenderer:EdgeLabelRenderer = null;

    public function AbstractUIEdge(diagram:Diagram, edge:Edge, edgeRenderer:EdgeRenderer, edgeLabelRendererClass:Class, knotRendererClass:Class, controlPointManager:EdgeControlPointManager) {
        super(diagram, edgeRenderer);
        AbstractClassEnforcer.enforceConstructor(this, AbstractUIEdge);
        this.moveDisabled = true;
        this._startPoint = new Point(0, 0);
        this._endPoint = new Point(0, 0);
        this._controlPoints = new Vector.<Point>();
        this._lineType = ToolTypes.LINK_STRAIGHT.name;

        this._edge = edge;
        this._knotRendererClass = knotRendererClass;
        this._selectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.DIAGRAM_OBJECTS + "_" + UIComponent(diagram).uid);
        this._controlPointManager = controlPointManager;
        this._pointSelectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.EDGE_CONTROLS + "_" + UIComponent(diagram).uid);


        if (edgeLabelRendererClass == null) {
            edgeLabelRendererClass = diagram.defaultEdgeLabelRenderer;
        }

        var edgeLabel:EdgeLabel = new DefaultEdgeLabel() as EdgeLabel;
        this._edgeLabelRenderer = new edgeLabelRendererClass(diagram);
        this._edgeLabelRenderer.edgeLabel = edgeLabel;
        this._edgeLabelRenderer.edge = edge;
    }

    override protected function init(event:FlexEvent):void {
        super.init(event);
        addEventListener(MouseEvent.CLICK, mouseClickHandler);
        addEventListener(MouseEvent.DOUBLE_CLICK, mouseDoubleClickHandler);
        addEventListener(MouseEvent.MOUSE_OVER, onMouseOver);
        addEventListener(MouseEvent.MOUSE_OUT, onMouseOut);
    }

    protected override function onMouseOver(event:MouseEvent):void {
        super.onMouseOver(event);
        if (!event.buttonDown && !isOnBoundingRect(event) && event.currentTarget == this) {
            highlightEdge();
            _edgeLabelRenderer.highlight();
        }
    }

    protected override function onMouseOut(event:MouseEvent):void {
        super.onMouseOut(event);
        if (!event.buttonDown) {
            unhighlightEdge();
            if (!isSelected) {
                _edgeLabelRenderer.unhighlight();
            }
        }


    }

    private function isOnBoundingRect(e:MouseEvent):Boolean {

        if(_line == null){
            return true;
        }
        var rect:Rectangle = _line.getBoundingRectangle();

        var startX:int = rect.x + 1;
        var endX:int = (rect.x + 1) + rect.width;

        var startY:int = rect.y;
        var endY:int = rect.y + rect.height;

        var bingo:Boolean = (e.localX == startX || e.localX == endX || e.localY == startY || e.localY == endY);

        return bingo;

    }

    public function createLabel():void {
        if (_uiEdgeLabel == null) {
            this._uiEdgeLabel = new DefaultUIEdgeLabel(diagram, edgeLabelRenderer);

            _uiEdgeLabel.addEventListener(MouseEvent.MOUSE_DOWN, edgeLabelMouseDown);
            _uiEdgeLabel.addEventListener(MouseEvent.MOUSE_UP, edgeLabelMouseUp);

            _uiEdgeLabel.move(-200, 0);
        }
        updateLabel();
    }


    public function updateLabel():void {
        if (_edge != null && _edge.data != null) {
            if (edge.data.hasOwnProperty("label") && edge.data.label != "") {
                _uiEdgeLabel.edgeLabel.text = edge.data.label;
            } else if (edge.data.hasOwnProperty("name") && edge.data.name != "") {
                _uiEdgeLabel.edgeLabel.text = edge.data.name;
            } else {
                _uiEdgeLabel.edgeLabel.text = niceResourceManager.getString('json', 'workflow.designer.unknown.label');
            }
        }

        UIComponent(_uiEdgeLabel).validateNow();

        updateLabelPosition();
    }

    public function updateLabelPosition():void {
        AbstractClassEnforcer.enforceMethod("updateLabelPosition");
    }

    private function edgeLabelMouseDown(e:MouseEvent):void {
        UIComponent(_uiEdgeLabel).startDrag();
    }

    private function edgeLabelMouseUp(e:MouseEvent):void {
        UIComponent(_uiEdgeLabel).stopDrag();
    }

    public function highlightEdge():void {
        /* var objs:Array = DisplayObjectContainer(_diagram).getObjectsUnderPoint(new Point(stage.mouseX, stage.mouseY));
         var i:int;
         trace("objects under mouse");
         for(i=0;i<objs.length;i++) {
         trace("obj = " + objs[i]);
         }*/

        var edgeRenderer:EdgeRenderer = this.renderer as EdgeRenderer;
        edgeRenderer.lineColor = AbstractEdgeRenderer.HIGHLIGHT_COLOR;
        this.invalidateDisplayList();
        this.validateNow();
    }

    public function unhighlightEdge():void {
        var edgeRenderer:EdgeRenderer = this.renderer as EdgeRenderer;
        edgeRenderer.lineColor = AbstractEdgeRenderer.DEFAULT_COLOR;
        this.invalidateDisplayList();
        this.validateNow();
    }


    protected function mouseClickHandler(e:MouseEvent):void {
        var evt:DiagramEvent = new DiagramEvent(DiagramEvent.EDGE_CLICK, {edge:_edge,localX:e.localX,localY:e.localY,x:e.stageX,y:e.stageY});
        dispatchEvent(evt);
        if (isSelected && _selectionManager.numSelected == 1 && e.ctrlKey) {
            var localPoint:Point = new Point(e.localX, e.localY);
            var diagramPoint:Point = PointUtils.convertCoordinates(localPoint, this, _diagram.edgeLayer);
            insertControlPoint(diagramPoint);
        }

    }

    override protected function onKeyDown(e:KeyboardEvent):void {
        if (e.keyCode == Keyboard.DELETE) {
            e.stopPropagation();
            handleDeletePress();
        } else if (e.keyCode == Keyboard.ENTER) {
            var evt:DiagramEvent = new DiagramEvent(DiagramEvent.EDGE_ENTER_KEY, {diagram:_diagram,edge:_edge});
            dispatchEvent(evt);
        } else {
            super.onKeyDown(e);
        }
    }

    protected function mouseDoubleClickHandler(e:MouseEvent):void {
        if (!selectable) {
            return;
        }
        var evt:DiagramEvent = new DiagramEvent(DiagramEvent.EDGE_DOUBLE_CLICK, {diagram:_diagram,edge:_edge,localX:e.localX,localY:e.localY,x:e.stageX,y:e.stageY});
        dispatchEvent(evt);
    }

    override protected function handleDeletePress():void {
        if (isSelected) {
            if (_controlPointManager.currentUIEdge === this && _pointSelectionManager.numSelected > 0) {
                removeSelectedControlPoints();
            } else {
                dispatchEvent(new InteractiveDiagramObjectEvent(InteractiveDiagramObjectEvent.OBJECT_DELETE, false, true));
            }
        }
    }

    public function removeSelectedControlPoints():void {
        if (_pointSelectionManager.numSelected > 0) {
            var points:Vector.<Selectable> = _pointSelectionManager.currentlySelected;
            var i:int;
            var uiPoint:UIControlPoint;
            for (i = 0; i < points.length; i++) {
                uiPoint = points[i] as UIControlPoint;
                removeControlPoint(uiPoint.point);
            }
            validateNow();
            _controlPointManager.hideControls();
            _controlPointManager.showControls(this);
        }
    }

    override public function select(quiet:Boolean = false):void {
        super.select(quiet);
        _edgeLabelRenderer.highlight();
        invalidateDisplayList();
    }

    override public function deselect(quiet:Boolean = false):void {
        super.deselect(quiet);
        _edgeLabelRenderer.unhighlight();
        invalidateDisplayList();
    }

    protected function resetPropertyFlags():void {
        _edgeChanged = false;
        _lineTypeChanged = false;
        _startPointChanged = false;
        _endPointChanged = false;
        _controlPointsChanged = false;
        _startPointMoved = false;
        _endPointMoved = false;
        _endPointPreviewMoved = false;
        _controlPointMoved = false;
        _controlPointAdded = false;
        _controlPointInserted = false;
        _controlPointRemoved = false;
    }

    protected function propChanged():Boolean {
        return(_edgeChanged
                || _lineTypeChanged
                || _startPointChanged
                || _endPointChanged
                || _controlPointsChanged
                || _startPointMoved
                || _endPointMoved
                || _endPointPreviewMoved
                || _controlPointMoved
                || _controlPointAdded
                || _controlPointInserted
                || _controlPointRemoved);
    }

    public function set startPoint(p:Point):void {
        _startPointChanged = true;
        _startPoint = p;
        invalidateProperties();
    }

    public function get startPoint():Point {
        return _startPoint;
    }

    public function moveStartPoint(newX:Number, newY:Number):void {
        _startPointMoved = true;
        _startPoint.x = newX;
        _startPoint.y = newY;
        invalidateProperties();
    }

    public function set endPoint(p:Point):void {
        _endPointChanged = true;
        _endPoint = p;
        invalidateProperties();
    }

    public function get endPoint():Point {
        return _endPoint;
    }

    public function moveEndPoint(newX:Number, newY:Number):void {
        _endPointMoved = true;
        _endPoint.x = newX;
        _endPoint.y = newY;
        invalidateProperties();
    }

    public function moveEndPointPreview(newX:Number, newY:Number):void {
        _endPointPreviewMoved = true;
        _endPoint.x = newX;
        _endPoint.y = newY;
        invalidateProperties();
    }

    public function addControlPoint(p:Point):void {
        _controlPointAdded = true;
        _controlPoints.push(p);
        invalidateProperties();
    }

    public function pushControlPoint(p:Point):void {
        _controlPoints.push(p);
    }

    public function insertControlPoint(p:Point):void {
        var index:int = findInsertionIndexForPoint(p);
        if (index > -1) {
            insertControlPointAt(p, index);
        }
    }

    public function insertControlPointAt(p:Point, i:int):void {
        if (i > -1) {
            _controlPoints.splice(i, 0, p);
            _controlPointInserted = true;
            _editedControlPointIndex = i;
            invalidateProperties();
        }
    }

    public function removeControlPointAt(i:int):void {
        _controlPoints.splice(i, 1);
        _controlPointRemoved = true;
        _editedControlPointIndex = i;
        invalidateProperties();
    }

    public function removeControlPoint(p:Point):void {
        var i:int = _controlPoints.indexOf(p);
        if (i > -1) {
            removeControlPointAt(i);
        }
    }

    public function moveControlPoint(p:Point, newX:Number, newY:Number):void {
        var foundIndex:int = _controlPoints.indexOf(p);
        if (foundIndex > -1) {
            _controlPointMoved = true;
            p.x = newX;
            p.y = newY;

            _editedControlPointIndex = foundIndex;
            invalidateProperties();
        }
    }

    public function moveControlPointAt(i:int, newX:Number, newY:Number):void {
        var p:Point = _controlPoints[i];
        if (p) {
            _controlPointMoved = true;
            p.x = newX;
            p.y = newY;

            _editedControlPointIndex = i;
            invalidateProperties();
        }
    }

    public function set controlPoints(v:Vector.<Point>):void {
        _controlPointsChanged = true;
        if (v == null) {
            _controlPoints = null;
        } else {
            _controlPoints = v;
        }

        if (_controlPoints == null) {
            _controlPoints = new Vector.<Point>();
        }
        invalidateProperties();
    }

    public function get controlPoints():Vector.<Point> {
        return _controlPoints;
    }

    public function getPositiveControllerForPoint(p:Point):Point {
        return new Point(0, 0);
    }

    public function getNegativeControllerForPoint(p:Point):Point {
        return new Point(0, 0);
    }

    public function getPositiveControllerAt(i:int):Point {
        return new Point(0, 0);
    }

    public function getNegativeControllerAt(i:int):Point {
        return new Point(0, 0);
    }

    public function movePositiveController(p:Point, newX:Number, newY:Number):void {
    }

    public function moveNegativeController(p:Point, newX:Number, newY:Number):void {
    }

    public function movePositiveControllerAt(i:int, newX:Number, newY:Number):void {
    }

    public function moveNegativeControllerAt(i:int, newX:Number, newY:Number):void {
    }

    public function getBoundingRectangle():Rectangle {
        return new Rectangle(0, 0, 0, 0);
    }

    public function set lineType(type:String):void {
        _lineTypeChanged = true;
        _lineType = type;
        invalidateProperties();
    }

    public function get lineType():String {
        return _lineType;
    }

    public function get edgeRenderer():EdgeRenderer {
        return renderer as EdgeRenderer;
    }

    public function get knotRendererClass():Class {
        return _knotRendererClass;
    }

    public function get edge():Edge {
        return _edge;
    }

    public function getGraphicsData():Vector.<IGraphicsData> {
        AbstractClassEnforcer.enforceMethod("getGraphicsData");
        return null;
    }

    protected function convertPointsToLocal():void {
        convertStartPointToLocal();
        convertControlPointsToLocal();
        convertEndPointToLocal();
    }

    protected function convertEndPointToLocal():void {
        var localPoint:Point;
        if (_localEndPoint == null) {
            this._localEndPoint = PointUtils.convertCoordinates(_endPoint, diagram.edgeLayer, this);
        } else {
            localPoint = PointUtils.convertCoordinates(_endPoint, diagram.edgeLayer, this);
            _localEndPoint.x = localPoint.x;
            _localEndPoint.y = localPoint.y;
        }
    }

    protected function convertStartPointToLocal():void {
        var localPoint:Point;
        if (_localStartPoint == null) {
            this._localStartPoint = PointUtils.convertCoordinates(_startPoint, diagram.edgeLayer, this);
        } else {
            localPoint = PointUtils.convertCoordinates(_startPoint, diagram.edgeLayer, this);
            _localStartPoint.x = localPoint.x;
            _localStartPoint.y = localPoint.y;
        }
    }

    protected function convertControlPointsToLocal():void {
        if (_localControlPoints == null || _localControlPoints.length != _controlPoints.length) {
            this._localControlPoints = VectorUtils.mapPointsToLocal(_controlPoints, diagram.edgeLayer, this);
        } else {
            VectorUtils.updatePointsToLocal(_controlPoints, _localControlPoints, diagram.edgeLayer, this);
        }
    }

    public function findInsertionIndexForPoint(p:Point):int {
        AbstractClassEnforcer.enforceMethod("findInsertionIndexForPoint");
        return -1;
    }

    public function get includeSegmentBoundries():Boolean {
        AbstractClassEnforcer.enforceMethod("get includeSegmentBoundries");
        return false;
    }

    public function set includeSegmentBoundries(b:Boolean):void {
        AbstractClassEnforcer.enforceMethod("set includeSegmentBoundries");
    }


    public function get uiEdgeLabel():UIEdgeLabel {
        return _uiEdgeLabel;
    }

    public function set uiEdgeLabel(uiEdgeLabel:UIEdgeLabel):void {
        this._uiEdgeLabel = uiEdgeLabel;
    }

    public function renderAsRecursive():void {
        if ((edge.startNode.id == edge.endNode.id)) {
            endPoint.x = startPoint.x;
            endPoint.y = startPoint.y;
            lineType = ToolTypes.LINK_POLY.name;
            controlPoints = null;

            var nodePoint:Point = new Point(edge.startNode.uiNode.x, edge.startNode.uiNode.y);
            nodePoint = diagram.nodeLayer.localToGlobal(nodePoint);
            nodePoint = diagram.edgeLayer.globalToLocal(nodePoint);

            var p1:Point = new Point(startPoint.x - ((startPoint.x - nodePoint.x) + 15), startPoint.y);
            var p2:Point = new Point(p1.x, ((p1.y + edge.startNode.uiNode.height) + 15));
            var p3:Point = new Point(endPoint.x, p2.y);

            //p1 = diagram.edgeLayer.globalToLocal(p1);
            //p2 = diagram.edgeLayer.globalToLocal(p2);
            //p3 = diagram.edgeLayer.globalToLocal(p3);

            pushControlPoint(p1);
            pushControlPoint(p2);
            pushControlPoint(p3);
        }
    }

    public function get edgeLabelRenderer():EdgeLabelRenderer {
        return _edgeLabelRenderer;
    }
}
}