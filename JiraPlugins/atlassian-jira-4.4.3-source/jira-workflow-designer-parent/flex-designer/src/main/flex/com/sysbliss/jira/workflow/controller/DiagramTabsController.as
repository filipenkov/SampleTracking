package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.JiraDiagramEvent;

	import flash.display.DisplayObject;
	import flash.events.Event;
	
	import flexlib.containers.SuperTabNavigator;
	import flexlib.events.SuperTabEvent;
	
	import mx.core.Container;
	import mx.core.UIComponent;
	import mx.events.IndexChangedEvent;
	import mx.logging.ILogger;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class DiagramTabsController extends WorkflowAbstractController
	{
		private var _tabs:SuperTabNavigator;

        private var _hideTabHeaders:Boolean;

		public function DiagramTabsController()
		{
			super();
		}
		
		private function setupTabContainer():void {
			_tabs.addEventListener(IndexChangedEvent.CHANGE,tabClicked);
			_tabs.addEventListener(SuperTabEvent.TAB_CLOSE,tabClosed);	
		}

        public function hideTabHeaders():void {
           _hideTabHeaders = true;
        }
		
		private function tabClicked(e:Event):void {
			//log.debug("processing tab click: " + e.type);
			Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.CURRENT_DIAGRAM_CHANGED,_tabs.selectedChild as Diagram));
		}
		
		private function tabClosed(e:SuperTabEvent):void {
			var d:Diagram = _tabs.getChildAt(e.tabIndex) as Diagram;
			Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.DIAGRAM_TAB_CLOSED,d));
			
			//if it's not the last tab, we need to dispatch an index change
			if(e.tabIndex < _tabs.numChildren-1){
				var newDiagram:Diagram = _tabs.getChildAt(e.tabIndex+1) as Diagram;
				Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.CURRENT_DIAGRAM_CHANGED,newDiagram));
			}
			//the tab hasn't been removed yet, so index is off by 1
			if(_tabs.numChildren == 1){
				Swiz.dispatch(EventTypes.ALL_WORKFLOWS_CLOSED);
			}
		}
		
		[Mediate(event="${eventTypes.DIAGRAM_CREATED}", properties="diagram")]
		public function addDiagramToTab(diagram:Diagram):void {
			_tabs.addChild(DisplayObject(diagram));
			_tabs.selectedChild = Container(diagram);
			_tabs.invalidateDisplayList();
			_tabs.validateNow();
			UIComponent(diagram).validateNow();

            if (_hideTabHeaders) {
                _tabs.getTabAt(_tabs.numChildren-1).visible = false;
                _tabs.getTabAt(_tabs.numChildren-1).height = 0;
            }
		}
		
		[Mediate(event="${eventTypes.DIAGRAM_REMOVED_FROM_MANAGER}", properties="diagram")]
		public function removeDiagramFromTab(diagram:Diagram):void {
			//log.debug("got diagram removed event");
			var i:int = _tabs.getChildIndex(DisplayObject(diagram));
			if(i>-1){
				//log.debug("removing tab " + i);
				_tabs.removeChildAt(i);
			}
		}
		
		[Mediate(event="${eventTypes.FOCUS_DIAGRAM}", properties="diagram")]
		public function focusDiagramTab(diagram:Diagram):void {
			_tabs.selectedIndex = _tabs.getChildIndex(DisplayObject(diagram));
			Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.CURRENT_DIAGRAM_CHANGED,_tabs.selectedChild as Diagram));
		}
			
		public function set tabs(t:SuperTabNavigator):void {
			this._tabs = t;
			setupTabContainer();
		}
		
		public function get tabs():SuperTabNavigator {
			return this._tabs;
		}
		
		
	}
}