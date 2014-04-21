package com.sysbliss.diagram.data
{
	import com.sysbliss.diagram.ui.UIEdge;
	
	
	public interface Edge
	{
		function get id():String;
		
		function get data():Object;
		function set data(value:Object):void;
		
		function get startNode():Node;
		function set startNode(n:Node):void;
		
		function get endNode():Node;
		function set endNode(n:Node):void;
		
		function set uiEdge(uiEdge:UIEdge):void;
		function get uiEdge():UIEdge;
		
	}
}