(function ($) {
    var shareDialog, $contents;

    function hideDialog(reset) {
        if (reset) {
            // We have to bind to the event triggered by the inline-dialog hide code, as the actual hide runs in a
            // setTimeout callback. This caused JRADEV-7962 when trying to empty the contents synchronously.
            $(document).one("hideLayer", function (e, type, dialog) {
                if (type == "inlineDialog" && dialog.popup == shareDialog) {
                    $(document).unbind('.share-dialog');
                    $contents.empty();
                }
            });
        }
        shareDialog.hide();
        return false;
    }

    function getUsernameValue() {
        return AJS.$(this).attr("data-username");
    }

    function getEmailValue() {
        return AJS.$(this).attr("data-email");
    }

    function submit() {
        // 0. Get the entered Users and Email addresses and abort if none found. Note that we can't just use
        // #sharenames.val() because we need to split out the different types. It might be nice if MultiSelect provided
        // a method for this...
        var $recipients = $contents.find('.recipients');
        var users = $recipients.find('li[data-username]').map(getUsernameValue).toArray();
        var emails = $recipients.find('li[data-email]').map(getEmailValue).toArray();
        if (!(users.length || emails.length)) {
            return false;
        }

        $("button,input,textarea", this).attr("disabled", "disabled");


        var icon = $contents.find(".button-panel .icon");
        icon.css("left", "10px").css("position", "absolute");
        var killSpinner = Raphael.spinner(icon[0], 7, "#666");

        var messages = $contents.find(".progress-messages");
        messages.text(AJS.I18n.getText("jira-share-plugin.dialog.progress.sending"));
        messages.css("padding-left", icon.innerWidth() + 5);

        var message = $contents.find("#note").val();
        var request = {
            usernames: users,
            emails: emails,
            message: message
        };
        var url = contextPath + '/rest/share/1.0';

        var issueKey = JIRA.Meta.getIssueKey();
        if (issueKey) {
            // Share Issue
            url += '/issue/' + issueKey;
        } else {
            // Share Search

            var filterId = AJS.Meta.get('filter-id');
            if (filterId) {
                // A saved search
                url += '/filter/' + filterId;
            } else {
                // A JQL search
                request.jql = AJS.Meta.get('filter-jql');
                url += '/search';
            }
        }

        JIRA.SmartAjax.makeRequest({
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            url: url,
            data: JSON.stringify(request),
            success: function() {
                killSpinner();

                icon.addClass('icon-tick');
                messages.text(AJS.I18n.getText("jira-share-plugin.dialog.progress.sent"));
                setTimeout(function() {
                    hideDialog(true);
                }, 1000);
            },
            error: function (data, status) {
                killSpinner();

                icon.addClass('icon-cross');
                messages.text(AJS.I18n.getText("jira-share-plugin.dialog.progress.error"));
            }
        });

        return false;
    }

    function enableSubmit(enabled) {
        $contents.find(".submit").prop("disabled", !enabled);
    }

    /**
     * Invoke a bunch of magical JS event delegation to make sure that we only trigger the execution of the functions
     * attached to the access keys defined within this dialog's form.
     *
     * @param shareDialogForm the share dialog's form.
     */
    function enableAccessKeys(shareDialogForm){
        AJS.$(shareDialogForm).handleAccessKeys({
            selective: false // only trigger the access keys defined in this form.
        });
    }

    function generatePopup(contents, trigger, doShowPopup) {
        $contents = contents;
        if ($contents.children().length) {
            // Dialog already opened once and not reset - just reuse it
            doShowPopup();
            return;
        }

        $contents.append(JIRA.Templates.Dialogs.Share.contentPopup({
            modifierKey: AJS.Navigator.modifierKey()
        }));
        if (AJS.$.browser.msie) {
            $contents.find("form").ieImitationPlaceholder();
        }
        enableSubmit(false);
        $contents.find('#sharenames').bind('change unselect', function () {
            var val = $(this).val();
            enableSubmit(val && val.length);
        });

        $contents.find(".close-dialog").click(function() {
            hideDialog(true);
        });

        $contents.find("form").submit(function() {
            submit();
            return false;
        });

        $(document).bind('keyup.share-dialog', function (e) {
            // Close on Escape key
            if (e.keyCode == 27) {
                return hideDialog(false);   // leave the dialog contents alone
            }
            return true;
        });

        $(document).bind("showLayer.share-dialog", function (e, type, dialog) {
            if (type == "inlineDialog" && dialog.popup == shareDialog) {
                $contents.find("#sharenames-textarea").focus();
            }
        });

        enableAccessKeys(AJS.$("form", $contents));

        doShowPopup();

        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$contents]);
    }

    AJS.toInit(function ($) {
        var dialogOptions = {
            hideCallback: function () {
                $(".dashboard-actions .explanation").hide();
            },
            width: 273,
            offsetY: 17,
            offsetX: -100,
            hideDelay: 36e5,         // needed for debugging! Sit for an hour.
            useLiveEvents: true
        };

        shareDialog = AJS.InlineDialog($('#jira-share-trigger'), "share-entity-popup", generatePopup, dialogOptions);

        // JRADEV-8136 - Clicking the share button again doesn't close the share dialog.
        $('#jira-share-trigger').live("click", function() {
            if (shareDialog.find(".contents:visible").length) {
                shareDialog.find("a.close-dialog").click();
            }
        });

        //this is a hack, but it's necessary to stop click on the multi-select autocomplete from closing the
        //inline dialog. See JRADEV-8136
        AJS.$(document).bind("showLayer", function(e, type, hash) {
            if(type && type === "inlineDialog" && hash && hash.id && hash.id === "share-entity-popup") {
                $("body").unbind("click.share-entity-popup.inline-dialog-check");
            }
        });

        AJS.$(document).bind("keydown", function (e) {
            // special case for when user hover is open at same time
            if (e.keyCode === 27 && AJS.InlineDialog.current != shareDialog && shareDialog.is(":visible")) {
                if (AJS.InlineDialog.current) {
                    AJS.InlineDialog.current.hide();
                }
                shareDialog.hide();
            }
        });

        // JRA-27476 - share dialog doesn't stalk. So hide it, without reset, when the page is scrolled
        AJS.$(window).scroll(function () {
            hideDialog(false);
        });

    });
})(AJS.$);

