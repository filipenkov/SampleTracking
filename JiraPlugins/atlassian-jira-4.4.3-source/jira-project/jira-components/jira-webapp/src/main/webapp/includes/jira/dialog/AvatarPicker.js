/**
 * @constructor JIRA.AvatarPicker
 * @author Scott Harwood
 */
JIRA.AvatarPicker = function (options) {

    var prefs, dialog, avatars = {}, selectedAvatar,

    /**
     * Creates a json object of preferences from a <dl> element
      *
     * @method setPrefs
     * @private
     */
    setPrefs = function () {
            prefs = AJS.$.readData(options.prefsSelector);
    },

    /**
     * Checks the response is not an error message. If it is an error message we do not want to replace all of
     * the panel but rather insert the message before the first element.
     *
     * @method checkForNoErrors
     * @private
     * @param {String} response - the html fragment returned from the server
     * @return {Boolean} if the response contained errors or not
     */
    checkForNoErrors = function (response) {
        if (response.indexOf("class=\"module message error\"") !== -1) {
            AJS.$("div.error", dialog.getCurPanel().body).remove();
            dialog.getCurPanel().body.prepend(response);
            dialog.updateHeight();
            return false;
        }
        return true;
    },

    checkNeedsCropping = function (response) {
        if (response.indexOf("avatar-panel") !== -1) {
            delete dialog.getCurPanel().restore;
            AJS.$("#avatar-panel").remove();
            AJS.$("body").append(response);
            resetPanels();
            selectedAvatar.select();
            return false;
        }
        return true;
    },

    /**
     * Appends loading indicator (throbber) to all ajax requests. Indicator, controlled via css, appears on the active
     * panel button
     *
     * @method addThrobber
     * @private
     */
    addThrobber = function () {
        AJS.$().ajaxSend(function(a,xhr) {
            if (dialog.popup.element.is(":visible")) {
                AJS.$(xhr).throbber({target: dialog.getCurPanelButton().parent()});
            }
        });
    },

    /**
     * Handles failed ajax requests. Will show the user a message saying there was an error and functionality to
     * retry the request.
     *
     * @method addErrorHandling
     * @private
     */
    addErrorHandling = function () {
       AJS.$().ajaxError(function() {
            var errorNode = AJS.$("#avatar-panel div.error").remove(), counter = errorNode.find(".retry").find("em"), t;
            return function (event, XMLHttpRequest, ajaxOptions, thrownError) {
                var handler = function(){
                    ajaxOptions.success = function (success) {
                        return function () {
                            cancel();
                            success();
                        };
                    }(ajaxOptions.success);
                    ajaxOptions.retryCount = true;
                    AJS.$.ajax(ajaxOptions);
                    errorNode.find(".retry").css({display: "inline"});
                },
                cancel = function () {
                    if (t) {
                        clearTimeout(t);
                    }
                    errorNode.removeClass("retry").removeClass("contactadmin");
                    counter.text(0);
                    delete ajaxOptions.retryCount;
                    errorNode.find(".retry").css({display: "none"});
                    errorNode.remove();
                    dialog.updateHeight();
                };
                if (dialog.popup.element.is(":visible") && !ajaxOptions.retryCount) {
                    dialog.getCurPanel().body.find(".message").remove();
                    AJS.$(errorNode).insertBefore(AJS.$(dialog.getCurPanel().body.get(0).firstChild));
                    errorNode.addClass("retry");
                    errorNode.find("button.tryagain").unbind("click", handler);
                    errorNode.find("button.tryagain").click(handler);
                    errorNode.find("button.cancel").unbind("click", cancel);
                    errorNode.find("button.cancel").click(cancel);
                    dialog.updateHeight();
                } else if (parseInt(counter.text()) === 3) {
                    errorNode.addClass("contactadmin");
                    t = window.setTimeout(cancel, 3000);
                } else {
                    counter.text(1+parseInt(counter.text()));
                }
            };
        }());
    },

    /**
     * Creates dialog box. Adds global functionality including error handling and ajax throbber indicator.
     *
     * @method createDialog
     * @private
     * @param {String, Number} width - width of dialog box
     * @param {String, Number} height - height of dialog box
     */
    createDialog = function (width, height) {
        dialog = new AJS.Dialog(width, height, "avatar-dialog");
        dialog.addHeader(prefs.title + "<button class='close'>Close</button>");
        AJS.$("#avatar-dialog button.close").click(function(e){
            dialog.hide();
            e.preventDefault();
        });
        dialog.addButtonPanel();
        dialog.uploadButton = AJS.$('<input class="upfile" name="imageFile" type="file" />')
                .appendTo(dialog.page[0].buttonpanel);
        addErrorHandling();
        addThrobber();
    },

    /**
     * Creates avatar objects and adds them to their associates group/category
     *
     * @method setAvatars
     * @private
     * @param {String, Number} group - panel id
     * @param {Object} collection - jQuery object containing all avatar HTML elements for that panel (<li>)
     */
    setAvatars = function (group, collection) {
        avatars[group] = [];
        collection.each(function(){
            var avater = avatar({
                element: AJS.$(this),
                group: group,
                id: avatars[group].length
            }).init();
            avatars[group].push(avater);
        });
    },

    /**
     * Handles sumission of cropped image
     *
     * @method applyImageCropper
     * @private
     * @param {Object} panel - panel body element as jquery object
     */
    applyImageCropper = function (panel) {
        panel.find("form").submit(function(e){
            e.preventDefault();
            AJS.$.ajax({
                url: this.action,
                type: "POST",
                data: AJS.$(this).serialize(),
                success: function (response) {
                    delete dialog.getCurPanel().restore;
                    AJS.$("#avatar-panel").remove();
                    jQuery("body").append(response);
                    resetPanels();
                    selectedAvatar.select();
                }
            });
        });
    },

    updateRestResource = function (id, callback) {

        if (prefs.updateUrl) {

            JIRA.SmartAjax.makeRequest({
                url: prefs.updateUrl + "/" + id,
                type: "POST",
                dataType: "json",
                complete: function (xhr, textStatus, smartAjaxResult) {

                    callback(smartAjaxResult);

                    if (!smartAjaxResult.successful) {
                        new JIRA.FormDialog({
                            content: function () {

                                var $content = AJS.$("<div class='aui-popup-content'/>"),
                                    $contentBody = AJS.$("<div class='content-body' />").appendTo($content),
                                    $footer = AJS.$("<div class='buttons-container content-footer' />").html(
                                            "<div class=\"buttons\"><a href=\"#\" class=\"cancel\" id=\"aui-dialog-close\">"
                                                    + AJS.I18n.getText("common.words.close")
                                            + "</a></div>"
                                    );

                                $contentBody.append(JIRA.SmartAjax.buildDialogErrorContent(smartAjaxResult));
                                $content.append($footer);

                                return $content;
                            }
                        }).show();
                    }
                }
            });
        }
    },

    getSelectedId = function () {
        return selectedAvatar ? selectedAvatar.element.attr("data-avatarId") : "";
    };

    /**
     * Handles uploading of image, through remote iframing. After which applies cropping control. Also restores
     * previous state if cancel button is clicked.
     *
     * @method uploadAvatar
     * @private
     * @param {Object} element - trigger that when clicked launches file dialog box
     */
    applyImageUploader = function (element) {
        element.upload({
            method: 'post',
            enctype: 'multipart/form-data',
            action: prefs.uploadAction,
            onSubmit: function () {
                dialog.getCurPanelButton().parent().addClass("loading");
            },
            onComplete: function(data) {
                var img;
                dialog.getCurPanelButton().parent().removeClass("loading");
                if (!checkForNoErrors(data)) {
                    return;
                }
                if (!checkNeedsCropping(data)) {
                    return;
                }
                dialog.gotoPanel(2);
                dialog.getCurPanel().body.wrapInner("<div class='prev-content'>");
                var oldContent = AJS.$(".prev-content").remove();
                dialog.getCurPanel().html(data);

                if (AJS.$.browser.msie) {
                    img = dialog.getCurPanel().body.find("img");
                    img.attr("src", img.attr("src") + "?" + new Date().getTime());
                }

                AJS.$(".avataror").avataror({previewElement: dialog.getCurPanel().body.find(".message"),
                    parent: AJS.$("#avatar-dialog") });

                img = dialog.getCurPanel().body.find("img");

                img.load(function(){
                    dialog.updateHeight();
                    applyImageCropper(dialog.getCurPanel().body);
                });
                dialog.getCurPanel().restore = function () {
                    dialog.getCurPanel().body.empty();
                    dialog.getCurPanel().html(oldContent);
                    setAvatars(dialog.getCurPanel(), dialog.getCurPanel().body.find(".avatar"));
                };
                dialog.getCurPanel().body.find("input:button").click(function(){
                    dialog.getCurPanel().restore();
                    dialog.updateHeight();
                });


            },
            params: {
                ownerId: prefs.ownerId,
                avatarType: prefs.avatarType,
                pickerField: prefs.pickerFieldID
            }
        });

    },

    /**
     * Loops through hidden html nodes and uses them to create the panels of dialog
     *
     * @method resetPanels
     * @private
     */
    resetPanels = function () {
        AJS.$("#avatar-panel .panel").each(function(i) {
            var $this = AJS.$(this),
            group = $this.find("h2").text(),
            avatarContainer = $this.find("ul.avatars");
            if (/avatar-all/.test($this.attr("id"))) {
                AJS.$("#avatar-panel .panel" + " " + "ul.avatars").clone().appendTo($this);
                avatarContainer = $this.find("ul.avatars");
            }

            if (AJS.$(".avatar", avatarContainer).length === 0) {
                avatarContainer = AJS.$("<p>" + prefs.noCustom + "</p>");
            }

            if (!dialog.getPanel(i)) {
                dialog.addPanel(group, avatarContainer, $this.attr("id"));
            } else {
                dialog.getPanel(i).body.html(avatarContainer);
            }
            setAvatars(i, dialog.getPanel(i).body.find(".avatar"));
        });
        for (var i=0; dialog.getPanel(i); i++) {
            var panel = dialog.getPanel(i);
            panel.body.hide = function (panel, hide) {
                return function() {
                    panel.body.find(".message").remove();
                    if (panel.restore) {
                        panel.restore();
                        dialog.updateHeight();
                        delete panel.restore;
                    }
                    hide.call(panel.body);
                };
            }(panel, panel.body.hide);
        }

        var aEl = AJS.$("#selected-avatar", dialog.popup.element.body);
        if (aEl.length != 0) {
            selectedAvatar = avatar({element: aEl, group: 2});
        }
    },

    /**
     * If there is a default avatar, select id
     * @method selectDefaultAvatar
     */
    selectDefaultAvatar = function () {
        if (prefs.defaultAvatarId && prefs.defaultAvatarId !== "") {
            dialog.popup.element.find(".avatar[data-avatarId=" + prefs.defaultAvatarId +"]:first").trigger("select", [false]);
        }
    },


    /**
     * Launches Dialog box
     *
     * @method launchDialog
     * @param url
     * @param width
     * @param height
     */
    launchDialog = function (url, width, height) {
        jQuery("#avatar-prefs").remove();
        AJS.$(AJS.$.ajax({
            url: url,
            success: function (response) {
                jQuery("#avatar-panel").remove();
                    AJS.$("body").append(response);
                    if (!document.getElementById("avatar-panel")) {
                        window.location.reload();
                        return;
                    }
                    setPrefs();
                    createDialog(width, height);
                    resetPanels();

                    if (prefs.selectedId) {
                        selectedAvatar = avatar({
                            id: prefs.selectedId,
                            element: dialog.popup.element.find(".avatar[data-avatarId=" + prefs.selectedId + "]:first")
                        })
                    }

                    applyImageUploader(dialog.uploadButton);
                dialog.show();
                dialog.updateHeight();
                dialog.gotoPanel(0);
            },
            error: function (){
                window.location.reload();
            }
        })).throbber({target: AJS.$(options.triggerSelector)});
    },

    avatar = function (opts) {

        var getId = function () {
            return opts.element.find("img").attr("id");
        };

        return {

            element: opts.element,
            id: opts.id,
            group: opts.group,


            /**
             * Selects clicked avatar
             *
             * @method select
             * @param {Boolean} closeDialog
             */
            select: function (closeDialog) {
                if (getId()) {
                    AJS.$("#" + prefs.pickerFieldID).attr("value", getId());
                } else {
                    AJS.$("#" + prefs.pickerFieldID).attr("value", "");
                }
                var that = this;
                //optionally save the selected avatar by posting to the server
                if(options.saveOnClose) {
                    updateRestResource(getId(), function (smartAjaxResponse) {
                        if (smartAjaxResponse.successful) {
                            AJS.$(".avatar-image").attr("src", that.element.find("img").attr("src"));
                        }

                        if (closeDialog !== false) {
                            dialog.hide();
                        }
                    });
                } else {
                    AJS.$(".avatar-image").attr("src", that.element.find("img").attr("src"));

                    if (closeDialog !== false) {
                        dialog.hide();
                    }
                }
            },

            /**
             *
             * Is this avatar the selected avatar
             *
             * @method isSelected
             */
            isSelected: function () {
                return getSelectedId() === this.element.attr("data-avatarId");
            },

            /**
             * Removes clicked avatar
             *
             * @method remove
             */
            remove: function (removeURL) {
                var that = this;
                if (!this.remove.lock) {
                    this.remove.lock = true;
                    AJS.$.ajax({
                       url: removeURL,
                       success: function (response) {
                           AJS.$(response).insertBefore(dialog.getCurPanel().body.get(0).firstChild);
                           dialog.getCurPanel().body.delegate(".cancel","click", function(e){
                                jQuery(this).parents(".message").remove();
                                dialog.updateHeight();
                                e.preventDefault();
                            });
                           dialog.getCurPanel().body.find("input:submit").focus();
                           dialog.getCurPanel().body.find("form").submit(function(e){
                               AJS.$.ajax({
                                   url: this.action,
                                   data: AJS.$(this).serialize(),
                                   type: "POST",
                                   success: function (response) {

                                       if (that.isSelected()) {
                                            selectDefaultAvatar();
                                       }

                                       avatars[that.group].splice(that.id-1,1);
                                       if (AJS.$("#" + prefs.pickerFieldID).val() === getId()) {
                                            AJS.$("#" + prefs.pickerFieldID).val("");
                                            AJS.$(".avatar-image").attr("src", "");
                                       }

                                       that.element.fadeOut(function(){
                                           AJS.$("#avatar-panel").remove();
                                           jQuery("body").append(response);
                                           resetPanels();
                                           dialog.updateHeight();
                                       });

                                       dialog.getCurPanel().body.find(".message").remove();
                                   }

                                });
                               e.preventDefault();
                           });
                           that.remove.lock = false;
                           dialog.updateHeight();
                       }
                    });
                }

            },

            /**
             * Applies event handlers
             *
             * @method init
             * @private
             */
            init: function () {
                var that = this;
                this.element.find(".del").hover(function(){
                    that.element.addClass("del");
                }, function (){
                    that.element.removeClass("del");
                }).click(function(e){
                    that.remove(this.href);
                    e.preventDefault();
                    e.stopPropagation();
                });
                this.element.click(function(e){
                    that.select();
                    e.preventDefault();
                });
                this.element.hover(function(){
                    that.element.addClass("active");
                }, function () {
                    that.element.removeClass("active");
                    that.element.removeClass("del");
                });

                this.element.bind("select", function (e, hideDialog) {
                    that.select(hideDialog);
                });

                return this;
            }
        };
    };
    AJS.$(options.triggerSelector).click(function(e){
        launchDialog(this.href, 800, 300);
        e.preventDefault();
    });
};

jQuery(function(){
    JIRA.AvatarPicker({
        triggerSelector:  "#user_avatar_link",
        prefsSelector: "#avatar-prefs",
        saveOnClose:true
    });
});

/** Preserve legacy namespace
    @deprecated jira.app.AvatarPicker */
AJS.namespace("jira.app.AvatarPicker", null, JIRA.AvatarPicker);
