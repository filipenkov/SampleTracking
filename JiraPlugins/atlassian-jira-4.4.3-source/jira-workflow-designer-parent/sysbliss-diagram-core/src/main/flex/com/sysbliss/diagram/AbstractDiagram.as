package com.sysbliss.diagram {
import com.sysbliss.collections.HashMap;
import com.sysbliss.diagram.data.Annotation;
import com.sysbliss.diagram.data.DefaultEdge;
import com.sysbliss.diagram.data.DefaultNode;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.diagram.data.HiddenRootNodeImpl;
import com.sysbliss.diagram.data.Node;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.diagram.manager.ClassInstanceManager;
import com.sysbliss.diagram.manager.EdgeControlPointManager;
import com.sysbliss.diagram.renderer.DefaultEdgeLabelRenderer;
import com.sysbliss.diagram.renderer.DefaultKnotControllerRenderer;
import com.sysbliss.diagram.renderer.DefaultKnotRenderer;
import com.sysbliss.diagram.renderer.DefaultNodeRenderer;
import com.sysbliss.diagram.renderer.DirectedEdgeRenderer;
import com.sysbliss.diagram.renderer.EdgeRenderer;
import com.sysbliss.diagram.renderer.NodeRenderer;
import com.sysbliss.diagram.tools.DiagramLinkTool;
import com.sysbliss.diagram.tools.DiagramSelectTool;
import com.sysbliss.diagram.tools.DiagramTool;
import com.sysbliss.diagram.tools.DiagramZoomInTool;
import com.sysbliss.diagram.tools.DiagramZoomOutTool;
import com.sysbliss.diagram.ui.DefaultUIEdge;
import com.sysbliss.diagram.ui.DefaultUINode;
import com.sysbliss.diagram.ui.DiagramUIObject;
import com.sysbliss.diagram.ui.InteractiveDiagramObject;
import com.sysbliss.diagram.ui.StickyNote;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.diagram.ui.UIEdgeLabel;
import com.sysbliss.diagram.ui.UIEdgePreviewImpl;
import com.sysbliss.diagram.ui.UINode;
import com.sysbliss.diagram.ui.selectable.Selectable;
import com.sysbliss.diagram.ui.selectable.SelectionManager;
import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
import com.sysbliss.diagram.util.CursorUtil;
import com.sysbliss.diagram.util.VectorUtils;

import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.events.TimerEvent;
import flash.geom.Matrix;
import flash.geom.Point;
import flash.geom.Rectangle;
import flash.ui.Keyboard;
import flash.utils.Timer;

import flexlib.containers.DragScrollingCanvas;
import flexlib.mdi.containers.MDIWindow;
import flexlib.mdi.events.MDIWindowEvent;
import flexlib.mdi.managers.MDIManager;

import mx.collections.ArrayCollection;
import mx.controls.scrollClasses.ScrollBar;
import mx.core.EventPriority;
import mx.core.ScrollPolicy;
import mx.core.UIComponent;
import mx.core.mx_internal;
import mx.effects.Zoom;
import mx.events.DragEvent;
import mx.events.FlexEvent;
import mx.managers.DragManager;
import mx.utils.UIDUtil;

[Event(name="nodeClick", type="com.sysbliss.diagram.event.DiagramEvent")]
[Event(name="nodeDoubleClick", type="com.sysbliss.diagram.event.DiagramEvent")]
[Event(name="edgeClick", type="com.sysbliss.diagram.event.DiagramEvent")]
[Event(name="edgeDoubleClick", type="com.sysbliss.diagram.event.DiagramEvent")]
[Event(name="selectionsDeleted", type="com.sysbliss.diagram.event.DiagramEvent")]

public class AbstractDiagram extends DragScrollingCanvas implements Diagram {
    public var zoomFactor:Number = .1;
    protected var _nodes:HashMap;
    protected var _uinodes:HashMap;
    protected var _edges:HashMap;
    protected var _uiedges:HashMap;
    protected var _currentTool:DiagramTool;
    protected var _currentLineType:String;
    protected var _isLinking:Boolean;
    protected var _isPanning:Boolean;
    protected var _edgePreview:UIEdgePreviewImpl;
    protected var _edgePreviewRendererClass:Class;
    protected var _selectionManager:SelectionManager;
    protected var _defaultEdgeRenderer:Class;
    protected var _defaultEdgeLabelRenderer:Class;
    protected var _defaultKnotRenderer:Class;
    protected var _defaultKnotControllerRenderer:Class;
    protected var _layout:ArrayCollection;

    private var _edgeHandleManager:EdgeControlPointManager;
    private var _annotationLayer:DiagramLayer;
    private var _nodeLayer:DiagramLayer;
    private var _edgeLayer:DiagramLayer;
    private var _controlsLayer:DiagramLayer;
    private var _contentLayer:DiagramLayer;
    private var _labelLayer:DiagramLayer;
    private var _hiddenUIRootNode:UINode;
    private var _id:String;
    private var _cursorId:int = -1;

    private var annotationWindowManager:MDIManager;

    private var regX:Number;
    private var regY:Number;
    private var regHScrollPosition:Number;
    private var regVScrollPosition:Number;

    private var sketch:UIComponent;
    private var modalBlocker:ModalLayer;
    private var scrollTimer:Timer;
    private var currentDragNode:DisplayObject;

    use namespace mx.core.mx_internal;

    public function AbstractDiagram() {
        this._id = UIDUtil.createUID();
        this._nodes = new HashMap();
        this._uinodes = new HashMap();
        this._edges = new HashMap();
        this._uiedges = new HashMap();
        this._isLinking = false;
        this._isPanning = false;
        this._edgePreviewRendererClass = DirectedEdgeRenderer;
        this._currentLineType = ToolTypes.LINK_STRAIGHT.name;
        this._defaultEdgeRenderer = DirectedEdgeRenderer;
        this._defaultEdgeLabelRenderer = DefaultEdgeLabelRenderer;
        this._defaultKnotRenderer = DefaultKnotRenderer;
        this._defaultKnotControllerRenderer = DefaultKnotControllerRenderer;
        this._selectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.DIAGRAM_OBJECTS + "_" + this.uid);
        this._edgeHandleManager = new EdgeControlPointManager(this, _selectionManager);
        this._currentTool = ToolTypes.TOOL_SELECT;
        this.scrollTimer = new Timer(10, 0);
        scrollTimer.addEventListener(TimerEvent.TIMER, onScrollTimer);

        addEventListener(MouseEvent.MOUSE_UP, mouseUpHandler);
        addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
        addEventListener(MouseEvent.MOUSE_OVER, mouseOverHandler);
        addEventListener(DiagramEvent.SELECTIONS_DELETED, defaultSelectionDeleteHandler, false, EventPriority.DEFAULT_HANDLER, true);
        addEventListener(DiagramEvent.NODE_DELETED, defaultNodeDeleteHandler, false, EventPriority.DEFAULT_HANDLER, true);
        addEventListener(DiagramEvent.EDGE_DELETED, defaultEdgeDeleteHandler, false, EventPriority.DEFAULT_HANDLER, true);
        addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
        addEventListener(DragEvent.DRAG_ENTER, dragEnterHandler, false, EventPriority.DEFAULT_HANDLER);
        addEventListener(DragEvent.DRAG_DROP, dragDropHandler, false, EventPriority.DEFAULT_HANDLER);

        this.horizontalScrollPolicy = ScrollPolicy.ON;
        this.verticalScrollPolicy = ScrollPolicy.ON;

        this.addEventListener(FlexEvent.CREATION_COMPLETE, initScrollPosition, false, 10000);

        this.childrenDoDrag = false;

        this.autoLayout = true;

        this.percentWidth = 100;
        this.percentHeight = 100;
    }


    override protected function createChildren():void {
        super.createChildren();

        //set to true for debugging ONLY
        var layerColors:Boolean = false;
        var layerScrolls:Boolean = false;

        this._annotationLayer = new DiagramLayer("AnnotationLayer");

        this._edgeLayer = new DiagramLayer("EdgeLayer");

        this._nodeLayer = new DiagramLayer("NodeLayer");

        this._controlsLayer = new DiagramLayer("ControlsLayer");

        this._labelLayer = new DiagramLayer("LabelLayer");

        this._contentLayer = new DiagramLayer("ContentLayer");


        _contentLayer.horizontalScrollPolicy = ScrollPolicy.AUTO;
        _contentLayer.verticalScrollPolicy = ScrollPolicy.AUTO;


        _contentLayer.setStyle("backgroundColor", 0xffffff);

        if (layerColors) {
            //red
            _contentLayer.setStyle("backgroundColor", 0xff0000);

            //seafoam green
            //_edgeLayer.setStyle("backgroundColor", 0x66ffcc);

            //mustard
           // _nodeLayer.setStyle("backgroundColor", 0xffcc33);
            //_controlsLayer.setStyle("backgroundColor", 0xffccff);
            //_labelLayer.setStyle("backgroundColor", 0x9966ff);
        }
        _contentLayer.mouseEnabled = true;
        addChild(_contentLayer);

        _contentLayer.addChild(_edgeLayer);
        _contentLayer.addChild(_nodeLayer);
        _contentLayer.addChild(_labelLayer);
        _contentLayer.addChild(_controlsLayer);
        _contentLayer.addChild(_annotationLayer);

        annotationWindowManager = new MDIManager(_annotationLayer);

        var hr:NodeRenderer = new DefaultNodeRenderer(this);
        hr.node = new HiddenRootNodeImpl({label:"hidden root"});

        _hiddenUIRootNode = new DefaultUINode(this, hr);
        _hiddenUIRootNode.visible = false;

        _nodeLayer.addChild(DisplayObject(_hiddenUIRootNode));

    }

    private function initScrollPosition(e:FlexEvent):void {
        this.removeEventListener(FlexEvent.CREATION_COMPLETE, initScrollPosition);
        horizontalScrollPosition = (maxHorizontalScrollPosition / 2);
        //verticalScrollPosition = (maxVerticalScrollPosition/2);
        var i:int;
        var uiEdge:UIEdge;
        for (i = 0; i < _uiedges.getKeys().length; i++) {
            uiEdge = _uiedges.getValue(_uiedges.getKeys()[i]);
            UIComponent(uiEdge).invalidateProperties();
            UIComponent(uiEdge).validateNow();
        }
    }

    public function createNode(rendererClass:Class = null, data:Object = null, location:Point = null):Node {
        var node:Node = createNodeQuietly(rendererClass, data, location);
        dispatchEvent(new DiagramEvent(DiagramEvent.NODE_CREATED, {diagram:this,node:node}, false, true));
        return node;
    }

    public function createNodeQuietly(rendererClass:Class = null, data:Object = null, location:Point = null):Node {
        var node:Node = new DefaultNode(data) as Node;

        if (!rendererClass) {
            rendererClass = DefaultNodeRenderer;
        }
        var nodeRenderer:NodeRenderer = new rendererClass(this);

        nodeRenderer.node = node;
        var nodeUI:UINode = new DefaultUINode(this, nodeRenderer);
        _nodeLayer.addChild(DisplayObject(nodeUI));

        UIComponent(nodeUI).doubleClickEnabled = true;
        InteractiveDiagramObject(nodeUI).dragBounds = new Rectangle(20, 20, _contentLayer.width - 20, _contentLayer.height - 20);

        nodeUI.addEventListener(DiagramEvent.NODE_CLICK, onNodeClick);
        nodeUI.addEventListener(MouseEvent.MOUSE_DOWN, onNodeMouseDown);
        nodeUI.addEventListener(DiagramEvent.NODE_DOUBLE_CLICK, onNodeDoubleClick);
        nodeUI.addEventListener(DiagramEvent.NODE_ENTER_KEY, onNodeEnterKey);


        var nodePosition:Point = new Point(0, 0);
        nodePosition.x = 700;
        nodePosition.y = 10;
        if (location != null) {
            nodePosition = _nodeLayer.globalToLocal(location);
        }

        nodeUI.move(nodePosition.x, nodePosition.y);

        UIComponent(nodeUI).validateNow();

        _uinodes.put(node.id, nodeUI);
        _nodes.put(node.id, node);

        return node;
    }


    public function createLink(startNode:Node, endNode:Node, edgeRendererClass:Class = null, edgeLabelRendererClass:Class = null, data:Object = null, lineType:String = "", startPoint:Point = null, endPoint:Point = null, controlPoints:Vector.<Point> = null, knotRendererClass:Class = null):Edge {
        var edge:Edge = createLinkQuietly(startNode, endNode, edgeRendererClass, edgeLabelRendererClass, data, lineType, startPoint, endPoint, controlPoints, knotRendererClass);
        dispatchEvent(new DiagramEvent(DiagramEvent.EDGE_CREATED, {diagram:this,edge:edge}, false, true));
        return edge;
    }

    public function createLinkQuietly(startNode:Node, endNode:Node, edgeRendererClass:Class = null, edgeLabelRendererClass:Class = null, data:Object = null, lineType:String = "", startPoint:Point = null, endPoint:Point = null, controlPoints:Vector.<Point> = null, knotRendererClass:Class = null):Edge {
        if (edgeRendererClass == null) {
            edgeRendererClass = _defaultEdgeRenderer;
        }

        if (knotRendererClass == null) {
            knotRendererClass = _defaultKnotRenderer;
        }

        var edge:Edge = new DefaultEdge(startNode, endNode, data) as Edge;
        var edgeRenderer:EdgeRenderer = ClassInstanceManager.getClassInstance(edgeRendererClass) as EdgeRenderer;

        var edgeUI:UIEdge = new DefaultUIEdge(this, edge, edgeRenderer, edgeLabelRendererClass, knotRendererClass, _edgeHandleManager);
        edge.uiEdge = edgeUI;
        UIComponent(edgeUI).doubleClickEnabled = true;
        _edgeLayer.addChild(DisplayObject(edgeUI));

        _labelLayer.addChild(DisplayObject(edgeUI.uiEdgeLabel));

        edgeUI.addEventListener(DiagramEvent.EDGE_LABEL_CLICK, onEdgeLabelClick);
        //edgeUI.addEventListener(ObjectHandleEvent_old.OBJECT_MOVED_EVENT, onEdgeLabelMove);
        //edgeUI.addEventListener(ObjectHandleEvent_old.OBJECT_MOVING_EVENT, onEdgeLabelMove);
        edgeUI.addEventListener(DiagramEvent.EDGE_LABEL_DOUBLE_CLICK, onEdgeLabelDoubleClick);
        edgeUI.addEventListener(DiagramEvent.EDGE_LABEL_ENTER_KEY, onEdgeLabelEnterKey);

        edgeUI.addEventListener(DiagramEvent.EDGE_DOUBLE_CLICK, onEdgeDoubleClick);
        edgeUI.addEventListener(DiagramEvent.EDGE_ENTER_KEY, onEdgeEnterKey);

        var sPoint:Point;
        var ePoint:Point;
        var cPoints:Vector.<Point>;

        if (startPoint) {
            sPoint = _edgeLayer.globalToLocal(startPoint);
        }
        if (endPoint) {
            ePoint = _edgeLayer.globalToLocal(endPoint);
        }

        if (controlPoints) {
            cPoints = VectorUtils.mapGlobalToLocal(controlPoints, this._edgeLayer);
        } else {
            cPoints = new Vector.<Point>();
        }

        if (sPoint == null) {
            var sui:UINode = UINode(_uinodes.getValue(startNode.id));
            sPoint = new Point(sui.centerPoint.x, sui.centerPoint.y);
            //sPoint = PointUtils.convertnew Point(sui.x,sui.y);
            //sPoint = globalToLocal(UINode(_uinodes.getValue(startNode.id)).centerPoint);
        }
        if (ePoint == null) {
            var eui:UINode = UINode(_uinodes.getValue(endNode.id));
            ePoint = new Point(eui.centerPoint.x, eui.centerPoint.y);
            //ePoint = globalToLocal(UINode(_uinodes.getValue(endNode.id)).centerPoint);
        }


        edgeUI.startPoint = sPoint;
        edgeUI.endPoint = ePoint;
        edgeUI.controlPoints = cPoints;


        if (lineType == "") {
            lineType = ToolTypes.LINK_STRAIGHT.name;
        }
        edgeUI.lineType = lineType;

        if (startNode.id == endNode.id) {
            edgeUI.renderAsRecursive();
        }

        UIComponent(edgeUI).invalidateDisplayList();
        UIComponent(edgeUI).validateNow();

        _uiedges.put(edge.id, edgeUI);
        _edges.put(edge.id, edge);

        if ((currentTool is DiagramLinkTool)) {
            //need to disable selection for now
            edgeUI.selectable = false;
        }

        return edge;
    }

    public function passThruKeyHandler(e:KeyboardEvent):void {
        keyDownHandler(e);
    }

    override protected function keyDownHandler(e:KeyboardEvent):void {
        //trace("keyDown:" + String.fromCharCode(e.charCode).toLowerCase());
        super.keyDownHandler(e);
        var chararcter:String = String.fromCharCode(e.charCode).toLowerCase();

        if ("a" == chararcter && e.ctrlKey) {
            _selectionManager.selectAll();
        } else if (Keyboard.ESCAPE == e.charCode && _isLinking) {
            cancelLink();
        }
    }

    public function cancelLink():void {
        _isLinking = false;
        if (_edgePreview) {
            _edgePreview.completeLink(null);
            removeEdgePreview();
        }

    }

    protected function onNodeDoubleClick(e:DiagramEvent):void {
        var selectedNode:UINode = _selectionManager.currentlySelected[0] as UINode;
        if (_selectionManager.numSelected < 2 && selectedNode) {
            e.data.node = selectedNode.node;
            dispatchEvent(e);
        }
    }

    protected function onNodeEnterKey(e:DiagramEvent):void {
        var selectedNode:UINode = _selectionManager.currentlySelected[0] as UINode;
        if (_selectionManager.numSelected < 2 && selectedNode) {
            e.data.node = selectedNode.node;
            dispatchEvent(e);
        }
    }

    protected function onEdgeDoubleClick(e:DiagramEvent):void {
        var selectedEdge:UIEdge = _selectionManager.currentlySelected[0] as UIEdge;
        if (_selectionManager.numSelected < 2 && selectedEdge) {
            e.data.edge = selectedEdge.edge;
            dispatchEvent(e);
        }
    }

    protected function onEdgeEnterKey(e:DiagramEvent):void {
        var selectedEdge:UIEdge = _selectionManager.currentlySelected[0] as UIEdge;
        if (_selectionManager.numSelected < 2 && selectedEdge) {
            e.data.edge = selectedEdge.edge;
            dispatchEvent(e);
        }
    }


    public function startScrollTimer():void {
        if (!scrollTimer.running) {
            scrollTimer.start();
        }

    }

    public function stopScrollTimer():void {
        if (scrollTimer.running) {
            scrollTimer.stop();
        }

    }

    public function onScrollTimer(e:TimerEvent):void {
        adjustScrollbarsForNode(currentDragNode);
    }

    protected function adjustScrollbarsForNode(dobj:DisplayObject):void {

        var offY:Number = 0;
        var offX:Number = 0;
        var pad:Number = 20;
        var amount:int = 15;

        var scaledMouseX:Number = (_contentLayer.mouseX * _contentLayer.scaleX);
        var scaledMouseY:Number = (_contentLayer.mouseY * _contentLayer.scaleY);

        var visRight:Number = (visibleRectangle.x + visibleRectangle.width);
        var visBottom:Number = (visibleRectangle.y + visibleRectangle.height);

        if (scaledMouseX < visibleRectangle.x + pad) {
            offX = -amount;
        } else if (scaledMouseX > (visRight - pad)) {
            offX = amount;
        }

        if ((scaledMouseY < visibleRectangle.y + pad) && horizontalScrollPosition > 5) {
            offY = -amount;
        } else if (scaledMouseY > (visBottom - pad)) {
            offY = amount;
        }

        if (dobj && dobj != null) {
            if (dobj is InteractiveDiagramObject) {
                InteractiveDiagramObject(dobj).nudge(new Point(offX, offY));
            } else if(dobj is MDIWindow) {

                var pos:Point = new Point(dobj.x, dobj.y);
                pos.x = dobj.x + offX;
                pos.y = dobj.y + offY;

                var newX:int = dobj.x;
                var newY:int = dobj.y;
                if((pos.x + dobj.width) < _annotationLayer.width && pos.x > _annotationLayer.x){
                    newX = pos.x;
                }
                if((pos.y + dobj.height) < _annotationLayer.height && pos.y > _annotationLayer.y){
                    newY = pos.y;
                }

                annotationWindowManager.absPos(MDIWindow(dobj), newX,  newY);

            }
        }


        horizontalScrollPosition += offX;
        verticalScrollPosition += offY;
    }

    public function scrollToPoint(p:Point):void {
        var offY:Number = 0;
        var offX:Number = 0;
        var pad:Number = 1;
        if (p.x <= visibleRectangle.x + pad) {
            offX = (p.x - visibleRectangle.x) - pad;
        } else if (p.x > (visibleRectangle.x + visibleRectangle.width) - pad) {
            offX = (p.x - (visibleRectangle.x + visibleRectangle.width)) + pad;
        }

        if (p.y <= visibleRectangle.y + pad) {
            offY = (p.y - visibleRectangle.y) - pad;
        } else if (p.y > (visibleRectangle.y + visibleRectangle.height) - pad) {
            offY = (p.y - (visibleRectangle.y + visibleRectangle.height)) + pad;
        }
        horizontalScrollPosition += offX / 4;
        verticalScrollPosition += offY / 4;
    }


    protected function onNodeClick(e:DiagramEvent):void {
        e.stopPropagation();
        if ((currentTool is DiagramLinkTool)) {
            if (!_isLinking) {
                _isLinking = true;
                startLinking(e.data.node, _edgeLayer.globalToLocal(new Point(e.data.x, e.data.y)));
            } else {
                _isLinking = _edgePreview.completeLink(e.data.node);
                if (!_isLinking) {
                    //createLink expects global coord points, so we have to convert them back
                    var startPoint:Point = this._edgeLayer.localToGlobal(_edgePreview.startPoint);
                    var endPoint:Point = this._edgeLayer.localToGlobal(_edgePreview.endPoint);
                    var controlPoints:Vector.<Point> = VectorUtils.mapLocalToGlobal(_edgePreview.controlPoints, this._edgeLayer);
                    createLink(_edgePreview.startNode, _edgePreview.endNode, null, null, null, _currentLineType, startPoint, endPoint, controlPoints, null);
                    removeEdgePreview();
                }
            }
        }
    }

    protected function onNodeMouseDown(e:MouseEvent):void {
        if (!_isLinking && !(currentTool is DiagramLinkTool)) {
            CursorUtil.showClosedHand();
            stage.addEventListener(MouseEvent.MOUSE_MOVE, onNodeMouseMove);
            stage.addEventListener(MouseEvent.MOUSE_UP, onNodeMouseUp);
            currentDragNode = e.currentTarget as DisplayObject;

        }

    }

    protected function onNodeMouseMove(e:MouseEvent):void {
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onNodeMouseMove);

        startScrollTimer();
    }

    protected function startAnnotationDrag(e:MDIWindowEvent) {
        e.window.addEventListener(MDIWindowEvent.DRAG_END, endAnnotationDrag);
        currentDragNode = e.window;
        startScrollTimer();
    }

    protected function endAnnotationDrag(e:MDIWindowEvent) {
        e.window.removeEventListener(MDIWindowEvent.DRAG_END, endAnnotationDrag);
        stopScrollTimer();
        currentDragNode = null;
    }

    protected function onNodeMouseUp(e:MouseEvent):void {
        CursorUtil.showOpenHand();
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onNodeMouseMove);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onNodeMouseUp);
        stopScrollTimer();
        currentDragNode = null;
    }

    protected function mouseDownHandler(e:MouseEvent):void {
        if ((e.target == this || e.target == _contentLayer) && !_isLinking && (_currentTool is DiagramSelectTool)) {
            setFocus();

            if (currentDragNode == null) {
                CursorUtil.showClosedHand();
            }

            regX = e.stageX;
            regY = e.stageY;

            regHScrollPosition = this.horizontalScrollPosition;
            regVScrollPosition = this.verticalScrollPosition;
            addEventListener(MouseEvent.MOUSE_MOVE, mouseMovePanHandler);
            _isPanning = true;
        }
    }

    protected function mouseOverHandler(e:MouseEvent):void {
        if ((e.target == this || e.target == _contentLayer)) {
            setFocus();

            CursorUtil.showPointer();
        }
    }

    protected function mouseMovePanHandler(e:MouseEvent):void {
        e.stopImmediatePropagation();

        this.verticalScrollPosition = regVScrollPosition - (e.stageY - regY);
        this.horizontalScrollPosition = regHScrollPosition - (e.stageX - regX);
    }

    protected function mouseUpHandler(e:MouseEvent):void {
        if (_isPanning) {
            removeEventListener(MouseEvent.MOUSE_MOVE, mouseMovePanHandler);

            if (currentDragNode == null) {
                CursorUtil.showPointer();
            }
            this._isPanning = false;
        }

        if (e.target == this || e.target == _contentLayer) {
            setFocus();

            if (!_isLinking && !(_currentTool is DiagramZoomInTool) && !(_currentTool is DiagramZoomOutTool)) {
                _selectionManager.deselectAll();
            }
        }
        var nodePoint:Point;
        if ((_currentTool is DiagramZoomInTool)) {
            nodePoint = _contentLayer.globalToLocal(new Point(e.stageX, e.stageY));
            if (e.shiftKey) {
                zoomOut(nodePoint.x, nodePoint.y);
            } else {
                zoomIn(nodePoint.x, nodePoint.y);
            }
        } else if ((_currentTool is DiagramZoomOutTool)) {
            nodePoint = _contentLayer.globalToLocal(new Point(e.stageX, e.stageY));
            if (e.shiftKey) {
                zoomIn(nodePoint.x, nodePoint.y);
            } else {
                zoomOut(nodePoint.x, nodePoint.y);
            }
        } else {
            //dispatch event for navigator
            dispatchEvent(new DiagramEvent(DiagramEvent.DIAGRAM_CHANGED, {diagram:this}));
        }

    }

    private function startLinking(node:Node, startPoint:Point):void {
        validateEdgePreview();
        _edgePreview.lineType = _currentLineType;
        _edgePreview.startPreview(node, startPoint);
        if (stage) {
            stage.addEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
        }
    }

    private function validateEdgePreview():void {
        if (_edgePreviewRendererClass == null) {
            _edgePreviewRendererClass = DirectedEdgeRenderer;
        }

        if (!_edgePreview) {
            _edgePreview = new UIEdgePreviewImpl(this, null, null, _edgeHandleManager);
        }
        if (!this.contains(_edgePreview)) {
            this.addChild(_edgePreview);
        }

        _edgePreview.edgeRenderer = ClassInstanceManager.getClassInstance(_edgePreviewRendererClass) as EdgeRenderer;
    }

    private function removeEdgePreview():void {
        _edgePreview.clear();
        removeChild(DisplayObject(_edgePreview));
        _edgePreview = null;

        if (stage) {
            stage.removeEventListener(KeyboardEvent.KEY_DOWN, keyDownHandler);
        }
    }

    public function updateSelectedEdgesLineType(lineType:String):void {
        if (_selectionManager.numSelected > 0) {
            var i:int;
            var uiEdge:UIEdge;
            var obj:Selectable;
            var midPoint:Point;
            for (i = 0; i < _selectionManager.currentlySelected.length; i++) {
                obj = _selectionManager.currentlySelected[i];
                if ((obj is UIEdge)) {
                    uiEdge = obj as UIEdge;
                    uiEdge.lineType = lineType;
                    UIComponent(uiEdge).validateNow();
                }
            }
        }
    }

    public function updateAllEdgesLineType(lineType:String):void {
        var i:int;
        var uiEdge:UIEdge;
        var uiEdges:Vector.<UIEdge> = getUIEdges();
        for (i = 0; i < uiEdges.length; i++) {
            uiEdge = uiEdges[i];
            uiEdge.lineType = lineType;
            UIComponent(uiEdge).validateNow();
        }
    }

    public function getNodes():Vector.<Node> {
        return Vector.<Node>(_nodes.getValues());
    }

    public function getNode(id:String):Node {
        return _nodes.getValue(id) as Node;
    }

    public function getUINodes():Vector.<UINode> {
        return Vector.<UINode>(_uinodes.getValues());
    }

    public function getUINode(id:String):UINode {
        return _uinodes.getValue(id) as UINode;
    }

    public function getEdges():Vector.<Edge> {
        return Vector.<Edge>(_edges.getValues());
    }

    public function getEdge(id:String):Edge {
        return _edges.getValue(id) as Edge;
    }

    public function getUIEdges():Vector.<UIEdge> {
        return Vector.<UIEdge>(_uiedges.getValues());
    }

    public function getUIEdge(id:String):UIEdge {
        return _uiedges.getValue(id) as UIEdge;
    }

    [Bindable]
    public function get currentTool():DiagramTool {
        return _currentTool;
    }

    public function set currentTool(tool:DiagramTool):void {
        if (tool != _currentTool) {
            if (!(tool is DiagramSelectTool)) {
                _selectionManager.disableAll();
            } else {
                _selectionManager.enableAll();
            }
        }
        _currentTool = tool;

    }

    [Bindable]
    public function set currentLineType(s:String):void {
        _currentLineType = s;
    }

    public function get currentLineType():String {
        return _currentLineType;
    }

    public function set previewEdgeRenderer(c:Class):void {
        _edgePreviewRendererClass = c;
    }

    public function get previewEdgeRenderer():Class {
        return _edgePreviewRendererClass;
    }

    public function set defaultEdgeRenderer(c:Class):void {
        _defaultEdgeRenderer = c;
    }

    public function get defaultEdgeRenderer():Class {
        return _defaultEdgeRenderer;
    }

    public function set defaultEdgeLabelRenderer(c:Class):void {
        _defaultEdgeLabelRenderer = c;
    }

    public function get defaultEdgeLabelRenderer():Class {
        return _defaultEdgeLabelRenderer;
    }

    public function set defaultKnotRenderer(c:Class):void {
        _defaultKnotRenderer = c;
    }

    public function get defaultKnotRenderer():Class {
        return _defaultKnotRenderer;
    }

    public function set defaultKnotControllerRenderer(c:Class):void {
        _defaultKnotControllerRenderer = c;
    }

    public function get defaultKnotControllerRenderer():Class {
        return _defaultKnotControllerRenderer;
    }

    public function get selectionManager():SelectionManager {
        return this._selectionManager;
    }

    public function selectAll():void {
        _selectionManager.selectAll();
    }

    public function deselectAll():void {
        _selectionManager.deselectAll();
    }

    public function get centerPoint():Point {
        return new Point((_contentLayer.width / 2), (_contentLayer.height / 2));
    }

    public function get visibleCenterPoint():Point {
        var visRect:Rectangle = visibleRectangle;
        var pt:Point = new Point();

        pt.x = visRect.x + (visRect.width / 2);
        pt.y = visRect.y + (visRect.height / 2);
        return pt;
    }

    public function get visibleRectangle():Rectangle {
        var visRect:Rectangle = new Rectangle();
        visRect.x = horizontalScrollPosition;
        visRect.y = verticalScrollPosition;
        visRect.width = width - verticalScrollBar.width;
        visRect.height = height - horizontalScrollBar.height;
        return visRect;
    }

    public function get edgeLayer():DiagramLayer {
        return _edgeLayer;
    }

    public function get nodeLayer():DiagramLayer {
        return _nodeLayer;
    }

    public function get controlsLayer():DiagramLayer {
        return this._controlsLayer;
    }

    public function get contentLayer():DiagramLayer {
        return this._contentLayer;
    }

    public function get labelLayer():DiagramLayer {
        return this._labelLayer;
    }

    public function getRootUINode():UINode {
        //find the node with the lkeast parents and the most children
        _hiddenUIRootNode.node.successors.splice(0, _hiddenUIRootNode.node.successors.length);
        var currentRoot:Node;
        var nodes:Vector.<Node> = getNodes();
        var node:Node;
        var i:int;
        for (i = 0; i < nodes.length; i++) {
            node = nodes[i];
            if (node.predecessors.length < 1) {
                _hiddenUIRootNode.node.successors.push(node);
            }
            /*if(!currentRoot){
             currentRoot = node;
             continue;
             }
             if(node.predecessors.length < currentRoot.predecessors.length){
             if(node.successors.length > currentRoot.successors.length){
             currentRoot = node;
             }
             }*/
        }
        return _hiddenUIRootNode;
    }

    public function getRootUINodes():Array {
        var roots:Array = new Array();
        var nodes:Vector.<Node> = getNodes();
        var node:Node;
        var i:int;
        for (i = 0; i < nodes.length; i++) {
            node = nodes[i];
            if (node.predecessors.length < 1) {
                roots.push(node.uiNode);
            }
        }

        return roots;
    }

    public function deleteEdge(edge:Edge):void {
        dispatchEvent(new DiagramEvent(DiagramEvent.EDGE_DELETED, {diagram:this,edge:edge}, false, true));
    }

    public function deleteNode(node:Node):void {
        dispatchEvent(new DiagramEvent(DiagramEvent.NODE_DELETED, {diagram:this,node:node}, false, true));
    }

    public function forceDeleteEdge(edge:Edge):void {
        _edgeHandleManager.hideControls();
        var uiEdge:UIEdge = getUIEdge(edge.id);
        if (edge.startNode) {
            edge.startNode.removeOutLink(edge);
        }
        if (edge.endNode) {
            edge.endNode.removeInLink(edge);
        }

        _edges.remove(edge.id);

        if (uiEdge) {
            _selectionManager.removeSelectable(uiEdge);
            _uiedges.remove(edge.id);

            if (uiEdge.uiEdgeLabel) {
                _labelLayer.removeChild(DisplayObject(uiEdge.uiEdgeLabel));
            }

            if (_edgeLayer.getChildIndex(DisplayObject(uiEdge)) > -1) {
                _edgeLayer.removeChild(DisplayObject(uiEdge));
            }
            uiEdge = null;
        }

        edge = null;
    }

    public function forceDeleteNode(node:Node):void {
        var uiNode:UINode = getUINode(node.id);
        var i:int;
        var edge:Edge;

        for (i = 0; i < node.inLinks.length; i++) {
            edge = node.inLinks[i];
            edge.endNode = null;
            forceDeleteEdge(edge);
        }

        for (i = 0; i < node.outLinks.length; i++) {
            edge = node.outLinks[i];
            edge.startNode = null;
            forceDeleteEdge(edge);
        }

        node.inLinks.splice(0, node.inLinks.length);
        node.outLinks.splice(0, node.outLinks.length);

        _nodes.remove(node.id);

        if (uiNode) {
            uiNode.removeEventListener(DiagramEvent.NODE_CLICK, onNodeClick);

            _selectionManager.removeSelectable(uiNode);
            _uinodes.remove(node.id);
            if (_nodeLayer.getChildIndex(DisplayObject(uiNode)) > -1) {
                _nodeLayer.removeChild(DisplayObject(uiNode));
            }
            uiNode.nodeRenderer.node = null;
            uiNode = null;
        }

        node = null;

    }

    public function deleteSelected():void {
        var objects:Vector.<Selectable> = _selectionManager.currentlySelected;
        var selections:Vector.<Object> = new Vector.<Object>();
        var diagramObject:DiagramUIObject;
        //var nodes:Vector.<Node> = new Vector.<Node>();
        //var edges:Vector.<Edge> = new Vector.<Edge>();
        var i:int;
        for (i = 0; i < objects.length; i++) {
            diagramObject = objects[i] as DiagramUIObject;
            if (diagramObject) {
                if ((diagramObject is UINode)) {
                    selections.push(UINode(diagramObject).node);
                } else if ((diagramObject is UIEdge)) {
                    selections.push(UIEdge(diagramObject).edge);
                } else if ((diagramObject is UIEdgeLabel)) {
                    selections.push(UIEdgeLabel(diagramObject).edgeLabelRenderer.edge);
                }
            }
        }

        dispatchEvent(new DiagramEvent(DiagramEvent.SELECTIONS_DELETED, {diagram:this,selections:selections}, false, true));
    }

    protected function defaultSelectionDeleteHandler(e:DiagramEvent):void {
        if (!e.isDefaultPrevented()) {
            forceDeleteSelected();
        }
    }

    protected function defaultNodeDeleteHandler(e:DiagramEvent):void {
        if (!e.isDefaultPrevented()) {
            forceDeleteNode(e.data.node);
        }
    }

    protected function defaultEdgeDeleteHandler(e:DiagramEvent):void {
        if (!e.isDefaultPrevented()) {
            forceDeleteEdge(e.data.edge);
        }
    }

    public function forceDeleteSelected():void {
        var objects:Vector.<Selectable> = _selectionManager.currentlySelected;
        var diagramObject:DiagramUIObject;
        var nodes:Vector.<Node> = new Vector.<Node>();
        var edges:Vector.<Edge> = new Vector.<Edge>();
        var node:Node;
        var edge:Edge;

        var i:int;
        for (i = 0; i < objects.length; i++) {
            diagramObject = objects[i] as DiagramUIObject;
            if (diagramObject) {
                if ((diagramObject is UINode)) {
                    nodes.push(UINode(diagramObject).node);
                } else if ((diagramObject is UIEdge)) {
                    edges.push(UIEdge(diagramObject).edge);
                } else if ((diagramObject is UIEdgeLabel)) {
                    edges.push(UIEdgeLabel(diagramObject).edgeLabelRenderer.edge);
                }
            }
        }

        for (i = 0; i < nodes.length; i++) {
            node = nodes[i];
            if (_nodes.keyExists(node.id)) {
                forceDeleteNode(node);
            }
        }

        for (i = 0; i < edges.length; i++) {
            edge = edges[i];
            if (_edges.keyExists(edge.id)) {
                forceDeleteEdge(edge);
            }
        }
    }

    public function zoomIn(centerX:Number = -1, centerY:Number = -1):void {

        var oldMaxX:Number = maxHorizontalScrollPosition;
        var oldMaxY:Number = maxVerticalScrollPosition;

        if (_contentLayer.scaleY >= 1.6) {
            return;
        }

        _contentLayer.scaleX = (_contentLayer.scaleX + zoomFactor);
        _contentLayer.scaleY = (_contentLayer.scaleY + zoomFactor);

        UIComponent(this).validateNow();
        UIComponent(_contentLayer).validateNow();

        this.callLater(scrollInToCenterX,[oldMaxX,oldMaxY]);
        dispatchEvent(new DiagramEvent(DiagramEvent.DIAGRAM_ZOOMED, {diagram:this}));
    }

    public function zoomOut(centerX:Number = -1, centerY:Number = -1):void {

        var oldMaxX:Number = maxHorizontalScrollPosition;
        var oldMaxY:Number = maxVerticalScrollPosition;
        if (_contentLayer.scaleY <= .7) {
            return;
        }

        var newXScale:Number = (_contentLayer.scaleX - zoomFactor);
        var newYScale:Number = (_contentLayer.scaleY - zoomFactor);

        _contentLayer.scaleX = newXScale;
        _contentLayer.scaleY = newYScale;


        UIComponent(this).validateNow();
        UIComponent(_contentLayer).validateNow();

        this.callLater(scrollOutToCenterX,[oldMaxX,oldMaxY]);
        dispatchEvent(new DiagramEvent(DiagramEvent.DIAGRAM_ZOOMED, {diagram:this}));
    }

    public function scrollOutToCenterX(oldMaxScrollX:Number,oldMaxScrollY:Number):void {

        var newX = horizontalScrollPosition - ((oldMaxScrollX - maxHorizontalScrollPosition)/2);

        if(newX < 0) {
            newX = 0;
        } else if(newX > maxHorizontalScrollPosition) {
            newX = maxHorizontalScrollPosition;
        }

        horizontalScrollPosition = newX;

        var newY = verticalScrollPosition - ((oldMaxScrollY - maxVerticalScrollPosition)/2);

        if(newY < 0) {
            newY = 0;
        } else if(newY > maxVerticalScrollPosition) {
            newY = maxVerticalScrollPosition;
        }

        verticalScrollPosition = newY;

    }

    public function scrollInToCenterX(oldMaxScrollX:Number,oldMaxScrollY:Number):void {

        var newX = horizontalScrollPosition - ((oldMaxScrollX - maxHorizontalScrollPosition)/2);

        if(newX < 0) {
            newX = 0;
        } else if(newX > maxHorizontalScrollPosition) {
            newX = maxHorizontalScrollPosition;
        }

        horizontalScrollPosition = newX;

        var newY = verticalScrollPosition - ((maxVerticalScrollPosition - oldMaxScrollY)/2);

        if(newY < 0) {
            newY = 0;
        } else if(newY > maxVerticalScrollPosition) {
            newY = maxVerticalScrollPosition;
        }

        verticalScrollPosition = newY;

    }

    private function dragEnterHandler(e:DragEvent):void {
        e.preventDefault();
        var itemsArray:Array = new Array();
        if (e.dragSource.hasFormat("items")) {
            itemsArray = itemsArray.concat(e.dragSource.dataForFormat("items") as Array);
        }

        if (e.dragSource.hasFormat("treeItems")) {
            itemsArray = itemsArray.concat(e.dragSource.dataForFormat("treeItems") as Array);
        }

        var i:int;
        var accept:Boolean = false;
        var item:Object;
        for (i = 0; i < itemsArray.length; i++) {
            item = itemsArray[i];
            if (!item.hasOwnProperty("nodeRendererClass")) {
                accept = false;
                break;
            } else {
                accept = true;
            }
        }

        if (accept) {
            DragManager.acceptDragDrop(this);
            DragManager.showFeedback(DragManager.COPY);
        } else {
            DragManager.showFeedback(DragManager.NONE);
        }

    }

    private function dragDropHandler(e:DragEvent):void {
        if (!e.isDefaultPrevented()) {
            var nodePoint:Point = new Point(e.stageX, e.stageY);
            //nodePoint = _nodeLayer.globalToLocal(nodePoint);
            var itemsArray:Array = new Array();
            if (e.dragSource.hasFormat("items")) {
                var dsItem:Object = e.dragSource.dataForFormat("items");
                itemsArray = itemsArray.concat(e.dragSource.dataForFormat("items") as Array);
            }

            if (e.dragSource.hasFormat("treeItems")) {
                itemsArray = itemsArray.concat(e.dragSource.dataForFormat("treeItems") as Array);
            }
            var i:int;
            var item:Object;
            for (i = 0; i < itemsArray.length; i++) {
                item = itemsArray[i];
                if (item.hasOwnProperty("nodeRendererClass")) {
                    createNode(item.nodeRendererClass, item.data, nodePoint);
                }
            }
        }
    }

    public static function getDiagramBounds(target:DisplayObject):Rectangle {
        var bitmap:Bitmap = new Bitmap();
        var origW:Number = target.width / Math.abs(target.scaleX);
        var origH:Number = target.height / Math.abs(target.scaleY);
        var returnRect:Rectangle = new Rectangle();
        returnRect.width = origW;
        returnRect.height = origH;
        returnRect.x = 0;
        returnRect.y = 0;

        try {
            var data:BitmapData = new BitmapData(origW, origH);
            data.draw(target);

            var imgBounds:Rectangle = data.getColorBoundsRect(0xFFFFFFFF, 0xFFFFFFFF, false);

            returnRect.width = imgBounds.width;
            returnRect.height = imgBounds.height;

        } catch (e:Error) {
            //do nothing
        }

        return returnRect;
    }

    public function createAnnotation(annotation:Annotation):void {
        var sticky:StickyNote = new StickyNote();
        sticky.annotation = annotation;
        sticky.diagram = this;

        annotationWindowManager.add(sticky);

        var visCenter:Point = visibleCenterPoint;
        var newX:int = visCenter.x - (sticky.width / 2);
        var newY:int = visCenter.y - (sticky.height / 2);

        annotationWindowManager.absPos(sticky, newX, newY);

        sticky.addEventListener(MDIWindowEvent.DRAG_START, startAnnotationDrag);

    }


    public function updateStickyDisplay(sticky:StickyNote, rect:Rectangle):void {
        sticky.width = rect.width;
        sticky.height = rect.height;

        annotationWindowManager.absPos(sticky, rect.x, rect.y);
    }

    public function moveSticky(sticky:StickyNote, x:int, y:int):void {
        annotationWindowManager.absPos(sticky, x, y);
    }

    public function removeAnnotation(sticky:StickyNote):void {

        annotationWindowManager.remove(sticky);
    }


    public function getStickyByAnnotationId(id:String):StickyNote {
        var stickies:Array = annotationWindowManager.windowList;
        var i:int;
        var found:StickyNote = null;
        var sticky:StickyNote;
        for (i = 0; i < stickies.length; i++) {
            sticky = stickies[i];
            if (sticky.annotation.id == id) {
                found = sticky;
                break;
            }
        }

        return found;
    }


    public function getAllStickies():Array {

        return annotationWindowManager.windowList;
    }

    override public function get uid():String {
        return _id;
    }

    [Bindable]
    public function get layout():ArrayCollection {
        return this._layout;
    }

    public function set layout(roots:ArrayCollection):void {
        this._layout = roots;
    }


    public function showLabels():void {
        _labelLayer.visible = true;
    }

    public function hideLabels():void {
        _labelLayer.visible = false;
    }


    public function toggleLabels():void {
        if (_labelLayer.visible) {
            hideLabels();
        } else {
            showLabels();
        }
    }

    public function isLinking():Boolean {
        return this._isLinking;
    }

    protected function onEdgeLabelClick(e:DiagramEvent):void {
        e.stopPropagation();
    }

    protected function onEdgeLabelDoubleClick(e:DiagramEvent):void {
        // do nothing yet
    }

    protected function onEdgeLabelEnterKey(e:DiagramEvent):void {
        // do nothing yet
    }


}
}