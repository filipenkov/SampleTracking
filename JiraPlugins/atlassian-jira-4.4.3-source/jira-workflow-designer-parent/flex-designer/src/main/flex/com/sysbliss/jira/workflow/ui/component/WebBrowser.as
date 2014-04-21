package com.sysbliss.jira.workflow.ui.component
{
import com.sysbliss.jira.workflow.utils.FlexUtilities;

import flash.display.DisplayObjectContainer;
import flash.events.Event;
import flash.events.TimerEvent;
import flash.external.ExternalInterface;
import flash.geom.Point;
import flash.geom.Rectangle;

import flash.utils.Timer;

import mx.containers.Canvas;
import mx.core.Application;
import mx.core.UIComponent;
import mx.events.FlexEvent;
import mx.events.MoveEvent;
import mx.events.ResizeEvent;

public class WebBrowser extends Canvas
{
    private static var ID_GENERATOR:int = 0;

    [Inspectable(defaultValue=null)]
    private var pageUrl:String = null;
    private var initted:Boolean = false;

    private var changed:Boolean;
    private var lastChanged:Number;
    private var hidden:Boolean;

    private var lastX:Number = -1;
    private var lastY:Number = -1;
    private var lastW:Number = -1;
    private var lastH:Number = -1;

    private var timer:Timer;

    public function WebBrowser()
    {
        super();
        //id = String(++ID_GENERATOR);
        this.addEventListener(FlexEvent.CREATION_COMPLETE, onFlex, false, 0, true);

        timer = new Timer(15);
        timer.addEventListener(TimerEvent.TIMER, update);
        timer.start();

        setStyle('borderThickness', 0);
        clipContent = false;

        changed = false;
        hidden = true;

        lastChanged = -1;
    }

    public function stopTimer():void {
        timer.stop();
        timer.removeEventListener(TimerEvent.TIMER, update);
        timer = null;
    }

    private function onEvent(event:Event):void
    {
        if (event.target is UIComponent)
        {
            if (event.target.owns(this))
            {
                if (event.type == 'remove')
                {
                    // Hide it
                    visible = false;
                }
                validateWindow();
            }
        }
    }

    private function onFlex(event:FlexEvent):void
    {
        validateWindow();
        if (pageUrl != null) ExternalInterface.call('loadURL', id, pageUrl);
        systemManager.addEventListener(FlexEvent.HIDE, onEvent, true, 0, true);
        systemManager.addEventListener(FlexEvent.SHOW, onEvent, true, 0, true);
        systemManager.addEventListener(MoveEvent.MOVE, onEvent, true, 0, true);
        systemManager.addEventListener(ResizeEvent.RESIZE, onEvent, true, 0, true);
        systemManager.addEventListener(FlexEvent.REMOVE, onEvent, true, 0, true);
        systemManager.addEventListener(FlexEvent.ADD, onEvent, true, 0, true);
        initted = true;
    }

    private function onHide(event:FlexEvent):void
    {
        ExternalInterface.call('hideBrowser', id);
    }

    private function isVisible():Boolean
    {
        var obj:DisplayObjectContainer = this;
        while (obj != Application.application)
        {
            if (obj == null)
            {
                // Removed from container
                return false;
            }
            if (!obj.visible)
            {
                return false;
            }
            obj = obj.parent;
        }
        return true;
    }

    public function validateWindow():void
    {
        if (!isVisible())
        {
            onHide(null);
            return;
        }
        changed = true;
    }

    private function update(evt:TimerEvent):void
    {
        var time:Number = new Date().time;

        var p:Point = new Point(0, 0);
        p = localToGlobal(p);
        if ((p.x == 0) && (p.y == 0))
        {
            return;
        }
        if ((lastX != p.x) || (lastY != p.y) || (lastW != width) || (lastH != height))
        {
            changed = true;
        } else if (isVisible() == hidden)
        {
            changed = true;
        } else if ((lastChanged != -1) && (time - lastChanged > 500))
        {
            changed = true;
        }

        if (changed)
        {
            changed = false;
            lastChanged = time;

            if (this.visible)
            {
                hidden = false;
                lastX = p.x;
                lastY = p.y;
                lastW = width;
                lastH = height;

                var rect:Rectangle = FlexUtilities.getVisibleBounds(this);
                ExternalInterface.call('updateBrowser', id, p.x, p.y, width, height, rect.x, rect.y, rect.width, rect.height);
            } else
            {
                hidden = true;
                onHide(null);
            }
        }
    }

    public function set source(_pageUrl:String):void
    {
        pageUrl = _pageUrl;
        if (initted)
        {
            ExternalInterface.call('loadURL', id, pageUrl);
        }
    }

    [Bindable(event="changeUrl")]
    public function get source():String
    {
        return pageUrl;
    }
}
}