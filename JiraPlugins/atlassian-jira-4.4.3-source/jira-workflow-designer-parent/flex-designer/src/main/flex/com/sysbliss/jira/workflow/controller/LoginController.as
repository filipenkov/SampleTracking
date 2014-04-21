package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.GenericDataEvent;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
	
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class LoginController extends WorkflowAbstractController
	{
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
		
		public function LoginController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.DO_LOGIN}", properties="data")]
		public function doLogin(data:Object):void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_logging_in");

			executeServiceCall(jiraService.login(data.username,data.password),onLogin,DefaultFaultHandler.handleFault);
		}
		
		private function onLogin(e:ResultEvent):void {
			Swiz.dispatchEvent(new GenericDataEvent(EventTypes.USER_SESSION_RETRIEVED,e.result as String));	
			Swiz.dispatch(EventTypes.LOGIN_SUCCESS);
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
		}
		
	}
}