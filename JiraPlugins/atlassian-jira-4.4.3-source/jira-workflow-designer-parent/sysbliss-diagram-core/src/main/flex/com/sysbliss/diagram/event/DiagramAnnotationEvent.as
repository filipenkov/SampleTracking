/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 6/10/11
 * Time: 3:04 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.event {
import com.sysbliss.diagram.ui.StickyNote;

import flash.events.Event;

public class DiagramAnnotationEvent extends Event {

    public var stickyNote:StickyNote;
    public function DiagramAnnotationEvent(type:String, sticky:StickyNote, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.stickyNote = sticky;
		}

		override public function clone():Event {
			return new DiagramAnnotationEvent(type, stickyNote, bubbles, cancelable);
		}
}
}
