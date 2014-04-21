package com.sysbliss.jira.workflow.event
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraMetadataContainer;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.plugins.workflow.model.FlexWorkflowObject;
	import com.sysbliss.diagram.data.DiagramObject;
	
	import flash.events.Event;

	public class WorkflowMetadataEvent extends Event
	{
		[Bindable]
		public var workflow:FlexJiraWorkflow;
		
		[Bindable]
		public var metadataObject:FlexJiraMetadataContainer;
		
		[Bindable]
		public var diagramObject:DiagramObject;
		
		public function WorkflowMetadataEvent(type:String, wf:FlexJiraWorkflow, md:FlexJiraMetadataContainer, dobj:DiagramObject, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.workflow = wf;
			this.metadataObject = md;
			this.diagramObject = dobj;
		}
		
		override public function clone():Event {
			return new WorkflowMetadataEvent(type, workflow, metadataObject, diagramObject, bubbles, cancelable);
		}
		
	}
}