package com.sysbliss.diagram.ui
{
    import com.sysbliss.diagram.Diagram;
    import com.sysbliss.diagram.ToolTypes;
    import com.sysbliss.diagram.data.Edge;
    import com.sysbliss.diagram.event.SelectableEvent;
    import com.sysbliss.diagram.geom.CubicBezier;
    import com.sysbliss.diagram.geom.CubicBezierLine;
    import com.sysbliss.diagram.geom.Line;
    import com.sysbliss.diagram.geom.PolyLine;
    import com.sysbliss.diagram.geom.SegmentedLine;
    import com.sysbliss.diagram.geom.SimpleLine;
    import com.sysbliss.diagram.manager.ControllerKnotManager;
    import com.sysbliss.diagram.manager.EdgeControlPointManager;
import com.sysbliss.diagram.renderer.AbstractEdgeRenderer;
import com.sysbliss.diagram.renderer.EdgeRenderer;
    import com.sysbliss.diagram.ui.selectable.SelectionManager;
    import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
    import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
    import com.sysbliss.util.PointUtils;

    import flash.display.IGraphicsData;
    import flash.geom.Point;
    import flash.geom.Rectangle;

    import mx.controls.Label;
    import mx.core.UIComponent;

    public class DefaultUIEdge extends AbstractUIEdge
    {

        protected var _knotSelectionManager:SelectionManager;
        protected var _controllerKnots:Vector.<UIKnot>;
        protected var _selectedControlPoint:Point;
        protected var _selectedControlPointChanged:Boolean;
        protected var _controllerMoved:Boolean;
        protected var _selectedChanged:Boolean;
        protected var _oldBoundingRectCenter:Point;

        public function DefaultUIEdge(diagram:Diagram, edge:Edge, edgeRenderer:EdgeRenderer, edgeLabelRendererClass:Class, knotRendererClass:Class, controlPointManager:EdgeControlPointManager)
        {
            super(diagram, edge, edgeRenderer, edgeLabelRendererClass, knotRendererClass, controlPointManager);
            this._knotSelectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.EDGE_CONTROLS + "_" + UIComponent(diagram).uid);
            this._controllerKnots = ControllerKnotManager.getControllerKnots(diagram);

            createLabel();
        }

        override protected function commitProperties():void
        {
            if (_lineTypeChanged || _line == null)
            {
                convertPointsToLocal();
                switch (lineType)
                {
                    case ToolTypes.LINK_STRAIGHT.name :
                        _line = new SimpleLine(_localStartPoint, _localEndPoint);
                        break;
                    case ToolTypes.LINK_POLY.name :
                        _line = new PolyLine(_localStartPoint, _localEndPoint, _localControlPoints);
                        break;
                    case ToolTypes.LINK_BEZIER.name :
                        _line = new CubicBezier(_localStartPoint, _localEndPoint, _localControlPoints);
                        break;
                }

                hidePointControls();
                _controlPointManager.hideControls();
                if (isSelected && _selectionManager.numSelected == 1)
                {
                    _controlPointManager.showControls(this);
                }
            }

            var r:Rectangle = _line.getBoundingRectangle();
            if(_startPoint.y < _endPoint.y) {
                _oldBoundingRectCenter = new Point(r.x + (r.width / 2), (r.y + ((r.height / 2)/2)));
            } else {
                _oldBoundingRectCenter = new Point(r.x + (r.width / 2), (r.y + ((r.height / 4)*3)));
            }


            if (_startPointChanged)
            {
                convertStartPointToLocal();
                _line.startPoint = _localStartPoint;
            }

            if (_startPointMoved)
            {
                convertStartPointToLocal();
                _line.moveStartPoint(_localStartPoint.x, _localStartPoint.y);
            }

            if (_endPointChanged)
            {
                convertEndPointToLocal();
                _line.endPoint = _localEndPoint;
                if (isSelected)
                {
                    _controlPointManager.showControls(this);
                }
            }

            if (_endPointMoved)
            {
                convertEndPointToLocal();
                _line.moveEndPoint(_localEndPoint.x, _localEndPoint.y);
            }

            if (_endPointPreviewMoved)
            {
                convertEndPointToLocal();
                _line.moveEndPointPreview(_localEndPoint.x, _localEndPoint.y);
            }

            if (_controlPointsChanged && (_line is SegmentedLine))
            {
                convertControlPointsToLocal();
                var segLine:SegmentedLine = _line as SegmentedLine;
                segLine.controlPoints = _localControlPoints;
            }

            if (_controlPointMoved && (_line is SegmentedLine))
            {
                var movedControlPoint:Point = PointUtils.convertCoordinates(_controlPoints[_editedControlPointIndex], diagram.edgeLayer, this);

                if ((_line is CubicBezierLine))
                {
                    var negControl:UIControlPointController = _controllerKnots[0] as UIControlPointController;
                    var posControl:UIControlPointController = _controllerKnots[1] as UIControlPointController;
                    var diffX:Number = (movedControlPoint.x - _localControlPoints[_editedControlPointIndex].x);
                    var diffY:Number = (movedControlPoint.y - _localControlPoints[_editedControlPointIndex].y);

                    negControl.x = (negControl.x + diffX);
                    negControl.y = (negControl.y + diffY);
                    posControl.x = (posControl.x + diffX);
                    posControl.y = (posControl.y + diffY);
                }
                SegmentedLine(_line).moveControlPoint(_localControlPoints[_editedControlPointIndex], movedControlPoint.x, movedControlPoint.y);
            }

            if (_controlPointAdded && (_line is SegmentedLine))
            {
                var addedControlPoint:Point = PointUtils.convertCoordinates(_controlPoints[(_controlPoints.length - 1)], diagram.edgeLayer, this);
                SegmentedLine(_line).addControlPoint(addedControlPoint);
            }

            if (_controlPointInserted && (_line is SegmentedLine))
            {
                var insertedControlPoint:Point = PointUtils.convertCoordinates(_controlPoints[_editedControlPointIndex], diagram.edgeLayer, this);
                SegmentedLine(_line).insertControlPointAt(insertedControlPoint, _editedControlPointIndex);
                _controlPointManager.hideControls();
                _controlPointManager.showControls(this);
            }

            if (_controlPointRemoved && (_line is SegmentedLine))
            {
                SegmentedLine(_line).removeControlPointAt(_editedControlPointIndex);
            }

            invalidateSize();

            if (propChanged())
            {
                invalidateDisplayList();
            }
        }

        override protected function propChanged():Boolean
        {
            return (super.propChanged() || _selectedControlPointChanged || _controllerMoved || _selectedChanged);
        }

        override protected function resetPropertyFlags():void
        {
            super.resetPropertyFlags();
            _selectedControlPointChanged = false;
            _controllerMoved = false;
            _selectedChanged = false;
        }

        override public function set lineType(type:String):void
        {
            if (type == ToolTypes.LINK_POLY.name || type == ToolTypes.LINK_BEZIER.name)
            {
                if (_controlPoints.length < 1)
                {
                    var newPoint:Point = findDefaultPositionForFirstControlPoint();
                    _controlPoints.push(newPoint);
                }
            }
            super.lineType = type;
        }

        override public function select(quiet:Boolean = false):void
        {
            _selectedChanged = true;
            super.select(quiet);
            _knotSelectionManager.addEventListener(SelectableEvent.SELECTED, onKnotSelected);
            _knotSelectionManager.addEventListener(SelectableEvent.DESELECTED, onKnotDeSelected);
            _knotSelectionManager.addEventListener(SelectableEvent.DESELECT_ALL, onKnotDeSelected);
        }

        override public function deselect(quiet:Boolean = false):void
        {

            super.deselect(quiet);
            _selectedChanged = true;
            _knotSelectionManager.removeEventListener(SelectableEvent.SELECTED, onKnotSelected);
            _knotSelectionManager.removeEventListener(SelectableEvent.DESELECTED, onKnotDeSelected);
            _knotSelectionManager.removeEventListener(SelectableEvent.DESELECT_ALL, onKnotDeSelected);
            if (_line is CubicBezier)
            {
                hidePointControls();
            }

            _isSelected = false;

            //need to make sure we're not highlighted
            edgeRenderer.lineColor = AbstractEdgeRenderer.DEFAULT_COLOR;
            edgeRenderer.draw(graphics, _line, this);

            invalidateDisplayList();
            validateNow();

        }

        override protected function measure():void {
            if(edgeRenderer) {
                var lineRect:Rectangle = _line.getBoundingRectangle();
                measuredMinWidth = measuredWidth = width = lineRect.width;
                measuredMinHeight = measuredHeight = height = lineRect.height;

            }
        }

        override protected function updateDisplayList(w:Number, h:Number):void
        {

            super.updateDisplayList(w, h);


            if (edgeRenderer)
            {
                graphics.clear();
                if (isSelected)
                {
                    edgeRenderer.drawSelected(graphics, _line, this, _selectedControlPoint);
                } else
                {
                    edgeRenderer.draw(graphics, _line, this);
                }

            }

            updateLabelPosition();


            if (propChanged())
            {
                resetPropertyFlags();
            }

        }

        override public function updateLabelPosition():void {
            if (_uiEdgeLabel != null && _line != null && (_edgeChanged
                    || _lineTypeChanged
                    || _startPointChanged
                    || _endPointChanged
                    || _controlPointsChanged
                    || _startPointMoved
                    || _endPointMoved
                    || _endPointPreviewMoved
                    || _controlPointMoved
                    || _controlPointAdded
                    || _controlPointInserted
                    || _controlPointRemoved
                    || (_uiEdgeLabel.x == -200)))
            {
                var r:Rectangle = _line.getBoundingRectangle();
                var rectCenter:Point
                if(_startPoint.y < _endPoint.y) {
                   rectCenter = new Point(r.x + (r.width / 2), (r.y + ((r.height / 2)/2)));
                } else {
                    rectCenter = new Point(r.x + (r.width / 2), (r.y + ((r.height / 4)*3)));
                }

                var convertedCenter:Point = PointUtils.convertCoordinates(rectCenter, this, _diagram.labelLayer);
                var oldConvertedCenter:Point = PointUtils.convertCoordinates(_oldBoundingRectCenter, this, _diagram.labelLayer);
                var offset:Point = new Point((convertedCenter.x - oldConvertedCenter.x), (convertedCenter.y - oldConvertedCenter.y));
                if (_uiEdgeLabel.x == -200)
                {
                    if(_line is CubicBezier) {
                        var bezier:CubicBezier = _line as CubicBezier;
                        var linePoint:Point = bezier.closestPointToPoint(convertedCenter);
                        _uiEdgeLabel.move((linePoint.x - (_uiEdgeLabel.width / 2)), (convertedCenter.y - (_uiEdgeLabel.height / 2)));
                    } else {
                        _uiEdgeLabel.move((convertedCenter.x - (_uiEdgeLabel.width / 2)), (convertedCenter.y - (_uiEdgeLabel.height / 2)));
                    }

                } else
                {
                    _uiEdgeLabel.move((_uiEdgeLabel.x + offset.x), (_uiEdgeLabel.y + offset.y));
                    //adjustLabelCollision();
                }
            }
        }

        public function adjustLabelCollision():void {
            var allLabels:Array = _diagram.labelLayer.getChildren();
            var i:int;
            var currentLabel:UIEdgeLabel;
            var myBounds:Rectangle = _uiEdgeLabel.getRect(_diagram.labelLayer);
            var currentBounds:Rectangle;

            for(i=0;i<allLabels.length;i++) {
                currentLabel = allLabels[i] as UIEdgeLabel;
                if(currentLabel == null || currentLabel == _uiEdgeLabel)
                {
                    continue;
                }

                currentBounds = currentLabel.getRect(_diagram.labelLayer);
                if(myBounds.intersects(currentBounds))
                {
                    _uiEdgeLabel.move(_uiEdgeLabel.x, ((currentLabel.y + currentLabel.height) + 10));
                }
            }
        }

        override public function getGraphicsData():Vector.<IGraphicsData>
        {
            if (_line)
            {
                return _line.getGraphicsData();
            } else
            {
                return null;
            }
        }

        protected function onKnotSelected(e:SelectableEvent):void
        {
            if (_line is CubicBezier)
            {
                var knot:UIControlPoint = e.data as UIControlPoint;
                if (knot.point != _startPoint && knot.point != _endPoint)
                {
                    showPointControls(knot.point);
                }
            }
        }

        protected function onKnotDeSelected(e:SelectableEvent):void
        {
            if (_line is CubicBezier)
            {
                hidePointControls();
            }
        }

        private function showPointControls(p:Point):void
        {
            var pointIndex:int = _controlPoints.indexOf(p);
            var localPoint:Point = _localControlPoints[pointIndex];
            _selectedControlPoint = localPoint;
            _selectedControlPointChanged = true;

            var bezier:CubicBezier = _line as CubicBezier;
            var neg:Point = bezier.getNegativeControllerForPoint(localPoint);
            var pos:Point = bezier.getPositiveControllerForPoint(localPoint);
            var dNeg:Point = PointUtils.convertCoordinates(neg, this, _diagram.controlsLayer);
            var dPos:Point = PointUtils.convertCoordinates(pos, this, _diagram.controlsLayer);

            var negControl:UIControlPointController = _controllerKnots[0] as UIControlPointController;
            var posControl:UIControlPointController = _controllerKnots[1] as UIControlPointController;

            negControl.uiEdge = this;
            negControl.point = p;
            negControl.sibling = posControl;
            negControl.x = dNeg.x;
            negControl.y = dNeg.y;
            negControl.isNegative = true;
            negControl.isPositive = false;

            posControl.uiEdge = this;
            posControl.point = p;
            posControl.sibling = negControl;
            posControl.x = dPos.x;
            posControl.y = dPos.y;
            posControl.isNegative = false;
            posControl.isPositive = true;

            negControl.visible = true;
            posControl.visible = true;

            //need to invalidate and validate to get handle rotation
            negControl.invalidateDisplayList();
            negControl.validateNow();

            posControl.invalidateDisplayList();
            posControl.validateNow();

            //need to invalidate and validate to get line drawn
            invalidateDisplayList();
            validateNow();
        }

        private function hidePointControls():void
        {
            var needsRedraw:Boolean = (_selectedControlPoint != null);
            _selectedControlPoint = null;
            _selectedControlPointChanged = true;

            _controllerKnots[0].visible = false;
            _controllerKnots[1].visible = false;

            if (needsRedraw)
            {
                invalidateDisplayList();
                validateNow();
            }
        }

        override public function getPositiveControllerForPoint(p:Point):Point
        {
            var i:int = _controlPoints.indexOf(p);
            if ((_line is CubicBezierLine) && (i > -1))
            {
                var localControlPoint:Point = _localControlPoints[i];
                return CubicBezierLine(_line).getPositiveControllerForPoint(p);
            }

            return super.getPositiveControllerForPoint(p);
        }

        override public function getNegativeControllerForPoint(p:Point):Point
        {
            var i:int = _controlPoints.indexOf(p);
            if ((_line is CubicBezierLine) && (i > -1))
            {
                var localControlPoint:Point = _localControlPoints[i];
                return CubicBezierLine(_line).getNegativeControllerForPoint(p);
            }
            return super.getNegativeControllerForPoint(p);
        }

        override public function getPositiveControllerAt(i:int):Point
        {
            if ((_line is CubicBezierLine) && (i > -1))
            {
                return CubicBezierLine(_line).getPositiveControllerAt(i);
            }
            return super.getPositiveControllerAt(i);
        }

        override public function getNegativeControllerAt(i:int):Point
        {
            if ((_line is CubicBezierLine) && (i > -1))
            {
                return CubicBezierLine(_line).getNegativeControllerAt(i);
            }
            return super.getNegativeControllerAt(i);
        }

        override public function movePositiveController(p:Point, newX:Number, newY:Number):void
        {
            var i:int = _controlPoints.indexOf(p);

            if ((_line is CubicBezierLine) && (i > -1))
            {
                var localPoint:Point = convertXYToLocalPoint(newX, newY);

                CubicBezierLine(_line).movePositiveController(_localControlPoints[i], localPoint.x, localPoint.y);
                _controllerMoved = true;
                invalidateDisplayList();
            }
        }

        override public function moveNegativeController(p:Point, newX:Number, newY:Number):void
        {
            var i:int = _controlPoints.indexOf(p);

            if ((_line is CubicBezierLine) && (i > -1))
            {
                var localPoint:Point = convertXYToLocalPoint(newX, newY);

                CubicBezierLine(_line).moveNegativeController(_localControlPoints[i], localPoint.x, localPoint.y);
                _controllerMoved = true;
                invalidateDisplayList();
            }

        }

        override public function movePositiveControllerAt(i:int, newX:Number, newY:Number):void
        {
            var p:Point = _controlPoints[i];
            if (p && (_line is CubicBezierLine))
            {
                var localPoint:Point = convertXYToLocalPoint(newX, newY);
                CubicBezierLine(_line).movePositiveControllerAt(i, localPoint.x, localPoint.y);
                _controllerMoved = true;
                invalidateDisplayList();
            }

        }

        override public function moveNegativeControllerAt(i:int, newX:Number, newY:Number):void
        {
            var p:Point = _controlPoints[i];
            if (p && (_line is CubicBezierLine))
            {
                var localPoint:Point = convertXYToLocalPoint(newX, newY);
                CubicBezierLine(_line).moveNegativeControllerAt(i, localPoint.x, localPoint.y);
                _controllerMoved = true;
                invalidateDisplayList();
            }
        }

        override public function findInsertionIndexForPoint(p:Point):int
        {
            var foundIndex:int = -1;
            if (_line && (_line is SegmentedLine))
            {
                foundIndex = SegmentedLine(_line).findInsertionIndexForPoint(p);
            }

            if (foundIndex < 0)
            {
                foundIndex = 0;
            }
            return foundIndex;
        }

        override public function get includeSegmentBoundries():Boolean
        {
            if (_line && (_line is CubicBezierLine))
            {
                return CubicBezierLine(_line).includeSegmentBoundries;
            } else
            {
                return false;
            }
        }

        override public function set includeSegmentBoundries(b:Boolean):void
        {
            if (_line && (_line is CubicBezierLine))
            {
                CubicBezierLine(_line).includeSegmentBoundries = b;
            }
        }

        private function convertXYToLocalPoint(newX:Number, newY:Number):Point
        {
            var originalPoint:Point = new Point(newX, newY);
            return PointUtils.convertCoordinates(originalPoint, _diagram.edgeLayer, this);

        }

        private function findDefaultPositionForFirstControlPoint():Point
        {
            var midPoint:Point = Point.interpolate(_startPoint, _endPoint, .5);
            var newX:Number;
            var newY:Number;
            if (_startPoint.x == _endPoint.x)
            {
                newX = _startPoint.x + 20;
                newY = midPoint.y;
            } else if (_startPoint.y == _endPoint.y)
            {
                newX = midPoint.x;
                newY = _startPoint.y + 20;
            } else
            {
                var diff:Point = _startPoint.subtract(_endPoint);
                var edgeAngle:Number = Math.atan2(diff.y, diff.x);
                var tan:Number = Math.tan(edgeAngle);
                var normal:Point;
                if (tan < 0)
                {
                    normal = new Point(diff.y, -diff.x);
                } else
                {
                    normal = new Point(-diff.y, diff.x);
                }
                normal.normalize(1);

                newX = midPoint.x + (normal.x * 20);
                newY = midPoint.y + (normal.y * 20);
            }

            newX = newX + randRange(0, 20);
            newY = newY + randRange(0, 20);

            return new Point(newX, newY);

        }

        private function randRange(start:Number, end:Number):Number
        {
            return Math.floor(start + (Math.random() * (end - start)));
        }
    }
}