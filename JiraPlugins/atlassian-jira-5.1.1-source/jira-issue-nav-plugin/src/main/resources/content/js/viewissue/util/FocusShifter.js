AJS.namespace("JIRA.Issues.FocusShifter");

/**
 * @param {Object} options
 * ... {EditIssueController} editIssueController The associated EditIssueController.
 * ... {object} hideTriggers Events that should cause the focus shifter to hide.
 */
JIRA.Issues.FocusShifter = function(options) {
    _.extend(this, Backbone.Events);

    options = _.defaults(options, {
        messageFactory: JIRA.Messages.showMsg
    });

    var BLUR_FOCUS_DELAY = 50;
    var ESCAPE_KEY_CODE = 27;
    var viewIssueController = options.viewIssueController;
    var editIssueController = viewIssueController.getSelectedEditIssueController();
    var instance = this;

    /**
     * Binds the hide triggers passed in the options object.
     */
    this._bindHideTriggers = function() {
        this.bind("show", function() {
            // Hide the focus shifter for each of the described events.
            _.each(options.hideTriggers, function(eventBus, events) {
                eventBus.bind(events, instance._hide);
            });

            // Unbind all the events so the focus shifter can be GC-ed.
            this.bind("hide", function() {
                _.each(options.hideTriggers, function(eventBus, events) {
                    eventBus.unbind(events, instance._hide);
                })
            });
        });
    };

    /**
     * Hide the focus shifter.
     *
     * Triggers the "hide" event.
     */
    this._hide = function() {
        if (this.$message) {
            this.$message.detach();
        }

        this.trigger("hide");
    };

    this._hideOnBlur = function() {
        var instance = this;
        AJS.$('*', this.$message).blur(function(e) {
            setTimeout(function() {
                // If queryableDropdownSelect is disabled, then we're waiting for a save to complete before focusing a
                // field. We don't want the focus shifter to disappear on the user because that would be confusing.
                if (!instance.queryableDropdownSelect.disabled) {
                    var m = instance.$message;
                    if (m && !m.find(document.activeElement).length && m !== document.activeElement) {
                        instance._hide();
                    }
                }
            }, BLUR_FOCUS_DELAY);
        });
    };

    /**
     * Fill the given input with the field last selected by the user.
     *
     * Does nothing if we are yet to store any "selected field" information.
     */
    this._loadLastField = function() {
        var lastFieldId = sessionStorage.getItem("JIRA.Issues.FocusShifter.lastFieldId");
        if (lastFieldId !== null) {
            var fieldModels = editIssueController.getFields().models;
            var suggestions = JIRA.Issues.FocusShifter._suggestions(fieldModels)();
            var lastSelectedSuggestion = _.find(suggestions, function(suggestion) {
                return suggestion.value() === lastFieldId;
            });

            if (lastSelectedSuggestion) {
                this.queryableDropdownSelect.$field.val(lastSelectedSuggestion.label());
                this.queryableDropdownSelect._handleCharacterInput(false, false);
            }
        }
    };

    /**
     * Show the focus shifter.
     *
     * Triggers the "show" event.
     */
    this._show = function() {
        if (!this.$message) {
            this.$message = options.messageFactory("", {
                closeable: true,
                type: JIRA.Templates.ViewIssue.Body.focusShifter
            });
        }

        var fieldModels = editIssueController.getFields();
        var $el = AJS.$("<div/>").addClass("aui-list").appendTo("#focus-shifter-content");
        this.queryableDropdownSelect = new AJS.QueryableDropdownSelect({
            element: $el,
            suggestions: JIRA.Issues.FocusShifter._suggestions(fieldModels.models)
        });

        this._loadLastField();
        this.queryableDropdownSelect.$field.focus().select();

        var instance = this;
        this.$message.keyup(function (e) {
            if (e.keyCode === ESCAPE_KEY_CODE) {
                e.stopPropagation();
                instance._hide();
            }
        });
        $el.delegate("li", "click", function(e) {
            // Will be undefined if the user clicks the "No Matches" item.
            var descriptor = AJS.$(this).data("descriptor");
            if (descriptor) {
                var field = fieldModels.get(descriptor.value());
                if (field) {
                    sessionStorage.setItem("JIRA.Issues.FocusShifter.lastFieldId", field.id);

                    if (!field.getSaving()) {
                        field.edit();
                    } else {
                        // A save is in progress, so we can't put the field into edit mode. Wait until the save is done.
                        instance.queryableDropdownSelect.disable();
                        JIRA.one(JIRA.Events.ISSUE_REFRESHED, function() {
                            instance._hide();
                            field.edit();
                        });
                    }
                }
            }

            e.preventDefault();
        });

        JIRA.Issues.FocusShifter.suppressTip();
        this._hideOnBlur();
        this.trigger("show");
    };

    _.bindAll(this);
    this._bindHideTriggers();

    var fieldModels = editIssueController.getFields().models;
    if (JIRA.Issues.FocusShifter._shouldShow(fieldModels)) {
        this._show();
    }

    JIRA.Issues.Analytics.trigger("kickass.focusshifteropened");
};

/**
 * Determines whether the focus shifter should show with the given field data.
 *
 * It shouldn't appear if the suggestions list will be empty.
 *
 * @param {Array<FieldModel>} fieldModels The field data that is to be used.
 * @return {boolean} Whether the focus shifter should appear.
 */
JIRA.Issues.FocusShifter._shouldShow = function(fieldModels) {
    // TODO: Calculate the cost of fetching the suggestions twice.
    return JIRA.Issues.FocusShifter._suggestions(fieldModels)().length > 0;
};

/**
 * Create and return a suggestion function based on the given field models.
 *
 * The given array is filtered to include only editable, visible fields.
 *
 * @param {Array<FieldModel>} fieldModels The field models on which suggestions are to be based.
 * @return {function} A function that returns suggestions to be shown by the focus shifter.
 */
JIRA.Issues.FocusShifter._suggestions = function(fieldModels) {
    // Creates an AJS.ItemDescriptor for the given field model.
    var makeDescriptor = function(fieldModel) {
        return new AJS.ItemDescriptor({
            label: fieldModel.getLabel(),
            value: fieldModel.id
        });
    };

    var hasEditableView = function(fieldModel) {
        return JIRA.Issues.IssueFieldModel.IS_EDITABLE(fieldModel)
            && fieldModel.matchesFieldSelector();
    };

    return function() {
        var editableFields = _.filter(fieldModels, hasEditableView);
        return _.map(editableFields, makeDescriptor);
    }
};

/**
 * Suppress the focus shifter tip for the authenticated user.
 */
JIRA.Issues.FocusShifter.suppressTip = function() {
    AJS.$.ajax({
        data: "tipKey=focusShifter",
        type: "POST",
        url: contextPath + "/rest/issueNav/1/suppressedTips"
    });
};
