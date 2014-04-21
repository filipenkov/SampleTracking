/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 5/13/11
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.ui {
import flexlib.mdi.containers.MDIWindowControlsContainer;

public class StickyNoteControls extends MDIWindowControlsContainer {
    public function StickyNoteControls() {
        super();
    }

    override protected function createChildren():void
		{
			super.createChildren();

            removeChild(minimizeBtn);
            removeChild(maximizeRestoreBtn);
        }

}
}
