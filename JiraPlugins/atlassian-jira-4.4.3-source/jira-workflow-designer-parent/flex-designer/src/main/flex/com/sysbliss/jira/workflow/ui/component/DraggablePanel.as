package com.sysbliss.jira.workflow.ui.component
{
import com.arc90.flexlib.containers.CollapsiblePanel;

import flash.events.Event;
import flash.events.MouseEvent;

public class DraggablePanel extends CollapsiblePanel
{

    public function DraggablePanel()
    {
        super();
    }

    override protected function createChildren():void
    {
        super.createChildren();
        super.titleBar.addEventListener(MouseEvent.MOUSE_DOWN, handleDown);
        super.titleBar.addEventListener(MouseEvent.MOUSE_UP, handleUp);
    }

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
    {
        super.updateDisplayList(unscaledWidth, unscaledHeight);
    }

    private function handleDown(e:Event):void
    {
        this.startDrag();
    }

    private function handleUp(e:Event):void
    {
        this.stopDrag();
    }

}
}
