package com.sysbliss.diagram
{
	import flash.geom.Point;

import mx.containers.Canvas;
import mx.core.LayoutContainer;
	import mx.core.ScrollPolicy;

	public class DiagramLayer extends LayoutContainer
	{
		//initial width and height are 1024x768 * 3
        public static var CANVAS_WIDTH:Number = 3072;
        public static var CANVAS_HEIGHT:Number = 2304;

		private var _moveScrollPosition:Boolean;
		private var _offsetSize:Point;
		
		public function DiagramLayer(layerName:String)
		{
			super();
            this.name = layerName;
			this.layout = "absolute";
			this.mouseEnabled = false;
			this.horizontalScrollPolicy = ScrollPolicy.OFF;
			this.verticalScrollPolicy = ScrollPolicy.OFF;


            this.width = CANVAS_WIDTH;
            this.height = CANVAS_HEIGHT;
		}
		
	}
}