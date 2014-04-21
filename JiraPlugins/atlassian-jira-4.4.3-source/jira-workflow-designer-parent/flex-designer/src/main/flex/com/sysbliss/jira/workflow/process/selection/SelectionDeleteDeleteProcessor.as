package com.sysbliss.jira.workflow.process.selection
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.WorkflowEvent;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import org.swizframework.Swiz;
	
	public class SelectionDeleteDeleteProcessor extends AbstractProcessor
	{
		
		public function SelectionDeleteDeleteProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var delRequest:WorkflowSelectionDeleteRequest = request as WorkflowSelectionDeleteRequest;
			Swiz.dispatchEvent(new WorkflowEvent(EventTypes.CONFIRM_SELECTION_DELETE,delRequest.workflow));
		}
		
	}
}