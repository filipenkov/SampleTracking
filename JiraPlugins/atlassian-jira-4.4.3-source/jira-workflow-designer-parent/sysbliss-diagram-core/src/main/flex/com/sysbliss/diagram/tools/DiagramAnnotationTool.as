/**
 * Created by IntelliJ IDEA.
 * User: jdoklovic
 * Date: 5/12/11
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.tools {
import flash.ui.MouseCursor;

public class DiagramAnnotationTool extends DiagramToolImpl implements DiagramTool {

    public function DiagramAnnotationTool() {
        super();
			_name = "annotationTool";
			_toolTip = niceResourceManager.getString('json','workflow.designer.create.sticky.note');
			_cursorDisplay = DiagramToolImpl.CURSOR_REPLACE;
    }
}
}
