AJS.$(function() {
    if(window.location.href.indexOf("wfDesigner=true") > -1) {
        //remove crap at the top on the ViewWorkflowTransition page.
        if(window.location.pathname.indexOf("ViewWorkflowTransition.jspa") > -1) {
            AJS.$("table:first").detach();
            if (!jQuery.browser.msie || (jQuery.browser.msie && jQuery.browser.version > 7)) {
                AJS.$("p:first").detach();
                AJS.$("p:last").detach();
            }
        }

        var updateUrlAttr = function(attr, $elem) {
            var urlParam = "decorator=inline&wfDesigner=true";

            var url = $elem.attr(attr);
            if(url !== undefined) {
                if(url.indexOf("?") > -1) {
                    $elem.attr(attr, url + "&" + urlParam);
                } else {
                    $elem.attr(attr, url + "?" + urlParam);
                }
            }
        };

        AJS.$("a").each(function() {
            updateUrlAttr("href", AJS.$(this));
        });
        AJS.$("form").each(function() {
            updateUrlAttr("action", AJS.$(this));
        });

        var $cancelButton = AJS.$("input[id|=cancelButton]");
        var cancelOnClick = $cancelButton.attr("onclick");
        if(cancelOnClick !== undefined) {
            $cancelButton.attr("onclick", "").click(function() {
                location.href = document.referrer + "&amp;decorator=inline&amp;wfDesigner=true";
            });
        }
    }
});