package com.sysbliss.diagram.data
{
    import com.sysbliss.diagram.ui.UIEdgeLabel;

    public interface EdgeLabel
	{	
		function get data():Object;
		function set data(value:Object):void;

        function get text():String;
        function set text(value:String):void;

		function set uiEdgeLabel(uiEdgeLabel:UIEdgeLabel):void;
		function get uiEdgeLabel():UIEdgeLabel;
	}
}