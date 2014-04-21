package assets.skins
{
	import mx.skins.ProgrammaticSkin;
	import flash.display.Graphics;
	public class NullSkin extends ProgrammaticSkin
	{
		public function NullSkin()
		{
			super();
		}
		
		override protected function updateDisplayList(w:Number, h:Number):void
		{
			var g:Graphics = graphics;
	        g.clear();
	        g.beginFill(0xffffff,0.0);
	        g.lineStyle(0,0xffffff,0);
	        g.drawRect(0, 0, 0, 0);
	        g.endFill();
	 }
		
	}
}
