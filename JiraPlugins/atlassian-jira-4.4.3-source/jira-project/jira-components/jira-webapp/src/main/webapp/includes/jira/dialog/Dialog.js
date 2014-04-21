/**
 * @constructor
 */
JIRA.Dialog = AJS.Control.extend({

    _getDefaultOptions: function () {
        return {
            height: "auto",
            cached: false,
            widthClass: "medium",
            ajaxOptions: {
                data: {
                    inline: true,
                    decorator: "dialog"
                }
            }
        };
    },

    init: function (options) {

        if (typeof options === "string" || options instanceof jQuery) {
            options = {
                trigger: options
            };
        } else if (options && options.width) {
            options.widthClass = "custom";
        }

        this.options = jQuery.extend(true, this._getDefaultOptions(), options);
        this.options.width = JIRA.Dialog.WIDTH_PRESETS[this.options.widthClass] || options.width;

        if (typeof this.options.content === "function") {
            this.options.type = "builder";
        } else if (this.options.content instanceof jQuery || (typeof this.options.content === "object" && this.options.nodeName)) {
            this.options.type = "element";
        } else if (!this.options.type && !this.options.content || (typeof this.options.content === "object" && this.options.content.url)) {
            this.options.type = "ajax";
        }

        if (this.options.trigger) {
            this._assignEvents("trigger", this.options.trigger);
        }

        this.onContentReadyCallbacks = [];

        this._assignEvents("container", document);
    },

    _runContentReadyCallbacks: function () {
        var that = this;
        AJS.$.each(this.onContentReadyCallbacks, function () {
            this.call(that);
        });
    },

    /**
     * This is called to set new content into the Popup.  if the decorate flag is false then
     * it will not be decorated.
     *
     * @method _setContent
     * @param {String | jQuery | HTMLElement} content - the content to place in the Popup
     * @param {Boolean} decorate - whether to decorate.  If undefined then decoration will take place
     */
    _setContent: function (content, decorate) {
        if (!content) {
            this._contentRetrievers[this.options.type].call(this, this._setContent);
        } else if (JIRA.Dialog.current === this) {
            var $popup = this.get$popup();

            this.$content = content;
            this.get$popupContent().html(content);

            $popup.addClass("popup-width-" + this.options.widthClass);
            $popup.css({
                marginLeft: -9999
            }).show();

            if (decorate !== false) {

                if (this.decorateContent) {
                    this.decorateContent();
                }
                AJS.$(document).trigger("dialogContentReady", [this]);
                this._runContentReadyCallbacks();
            }

            this._positionInCenter();

            if (decorate !== false) {
                if (AJS.$.isFunction(this.options.onContentRefresh)) {
                    this.options.onContentRefresh.call(this);
                }
            }
            AJS.$('.aui-dialog-open').addClass('aui-dialog-content-ready');

        } else if (this.options.cached === false) {
            delete this.$content;
        }
    },

    /**
     * This is called when the original AJAX 'complete' code path is taken with a serverIsDone = true.
     *
     * @param data the response body
     * @param xhr the AJAX bad boy
     * @param smartAjaxResult the smart AJAX result object we need
     */
    _handleInitialDoneResponse: function(data, xhr, smartAjaxResult){
    },

    _getRequestOptions: function () {

        var options = {};
        if(this._getAjaxOptionsObject() === false) {
            return false;
        }
        // copy to prevent setting url into the original options object
        options = AJS.$.extend(true, options, this._getAjaxOptionsObject());
        if (!options.url && this.$activeTrigger) {
            options.url = this.$activeTrigger.attr("href");
        }
        return options;
    },

    _getAjaxOptionsObject: function()
    {
        var ajaxOpts = this.options.ajaxOptions;
        if (AJS.$.isFunction(ajaxOpts)) {
            return ajaxOpts.call(this);
        } else {
            return ajaxOpts;
        }
    },

    _contentRetrievers: {

        "element" : function (callback) {
            if (!this.$content) {
                this.$content = jQuery(this.options.content).clone(true);
            }
            callback.call(this, this.$content);
        },

        "builder" : function (callback) {
            var instance = this;
            if (!this.$content) {
                this._showloadingIndicator();
                this.options.content.call(this, function (content) {
                    instance._hideloadingIndicator();
                    instance.$content = AJS.$(content);
                    callback.call(instance, instance.$content);
                });
            }
        },

        "ajax" : function (callback) {
            var instance = this, ajaxOptions;
            if (!this.$content) {
                ajaxOptions = this._getRequestOptions();
                this._showloadingIndicator();
                this.serverIsDone = false;

                ajaxOptions.complete = function (xhr, textStatus, smartAjaxResult) {
                    if (smartAjaxResult.successful)
                    {
                        //
                        // Check the status of the X-Atlassian-Dialog-Control header to see if we need to redirect
                        //
                        var instructions = instance._detectRedirectInstructions(xhr);
                        instance.serverIsDone = instructions.serverIsDone;
                        if (instructions.redirectUrl) {
                            //
                            // this will reload the page  and hence stop all processing
                            //
                            instance._performRedirect(instructions.redirectUrl);
                        } else {

                            if (ajaxOptions.dataType && ajaxOptions.dataType.toLowerCase() === "json" && instance._buildContentFromJSON) {
                                instance.$content = instance._buildContentFromJSON(smartAjaxResult.data);
                            } else {
                                instance.$content = smartAjaxResult.data;
                            }

                            if ( instance.serverIsDone){
                                instance._handleInitialDoneResponse(smartAjaxResult.data, xhr, smartAjaxResult);
                            } else {
                                instance._hideloadingIndicator();
                                callback.call(instance, instance.$content);
                            }
                        }

                    } else {
                        instance._hideloadingIndicator();
                        var errorContent = JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult);
                        callback.call(instance, errorContent);
                    }
                };
                JIRA.SmartAjax.makeRequest(ajaxOptions);
            }
        }
    },

    /**
     * This method will look for the magic header instructions from JIRA and set variables accordingly
     *
     * Returns a tuple value indicating what the instructions are :
     *
     *  {
     *      serverIsDone : boolean - will be set to true if the header is present
     *      redirectUrl : string - will be set to a value if the redirect instruction is given
     *  }
     *
     * @param xhr the AJAX bad boy
     * @return a tuple with instructions
     */
    _detectRedirectInstructions: function(xhr) {
        var instructions = {
            serverIsDone : false,
            redirectUrl : ""
        };
        var doneHeader = xhr.getResponseHeader('X-Atlassian-Dialog-Control');
        if (doneHeader) {
            instructions.serverIsDone = true;
            var idx = doneHeader.indexOf("redirect:");
            if (idx == 0) {
                instructions.redirectUrl = doneHeader.substr("redirect:".length);
            }
        }
        return instructions;
    },

    /**
     * This will redirect the page to the specified url
     * @param url {String} the url to redirect to
     */
    _performRedirect: function(url) {
    	AJS.reloadViaWindowLocation(url);
    },

    _renders: {

        popupHeading: function() {
            return jQuery("<h2 />").addClass("aui-popup-heading");
        },

        popupContent: function () {
            return jQuery("<div />")
                    .addClass("aui-popup-content");
        },

        popup: function () {
            return jQuery("<div />")
                    .attr("id", this.options.id || "")
                    .addClass("aui-popup").hide();
        },

        loadingIndicator: function () {
            return jQuery("<div />").addClass("aui-loading");
        }
    },

    _events: {
        "trigger" : {
            click: function (e, item) {
                this.$activeTrigger = item;
                this.show();
                e.preventDefault();
            }
        },

        "container" : {
            "keydown" : function (e) {
                function calendarClosingBy(e) {
                    if (window._dynarch_popupCalendar && !window._dynarch_popupCalendar.hidden) {
                        // will be closed
                        return true;
                    } else if (e.calendarClosed) {
                        // was already closed
                        return true;
                    } else if (e.originalEvent && e.originalEvent.calendarClosed) {
                        // wrapped native event
                        return true;
                    }
                    return false;
                }
                // TODO THIS IS BAD! we need to be doing this in a way that doesn't require us to extend this freakin' if
                // TODO just because another layering component happens to be placed within the dialog
                // TODO this should be unit-tested once we have JS unit tests. Selenium-testing this is an over-kill!
                if (e.which === jQuery.ui.keyCode.ESCAPE && !AJS.InlineLayer.current && !JIRA.Dropdown.current &&
                        !AJS.InlineDialog.current && !calendarClosingBy(e)) {
                    this.handleCancel();
                }
            }
        }
    },

    handleCancel: function () {
        this.hide();
    },

    _get$loadingIndicator: function () {
        if (!JIRA.Dialog.$loadingIndicator) {
            JIRA.Dialog.$loadingIndicator = this._render("loadingIndicator").css("zIndex", 9999).appendTo("body");
        }
        return JIRA.Dialog.$loadingIndicator;
    },

    _showloadingIndicator: function () {

        var instance = this,
            heightOfSprite = 440,
            currentOffsetOfSprite = 0;

        clearInterval(this.loadingTimer);

        this._get$loadingIndicator().show();

        this.loadingTimer = window.setInterval(function () {

            if (currentOffsetOfSprite === heightOfSprite) {
                currentOffsetOfSprite = 0;
            }
            currentOffsetOfSprite = currentOffsetOfSprite + 40;
            instance._get$loadingIndicator().css("backgroundPosition", "0 -" + currentOffsetOfSprite + "px");
        }, 50);
    },

    _hideloadingIndicator: function () {
        clearInterval(this.loadingTimer);
        this._get$loadingIndicator().hide();
    },

    _positionInCenter: function () {

        var $window = AJS.$(window),
            $popup = this.get$popup(),
            $container = this.getContentContainer(),
            $contentArea =  this.getContentArea();


        var cushion = 40;
        var windowHeight = $window.height();

        if (typeof this.options.width === "number") {
            $popup.width(this.options.width);
        }

        $popup.css({
            marginLeft: -$popup.outerWidth() / 2,
            marginTop: Math.max(-$popup.outerHeight() / 2, cushion - windowHeight / 2)
        });

        var top = 0;
        var el = $popup[0];
        while (el) {
            top += el.offsetTop;
            el = el.offsetParent;
        }

        var popupMaxHeight = windowHeight - top - cushion;
        var padding = parseInt($contentArea.css("padding-top"), 10) + parseInt($contentArea.css("padding-bottom"), 10);

        $contentArea.css("maxHeight", "");

        var contentMaxHeight = popupMaxHeight - ($popup.outerHeight() - $container.outerHeight()) - padding;

        $contentArea.css('maxHeight', popupMaxHeight - ($popup.outerHeight() - $container.outerHeight()) - padding);

        AJS.$(this).trigger("contentMaxHeightChanged", [contentMaxHeight])
    },

    /**
     * Gets scrollable content area. A max height will be applied to these areas
     */

    getContentArea: function () {
        return this.$popup.find(".content-body, .remove-me");
    },

    /**
     * Gets content container. Should wrap all content areas, used to calculated max height for content areas.
     */
    getContentContainer: function () {

        var $container = this.$popup.find(".content-area-container");

        if ($container.length === 1) {
            return $container;
        } else {
            return this.$popup.find(".content-body, .remove-me");
        }
    },

    get$popup: function () {
        if (!this.$popup) {
            this.$popup = this._render("popup").appendTo("body");
            this.$popup.addClass("box-shadow");
        }
        return this.$popup;
    },

    get$popupContent: function () {
        if (!this.$popupContent) {
            this.$popupContent = this._render("popupContent").appendTo(this.get$popup());
        }
        return this.$popupContent;
    },

    get$popupHeading: function() {
        if (!this.$popupHeading) {
            this.$popupHeading = this._render("popupHeading").prependTo(this.get$popup());
        }
        return this.$popupHeading;
    },

    getLoadingIndicator: function () {
        return this.get$popupContent().find(".throbber:last");
    },

    showFooterLoadingIndicator: function () {

        var $throbber = this.getLoadingIndicator();

        if ($throbber.length) {
            $throbber.addClass("loading");
        }
    },

    hideFooterLoadingIndicator: function () {

        var $throbber = this.getLoadingIndicator();

        if ($throbber.length) {
            $throbber.removeClass("loading");
        }
    },

    _watchTab: function(e) {
        var $dialog_selectable,
            $first_selectable,
            $last_selectable;
        // make sure we are still in the dialog.
        if (AJS.$(e.target).parents(this.get$popupContent()).length > 0) {
            if (AJS.$.browser.safari ) {
                // Safari does not allow tabbing to links, although links can have focus ... stupid safari
                $dialog_selectable = AJS.$(':input:visible:enabled, :checkbox:visible:enabled, :radio:visible:enabled', '.aui-popup.aui-dialog-open');
            } else {
                $dialog_selectable = AJS.$('a:visible, :input:visible:enabled, :checkbox:visible:enabled, :radio:visible:enabled', '.aui-popup.aui-dialog-open');
            }
            $first_selectable = $dialog_selectable.first();
            $last_selectable = $dialog_selectable.last();

            if((e.target == $first_selectable[0] && e.shiftKey) ||
                (e.target == $last_selectable[0] && !e.shiftKey))  {
                if (e.shiftKey) {
                    $last_selectable.focus();
                }
                else {
                    $first_selectable.focus();
                }
                e.preventDefault();
            }
        }
    },

    show: function () {
        var myEvent = new AJS.$.Event("beforeShow");

        if (JIRA.Dialog.current === this) {
            return false;
        }

        AJS.$(this).trigger(myEvent);

        if (myEvent.result === false) {
            return false;
        }

        //Fix this when JRADEV-2814 is done.
        if (AJS.InlineLayer.current) {
            AJS.InlineLayer.current.hide();
        }

        if (AJS.dropDown.current) {
            AJS.dropDown.current.hide();
        }

        if (JIRA.Dialog.current) {

            if (JIRA.Dialog.current.options.stacked) {

                var prev = JIRA.Dialog.current,
                    prevCachedValue =  prev.options.cached,
                    hideHandler = this.hide;

                prev.options.cached = true;

                JIRA.Dialog.current.hide(false);

                this.hide = function () {
                    prev.options.cached = prevCachedValue;
                    this.hide = hideHandler;
                    prev.show();
                };

            } else {
                JIRA.Dialog.current.hide(false);
            }
        } else {
            AJS.dim(false);
        }

        JIRA.Dialog.current = this;

        var $popup = this.get$popup().addClass("aui-dialog-open");

        if (this.options.type !== "blank" && !this.$content) {
            this._setContent();
        } else {
            $popup.show();
            this._positionInCenter();
        }

        this.tabWatcher = function (e) {
            if (e.keyCode == 9) { // TAB
                JIRA.Dialog.current._watchTab(e);
            }
        };

        AJS.$(document).bind('keydown', this.tabWatcher);

        AJS.$(this).trigger("Dialog.show");

        AJS.disableKeyboardScrolling(); // stop up and down keys scrolling page under popup
    },

    notifyOfNewContent: function () {
        if (this.$content) {
            this.decorateContent(); // Make sure title is updated
            this._positionInCenter(); // our content height might have changed so take up available realestate
            jQuery(document).trigger("dialogContentReady", [this]);
        }
    },

    destroy: function () {
        this.$popup.remove();
        delete this.$popup;
        delete this.$popupContent;
        delete this.$popupHeading;
        delete this.$content;
    },

    hide: function (undim) {

        if (JIRA.Dialog.current !== this) {
            return false;
        }

        var atlToken = AJS.$(".aui-dialog-open  input[name=atl_token]").attr("value");
        if ( atlToken !== undefined) {
             JIRA.XSRF.updateTokenOnPage(atlToken);
        }

        if (this.options.cached === false) {
            this.destroy();
        }

        if (undim !== false) {
            AJS.undim();
        }

        this.get$popup().removeClass("aui-dialog-open").removeClass("aui-dialog-content-ready").hide();
        this._hideloadingIndicator();

        JIRA.Dialog.current = null;

        AJS.$(document).trigger("hideAllLayers");

        AJS.$(this).trigger("Dialog.hide");

        AJS.enableKeyboardScrolling(); // allow up and down keys to scroll page again

        if (this.tabWatcher) {
            AJS.$(document).unbind('keydown', this.tabWatcher);
        }
    },

    addHeading: function(heading) {
        this.get$popupHeading().html(heading);
    },

    onContentReady: function (func) {
        if (AJS.$.isFunction(func)) {
            this.onContentReadyCallbacks.push(func);
        }
    }

});

/**
 * This is a bridge so that all AJS.popup's do in fact use JIRA.Dialog
 * @deprecated
 */
AJS.popup = function (options, width, id) {

    // for backwards-compatibility
    if (typeof options !== "object") {
        options = {
            width: arguments[0],
            height: arguments[1],
            id: arguments[2]
        };
    }

    var popup = new JIRA.Dialog({
        type: "blank",
        id: options.id || id,
        width: options.width,
        cached: true
    });

    return {

        element: popup.get$popup(),

        show: function () {
            popup.show();
        },
        hide: function () {
            popup.hide();
        },
        changeSize: function () {
            popup._positionInCenter();
        },

        remove: function () {
            this.element.remove();
            this.element = null;
        },

        /**
         * Not supported in bridge
         * @method disable
        */
        disable: function() {},

        /**
         * Not supported in bridge
         * @method enable
        */
        enable: function() {}
    };
};

JIRA.Dialog.WIDTH_PRESETS = {
    small: 360,
    medium: 540,
    large: 810
};

/** Preserve legacy namespace
    @deprecated AJS.FlexiPopup */
AJS.namespace("AJS.FlexiPopup", null, JIRA.Dialog);
