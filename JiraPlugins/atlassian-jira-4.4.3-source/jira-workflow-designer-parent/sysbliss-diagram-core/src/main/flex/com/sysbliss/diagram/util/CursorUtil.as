/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 5/16/11
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.util {
import flash.ui.Mouse;

import mx.managers.CursorManager;
import mx.managers.CursorManagerPriority;

public class CursorUtil {

    [Embed('/assets/cursors/link-cursor.png')]
    public static var CURSOR_LINK:Class;

    [Embed('/assets/cursors/open-hand.png')]
    public static var CURSOR_OPEN_HAND:Class;

    [Embed('/assets/cursors/closed-hand.png')]
    public static var CURSOR_CLOSED_HAND:Class;

    [Embed('/assets/cursors/cross-hairs.png')]
    public static var CURSOR_CROSS_HAIRS:Class;

    protected static var currentCursorId:int = -1;

    public static var linkAsPointer:Boolean = false;

    public static var isCustomShowing:Boolean = false;


    public function CursorUtil() {
    }

    public static function showPointer():void {
        CursorManager.removeAllCursors();

        if (linkAsPointer) {
            showLink();
        } else {
            Mouse.show();
            currentCursorId = -1;
            isCustomShowing = false;
        }
    }

    public static function forcePointer():void {
        CursorManager.removeAllCursors();
        Mouse.show();
    }

    public static function showOpenHand():void {
        CursorManager.removeAllCursors();
        Mouse.hide();
        currentCursorId = CursorManager.setCursor(CURSOR_OPEN_HAND, CursorManagerPriority.HIGH);
        isCustomShowing = true;
    }

    public static function showClosedHand():void {
        CursorManager.removeAllCursors();
        Mouse.hide();
        currentCursorId = CursorManager.setCursor(CURSOR_CLOSED_HAND, CursorManagerPriority.HIGH);
        isCustomShowing = true;
    }

    public static function showCrossHairs():void {
        CursorManager.removeAllCursors();
        Mouse.hide();
        currentCursorId = CursorManager.setCursor(CURSOR_CROSS_HAIRS, CursorManagerPriority.HIGH, -7, -7);
        isCustomShowing = true;
    }

    public static function showLink():void {
        CursorManager.removeAllCursors();
        Mouse.hide();
        currentCursorId = CursorManager.setCursor(CURSOR_LINK, CursorManagerPriority.HIGH, 0, -5);
        isCustomShowing = true;
    }
}
}
