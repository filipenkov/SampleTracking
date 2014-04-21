package com.sysbliss.diagram.ui
{
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
	import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
	import com.sysbliss.util.AbstractClassEnforcer;

	public class AbstractUIControlPoint extends AbstractUIKnot implements UIControlPoint
	{
		protected var _isStart:Boolean;
		protected var _isEnd:Boolean;
		
		public function AbstractUIControlPoint(d:Diagram)
		{
			super(d);
			AbstractClassEnforcer.enforceConstructor(this,AbstractUIControlPoint);
			this._selectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.EDGE_CONTROLS);
			this._isEnd = false;
			this._isStart = false;
		}
				
		public function set isStart(b:Boolean):void {
			this._isStart = b;
		}
		
		public function get isStart():Boolean {
			return _isStart;
		}
		
		public function set isEnd(b:Boolean):void {
			this._isEnd = b;
		}
		
		public function get isEnd():Boolean {
			return _isEnd;
		}
		
		
	}
}