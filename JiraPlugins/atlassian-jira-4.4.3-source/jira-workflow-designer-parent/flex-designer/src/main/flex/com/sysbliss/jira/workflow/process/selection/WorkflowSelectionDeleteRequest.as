package com.sysbliss.jira.workflow.process.selection
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.process.Request;
	import com.sysbliss.jira.workflow.process.WorkflowRequest;
	
	import mx.collections.ArrayCollection;
	
	public class WorkflowSelectionDeleteRequest extends WorkflowRequest implements Request
	{
		public var selections:ArrayCollection;
		
		public function WorkflowSelectionDeleteRequest(fjw:FlexJiraWorkflow,sel:ArrayCollection)
		{
			super(fjw);
			this.selections = sel; 
		}

	}
}