/**
 * @constructor
 */
JIRA.FormDialog = JIRA.Dialog.extend({

    _getDefaultOptions: function () {
        return AJS.$.extend(this._super(), {
            autoClose: false,
            targetUrl: "",
            handleRedirect: false,
            onUnSuccessfulSubmit : function() {},
            onSuccessfulSubmit : function() {},

            /**
             * By default if we have been told by the markup to go to a specific URL, then we do
             * otherwise we reload the current page by going to it again.
             */
            onDialogFinished : function() {
                // Always close the dialog before attempting to unload the page, in case
                // dirty-form or some other onunload check blocks it. Be sure to get the target first,
                // though, because it's stored in the dialog's DOM and is lost when the dialog is hidden.
                var targetUrl = this._getTargetUrlValue();
                this.hide();

                if (targetUrl) {
                    AJS.$(document).trigger('page-unload.location-change.from-dialog');
                    window.location.href = targetUrl;
                } else {
                    AJS.$(document).trigger('page-unload.refresh.from-dialog');
                    AJS.reloadViaWindowLocation(window.location.href);
                }
            },

            submitAjaxOptions: {
                type: "post",
                data: {
                    inline: true,
                    decorator: "dialog"
                },
                dataType: "html"
            }
        });
    },

    _getFormDataAsObject: function ()
    {
        var fieldValues = {};
        // save form configuration to user prefs
        AJS.$(this.$form.serializeArray()).each(function(){
            var fieldVal = fieldValues[this.name];
            if (!fieldVal) {
                fieldVal = this.value;
            } else if (AJS.$.isArray(fieldVal)) {
                fieldVal.push(this.value);
            } else {
                fieldVal = [fieldVal, this.value];
            }
            fieldValues[this.name] = fieldVal;
        });
        return fieldValues;
    },


    _getRelativePath: function () {
        return parseUri(this.options.url || this.$activeTrigger.attr("href")).directory;
    },

    _getPath: function (action) {
        var relPath = this._getRelativePath();
        if (action.indexOf(relPath)==0) {
            return action;
        }  else {
            return relPath + action;
        }
    },

    _submitForm: function (e) {
        this.cancelled = false;
        this.xhr = null;
        this.redirected = false;
        this.serverIsDone = false;
        this.$form.addClass("submitting");

        var instance = this, defaultRequestOptions = AJS.$.extend(true, {}, this.options.submitAjaxOptions),
            requestOptions = AJS.$.extend(true, defaultRequestOptions, {
                url: this._getPath(this.$form.attr("action")),
                data: this._getFormDataAsObject(),
                complete: function (xhr, textStatus, smartAjaxResult)
                {
                    instance.hideFooterLoadingIndicator();

                    if (! instance.cancelled)
                    {
                        if (smartAjaxResult.successful)
                        {
                            instance.$form.trigger("fakesubmit");
                            instance._handleServerSuccess(smartAjaxResult.data, xhr, textStatus, smartAjaxResult);
                            //
                            // if we have already been redirected then the page is asynchronously unloading and going elsewhere
                            // and hence we should not do the complete processing since its pointless and could only do harm
                            //
                            if (! instance.redirected)
                            {
                                instance._handleSubmitResponse(smartAjaxResult.data, xhr, smartAjaxResult);
                            }
                        }
                        else
                        {
                            instance._handleServerError(xhr, textStatus, smartAjaxResult.errorThrown, smartAjaxResult);
                        }
                    }
                    //The form may have changed. Lets just make sure there us no 'submitting' class to be extra sure.
                    instance.$form.removeClass("submitting");
                }
            });

        this.showFooterLoadingIndicator();

        this.xhr = JIRA.SmartAjax.makeRequest(requestOptions);

        e.preventDefault();
    },

    /**
     * This is called when the AJAX 'error' code path is taken.  It takes the response text and plonks it into
     * the dialog as content.
     *
     * @param xhr the AJAX bad boy
     * @param textStatus the status
     * @param errorThrown the error in play
     * @param smartAjaxResult the smart AJAX result object we need
     */
    _handleServerError: function (xhr, textStatus, errorThrown, smartAjaxResult)
    {
        if (this.options.onUnSuccessfulSubmit)
        {
            this.options.onUnSuccessfulSubmit.call(xhr, textStatus, errorThrown, smartAjaxResult);
        }
        // we stick this in as an error message at the top of the content if we can otherwise
        // we replace the content.  The former allows the form details to be saved via copy and paste
        // handy for really large comments say.  We only do this if we dont have any data to display
        var errorContent = JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult, true);
        var content$ = this.get$popupContent().find(".content-body");
        if(content$.length !== 1) {
            content$ = this.get$popupContent();
        }
        var insertErrMsg = content$.length == 1 && ! smartAjaxResult.hasData;
        if (insertErrMsg) {
            content$.prepend(errorContent);
        } else {
            this._setContent(errorContent);
        }
    },

    /**
     * This is called on the AJAX 'success' code path.  At this stage its a 200'ish message.
     *
     * If there is content and its has the magic 'redirect' we handle the redirect
     * then we will redirect to the specified place!
     *
     * @param xhr the AJAX bad boy
     * @param data the response body
     * @param textStatus the status
     * @param smartAjaxResult the smart AJAX result object we need
     */
    _handleServerSuccess: function (data, xhr, textStatus, smartAjaxResult) {
        //
        // Check the status of the X-Atlassian-Dialog-Control header to see if we are done
        //
        var instructions = this._detectRedirectInstructions(xhr);
        this.serverIsDone = instructions.serverIsDone;

        if (instructions.redirectUrl) {
            if (this.options.onSuccessfulSubmit) {
                this.options.onSuccessfulSubmit.call(this, data, xhr, textStatus, smartAjaxResult);
            }
            this._performRedirect(instructions.redirectUrl);
        } else {
            this._setContent(data);
        }
    },

    /**
     * This is called when the original AJAX 'complete' code path is taken with a serverIsDone = true.
     *
     * @param data the response body
     * @param xhr the AJAX bad boy
     * @param smartAjaxResult the smart AJAX result object we need
     */
    _handleInitialDoneResponse: function (data, xhr, smartAjaxResult) {
        this._handleSubmitResponse(data, xhr, smartAjaxResult);
    },

    /**
     * This is called when the AJAX 'complete' code path is taken.
     *
     * @param data the response body
     * @param xhr the AJAX bad boy
     * @param smartAjaxResult the smart AJAX result object we need
     */
    _handleSubmitResponse: function (data, xhr, smartAjaxResult) {
        if (this.serverIsDone) {
            if (this.options.onSuccessfulSubmit) {
                this.options.onSuccessfulSubmit.call(this, data, xhr, smartAjaxResult);
            }
            if (this.options.autoClose) {
                this.hide();
            }
            if (this.options.onDialogFinished) {
                this.options.onDialogFinished.call(this, data, xhr, smartAjaxResult);
            }
        }
    },

    /**
     * This will hide the dialog and redirect the page to the specified url
     * @param url {String} the url to redirect to
     */
    _performRedirect: function (url) {
        this.hide();
        this.redirected = true;
        this._super(url);
    },

    _hasTargetUrl: function() {
        return this._getTargetUrlHolder().length > 0;
    },

    _getTargetUrlHolder: function() {
        return AJS.$(this.options.targetUrl);
    },

    _getTargetUrlValue: function() {
        return this._getTargetUrlHolder().val();
    },

    decorateContent: function () {

        var instance = this, $formHeading, $buttons, $cancel, $buttonContainer, $closeLink;

        this.$form = AJS.$("form", this.get$popupContent());
        $formHeading = AJS.$(":header:first", this.get$popupContent());
        if($formHeading.length > 0) {
            // append to heading but retain event handlers
            this.addHeading($formHeading.contents());
            $formHeading.remove();
        }

        this.$form.submit(function (e) {
            if (instance.$form.trigger("before-submit", [e, instance])) {
                var submitButtons = AJS.$(':submit', instance.$form);
                submitButtons.attr('disabled','disabled');

                if (instance.options.submitHandler) {
                    instance.showFooterLoadingIndicator();
                    instance.options.submitHandler.call(instance, e, function () {
                        instance.hideFooterLoadingIndicator();
                    });
                } else {
                    instance._submitForm(e);
                }
            }
        });

        this.$form.find("input[type=file]:not('.ignore-inline-attach')").inlineAttach();

        $cancel = AJS.$(".cancel", this.get$popupContent());
        $cancel.click(function (e) {
            if (instance.xhr)
            {
                instance.xhr.abort();
            }
            instance.xhr = null;
            instance.cancelled = true;
            instance.hide();
            e.preventDefault();
        });

        // We want people to cancel forms like they used to when cancel was a button.
        // JRADEV-1823 - Alt-` does not work in IE
        if (AJS.$.browser.msie) {
            $cancel.focus(function(e){
                if (e.altKey){
                    $cancel.click();
                }
            });
        }

        //if there's no buttons (i.e. when there's an error) then add a close link!
        var $popupContent = this.get$popupContent();
        $buttons = AJS.$(".button", $popupContent);
        $buttonContainer = AJS.$("div.buttons", $popupContent);
        if($cancel.length == 0 && $buttons.length == 0) {
            if($buttonContainer.length == 0) {
                $buttonContainer = AJS.$('<div class="buttons-container content-footer"><div class="buttons"/></div>').appendTo($popupContent);
            }
            AJS.populateParameters();
            $closeLink = AJS.$("<a href='#' class='cancel' id='aui-dialog-close'>" + AJS.I18n.getText("admin.common.words.close") + "</a>");
            AJS.$($popupContent).find('.buttons').append($closeLink);

            $closeLink = AJS.$(".cancel", this.get$popupContent());
            $closeLink.click(function(e) {
                instance.hide();
                e.preventDefault();
            });
        }
        $buttonContainer.prepend(AJS.$("<span class='icon throbber'/>"));

        AJS.$(".shortcut-tip-trigger", $popupContent).click(function(e) {
            e.preventDefault();
            if(!$popupContent.isDirty() || confirm(AJS.I18n.getText("common.forms.dirty.dialog.message"))) {
                instance.hide();
                AJS.$("#keyshortscuthelp").click();
            }
        });
    },

    _setContent: function (content,decorate) {
        this._super(content,decorate);

        if (content)
        {
            //Hitting enter on MSIE input forms will not submit when:
            //
            //  {quote: http://www.thefutureoftheweb.com/blog/submit-a-form-in-ie-with-enter}
            //      There is more than one text/password field, but no <input type="submit"/> or <input type="image"/>
            //      is visible when the page loads.
            //  {quote}
            //
            // This seems to be roughly correct. When we initially load the dialog we do it offscreen which means that
            // enter on text input may not work. To get it to work we explicity listen for enter key.

            if (AJS.$.browser.msie) {
                this.$form.bind("keypress", function (e) {
                    var $target = AJS.$(e.target);
                    if ($target.is(":input") && !$target.is("textarea") && e.keyCode === 13) {
                        AJS.$(this).submit();
                    }
                });
            }

            if (JIRA.Dialog.current === this) {
                this._focusFirstField();
            }
        }
    },

    _focusFirstField: function () {

        var triggerConfig = new JIRA.setFocus.FocusConfiguration();

        if (this.$activeTrigger && this.$activeTrigger.attr("data-field")) {
            triggerConfig.focusElementSelector =  "[name='" + this.$activeTrigger.attr("data-field") + "']";
        }
        
        /**
         * The below snippet is to fix a bug in Internet Explorer. The bug is as follows:
         *   
         * 1. Open a FormDialog that has a <select> as the first focused field
         * 2. Tab to a text field with in the same FormDialog
         * 3. Submit dialog
         * 4. VALIDATION ERRORS RETURN FROM SERVER CORRECTLY BUT FIRST FIELD IS NOT FOCUSED.
         *
         * In Internet Explorer programatically focus a <select> after navigating to a text field that no longer
         * exists in the DOM Internet Explorers tab ordering gets all messed up.
         *
         * It seems the only fix is to focus a random field/link.
         */
        triggerConfig.context = this.get$popup()[0];

        if (AJS.$.browser.msie) {
            var $focusHack = AJS.$(".trigger-hack", triggerConfig.context );
            if ($focusHack.length === 0){
                $focusHack = AJS.$("<input Class='trigger-hack' type='text' value=''/>").css({
                    position: "absolute",
                    left: -9000
                }).appendTo(triggerConfig.context);
            }
            $focusHack.focus();
        }

        JIRA.setFocus.pushConfiguration(triggerConfig);
        JIRA.setFocus.triggerFocus();
        JIRA.setFocus.triggerFocus();
    },

    hide: function (undim) {

        if (this._super(undim) === false) {
            return false;
        }

        JIRA.setFocus.popConfiguration();
    }
});

/** Preserve legacy namespace
    @deprecated AJS.FormPopup */
AJS.namespace("AJS.FormPopup", null, JIRA.FormDialog);