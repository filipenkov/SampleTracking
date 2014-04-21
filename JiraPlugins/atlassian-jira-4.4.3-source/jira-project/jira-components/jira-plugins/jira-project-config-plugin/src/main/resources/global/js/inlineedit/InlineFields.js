/**
 * An abstract field. Here we define default behaviour for fields such as error handling.
 * 
 */
JIRA.InlineEdit.Field = AJS.Control.extend({

    /**
     * Shows specified error in inline dialog under field
     * @param error
     */
    showError: function (error) {

        var instance = this;

        this.error = JIRA.InlineEdit.getInlineErrorDialog(this.$field, error);

        // triggered when cancel link is clicked on
        this.error.bind("revert", function () {

            // this will hide dialog and revert the value
            instance.error.hide();
            instance.deactivate();
        });

        this.error.show();
        this.$field.focus();
    },


    /**
     * Checks if the inline error dialog is visible
     */
    hasErrors: function () {
        return this.error && this.error.is(":visible");
    },

    /**
     * Determines what action to perform when we blur. Should we attempt to save etc.
     */
    handleConfirm: function () {

        // If the value has changed (since field was init or since last "update" attempt) and there is no errors
        if (this.valueHasChanged() && !this.hasErrors()) {
            this.trigger("updated");

        // The field value as not changed so error is still valid
        } else if (this.hasErrors()) {
            this.$field.focus();

        // The field has not changed and there is no errors, it has not been updated.
        } else {
            this.deactivate();
        }
    }

});


/**
 * A text input that will grow as your type
 */
JIRA.InlineEdit.Text = JIRA.InlineEdit.Field.extend({

    /**
     * @constructor
     */
    init: function () {
        this.$field = jQuery("<textarea class='aui-inline-edit-field aui-inline-edit-text' />");
        this.$throbber = jQuery("<div class='aui-inline-edit-throbber' />");
    },

    /**
     * Positions and shows throbber (spinner) at the right end of field
     */
    showLoading: function () {
        var fieldOffset = this.$field.offset();
        this.$throbber.insertAfter(this.$field).css({
            left: fieldOffset.left + this.$field.outerWidth() - 25,
            top: fieldOffset.top + (this.$field.outerHeight() / 2)
        }).show();
    },

    /**
     * Removes throbber from DOM
     */
    hideLoading: function () {
        this.$throbber.remove();
    },

    /**
     * Activates field
     *
     * @param value
     * @param referenceElement
     */
    activate: function (value, referenceElement) {

        // we want to make sure that the field size represents that of the read only view
        this.$field.css({
            fontSize: referenceElement.css("fontSize"),
            fontWeight: referenceElement.css("fontWeight"),
            fontFamily: referenceElement.css("fontFamily"),
            height: referenceElement.css("fontSize"),
            lineHeight: referenceElement.css("fontSize")

        })
        .show()
        .val(value)
        .focus()
        .select();

        // store the value so we can check if it has changed later
        this.initValue = value || "";

        this._assignEvents("field", this.$field);
        this.$field.width(this.getWidth());
    },

    /**
     * Reverts value
     */
    deactivate: function () {
        this._unassignEvents("field", this.$field);
        this.trigger("deactivate");
    },

    /**
     * Lazily adds an element that we use to determine what width our input should be
     *
     * @return jQuery
     */
    getMeasure: function () {

        if (!this.$measure) {
            this.$measure = jQuery("<span />").css({
                position:"absolute",
                left: -9999,
                top: 0
            }).appendTo("body");
        }

        this.$measure.css({
            fontSize: this.$field.css("fontSize"),
            fontWeight: this.$field.css("fontWeight"),
            fontFamily: this.$field.css("fontFamily")
        });

        return this.$measure;
    },

    /**
     * gets the field width, which is calculated of our measure element
     */
    getWidth: function () {
        var throbber_affordance = this.$field.height() + 8; // some space for loading indicator
        return this.getMeasure().text(this.$field.val()).outerWidth() + throbber_affordance;
    },

    /**
     * Gets label, text displayed in view
     *
     * @return label - text that is displayed in readonly view
     */
    getLabel: function () {
        return this.$field.val();
    },

    /**
     * Gets value, string that is sent to server
     *
     * @return value - value that is sent to the server
     */
    getValue: function () {
        return this.$field.val();
    },

    /**
     * Gets parent element of field
     *
     * @return jQuery
     */
    getContainer: function () {
        return this.$field;
    },

    /**
     * Focuses field
     */
    focus: function () {
        this.$field.focus();
    },

    /**
     * Has value changed
     *
     * @return boolean
     */
    valueHasChanged: function () {
        return this.initValue !== this.$field.val();
    },

    keys: {
        // submit when we press enter
        "Return" : function (e) {
            this.$field.blur();
            e.preventDefault();
        },
        "Esc" : function (e) {
            if (this.error) {
                this.error.hide();
            }
            this.deactivate();
        }
    },

    /**
     * When ever we edit the value hide errors and update field width
     */
    onEdit: function () {
        var instance = this;

        if (this.hasErrors()) {
            this.error.hide();
        }

        this.$field.one("keyup", function () {
            instance.$field.width(instance.getWidth());
        });
    },

    _events: {
        field: {
            "aui:keydown aui:keypress": function(e) {
               this._handleKeyEvent(e);
            },
            // if we leave the field, determine if we save (value has changed) or cancel (value has not changed)
            blur: function () {
                this.handleConfirm();
            }
        }
    }
});




