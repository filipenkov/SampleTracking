(function ($) {

    var preventEscClosingComments = false;

    var commentForm = {
        /**
         * Cancels a comment. This means clearing the text area, resetting the
         * dirty state for the closes form, and collapsing the comment box.
         *
         * If comment preview mode is enabled, this function disables it before
         * attempting to clear the comment textarea.
         */
        setCaretAtEndOfCommentField: function () {
            var $field = this.getField(),
                    field = $field[0],
                    length;
            if ($field.length) {
                length = $field.val().length;
                $field.scrollTop($field.attr("scrollHeight"));
                if (field.setSelectionRange && length > 0) {
                    field.setSelectionRange(length, length);
                }
            }
        },

        /**
         * When we submit disable all the fields to avoid mods and double submit
         */
        disable: function () {
            this.getForm().find("textarea").attr("readonly", "readonly");
            this.getForm().find(":submit").attr("disabled", "disabled");
        },

        /**
         * Check if value has changed
         *
         * @return {Boolean}
         */
        isDirty: function () {
            var form = this.getForm();
            if (form) {
                var field = this.getField()[0];
                return field.value !== field.defaultValue;
            }
            return false;
        },

        handleBrowseAway: function () {
            if (commentForm.isDirty()) {
                return AJS.I18n.getText("common.forms.dirty.comment");
            }
        },

        setSubmitState: function () {
            if (jQuery.trim(this.getField().val()).length > 0) {
                this.getForm().find(":submit").removeAttr("disabled");
            } else {
                this.getForm().find(":submit").attr("disabled", "disabled");
            }
        },

        /**
         * Enables the comment form
         */
        enable: function () {
            this.getForm().find("textarea").removeAttr("readonly");
            this.getForm().find(":submit").removeAttr("disabled");
        },

        getCommentVisibility: function() {
            var visibility = $("#commentLevel :selected").val();
            if (visibility !== "") {
                var split_v = visibility.split(':');
                return {
                    "type" : split_v[0],
                    "value" : $("#commentLevel :selected").text()
                };
            }
            return visibility;
        },

        /**
         * Submits comments via ajax, used in kickass
         */
        ajaxSubmit: function (callback) {
            var $loading = jQuery('<span class="icon throbber loading"></span>');
            var issueId = JIRA.Issue.getIssueId();
            var issueKey = JIRA.Issue.getIssueKey();
            var restURL = contextPath + "/rest/api/2/issue/" + issueKey + "/comment";
            //build rest request
            var newComment = {
                "body":this.getField()[0].value
            }
            var visibility = this.getCommentVisibility();
            if (visibility !== "") {
                newComment["visibility"] = visibility;
            }

            $.ajax({
                url:restURL,
                type:"POST",
                contentType:"application/json",
                data:JSON.stringify(newComment),
                success:function (data)
                {
                    JIRA.trigger(JIRA.Events.UNLOCK_PANEL_REFRESHING, ["addcommentmodule"]);
                    JIRA.trigger(JIRA.Events.REFRESH_ISSUE_PAGE, [issueId, {
                        complete:function () {
                            //highlight comment, set anchor
                            var newCommentId = "comment-" + data.id;
                            window.location.hash = newCommentId
                            //remove the focusing from any other comments
                            var $focusedTabs = $("#issue_actions_container > .issue-data-block.focused");
                            $focusedTabs.removeClass("focused");
                            var $newfocusedTab = $("#" + newCommentId);
                            //assume only one focused comment
                            $newfocusedTab.addClass("focused");
                            $loading.remove();
                            commentDrawer.hide(true);
                            footerComment.hide(true); //hiding both?
                        }
                    }]);
                },
                error: function (xhr) {
                    function buildErrorDialog(errorMessage) {
                        var errorContent =
                                '<h2>' + AJS.I18n.getText("common.words.error") + '</h2>' +
                                '<div class="ajaxerror">' +
                                    '<div class="aui-message error">' +
                                        '<span class="aui-icon icon-warning"/>' +
                                        errorMessage +
                                    '</div>' +
                                '</div>';
                        return $(errorContent);
                    }

                    var response = $.parseJSON(xhr.responseText);
                    var content;
                    if (response && response.errors && response.errors.comment) {
                        content = buildErrorDialog(response.errors.comment);
                    } else {
                        content = JIRA.SmartAjax.buildDialogErrorContent(xhr);
                    }
                    new JIRA.FormDialog({
                        content: content
                    }).show();
                    $loading.remove();
                    commentForm.enable();
                }
            });
            $loading.appendTo(this.getForm().find(":submit").parent());
        },

        /**
         * Gets form from dom or cached one
         *
         * @return {jQuery}
         */
        getForm: function () {
            var $form = $("form#issue-comment-add");
            if ($form.length === 1) {
                // on page load or panels have been refeshed and we have another comment form
                this.$form = $form
            }
            return this.$form;
        },

        /**
         * Gets the comment textarea
         * @return {jQuery}
         */
        getField: function () {
            var $form = this.getForm();
            return this.getForm().find("#comment");
        },

        /**
         * Hides form by removing it from dom
         *
         * @param cancel
         */
        hide: function (cancel) {
            this.getForm().detach();
            if (JIRA.Events.UNLOCK_PANEL_REFRESHING) {
                // disable panel refreshing in kickass
                JIRA.trigger(JIRA.Events.UNLOCK_PANEL_REFRESHING, ["addcommentmodule"]);
            }
            if (cancel) {
                this.cancel();
            }
        },

        /**
         * Focuses form
         */
        show: function () {
            this.focus();
            if (JIRA.Events.LOCK_PANEL_REFRESHING) {
                // disable panel refreshing in kickass
                JIRA.trigger(JIRA.Events.LOCK_PANEL_REFRESHING, ["addcommentmodule"]);
            }
        },

        /**
         * Focuses field and puts cursor at end of text
         */
        focus: function () {
            this.getField().focus().trigger("keyup");
            this.setCaretAtEndOfCommentField();
        },
        /**
         *
         * @param e
         * @return {Boolean} - Did it show message or not
         */
        showNoCommentMsg: function (e) {
            if (this.getField().val() === "") {
                $("#emptyCommentErrMsg").show();
                return true;
            }
        },
        /**
         * Cancels comment, removing the value from the textarea
         */
        cancel: function () {
            var instance = this;
            // now clear the input value.  Need to do this in a timeout since FF 3.0 otherwise doesn't
            //clear things.
            setTimeout(function() {instance.getField().val('')}, 100);
            // JRADEV-3411: disable preview if necessary so the comment gets cleared properly
            $('#comment-preview_link.selected').click();
        }
    };


    var footerComment = {
        /**
         * Gets comment module
         * @return {jQuery}
         */
        getModule: function () {
            return $("#addcomment");
        },
        /**
         * Is the comment area visible
         * @return {*}
         */
        isActive: function () {
            return this.getModule().hasClass("active");
        },
        /**
         * Hides comment area
         *
         * @param cancel - clear textarea
         */
        hide: function (cancel) {
            if (this.isActive()) {
                this.getModule().removeClass("active");
                commentForm.hide(cancel);
            }
        },
        ajaxSubmit: function () {
            commentForm.ajaxSubmit(function () {
                footerComment.hide();
            });
        },
        /**
         * Shows comment area
         */
        show: function () {
            if (!this.isActive()) {
                commentDrawer.hide();
                this.getModule().addClass("active");
                this.appendForm();
                commentForm.show();
            }
        },
        /**
         * Appends form to correct location
         */
        appendForm: function () {
            this.getModule().find(".mod-content").append(commentForm.getForm());
        }
    };


    var commentDrawer = {
        /**
         * Gets the comment button from opsbar
         * @return {jQuery}
         */
        getTrigger: function () {
            return $("#comment-issue");
        },
        /**
         * Appends form to correct locaiton
         */
        appendForm: function () {
            $(".ops-cont").append(commentForm.getForm());
        },
        ajaxSubmit: function () {
            commentForm.ajaxSubmit(function () {
                commentDrawer.hide();
            });
        },
        /**
         * Is the comment form visible
         * @return {*}
         */
        isActive: function () {
            return this.getTrigger().hasClass("active");
        },
        /**
         * Hidesc comment form
         * @param cancel - clean input
         */
        hide: function (cancel) {
            if (this.isActive()) {
                this.getTrigger().removeClass("active");
                commentForm.hide(cancel);
                JIRA.Issue.getStalker().removeClass("action").trigger("stalkerHeightUpdated");
            }
        },
        /**
         * Shows comment form in ops bar
         */
        show: function () {
            if (!this.isActive()) {
                footerComment.hide();
                this.getTrigger().addClass("active");
                this.appendForm();
                commentForm.show();
                JIRA.Issue.getStalker().addClass("action").trigger("stalkerHeightUpdated");
            }
        },
        /**
         * Toggle visibility of comment form in ops bar
         */
        toggle: function () {
            if (this.isActive()) {
                this.hide()
            } else {
                this.show();
            }
        }
    };

    var oldBeforeUnload = window.onbeforeunload;

    $(document)
            .delegate("body:not(.ka) #issue-comment-add", "submit", function (e) {
                window.onbeforeunload = oldBeforeUnload;
            })
        // ajax submit form only when in kickass for stalker
            .delegate(".ka .issue-header #issue-comment-add", "submit", function (e) {
                commentDrawer.ajaxSubmit();
                e.preventDefault();
            })
        // ajax submit form only when in kickass for footer
            .delegate(".ka #addcomment #issue-comment-add", "submit", function (e) {
                footerComment.ajaxSubmit();
                e.preventDefault();
            })
        // show/hide of comment in header
            .delegate(".issue-header #comment-issue", "click", function (e) {
                commentDrawer.toggle();
                e.preventDefault();
            })
        // Cancel comment in header
            .delegate("#issue-comment-add .cancel", "click", function (e) {
                e.preventDefault();
                commentDrawer.hide(true);
            })
        // show/hide of comment in footer
            .delegate("#footer-comment-button", "click", function (e) {
                footerComment.show();
                e.preventDefault();
            })
        // Cancel comment in footer
            .delegate("#addcomment .cancel", "click", function (e) {
                e.preventDefault();
                footerComment.hide(true);
            })
            .delegate("#issue-comment-add", "submit", function () {
                window.setTimeout(function () { // JRADEV-11111 - IE8 requires a timeout
                    commentForm.disable();
                }, 0);
            })
            .delegate("#issue-comment-add #comment", "txtinput", function () {
                commentForm.setSubmitState();
            })
            .delegate("#issue-comment-add input[type='submit']", "click", function (e) {
                if (commentForm.showNoCommentMsg()) {
                    e.preventDefault();
                }
            })
            .bind("showWikiInput", function () {
                var $commentField = commentForm.getField();
                if ($commentField.is(":visible:enabled")) {
                    JIRA.Issue.getStalker().trigger("stalkerHeightUpdated");
                    if ($commentField.length > 0) {
                        $commentField.focus();
                    }
                }
            })
            .bind("showWikiInput", function () {
                commentForm.setCaretAtEndOfCommentField();
            })
            .bind("showWikiPreview", function () {
                JIRA.Issue.getStalker().trigger("stalkerHeightUpdated");
            })
            .bind('keydown', function (e) {
                // Pressing a key can result in multiple keydown events being sent (if it's held, or in safari, immediately).
                // Ensure that we only handle the first keydown by blocking until after the mention dialog is hidden.
                if (e.keyCode === $.ui.keyCode.ESCAPE && AJS.InlineLayer.current) {
                    preventEscClosingComments = true;
                    $(document).one('Mention.afterHide', function (e) {
                        preventEscClosingComments = false;
                    });
                }
                if (e.keyCode === $.ui.keyCode.ESCAPE && !AJS.InlineLayer.current && !preventEscClosingComments) {
                    commentDrawer.hide();
                }
            });

    // Why not just use jQuery I hear you say?? Well it doesn't work for IE!
    // JRADEV-11612
    window.onbeforeunload = function () {
        return oldBeforeUnload.apply(this, arguments) ||
                commentForm.handleBrowseAway.apply(this, arguments);
    };

    $(function () {
        new AJS.SecurityLevelSelect($("#commentLevel"));
    });


})(AJS.$);

/**
 * Check for add-comment anchor and open the bottom comment box
 */
AJS.$(function () {
    if (parseUri(window.location.href).anchor === "add-comment") {
        AJS.$("#footer-comment-button").click();
    }
});
