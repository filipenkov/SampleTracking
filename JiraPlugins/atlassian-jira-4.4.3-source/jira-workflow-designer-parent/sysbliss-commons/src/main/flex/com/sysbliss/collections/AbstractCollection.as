package com.sysbliss.collections
{
	import com.sysbliss.util.AbstractClassEnforcer;
	
	public class AbstractCollection
	{
		protected var _arr:Array;
		
		public function AbstractCollection()
	    {
	        AbstractClassEnforcer.enforceConstructor(this,AbstractCollection);
	        this._arr = new Array();
	    }	
		
		public function toArray() : Array {
			var a:Array = new Array();
			var i:Number = 0;
			var s:Number = this.size();
			for( i = 0; i < s; i++ ){
				a.push( this._arr[i] );
			}
			return a;
		}
	
		public function isEmpty() : Boolean {
			return (this._arr.length == 0);
		}
		
		public function contains(obj : *) : Boolean {
			var idx:Number = this.findIndex(obj);
			return (idx > -1 );
		}
	
		public function containsAll( list : ICollection ) : Boolean {
			var it:IIterator = list.iterator();
			while( it.hasNext() ){
				if( ! this.contains( it.next() ) ){
					return false;
				}
			}
			return true;
		}
	
		public function addItem( obj : * ) : void {
			this._arr.push( obj );
		}
	
		public function addAll( list : ICollection ) : void {
			var it:IIterator = list.iterator();
			while( it.hasNext() ){
				addItem( it.next() );
			}
		}
	
		public function remove( obj : * ) : void {
			var idx:Number = this.findIndex(obj);
			if( idx != -1 ){
				removeItemAt(idx);
			}
		}
	
		public function clear() : void {
			this._arr.splice(0,this.size());
		}
	
		public function size():Number{
			return this._arr.length;
		}
	
		public function removeAll( list : ICollection ) : void {
			var it:IIterator = list.iterator();
			while( it.hasNext() ){
				this.remove( it.next() );
			}
		}
		
		public function removeItemAt(idx:Number):void
		{
			this._arr.splice( idx, 1 );	
		}
	
		public function retainAll( list : ICollection ):void{
			var i:Number;
			var new_arr:Array = new Array();
			var s:Number = this.size();
			for( i = 0; i < s; i++ ){
				if( list.contains( this._arr[i] ) ){
					new_arr.push( this._arr[i] );
				}
			}
			this._arr = new_arr;
		}
	
		public function toString() : String {
			return this._arr.toString();
		}
	
		public function iterator():IIterator {
			return new ArrayIterator( this.toArray() );
		}
		
		public function findIndex( obj:* ):Number{
			var i:Number;
			var s:Number = this.size();
			for( i = 0; i < s; i++ ){
				if( this._arr[i] == obj ){
					return i;
				}
			}
			return -1;
		}


	}
}