/**
 * A text input that will grow as your type
 */
JIRA.InlineEdit.Textarea = JIRA.InlineEdit.Field.extend({

    /**
     * @constructor
     */
    init: function () {
        this.$field = jQuery("<textarea class='aui-inline-edit-field aui-inline-edit-textarea' />");

        this.$field.expandOnInput();
        
        this.$throbber = jQuery("<div class='aui-inline-edit-throbber' />");
    },

    /**
     * Positions and shows throbber (spinner) at the right end of field
     */
    showLoading: function () {
        var fieldOffset = this.$field.offset();
        this.$throbber.insertAfter(this.$field).css({
            left: fieldOffset.left + this.$field.outerWidth() - 25,
            top: fieldOffset.top + 5
        }).show();
    },

    /**
     * Removes throbber from DOM
     */
    hideLoading: function () {
        this.$throbber.remove();
    },

    /**
     * Activates field
     *
     * @param value
     * @param referenceElement
     */
    activate: function (value, referenceElement) {

        // we want to make sure that the field size represents that of the read only view
        this.$field
        .show()
        .val(value)
        .focus();

        // store the value so we can check if it has changed later
        this.initValue = value || "";

        this.$field.trigger("keyup");

        this._assignEvents("field", this.$field);
    },

    /**
     * Reverts value
     */
    deactivate: function () {
        this._unassignEvents("field", this.$field);
        this.trigger("deactivate");
    },

    /**
     * gets the field width, which is calculated of our measure element
     */
    getWidth: function () {
        var throbber_affordance = this.$field.height() + 8; // some space for loading indicator
        return this.getMeasure().text(this.$field.val()).outerWidth() + throbber_affordance;
    },

    /**
     * Gets label, text displayed in view
     *
     * @return label - text that is displayed in readonly view
     */
    getLabel: function () {
        return this.$field.val();
    },

    /**
     * Gets value, string that is sent to server
     *
     * @return value - value that is sent to the server
     */
    getValue: function () {
        return this.$field.val();
    },

    /**
     * Gets parent element of field
     *
     * @return jQuery
     */
    getContainer: function () {
        return this.$field;
    },

    /**
     * Focuses field
     */
    focus: function () {
        this.$field.focus();
    },

    /**
     * Has value changed
     *
     * @return boolean
     */
    valueHasChanged: function () {
        return this.initValue !== this.$field.val();
    },

    keys: {
        "Esc" : function (e) {
            if (this.error) {
                this.error.hide();
            }
            this.deactivate();
        }
    },

    /**
     * When ever we edit the value hide errors and update field width
     */
    onEdit: function () {
        if (this.hasErrors()) {
            this.error.hide();
        }
    },

    _events: {
        field: {
            "aui:keydown aui:keypress": function(e) {
               this._handleKeyEvent(e);
            },
            // if we leave the field, determine if we save (value has changed) or cancel (value has not changed)
            blur: function () {
                this.handleConfirm();
            }
        }
    }
});




