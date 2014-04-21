package com.sysbliss.diagram.ui {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.EdgeLabel;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.diagram.renderer.EdgeLabelRenderer;
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

public class AbstractUIEdgeLabel extends AbstractDiagramUIObject implements UIEdgeLabel {
    protected var _edgeLabel:EdgeLabel;
    protected var _edgeLabelRenderer:EdgeLabelRenderer;
    protected var defaultBGColor:Number;
    protected var selectedBGColor:Number;

    public function AbstractUIEdgeLabel(diagram:Diagram, edgeLabelRenderer:EdgeLabelRenderer) {
        super(diagram, edgeLabelRenderer);
        AbstractClassEnforcer.enforceConstructor(this, AbstractUIEdgeLabel);
        this._edgeLabel = edgeLabelRenderer.edgeLabel;
        this._edgeLabelRenderer = edgeLabelRenderer;
        _edgeLabel.uiEdgeLabel = this;
        edgeLabelRenderer.percentWidth = 100;
        edgeLabelRenderer.percentHeight = 100;
        doubleClickEnabled = true;
        this._selectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.EDGE_LABELS + "_" + UIComponent(diagram).uid);

        moveDisabled = true;
    }


    override protected function init(event:FlexEvent):void {
        super.init(event);
        addEventListener(MouseEvent.CLICK, mouseClickHandler);
        addEventListener(MouseEvent.DOUBLE_CLICK, mouseDoubleClickHandler);
    }

    protected function mouseClickHandler(e:MouseEvent):void {
        var evt:DiagramEvent = new DiagramEvent(DiagramEvent.EDGE_LABEL_CLICK, {edgeLabel:_edgeLabel,localX:e.localX,localY:e.localY,x:e.stageX,y:e.stageY});
        dispatchEvent(evt);
    }

    protected function mouseDoubleClickHandler(e:MouseEvent):void {
        if (!selectable) {
            return;
        }
        var evt:DiagramEvent = new DiagramEvent(DiagramEvent.EDGE_LABEL_DOUBLE_CLICK, {diagram:_diagram,edgeLabel:_edgeLabel,localX:e.localX,localY:e.localY,x:e.stageX,y:e.stageY});
        dispatchEvent(evt);
    }

    override protected function onKeyDown(e:KeyboardEvent):void {
        if (e.keyCode == Keyboard.ENTER) {
            var evt:DiagramEvent = new DiagramEvent(DiagramEvent.EDGE_LABEL_ENTER_KEY, {diagram:_diagram,edgeLabel:_edgeLabel});
            dispatchEvent(evt);
        } else {
            super.onKeyDown(e);
        }
    }

    override protected function measure():void {
        super.measure();
        var viewWidth:Number = edgeLabelRenderer.getExplicitOrMeasuredWidth();
        var viewHeight:Number = edgeLabelRenderer.getExplicitOrMeasuredHeight();

        var w:Number = Math.max(viewWidth, 20);
        var h:Number = Math.max(viewHeight, 20);

        measuredWidth = minWidth = w;
        measuredHeight = minHeight = h;
    }


    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
    }

    override protected function onMouseOver(event:MouseEvent):void {
        super.onMouseOver(event);
        if(!_moving) {
            CursorUtil.showOpenHand();
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
        _selectionManager.moveSelected(offset, UIEdgeLabel);


    }

    override public function move(_x:Number, _y:Number):void {

        var oldX:Number = x;
        var oldY:Number = y;
        var offset:Point = new Point((_x - oldX), (_y - oldY));
        if (offset.x == 0 && offset.y == 0) {
            return;
        }

        super.move(_x, _y);
    }

    public function highlightEdgeLabel():void {

        _edgeLabelRenderer.highlight();
    }

    public function unhighlightEdgeLabel():void {
        _edgeLabelRenderer.unhighlight();
    }

    public function get edgeLabel():EdgeLabel {
        return _edgeLabel;
    }

    public function get edgeLabelRenderer():EdgeLabelRenderer {
        return _renderer as EdgeLabelRenderer;
    }

    public function set edgeLabelRendererClass(c:Class):void {
        var i:int = getChildIndex(DisplayObject(renderer));
        var newRenderer:EdgeLabelRenderer = new c(this._diagram);
        newRenderer.percentWidth = 100;
        newRenderer.percentHeight = 100;
        newRenderer.edgeLabel = edgeLabel;

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
