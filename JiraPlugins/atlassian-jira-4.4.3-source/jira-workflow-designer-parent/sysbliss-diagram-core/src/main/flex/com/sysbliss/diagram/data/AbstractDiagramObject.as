package com.sysbliss.diagram.data
{
	import com.sysbliss.diagram.ui.DiagramUIObject;

import mx.utils.UIDUtil;

public class AbstractDiagramObject implements DiagramObject
	{
		private var _id:String;
		private var _data:Object;
		
		public function AbstractDiagramObject(dataObj:Object=null)
		{
			this._id = UIDUtil.createUID();
			this.data = dataObj;
		}

		public function get id():String
		{
			return _id;
		}

		[Bindable]
		public function get data():Object
		{
			return _data;
		}
		
		public function set data(value:Object):void
		{
			_data = value;
		}
		
		public function get uiObject():DiagramUIObject
		{
			return null;
		}
		
	}
}