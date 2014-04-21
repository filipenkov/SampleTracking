/**
 * Created by IntelliJ IDEA.
 * User: rshuttleworth
 * Date: 10/03/11
 * Time: 12:11 PM
 * To change this template use File | Settings | File Templates.
 */
package com.sysbliss.diagram.ui
{
    import com.sysbliss.diagram.data.EdgeLabel;
    import com.sysbliss.diagram.renderer.EdgeLabelRenderer;

    public interface UIEdgeLabel extends DiagramUIObject
    {
        function get edgeLabelRenderer():EdgeLabelRenderer;
        function get edgeLabel():EdgeLabel;

        function highlightEdgeLabel():void;

        function unhighlightEdgeLabel():void;
    }
}
