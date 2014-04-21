package com.sysbliss.diagram.ui.selectable
{
	import com.sysbliss.diagram.event.SelectableEvent;
	
	import mx.containers.Canvas;
	
	[Event(name="selected", type="com.sysbliss.diagram.event.SelectableEvent")]
	[Event(name="deselected", type="com.sysbliss.diagram.event.SelectableEvent")]
	
	public class AbstractSelectable extends Canvas implements Selectable
	{
		protected var _isSelected:Boolean = false;
		protected var _selecatable:Boolean = true;
		protected var _selectionManager:SelectionManager;
		
		public function AbstractSelectable()
		{
			super();
			//this is the default selection manager and should be overridden in subclasses
			this._selectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.DEFAULT);
		}
		
		public function select(quiet:Boolean = false) : void
		{	
			_isSelected = true;
			if(!quiet){
				dispatchEvent( new SelectableEvent(SelectableEvent.SELECTED,this) );
			}		
		}
		public function deselect(quiet:Boolean = false) : void
		{
			_isSelected = false;
			if(!quiet){
				dispatchEvent( new SelectableEvent(SelectableEvent.DESELECTED,this) );
			}	
		}
		
		public function get isSelected():Boolean {
			return _isSelected;
		}
		
		public function set selectable(b:Boolean):void {
			this._selecatable = b;
		}
		
		public function get selectable():Boolean {
			return _selecatable;
		}
		
		public function set selectionManager(manager:SelectionManager):void {
			this._selectionManager = manager;	
		}
		
		public function get selectionManager():SelectionManager {
			return _selectionManager;
		}

        public function onSelectionChanged():void {
            //subclasses can override this
        }
    }
}