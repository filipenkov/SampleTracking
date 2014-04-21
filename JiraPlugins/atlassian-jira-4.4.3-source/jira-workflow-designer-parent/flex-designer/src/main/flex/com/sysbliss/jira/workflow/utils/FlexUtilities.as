package com.sysbliss.jira.workflow.utils
{
import flash.display.DisplayObject;

import flash.geom.Point;

import flash.geom.Rectangle;

import mx.core.Application;
import mx.core.UIComponent;

public class FlexUtilities
{
    public function FlexUtilities()
    {
    }

    public static function getVisibleBounds(component:UIComponent):Rectangle {
    var r:Rectangle = new Rectangle();

    var step:Number = 50;

    var best:Rectangle = new Rectangle();

    var yOffset:Number = 0;

    // Find largest bounding area
    do {
        nextBounds(component, yOffset, step, r);
        yOffset += r.y + r.height + step;

        if (r.width * r.height > best.width * best.height) {
            best.x = r.x;
            best.y = r.y;
            best.width = r.width;
            best.height = r.height;
        }
    } while (r.x != -1);

    // Expand bounds broadly
    expandBounds(component, best, step, step);

    // Expand bounds narrowly
    expandBounds(component, best, 1, step);

    component.graphics.clear();
    component.graphics.beginFill(0xffffff);
    component.graphics.drawRect(best.x, best.y, best.width, best.height);
    component.graphics.endFill();

    return best;
}

private static function expandBounds(component:UIComponent, r:Rectangle, step:int, stepJump:int):void {
    // Look up
    while (validateHorizontal(component, r.x, r.y - step, r.width, stepJump)) {
        r.y -= step;
        r.height += step;
    }

    // Look down
    while (validateHorizontal(component, r.x, r.y + r.height + step, r.width, stepJump)) {
        r.height += step;
    }

    // Look left
    while (validateVertical(component, r.x - step, r.y, r.height, stepJump)) {
        r.x -= step;
        r.width += step;
    }

    // Look right
    while (validateVertical(component, r.x + r.width + step, r.y, r.height, stepJump)) {
        r.width += step;
    }
}

private static function validateHorizontal(component:UIComponent, x:int, y:int, width:int, step:int):Boolean {
    for (var i:int = x; i <= width; i += step) {
        if (!isUnderPoint(component, i, y)) {
            return false;
        }
    }
    if (isUnderPoint(component, x, y)) {
        if (isUnderPoint(component, x + width, y)) {
            return true;
        }
    }
    return false;
}

private static function validateVertical(component:UIComponent, x:int, y:int, height:int, step:int):Boolean {
    for (var i:int = y; i <= height; i += step) {
        if (!isUnderPoint(component, x, i)) {
            return false;
        }
    }
    if (isUnderPoint(component, x, y)) {
        if (isUnderPoint(component, x, y + height)) {
            return true;
        }
    }
    return false;
}

private static function nextBounds(component:UIComponent, yOffset:Number, step:int, r:Rectangle):Rectangle {
    r.x = -1;
    r.y = -1;
    r.width = -1;
    r.height = 0;

    var p:Point = findFirstVisible(component, yOffset, step);
    if (p != null) {
        r.x = p.x;
        r.y = p.y;
        for (var y:int = p.y + step; y <= component.height; y += step) {
            var currentWidth:Number = 0;
            for (var x:int = p.x + step; x <= component.width; x += step) {
                if (isUnderPoint(component, x, y)) {
                    currentWidth += step;
                }
            }
            if (r.width == -1) {
                r.width = currentWidth;
            } else if (r.width > currentWidth) {
                return r;
            }
            r.height += step;
        }
    }

    return r;
}

private static function findFirstVisible(component:UIComponent, yOffset:Number, step:int):Point {
    for (var y:int = yOffset; y <= component.height; y += step) {
        for (var x:int = 0; x <= component.width; x += step) {
            if (isUnderPoint(component, x, y)) {
                return new Point(x, y);
            }
        }
    }
    return null;
}

private static var underPoint:Point = new Point();
public static function isUnderPoint(component:UIComponent, localX:Number, localY:Number):Boolean {
    underPoint.x = localX + 1;
    underPoint.y = localY;
    underPoint = component.localToGlobal(underPoint);
    var a:Array = Application.application.stage.getObjectsUnderPoint(underPoint);
    for (var i:int = a.length - 1; i >= 0; i--) {
        var c:UIComponent = getComponent(a[i]);
        if (c != null) {
            if (c.mouseEnabled) {
                return c == component;
            }
        }
    }
    return false;
}

public static function getComponent(obj:DisplayObject):UIComponent {
    while (obj != null) {
        if (obj is UIComponent) {
            return UIComponent(obj);
        }
        obj = obj.parent;
    }
    return null;
}
}
}