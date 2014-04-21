package com.sysbliss.collections
{
	public interface IList extends ICollection
	{
		 function subList( start:Number, end:Number ) : IList ;
		 function indexOf( obj : * ) : Number ;
		 function lastIndexOf( obj : * ) : Number ;
		 function setItemAt( idx:Number, obj:* ):void;
		 function getItemAt( idx:Number ):*;
		 function clone():IList;

	}
}