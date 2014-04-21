package com.sysbliss.collections
{
	public interface ICollection
	{
		function toArray() : Array ;
		function isEmpty() : Boolean ;
		function contains(obj : *) : Boolean ;
		function containsAll( list : ICollection ) : Boolean ;
		function addItem( obj : * ) : void ;
		function addAll( list : ICollection ) : void;
		function remove( obj : * ) : void ;
		function clear() : void ;
		function size():Number;
		function removeAll( list : ICollection ) : void ;
		function removeItemAt(idx:Number):void;
		function retainAll( list : ICollection ):void;
		function toString() : String;
		function iterator():IIterator;
	}
}