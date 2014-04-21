package com.sysbliss.jira.workflow.process.transition
{
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.ProcessChain;
	import com.sysbliss.jira.workflow.process.Request;
	
	import org.swizframework.factory.IInitializingBean;

	public class WorkflowEditTransitionProcessChain extends AbstractProcessor implements IInitializingBean
	{
		[Autowire(bean="editableProcessor")]
		public var editableProcessor:ProcessChain;
		
		[Autowire(bean="editTransitionInitialActionProcessor")]
		public var initialActionProcessor:ProcessChain;
		
		[Autowire(bean="editTransitionDialogProcessor")]
		public var dialogProcessor:ProcessChain;
		
		
		public function WorkflowEditTransitionProcessChain()
		{
			super();
		}
		
		public function initialize():void {
			initialActionProcessor.successor = dialogProcessor;
			editableProcessor.successor = initialActionProcessor;
			successor = editableProcessor;		
		}
		
		override public function processRequest(request:Request):void {
			editableProcessor.successor = initialActionProcessor;
			successor.processRequest(request);
		}
		
	}
}