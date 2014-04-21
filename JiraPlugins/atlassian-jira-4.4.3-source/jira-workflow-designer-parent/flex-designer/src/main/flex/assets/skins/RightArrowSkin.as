
package assets.skins {
import flash.display.Graphics;

import mx.skins.Border;
import mx.skins.halo.HaloColors;
import mx.utils.ColorUtil;

public class RightArrowSkin extends Border
{

    private static var cache:Object = {};
    
	public function RightArrowSkin()
	{
		super();
	}

    override public function get measuredWidth():Number
    {
        return 10;
    }

    override public function get measuredHeight():Number
    {
        return 10;
    }

    private static function calcDerivedStyles(themeColor:uint,
											  borderColor:uint,
											  fillColor0:uint,
											  fillColor1:uint):Object
	{
		var key:String = HaloColors.getCacheKey(themeColor, borderColor,
												fillColor0, fillColor1);

		if (!cache[key])
		{
			var o:Object = cache[key] = {};

			// Cross-Component Styles
			HaloColors.addHaloColors(o, themeColor, fillColor0, fillColor1);

			// ScrollArrowUp-Unique Styles
			o.borderColorDrk1 = ColorUtil.adjustBrightness2(borderColor, -30);
		}

		return cache[key];
	}

	override protected function updateDisplayList(w:Number, h:Number):void
	{
		super.updateDisplayList(w, h);

		// User-defined styles.
		var bevel:Boolean = getStyle("bevel");
		var borderColor:uint = getStyle("borderColor");
		var radius:Number = getStyle("cornerRadius");
		var fillColors:Array = getStyle("fillColors");
		var themeColor:uint = getStyle("themeColor");

		// Placeholder styles stub.
		var arrowColor:uint = 0x111111;

        // Derived styles.
		var derStyles:Object = calcDerivedStyles(themeColor, borderColor,fillColors[0], fillColors[1]);

		var cornerRadius:Object = [ 0, 0, radius, radius ]; // tl, tr, bl, br
		var cornerRadius2:Array = [];
		cornerRadius2[0] = Math.max(cornerRadius[0] - 1, 0);
		cornerRadius2[1] = Math.max(cornerRadius[1] - 1, 0);
		cornerRadius2[2] = Math.max(cornerRadius[2] - 1, 0);
		cornerRadius2[3] = Math.max(cornerRadius[3] - 1, 0);

		var g:Graphics = graphics;

		g.clear();

		switch (name)
		{
			case "upSkin":
			{
				if (bevel)
				{
					// border
					drawRoundRect(
						0, 0, w, h,
						{ tl: cornerRadius[0], tr: cornerRadius[1],
						  bl: cornerRadius[2], br: cornerRadius[3] },
						[ borderColor,
						  derStyles.borderColorDrk1 ], 1,
						horizontalGradientMatrix(0, 0, w, h));

					// bevel highlight edge
					drawRoundRect(
						1, 1, w - 2, h - 2,
						{ tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
						[ derStyles.bevelHighlight1,
						  derStyles.bevelHighlight2 ], 1,
						horizontalGradientMatrix(1, 0, w - 2, h - 2));

					// fill
					drawRoundRect(
						2, 2, w - 3, h - 3,
					    { tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
					    [ fillColors[0], fillColors[1] ], 1,
					    horizontalGradientMatrix(1, 0, w - 3, h - 3));
				}
				else
				{
					// border
					drawRoundRect(
						0, 0, w, h,
						{ tl: cornerRadius[0], tr: cornerRadius[1],
						  bl: cornerRadius[2], br: cornerRadius[3] },
						borderColor, 1);

					// fill
					drawRoundRect(
						1, 1, w - 2, h - 2,
						{ tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
						[ fillColors[0], fillColors[1] ], 1,
						horizontalGradientMatrix(1, 0, w - 2, h - 2));
				}
				break;
			}

			case "overSkin":
			{
				if (bevel)
				{
					// border
					drawRoundRect(
						0, 0, w, h,
						{ tl: cornerRadius[0], tr: cornerRadius[1],
						  bl: cornerRadius[2], br: cornerRadius[3] },
						[ derStyles.themeColDrk2, derStyles.themeColDrk1 ], 1,
						horizontalGradientMatrix(0, 0, w, h));

					// bevel highlight edge
					drawRoundRect(
						1, 1, w - 2, h - 2,
						{ tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
						[ derStyles.bevelHighlight1,
						  derStyles.bevelHighlight2 ], 1,
						horizontalGradientMatrix(1, 0, w - 2, h - 2));

					// fill
					drawRoundRect(
						2, 2, w - 3, h - 3,
						{ tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
						[ derStyles.fillColorBright1,
						  derStyles.fillColorBright2 ], 1,
						horizontalGradientMatrix(1, 0, w - 3, h - 3));
				}
				else
				{
					// border
					drawRoundRect(
						0, 0, w, h,
						{ tl: cornerRadius[0], tr: cornerRadius[1],
						  bl: cornerRadius[2], br: cornerRadius[3] },
						derStyles.themeColDrk2, 1);

					// fill
					drawRoundRect(
						1, 1, w - 2, h - 2,
						{ tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
						[ derStyles.fillColorBright1,
						  derStyles.fillColorBright2 ], 1,
						horizontalGradientMatrix(1, 0, w - 2, h - 2));
				}
				break;
			}

			case "downSkin":
			{
				if (bevel)
				{
					// border
					drawRoundRect(
						0, 0, w, h,
						{ tl: cornerRadius[0], tr: cornerRadius[1],
						  bl: cornerRadius[2], br: cornerRadius[3] },
						[ derStyles.themeColDrk2, derStyles.themeColDrk1 ], 1,
						horizontalGradientMatrix(0, 0, w, h));

					// fill
					drawRoundRect(
						1, 1, w - 2, h - 2,
						{ tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
						[ derStyles.fillColorPress2,
						  derStyles.fillColorPress1 ], 1,
						horizontalGradientMatrix(1, 0, w, h));
				}
				else
				{
					// border
					drawRoundRect(
						0, 0, w, h,
						{ tl: cornerRadius[0], tr: cornerRadius[1],
						  bl: cornerRadius[2], br: cornerRadius[3] },
						derStyles.themeColDrk2, 1);

					// fill
					drawRoundRect(
						1, 1, w - 2, h - 2,
						{ tl: cornerRadius2[0], tr: cornerRadius2[1],
						  bl: cornerRadius2[2], br: cornerRadius2[3] },
						[ derStyles.fillColorPress2,
						  derStyles.fillColorPress1 ], 1,
						horizontalGradientMatrix(1, 0, w, h));
				}
				break;
			}

			default:
			{
				drawRoundRect(
					0, 0, w, h, 0,
					0xFFFFFF, 0);

				return;

				break;
			}
		}

		// Draw up arrow

        var pad:int = 3;
		g.beginFill(arrowColor);
		g.moveTo(w - pad, h / 2);
		g.lineTo(pad, (h / 2) - 5);
		g.lineTo(pad, (h / 2) + 5);
        g.lineTo(w - pad, h / 2);
		g.endFill();
	}
}

}