/**
 * @namespace JIRA.ViewIssue
 * A module to encapsulate all view issue functionality
 */
JIRA.ViewIssue = (function () {

    var modules = {
        domReady: function () {
            headerDropdowns();
        }
    };

    function headerDropdowns() {
        AJS.Dropdown.create({
            trigger: AJS.$(".mod-header .aui-dropdown-trigger")
        });
    }

    function setFocusConfiguration() {
        // if the url has an anchor the same as the quick subtask create form, we will focus first field.
        if (parseUri(window.location.href).anchor !== "summary") {
            var triggerConfig = new JIRA.setFocus.FocusConfiguration();
            triggerConfig.excludeParentSelector = "#" + FORM_ID + ",.dont-default-focus";
            JIRA.setFocus.pushConfiguration(triggerConfig);
        } else {
            AJS.$("#summary").focus();
        }
    }

    var FORM_ID = "stqcform";

    var subtasks = {
        domReady: function () {
            // If we have not just created a subtask do not focus first field of form
            setFocusConfiguration();
        }
    };

    var STALKER_SELECTOR = "#stalker.stalker";

    var stalker = {
        init: function () {

            // offsets perm links, and any anchor's, scroll position so they are offset under ops bar
            new JIRA.OffsetAnchors(STALKER_SELECTOR + ", .stalker-placeholder");

            AJS.$(STALKER_SELECTOR).stalker();
        }
    };

    return {

        /**
         * Called whilst page is loading
         *
         * @method init
         */
        init: function () {
            stalker.init();
        },

        /**
         * Called when DOM is ready. Same as AJS.$(function() {...});
         *
         * @method domReady
         */
        domReady: function () {
            modules.domReady();
            subtasks.domReady();
        }
    };
})();

JIRA.ViewIssue.init();
AJS.$(JIRA.ViewIssue.domReady);

/** Preserve legacy namespace
    @deprecated jira.app.viewissue */
AJS.namespace("jira.app.viewissue", null, JIRA.ViewIssue);

/** todo: BELOW code seriously needs to refactored. Badly! If adding anything to this file, use module structure above. */

