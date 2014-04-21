package com.sysbliss.jira.workflow.utils
{
    import com.sysbliss.collections.ICollection;
	import com.sysbliss.diagram.Diagram;
    import com.sysbliss.diagram.data.Edge;
	import com.sysbliss.diagram.ui.UIEdge;
	import com.sysbliss.diagram.ui.UINode;
	import com.sysbliss.diagram.data.DiagramObject;
    import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
    import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
    import com.sysbliss.jira.workflow.event.DiagramEdgeEvent;
	import com.sysbliss.jira.workflow.event.DiagramNodeEvent;
	import com.sysbliss.jira.workflow.event.EventTypes;
import com.sysbliss.jira.workflow.event.ToggleIssueEditableEvent;
import com.sysbliss.jira.workflow.event.WorkflowMetadataEvent;
	import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraMetadataContainer;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;

	import flash.events.ContextMenuEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
    import mx.resources.IResourceManager;

    import mx.resources.ResourceManager;

    import org.swizframework.Swiz;
	
	public class ContextMenuUtils
	{
		private var _nodeMenu:ContextMenu;
		private var _edgeMenu:ContextMenu;
		
		private var _nodeDeleteItem:ContextMenuItem;
		private var _nodePropertiesItem:ContextMenuItem;
		
		private var _edgeConditionItem:ContextMenuItem;
		private var _edgeValidatorItem:ContextMenuItem;
		private var _edgeFunctionItem:ContextMenuItem;
		private var _edgeEditItem:ContextMenuItem;
		private var _edgeDeleteItem:ContextMenuItem;
		private var _edgePropertiesItem:ContextMenuItem;

        private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;

        protected function get resourceManager():IResourceManager {
            return ResourceManager.getInstance();
        }

		
		public function ContextMenuUtils()
		{
            // do nothing for now - delay creation until after JSON translation data is read
		}

        public function createContextMenus():void
        {
            _nodeMenu = createNodeContextMenu();
			_edgeMenu = createEdgeContextMenu();
        }

		
		public function getNodeMenu():ContextMenu {
			return _nodeMenu;
		}
		
		public function getEdgeMenu():ContextMenu {
			return _edgeMenu;
		}

		private function createNodeContextMenu():ContextMenu {
			var cmenu:ContextMenu = new ContextMenu();
			cmenu.hideBuiltInItems();

			_nodeDeleteItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.delete.selected'));
			_nodeDeleteItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, clickDeleteSelected);
			
			_nodePropertiesItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.step.properties'));
			_nodePropertiesItem.separatorBefore = true;
			_nodePropertiesItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, clickNodeProperties);
			
			cmenu.customItems.push(_nodeDeleteItem);
            cmenu.customItems.push(_nodePropertiesItem);
            
            cmenu.addEventListener(ContextMenuEvent.MENU_SELECT,onShowNodeMenu);
            return cmenu;
		}
				
		private function createEdgeContextMenu():ContextMenu {
			var cmenu:ContextMenu = new ContextMenu();
			cmenu.hideBuiltInItems();
			
			_edgeConditionItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.view.conditions'));
			_edgeConditionItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, clickEdgeConditions);
			
			_edgeValidatorItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.view.validators'));
			_edgeValidatorItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, clickEdgeValidators);
			
			_edgeFunctionItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.view.post.functions'));
			_edgeFunctionItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, clickEdgeFunctions);
			
			_edgeEditItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.edit.transition.title'));
			_edgeEditItem.separatorBefore = true;
			_edgeEditItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, clickEdgeEdit);

			_edgeDeleteItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.delete.selected'));
			_edgeDeleteItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, clickDeleteSelected);
			
			_edgePropertiesItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.transition.properties'));
			_edgePropertiesItem.separatorBefore = true;
			_edgePropertiesItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT,  clickEdgeProperties);
			
			cmenu.customItems.push(_edgeConditionItem);
			cmenu.customItems.push(_edgeValidatorItem);
			cmenu.customItems.push(_edgeFunctionItem);
			cmenu.customItems.push(_edgeEditItem);
			cmenu.customItems.push(_edgeDeleteItem);
            cmenu.customItems.push(_edgePropertiesItem);
            
            cmenu.addEventListener(ContextMenuEvent.MENU_SELECT,onShowEdgeMenu);
            
            return cmenu;
		}
		
		private function onShowNodeMenu(e:ContextMenuEvent):void {
			var cmenu:ContextMenu = e.target as ContextMenu;
			var uiNode:UINode = e.contextMenuOwner as UINode;
			var step:FlexJiraStep = uiNode.node.data as FlexJiraStep;
			var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
			
			if(!uiNode.isSelected){
				uiNode.selectionManager.addSelected(uiNode);
			}
			
			if(!step || !workflow.isEditable){
				_nodeDeleteItem.enabled = false;
				_nodePropertiesItem.enabled = false;
			} else {
				_nodeDeleteItem.enabled = true;
				_nodePropertiesItem.enabled = true;
			}
		}
		
		private function onShowEdgeMenu(e:ContextMenuEvent):void {
			var cmenu:ContextMenu = e.target as ContextMenu;
			var uiedge:UIEdge = e.contextMenuOwner as UIEdge;
			var action:FlexJiraAction = uiedge.edge.data as FlexJiraAction;
			var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
			
			if(!uiedge.isSelected){
				uiedge.selectionManager.addSelected(uiedge);
			}
			
			_edgeConditionItem.enabled = true;
			_edgeValidatorItem.enabled = true;
			_edgeFunctionItem.enabled = true;
			_edgeDeleteItem.enabled = true;
			_edgeEditItem.enabled = true;
			_edgePropertiesItem.enabled = true;
			
			if(!action || !workflow.isEditable || workflow.isInitialAction(action.id)){
				_edgeDeleteItem.enabled = false;
				_edgeEditItem.enabled = false;
			}

			if(!action || !workflow.isEditable){
				_edgePropertiesItem.enabled = false;
			}

            if(workflow.isInitialAction(action.id)) {
				_edgeConditionItem.enabled = false;
			}
		}


		private function clickNodeProperties(e:ContextMenuEvent):void {
			var uiNode:UINode = e.contextMenuOwner as UINode;
            onNodeProperties(uiNode);
		}

		public function onNodeProperties(uiNode:UINode):void {
			var wf:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

			Swiz.dispatchEvent(new WorkflowMetadataEvent(EventTypes.SHOW_EDIT_PROPERTIES,wf,uiNode.node.data as FlexJiraMetadataContainer,uiNode.node as DiagramObject));
		}

        private function clickDeleteSelected(e:ContextMenuEvent):void
        {
            onDeleteSelected();
        }
		
		private function clickEdgeConditions(e:ContextMenuEvent):void
        {
			var uiEdge:UIEdge = e.contextMenuOwner as UIEdge;
            onEdgeConditions(uiEdge);
		}
		
		private function clickEdgeValidators(e:ContextMenuEvent):void
        {
			var uiEdge:UIEdge = e.contextMenuOwner as UIEdge;
			onEdgeValidators(uiEdge);
		}
		
		private function clickEdgeFunctions(e:ContextMenuEvent):void
        {
			var uiEdge:UIEdge = e.contextMenuOwner as UIEdge;
			onEdgeFunctions(uiEdge);
		}
		
		private function clickEdgeEdit(e:ContextMenuEvent):void
        {
			var uiEdge:UIEdge = e.contextMenuOwner as UIEdge;
			onEdgeEdit(uiEdge);
		}
		
		private function clickEdgeProperties(e:ContextMenuEvent):void
        {
			var uiEdge:UIEdge = e.contextMenuOwner as UIEdge;
			onEdgeProperties(uiEdge);
		}

        public function onDeleteSelected():void
        {
            workflowDiagramManager.getCurrentDiagram().deleteSelected();
        }

		public function onEdgeConditions(uiEdge:UIEdge):void
        {
			var d:Diagram = workflowDiagramManager.getCurrentDiagram();

			Swiz.dispatchEvent(new DiagramEdgeEvent(EventTypes.SHOW_ACTION_CONDITIONS,d,uiEdge.edge));
		}

		public function onEdgeValidators(uiEdge:UIEdge):void
        {
			var d:Diagram = workflowDiagramManager.getCurrentDiagram();

			Swiz.dispatchEvent(new DiagramEdgeEvent(EventTypes.SHOW_ACTION_VALIDATORS,d,uiEdge.edge));
		}

		public function onEdgeFunctions(uiEdge:UIEdge):void
        {
			var d:Diagram = workflowDiagramManager.getCurrentDiagram();

			Swiz.dispatchEvent(new DiagramEdgeEvent(EventTypes.SHOW_ACTION_FUNCTIONS,d,uiEdge.edge));
		}

		public function onEdgeEdit(uiEdge:UIEdge):void
        {
			var d:Diagram = workflowDiagramManager.getCurrentDiagram();

			Swiz.dispatchEvent(new DiagramEdgeEvent(EventTypes.SHOW_EDIT_ACTION,d,uiEdge.edge));
		}

		public function onEdgeProperties(uiEdge:UIEdge):void
        {
			var wf:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

			Swiz.dispatchEvent(new WorkflowMetadataEvent(EventTypes.SHOW_EDIT_PROPERTIES,wf,uiEdge.edge.data as FlexJiraMetadataContainer,uiEdge.edge as DiagramObject));
		}

        public function onToggleIssueEditable(step:FlexJiraStep, newValue:Boolean, item:Object):void {
            var wf:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow()

			Swiz.dispatchEvent(new ToggleIssueEditableEvent(EventTypes.TOGGLE_ISSUE_EDITABLE,wf, step, newValue, item));
        }
    }
}