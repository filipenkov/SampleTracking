package com.sysbliss.jira.workflow.process.transition
{
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.ProcessChain;
	import com.sysbliss.jira.workflow.process.Request;
	
	import org.swizframework.factory.IInitializingBean;

	public class WorkflowAddTransitionProcessChain extends AbstractProcessor implements IInitializingBean
	{
		
		[Autowire(bean="editableProcessor")]
		public var editableProcessor:ProcessChain;
		
		[Autowire(bean="addTransitionDraftProcessor")]
		public var draftProcessor:ProcessChain;
		
		[Autowire(bean="addTransitionInitialActionProcessor")]
		public var initialActionProcessor:ProcessChain;
		
		[Autowire(bean="addTransitionDialogProcessor")]
		public var dialogProcessor:ProcessChain;
		
		
		public function WorkflowAddTransitionProcessChain()
		{
			super();
		}
		
		public function initialize():void {
			initialActionProcessor.successor = dialogProcessor;
			draftProcessor.successor = initialActionProcessor;
			editableProcessor.successor = draftProcessor;
			successor = editableProcessor;		
		}
		
		override public function processRequest(request:Request):void {
			editableProcessor.successor = draftProcessor;
			successor.processRequest(request);
		}
		
	}
}