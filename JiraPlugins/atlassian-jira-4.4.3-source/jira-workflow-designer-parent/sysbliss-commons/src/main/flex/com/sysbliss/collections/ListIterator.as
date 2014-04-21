package com.sysbliss.collections
{
	public class ListIterator implements IIterator
	{
		private var _list:IList;
		private var _idx:Number;

		public function ListIterator(l:IList)
		{
			_list = l.clone();
			_idx = 0;

		}

		public function next():*
		{
			var obj:* = _list.getItemAt(_idx);
			_idx += 1;
			return obj;

		}
		
		public function hasNext():Boolean
		{
			return ( _list.size() != 0 && _idx < _list.size() );
		}
		
		public function reset():void{
			_idx = 0;
		}

		
	}
}