package com.sysbliss.diagram.ui {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.diagram.data.Node;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.diagram.renderer.NodeRenderer;
import com.sysbliss.diagram.tools.DiagramLinkTool;
import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
import com.sysbliss.diagram.util.CursorUtil;
import com.sysbliss.util.AbstractClassEnforcer;

import flash.display.DisplayObject;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.ui.Keyboard;

import mx.core.UIComponent;
import mx.events.FlexEvent;

public class AbstractUINode extends AbstractDiagramUIObject implements UINode {
    protected var _node:Node;

    public function AbstractUINode(diagram:Diagram, renderer:NodeRenderer) {
        super(diagram, renderer);
        AbstractClassEnforcer.enforceConstructor(this, AbstractUINode);
        this._node = nodeRenderer.node;
        _node.uiNode = this;
        nodeRenderer.percentWidth = 100;
        nodeRenderer.percentHeight = 100;
        doubleClickEnabled = true;
        this._selectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.DIAGRAM_OBJECTS + "_" + UIComponent(diagram).uid);

    }

    override protected function init(event:FlexEvent):void {
        super.init(event);
        addEventListener(MouseEvent.CLICK, mouseClickHandler);
        addEventListener(MouseEvent.DOUBLE_CLICK, mouseDoubleClickHandler);
    }

    protected function mouseClickHandler(e:MouseEvent):void {
        var evt:DiagramEvent = new DiagramEvent(DiagramEvent.NODE_CLICK, {node:_node,localX:e.localX,localY:e.localY,x:e.stageX,y:e.stageY});
        dispatchEvent(evt);
    }

    protected function mouseDoubleClickHandler(e:MouseEvent):void {
        if (!selectable) {
            return;
        }
        var evt:DiagramEvent = new DiagramEvent(DiagramEvent.NODE_DOUBLE_CLICK, {diagram:_diagram,node:_node,localX:e.localX,localY:e.localY,x:e.stageX,y:e.stageY});
        dispatchEvent(evt);
    }

    override protected function onKeyDown(e:KeyboardEvent):void {
        if (e.keyCode == Keyboard.ENTER) {
            var evt:DiagramEvent = new DiagramEvent(DiagramEvent.NODE_ENTER_KEY, {diagram:_diagram,node:_node});
            dispatchEvent(evt);
        } else {
            super.onKeyDown(e);
        }
    }

    override protected function measure():void {
        super.measure();
        var viewWidth:Number = nodeRenderer.getExplicitOrMeasuredWidth();
        var viewHeight:Number = nodeRenderer.getExplicitOrMeasuredHeight();
        var w:Number = 20;
        var h:Number = 20;

        if (viewWidth > 20) {
            w = viewWidth;
        }

        if (viewHeight > 20) {
            h = viewHeight;
        }
        measuredWidth = minWidth = w;
        measuredHeight = minHeight = h;
    }


    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
    }

    override protected function onMouseOver(event:MouseEvent):void {
        super.onMouseOver(event);
        if(!_moving) {
            if(_diagram.currentTool is DiagramLinkTool) {
                CursorUtil.showCrossHairs();
            } else {
                CursorUtil.showOpenHand();
            }

        }

    }

    override protected function onMouseOut(e:MouseEvent):void {
        super.onMouseOut(e);

        if(!_moving) {
            CursorUtil.showPointer();
        }

    }

    override protected function onMouseDown(e:MouseEvent):void {
        super.onMouseDown(e);
        CursorUtil.showClosedHand();
    }
    //we need to override this so we can filter out non-node objects
    override protected function doMove(desiredPos:Point):void {
        if (moveDisabled) {
            return;
        }

        applyConstraints(desiredPos);
        var offset:Point = new Point((desiredPos.x - x), (desiredPos.y - y));
        _selectionManager.moveSelected(offset, UINode);

    }

    override public function move(_x:Number, _y:Number):void {

        var oldX:Number = x;
        var oldY:Number = y;
        var offset:Point = new Point((_x - oldX), (_y - oldY));
        if (offset.x == 0 && offset.y == 0) {
            return;
        }

        super.move(_x, _y);


        var moveTo:Point;
        var edgePoint:Point;
        var i:int;
        var inLink:Edge;
        var outLink:Edge;
        var uiEdge:UIEdge;

        for (i = 0; i < node.inLinks.length; i++) {
            inLink = node.inLinks[i] as Edge;
            uiEdge = diagram.getUIEdge(inLink.id);
            edgePoint = uiEdge.endPoint;

            moveTo = new Point((edgePoint.x + offset.x), (edgePoint.y + offset.y));
            uiEdge.moveEndPoint(moveTo.x, moveTo.y);
            UIComponent(uiEdge).validateNow();
        }

        for (i = 0; i < node.outLinks.length; i++) {
            outLink = node.outLinks[i] as Edge;
            uiEdge = diagram.getUIEdge(outLink.id);
            edgePoint = uiEdge.startPoint;
            moveTo = new Point((edgePoint.x + offset.x), (edgePoint.y + offset.y));
            uiEdge.moveStartPoint(moveTo.x, moveTo.y);
            UIComponent(uiEdge).validateNow();
        }
    }

    public function get node():Node {
        return _node;
    }

    public function get nodeRenderer():NodeRenderer {
        return _renderer as NodeRenderer;
    }

    public function set nodeRendererClass(c:Class):void {
        var i:int = getChildIndex(DisplayObject(renderer));
        var newRenderer:NodeRenderer = new c(this._diagram);
        newRenderer.percentWidth = 100;
        newRenderer.percentHeight = 100;
        newRenderer.node = node;

        var _do:DisplayObject = newRenderer as DisplayObject;
        removeChild(DisplayObject(renderer));
        addChildAt(_do, i);

        _renderer = newRenderer;
        invalidateDisplayList();
        validateNow();
    }

    public function get centerPoint():Point {
        return new Point(Math.round(this.x + (measuredWidth / 2)), Math.round(this.y + (measuredHeight / 2)));

    }
}
}