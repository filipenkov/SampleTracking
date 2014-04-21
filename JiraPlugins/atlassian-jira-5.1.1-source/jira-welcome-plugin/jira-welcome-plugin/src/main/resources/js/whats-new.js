Raphael.spinner = function (holderId, radius, colour) {
    var color = colour || "#fff",
        width = radius * 13 / 60,
        r1 = radius * 35 / 60,
        r2 = radius,
        cx = r2 + width,
        cy = r2 + width,
        r = Raphael(holderId, r2 * 2 + width * 2, r2 * 2 + width * 2),

        sectors = [],
        opacity = [],
        beta = 2 * Math.PI / 12,

        pathParams = {stroke: color, "stroke-width": width, "stroke-linecap": "round"};
    for (var i = 0; i < 12; i++) {
        var alpha = beta * i - Math.PI / 2,
            cos = Math.cos(alpha),
            sin = Math.sin(alpha);
        opacity[i] = i / 12;
        sectors[i] = r.path([["M", cx + r1 * cos, cy + r1 * sin], ["L", cx + r2 * cos, cy + r2 * sin]]).attr(pathParams);
    }
    var tick;
    (function ticker() {
        opacity.unshift(opacity.pop());
        for (var i = 0; i < 12; i++) {
            sectors[i].attr("opacity", opacity[i]);
        }
        r.safari();
        tick = setTimeout(ticker, 80);
    })();
    return function () {
        clearTimeout(tick);
        r.remove();
    };
};

//temporary fix for AJS-703
var dim = AJS.dim;
AJS.dim = function() {
    try {
        dim.apply(this, arguments);
    } catch (err) {
        // do nothing
    }
};

AJS.toInit(function (jQuery) {

    var whatsNewMenuItem = jQuery("#whats-new-menu-link"),
        throbber, iframe, popup, timeoutDiv, killSpinner, timeout,

    /**
     * Called when the user clicks on the "Don't show again checkbox or its label, stores a flag against the user to
     * not auto-show the What's New dialog on first visit of session.
     */
    toggleDontShow = function (e) {
        var isShownForUser = !jQuery(this).attr('checked');
        var url = contextPath + "/rest/whatsnew/1.0/show";
        jQuery.ajax({
            url: url,
            data:{},
            type: isShownForUser ? 'POST' : 'DELETE',
            success: function () {
                AJS.log("whatsnew > toggleDontShow > isShownForUser set to: " + isShownForUser);
            }
        });
    },
    // When the iframe content has loaded a page-loaded.whats-new event will be
    // triggered on the parent frame AJS object. At this point, hide the loading
    // div and display the iframe.
    iframeReadyStateChanged = function () {
        // Load already cancelled
        if (!iframe) return;

        timeout && clearTimeout(timeout);
        hideThrobber();
        AJS.setVisible(iframe, true);
    },

    createDialog = function (isShownForUser) {
        var dialog = new AJS.Dialog({
            width: 855,
            height: 545,
            id: "whats-new-dialog",
            onCancel: function () {
                dialog.hide().remove();
                timeout && clearTimeout(timeout);
            }
        });
        var src = AJS.Meta.get("whats-new-iframe-src-override");
        if (typeof(src) == "undefined" || src === "") {
            src = AJS.Meta.get("whatsnew-full-link");
        }

        // getText and format separately so that js-i18n-transformer will work
        var headingPattern = AJS.I18n.getText("whats.new.dialog.heading");

        var minorVersion =  AJS.Meta.get("version-number").match(/^\d+\.\d+/);
        var header = AJS.format(headingPattern, minorVersion);
        dialog.addHeader(header);
        var panelHtml = JIRA.Templates.WhatsNew.whatsNewDialogPanel({
            whatsNewHelpLink: src,
            whatsNewFullLink: AJS.Meta.get("whatsnew-full-link")
        });
        dialog.addPanel("default", panelHtml);
        dialog.addCancel(AJS.I18n.getText("common.words.close"), function () {
            dialog.hide().remove();
            timeout && clearTimeout(timeout);
            return false;
        });
        popup = dialog.popup.element;

        if(AJS.Meta.get("remote-user"))
        {
            dialog.page[dialog.curpage].buttonpanel.append(JIRA.Templates.WhatsNew.whatsNewDialogTipPanel({
                isOnDemand: AJS.DarkFeatures.isEnabled('com.atlassian.jira.config.CoreFeatures.ON_DEMAND')
            }));
            popup.find('#dont-show-whats-new').change(toggleDontShow).attr('checked', isShownForUser ? '' : 'checked');
        }

        iframe = popup.find("iframe");
        iframe.load(iframeReadyStateChanged);

        timeoutDiv = popup.find(".whats-new-timeout");

        return dialog;
    },

    hideThrobber = function () {
        if (killSpinner) {
            killSpinner();
            killSpinner = null;
        }
        throbber.addClass("hidden");
    },

    /**
     * Creates and shows the what's new dialog after checking the user's settings..
     */
    showWhatsNewDialog = function () {
        var url = contextPath + "/rest/whatsnew/1.0/show";
        jQuery.getJSON(url, function (settings) {
            createDialog(settings.isShownForUser).show();
            // If the iframe takes too long to load, show a timeout message
            throbber = popup.find(".whats-new-throbber.hidden");
            throbber.removeClass("hidden");
            killSpinner = Raphael.spinner(throbber[0], 80, "#666");

            timeout = setTimeout(function () {
                iframe = null;
                hideThrobber();
                AJS.setVisible(timeoutDiv, true);
            }, 10000);
        })
    };

    // Show the dialog if the user selects the menu item.
    whatsNewMenuItem.click(function (e) {
        e.preventDefault();
        showWhatsNewDialog();
    });

    // If dialog should show automatically on page load, show it now.
    AJS.Meta.getBoolean("show-whats-new") && showWhatsNewDialog();
});
