package com.sysbliss.diagram.extras
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.event.DiagramEvent;
	import com.sysbliss.util.ImageUtils;
	
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.geom.Rectangle;
	
	import mx.containers.Canvas;
	import mx.controls.Image;
	import mx.core.ScrollPolicy;

	public class DiagramNavigator extends Canvas
	{
		private var _diagram:Diagram;
		private var _bmp:Bitmap;
		private var _navigatorImg:Image;
		private var _mask:Canvas;
		private var _rect:Rectangle;
		private var _isDragging:Boolean = false;
		
		public function DiagramNavigator()
		{
			super();
			this.percentWidth = 100;
			this.percentHeight = 100;
			this.horizontalScrollPolicy = ScrollPolicy.OFF;
			this.verticalScrollPolicy = ScrollPolicy.OFF;
		}
		
		override protected function createChildren():void {
			super.createChildren();
			
			this._navigatorImg = new Image();
			_navigatorImg.percentWidth = 100;
			_navigatorImg.percentHeight = 100;
			_navigatorImg.maintainAspectRatio = false;
			this.addChild(_navigatorImg);
			
			createNavigatorMask();
			
		}
		
		public function updateSnapshot():void {
			createNavigatorImg();
		}
		
		private function createNavigatorImg():void {
			var dobj:DisplayObject = DisplayObject(_diagram.contentLayer);
			
			var origW:Number = _diagram.contentLayer.width / Math.abs(_diagram.contentLayer.scaleX);
			var origH:Number = _diagram.contentLayer.height / Math.abs(_diagram.contentLayer.scaleY);
			
			try {
	            var data:BitmapData = new BitmapData(origW,origH);
	            data.draw(dobj);
	            
	            _bmp = new Bitmap(data);
	  		}catch (e:Error){
	  			return;
	  		}
            
            ImageUtils.resizeBitmap(_bmp,_navigatorImg.width,_navigatorImg.height);
            _navigatorImg.maintainAspectRatio = false;
            _navigatorImg.source = _bmp;
            
            
            updateNavigatorMask();
				
            invalidateDisplayList();
            
		}
		
		public function updateNavigatorMask():void {
        	var scrollX:Number = _diagram.horizontalScrollPosition;
        	var scrollY:Number = _diagram.verticalScrollPosition;   	
			var lDiff:Number = _diagram.contentLayer.x+scrollX;
			var tDiff:Number = _diagram.contentLayer.y+scrollY;
			var mwidth:Number = _diagram.visibleRectangle.width/(_diagram.contentLayer.width/_navigatorImg.width);
			var mheight:Number = _diagram.visibleRectangle.height/(_diagram.contentLayer.height/_navigatorImg.height);
			var mx:Number = scrollX/(_diagram.contentLayer.width/_navigatorImg.width);
			var my:Number = scrollY/(_diagram.contentLayer.height/_navigatorImg.height);
			
			_mask.width = mwidth;
			_mask.height = mheight;			
			_mask.x = mx;
			_mask.y = my;
			_rect.width = _navigatorImg.width-(mwidth);	
			_rect.height = _navigatorImg.height-(mheight);
        }
		
		private function createNavigatorMask():void {  
			this._mask = new Canvas();
			_mask.setStyle("backgroundColor",0xE5B421);
		    _rect = new Rectangle(0, 0, 100, 100);  					  	
			//_mask.graphics.beginFill(0xcc0000);
			//_mask.graphics.drawRect(0, 0, 100, 100);
			assignMaskHandlers();		
			_mask.useHandCursor = true;				   
			_mask.alpha = 0.5;
			
			this.addChild(_mask);
        }
        
        private function assignMaskHandlers():void {
            _mask.addEventListener(MouseEvent.MOUSE_DOWN, mouseDownHandler);
			_mask.addEventListener(MouseEvent.MOUSE_UP, mouseUpHandler);				
			_mask.addEventListener(MouseEvent.MOUSE_MOVE, mouseMoveHandler);
        }
		
		private function mouseDownHandler(event:MouseEvent):void {             	
        	 event.currentTarget.startDrag(false, _rect);
        	 _isDragging = true;
        } 
        
        private function mouseUpHandler(event:MouseEvent):void { 
        	 event.currentTarget.stopDrag();
        	 _isDragging = false;
        } 
        
        private function mouseMoveHandler(event:MouseEvent):void { 
        	 if(_isDragging){
        	 	updateDiagram();
        	 }
        }
        
        private function updateDiagram():void {
			var x:Number = (_mask.x*(_diagram.contentLayer.width/_bmp.width))-_diagram.contentLayer.x;
			var y:Number = (_mask.y*(_diagram.contentLayer.height/_bmp.height))-_diagram.contentLayer.y;
			_diagram.horizontalScrollPosition = x;
			_diagram.verticalScrollPosition = y;				
        }
            
		private function onLayoutFinished(e:Event):void {
			updateSnapshot();
		}
		
		private function onDiagramChanged(e:Event):void {
			updateSnapshot();
		}
		
		private function onDiagramZoomed(e:Event):void {
			//updateNavigatorMask();
			updateSnapshot();
		}
		
		public function setDiagram(d:Diagram):void {
			this._diagram = d;
			this._diagram.addEventListener(DiagramEvent.DIAGRAM_CHANGED,onDiagramChanged);
			this._diagram.addEventListener(DiagramEvent.DIAGRAM_ZOOMED,onDiagramZoomed);
		}
	}
}