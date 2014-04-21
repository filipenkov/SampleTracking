package com.sysbliss.diagram.util
{
	
	import com.sysbliss.util.PointUtils;
	
	import flash.geom.Point;
	
	import mx.core.IFlexDisplayObject;
	
	public class VectorUtils
	{
		public function VectorUtils()
		{
		}
		
		public static function mapPointsToLocal(v:Vector.<Point>, source:IFlexDisplayObject, target:IFlexDisplayObject):Vector.<Point> {
			var i:int;
			var newPoint:Point;
			var newVector:Vector.<Point> = new Vector.<Point>();
			for(i=0;i<v.length;i++){
				newVector.push(PointUtils.convertCoordinates(v[i],source,target));
			}
			return newVector;
		}
		
		public static function updatePointsToLocal(sourceVector:Vector.<Point>,targetVector:Vector.<Point>, source:IFlexDisplayObject, target:IFlexDisplayObject):void {
			if(sourceVector.length != targetVector.length){
				throw new Error("VectorUtils::updatePointsToLocal - source and target vectors must be the same length");
			}
			var i:int;
			var newPoint:Point;

			for(i=0;i<sourceVector.length;i++){
				newPoint = PointUtils.convertCoordinates(sourceVector[i],source,target);
				targetVector[i].x = newPoint.x;
				targetVector[i].y = newPoint.y;
			}

		}
		
		public static function mapGlobalToLocal(v:Vector.<Point>, target:IFlexDisplayObject):Vector.<Point> {
			var i:int;
			var newPoint:Point;
			var newVector:Vector.<Point> = new Vector.<Point>();
			for(i=0;i<v.length;i++){
				newVector.push(target.globalToLocal(v[i]));
			}
			return newVector;
		}
		
		public static function mapLocalToGlobal(v:Vector.<Point>, target:IFlexDisplayObject):Vector.<Point> {
			var i:int;
			var newPoint:Point;
			var newVector:Vector.<Point> = new Vector.<Point>();
			for(i=0;i<v.length;i++){
				newVector.push(target.localToGlobal(v[i]));
			}
			return newVector;
		}
		
		public static function indexOfPoint(v:Vector.<Point>,p:Point):int {
			var i:int;
			var ret:int = -1;
			var vp:Point;
			for(i=0;i<v.length;i++){
				vp = v[i];
				if(vp.x == p.x && vp.y == p.y){
					ret = i;
					break;
				}
			}
			return ret;
		}
		
		
		
	}
}