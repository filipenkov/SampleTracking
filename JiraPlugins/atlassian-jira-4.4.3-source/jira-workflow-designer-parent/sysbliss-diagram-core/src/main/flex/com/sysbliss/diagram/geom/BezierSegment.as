package com.sysbliss.diagram.geom {

	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	public class BezierSegment{	
	
		public var A:Point;
		public var B:Point;
		public var C:Point;
		public var D:Point;
		
		private var _point:Point;
		private var _pos:Point;
		private var _nextNeg:Point;
		private var _nextPoint:Point;

		public function BezierSegment(a:Point, b:Point, c:Point, d:Point){
			/*
				a = point[i]
				b = positive control[i]
				c = negative control[i+1]
				d = point[i+1]
			*/
			updatePoints(a,b,c,d);		
		}
		
		public function updatePoints(a:Point, b:Point, c:Point, d:Point):void {
			_point = a;
			_pos = b;
			_nextNeg = c;
			_nextPoint = d;
			
			this.A = new Point(a.x, a.y);
			
			this.B = new Point(-3.0 * a.x + 3 * b.x, 
							   -3.0 * a.y + 3 * b.y);
							   
			this.C = new Point(3.0 * a.x - 6.0 * b.x + 3.0 * c.x,
							   3.0 * a.y - 6.0 * b.y + 3.0 * c.y);
							   
			this.D = new Point(-a.x + 3.0 * b.x - 3.0 * c.x + d.x,
							   -a.y + 3.0 * b.y - 3.0 * c.y + d.y);	
		}
		
	    /**
	     * Calculates the location of a two-dimensional cubic Bezier curve at a specific time.
	     *
	     * @param t The <code>time</code> or degree of progress along the curve, as a decimal value between <code>0</code> and <code>1</code>.
	     * <p><strong>Note:</strong> The <code>t</code> parameter does not necessarily move along the curve at a uniform speed. For example, a <code>t</code> value of <code>0.5</code> does not always produce a value halfway along the curve.</p>
	     *
	     * @return A point object containing the x and y coordinates of the Bezier curve at the specified time. 
		*/
		public function getValue(t:Number):Point{
	
			return new Point(
				this.A.x + t * (this.B.x + t * (this.C.x + t * this.D.x)),
				this.A.y + t * (this.B.y + t * (this.C.y + t * this.D.y))
			);
		}
		
		public function getBoundingRectangle():Rectangle {
			var _minX:Number = Number.MAX_VALUE;
			var _minY:Number = Number.MAX_VALUE;
			var _maxX:Number = 0;
			var _maxY:Number = 0;
			
			_minX = Math.min(_point.x,_pos.x,_nextNeg.x,_nextPoint.x);
			_minY = Math.min(_point.y,_pos.y,_nextNeg.y,_nextPoint.y);
			_maxX = Math.max(_point.x,_pos.x,_nextNeg.x,_nextPoint.x);
			_maxY = Math.max(_point.y,_pos.y,_nextNeg.y,_nextPoint.y);
			
			return new Rectangle(_minX,_minY,_maxX-_minX,_maxY-_minY);
		}
		
		public function toString():String {
			var s:String = "BezierSegment:\n";
			
			s = s + "    A: " + A.toString() + "\n";
			s = s + "    B: " + B.toString() + "\n";
			s = s + "    C: " + C.toString() + "\n";
			s = s + "    D: " + D.toString() + "\n";
			
			return s;
		}
	}
}


