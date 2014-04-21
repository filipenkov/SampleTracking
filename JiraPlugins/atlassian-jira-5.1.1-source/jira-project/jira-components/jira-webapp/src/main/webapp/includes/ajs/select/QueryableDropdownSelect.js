/**
 * A dropdown that can be queried and it's links selected via keyboard. Dropdown contents retrieved via AJAX.
 *
 * @constructor AJS.QueryableDropdownSelect
 * @extends AJS.Control
 */
AJS.QueryableDropdownSelect = AJS.Control.extend({

    /**
     *  A request will not be fired and suggestions will not reset if any of these keys are inputted.
     *
     * @property {Array} INVALID_KEYS
     */
    INVALID_KEYS: {
        "Shift": true,
        "Esc": true,
        "Right": true
    },

    /**
     * Overrides default options with user options. Inserts an input field before dropdown.
     *
     * @param {Object} options
     */
    init: function (options) {

        var instance = this;

        this._setOptions(options);

        // setting default properties
        this._queuedRequest = 0;
        this.suggestionsVisible = false;

        // if the minQueryLength property comes from the DOM we need to convert it to an int
        if (this.options.ajaxOptions.minQueryLength) {
            this.options.ajaxOptions.minQueryLength = parseInt(this.options.ajaxOptions.minQueryLength, 10);
        }

        this._createFurniture();

        if (this.options.dropdownController) {
            this.dropdownController = this.options.dropdownController;
        } else {
            this.dropdownController = AJS.InlineLayer.create({
                offsetTarget: this.$field,
                width: this.$field.innerWidth(),
                content: options.element
            });
        }

        this.dropdownController.onhide(function () {
           instance.hideSuggestions();
        });

        this.listController = new AJS.List({
            containerSelector: options.element,
            groupSelector: "ul.aui-list-section",
            matchingStategy: this.options.matchingStategy,
            eventTarget: this.$field,
            selectionHandler: function () {
                // prevent form field from being dirty
                instance.$field.val(AJS.I18n.getText("common.concepts.loading")).css("color", "#999");
                instance.hideSuggestions();
                return true;
            }
        });

        if (this.options.width) {
            this.setFieldWidth(this.options.width);
        }


        this._assignEventsToFurniture();

        if(this.options.loadOnInit) {
            this.suggestionsDisabled = true;
            this._requestThenResetSuggestions();
        }
    },

    /**
     * Sets field width
     *
     * @param {Number} width - field width
     */
    setFieldWidth: function (width) {

        this.$container.css({
            width: width,
            minWidth: width
        });

    },


    /**
     * Show an error message near this field
     *
     * @param {String} value (optional) - The user input text responsible for the error
     */
    showErrorMessage: function (value) {

        var $container = this.$container.parent(".field-group"); // aui container

        this.hideErrorMessage() // remove old

        this.$errorMessage.text(AJS.format(this.options.errorMessage, value || this.getQueryVal()));

        if ($container.length === 1) {
            $container.append(this.$errorMessage);
            return;
        }

        if ($container.length === 0) {
            $container = this.$container.parent(".frother-control-renderer"); // not in aui but JIRA renderer
        }

        if ($container.length === 1) {
            this.$errorMessage.prependTo($container);
            return;
        }

        if ($container.length === 0) {
            this.$container.parent().append(this.$errorMessage);
        }
    },

    /**
     * @method hideErrorMessage - Hide the error message-
     */
    hideErrorMessage: function() {
        if (this.$errorMessage) {
            this.$errorMessage.remove();
        }
        this.$container.parent().find(".error").remove(); // remove all error message from server also
    },

    /**
     * Gets default options
     *
     * @method _getDefaultOptions
     * @private
     * @return {Object}
     */
    _getDefaultOptions: function () {
        return {
            id: "default",
            ajaxOptions: {
                data: { },
                dataType: "json",
                minQueryLength: 0
            },
            // keyInputPeriod: expected milliseconds between consecutive keystrokes
            // If this user types faster than this, no requests will be issued until they slow down.
            keyInputPeriod: 75,
            // localListLiveUpdateLimit: Won't search for new options if there are more options than this value
            localListLiveUpdateLimit: 25,
            // Only search for new options locally after this delay.
            localListLiveUpdateDelay: 150
        };
    },

     /**
      * Gets ajaxOptions. We get them from the options object but copy it so that if we modify and properties it
      * won't change those on the options object through reference.
      *
      * @method getAjaxOptions
      * @return {Object}
      */
    getAjaxOptions: function () {
        return AJS.copyObject(this.options.ajaxOptions); // new object, no references
    },

    /**
     * Issue an ajax request.
     *
     * @method issueRequest
     */
    issueRequest: function() {

        var instance = this,
            ajaxOptions = this.getAjaxOptions(),
            originalQuery = AJS.$.trim(this.getQueryVal());

        // If data property is a function, it returns a custom object seeded with the query string.
        if (typeof ajaxOptions.data === 'function') {
            ajaxOptions.data = ajaxOptions.data.call(this, originalQuery);
        } else {
            ajaxOptions.data.query = originalQuery;
        }

        ajaxOptions.complete = function (xhr, textStatus, smartAjaxResult) {

            instance.outstandingRequest = null;

            if (!instance.$container.is(":visible")) {
                // If this field was removed from the page, the suggestions should not be displayed.
                return;
            }

            if (smartAjaxResult.successful) {
                instance._handleServerSuccess(smartAjaxResult, originalQuery);
            } else if (!smartAjaxResult.aborted) {
                instance.hideSuggestions();
                instance._handleServerError(smartAjaxResult);
            }
        };

        this.outstandingRequest = JIRA.SmartAjax.makeRequest(ajaxOptions);

        AJS.$(this.outstandingRequest).throbber({
            target: this.$dropDownIcon,
            isLatentThreshold: 500
        });
    },

    /**
     * This is called when the AJAX request is finished, to ensure we are talking about the right request
     *
     * @method _handleServerSuccess
     * @param {Object} smartAjaxResult - the smart AJAX result object in play
     */
    _handleServerSuccess : function(smartAjaxResult, originalQuery) {
        if (!AJS.isSelenium() && !AJS.elementIsFocused(this.$field)) {
            return;
        }
        var freshQuery = this.getQueryVal() == originalQuery;
        if (freshQuery || !this.options.ajaxOptions.query || this.options.loadOnInit) {
            this._handleServerSuggestions(smartAjaxResult.data, originalQuery);

            if (freshQuery) {
                // Lets us wait for the correct suggestions with webdriver
                this.$container.attr("data-query", originalQuery);
            }
        }
    },

    /**
     * This is called when an error has happened during the AJAX request.  By default
     * we show an alert messages but other instances can do more specific error handling
     *
     * @method _handleServerError
     * @param {Object} smartAjaxResult  - the smart AJAX result object in play
     */
    _handleServerError : function(smartAjaxResult) {
        // we have an error of some sort so show an error message
        var errMsg = JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult,{ alert : true });
        /* [alert] */
        alert(errMsg);
        /* [alert] end */
    },

    /**
     * Appends furniture around specified dropdown element. This includes:
     *
     * <ul>
     *  <li>Field - text field used fro querying</li>
     *  <li>Container - Wrapper used to contain all furniture</li>
     *  <li>Dropdown Icon - Button in right of field used to open dropdown via mouse</li>
     * </ul>
     *
     * @method _createFurniture
     * @private
     */
    _createFurniture: function () {
        this.$container = this._render("container").insertBefore(this.options.element);
        this.$field = this._render("field").appendTo(this.$container);
        this.$dropDownIcon = this._render("dropdownAndLoadingIcon", this._hasDropdownButton()).appendTo(this.$container);
        this.$suggestionsContainer = this._render("suggestionsContainer");
        if (this.options.overlabel) {
            this.$overlabel = this._render("overlabel").insertBefore(this.$field);
            this.$overlabel.overlabel();
        }
    },

    /**
     * Whether or not to display dropdown icon/button
     *
     * @method _hasDropdownButton
     * @protected
     * @return {Boolean}
     */
    _hasDropdownButton: function () {
        if (this.options.showDropdownButton === false) {
            return false;
        } else if (this.options.showDropdownButton || this.options.ajaxOptions.minQueryLength === 0) {
            return true;
        }
    },

    /**
     * Assigns events to DOM nodes
     *
     * @method _assignEventsToFurniture
     * @protected
     */
    _assignEventsToFurniture: function () {

        var instance = this;

        this._assignEvents("ignoreBlurElement", this.dropdownController.$layer);

        this._assignEvents("container", this.$container);

        if (this._hasDropdownButton()) {
            this._assignEvents("ignoreBlurElement", this.$dropDownIcon);
            this._assignEvents("dropdownAndLoadingIcon", this.$dropDownIcon);
        }

        // if this control is created as the result of a keydown event then we do no want to catch keyup or keypress for a moment
        setTimeout(function() {
            instance._assignEvents("field", instance.$field);
            instance._assignEvents("keys", instance.$field);
        }, 15);
    },

    /**
     * Returns true if there is cheched request, and the query setting is false (meaning you would not like to go back to
     * the server for every keystroke)
     *
     * @method _isValidRequest
     * @return {Boolean}
     */
    _useCachedRequest: function () {
        return !!(this.cachedList && !this.options.ajaxOptions.query);
    },

    /**
     * Returns true if the query setting is true (meaning you would like to go back to the server for every keystroke)
     * Or if there has not been any request yet, so we will just get the data once thanks sir.
     *
     * @method _isValidRequest
     * @return {Boolean}
     */
    _isValidRequest: function () {
        return this.options.ajaxOptions.query || (!this.cachedList && !this.outstandingRequest);
    },

    /**
     * Requests JSON formatted suggestions from specified resource. Resource is sepecified in the ajaxOptions object
     * passed to the constructed during initialization.
     *
     * If the query option of ajaxOptions is set to true, an ajax request will be made for every keypress. Otherwise
     * ajax request will be made only the first time the dropdown is shown.
     *
     * @method _requestThenResetSuggestions
     * @private
     * @param {Boolean} ignoreBuffer - flag to specify that gating by keyInputPeriod should be circumvented
     * @param {String} query - (optional) query that may have already been extracted from field
     */
    _requestThenResetSuggestions: function (ignoreBuffer, query) {

        var instance = this;

        // More information needs to be communicated between data retriever (AJS.QueryableDropdownSelect) and suggestion
        // handler (AJS.List). A refactor should address this problem, but in the meantime this hack seems to work
        // best. Fixes JRADEV-3583.
        this.listController._latestQuery = query || AJS.$.trim(this.getQueryVal());

        if (this._useCachedRequest()) {
            this._handleServerSuggestions(this.cachedList);

        } else if (this._isValidRequest()) {

            if (ignoreBuffer && this.outstandingRequest) {
                this.outstandingRequest.abort();
                this.outstandingRequest = null;
            }

            clearTimeout(this._queuedRequest); // cancel any queued requests

            if (!this.outstandingRequest) {
                this.issueRequest();
            } else {
                this._queuedRequest = setTimeout(function () {
                    instance._requestThenResetSuggestions(ignoreBuffer);
                }, this.options.keyInputPeriod);
            }
        }
    },

    /**
     * Shows suggestions
     */
    showSuggestions: function () {
        this._requestThenResetSuggestions(true);
    },


    /**
     * Propagates list with suggestions from the server or cache, performing necessary formatting of data.
     *
     * @method _handleServerSuggestions
     * @private
     * @param {Object} data
     * @param {String} originalQuery
     */
    _handleServerSuggestions: function (data, query) {
        if (this.cachedList !== data) {
            if (this._formatResponse) {
                data = this._formatResponse(data, query);
            } else if (this.options.ajaxOptions.formatResponse) {
                data = this.options.ajaxOptions.formatResponse.call(this, data, query);
            }
            this.cachedList = data;
        }

        // If the server is filtering every query, no need to filter here.
        var context = {
            groupId: this.options.serverDataGroupId,  // may be undefined
            filter: !this.options.ajaxOptions.query
        };
        this._setSuggestions(this.cachedList, context);
    },

    /**
     *
     * Sets suggestions and shows them
     *
     * @method _setSuggestions
     * @param {Array} data
     * @param {String} groupId - the id of a group to target with the suggestions
     * @param {boolean} filter - true if the data should be filtered on any query entered.
     *                           Usually false if from a server response.
     *
     *                           * @param {String} groupId - a group in the model to set suggestions inside of. If blank, set for the entire model.
     * @param {boolean} filter - true if the data should be filtered on any query entered
     */
    _setSuggestions: function (data, context) {

        //if the user hasn't entered anything yet and we load the data on init then don't show any suggestions!
        if (this.suggestionsDisabled) {
            this.suggestionsDisabled = false;
            return;
        }

        // JRADEV-2053: If the field is no longer focused (i.e. the user has already tabbed away) don't set
        // suggestions as it will bring focus back to this field.
        this.suggestionsVisible = true;

        var query = this.getQueryVal();
        if (data) {

            this.dropdownController.show();
            this.dropdownController.setWidth(this.$field.innerWidth());
            this.dropdownController.setPosition();

            context || (context = {});
            context.query = query;

            // Only filter for:
            // a) server data where we still want to filter it, OR
            // b) local data with a query string
            if (typeof context.filter === 'undefined') {
                context.filter = !!query;
            }
            this.listController.generateListFromJSON(data, context);
            this.listController.enable();

        } else {
            this.hideSuggestions();
        }

        // Makes WebDriver wait for the correct suggestions
        this.$container.attr("data-query", query);
    },

    /**
     * Fades out & disables interactions with field
     */
    disable: function () {
        if (!this.disabled) {
            this.$container.addClass("aui-disabled");
            // The disabledBlanket is necessary to prevent clicks on other elements positioned over the field.
            this.$disabledBlanket = this._render("disabledBlanket").appendTo(this.$container);
            this.$field.attr('disabled', true);
            this.dropdownController.hide();
            this.disabled = true;
        }
    },

    /**
     * Enables interactions with field
     */
    enable: function () {
        if (this.disabled) {
            this.$container.removeClass("aui-disabled");
            this.$disabledBlanket.remove();
            this.$field.attr('disabled', false);
            this.disabled = false;
        }
    },

    /**
     * Gets input field value
     *
     * @return {String}
     */
    getQueryVal: function () {
        return this.$field.val();
    },

    _isValidInput: function (event) {
        return this.$field.is(":visible") && !this.INVALID_KEYS[event.key];
    },

    /**
     * Hides list if the is no value in input, otherwise shows and resets suggestions in dropdown
     *
     * @method _handleCharacterInput
     * @param {Boolean} ignoreBuffer - Do not wait for interval between key strokes for requests
     * @param {Boolean} ignoreQueryLength - Ignore min query length before request is issued
     * @private
     */
    _handleCharacterInput: function (ignoreBuffer, ignoreQueryLength) {
        this.suggestionsDisabled = false;

        var query = AJS.$.trim(this.getQueryVal());
        var show = ignoreQueryLength || (query.length >= this.options.ajaxOptions.minQueryLength);

        if (show) {
            var sendRequest = this.options.ajaxOptions.url && !(this.options.ajaxOptions.noQueryNoRequest && !query);
            if (sendRequest) {
                //as soon as a key is pressed remove the noloading class so that if we're still retrieving data
                //from the server the spinner is shown!this.$field.val()
                this.$dropDownIcon.removeClass("noloading");
                this._requestThenResetSuggestions(ignoreBuffer);
            } else if (AJS.$.isFunction(this.options.suggestions)) {
                this._setSuggestions(this.options.suggestions());
            }

            // Local data group-id is the id of an <optgroup> that should be filtered from local data.
            // If not present and a request has been sent, there is no need to query the model - just wait for the
            // server response.
            // If a group id is present, let the server request do its thing when it returns, but for now filter the
            // local model's matching group.
            var localDataGroupId = this.options.localDataGroupId;
            if ((this.options.suggestions || sendRequest) && !localDataGroupId) return;

            // Either this dropdown only uses local data, or a combination of local and remote data.

            if (this.inlineBufferTimeout) {
                clearTimeout(this.inlineBufferTimeout);
            }

            var data = this.model.getUnSelectedDescriptors({groupId: localDataGroupId});
            var context = {
                groupId: localDataGroupId,
                filter: true
            };
            // TODO - existing bug here, descriptors.length might be 1 with a group and the group has >
            // localListLiveUpdateLimit items inside.
            if (data.length >= this.options.localListLiveUpdateLimit) {
                var instance = this;
                this.inlineBufferTimeout = setTimeout(function() {
                    instance._setSuggestions(data, context);
                }, this.options.localListLiveUpdateDelay);
            } else {
                this._setSuggestions(data, context);
            }
        } else {
            this._setSuggestions();
        }
    },

    /**
     * Handles down key
     *
     * @method _handleDown
     * @param {Event} e
     */
    _handleDown: function(e) {
        if (!this.suggestionsVisible) {
            this.listController._latestQuery = ""; // JRADEV-9009 Resetting query value
            this._handleCharacterInput(true, true);
        }
        e.preventDefault();
    },

    /**
     * Cancels and pending or outstanding requests
     *
     * @method _rejectPendingRequests
     * @protected
     */
    _rejectPendingRequests: function () {
        if (this.outstandingRequest) {
            this.outstandingRequest.abort();
        }
        clearTimeout(this._queuedRequest);
    },

    /**
     * Hides suggestions
     *
     * @method hideSuggestions
     */
    hideSuggestions: function () {

        if (!this.suggestionsVisible) {
            return;
        }
        clearTimeout(this.inlineBufferTimeout);
        this._rejectPendingRequests();
        this.suggestionsVisible = false;
        this.$dropDownIcon.addClass("noloading");
        this.dropdownController.hide();
        this.listController.disable();
    },

    _deactivate: function () {
        this.hideSuggestions();
    },

    /**
     * Handles Escape key
     *
     * @method _handleEscape
     * @param {Event} e
     */
    _handleEscape: function (e) {
        if (this.suggestionsVisible) {
            e.stopPropagation();
            if (e.type === "keyup") {
                this.hideSuggestions();
                if (AJS.$.browser.msie) {
                    // IE - field has already received the event and lost focus (default browser behaviour)
                    this.$field.focus();
                }
            }
        }
    },

    /**
     * Selects currently focused suggestion, if there is one
     */
    acceptFocusedSuggestion: function () {
        var focused = this.listController.getFocused();
        if (focused.length !== 0 && focused.is(":visible")) {
            this.listController._acceptSuggestion(focused)
        }
    },

    keys: {
        "Down": function (e) {
            if (this._hasDropdownButton()) {
                this._handleDown(e);
            }
        },
        "Up": function (e) {
            e.preventDefault();
        },
        "Return": function (e) {
            e.preventDefault();
        }
    },

    onEdit: function (e) {
        var instance = this;

        if (e.key === "\r") {
            return;
        }

        if (/paste|cut/.test(e.type)) {
            window.setTimeout(function () {
                // input doesn't actually change for a little bit
                instance._handleCharacterInput();
            }, 10);
        } else {
            // delay until character is insterted into field
            this.$field.one("keyup", function () {
                instance._handleCharacterInput();
            });
        }
    },

    _events: {

        dropdownAndLoadingIcon: {
            click: function (e) {
                if (this.suggestionsVisible) {
                    this.hideSuggestions();
                } else {
                    this._handleDown(e);
                    this.$field.focus();
                }
                e.stopPropagation();
            }
        },

        container: {
            disable : function () {
                this.disable();
            },
            enable: function () {
                this.enable();
            }
        },

        field: {
            blur: function () {
                this._deactivate();
            },
            click: function (e) {
                e.stopPropagation();
            },
            "cut paste": function (e) {
                var instance = this;
                instance.onEdit(e);
            },
            "keydown keyup": function (e) {
                if (e.keyCode === 27) {
                    this._handleEscape(e);
                }
            }
        },

        keys: {
            "aui:keydown aui:keypress": function (event) {
                this._handleKeyEvent(event);
            }
        },

        ignoreBlurElement: {
            mousedown: function (e) {

                var field = this.$field.get(0);
                /**
                 * Performs an IE specific "preventDefault"
                 */
                function onbeforedeactivate(event) {
                    event.returnValue = false;
                }

                // Preventing the default action of "mousedown" events stops the
                // activeElement losing focus in all non-IE. We need to use a funky
                // workaround for IE which allows the field to lose focus briefly
                // so that UI controls like scrollbars are still interactive.
                if (typeof document.addEventListener === "undefined") {
                    field.attachEvent("onbeforedeactivate", onbeforedeactivate);
                    setTimeout(function() {
                        field.detachEvent("onbeforedeactivate", onbeforedeactivate);
                    }, 0);
                } else {
                    e.preventDefault()
                }
            }
        }
    },

    _renders: {

        disabledBlanket: function () {
            return AJS.$("<div class='aui-disabled-blanket' />").height(this.$field.outerHeight());
        },
        overlabel: function () {
            return AJS.$("<span id='" + this.options.id + "-overlabel' data-target='" + this.options.id + "-field' class='overlabel'>" + this.options.overlabel + "</span>" );
        },
        field: function () {
            return AJS.$("<input class='text' id='" + this.options.id + "-field' type='text' autocomplete='off' />");
        },
        container: function () {
            return AJS.$("<div class='queryable-select' id='" + this.options.id + "-queryable-container' />");
        },
        dropdownAndLoadingIcon: function (showDropdown) {
            var $element = AJS.$('<span class="icon noloading"><span>More</span></span>');
            if  (showDropdown) {
                $element.addClass("drop-menu");
            }
            return $element;
        },
        suggestionsContainer : function () {
            return AJS.$("<div class='aui-list' id='" + this.options.id + "' tabindex='-1'></div>");
        }
    }

});

/** Preserve legacy namespace
    @deprecated AJS.QueryableDropdown */
AJS.namespace("AJS.QueryableDropdown", null, AJS.QueryableDropdownSelect);
