(function () {

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
            if (item && item.id && (item.id.indexOf("user-hover-dialog") >= 0 || item.id.indexOf("aui-inline-edit-error")  >= 0 )) {
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
