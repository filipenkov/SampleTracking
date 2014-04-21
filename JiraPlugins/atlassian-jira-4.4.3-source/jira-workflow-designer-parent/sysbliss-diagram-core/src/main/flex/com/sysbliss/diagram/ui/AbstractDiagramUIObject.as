package com.sysbliss.diagram.ui {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.event.InteractiveDiagramObjectEvent;
import com.sysbliss.diagram.renderer.DiagramObjectRenderer;
import com.sysbliss.util.AbstractClassEnforcer;

import flash.display.DisplayObject;

import mx.core.UIComponent;

public class AbstractDiagramUIObject extends InteractiveDiagramObject implements DiagramUIObject {
    protected var _renderer:DiagramObjectRenderer;

    public function AbstractDiagramUIObject(diagram:Diagram, renderer:DiagramObjectRenderer) {
        super(diagram);
        AbstractClassEnforcer.enforceConstructor(this, AbstractDiagramUIObject);
        this._renderer = renderer;
    }

    override protected function createChildren():void {
        super.createChildren();
        var _do:DisplayObject = renderer as DisplayObject;
        if (_do) {
            addChild(_do);
            _do.x = 0;
            _do.y = 0;
        }
    }

    override protected function defaultDeleteHandler(e:InteractiveDiagramObjectEvent):void {
        if (!e.isDefaultPrevented()) {
            _diagram.deleteSelected();
        }
    }

    public function get renderer():DiagramObjectRenderer {
        return this._renderer;
    }

    override public function select(quiet:Boolean = false):void {
        super.select(quiet);
        if (this._renderer is UIComponent) {
            UIComponent(this._renderer).invalidateDisplayList();
            UIComponent(this._renderer).validateNow();
        }

    }

    override public function deselect(quiet:Boolean = false):void {
        super.deselect(quiet);
        if (this._renderer is UIComponent) {
            UIComponent(this._renderer).invalidateDisplayList();
            UIComponent(this._renderer).validateNow();
        }
    }

}
}