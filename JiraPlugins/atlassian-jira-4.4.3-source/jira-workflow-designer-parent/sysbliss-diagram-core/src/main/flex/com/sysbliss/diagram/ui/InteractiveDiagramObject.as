package com.sysbliss.diagram.ui {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.event.InteractiveDiagramObjectEvent;
import com.sysbliss.diagram.ui.selectable.AbstractSelectable;

import flash.events.Event;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.geom.Point;
import flash.geom.Rectangle;

import flash.ui.Keyboard;

import mx.core.EventPriority;
import mx.core.ScrollPolicy;
import mx.events.FlexEvent;

[Event(name="objectMovedEvent", type="com.sysbliss.diagram.event.InteractiveDiagramObjectEvent")]
[Event(name="objectDeleteEvent", type="com.sysbliss.diagram.event.InteractiveDiagramObjectEvent")]
public class InteractiveDiagramObject extends AbstractSelectable {

    public var dragBounds:Rectangle = null;
    public var moveDisabled:Boolean;
    protected var wasMoved:Boolean;
    private var localClickPoint:Point;
    protected var _diagram:Diagram;
    protected var _moving:Boolean;

    public function InteractiveDiagramObject(d:Diagram) {
        super();
        _diagram = d;
        wasMoved = false;
        _moving = false;
        moveDisabled = false;
        localClickPoint = new Point(0,0);
        focusEnabled = true;
        creationPolicy = "all";
        mouseChildren = true;
        mouseEnabled = true;
        buttonMode = false;
        addEventListener(FlexEvent.CREATION_COMPLETE, init);
        addEventListener(InteractiveDiagramObjectEvent.OBJECT_DELETE, defaultDeleteHandler, false, EventPriority.DEFAULT_HANDLER, true);
        horizontalScrollPolicy = ScrollPolicy.OFF;
        verticalScrollPolicy = ScrollPolicy.OFF;
        clipContent = false;
    }

    protected function init(event:FlexEvent):void {

        addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);

        addEventListener(MouseEvent.MOUSE_OVER, onMouseOver);
        addEventListener(MouseEvent.MOUSE_OUT, onMouseOut);

        _selectionManager.addSelectable(this);
        setupKeyboardListeners();
    }

    protected function onMouseOver(event:MouseEvent):void {
        //override if needed
    }

    protected function onMouseOut(event:MouseEvent):void {
        //override if needed
    }

    protected function onMouseMove(event:MouseEvent):void {
        if (!selectable || !visible || !isSelected || !event.buttonDown || parent == null || moveDisabled) {
            return;
        }

        var dest:Point = parent.globalToLocal(new Point(event.stageX, event.stageY));

        var desiredPos:Point = new Point();

        desiredPos.x = dest.x - localClickPoint.x;
        desiredPos.y = dest.y - localClickPoint.y;

        callLater(doMove, [desiredPos]);
        _moving = true;
        wasMoved = true;

    }

    public function nudge(dest:Point):void {

        var pos:Point = new Point(x, y);
        pos.x = x + dest.x;
        pos.y = y + dest.y;

        doMove(pos);
        dispatchMoved();
    }


    protected function onMouseDown(event:MouseEvent):void {
        if(_diagram.isLinking()) {
            return;
        }

        if (!selectable) {
            return;
        }
        if (!isSelected) {
            _selectionManager.setSelected(this);
        }


        var sp:Point = new Point(event.stageX, event.stageY);
        localClickPoint = globalToLocal(sp);

        stage.addEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
        stage.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
        setFocus();

    }

    protected function onMouseUp(event:MouseEvent):void {
        stage.removeEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
        stage.removeEventListener(MouseEvent.MOUSE_UP, onMouseUp);
        _moving = false;
        if (!selectable) {
            return;
        }
        if (wasMoved) {
            dispatchMoved();
        }

        //handle multiple selection
        if (!wasMoved && !isSelected) {
                _selectionManager.setSelected(this);
        }

        wasMoved = false;
    }

    protected function setupKeyboardListeners():void {
       addEventListener(KeyboardEvent.KEY_DOWN, onKeyDown);
    }

    protected function onKeyDown(event:KeyboardEvent):void {
        if (!selectable || !isSelected) {
            return;
        }

        switch (event.keyCode) {
            case Keyboard.UP: {
                event.stopPropagation();
                handleUpPress(event.shiftKey);
                break;
            }
            case Keyboard.DOWN:{
                event.stopPropagation();
                handleDownPress(event.shiftKey);
                break;
            }
            case Keyboard.LEFT:{
                event.stopPropagation();
                handleLeftPress(event.shiftKey);
                break;
            }
            case Keyboard.RIGHT:{
                event.stopPropagation();
                handleRightPress(event.shiftKey);
                break;
            }
            case Keyboard.DELETE:{
                event.stopPropagation();
                handleDeletePress();
                break;
            }
        }
    }

    protected function handleDeletePress():void {
        dispatchEvent(new InteractiveDiagramObjectEvent(InteractiveDiagramObjectEvent.OBJECT_DELETE, false, true));
    }

    protected function defaultDeleteHandler(e:InteractiveDiagramObjectEvent):void {
        //does noting and should be overridden
    }

    protected function handleUpPress(shiftKeyDown:Boolean):void {
        if(moveDisabled) {
            return;
        }
        var pos:Point = new Point(x, y);
        pos.y--;

        doMove(pos);
        dispatchMoved();
    }

    protected function handleDownPress(shiftKeyDown:Boolean):void {
        if(moveDisabled) {
            return;
        }
        var pos:Point = new Point(x, y);
        pos.y++;

        doMove(pos);
        dispatchMoved();

    }

    protected function handleLeftPress(shiftKeyDown:Boolean):void {
        if(moveDisabled) {
            return;
        }
        var pos:Point = new Point(x, y);
        pos.x --;

        doMove(pos);
        dispatchMoved();

    }

    protected function handleRightPress(shiftKeyDown:Boolean):void {
        if(moveDisabled) {
            return;
        }
        var pos:Point = new Point(x, y);
        pos.x++;

        doMove(pos);
        dispatchMoved();
    }

    protected function doMove(desiredPos:Point):void {

        if(moveDisabled) {
            return;
        }

        //var offset:Point = new Point((desiredPos.x - x), (desiredPos.y - y));
        //_selectionManager.moveSelected(offset);

        move(desiredPos.x, desiredPos.y);
        validateNow();

    }

    protected function applyConstraints(desiredPositon:Point):void {
        if (dragBounds != null) {
            if (desiredPositon.x < dragBounds.x) {
                desiredPositon.x = dragBounds.x;
            } else if ((dragBounds.x + dragBounds.width) < (desiredPositon.x + width)) {
                desiredPositon.x = (dragBounds.x + (dragBounds.width - width));
            }

            if (desiredPositon.y < dragBounds.y) {
                desiredPositon.y = dragBounds.y;
            } else if ((dragBounds.y + dragBounds.height) < (desiredPositon.y + height)) {
                desiredPositon.y = (dragBounds.y + (dragBounds.height - height));
            }
        }
    }

    protected function dispatchMoved():void {
        dispatchEvent(new InteractiveDiagramObjectEvent(InteractiveDiagramObjectEvent.OBJECT_MOVED_EVENT));
    }

    override public function onSelectionChanged():void {
        if (_selectionManager.numSelected < 2) {
            mouseChildren = true;
        } else if (_selectionManager.numSelected > 1) {
            mouseChildren = false;
        }
    }

    override public function select(quiet:Boolean = false):void {
        super.select(quiet);
    }

    override public function deselect(quiet:Boolean = false):void {
        drawFocus(false);
        super.deselect(quiet);
    }

    public function get diagram():Diagram {
        return this._diagram;
    }
}
}
