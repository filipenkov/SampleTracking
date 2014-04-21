package com.sysbliss.jira.workflow.event
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.data.Edge;
	
	import flash.events.Event;

	public class DiagramEdgeEvent extends Event
	{
		[Bindable]
		public var diagram:Diagram;
		
		[Bindable]
		public var edge:Edge;
		
		public function DiagramEdgeEvent(type:String, d:Diagram, e:Edge, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.diagram = d;
			this.edge = e;
		}
		
		override public function clone():Event {
			return new DiagramEdgeEvent(type, diagram, edge, bubbles, cancelable);
		}
		
	}
}