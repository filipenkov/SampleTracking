package com.sysbliss.diagram.data
{

	public class HiddenRootNodeImpl extends DefaultNode
	{
		private var _predecessors:Vector.<Node>;
		private var _successors:Vector.<Node>;
		
		public function HiddenRootNodeImpl(data:Object=null)
		{
			super(data);
			_predecessors = new Vector.<Node>();
			_successors = new Vector.<Node>();
		}
		
		override public function get predecessors():Vector.<Node>{
			
			return _predecessors;
		}
		
		override public function get successors():Vector.<Node>{
			
			return _successors;
		}
		
	}
}