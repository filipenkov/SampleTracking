package com.sysbliss.util
{
	import flash.utils.ByteArray;
	
	public class ObjectUtils
	{
		public function ObjectUtils()
		{
		}
		
		public static function clone(source:*):*
		{
		    var buffer:ByteArray = new ByteArray();
		    buffer.writeObject(source);
		    buffer.position = 0;
		    return buffer.readObject();
		}

	}
}