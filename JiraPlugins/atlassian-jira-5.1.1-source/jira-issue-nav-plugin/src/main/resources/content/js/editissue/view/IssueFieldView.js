/**
 * Handles the saving, editing and canceling of an Issue Field.
 */
JIRA.Issues.IssueFieldView = JIRA.Issues.BaseView.extend({

    events: {
        "click .cancel" : "onClickCancel",
        "keydown" : "onKeyCancel",
        "click .submit" : "onSubmit",
        "click" : "onEdit",
        "submit form" : "onSubmit",
        "beforeBlurInput *": "_preventBlurByEsc"
    },

    initialize: function (options) {

        this.model.bind("validationError",  this.handleValidationError, this);
        this.model.bind("editingStarted",   this.switchToEdit,          this);
        this.model.bind("focusRequested",   this.focus,                 this);
        this.model.bind("editingCancelled", this.switchToRead,          this);
        this.model.bind("aaveError",        this.handleSaveError,       this);
        this.model.bind("updateRequired",   this.updateModel,           this);
        this.model.bind("aavingStarted",    this.handleSavingStarted,   this);
        this.model.bind("modelDestroyed",   this.destroy,               this);

        this.decorate();

        this.issueEventBus = options.issueEventBus;
        this.issueEventBus.bind("panelRendered", this.handlePanelRendered, this);

        this.model.setFieldType(jQuery.data(this.el, 'fieldtype'));

        this._editDelay = 0;

        var instance = this;

        JIRA.bind(JIRA.Events.INLINE_EDIT_BLURRED, function(e, fieldId) {
            if (fieldId === instance.model.getId()) {
                instance._onPossibleBlur();
            }
        });

        JIRA.bind(JIRA.Events.INLINE_EDIT_REQUESTED, function(e, fieldId) {
            if (fieldId !== instance.model.getId() && instance._editDelay !== 0) {
                clearTimeout(instance._editDelay);
                instance._editDelay = 0;
            }
        });
    },

    /**
     *  Unbinds events for collection
     */
    destroy: function () {
        // all model handlers are unbound as a result of the collection being reset. We just need to handle the event bus
        this.issueEventBus.off("panelRendered", this.handlePanelRendered);
    },

    /**
     * Wraps the display value of a field to offer :hover edit prompts.
     */
    decorate: function () {
        if (this.model.isEditable() && !this.model.getEditing()) {
            this.$el.addClass("editable-field inactive");
            this.$el.append('<span class="overlay-icon icon icon-edit-sml" />');
            this._addToolTip();
        }
    },

    /**
     * Returns field's edit elements, i.e. excluding save options and throbber
     */
    getEditElements: function() {
        return this.$el.find(".inline-edit-fields");
    },

    /**
     * Adds an edit prompting tooltip to the issue field value.
     */
    _addToolTip: function() {
        this.$el.attr("title", AJS.I18n.getText("viewissue.start.inline.edit"));
        // override the tooltip on anchors without
        jQuery("a:not([title])", this.$el).attr("title", AJS.I18n.getText("viewissue.follow.link"));
    },

    _removeToolTip: function() {
        this.$el.removeAttr("title");
    },

    _stealAccessKeys: function() {
        jQuery("[accessKey='" + AJS.I18n.getText("AUI.form.submit.button.accesskey") + "']").attr("accessKey", "_s");
        jQuery("[accessKey='" + AJS.I18n.getText("AUI.form.cancel.link.accesskey") + "']").attr("accessKey", "_x");
    },

    _returnAccessKeys: function() {
        jQuery("[accessKey=_s]").attr("accessKey", AJS.I18n.getText("AUI.form.submit.button.accesskey"));
        jQuery("[accessKey=_x]").attr("accessKey", AJS.I18n.getText("AUI.form.cancel.link.accesskey"));
    },

    _handleEditingStarted: function () {
        var $fieldTools = this.$el.find('.field-tools');

        JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [this.$el, JIRA.CONTENT_ADDED_REASON.inlineEditStarted]);
        JIRA.trigger(JIRA.Events.INLINE_EDIT_STARTED, [this.model.getId(), this.model.getFieldType(), this.getEditElements(), this.$el]);

        this.$el.find('.save-options').attr('tabindex', 1).prepend($fieldTools);
    },

    handleValidationError: function (errorHtml) {
        this.$el.html(JIRA.Templates.ViewIssue.Fields.field({
            issue: {
                id: this.model.id,
                editHtml: errorHtml
            },
            accessKey: JIRA.Issues.IssueFieldUtil.getAccessKeyModifier()
        }));
        this.$el.find(".error").attr("data-field", this.model.id);
        this._handleEditingStarted();
    },

    handlePanelRendered: function (panel, $ctx) {
        var $newEl = jQuery(JIRA.Issues.IssueFieldUtil.getFieldSelector(this.model.id), $ctx);
        if ($newEl.length === 1) {
            this.$el = $newEl;
            this.el = this.$el[0];
            this.decorate();
            this.delegateEvents();
        }
    },

    /**
     * Called in the event of a save error.
     *
     * Ensures the view is in edit mode so the error can be rectified.
     */
    handleSaveError: function () {
        this.$el.find(":input").removeAttr("disabled").trigger("enable");
        this.$el.removeClass("saving saving-" + this.model.id);
    },

    /**
     * The view is put into saving mode and any input is disabled to prevent changes whilst the save request is in flight.
     */
    handleSavingStarted: function () {
        this.$el.find(":input").attr("disabled", "disabled").trigger("disable");
        this.$el.addClass("saving saving-" + this.model.id);
    },

    updateModel: function () {
        this.model.update(this.$el);
    },

    /**
     * Reveals the Issue Field View's element.
     *
     * Reveal the element in the following situations (or a combination of them) by triggering.
     *  - Scrolled off screen.
     *  - Scrolled behind the stalker.
     *  - Toggle open a closed twixie container which contains the field.
     *  - Select an unselected tab of containing the field.
     *  - Toggle open a closed twixie container containing a closed tab which contains the element.
     *
     * This is done by:
     *  - Using scrollIntoView to eagerly reveal the position of the element on the screen.
     *  - Triggering a reveal event on the element which is subscribed to by parent elements to reveal if necessary.
     *
     *  Additional marginTop is given to scrollIntoView to ensure the field itself and it's label element are in view.
     *  i.e. Assignee and Reporter have stacked labels in certain situations.
     */
    reveal: function() {
        var padding = this.$el.height() * 2;
        this.$el.scrollIntoView({
            marginTop: AJS.$("#stalker").height() + padding,
            marginBottom: padding
        });
        this.$el.trigger("reveal");
    },

    focus: function () {
        this.reveal();
        // Ensure the field being focused is not disabled (throws an error in IE8)
        this.$el.find(":input").removeAttr("disabled");

        if (jQuery(".aui-blanket").length === 0) {
            // Focus the first visible input when editing starts,
            // but not if a modal dialog is on screen!
            this.$el.find(":input:visible:first").focus().select();
        }
    },

    /**
     * Transitions the view into the edit state.
     */
    switchToEdit: function () {
        this._stealAccessKeys();
        this._removeToolTip();
        this.$el.data("originalHeight", this.$el.height());
        this.$el.removeClass("inactive saving").addClass("active");
        this.model.switchElToEdit(this.$el);
        // NOTE:
        // This order is important! The INLINE_EDIT_STARTED event *must* be dispatched before the
        // edited field is focused, otherwise no INLINE_EDIT_FOCUSED event will be dispatched.
        this._handleEditingStarted();
        this.focus();
    },

    /**
     * Transitions the view into the view state.
     */
    switchToRead: function () {
        this._addToolTip();
        this.$el.addClass("inactive").removeClass("active");
        this.$el.html(this.model.getViewHtml());
        this.$el.closest("form").unbind("submit", this.onSubmit);
        this._returnAccessKeys();
    },

    /**
     * Cancels editing when an escape key is encountered
     * @param e {Event}
     */
    onKeyCancel: function (e) {
        if(e.keyCode === 27) {
            this.model.cancelEdit(JIRA.Issues.CANCEL_REASON.escPressed);
            e.preventDefault();
        }
    },

    /**
     * Cancels editing when cancel button is clicked.
     * @param e {Event}
     */
    onClickCancel: function (e) {
        this.model.cancelEdit();
        e.preventDefault();
    },

    onSubmit: function (e) {
        var event = new jQuery.Event("before-submit");
        this.$el.find("form").trigger(event);
        if (!event.isDefaultPrevented()) {
            this.$el.find(':focus').blur(); // JRADEV-10807 Make sure no inputs are capturing events while a save is in progress
            // Note: Even though the above blur() will trigger a save on its own,
            // the blur handler checks this.model.getSaving() to ensure we don't
            // fire duplicate save requests.
            this.model.save();
            this._returnAccessKeys();
        }
        e.preventDefault();
        
    },

    onEdit: function(event) {

        var time = new Date().getTime();

//        JIRA.one(JIRA.Events.INLINE_EDIT_STARTED, function () {
//            alert("Start Editing: " + (new Date().getTime() - time));
//        });

        if (this._editDelay !== 0) {

            // A click event was received while a pending inline edit request was
            // in progress. Cancel this request, since the user is double-clicking
            // or triple-clicking instead.

            clearTimeout(this._editDelay);
            this._editDelay = 0;

        } else if (this.$el.hasClass("inactive") && jQuery(event.target).closest("a, .uneditable").length === 0 && this._getCurrentlySelectedText() === "") {

            JIRA.trigger(JIRA.Events.INLINE_EDIT_REQUESTED, [this.model.getId()]);

            var self = this;

            if (jQuery(event.target).is(".overlay-icon.icon-edit-sml")) {

                // If this click event occurred directly on the pencil icon, enter
                // inline edit mode straight away.
                //
                // We handle this event on the document to allow event listeners
                // registered on ancestors to have the opportunity to preventDefault()
                // and thus cancel inline edit.

                jQuery(document).one("click", function(event) {
                    if (!event.isDefaultPrevented()) {
                        self.model.edit();
                    }
                });

            } else {

                // If this click occurs somewhere else on the field besides the pencil
                // icon, wait briefly to allow the user the opportunity to double-click.
                // Double-clicks will abort the pending transition to inline edit mode.

                this._editDelay = setTimeout(function() {
                    if (!event.isDefaultPrevented() && self.$el.hasClass("inactive") && self._getCurrentlySelectedText() === "") {
                        self.model.edit();
                    }
                    self._editDelay = 0;
                }, 250);
            }
        }
    },

    /**
     * Either the field inputs have blurred, or the save options have blurred,
     * but we need to check if both are blurred
     */
    _onPossibleBlur: function() {
        if (this.model.getSaving()) {
            // Saving is already in progress. No need to trigger save-on-blur.
            return;
        }
        if (jQuery(".aui-blanket").length > 0) {
            // A modal dialog has interrupted editing. Don't attempt to save now.
            return;
        }
        this.model.blurEdit();
    },

    /**
     * Prevent AUI's default handler from blurring inputs when pressing the Esc key, since we handle this ourselves in onCancel.
     * This stops the field from being blurred by pressing Esc when the calendar dropdown is shown.
     */
    _preventBlurByEsc: function(e) {
        e.preventDefault();
    },

    /**
     * @private
     * @return {string}
     */
    _getCurrentlySelectedText: function() {
        if (jQuery(document.activeElement).is(":input")) {
            // Text selections inside form elements are not considered.
            return "";
        }
        if (document.selection && document.selection.createRange) {
            return document.selection.createRange().text || "";
        }
        if (window.getSelection) {
            return window.getSelection().toString();
        }
        return "";
    }
});
