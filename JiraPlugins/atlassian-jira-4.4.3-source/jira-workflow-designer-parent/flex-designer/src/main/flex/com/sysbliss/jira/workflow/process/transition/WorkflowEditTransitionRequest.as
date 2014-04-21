package com.sysbliss.jira.workflow.process.transition
{
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.process.Request;
	import com.sysbliss.jira.workflow.process.WorkflowRequest;

	public class WorkflowEditTransitionRequest extends WorkflowRequest implements Request
	{
		public var edge:Edge;
		
		public function WorkflowEditTransitionRequest(fjw:FlexJiraWorkflow,edge:Edge)
		{
			super(fjw);
			this.edge = edge;
		}
		
	}
}