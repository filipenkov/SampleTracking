package com.sysbliss.collections
{
	
	public interface IMap
	{
		 function put( key:*, val:* ) : void;
		 function getKeys():Array;
		 function getKeyAt(i:int):*;
		 function getValues():Array;
		 function getValue( key:* ):*;
		 function keyExists( key:* ):Boolean;
		 function valueExists( val:* ):Boolean;
		 function size():int;
		 function clear():void;
		 function remove(key:*):void;

	}
}