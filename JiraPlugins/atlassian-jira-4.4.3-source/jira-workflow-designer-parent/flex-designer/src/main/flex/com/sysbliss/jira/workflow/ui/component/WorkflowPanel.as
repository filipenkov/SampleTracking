package com.sysbliss.jira.workflow.ui.component
{
	import com.arc90.flexlib.containers.CollapsiblePanel;
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.WorkflowEvent;
	import com.sysbliss.jira.workflow.event.WorkflowListEvent;
	import com.sysbliss.jira.workflow.manager.JiraServerManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfo;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.utils.WorkflowConstants;

	import flash.events.ContextMenuEvent;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.MouseEvent;
	import flash.ui.ContextMenu;
	import flash.ui.ContextMenuItem;
	import flash.ui.Keyboard;
    import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.List;
	import mx.controls.listClasses.ListItemRenderer;
    import mx.core.ScrollPolicy;
	import mx.core.UITextField;
	import mx.events.ListEvent;

import org.swizframework.Swiz;

	public class WorkflowPanel extends DraggablePanel
	{
		private var _vbox:VBox;
		private var _workflowList:List;
		private var _buttonBar:HBox;
		private var _workflows:ArrayCollection;
		
		private var _btnOpen:Button;
		private var _btnRefresh:Button;
		private var _btnNew:Button;
		private var _btnCopy:Button;
		private var _btnActivate:Button;
		private var _btnDelete:Button;
		private var _btnCreateDraft:Button;
		private var _btnPublishDraft:Button;
		
		//contextmenu
		private var _wfOpenItem:ContextMenuItem;
		private var _wfRefreshItem:ContextMenuItem;
		private var _wfNewItem:ContextMenuItem;
		private var _wfCopyItem:ContextMenuItem;
		private var _wfDeleteItem:ContextMenuItem;
		private var _wfDraftItem:ContextMenuItem;
		private var _wfPublishItem:ContextMenuItem;
		
		[Autowire]
		public var jiraServerManager:JiraServerManager;

        private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

		public function WorkflowPanel()
		{
			super();

            title = niceResourceManager.getString( 'json','workflow.designer.workflows' );
		}
		
		override protected function createChildren():void{
			super.createChildren();
			if(!_vbox){
				_vbox = new VBox();
				_vbox.percentWidth = 100;
				_vbox.percentHeight = 100;
				addChild(_vbox);
			}
			if(!_buttonBar){
				_buttonBar = new HBox();
                _buttonBar.styleName = "buttonBar";
				_buttonBar.percentWidth = 100;
				_buttonBar.setStyle("horizontalScrollPolicy",ScrollPolicy.AUTO);
				_buttonBar.setStyle("verticalScrollPolicy",ScrollPolicy.OFF);
				_vbox.addChild(_buttonBar);
			}
			
			if(!_btnRefresh){
				_btnRefresh = new Button();
				_btnRefresh.styleName = "panelButton";
				_btnRefresh.setStyle("icon",WorkflowConstants.ICON_REFRESH);
				_btnRefresh.width = 20;
				_btnRefresh.height = 20;
				_btnRefresh.toolTip = niceResourceManager.getString('json','workflow.designer.refresh.list');
				_btnRefresh.buttonMode = true;
				_btnRefresh.useHandCursor = true;
				_btnRefresh.addEventListener(MouseEvent.CLICK,onRefreshButton);
				_buttonBar.addChild(_btnRefresh);
			}
			
			if(!_btnNew){
				_btnNew = new Button();
				_btnNew.styleName = "panelButton";
				_btnNew.setStyle("icon",WorkflowConstants.ICON_ADD);
				_btnNew.width = 20;
				_btnNew.height = 20;
				_btnNew.toolTip = niceResourceManager.getString('json','workflow.designer.add.new.workflow');
				_btnNew.buttonMode = true;
				_btnNew.useHandCursor = true;
				_btnNew.addEventListener(MouseEvent.CLICK,onNewButton);
				_buttonBar.addChild(_btnNew);
			}
			
			if(!_btnOpen){
				_btnOpen = new Button();
				_btnOpen.styleName = "panelButton";
				_btnOpen.setStyle("icon",WorkflowConstants.ICON_OPEN);
				_btnOpen.setStyle("disabledIcon",WorkflowConstants.ICON_OPEN_DISABLED);
				_btnOpen.width = 20;
				_btnOpen.height = 20;
				_btnOpen.toolTip = niceResourceManager.getString('json','workflow.designer.open.workflow');
				_btnOpen.buttonMode = true;
				_btnOpen.useHandCursor = false;
				_btnOpen.enabled = false;
				_btnOpen.addEventListener(MouseEvent.CLICK,onOpenButton);
				_buttonBar.addChild(_btnOpen);
			}
			
			if(!_btnCopy){
				_btnCopy = new Button();
				_btnCopy.styleName = "panelButton";
				_btnCopy.setStyle("icon",WorkflowConstants.ICON_COPY);
				_btnCopy.setStyle("disabledIcon",WorkflowConstants.ICON_COPY_DISABLED);
				_btnCopy.width = 20;
				_btnCopy.height = 20;
				_btnCopy.toolTip = niceResourceManager.getString('json','workflow.designer.copy.workflow');
				_btnCopy.buttonMode = true;
				_btnCopy.useHandCursor = false;
				_btnCopy.enabled = false;
				_btnCopy.addEventListener(MouseEvent.CLICK,onCopyButton);
				_buttonBar.addChild(_btnCopy);
			}
			
			/*
			if(!_btnActivate){
				_btnActivate = new Button();
				_btnActivate.styleName = "workflowPanelButton";
				_btnActivate.setStyle("icon",WorkflowConstants.ICON_ACTIVATE);
				_btnActivate.setStyle("disabledIcon",WorkflowConstants.ICON_ACTIVATE_DISABLED);
				_btnActivate.width = 20;
				_btnActivate.height = 20;
				_btnActivate.toolTip = "Activate Workflow";
				_btnActivate.buttonMode = true;
				_btnActivate.useHandCursor = false;
				_btnActivate.enabled = false;
				_btnActivate.addEventListener(MouseEvent.CLICK,onActivateButton);
				_buttonBar.addChild(_btnActivate);
			}
			
			*/
			if(!_btnDelete){
				_btnDelete = new Button();
				_btnDelete.styleName = "panelButton";
				_btnDelete.setStyle("icon",WorkflowConstants.ICON_DELETE);
				_btnDelete.setStyle("disabledIcon",WorkflowConstants.ICON_DELETE_DISABLED);
				_btnDelete.width = 20;
				_btnDelete.height = 20;
				_btnDelete.toolTip = niceResourceManager.getString('json','workflow.designer.delete.workflow');
				_btnDelete.buttonMode = true;
				_btnDelete.useHandCursor = false;
				_btnDelete.enabled = false;
				_btnDelete.addEventListener(MouseEvent.CLICK,onDeleteButton);
				_buttonBar.addChild(_btnDelete);
			}
			
			if(!_btnCreateDraft){
				_btnCreateDraft = new Button();
				_btnCreateDraft.styleName = "panelButton";
				_btnCreateDraft.setStyle("icon",WorkflowConstants.ICON_ADD_DRAFT);
				_btnCreateDraft.setStyle("disabledIcon",WorkflowConstants.ICON_ADD_DRAFT_DISABLED);
				_btnCreateDraft.width = 20;
				_btnCreateDraft.height = 20;
				_btnCreateDraft.toolTip = niceResourceManager.getString('json','workflow.designer.create.draft');
				_btnCreateDraft.buttonMode = true;
				_btnCreateDraft.useHandCursor = false;
				_btnCreateDraft.enabled = false;
				_btnCreateDraft.addEventListener(MouseEvent.CLICK,onCreateDraftButton);
				_buttonBar.addChild(_btnCreateDraft);
			}
			
			if(!_btnPublishDraft){
				_btnPublishDraft = new Button();
				_btnPublishDraft.styleName = "panelButton";
				_btnPublishDraft.setStyle("icon",WorkflowConstants.ICON_PUBLISH_DRAFT);
				_btnPublishDraft.setStyle("disabledIcon",WorkflowConstants.ICON_PUBLISH_DRAFT_DISABLED);
				_btnPublishDraft.width = 20;
				_btnPublishDraft.height = 20;
				_btnPublishDraft.toolTip = niceResourceManager.getString('json','workflow.designer.publish.draft');
				_btnPublishDraft.buttonMode = true;
				_btnPublishDraft.useHandCursor = false;
				_btnPublishDraft.enabled = false;
				_btnPublishDraft.addEventListener(MouseEvent.CLICK,onPublishDraftButton);
				_buttonBar.addChild(_btnPublishDraft);
			}
			
			if(!_workflowList){
				_workflowList = new List();
				_workflowList.styleName = "listItem";
				_workflowList.labelFunction = determineItemLabel;
				_workflowList.iconFunction = determineItemIcon;
				_workflowList.percentWidth = 100;
				_workflowList.percentHeight = 100;
				_workflowList.allowMultipleSelection = false;
				_workflowList.doubleClickEnabled = true;
				_workflowList.addEventListener(ListEvent.ITEM_DOUBLE_CLICK,onListDoubleClick);
				_workflowList.addEventListener(KeyboardEvent.KEY_DOWN,onListKeyDown);
				_workflowList.addEventListener(ListEvent.CHANGE,onListChange);
				_vbox.addChild(_workflowList);
				createContextMenu();
			}
			
		}
		
		override protected function measure():void {
			super.measure();
			measuredWidth = measuredMinWidth = minWidth = _buttonBar.getExplicitOrMeasuredWidth() + 20;	
		}

		[Mediate(event="${eventTypes.JIRA_WORKFLOW_LIST_RETRIEVED}", properties="workflows")]
		public function setWorkflowList(list:ArrayCollection):void {
			this._workflows = list;
			this._workflowList.dataProvider = _workflows;
			updateButtonStates(null);
		}
		
		private function onListChange(e:ListEvent):void {
			var workflow:FlexJiraWorkflow = _workflowList.selectedItem as FlexJiraWorkflow;
			updateButtonStates(workflow);
		}
		
		private function createContextMenu():void {
			var cmenu:ContextMenu = new ContextMenu();
			
			cmenu.hideBuiltInItems();
			
			_wfRefreshItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.refresh.list'));
			_wfRefreshItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onRefreshButton);
			
			_wfOpenItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.open.workflow'));
			_wfOpenItem.separatorBefore = true;
			_wfOpenItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onOpenButton);
			
			_wfNewItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.add.new.workflow'));
			_wfNewItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onNewButton);
			
			_wfCopyItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.copy.workflow'));
			_wfCopyItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onCopyButton);
			
			_wfDeleteItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.delete.workflow'));
			_wfDeleteItem.separatorBefore = true;
			_wfDeleteItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onDeleteButton);
			
			_wfDraftItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.create.draft.workflow'));
			_wfDraftItem.separatorBefore = true;
			_wfDraftItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onCreateDraftButton);
			
			_wfPublishItem = new ContextMenuItem(niceResourceManager.getString('json','workflow.designer.publish.draft.workflow'));
			_wfPublishItem.addEventListener(ContextMenuEvent.MENU_ITEM_SELECT, onPublishDraftButton);

			
			cmenu.customItems.push(_wfRefreshItem);
			cmenu.customItems.push(_wfOpenItem);
            cmenu.customItems.push(_wfNewItem);
            cmenu.customItems.push(_wfCopyItem);
            cmenu.customItems.push(_wfDeleteItem);
            cmenu.customItems.push(_wfDraftItem);
            cmenu.customItems.push(_wfPublishItem);
            
            cmenu.addEventListener(ContextMenuEvent.MENU_SELECT,onShowContextMenu);
            _workflowList.contextMenu = cmenu;
		}
		
		private function onShowContextMenu(e:ContextMenuEvent):void {
			var itemRenderer:ListItemRenderer;
            itemRenderer.styleName = "listItem";
			if((e.mouseTarget is UITextField)){
				itemRenderer = e.mouseTarget.parent as ListItemRenderer;
			} else if((e.mouseTarget is ListItemRenderer)){
				itemRenderer = e.mouseTarget as ListItemRenderer;
			}
			
			if(itemRenderer){
				_workflowList.selectedItem = itemRenderer.data;
				updateButtonStates(itemRenderer.data as FlexJiraWorkflow);
			}
		}
		
		private function updateButtonStates(workflow:FlexJiraWorkflow):void {
			var server:FlexJiraServerInfo = jiraServerManager.getServerInfo();
			
			if(workflow){
				_btnOpen.enabled = true;
				_btnOpen.useHandCursor = true;
				_wfOpenItem.enabled = true;
				
				_btnCopy.enabled = true;
				_btnCopy.useHandCursor = true;
				_wfCopyItem.enabled = true;
				if(workflow.isEditable 
					&& (!server.isEnterprise || (server.isEnterprise && !workflow.hasSchemes) || workflow.isDraftWorkflow)
					){
					_btnDelete.enabled = true;
					_btnDelete.useHandCursor = true;
					_wfDeleteItem.enabled = true;
				} else {
					_btnDelete.enabled = false;
					_btnDelete.useHandCursor = false;
					_wfDeleteItem.enabled = false;
				}
				
				/*if(!workflow.isActive){
					_btnActivate.enabled = true;
					_btnActivate.useHandCursor = true;
				} else {
					_btnActivate.enabled = false;
					_btnActivate.useHandCursor = false;
				}*/
				
				if(!workflow.hasDraftWorkflow && workflow.isActive && !workflow.isSystemWorkflow){
					_btnCreateDraft.enabled = true;
					_btnCreateDraft.useHandCursor = true;
					_wfDraftItem.enabled = true;
				} else if(workflow.isDraftWorkflow && workflow.isActive){
					_btnCreateDraft.enabled = false;
					_btnCreateDraft.useHandCursor = false;
					_wfDraftItem.enabled = false;
					
					_btnPublishDraft.enabled = true;
					_btnPublishDraft.useHandCursor = true;
					_wfPublishItem.enabled = true;
				} else {
					_btnCreateDraft.enabled = false;
					_btnCreateDraft.useHandCursor = false;
					_wfDraftItem.enabled = false;
					
					_btnPublishDraft.enabled = false;
					_btnPublishDraft.useHandCursor = false;
					_wfPublishItem.enabled = false;
				}
			} else {
				_btnOpen.enabled = false;
				_btnOpen.useHandCursor = false;
				_wfOpenItem.enabled = false;
				
				_btnCopy.enabled = false;
				_btnCopy.useHandCursor = false;
				_wfCopyItem.enabled = false;
				
				_btnDelete.enabled = false;
				_btnDelete.useHandCursor = false;
				_wfDeleteItem.enabled = false;
				
				_btnCreateDraft.enabled = false;
				_btnCreateDraft.useHandCursor = false;
				_wfDraftItem.enabled = false;
				
				_btnPublishDraft.enabled = false;
				_btnPublishDraft.useHandCursor = false;
				_wfPublishItem.enabled = false;
				//_btnActivate.enabled = false;
				//_btnActivate.useHandCursor = false;
			}
		}
		private function onListDoubleClick(e:ListEvent):void {
			dispatchOpenEvent();
		}
		
		private function onListKeyDown(e:KeyboardEvent):void {
			if(e.keyCode == Keyboard.ENTER){
				dispatchOpenEvent();
			}
		}
		
		private function onOpenButton(e:Event):void {
			dispatchOpenEvent();
		}
		
		private function onRefreshButton(e:Event):void {
			Swiz.dispatch(EventTypes.REFRESH_WORKFLOW_LIST);
		}
		
		private function onCopyButton(e:Event):void {
			dispatchWorkflowEvent(EventTypes.COPY_WORKFLOW);
		}
		
		private function onActivateButton(e:Event):void {

		}
		
		private function onNewButton(e:Event):void {
			Swiz.dispatch(EventTypes.NEW_WORKFLOW);
		}
		
		private function onDeleteButton(e:Event):void {
			dispatchWorkflowEvent(EventTypes.DELETE_WORKFLOW);
		}
		
		private function onCreateDraftButton(e:Event):void {
			dispatchWorkflowEvent(EventTypes.CREATE_DRAFT_WORKFLOW);
		}
		
		private function onPublishDraftButton(e:Event):void {
			dispatchWorkflowEvent(EventTypes.PUBLISH_DRAFT_WORKFLOW);
		}
		
		private function dispatchWorkflowEvent(etype:String):void {
			var workflow:FlexJiraWorkflow = _workflowList.selectedItem as FlexJiraWorkflow;
			if(workflow){
				var event:WorkflowEvent = new WorkflowEvent(etype,workflow);
				Swiz.dispatchEvent(event);
			}
		}
		
		private function dispatchOpenEvent():void {
			if(_workflowList.selectedItems && _workflowList.selectedItems.length > 0){
				var event:WorkflowListEvent = new WorkflowListEvent(EventTypes.OPEN_WORKFLOWS,new ArrayCollection(_workflowList.selectedItems));
				Swiz.dispatchEvent(event);
			}
		}
		
		private function determineItemLabel(item:*):String {
			var label:String = niceResourceManager.getString('json','workflow.designer.untitled');
			var fjw:FlexJiraWorkflow = item as FlexJiraWorkflow;
			if(fjw.isDraftWorkflow){
				label = WorkflowConstants.DRAFT_PREFIX + fjw.name;
			} else {
				label = fjw.name;
			}
			
			return label;
		}
		
		private function determineItemIcon(item:*):Class {
			var clazz:Class = null;
			var fjw:FlexJiraWorkflow = item as FlexJiraWorkflow;
			if(fjw.isSystemWorkflow && fjw.isActive){
				clazz = WorkflowConstants.ICON_SYSTEM_ACTIVE;
			} else if(fjw.isSystemWorkflow){
				clazz = WorkflowConstants.ICON_SYSTEM_LOCK;
			} else if(!fjw.isEditable && fjw.isActive){
				clazz = WorkflowConstants.ICON_READONLY_ACTIVE;
			}else if(!fjw.isEditable){
				clazz = WorkflowConstants.ICON_READONLY;
			} else if(fjw.isDraftWorkflow){
				clazz = WorkflowConstants.ICON_DRAFT;
			}
			
			return clazz;
		}

        private function getWorkflowWithName(name:String,isDraft:Boolean):FlexJiraWorkflow
        {
            var workflowToReturn:FlexJiraWorkflow = null;

            for each (var workflow:FlexJiraWorkflow in _workflows)
            {
                if (workflow.name == name && workflow.isDraftWorkflow == isDraft)
                {
                    workflowToReturn = workflow;
                    break;
                }
            }

            return workflowToReturn;
        }

        public function openWorkflow (name:String,isDraft:Boolean):void {

            var workflow:FlexJiraWorkflow = getWorkflowWithName(name,isDraft);

            if (workflow != null)
            {
                var event:WorkflowListEvent = new WorkflowListEvent(EventTypes.OPEN_WORKFLOWS,new ArrayCollection([workflow]));
                Swiz.dispatchEvent(event);
            }
        }

	}
}