jQuery(function (){
    /**
     * The selector for the stalker comment textarea.
     */
    var STALKER_COMMENT_SELECTOR = 'textarea#comment';

    /**
     * The selector for the footer comment textarea.
     */
    var FOOTER_COMMENT_SELECTOR = '#addcomment textarea';

    var openInNewWindow = function(e)
    {
        e.preventDefault();
        e.stopPropagation();
        var $this = jQuery(this);

        // close the link
        jQuery(document).click();

        new JIRA.ScreenshotDialog({
            trigger: $this
        }).openWindow();
    };

    /**
     * Cancels a comment. This means clearing the text area, resetting the
     * dirty state for the closes form, and collapsing the comment box.
     *
     * If comment preview mode is enabled, this function disables it before
     * attempting to clear the comment textarea.
     *
     * @param $commentButton the comment button
     * @param textAreaSelector the JQuery selector used to find the text area.
     *      we can only look for the text area after disabling preview mode.
     */
    var cancelCommentInput = function($commentButton, textAreaSelector) {
        // first hide the comment box
        $commentButton.click();

        // now clear the input value.  Need to do this in a timeout since FF 3.0 otherwise doesn't
        //clear things.
        setTimeout(function() {AJS.$(textAreaSelector).val('')}, 100);

        // JRADEV-3411: disable preview if necessary so the comment gets cleared properly
        AJS.$('#comment-preview_link.selected').click();
    };
    
    jQuery(document).bind(JIRA.Keyboard.SpecialKey.eventType(), function (e) {
        if (JIRA.Keyboard.specialKeyEntered(e) === JIRA.Keyboard.SpecialKey.ESC && !AJS.InlineLayer.current) {
            if(jQuery("#comment-issue.active").length > 0) {
                //this means we're closing the stalker bar comment.  Reset it's dirty
                //form state since this is effectively like closing a dialog!
                jQuery("#comment-issue.active").click();
            }
        }
    });


    function setCaretAtEndOfCommentField() {
        var $field = AJS.$("#comment"),
            field = $field[0],
            length;

        if ($field.length) {

            length = $field.val().length;

            $field.scrollTop($field.attr("scrollHeight"));

            if (field.setSelectionRange && length > 0) {
                field.setSelectionRange(length, length);
            }
        }
    }

    AJS.$("#comment-issue").click(function (e) {

        var elem = jQuery(this);
        if (elem.hasClass("active")) {
            elem.removeClass("active");
            jQuery("#stalker").removeClass("action");
            jQuery("form#issue-comment-add").appendTo("#addcomment .mod-content");

        } else {
            if (AJS.$("#addcomment").hasClass("active")) {
                AJS.$("#footer-comment-button").click();
            }
            elem.addClass("active");
            jQuery("#stalker").addClass("action");
            jQuery("form#issue-comment-add").appendTo(".ops-cont");

            AJS.$("#comment").focus().trigger("keyup");
            setCaretAtEndOfCommentField();
        }

        jQuery("#stalker").trigger("stalkerHeightUpdated");

        e.preventDefault();
    });
    

    
    AJS.$("#footer-comment-button").click(function (e) {

        var elem = jQuery("#addcomment");

        if (elem.hasClass("active")) {
            elem.removeClass("active");
        } else {
            if (AJS.$("#comment-issue").hasClass("active")) {
                AJS.$("#comment-issue").click();
            }
            elem.addClass("active");
            jQuery("form#issue-comment-add").appendTo("#addcomment .mod-content");
            AJS.$("#comment").trigger("keyup").focus();
            setCaretAtEndOfCommentField();
        }

        e.preventDefault();
    });

    jQuery(document).bind("showWikiInput", function (e, previewElem) {
        var $commentField = jQuery("#comment:visible:enabled");
        jQuery("#stalker").trigger("stalkerHeightUpdated");
        if ($commentField.length > 0) {
            $commentField.focus();
        }
        return arguments.callee;
    }());

    jQuery(document).bind("showWikiInput", function () {
        setCaretAtEndOfCommentField();
    });

    jQuery(document).bind("showWikiPreview", function () {
        jQuery("#stalker").trigger("stalkerHeightUpdated");
    });

    /*
     * Cancels the comment input.
     */
    jQuery("#issue-comment-add-cancel").click(function (e) {
        var $stalkerCommentButton = AJS.$("#comment-issue"),
            $footerCommentModule =  AJS.$("#addcomment");

        if ($stalkerCommentButton.hasClass("active")) {
            cancelCommentInput($stalkerCommentButton, STALKER_COMMENT_SELECTOR)
        } else if ($footerCommentModule.hasClass("active")) {
            var $footerCommentButton = AJS.$("#footer-comment-button");
            cancelCommentInput($footerCommentButton, FOOTER_COMMENT_SELECTOR)
        }

        e.preventDefault();
    });

    AJS.$("#commentDiv input[type='submit']").click(function(e){
        if (AJS.$("#comment").val() === ""){
            e.preventDefault();
            AJS.$("#emptyCommentErrMsg").show();
        }
    });
    AJS.$("#attach-screenshot").click(openInNewWindow);
    AJS.$("#tt_include_subtasks input").click(function(e){
        if (AJS.$(this).is(":checked")){
            AJS.$("#tt_info_single").hide();
            AJS.$("#tt_info_aggregate").show();
        } else {
            AJS.$("#tt_info_aggregate").hide();
            AJS.$("#tt_info_single").show();
        }

    });

    if (jQuery.browser.mozilla && /^1\.9\.1/.test(jQuery.browser.version) && !jQuery.os.mac) {
        var stalker = jQuery("#stalker");
        stalker.addClass("fix-ff35-flicker");
        var setStalkerWidth = function () {
            var contentWidth = jQuery("#main-content").outerWidth();

            if (contentWidth < 1000) {
                stalker.width(contentWidth);
            } else {
                stalker.css("width", "");
            }
        };
        jQuery(window).resize(setStalkerWidth);
        setStalkerWidth();
        stalker.trigger("stalkerHeightUpdated");
    }

    var toggleVotingAndWatching = function(trigger, className, resultContainer, issueOpTrigger, i18n) {
        var classNameOn = className + "-on",
            classNameOff = className + "-off",
            icon = trigger.find('.icon'),
            restPath = "/voters",
            data,
            method = "POST";

        if(icon.hasClass(classNameOn)) {
            method = "DELETE";
        }

        if(className.indexOf("watch") !== -1) {
            restPath = "/watchers";
        }
        icon.removeClass(classNameOn).removeClass(classNameOff);

        if (method === "POST") {
            // If we are a post we want to include dummy data to prevent JRA-20675 BUT we cannot have data for DELETE
            // otherwise we introduce JRA-23257
            data = {
                dummy: true
            }
        }

        AJS.$(JIRA.SmartAjax.makeRequest({
            url:contextPath + "/rest/api/1.0/issues/" + trigger.attr("rel") + restPath,
            type: method,
            dataType: "json",
            data: data,
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    if(method === "POST") {
                        icon.addClass(classNameOn);
                        trigger.attr("title", i18n.titleOn).find('.action-text').text(i18n.actionTextOn);
                        issueOpTrigger.attr("title", i18n.titleOn).text(i18n.textOn);
                    } else {
                        icon.addClass(classNameOff);
                        trigger.attr("title", i18n.titleOff).find('.action-text').text(i18n.actionTextOff);
                        issueOpTrigger.attr("title", i18n.titleOff).text(i18n.textOff);
                    }

                    resultContainer.text(smartAjaxResult.data.count);
                } else {
                    /* [alert] */
                    alert(JIRA.SmartAjax.buildSimpleErrorContent(smartAjaxResult,{ alert : true }));
                    /* [alert] end */
                    if(method === "POST") {
                        icon.addClass(classNameOff);
                        trigger.attr("title", i18n.titleOff).find('.action-text').text(i18n.actionTextOff);
                        issueOpTrigger.attr("title", i18n.titleOff).text(i18n.textOff);
                    } else {
                        icon.addClass(classNameOn);
                        trigger.attr("title", i18n.titleOn).find('.action-text').text(i18n.actionTextOn);
                        issueOpTrigger.attr("title", i18n.titleOn).text(i18n.textOn);
                    }
                }
            }
        })).throbber({target: icon});
    };

    AJS.$("#toggle-vote-issue").click(function(e) {
        e.preventDefault();
        AJS.$("#vote-toggle").click();
    });

    AJS.$("#toggle-watch-issue").click(function(e) {
        e.preventDefault();
        AJS.$("#watching-toggle").click();
    });

    var addI18nErrorCodes = function(i18n) {
        AJS.$("input[type=hidden][id|=error]").each(function(index, elem) {
            var i18n_id = elem.id.replace("error-", "");
            i18n[i18n_id] = elem.value;
        });
    };

    AJS.$("#vote-toggle").click(function(e) {
        e.preventDefault();
        var i18n = {titleOn:AJS.I18n.getText("issue.operations.simple.voting.alreadyvoted"), titleOff:AJS.I18n.getText("issue.operations.simple.voting.notvoted"),
            textOn:AJS.I18n.getText("issue.operations.simple.unvote"), textOff:AJS.I18n.getText("issue.operations.simple.vote"),
            actionTextOff:AJS.I18n.getText("common.concepts.vote"), actionTextOn:AJS.I18n.getText("common.concepts.voted")};
        addI18nErrorCodes(i18n);
        toggleVotingAndWatching(AJS.$(this), "icon-vote", AJS.$("#vote-data"), AJS.$("#toggle-vote-issue"), i18n);
    });

    AJS.$("#watching-toggle").click(function(e) {
        e.preventDefault();
        var i18n = { titleOn:AJS.I18n.getText("issue.operations.simple.stopwatching"), titleOff:AJS.I18n.getText("issue.operations.simple.startwatching"),
            textOn:AJS.I18n.getText("issue.operations.unwatch"), textOff:AJS.I18n.getText("issue.operations.watch"),
            actionTextOff:AJS.I18n.getText("common.concepts.watch"), actionTextOn:AJS.I18n.getText("common.concepts.watching") };
        addI18nErrorCodes(i18n);
        toggleVotingAndWatching(AJS.$(this), "icon-watch",AJS.$("#watcher-data"), AJS.$("#toggle-watch-issue"), i18n);
    });

    //FF3.0 has issues with shortening so we disable it here! (JRADEV-3007)
    if(AJS.$.browser.mozilla && AJS.$.browser.version.indexOf("1.9.0") === 0) {
        AJS.$("#peoplemodule .shorten").removeClass("shorten");
    }

    AJS.$(".shorten").shorten();


    AJS.moveInProgress = false;
    AJS.$(document).bind("moveToStarted", function() {
        AJS.moveInProgress = true;
    }).bind("moveToFinished", function() {
        AJS.moveInProgress = false;
    });

    AJS.$(".issue-data-block").mouseover(function() {
        if(!AJS.moveInProgress) {
            AJS.$(".issue-data-block.focused").removeClass("focused");
            AJS.$(this).addClass("focused");
        }
    });
    AJS.$(".issuePanelContainer").mouseout(function() {
        if(!AJS.moveInProgress) {
            AJS.$(".issue-data-block.focused").removeClass("focused");
        }
    });

    if (jQuery.browser.msie && parseInt(jQuery.browser.version, 10) === 7) {
        jQuery("a.twixi").bind("focus", function (e) {
            e.preventDefault();
        });
    } else {
        jQuery(document).bind("moveToFinished", function (event, target) {
            jQuery("a.twixi:visible", target).focus();
        });
    }

});



