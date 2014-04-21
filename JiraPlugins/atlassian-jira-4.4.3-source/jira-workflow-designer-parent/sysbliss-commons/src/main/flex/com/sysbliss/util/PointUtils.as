package com.sysbliss.util
{
	import flash.geom.Point;
	
	import mx.core.IFlexDisplayObject;
	
	public class PointUtils
	{
		public function PointUtils()
		{
		}
		
		public static function convertCoordinates(point:Point,fromObj:IFlexDisplayObject,toObj:IFlexDisplayObject):Point {
			var pt:Point = fromObj.localToGlobal(point);
			pt = toObj.globalToLocal(pt);
			return pt;
		}

	}
}