/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 5/12/11
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram {
import flash.events.MouseEvent;

import mx.core.FlexSprite;

public class ModalLayer extends DiagramLayer {

    public function ModalLayer(name:String) {
        super(name);
        this.mouseEnabled = true;
        addEventListener(MouseEvent.CLICK,eatMouseEvent,true);
        addEventListener(MouseEvent.DOUBLE_CLICK,eatMouseEvent,true);
        addEventListener(MouseEvent.MOUSE_DOWN,eatMouseEvent,true);
        addEventListener(MouseEvent.MOUSE_MOVE,eatMouseEvent,true);
        addEventListener(MouseEvent.MOUSE_OUT,eatMouseEvent,true);
        addEventListener(MouseEvent.MOUSE_OVER,eatMouseEvent,true);
        addEventListener(MouseEvent.MOUSE_UP,eatMouseEvent,true);
        addEventListener(MouseEvent.MOUSE_WHEEL,eatMouseEvent,true);
        addEventListener(MouseEvent.ROLL_OUT,eatMouseEvent,true);
        addEventListener(MouseEvent.ROLL_OVER,eatMouseEvent,true);

    }

    private function eatMouseEvent(e:MouseEvent):void {
        e.stopPropagation();
        e.stopImmediatePropagation();
    }
}
}
