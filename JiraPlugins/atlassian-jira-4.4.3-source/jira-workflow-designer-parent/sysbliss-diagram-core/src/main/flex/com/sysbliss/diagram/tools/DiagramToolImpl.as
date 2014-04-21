package com.sysbliss.diagram.tools {
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;

import mx.resources.IResourceManager;
import mx.resources.ResourceManager;

public class DiagramToolImpl implements DiagramTool {
    public static var CURSOR_REPLACE:int = 0;
    public static var CURSOR_ATTACH:int = 1;

    protected var _icon:Class;
    protected var _disabledIcon:Class;
    protected var _name:String;
    protected var _toolTip:String;
    protected var _cursor:Class;
    protected var _cursorDisplay:int;

    protected const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

    protected function get resourceManager():IResourceManager {
        return ResourceManager.getInstance();
    }

    public function DiagramToolImpl() {
    }

    public function get icon():Class {
        return _icon;
    }

    public function set icon(c:Class):void {
        this._icon = c;
    }

    public function get disabledIcon():Class {
        return _disabledIcon;
    }

    public function set disabledIcon(c:Class):void {
        this._disabledIcon = c;
    }

    public function get name():String {
        return _name;
    }

    public function set name(s:String):void {
        this._name = s;
    }

    public function get toolTip():String {
        return _toolTip;
    }

    public function set toolTip(s:String):void {
        this._toolTip = s;
    }

    public function get cursor():Class {
        return _cursor;
    }

    public function set cursor(c:Class):void {
        this._cursor = c;
    }

    public function get cursorDisplay():int {
        return _cursorDisplay;
    }

    public function set cursorDisplay(i:int):void {
        this._cursorDisplay = i;
    }

}
}