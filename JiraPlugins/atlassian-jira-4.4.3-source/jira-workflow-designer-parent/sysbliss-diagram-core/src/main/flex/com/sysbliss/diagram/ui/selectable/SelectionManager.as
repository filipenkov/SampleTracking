package com.sysbliss.diagram.ui.selectable
{

	import flash.events.IEventDispatcher;
	import flash.geom.Point;
	
	
	[Event(name="selected", type="com.sysbliss.diagram.event.SelectableEvent")]
	[Event(name="selectionAdded", type="com.sysbliss.diagram.event.SelectableEvent")]
	[Event(name="selectAll", type="com.sysbliss.diagram.event.SelectableEvent")]
	[Event(name="deselected", type="com.sysbliss.diagram.event.SelectableEvent")]
	[Event(name="selectionRemoved", type="com.sysbliss.diagram.event.SelectableEvent")]
	[Event(name="deselectAll", type="com.sysbliss.diagram.event.SelectableEvent")]
	
	public interface SelectionManager extends IEventDispatcher
	{
		function get numSelected():int;
		function setSelected(obj:Selectable):void;
		function addSelected(obj:Selectable):void;
		function removeSelected(obj:Selectable):void;
		function moveSelected(point:Point,filter:*=null):void;
		function deselectAll():void;
		function selectAll():void;
		function enableAll():void;
		function disableAll():void;
		function getItems():Vector.<Selectable>;
		function addSelectable(obj:Selectable):void;
		function removeSelectable(obj:Selectable):void;
		function set currentlySelected(v:Vector.<Selectable>):void;
		function get currentlySelected():Vector.<Selectable>;
	}
}