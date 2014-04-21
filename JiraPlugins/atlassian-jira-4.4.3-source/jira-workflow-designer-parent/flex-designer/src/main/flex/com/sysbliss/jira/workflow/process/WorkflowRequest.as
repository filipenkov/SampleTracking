package com.sysbliss.jira.workflow.process
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	
	public class WorkflowRequest implements Request
	{
		public var workflow:FlexJiraWorkflow;
		
		public function WorkflowRequest(fjw:FlexJiraWorkflow)
		{
			this.workflow = fjw;
		}

	}
}