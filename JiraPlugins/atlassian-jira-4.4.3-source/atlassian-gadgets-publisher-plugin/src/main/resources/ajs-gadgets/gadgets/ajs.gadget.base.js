/**
 * Abstract Gadget object. This object provides the baseline functionality from which other objects can extend.
 *
 * @class Gadget
 * @constructor
 * @protected
 * @param {Object} options
 *
 * <ul>
 *      <li><strong>baseUrl:</strong> String that will prefix any remote request url</li>
 *      <li><p><strong>useOauth:</strong> Used to determine what authorisation headers (if any) are required for
 *      requests. Using the provided url, gadget will request resource without oauth headers, if data
 *      comes back in the response, all subsequent requests will not use oauth headers. If response does not
 *      return data, subsequent requests will use oauth. Full implementation details available here:
 *      https://extranet.atlassian.com/display/JIRADEV/OAuth+and+Gagdets+with+anonymous+access.</p>
 *
 *      <p>Alternatively you can provide the string "always" which will force all requests to use oauth. Please
 *      note this removes the ability to provide data to anonymous users (@AnonymousAllowed)</p>
 *
 *      </li>
 * </ul>
 *
 */

if (!console.warn) {
    console.warn = function () {};
}

// we do not include use raphael in all gadgets as the file size is to large. This is a stub to prevent gadgets
// failing when the configuration dropdown is opened in the bottom right.
// This can be removed once this issue is fixed, AJS-553.
Raphael = {
    shadow: function () {
        return AJS.$("<div />");
    }
};


// This is an override for AJS.messages, for the same reason as above
(function () {

    function getMessage (clss, body) {
        return "<div class=\"aui-message " + clss + "\">\n"
                + "    <p><span class=\"aui-icon icon-" + clss + "\"></span>" + body + "</p>\n"
                + "</div>";
    }

    AJS.messages = {
        error: function (container, options) {
            return container.html(getMessage("error", options.body));
        },
        warning: function (container, options) {
            return container.html(getMessage("warning", options.body));
        },
        info: function (container, options) {
            return container.html(getMessage("info", options.body));
        },
        setup: function () {
            // required as AJS calls this
        }
    };

})();





