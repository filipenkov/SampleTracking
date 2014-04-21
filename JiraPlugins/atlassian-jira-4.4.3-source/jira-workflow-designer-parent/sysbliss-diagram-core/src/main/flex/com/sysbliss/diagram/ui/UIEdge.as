package com.sysbliss.diagram.ui
{
    import com.sysbliss.diagram.data.Edge;
    import com.sysbliss.diagram.geom.CubicBezierLine;
    import com.sysbliss.diagram.renderer.EdgeLabelRenderer;
    import com.sysbliss.diagram.renderer.EdgeRenderer;

    public interface UIEdge extends DiagramUIObject,CubicBezierLine
    {
        function get edgeRenderer():EdgeRenderer;

        function get edgeLabelRenderer():EdgeLabelRenderer;

        function get knotRendererClass():Class;

        function get edge():Edge;

        function set lineType(type:String):void;

        function get lineType():String;

        function removeSelectedControlPoints():void;

        function get uiEdgeLabel():UIEdgeLabel;

        function set uiEdgeLabel(uiEdgeLabel:UIEdgeLabel):void;

        function createLabel():void;

        function updateLabelPosition():void;

        function updateLabel():void;

        function renderAsRecursive():void;

        function highlightEdge():void;

        function unhighlightEdge():void;
    }
}