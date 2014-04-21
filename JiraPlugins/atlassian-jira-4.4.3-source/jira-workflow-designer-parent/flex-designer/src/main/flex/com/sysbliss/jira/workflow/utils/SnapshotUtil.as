package com.sysbliss.jira.workflow.utils {
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRectImpl;

import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.DisplayObject;
import flash.geom.Point;
import flash.geom.Rectangle;

public class SnapshotUtil {
    public function SnapshotUtil() {
    }

    public static function getCroppedBitmap(target:DisplayObject):Bitmap {
        var bitmap:Bitmap = new Bitmap();
        var origW:Number = target.width / Math.abs(target.scaleX);
        var origH:Number = target.height / Math.abs(target.scaleY);

        try {
            var data:BitmapData = new BitmapData(origW, origH);
            data.draw(target);

            var imgBounds:Rectangle = data.getColorBoundsRect(0xFFFFFFFF, 0xFFFFFFFF, false);

            var cropped:BitmapData = new BitmapData(imgBounds.width, imgBounds.height);
            cropped.copyPixels(data, imgBounds, new Point(0, 0));

            bitmap = new Bitmap(cropped);


        } catch (e:Error) {
            return bitmap;
        }

        return bitmap;
    }

    public static function getGraphBounds(target:DisplayObject):LayoutRect {
        var bitmap:Bitmap = new Bitmap();
        var origW:Number = target.width / Math.abs(target.scaleX);
        var origH:Number = target.height / Math.abs(target.scaleY);
        var returnRect:LayoutRect = new LayoutRectImpl();
        returnRect.width = origW;
        returnRect.height = origH;
        returnRect.x = 0;
        returnRect.y = 0;

        try {
            var data:BitmapData = new BitmapData(origW, origH);
            data.draw(target);

            var imgBounds:Rectangle = data.getColorBoundsRect(0xFFFFFFFF, 0xFFFFFFFF, false);

            returnRect.width = imgBounds.width;
            returnRect.height = imgBounds.height;

        } catch (e:Error) {
            //do nothing
        }

        return returnRect;
    }

    public static function getFullBitmap(target:DisplayObject):Bitmap {
        var bitmap:Bitmap = new Bitmap();
        var origW:Number = target.width / Math.abs(target.scaleX);
        var origH:Number = target.height / Math.abs(target.scaleY);

        try {
            var data:BitmapData = new BitmapData(origW, origH);
            data.draw(target);

            bitmap = new Bitmap(data);


        } catch (e:Error) {
            return bitmap;
        }

        return bitmap;
    }

}
}