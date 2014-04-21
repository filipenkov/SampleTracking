AG.Sortable = function () {

    var
    sortableControl,
    dragGadget,
    hotspots = [],

    options = {
        cursor: "move",
        // The actual trigger from sorting is done through the absolute positioned gadget.
        // The gadget just triggers the click event onto this element.
        // See applyFocusControls in AG.Gadget to see how this happens.
        items: "li.gadget",
        tolerance: "pointer",
        placeholder: "placeholder",
        forcePointerForContainers: true,
        scroll: true,
        // not supporting in safari because it does not work in safari 3.
        // I also cannot find a reliable way to check for version.
        revert: AJS.$.browser.safari ? false : 250,
        scrollSensitivity: 300,
        scrollSpeed: 16,
        zIndex: 10,
        helper: function (event, item) {
            return item.get(0).getGadgetInstance().getElement();
        },
        change: function () {
            AG.DashboardManager.getLayout().refresh();
        },
        start: function (event, obj) {

            dragGadget = obj.item.get(0).getGadgetInstance();

            function preventTextSelection () {
                if (typeof document.onselectstart !== "undefined") {
                    document.onselectstart = function () {
                        return false;
                    };
                }
            }

            function preventHelperRemovalOnDrop () {
                obj.item.removeValidator = AJS.$.aop.around({target: AJS.$, method: "remove"}, function (invocation) {
                   if (obj.helper !== this) {
                       invocation.proceed();
                   }
                });
            }

            function setPlaceholder () {
                 obj.placeholder
                    .height(obj.helper.outerHeight() - 2)
                    .html("<p>" + AG.param.get("dragYourGadgetHere") + "</p>");
            }

            obj.helper.addClass("dragging");

            preventTextSelection();
            preventHelperRemovalOnDrop();
            setPlaceholder();

            AG.DashboardManager.getLayout().refresh();
            AG.DashboardManager.getDashboard().addClass("dragging");
            AG.DashboardManager.showShims();
            AG.DashboardManager.markReadOnlyLayouts();
        },
        stop: function (event, obj) {

            var gadgetElement = dragGadget.getElement();

            function enableTextSelection () {
                if (typeof document.onselectstart !== "undefined") {
                    document.onselectstart = null;
                }
            }

            gadgetElement.layoutRep.css({display: ""});
            gadgetElement.css({height: "auto"}).removeClass("dragging");
            gadgetElement.shadow.hide();
            
            if (!dragGadget.hasBeenDropped) {
                AG.DashboardManager.getLayout().saveLayout();
            } else {
                sortableControl.sortable( 'option' , "revert" , 250);
            }

            enableTextSelection();

            AG.DashboardManager.getDashboard().removeClass("dragging");
            AG.DashboardManager.hideShims();
            AG.DashboardManager.getLayout().refresh();
            AG.DashboardManager.unmarkReadOnlyLayouts();
        }
    };

    return {
        serialize: function () {
            var params = {};
            AJS.$.each(this.columns.filter(":visible"), function (i) {
                params[i] = [];
                AJS.$.each(AJS.$(this).sortable("toArray"), function () {
                    params[i].push(AJS.$("#" + this).get(0).getGadgetInstance().getId());
                });
            });
            return params;
        },
        addHotSpot: function (elem, callback) {
            var offset = elem.offset(), dashboardOffset = AG.DashboardManager.getDashboard().offset();
            hotspots.push(
                AJS.$("<div class='hotspot-shim hidden' />")
                    .hover(function() {
                        dragGadget.getElement().css({opacity: 0.5});
                        elem.addClass("hover");
                    }, function () {
                        dragGadget.getElement().css({opacity: ""});
                        elem.removeClass("hover");
                    })
                    .mouseup(function () {
                        sortableControl.sortable( 'option' , "revert" , false );
                        dragGadget.hasBeenDropped = true;
                        callback(dragGadget);
                    })
                    .css({
                        height: elem.outerHeight(),
                        width: elem.outerWidth(),
                        left: offset.left - dashboardOffset.left,
                        top: offset.top - dashboardOffset.top
                    })
                    .appendTo(AG.DashboardManager.getDashboard())
            );
        },
        update: function () {
            AG.Sortable.init();
        },
        init: function () {
            if (sortableControl) {
                sortableControl.sortable("destroy");
            }
            this.columns = AJS.$(".draggable .column.sortable");
            if (this.columns.length > 0) {
                sortableControl = this.columns.sortable(AJS.$.extend(options, {
                    connectWith: this.columns
                }));
            }
        }
    };
}();

