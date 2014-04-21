package com.sysbliss.collections
{
	import com.sysbliss.util.ClassLogger;
	
	import mx.logging.ILogger;
	
	public class ArrayList extends AbstractCollection implements IList
	{
		private var e:Error;
		private var log:ILogger = ClassLogger.getLogger();
		
		public function ArrayList(){}
		
		public function addFromArray( a:Array ) : void {
			var i:Number = 0;
			var s:Number = a.length;
			for( i = 0; i < s; i++ ){
				this.addItem( a[i] );
			}
		}
		
		public function subList( start:Number, end:Number ) : IList {
			var l:ArrayList = new ArrayList();
			var i:Number = 0;
			for( i = start; i < end; i++ ){
				l.addItem( this.getItemAt(i) );
			}
			return l;
		}
	
		
		public function indexOf( obj : * ) : Number {
			return this.findIndex( obj );
		}
		
		public function lastIndexOf( obj : * ) : Number {
			var i:Number;
			var idx:Number = -1;
			var s:Number = this.size();
			for( i = 0; i < s; i++ ){
				if( this.getItemAt(i) == obj ){
					idx = i;
				}
			}
			return idx;
		}
		
		public function setItemAt( idx:Number, obj:* ):void{
			_arr[idx] = obj;
		}
		
		public function getItemAt( idx:Number ):*{
			return _arr[idx];
		}
		
		public override function iterator() : IIterator {
			return new ListIterator( this );
		}
	
		public function clone() : IList {
			return this.subList( 0, this.size() );
		}
		
		public function forEach(callback:Function, thisObject:*=null):ArrayList{
			var array:Array = this.toArray();
			array.forEach(callback,thisObject);
			
			var newList:ArrayList = new ArrayList();
			newList.addFromArray(array);
			
			return newList;
		}
		
		public function insertItemAt(index:Number, object:*):void {
			if( index < 0 || index > _arr.length ){
				throw new Error("Attempted to add a negative value or a value larger than the ArrayList size", "ArrayList");
			}
			var previousItem:*;
			//Test to ensure that illegal conditions are not met
			if(index < 0 || index > this.size()+1){
				throw new Error();
			}
			for (var i:Number = index; i < this.size()+1; i++) {
				if (previousItem == undefined || previousItem == null) {
					previousItem = _arr[i];
					this.setItemAt(index, object);
					log.debug("previous item is null or undefined, set to " + previousItem);
				}
				else {
					var currentItem:* = _arr[i];
					_arr[i] = previousItem;
					previousItem = currentItem;
					log.debug("previous item found and is = " + previousItem);
				}
				
			}
		}
		
		/**
		 * Swaps the items in two positions
		 * @author Leif Nelson and Matt Wakefield
		 * @param index1 the first item to swap - it will end up at index2
		 * @param index2 the second item to swap - it will end up at index1
		 * @return void
		 */
		public function swap(index1:Number, index2:Number):void { 
			 var item1:* = getItemAt(index1); 
			 var item2:* = getItemAt(index2); 
			  
			 removeItemAt(index1); 
			 insertItemAt(index1, item2); 
			  
			 removeItemAt(index2); 
			 insertItemAt(index2, item1); 
		 }


	}
}