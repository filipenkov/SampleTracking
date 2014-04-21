package com.sysbliss.jira.workflow.event
{
	import com.sysbliss.diagram.Diagram;
	
	import flash.events.Event;

	public class JiraDiagramEvent extends Event
	{
		[Bindable]
		public var diagram:Diagram;
		
		public function JiraDiagramEvent(type:String, d:Diagram, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.diagram = d;
		}
		
		override public function clone():Event {
			return new JiraDiagramEvent(type, diagram, bubbles, cancelable);
		}
		
	}
}