(function () {

    // We want to make sure that we don't have any other layers open before we close the dialog
    JIRA.bind("Dialog.beforeHide", function (e) {

        if (typeof Calendar !== "undefined" && Calendar.current) {
            e.preventDefault();
        }

        // Old style dropdown
        if (JIRA.Dropdown && JIRA.Dropdown.current) {
            e.preventDefault();
        }

        // Don't hide dialog if we have an inline layer open
        if (AJS.InlineLayer && AJS.InlineLayer.current) {
            e.preventDefault();
        }
        // Or dropdown
        if (AJS.Dropdown && AJS.Dropdown.current) {
            e.preventDefault();
        }
        // or AJS inline dialog
        if (AJS.InlineDialog && AJS.InlineDialog.current) {
            e.preventDefault();
        }
    });


    JIRA.bind("Dialog.show", function (e) {

        if (typeof Calendar !== "undefined" && Calendar.current) {
            e.preventDefault();
        }

        // Old style dropdown
        if (JIRA.Dropdown && JIRA.Dropdown.current) {
            JIRA.Dropdown.current.hide();
        }

        // Don't hide dialog if we have an inline layer open
        if (AJS.InlineLayer && AJS.InlineLayer.current) {
            AJS.InlineLayer.current.hide();
            e.preventDefault();
        }
        // Or dropdown
        if (AJS.Dropdown && AJS.Dropdown.current) {
            AJS.Dropdown.current.hide();
            e.preventDefault();
        }
        // or AJS inline dialog
        if (AJS.InlineDialog && AJS.InlineDialog.current) {
            AJS.InlineDialog.current.hide();
            e.preventDefault();
        }
    });

    // JRADEV-10921
    jQuery(function () {
        if (typeof Calendar !== "undefined") {
            jQuery(document).bind("beforeBlurInput", function (e) {
                if (Calendar.current) {
                    e.preventDefault();
                }
            });
            if (JIRA.Events.BEFORE_INLINE_EDIT_CANCEL) {
                JIRA.bind(JIRA.Events.BEFORE_INLINE_EDIT_CANCEL, function (e, id, type, reason) {
                    if (reason === JIRA.Issues.CANCEL_REASON.escPressed && Calendar.current) {
                        e.preventDefault();
                    }
                });
            }
        }
    });


    var $doc = jQuery(document);

    function getWindow () {
        var topWindow = window;
        try {
            while (topWindow.parent.window !== topWindow.window && topWindow.parent.AJS) { // Note: Accessing topWindow.parent might throw an error.
                topWindow = topWindow.parent;
            }
        } catch (error) {
            // The same-origin policy prevents access to the top frame.
            // Ignore this error and return the topmost window that can be accessed.
        }
        return topWindow;
    }

    function getLayer(instance) {
        // instance is either an AJS.InlineLayer, JIRA.Dialog, AJS.dropDown or AJS.InlineDialog. Handle every case.
        // TODO: Uniformity would be nice.
        return (instance.$layer || instance.$popup || instance.$ || instance.popup || instance)[0];
    }

    function listenForLayerEvents ($doc) {
        $doc.bind("showLayer", function (e, type, item) {
            // User hover and inline edit dialogs don't participate in layer management.
            if (item && item.id && (item.id.indexOf("user-hover-dialog") >= 0 ||
                                    item.id.indexOf("aui-inline-edit-error")  >= 0 ||
                                    item.id.indexOf("watchers") >=0 ||
                                    item.id.indexOf("share-entity-popup")  >= 0 )) {
                return;
            }
            var topWindow = getWindow().AJS;
            //the user-hover-dialog has a dropdown in it which is why we're not hiding it on showLayer. It hides
            //itself anyway when the user doesn't hover over it any more with the mouse.
            if (topWindow.currentLayerItem && item !== topWindow.currentLayerItem && topWindow.currentLayerItem.type !== "popup") {
                topWindow.currentLayerItem.hide();
            }
            if (item) {
                topWindow.currentLayerItem = item;
                topWindow.currentLayerItem.type = type;
            }
        })
        .bind("hideLayer", function (e, type, item) {

            // User hover dialogs don't participate in layer management.
            if (!item || item.id && (item.id.indexOf("user-hover-dialog") >= 0 || item.id.indexOf("aui-inline-edit-error")  >= 0 )) {
                return;
            }
            var topWindow = getWindow().AJS;
            if (topWindow.currentLayerItem) {
                if (topWindow.currentLayerItem === item) {
                    topWindow.currentLayerItem = null;
                } else if (jQuery.contains(getLayer(item), getLayer(topWindow.currentLayerItem))) {
                    topWindow.currentLayerItem.hide();
                }
            }
        })
        .bind("hideAllLayers", function () {
            var topWindow = getWindow().AJS;
            if (topWindow.currentLayerItem) {
                topWindow.currentLayerItem.hide();
            }
        })
        .click(function (e) {
            var topWindow = getWindow().AJS;
            if (topWindow.currentLayerItem &&  topWindow.currentLayerItem.type !== "popup") {

                if (topWindow.currentLayerItem._validateClickToClose) {
                    if (topWindow.currentLayerItem._validateClickToClose(e)) {
                        topWindow.currentLayerItem.hide();
                    }
                } else {
                    topWindow.currentLayerItem.hide();
                }
            }
        });
    }

    $doc.bind("iframeAppended", function (e, iframe) {
        iframe = jQuery(iframe);
        iframe.load(function () {
            listenForLayerEvents(iframe.contents());
        });
    });

    listenForLayerEvents($doc);
})();
