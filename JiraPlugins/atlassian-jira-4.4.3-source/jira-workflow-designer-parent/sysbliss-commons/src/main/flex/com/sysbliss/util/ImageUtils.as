package com.sysbliss.util
{
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.geom.Matrix;
	
	public class ImageUtils
	{
		public static function resizeBitmap(bitmap:Bitmap, maxWidth:Number, maxHeight:Number):void
		{
			var bitmapData:BitmapData = bitmap.bitmapData;
			var scaleFactor:Number = 1;
			
			var newWidth:Number = maxWidth;
			var newHeight:Number = maxHeight;
			
			if(bitmapData.width > bitmapData.height) {
				scaleFactor = maxWidth / bitmapData.width;
			}
			else {
				scaleFactor = maxHeight / bitmapData.height;
			}
			
			newWidth = bitmapData.width * scaleFactor;
			newHeight = bitmapData.height * scaleFactor;
			
			var scaledBitmapData:BitmapData = new BitmapData(newWidth, newHeight);
			var scaleMatrix:Matrix = new Matrix();
			scaleMatrix.scale(scaleFactor, scaleFactor);
			
			scaledBitmapData.draw(bitmap, scaleMatrix);
			bitmap.bitmapData = scaledBitmapData;
		} 

	}
}