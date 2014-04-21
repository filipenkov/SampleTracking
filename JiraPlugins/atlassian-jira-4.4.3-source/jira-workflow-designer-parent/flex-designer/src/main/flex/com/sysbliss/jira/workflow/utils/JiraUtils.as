package com.sysbliss.jira.workflow.utils
{
	
	import com.sysbliss.diagram.data.Edge;
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteActionRequest;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteActionRequestImpl;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteRequest;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteRequestImpl;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	
	import mx.collections.ArrayCollection;
	
	public class JiraUtils
	{
		public function JiraUtils()
		{
		}
		
		public static function createJiraDeleteRequest(objects:Vector.<Object>):FlexJiraDeleteRequest {
			var request:FlexJiraDeleteRequest = new FlexJiraDeleteRequestImpl();
			var steps:ArrayCollection = new ArrayCollection();
			var actionRequests:ArrayCollection = new ArrayCollection();
			var alreadyAdded:ArrayCollection = new ArrayCollection();
			
			var i:int;
			var obj:Object;
			
			for(i=0;i<objects.length;i++){
				obj = objects[i];
				if((obj is Node)){
					steps.addItem(Node(obj).data);
					addIncomingActions(obj as Node,actionRequests,alreadyAdded);
					addOutgoingActions(obj as Node,actionRequests,alreadyAdded);
				} else if((obj is Edge)) {
					addAction(obj as Edge,actionRequests,alreadyAdded);
				}
			}
			
			request.steps = steps;
			request.actionRequests = actionRequests;
			
			return request;
		}
		
		private static function addIncomingActions(node:Node,actionRequests:ArrayCollection,alreadyAdded:ArrayCollection):void {
			var inLinks:Vector.<Edge> = node.inLinks;
			var i:int;
			var edge:Edge;
			var startNode:Node;
			var step:FlexJiraStep;
			var action:FlexJiraAction;
			var initialAction:FlexJiraAction;
			var key:String;
			for(i=0;i<inLinks.length;i++){
				edge = inLinks[i];
				startNode = edge.startNode;
				step = startNode.data as FlexJiraStep;
				initialAction = startNode.data as FlexJiraAction;
				action = edge.data as FlexJiraAction;
				
				if(!step && initialAction){
					continue;
				}
				key = step.id + "_" + action.id;
				
				if(!alreadyAdded.contains(key)){				
					var request:FlexJiraDeleteActionRequest = new FlexJiraDeleteActionRequestImpl();
					request.step = step;
					request.action = action;
				
					actionRequests.addItem(request);
					alreadyAdded.addItem(key);
				}
			}
		}
		
		private static function addOutgoingActions(node:Node,actionRequests:ArrayCollection,alreadyAdded:ArrayCollection):void {
			var outLinks:Vector.<Edge> = node.outLinks;
			var i:int;
			var edge:Edge;
			var step:FlexJiraStep;
			var action:FlexJiraAction;
			var key:String;
			for(i=0;i<outLinks.length;i++){
				edge = outLinks[i];
				step = node.data as FlexJiraStep;
				action = edge.data as FlexJiraAction;
				
				if(step){
					key = step.id + "_" + action.id;				
					if(!alreadyAdded.contains(key)){				
						var request:FlexJiraDeleteActionRequest = new FlexJiraDeleteActionRequestImpl();
						request.step = step;
						request.action = action;
					
						actionRequests.addItem(request);
						alreadyAdded.addItem(key);
					}
				}
			}
		}
		
		private static function addAction(edge:Edge,actionRequests:ArrayCollection,alreadyAdded:ArrayCollection):void {
			var startNode:Node = edge.startNode;
			var step:FlexJiraStep = startNode.data as FlexJiraStep;
			var action:FlexJiraAction = edge.data as FlexJiraAction;
			var key:String = step.id + "_" + action.id;
			
			if(!alreadyAdded.contains(key)){				
				var request:FlexJiraDeleteActionRequest = new FlexJiraDeleteActionRequestImpl();
				request.step = step;
				request.action = action;
			
				actionRequests.addItem(request);
				alreadyAdded.addItem(key);
			}
			
		}

	}
}