/**
 * @param {Object} prefs
 * @param {HTMLElement=} ctx (optional)
 */
JIRA.wikiPreview = function (prefs, ctx)
{

    var field, editField, trigger, inPreviewMode = false, origText,

    /**
     * Gets and sets fields as jQuery objects
     *
     * @method setFields
     * @private
     */
    setFields = function ()
    {
        field = AJS.$("#" + prefs.fieldId, ctx),
        editField = AJS.$("#" + prefs.fieldId + "-wiki-edit", ctx),
        trigger = AJS.$("#" + prefs.trigger, ctx);
    },

    /**
     *  Prevents scroll flicker from happending when at the bottom of the page
     *
     * @method  scrollSaver
     * @private
     * @return {Object}
     * @... {Function} show - reveals scrollSaver
     * @... {Function} hide - hides scrollSaver
     */
    scrollSaver = function ()
    {
        var elem;
        return {
            show: function ()
            {
                if (!elem)
                {
                    elem = AJS.$("<div>").html("&nbsp;").css({height: "300px"}).insertBefore(editField);
                }
                elem.css({display: "block"});
            },
            hide: function ()
            {
                elem.css({display: "none"});
            }
        };
    }(),

    /**
     *
     * If preview not present, uses REST to get preview of rendered wiki markup. Otherwise restores original state.
     * @method toggleRenderPreview
     * @private
     *
     */
    toggleRenderPreview = function ()
    {
        if (!inPreviewMode)
        {
            editField.find(".content-inner").css({
                maxHeight: field.css("maxHeight")
            });
            this.showPreview();
        }
        else
        {
            editField.find(".content-inner").css({
                maxHeight: ""
            });
            this.showInput();
        }
    },

    /**
     * This function replaces the input with the renderered content.
     *
     * @method renderPreviewCallback
     * @param {String} data from the AJAX call
     */
    renderData =  function(data)
    {
        editField.originalHeight = editField.height();
        scrollSaver.show();
        editField.addClass("previewClass");
        origText = field.val();
        field.hide();
        trigger.removeClass("loading").addClass("selected");
        editField.find(".content-inner").html(data);
        scrollSaver.hide();
        inPreviewMode = true;
        AJS.$(document).trigger("showWikiPreview", [editField]);
        // IE!!! - I will get to the bottom of this one day but for now work around.
        setTimeout(function() {
            trigger.focus();
        },0);
    },

    handleError = function(previewer){
        return function(XMLHttpRequest, textStatus, errorThrown)
        {
            trigger.removeClass("loading");
            origText = field.val();
            /* [alert] */
            if (textStatus){
                alert(textStatus);
            }
            if (errorThrown){
                alert(errorThrown);
            }
            /* [alert] end */
            previewer.showInput();

        };
    };


    return {

        /**
         * Make a request using the textarea/input value and displays the response (rendered wiki content)
         * @method showPreview
         */
        showPreview: function () {
            var that = this;

            var pid = AJS.$("#pid", ctx).val(),
                issueType = AJS.$("#issuetype", ctx).val();

            // Handle case where project is a frother control
            if (AJS.$.isArray(pid)) {
                pid = pid[0];
            }

            // Handle case where issue type is a frother control
            if (AJS.$.isArray(issueType)) {
                issueType = issueType[0];
            }

            AJS.$("#" + prefs.trigger, ctx).addClass("loading");
            AJS.$.ajax({
                url: contextPath + "/rest/api/1.0/render",
                contentType: "application/json",
                type:'POST',
                data: JSON.stringify({
                    rendererType: prefs.rendererType,
                    unrenderedMarkup: field.val(),
                    issueKey: prefs.issueKey,
                    projectId: pid,
                    issueType: issueType
                }),
                dataType: "html",
                success: renderData,
                error: handleError(that)
            });
        },

        /**
         * This restores the input field to allow the user to enter wiki text.
         * @method showInput
         */
        showInput: function (e) {
            if (editField) {
                scrollSaver.show();
                // clear the height before we reset
                editField.css({height: ""});
                editField.removeClass("previewClass").find(".content-inner").empty();
                field = AJS.$("#" + prefs.fieldId, ctx);
                field.val(origText);
                field.show();
                field.focus();
                trigger.removeClass("selected");
                scrollSaver.hide();
                
                inPreviewMode = false;
                AJS.$(document).trigger("showWikiInput", [editField]);
            }
        },

        /**
         * Applies click handler to trigger and associated behaviour.
         * @method init
         */
        init: function ()
        {
            var that = this, $trigger;

            prefs = AJS.$.readData(prefs);

            $trigger = AJS.$("#" + prefs.trigger, ctx);
            $trigger.click(function(e) {
                if (!$trigger.hasClass("loading")) {
                    setFields();
                    toggleRenderPreview.call(that);
                }
                e.preventDefault();
            });
        }
    };

};

/** Preserve legacy namespace
    @deprecated jira.app.wikiPreview */
AJS.namespace("jira.app.wikiPreview", null, JIRA.wikiPreview);
