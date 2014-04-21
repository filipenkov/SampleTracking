/**
 *  This class is a highly modified version of code from the objectHandles project.
 *  ObjectHandles is licensed under the MIT License http://www.opensource.org/licenses/mit-license.php
 *  Latest information on this project can be found at http://www.rogue-development.com/objectHandles.xml
 * 
 * Description:
 *    SelectionManager manages the state of a set of Selectable objects
 * 
 **/
 
package com.sysbliss.diagram.ui.selectable
{
	

	import com.sysbliss.diagram.event.SelectableEvent;
	
	import flash.events.EventDispatcher;
	import flash.geom.Point;
	
	import mx.core.IFlexDisplayObject;
	import mx.core.UIComponent;
	

	public class SelectionManagerImpl extends EventDispatcher implements SelectionManager
	{
		private var _items:Vector.<Selectable>;
		private var _currentlySelected:Vector.<Selectable>;
		
		public function SelectionManagerImpl() {
			this._items = new Vector.<Selectable>();
			this._currentlySelected = new Vector.<Selectable>();
		}

		public function get currentlySelected():Vector.<Selectable>{
			 return _currentlySelected;
		}
		public function set currentlySelected(v:Vector.<Selectable>):void {
			deselectAll();
			var i:int;
			for(i=0;i<v.length;i++){
				if(_items.indexOf(v[i]) > -1){
					addSelected(v[i]);
				}
			}
		}
		
		public function get numSelected():int {
			return _currentlySelected.length;
		}
		
		public function setSelected(obj:Selectable) : void
		{						
			doSetSelected(obj);
			dispatchEvent(new SelectableEvent(SelectableEvent.SELECTED,obj));
		}
		
		public function addSelected(obj:Selectable):void {
			doAddSelected(obj);
			dispatchEvent(new SelectableEvent(SelectableEvent.SELECTION_ADDED,obj));
		}
		
		public function removeSelected(obj:Selectable):void {
			doRemoveSelected(obj);
			dispatchEvent(new SelectableEvent(SelectableEvent.SELECTION_REMOVED,obj));
		}
		
		public function deselectAll():void {
			doDeselectAll();
			dispatchEvent(new SelectableEvent(SelectableEvent.DESELECT_ALL,_items));
		}
		
		public function selectAll():void {
			doSelectAll();
			dispatchEvent(new SelectableEvent(SelectableEvent.SELECT_ALL,_currentlySelected));
		}
		
		public function moveSelected(point:Point,filter:*=null):void {
			var obj:IFlexDisplayObject;
			var myPoint:Point;
			var i:int;
			for(i=0;i<_currentlySelected.length;i++) {
				obj = _currentlySelected[i] as IFlexDisplayObject;
				if(filter != null){
					if(!(obj is filter)){
						continue;
					}
				}
				obj.move((obj.x + point.x),(obj.y + point.y));
				UIComponent(obj).validateNow();
			}
		}
		
		
		public function enableAll():void {
			toggleSelectable(true);
		}
		
		public function disableAll():void {
			toggleSelectable(false);
		}
		
		public function getItems():Vector.<Selectable>
		{
          return _items;
        }
        
		public function addSelectable(obj:Selectable) : void
		{
			if(_items.indexOf(obj) < 0){
				_items.push(obj);
			}
		}
		
		public function removeSelectable( obj : Selectable ) : void
		{
			var i:int = _items.indexOf(obj);
			if(i>-1){
				_items.splice(i,1);
			}
			var i2:int = _currentlySelected.indexOf(obj);
			if(i2>-1){
				_currentlySelected.splice(i2,1);
			}
			
		}
		
		private function toggleSelectable(b:Boolean):void {
			var selectable:Selectable;
			var i:int;
			for(i=0;i<_items.length;i++) {
				selectable = _items[i] as Selectable;
				selectable.selectable = b;
			}
		}
		
		private function doSetSelected(obj:Selectable) : void
		{						
			if( obj != null )
			{
				doDeselectAll();
				doAddSelected(obj);
			}
		}
		
		private function doAddSelected(obj:Selectable):void {
			if( obj != null )
			{
				if( obj.isSelected ) { return; }
				if(_currentlySelected.indexOf(obj) < 0){
					_currentlySelected.push(obj);
				}
				obj.select();
                notifySelectionChanged();
			}
		}
		
		private function doDeselectAll():void {
			var i:int;
			var vCopy:Vector.<Selectable> = _currentlySelected.concat();
			for(i=0;i<vCopy.length;i++) {
				doRemoveSelected(vCopy[i]);
			}
		}
		
		private function doSelectAll():void {
			var selectable:Selectable;
			var i:int;
			for(i=0;i<_items.length;i++) {
				selectable = _items[i] as Selectable;
				doAddSelected(selectable);
			}
		}
		
		private function doRemoveSelected(obj:Selectable):void {
			if( obj != null )
			{
				if( !obj.isSelected ) { return; }
				
				var i:int = _currentlySelected.indexOf(obj);
				if(i < 0){ return; }
				_currentlySelected.splice(i,1);
				obj.deselect();
                obj.onSelectionChanged();
                notifySelectionChanged();
			}
		}

        private function notifySelectionChanged():void {
            var i:int;
            var obj:Selectable;
			for(i=0;i<_currentlySelected.length;i++) {
				obj = _currentlySelected[i] as Selectable;
				obj.onSelectionChanged();
			}
        }
	}
}