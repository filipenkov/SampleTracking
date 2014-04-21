/**
 * A View class that renders a list of buttons representing fields in a form
 * @see JIRA.Forms.AbstractConfigurableForm. These buttons @see JIRA.Forms.FieldPicker.Button
 * can be clicked to toggle the visibility of the corresponding @see JIRA.Forms.ConfigurableField.
 *
 * @class FieldPicker
 */
JIRA.Forms.FieldPicker = AJS.Control.extend({

    INLINE_DIALOG_ID: "field_picker_popup",

    /**
     * @constructor
     * @param {JIRA.Forms.AbstractConfigurableForm} form
     */
    init: function () {
        this.buttons = [];
    },

    switchToAll: function (silent) {
        this.isConfigurable = false;
        if (silent !== true) {
            this.hideCallback = function () {
                this.remove();
            };
            this.inlineDialog.hide();
            this.triggerEvent("switchedToAll");
        }
    },

    setForm: function (form) {
        this.form = form;
    },

    switchToCustom: function (silent) {
        this.isConfigurable = true;
        if (silent !== true) {
            this.hideCallback = function () {
                this.remove();
            };
            this.inlineDialog.hide();
            this.triggerEvent("switchedToCustom");
        }
    },

    hideCallback: function () {
    },

    /**
     * Builds an inline dialog that when triggered displayes a list of fields checkboxes that can be modified to
     * hide and show fields.
     *
     * @return AJS.InlineDialog
     */
    bindInlineDialog: function () {

        var instance = this,
            $trigger = this.getInlineDialogTrigger();

        function setContents (contents, trigger, doShowPopup) {
            instance.render().done(function (body) {
                if (contents.find(".qf-picker").length === 0) {
                    contents.html(body);
                    contents.click(function (e) {
                        e.stopPropagation();
                    });
                }

                var offsetY = jQuery("#qf-field-picker-trigger:visible").offset().top- jQuery(window).scrollTop(),
                windowHeight = jQuery(window).height(),
                maxHeight = windowHeight - offsetY - 100;

                contents.find(".qf-picker-content").css("maxHeight", maxHeight);
                doShowPopup();
            });
        }

        if (this.inlineDialog) {
            this.inlineDialog.remove();
        }

        this.inlineDialog = AJS.InlineDialog($trigger, this.INLINE_DIALOG_ID, setContents, {
            width: 400,
            upfrontCallback: function () {
                $trigger.parent().addClass("active");
            },
            hideCallback: function () {
                $trigger.parent().removeClass("active");
            },
            nobind : true
        });

        // JRADEV-8535: Second click on configure fields should hide dialog
        $trigger.click(function (e) {
            if (jQuery("#inline-dialog-" + instance.INLINE_DIALOG_ID).is(":visible")) {
                instance.inlineDialog.hide();
            } else {
                instance.inlineDialog.show();
            }
        });

        return this.inlineDialog;
    },

    /**
     * Gets the DOM element that when clicked opens inline dialog
     *
     * @return {jQuery}
     */
    getInlineDialogTrigger: function () {
        return this.form.$element.find("#qf-field-picker-trigger");
    },

    /**
     * Renders set of buttons to picker container
     *
     * @param fields - field descriptors
     */
    renderButtons: function (fields) {

        var instance = this,
            $list = jQuery('<ul class="qf-picker-buttons" />').appendTo(this.$content);

        jQuery.each(fields, function (i, field) {

            var $li,
                button;

            if (instance.isConfigurable) {
                instance.form.getFieldById(field.id).done(function (field) {
                    if (field && field.hasVisibilityFeature()) {
                        var $li = jQuery("<li />").appendTo($list);
                        var button = new JIRA.Forms.FieldPicker.ConfigurableButton({
                            field: field
                        });
                        button.render().appendTo($li);
                        instance.buttons.push(button);
                    }
                });
            } else {
                button = new JIRA.Forms.FieldPicker.UnconfigurableButton(field);
                $li = jQuery("<li />").appendTo($list);
                button.render().appendTo($li);
            }
        });

        if ($list.children().length === 0) {
            $list.addClass("qf-no-fields").append("<li><em>" + AJS.I18n.getText("quickform.no.fields") +"</em></li>")
        }
    },

    /**
     * Renders contents of picker
     *
     * @return jQuery.Promise
     */
    renderContents: function () {

        var instance = this,
            deferred = jQuery.Deferred();

        this.form.model.getSortedTabs().done(function (tabs) {

            if (tabs.length === 1) {
                instance.renderButtons(tabs[0].fields);
            } else {
                jQuery.each(tabs, function (i, tab) {
                    if (tab.fields.length > 0) {
                        jQuery('<h4><span></span></h4>').appendTo(instance.$content)
                            .find("span").text(tab.label);
                        instance.renderButtons(tab.fields);
                    }
                });
            }

            deferred.resolveWith(instance, [instance.$element]);
        });

        return deferred.promise();
    },

    /**
     * Shows inline dialog
     */
    show: function () {
        this.inlineDialog.show();
    },

    /**
     * Renders create issue picker with a link at the buttom to switch to "full create".
     *
     * @return jQuery.Promise
     */
    render: function () {


        this.$element = jQuery(JIRA.Templates.QuickForm.fieldPicker({
                isConfigurable: this.isConfigurable
            }));
        this.$content = this.$element.find(".qf-picker-content");

        this._assignEvents("switchToCustom", this.$element.find(".qf-configurable"));
        this._assignEvents("switchToAll", this.$element.find(".qf-unconfigurable"));

        return this.renderContents();


    },

    _events: {
        switchToCustom: {
            click: function (e) {
                this.switchToCustom();
                e.preventDefault();
            }
        },
        switchToAll: {
            click: function (e) {
                this.switchToAll();
                e.preventDefault();
            }
        }
    }
});


JIRA.Forms.FieldPicker.UnconfigurableButton = AJS.Control.extend({

    init: function (descriptor) {
        this.descriptor = descriptor;
    },

    render: function () {
        this.$element = jQuery(JIRA.Templates.QuickForm.unconfigurablePickerButton({
                required: this.descriptor.required,
                label: this.descriptor.label,
                fieldId: this.descriptor.id,
                isActive: true
            }));

        return this.$element;
    }
});

/**
 * A view class that renders a button that when clicked toggles the visibility of corresponding
 * @see JIRA.Forms.ConfigurableField.
 * @class Button
 */
JIRA.Forms.FieldPicker.ConfigurableButton = AJS.Control.extend({

    /**
     * @constructor
     * @param options
     * ... {field} JIRA.Forms.ConfigurableField
     */
    init: function (options) {

        var instance = this;

        this.field = options.field;

        this.field.bind("disabled", function () {
            instance.$element.removeClass("qf-active");
        }).bind("activated", function () {
            instance.$element.addClass("qf-active");
        });
    },

    /**
     * Toggles visibility of field
     */
    toggle: function () {
        if (this.field.isActive()) {
          this.field.disable();
        } else {
          this.field.activate();
        }
    },

    /**
     * Gets field id
     * @return {String}
     */
    getId: function () {
        return this.field.getId();
    },

    /**
     * Renders button
     * @return {jQuery}
     */
    render: function () {
        this.$element = jQuery(JIRA.Templates.QuickForm.configurablePickerButton({
            required: this.field.descriptor.required,
            label: this.field.getLabel(),
            fieldId: this.field.getId(),
            isActive: this.field.isActive()
        }));
        this._assignEvents("button", this.$element);
        return this.$element;
    },

    _events: {
        button: {
            click: function (e) {
                this.toggle();
                e.preventDefault();
            }
        }
    }
});