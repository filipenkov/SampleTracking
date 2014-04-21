package com.sysbliss.jira.workflow.service
{
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.workflow.manager.UserTokenManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteRequest;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraMetadataContainer;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;

	import mx.logging.ILogger;
	import mx.rpc.AsyncToken;
	import mx.rpc.remoting.RemoteObject;


	public class JiraServiceDelegate implements JiraWorkflowService
	{
		
		[Autowire(bean="jiraServiceRO")]
		public var service:RemoteObject;
		
		[Autowire]
		public var tokenManager:UserTokenManager;
		
		public function JiraServiceDelegate()
		{
			super();
		}
		
		public function getUserSession():AsyncToken {
			return service.getUserSession();	
		}
		
		public function getJiraServerInfo():AsyncToken{
			return service.getJiraServerInfo();
		}		
		
		public function getJiraUserPrefs():AsyncToken{
			return service.getUserPrefs(tokenManager.token);
		}
		
		public function getWorkflows():AsyncToken {
			return service.getWorkflows(tokenManager.token);
		}
		
		public function loadWorkflow(fjw:FlexJiraWorkflow):AsyncToken {
			return service.loadWorkflow(fjw,tokenManager.token);
		}
		
		public function getAllStatuses():AsyncToken {
			return service.getAllStatuses(tokenManager.token);
		}
		
		public function getFieldScreens():AsyncToken {
			return service.getFieldScreens(tokenManager.token);
		}
		
		public function addStep(step:FlexJiraStep,workflow:FlexJiraWorkflow,layout:JWDLayout):AsyncToken {
			return service.addStep(step,workflow,layout,tokenManager.token);
		}
		
		public function addTransition(name:String, desc:String, view:String, fjFromStep:FlexJiraStep, fjToStep:FlexJiraStep, fjw:FlexJiraWorkflow):AsyncToken {
			return service.addTransition(name,desc,view,fjFromStep,fjToStep,fjw,tokenManager.token);
		}

        public function cloneTransition(name:String, desc:String, actionIdToCopy:int, fjFromStep:FlexJiraStep, fjToStep:FlexJiraStep, fjw:FlexJiraWorkflow):AsyncToken {
			return service.cloneTransition(name,desc,actionIdToCopy,fjFromStep,fjToStep,fjw,tokenManager.token);
		}

		public function deleteStepsAndActions(deleteRequest:FlexJiraDeleteRequest,workflow:FlexJiraWorkflow,layout:JWDLayout):AsyncToken {
			return service.deleteStepsAndActions(deleteRequest,workflow,layout,tokenManager.token);
		}
		
		public function copyWorkflow(newName:String, newDesc:String, fjw:FlexJiraWorkflow):AsyncToken{
			return service.copyWorkflow(newName,newDesc,fjw,tokenManager.token);
		}
		
    	public function createDraftWorkflow(fjw:FlexJiraWorkflow):AsyncToken{
    		return service.createDraftWorkflow(fjw,tokenManager.token)
    	}
    	
    	public function deleteWorkflow(fjw:FlexJiraWorkflow):AsyncToken{
    		return service.deleteWorkflow(fjw,tokenManager.token);
    	}
    	
    	public function publishDraftWorkflow(fjw:FlexJiraWorkflow, enableBackup:Boolean=false, backupName:String=""):AsyncToken{
    		return service.publishDraftWorkflow(fjw,enableBackup,backupName,tokenManager.token);
    	}
    	
    	public function createNewWorkflow(name:String, desc:String):AsyncToken{
    		return service.createNewWorkflow(name,desc,tokenManager.token);
    	}
    	
    	public function updateStep(step:FlexJiraStep,newName:String,newStatus:String,fjw:FlexJiraWorkflow):AsyncToken {
    		return service.updateStep(step,newName,newStatus,fjw,tokenManager.token);
    	}

        public function updateIssueEditable(step:FlexJiraStep,editable:Boolean,fjw:FlexJiraWorkflow):AsyncToken {
    		return service.updateIssueEditable(step,editable,fjw,tokenManager.token);
    	}
    	
    	public function updateAction(action:FlexJiraAction,newName:String,newDesc:String,newDestStep:FlexJiraStep,newView:String,workflow:FlexJiraWorkflow):AsyncToken {
    		return service.updateAction(action,newName,newDesc,newDestStep,newView,workflow,tokenManager.token);
    	}
    	
    	public function createNewStatus(name:String, desc:String, iconUrl:String):AsyncToken {
    		return service.createNewStatus(name,desc,iconUrl,tokenManager.token);
    	}
    	
    	public function updateStatus(id:String, name:String, desc:String, iconUrl:String):AsyncToken{
    		return service.updateStatus(id,name,desc,iconUrl,tokenManager.token);
    	}
    	
		public function deleteStatus(id:String):AsyncToken {
			return service.deleteStatus(id,tokenManager.token);
		}
		
		public function updateProperties(mdo:FlexJiraMetadataContainer,data:Object,workflow:FlexJiraWorkflow):AsyncToken {
			return service.updateProperties(mdo,data,workflow,tokenManager.token);
		}
		
		public function login(username:String,password:String):AsyncToken {
			return service.login(username,password);
		}
		
		public function loadLayout(fjw:FlexJiraWorkflow):AsyncToken {
			return service.loadLayout(fjw,tokenManager.token);
		}
		
		public function calculateLayout(layout:JWDLayout):AsyncToken {
			return service.calculateLayout(layout);
		}
		
		public function saveActiveLayout(name:String,layout:JWDLayout):AsyncToken {
			return service.saveActiveLayout(name,layout,tokenManager.token);
		}

		public function saveDraftLayout(name:String,layout:JWDLayout):AsyncToken {
			return service.saveDraftLayout(name,layout,tokenManager.token);
		}

        public function useCommonTransition(actionIdToReuse:int, fjFromStep:FlexJiraStep, fjw:FlexJiraWorkflow):AsyncToken
        {
            return service.useCommonTransition(actionIdToReuse,fjFromStep,fjw,tokenManager.token);
        }

        public function addGlobalTransition(name:String, desc:String, resultId:int, view:String, fjw:FlexJiraWorkflow):AsyncToken
        {
            return service.addGlobalTransition(name,desc,resultId,view,fjw,tokenManager.token);
        }

        public function cloneGlobalTransition(name:String, desc:String, actionIdToCopy:int, fjw:FlexJiraWorkflow):AsyncToken {
			return service.cloneGlobalTransition(name,desc,actionIdToCopy,fjw,tokenManager.token);
		}

        public function updateGlobalAction(action:FlexJiraAction,newName:String,newDesc:String,newDestStepId:int,newView:String,workflow:FlexJiraWorkflow):AsyncToken {
    		return service.updateGlobalAction(action,newName,newDesc,newDestStepId,newView,workflow,tokenManager.token);
    	}

        public function deleteGlobalAction(actionId:int,fjw:FlexJiraWorkflow):AsyncToken
        {
            return service.deleteGlobalAction(actionId,fjw,tokenManager.token)
        }


        public function addAnnotationToWorkflow(workflow:FlexJiraWorkflow, annotation:WorkflowAnnotation,layout:JWDLayout):AsyncToken {
            return service.addAnnotationToWorkflow(workflow, annotation,layout,tokenManager.token);
        }

        public function removeAnnotationFromWorkflow(workflow:FlexJiraWorkflow, annotation:WorkflowAnnotation,layout:JWDLayout):AsyncToken {
            return service.removeAnnotationFromWorkflow(workflow, annotation,layout,tokenManager.token);
        }

        public function updateAnnotationForWorkflow(workflow:FlexJiraWorkflow, annotation:WorkflowAnnotation,layout:JWDLayout):AsyncToken {
            return service.updateAnnotationForWorkflow(workflow, annotation,layout,tokenManager.token);
        }
    }
}