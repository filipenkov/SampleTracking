package com.sysbliss.jira.workflow.process.selection
{
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.ProcessChain;
	import com.sysbliss.jira.workflow.process.Request;
	
	import org.swizframework.factory.IInitializingBean;
	
	public class WorkflowSelectionDeleteProcessChain extends AbstractProcessor implements IInitializingBean
	{
		[Autowire(bean="editableProcessor")]
		public var editableProcessor:ProcessChain;
		
		[Autowire(bean="draftDeleteProcessor")]
		public var draftProcessor:ProcessChain;
		
		[Autowire(bean="initialActionDeleteProcessor")]
		public var initialActionProcessor:ProcessChain;
		
		[Autowire(bean="initialResultDeleteProcessor")]
		public var initialResultProcessor:ProcessChain;
		
		[Autowire(bean="confirmDeleteProcessor")]
		public var confirmProcessor:ProcessChain;
		
		[Autowire(bean="deleteDeleteProcessor")]
		public var deleteProcessor:ProcessChain;
		
		public function WorkflowSelectionDeleteProcessChain()
		{
			super();
		}
		
		public function initialize():void {
			confirmProcessor.successor = deleteProcessor;
			initialResultProcessor.successor = confirmProcessor;
			initialActionProcessor.successor = initialResultProcessor;
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