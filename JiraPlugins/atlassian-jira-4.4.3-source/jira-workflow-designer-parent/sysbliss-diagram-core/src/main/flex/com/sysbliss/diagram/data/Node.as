package com.sysbliss.diagram.data
{

	import com.sysbliss.diagram.ui.UINode;
	
	public interface Node
	{	
		function get id():String;
		
		function get data():Object;
		function set data(value:Object):void;
		
		function addInLink(edge:Edge):void;
		function removeInLink(edge:Edge):void;
		
		function addOutLink(edge:Edge):void;
		function removeOutLink(edge:Edge):void;
		
		function get inLinks():Vector.<Edge>;
		function get outLinks():Vector.<Edge>;
		
		function get predecessors():Vector.<Node>;
		function get successors():Vector.<Node>;
		
		function set uiNode(uiNode:UINode):void;
		function get uiNode():UINode;

	}
}