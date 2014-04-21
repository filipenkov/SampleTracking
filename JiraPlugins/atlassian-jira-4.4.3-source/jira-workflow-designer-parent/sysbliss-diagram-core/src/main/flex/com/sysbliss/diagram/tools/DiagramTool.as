package com.sysbliss.diagram.tools
{
	public interface DiagramTool
	{
		function get icon():Class;
		function set icon(c:Class):void;

        function get disabledIcon():Class;
		function set disabledIcon(c:Class):void;
		
		function get name():String;
		function set name(s:String):void;
		
		function get toolTip():String;
		function set toolTip(s:String):void;
		
		function get cursor():Class;
		function set cursor(c:Class):void;
		
		function get cursorDisplay():int;
		function set cursorDisplay(i:int):void;
	}
}