/**
 * Inline wrapper for AJS.SingleSelect
 */
JIRA.InlineEdit.SingleSelect = JIRA.InlineEdit.Field.extend({

    /**
     * @constructor
     * @param options
     */
    init: function (options) {
        this.$select = jQuery(options.element); // <select> element
    },

    /**
     * Lazily creates single select control
     *
     * @param value
     */
    getSingleSelect: function (value) {

        var instance = this;

        if (!this.singleSelect) {

            this.singleSelect = new AJS.SingleSelect({
                element: this.$select,
                itemAttrDisplayed: "label",
                width: 150
            });

            /**
             * Handle the case where text remains unselected in the text field
             */
            this.singleSelect.handleFreeInput = function() {

                var value = AJS.$.trim(this.$field.val()),
                    descriptor;

                if (value === "") {
                    instance.setNullValue();
                } else if (value) {
                    descriptor = this.model.getDescriptor(value);
                    if (descriptor) {
                        this.setSelection(descriptor);
                        this.model.$element.trigger("change");
                    } else {
                        instance.showError("Invalid value");
                    }
                }
            };


            // save to server when we select something
            this.$select.bind("selected", function (e, selected) {

                instance.singleSelect.disable();
                instance.handleConfirm();
            }).bind("query", function () {
                if (instance.error) {
                    instance.error.hide();
                }
            });

            this.$field = this.singleSelect.$field.bind("keydown", function (e) {
                if (e.keyCode === 27 && !instance.singleSelect.dropdownController.$layer.is(":visible")) {
                    if (instance.error) {
                        instance.error.hide();
                    }
                    instance.deactivate();
                }
            });
        }

        return this.singleSelect;
    },

    /**
     * Sets null value
     */
    setNullValue: function () {
        var instance = this,
            descriptors = this.getSingleSelect().model.getAllDescriptors(false);
        jQuery.each(descriptors, function (i, descriptor) {
            if (descriptor && descriptor.value() === "-1") {
//                debugger;
                instance.getSingleSelect().setSelection(descriptor);
            }
        })
    },

    /**
     * Reverts value
     */
    deactivate: function () {
        if (this.singleSelect.model.getSelectedDescriptors()[0] !== this.initDescriptor) {
            this.singleSelect.setSelection(this.initDescriptor);
        }
        this.trigger("deactivate");
    },

    /**
     * Determins if value has changed
     */
    valueHasChanged: function () {
        if (this.getValue() !== this.initDescriptor.value()) {
            return true;
        }
    },

    /**
     * Shows throbber
     */
    showLoading: function () {
        this.getSingleSelect().$dropDownIcon.addClass("loading");
    },

    /**
     * Hides throbber
     */
    hideLoading: function () {
        this.getSingleSelect().$dropDownIcon.removeClass("loading");
    },

    /**
     * Gets string that will be displayed in readonly view
     */
    getLabel: function () {
        var selectedDescriptor = this.getSingleSelect().model.getSelectedDescriptors()[0];
        if (selectedDescriptor) {
            return selectedDescriptor.label();
        } else {
            return "";
        }
    },

    /**
     * Gets value that will be sent back to server
     */
    getValue: function () {
        var value = this.getSingleSelect().model.getSelectedDescriptors()[0];
        if (value) {
            return value.value();
        } else {
            return "-1";
        }
    },

    keys: {},

    /**
     * Activates field
     * 
     * @param value
     */
    activate: function () {
        this.getSingleSelect().$field.focus();
        this.getSingleSelect().enable();
        this.initDescriptor = this.singleSelect.model.getSelectedDescriptors()[0];
    },

    /**
     * Gets parent container
     */
    getContainer: function () {
        return this.getSingleSelect().$container;
    }
});