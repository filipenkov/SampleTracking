package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.diagram.DefaultDiagram;
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.ToolTypes;
    import com.sysbliss.diagram.ui.UIEdge;
    import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
    import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayoutImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.workflow.JiraWorkflowImporter;
    import com.sysbliss.jira.workflow.diagram.renderer.DefaultJiraEdgeLabelRenderer;
    import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.JiraDiagramEvent;
	import com.sysbliss.jira.workflow.event.WorkflowEvent;
	import com.sysbliss.jira.workflow.layout.LayoutExporter;
	import com.sysbliss.jira.workflow.layout.LayoutImporter;
	import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
import com.sysbliss.jira.workflow.utils.SnapshotUtil;
import com.sysbliss.jira.workflow.utils.WorkflowConstants;

import flash.display.DisplayObject;

import mx.collections.ArrayCollection;
	import mx.containers.Canvas;
    import mx.core.UIComponent;
	import mx.events.FlexEvent;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.command.CommandChain;
	
	[Bindable]
	[Event(name="workflowLoaded", type="com.sysbliss.jira.workflow.event.WorkflowEvent")]
	public class OpenWorkflowController extends WorkflowAbstractController
	{


		[Autowire]
		public var jiraService:JiraWorkflowService;
		
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		[Autowire]
		public var layoutImporter:LayoutImporter;
		
		[Autowire]
		public var layoutExporter:LayoutExporter;
		
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		public function OpenWorkflowController(){
			super();
		}
		
		[Mediate(event="${eventTypes.OPEN_WORKFLOWS}", properties="workflows")]
		public function openWorkflows(workflows:ArrayCollection):void {
			var chain:CommandChain = new CommandChain(CommandChain.SERIES);
			var numInChain:int = 0;
			var i:int;
			var workflow:FlexJiraWorkflow;
			for(i=0;i<workflows.length;i++){
				workflow = workflows.getItemAt(i) as FlexJiraWorkflow;
				if(workflowDiagramManager.getOpenWorkflowIds().contains(workflow.uid)){
					Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.FOCUS_DIAGRAM,workflowDiagramManager.getDiagramForWorkflow(workflow)));
				} else {
					numInChain++;
					chain.addCommand(createCommand(jiraService.loadWorkflow,[workflow],onOpenWorkflow,DefaultFaultHandler.handleFault,[workflow]));
				}
			}
			
			if(numInChain > 0){
				chain.completeHandler = completeHandler;
				MDIDialogUtils.popModalDialog(jiraProgressDialog);
				jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_workflow");
				chain.proceed();
			}
		}
		
		[Mediate(event="${eventTypes.LOAD_WORKFLOW}", properties="workflow,reason")]
		public function loadWorkflow(workflow:FlexJiraWorkflow,reason:String):void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_workflow");
			executeServiceCall(jiraService.loadWorkflow(workflow),onLoadWorkflow,DefaultFaultHandler.handleFault,[workflow,reason]);
		}
		
		private function onLoadWorkflow(e:ResultEvent,fjw:FlexJiraWorkflow,reason:String):void {
			var workflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;
			workflow.uid = fjw.uid;
			workflow.isLoaded = true;
			workflowDiagramManager.updateWorkflow(workflow);
			//log.debug("dispatching loaded event: " + EventTypes.WORKFLOW_LOADED + " - " + reason);
			Swiz.dispatchEvent(new WorkflowEvent(EventTypes.WORKFLOW_LOADED,workflow,reason));
		}
		
		private function onOpenWorkflow(e:ResultEvent,fjw:FlexJiraWorkflow):void {
			var workflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;
			workflow.uid = fjw.uid;
			workflow.isLoaded = true;
			var name:String = workflow.name;
			if(workflow.isDraftWorkflow){
				name = WorkflowConstants.DRAFT_PREFIX + workflow.name;
			}
			
			var diagram:Diagram = createDiagram(name);
			workflowDiagramManager.updateWorkflow(workflow);
			workflowDiagramManager.addOpenWorkflow(workflow,diagram);
			Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.DIAGRAM_CREATED,diagram));
		}
		
		private function completeHandler():void {
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_complete");
			//MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			Swiz.dispatch(EventTypes.LOAD_WORKFLOWS_COMPLETED);
		}
		
		private function createDiagram(name:String):Diagram {
			//log.debug("creating new diagram");
			var diagram:Diagram = new DefaultDiagram();
			diagram.currentLineType = ToolTypes.LINK_STRAIGHT.name;
			UIComponent(diagram).percentWidth = 100;
			UIComponent(diagram).percentHeight = 100;
			diagram.setStyle("closable", true);
			Canvas(diagram).label = name;
			diagram.name = name;
			
				
			diagram.addEventListener(FlexEvent.CREATION_COMPLETE,initDiagram);
			
			return diagram;
		}
		
		private function initDiagram(e:FlexEvent):void {
			//log.debug("init diagram " + e.target);
			var diagram:Diagram = e.target as Diagram;
			var workflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowForDiagram(diagram);
			
			diagram.removeEventListener(FlexEvent.CREATION_COMPLETE,initDiagram);
			
			var importer:JiraWorkflowImporter = new JiraWorkflowImporter(diagram,workflow);
			Swiz.autowire(importer);
			importer.importWorkflow();

            diagram.defaultEdgeLabelRenderer = DefaultJiraEdgeLabelRenderer;

			Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.DIAGRAM_INITIALIZED,diagram));
		}
		
		[Mediate(event="${eventTypes.DIAGRAM_INITIALIZED}", properties="diagram")]
		public function loadDiagramLayout(diagram:Diagram):void {
			var layoutWidth:int = Math.round(diagram.width/2);
            var workflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowForDiagram(diagram);

			if(!MDIDialogUtils.isShowing(jiraProgressDialog)){
				MDIDialogUtils.popModalDialog(jiraProgressDialog);
			}
			
			jiraProgressDialog.progressLabel = niceResourceManager.getString('json','workflow.designer.calculating.layout');
			executeServiceCall(jiraService.loadLayout(workflow),onLoadLayout,DefaultFaultHandler.handleFault,[diagram]);

		}
		
		private function onLoadLayout(e:ResultEvent,diagram:Diagram):void {

			var jwdLayout:JWDLayout = e.result as JWDLayoutImpl;
			//import the layout here
			layoutImporter.applyLayout(diagram,jwdLayout, true);

            var i:int;
            var allEdges:Array = diagram.edgeLayer.getChildren();
            var myEdge:UIEdge;
            for(i=0;i<allEdges.length;i++) {
                myEdge = allEdges[i] as UIEdge;
                if(myEdge != null && myEdge.uiEdgeLabel.x == -200) {
                    //myEdge.uiLabel.x = -200;
                    myEdge.updateLabelPosition();
                    //myEdge.adjustLabelCollision();
                }
            }


			Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.CURRENT_DIAGRAM_UPDATED,diagram));
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
		}


				
	}
}