// adds fancy box support to viewissue
jQuery(function()
{
    if (jQuery.browser.msie && jQuery.browser.version < 7) {
        return;
    }

    var initFancyBoxForClass = function(aClassName)
    {
        var closeFancyBox = function()
        {
            jQuery(aClassName).fancybox.close();
        };

        var isFireFoxLinux = function()
        {
            return jQuery.os.linux && jQuery.browser.mozilla;
        };

        var useOverlay = true;
        // FF on Linux looks like a car accident when the overlay is applied.  Its all over the place
        // like a mad womans breakfast.  So lets opt out for that combination.  FF in Windows/M<ac is not affected
        if (isFireFoxLinux())
        {
            useOverlay = false;
        }

        var fancyBoxOptions = {
            'imageScale' : true,
            'centerOnScroll' : false,
            'overlayShow': useOverlay,
            //looks like this isn't used a all??
            callbackOnStart : function ()
            {
                jQuery("#header").css("zIndex", "-1");
                if (useOverlay) {
                    jQuery("body").addClass("fancybox-show");
                }
            },
            //looks like this isn't used a all??
            'callbackOnShow' : function()
            {
                jQuery(document).click(function()
                {
                    closeFancyBox();
                });
            },
            'onComplete' : function()
            {
                // fix up title lozenge placement for narrow images (JRADEV-1797)
                var title = AJS.$('#fancybox-title');

                var mainWidth = AJS.$('#fancybox-title-main').outerWidth();
                var leftWidth = AJS.$("#fancybox-title-left").outerWidth();
                var rightWidth = AJS.$("#fancybox-title-right").outerWidth();

                title.width(mainWidth + leftWidth + rightWidth + 5);

                var imageDivWidth = AJS.$('#fancybox-inner').width();
                title.css("marginLeft", -(title.width() / 2) + (imageDivWidth / 2));
                title.css('bottom', title.outerHeight(true) * -1);
            },
            //looks like this isn't used a all??
            'callbackOnClose' : function()
            {
                jQuery("#header").css("zIndex", "");
                if (useOverlay) {
                    jQuery("body").removeClass("fancybox-show");
                }
                jQuery(document).unbind('click', closeFancyBox);
                if (jQuery.browser.safari) {
                    var top = AJS.$(window).scrollTop();
                    AJS.$(window).scrollTop(10 + 5 * (top == 10)).scrollTop(top);
                }
            }
        };
        if(AJS.$.browser.msie) {
            fancyBoxOptions.transitionIn = 'none';
            fancyBoxOptions.transitionOut = 'none';
        }
        jQuery(aClassName).fancybox(fancyBoxOptions);
    };

    // the class mentioned here is declared in view_attachments.jsp
    initFancyBoxForClass("a.gallery");

     AJS.$("a.issueaction-viewworkflow").each(function() {
        AJS.$(this).fancybox({
            type: "image",
            href: this.href,
            title: AJS.escapeHTML(AJS.$(this).attr("rel")),
            titlePosition: "outside",
            imageScale: true,
            centerOnScroll: true,
            overlayShadow: true
        });
    });
});

jQuery(function () {
    // Customfield Tabs
    AJS.$("#customfield-tabs li a").click(function(e){
        e.preventDefault();
        var $this = AJS.$(this);
        var rel = $this.attr("rel");
        // Set active tab
        AJS.$("#customfield-tabs li.active").removeClass("active");
        AJS.$("#tabCell" + rel).addClass("active");
        // Display active tab content
        AJS.$("#customfieldmodule ul.property-list:not(hidden)").addClass("hidden");
        AJS.$("#tabCellPane" + rel).removeClass("hidden");
    });
});

jQuery(function () {
    // Toggles
    var toggle = new JIRA.ToggleBlock({
        blockSelector: ".toggle-wrap",
        triggerSelector: ".mod-header h3",
        cookieCollectionName: "block-states",
        originalTargetIgnoreSelector: "a"
    });
});
