package assets.skins
{
	import flash.display.BitmapData;
	
	import mx.core.BitmapAsset;
	import mx.skins.ProgrammaticSkin;

	public class ApplicationSkin extends ProgrammaticSkin
	{
		public function ApplicationSkin()
		{
			super();
		}
		
		override protected function updateDisplayList(unscaledWidth:Number,    unscaledHeight:Number):void
		{
			graphics.clear();
			var backgroundColor:uint = getStyle("backgroundColor");
			var backgroundAlpha:Number = 1.0;
			graphics.beginFill(backgroundColor, backgroundAlpha);
			graphics.drawRect(0, 0, unscaledWidth, unscaledHeight);
			
			
			var backgroundImageClass:* = getStyle("backgroundImage");
			var backgroundRepeat:* = getStyle("backgroundRepeat");
			var backgroundImage:BitmapAsset;
			var bitmapData:BitmapData;
			if(backgroundImageClass && backgroundRepeat == "repeat-x")
			{
				backgroundImage = new backgroundImageClass();
				bitmapData = backgroundImage.bitmapData;
				
				graphics.beginBitmapFill(bitmapData,null,true);
				graphics.drawRect(0, 0,unscaledWidth,backgroundImage.height);
				graphics.endFill();
			} else if(backgroundImageClass && backgroundRepeat == "repeat-xy")
			{
				backgroundImage = new backgroundImageClass();
				bitmapData = backgroundImage.bitmapData;
				
				graphics.beginBitmapFill(bitmapData,null,true);
				graphics.drawRect(0, 0,unscaledWidth,unscaledHeight);
				graphics.endFill();
			}
		}
	}
}