var Gadget = function (options) {

    if(AJS.debug) {
        AJS.$.aop.around( {target: gadgets.io, method: 'makeRequest'}, function(invocation) {
            var url = invocation.arguments[0];
            if (!/\?/.test(url)) url += "?";
            else if (!/&$/.test(url)) url += "&";
            invocation.arguments[0] = url + "cacheBuster=" + new Date().getTime();
            return invocation.proceed();
        });
    }


    // private

    var

    /**
     * User preferencesf
     *
     * @property prefs
     * @type Object
     * @private
     *
     */
    prefs = new gadgets.Prefs(),


    /**
     * Stores the current mode the view is in. For example "canvas"
     *
     * @property view
     * @type Object
     * @private
     */
    viewMode,


    isAtlassianContainer = function () {
        return window._args && _args().container === "atlassian" &&
               window.top.AG && window.top.AG.DashboardManager;
    },

    getMenu = function () {
        var res = {};

        function getFooterMenu () {
            if (!getMenu.dropdown) {
                res.$("<div class='aui-dd-parent'><a href='#' class='aui-dd-link'><span></span></a><ul class='hidden aui-dropdown'></ul></div>")
                        .appendTo(gadget.getFooter());
                getMenu.elem = res.$(".aui-dropdown", gadget.getFooter());

                getMenu.dropdown = res.$(".footer .aui-dd-parent").dropDown("Standard", {
                    trigger: ".aui-dd-link",
                    selectionHandler: function (e) {
                        e.preventDefault();
                    }
                })[0];
                AJS.$(".gadget").addClass("needs-min");
                gadget.resize();
            }

            return getMenu.elem;
        }
        
        if (isAtlassianContainer()) {
            res.$ = window.top.AJS.$;
            getMenu.dropdown = res.$("#" + window.name + "-renderbox").get(0).getGadgetInstance().getElement().dropdown;
            res.elem = getMenu.dropdown.$;
        } else {
            res.$ = AJS.$;
            res.elem = getFooterMenu();
        }

        return res;
    },


    message = function () {

        var msgContainer;

        /**
         * Displays a message in dialog box
         *
         * @method message.show
         * @param {String, Object} msg - A html string or jQuery object containing message
         * @param {String} classNm - className used for styling of message (error/info)
         * @param {Boolean} dismissible - if set to false no cancel button will be available
         */
        function showPopup(msg, classNm, dismissible) {
            var messageEl, buttons, cancelButton, buttonPanel;

            if (classNm == "error" || classNm == "info")
                classNm += "-inner";

            // we create the message node first and append it to the body. We can then use it to determine the
            // height of dialog box we need.
            messageEl = AJS.$("<div />").addClass(classNm).css({
                width: 260,
                visiblity: "hidden"
            }).html(msg).appendTo("body");

            // if there are any buttons in the argumented message then put them alongside the cancel button
            buttons = AJS.$("button", messageEl);
            if (dismissible !== false) {
                cancelButton = AJS.$("<button class='cancel'>").text(prefs.getMsg("gadget.common.cancel")).click(message.hide);
            }
            if (buttons.length > 0 || cancelButton) {
                buttonPanel = AJS.$("<div class='buttons'>").append(buttons).append(cancelButton);
                messageEl.append(buttonPanel);
            }

            // if there is already a dialog showing, remove it.
            message.hide();

            msgContainer = new AJS.popup(messageEl.outerWidth(), messageEl.outerHeight(), "message");
            msgContainer.element.html(messageEl);
            msgContainer.show();

            // hack because AJS.popup is failing to show blanket if popup is instantiated more than once.
            AJS.$(".blanket").show();
        }

        /**
         * Displays a message in an inline message box
         *
         * @method message.show
         * @param {String, Object} msg - A html string or jQuery object containing message
         * @param {String} classNm - className used for styling of message (error/warning/info)
         * @param {Boolean} dismissible - if set to false no cancel button will be available
         */
        function showInline(msg, classNm, dismissible) {
            if (!AJS.messages[classNm]) {
                console.warn("Invalid class name '"+classNm+"' specified for styling AUI Message. Defaulting to error.");
                classNm = "error";
            }

            message.hide();
            msgContainer = AJS.$('<div id="gadgetMessage" />').prependTo("body .gadget .view");
            AJS.messages[classNm](msgContainer, {
                body: msg,
                closeable: dismissible
            });
        }

        return {

            /**
             * Displays a message in dialog box
             *
             * @method message.show
             * @param {String, Object} msg - A html string or jQuery object containing message
             * @param {String} classNm - className used for styling of message
             * @param {Boolean} dismissible - if set to false no cancel button will be available
             * @param {Boolean} usePopup - uses AUI Dialog if set to true, AUI Message otherwise
             */
            show: function (msg, classNm, dismissible, usePopup) {
                // TODO: Automatically detect gadget size when usePopup is not set and use showPopup for "large" gadgets 
                if (usePopup)
                    showPopup(msg, classNm, dismissible);
                else
                    showInline(msg, classNm, dismissible);
            },

            /**
             * Destroys message
             *
             * @method message.hide
             */
            hide: function () {
                if (msgContainer) {
                    msgContainer.remove();
                    // fix for bug in AJS, blanket will not hide
                    AJS.$(".aui-blanket").remove();
                    AJS.dim.dim = undefined;
                }
            }
        };

    }(),

    gadget = {

        isAtlassianContainer: function () {
            return isAtlassianContainer();
        },

        addMenuItem: function (clss, title, handler) {
            var menu = getMenu(true);
            if (menu.$("." + clss, menu.elem).length === 0) {
                menu.$("<li class='dropdown-item " + clss + "'><a class='item-link no_target' href='#'>" + title + "</a></li>").click(handler).appendTo(menu.elem);
                if (!menu.elem.is(":visible")) {
                    menu.elem.parent().show();
                }
            } else {
                // We need to re-bind in the case iframe is refreshed. (JRA-18879)
                menu.$("." + clss, menu.elem).unbind("click").bind("click", handler);
            }
            getMenu.dropdown.reset();
        },

        removeMenuItem: function (clss) {
            var menu = getMenu();
            if (menu) {
                window.setTimeout(function() {
                    menu.$("." + clss, menu.elem).remove();
                    if (menu.$("li", menu.elem).length === 0) {
                        menu.elem.parent().hide();
                    }
                }, 10);
            }
            getMenu.dropdown.reset();
        },

        /**
         * When calling createCookie() you have to give it three bits of information: the name and value of the
         * cookie and the number of days it is to remain active
         *
         * @method createCookie
         * @param name
         * @param value
         * @param days
         */
        createCookie: function(name, value, days) {
            if (days) {
                var date = new Date();
                date.setTime(date.getTime()+(days*24*60*60*1000));
                var expires = "; expires="+date.toGMTString();
            }
            else var expires = "";
            document.cookie = window.name + name+"="+value+expires+"; path=/";
        },

        /**
         * To read out a cookie, call this function and pass the name of the cookie. Put the name in a variable.
         * First check if this variable has a value (if the cookie does not exist the variable becomes null, which
         * might upset the rest of your function), then do whatever is necessary.
         *
         * @method readCookie
         * @param name
         */
        readCookie: function (name) {
            var nameEQ = window.name +name + "=";
            var ca = document.cookie.split(';');
            for(var i=0;i < ca.length;i++) {
                var c = ca[i];
                while (c.charAt(0)==' ') c = c.substring(1,c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
            }
            return null;
        },

        /**
         * Removes cookie
         *
         * @method eraseCookie
         * @param name
         */
        eraseCookie: function (name) {
            gadget.createCookie(window.name + name,"",-1);
        },

        /**
         * Displays a message in dialog box
         *
         * @method showMessage
         * @private
         * @param {String} type - style of message. Options include "error, info"
         * @param {String, Object} msg - A html string or jQuery object containing message
         * @param {String} dismissible - if set to false no cancel button will be available
         * @param {Boolean} usePopup - uses AUI Dialog if set to true, AUI Message otherwise
         */
        showMessage: function (type, msg, dismissible, usePopup) {
            message.show(msg, type, dismissible, usePopup);
        },


        /**
         * Saves user preferences locally and to the database
         *
         * @method savePref
         * @private
         * @param {String} name
         * @param {String, Array} value
         */
        savePref: function (name, value) {
            if (!AJS.$.isArray(value)) {
                prefs.set(name, value);
            } else {
                prefs.setArray(name, value);
            }

        },

        /**
         * Toggles class of gadget to the specified view. This class is used to style the view accordingly.
         *
         * @method setViewMode
         * @param {String} mode
         */
        setViewMode: function (mode) {
            if (viewMode) {
                gadget.getGadget().removeClass(viewMode);
            }
            gadget.getGadget().addClass(mode);
            viewMode = mode;
        },

        /**
         * Returns the current view mode. For example "Canvas".
         *
         * @method getViewMode
         * @return [String} view mode
         */
        getViewMode: function () {
            return viewMode;
        },

        /**
         * Helper function to get the context path for jira. Necessary for remote requests.
         *
         * @method getBaseUrl
         * @return {String} url
         */
        getBaseUrl: function () {
            return options.baseUrl;
        },

        /**
         * Gets user preference object
         *
         * @method getPrefs
         * @return {Object} user preference object
         */
        getPrefs: function () {
           return prefs;
        },

        /**
         * Some sugar for getting a preference by name
         *
         * @method getPref
         * @param {String} name - Specify an explicit preference to return.
         * @return {Object}
         */
        getPref: function (name) {
            return prefs.getString(name);
        },

        /**
         * Gets user pref as an array
         *
         * @method getPrefArray
         * @param name
         * @return {Array} user pref as an array
         */
        getPrefArray: function (name) {
            return prefs.getArray(name);
        },

        /**
         * Gets il8n string
         *
         * @method getMsg
         * @param {String} key
         * @param {String, Array} params to be injected in to message string using java message format
         * @return {String} il8n string for given key
         */
        getMsg: function (key, params) {
            var msg = prefs.getMsg(key);
            if (params instanceof Array) {
                params.splice(0,0, msg);
                msg = AJS.format.apply(window, params);
            } else if (typeof params === "string") {
                msg = AJS.format.apply(window, [msg, params]);
            }
            return msg !== "" ? msg : key;
        },

        /**
         * Gets gadget object, wrapper div for all gadget html (jQuery Object)
         *
         * @method getPrefs
         * @return {Object} jQuery object
         */
        getGadget: function () {
            var gadget = AJS.$("<div />").addClass("gadget").appendTo("body");
            return function () {
                return gadget;
            };
        }(),

        /**
         * Resizes iframe to fit content
         *
         * @method resize
         */
        resize: function (size) {
            if (gadgets.window.adjustHeight) {
                gadget.resize.pending = false;
                window.setTimeout(function () {
                    gadgets.window.adjustHeight(size);
                },0);
            }
        },

        isLocal: function () {
            if (typeof atlassian !== "undefined" && atlassian.util) {
                return atlassian.util.getRendererBaseUrl() === options.baseUrl;
            }
        },

        /**
         * Shows loading indicator
         *
         * @method showLoading
         */
        showLoading: function () {
            if (gadgets.window.getViewportDimensions && gadgets.window.getViewportDimensions().width < 250){
                AJS.$("body").addClass("loading-small");
            } else {
                AJS.$("body").addClass("loading");
            }
        },

        /**
         * Hides loading indicator
         *
         * @method hideLoading
         */
        hideLoading: function () {
            AJS.$("body").removeClass("loading").removeClass("loading-small");
            if (!AJS.$("body").hasClass("loading") && gadget.hideLoading.callbacks) {
                AJS.$.unique(gadget.hideLoading.callbacks);
                AJS.$.each(gadget.hideLoading.callbacks, function () {
                    this();
                });
                delete gadget.hideLoading.callbacks;
            }
        },

        /**
         * Object responsible for generic handling of request errors
         *
         * @property ajaxErrorHandler
         * @type Object
         * @private
         */
        ajaxErrorHandler: {

            /**
             * Handles server errors
             *
             * @method ajaxErrorHandler.handle500
             * @private
             */
            handle500: function (evt, data, settings) {
                gadget.showMessage("error", AJS.format(gadget.getMsg("gadget.common.error.500"),
                        settings.url, settings.url), false);
            },
            handle503: function(evt, data, settings) {
                gadget.showMessage("error", data.errorMessages, false);
            },

            /**
             * Handles page not found errors
             *
             * @method ajaxErrorHandler.handle500
             * @private
             */
            handle404: function (evt, data, settings) {
                gadget.showMessage("error", AJS.format(gadget.getMsg("gadget.common.error.404"),
                        settings.url, settings.url), false);
            },



            /**
             * Executes, if it exists, the correct error handler using of the response code
             *
             * @method ajaxErrorHandler.execute
             * @private
             * @param evt - request event
             * @param xhr - response from server
             * @param settings - settings for request
             */
            execute: function (evt, xhr, settings) {
                var data;
                try {
                    data = jQuery.parseJSON(xhr.responseText);
                } catch (e) {
                    // Not JSON, ignore.
                    AJS.log("The " + xhr.status + " response is not in JSON format.");
                }
                if (xhr && gadget.ajaxErrorHandler["handle" + xhr.status]) {
                    gadget.ajaxErrorHandler["handle" + xhr.status].call(gadget, evt, data, settings);
                }
            }
        }

    };

    if(AJS.debug) {
        AJS.$(["error.500", "error.404", "oauth.approve.button", "oauth.approve.message", "container.login"]).each(function(){
            if (prefs.getMsg("gadget.common." + this) === "") {
                console.warn("il8n key missing:\"" + "gadget.common." + this + "\"");
            }
        });

        if (!options.baseUrl) {
            throw "@constructor Gadget: \n baseUrl is required but has not been set";
        }

        if (!(gadgets && gadgets.oauth && gadgets.oauth.Popup)) {
            throw "@constructor Gadget: \n This framework requires support for oauth. Please add the following lines to"
            + "<ModulePrefs> in your gadget XML:\n"
            + "<Require feature=\"oauthpopup\" />\n"
            + "#oauth";
        }
    }

    function setTarget () {
        if (!AJS.$(this).attr("target")) {
            AJS.$(this).attr({target: "_parent"});
        }
    }

    AJS.$("a:not(.no_target), area")
            .live("click", setTarget)
            .live("mouseover", setTarget);

    /* export for utility methods */
    AJS.gadget.getBaseUrl = function () {
        return options.baseUrl;
    };




    /*
     * Because gadgets exist inside of iframes dropdowns need to act differently. This method basically captures requests
     * to the dropdown constructor and appends some additional functionality to them. The functionality varies between
     * browsers.
     *
     * - Closes dropdowns from parent window when gadget dropdown is opened and vise versa (All browsers)
     *
     * - Removes the dropdown from the gadget iframe and appends to parent window, allowing for overflow. Note, this only
     * occurs when the dropdown overflows the height of the iframe and gadget exists on the same domain.
     * (Firefox & Safari)
     *
     * - Resizes the height of the gadget iframe to allowing for height of dropdown. Note, this only occurs if the browser
     * is either opera or MSIE, or in the case of the gadget iframe being on a different domain  (All browsers)
     *
     * - Modifies default selection handler so that selected links will be loaded in parent window rather then iframe.
     *
     */

    (function(){
        function canBeMovedToParentFrame () {
             return !AJS.$.browser.msie && !AJS.$.browser.opera && isAtlassianContainer();
        }

        function overflowsIframeHeight () {
            return parseInt(AJS.$("body").attr("scrollHeight")) > AJS.$("body").outerHeight();
        }

        function adjustDDPositioning () {
            if (AJS.dropDown.current && AJS.dropDown.current.calculateDDPosition) {
                AJS.dropDown.current.updateDDPosition();
            }
        }

        function parentAsContainerHandler () {
            if (!this.positionMeInParent) {
                this.$.remove().addClass("gadget-dropdown");
                this.shadow.remove();
                this.shadow.appendTo(window.top.document.body);
                this.$.appendTo(window.top.document.body);
                this.positionMeInParent = function () {

                    var dropdownOffsets;
                    function getLeftInPercent (left) {
                        return left / window.top.AJS.$("body").width() * 100 + "%";
                    }

                    function calculateOffsets () {
                        var dropdownOffset = this.trigger.offset(),
                                iframeOffset = window.top.AJS.$("#" + window.name).offset();
                        return {
                            top: dropdownOffset.top + this.trigger.outerHeight() + iframeOffset.top,
                            left: dropdownOffset.left + iframeOffset.left + this.trigger.outerWidth() - this.$.outerWidth(),
                            width: this.$.outerWidth()
                        };
                    }

                    if (!this.trigger.is(":visible")) {
                        this.hide();
                    } else {
                        dropdownOffsets = calculateOffsets.call(this);
                        this.$.css({
                            top: dropdownOffsets.top,
                            margin:0,
                            left: getLeftInPercent(dropdownOffsets.left),
                            right: "auto"
                        });
                        this.shadow.css({
                            left: getLeftInPercent(dropdownOffsets.left - 7),
                            top: dropdownOffsets.top,
                            right: "auto"
                        });
                    }
                };
            }

            this.positionMeInParent();
        };

        var iframeAsContainerHandler = {
            show: function () {
                if (overflowsIframeHeight() && gadgets.window.adjustHeight) {
                    if (AJS.$("body").attr("scrollHeight") !== AJS.$("body").height())
                        gadgets.window.adjustHeight(AJS.$("body").attr("scrollHeight"));
                }
            },
            hide: function () {
                if (AJS.$(".active .aui-dropdown").length === 0) {
                    if (iframeAsContainerHandler.restoreHeight && AJS.$("body").height() !== iframeAsContainerHandler.restoreHeight)
                        gadget.resize(iframeAsContainerHandler.restoreHeight);
                    delete iframeAsContainerHandler.restoreHeight;
                }
            }
        };

        AJS.$(window).resize(adjustDDPositioning);


        AJS.dropDown.Standard = function (dropDown) {


            return function (options) {

                var dropdowns;

                options.selectionHandler = options.selectionHandler || function (e, item) {
                    if (item[0].nodeName === "A") {
                        window.top.location.href = item.attr("href");
                    }
                    else {
                        window.top.location.href = AJS.$("a", item).attr("href");
                    }
                };

                dropdowns = dropDown.call(this, options);

                AJS.$.each(dropdowns, function () {
                    if (canBeMovedToParentFrame()) {
                        if (options.positionOnShow !== false) {
                            this.addCallback("show", function () {
                                parentAsContainerHandler.call(this);
                            });
                        }
                    }
                    else {
                        this.trigger.focus(function () {
                            if (!iframeAsContainerHandler.restoreHeight) {
                                iframeAsContainerHandler.restoreHeight = AJS.$("body").outerHeight();
                            }
                        });
                        this.addCallback("show", iframeAsContainerHandler.show);
                        this.addCallback("hide", iframeAsContainerHandler.hide);
                    }
                    this.trigger.mousedown(function () {
                        AJS.$.ajaxSetup({globalThrobber: false});
                    });
                });

                return dropdowns;
            };
        }(AJS.dropDown.Standard);

        AJS.dropDown.Ajax = function (dropDown) {

            return function (options) {

                options.positionOnShow = false;

                var dropdowns = dropDown.call(this, options);
                AJS.$.each(dropdowns, function () {

                    this.$.addClass("loading");

                    this.addCallback("reset", function () {
                        this.$.removeClass("loading");
                    });

                    if (canBeMovedToParentFrame()) {
                        this.addCallback("reset", function () {
                            var that = this;
                            parentAsContainerHandler.call(that);
                        });
                    } else {
                        this.addCallback("reset", iframeAsContainerHandler.show);
                    }
                });
                return dropdowns;
            };

        }(AJS.dropDown.Ajax);

    })();

    /* set some defaults for ajax requests */
    jQuery.ajaxSetup({
        dataType: "json",
        type: "get",
        baseUrl: options.baseUrl
    });


    // make sure we call the extending error handlers instead of the ones from this class
    jQuery(document).ajaxError(gadget.ajaxErrorHandler.execute);

    // throbber display
    jQuery(document).ajaxStart(function (evt, req, options) {
       if (options && options.globalThrobber !== false) {
            gadget.showLoading.apply(this, arguments);
       }
    });

    jQuery(document).ajaxStop(gadget.hideLoading);

    return gadget;

};
