package com.sysbliss.diagram.data
{
	import com.sysbliss.diagram.ui.DiagramUIObject;
	
	public interface DiagramObject
	{
		function get id():String;
		
		function get data():Object;
		function set data(value:Object):void;
		
		function get uiObject():DiagramUIObject;
	}
}