package com.sysbliss.collections
{
	public class ArrayIterator implements IIterator
	{
		private var _arr:Array;
		private var _idx:Number;
		
		public function ArrayIterator(arr:Array)
		{
			this._arr = arr;
			this._idx = 0;
		}

		public function next():*
		{
			var obj:* = _arr[_idx];
			_idx += 1;
			return obj;

		}
		
		public function hasNext():Boolean
		{
			return ( _arr.length != 0 && _idx < _arr.length );
		}
		
		public function reset():void{
			_idx = 0;
		}
		
	}
}