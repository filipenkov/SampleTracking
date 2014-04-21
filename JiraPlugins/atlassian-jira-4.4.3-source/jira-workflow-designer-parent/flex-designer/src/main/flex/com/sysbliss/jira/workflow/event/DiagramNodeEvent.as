package com.sysbliss.jira.workflow.event
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.data.Node;
	
	import flash.events.Event;

	public class DiagramNodeEvent extends Event
	{
		[Bindable]
		public var diagram:Diagram;
		
		[Bindable]
		public var node:Node;
		
		public function DiagramNodeEvent(type:String, d:Diagram, n:Node, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.diagram = d;
			this.node = n;
		}
		
		override public function clone():Event {
			return new DiagramNodeEvent(type, diagram, node, bubbles, cancelable);
		}
		
	}
}