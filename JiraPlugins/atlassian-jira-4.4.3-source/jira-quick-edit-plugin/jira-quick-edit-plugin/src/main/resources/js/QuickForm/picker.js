/**
 * A View class that renders a list of buttons representing fields in a form
 * @see JIRA.QuickForm.AbstractConfigurableForm. These buttons @see JIRA.QuickForm.FieldPicker.Button
 * can be clicked to toggle the visibility of the corresponding @see JIRA.QuickForm.ConfigurableField.
 *
 * @class FieldPicker
 */
JIRA.QuickForm.FieldPicker = AJS.Control.extend({

    INLINE_DIALOG_ID: "field_picker_popup",

    /**
     * @constructor
     * @param {JIRA.QuickForm.AbstractConfigurableForm} form
     */
    init: function () {
        this.buttons = [];
    },

    switchToAll: function (silent) {
        var instance = this;
        this.isConfigurable = false;
        if (silent !== true) {
            this.inlineDialog.hide();
            this.triggerEvent("switchedToAll");
        }
    },

    setForm: function (form) {
        this.form = form;
    },

    switchToCustom: function (silent) {
        var instance = this;
        this.isConfigurable = true;
        if (silent !== true) {
            this.inlineDialog.hide();
            this.triggerEvent("switchedToCustom");
        }
    },

    /**
     * Builds an inline dialog that when triggered displayes a list of fields checkboxes that can be modified to
     * hide and show fields.
     *
     * @return AJS.InlineDialog
     */
    bindInlineDialog: function () {

        var instance = this;

        function setContents (contents, trigger, doShowPopup) {
            instance.render().done(function (body) {
                if (contents.find(".qf-picker").length === 0) {
                    contents.html(body);
                    contents.click(function (e) {
                        e.stopPropagation();
                    });
                }

                doShowPopup();

                var offsetY = jQuery(trigger).offset().top,
                    windowHeight = jQuery(window).height(),
                    maxHeight = windowHeight - offsetY - 100;
                    body.find(".qf-picker-content").css("maxHeight", maxHeight);
            });
        }

        this.getInlineDialogTrigger().click(function (e) {
            jQuery(this).addClass("active");
        });

        this.inlineDialog = AJS.InlineDialog(this.getInlineDialogTrigger(), this.INLINE_DIALOG_ID, setContents, {
            width: 400
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
                        var button = new JIRA.QuickForm.FieldPicker.ConfigurableButton({
                            field: field
                        });
                        button.render().appendTo($li);
                        instance.buttons.push(button);
                    }
                });
            } else {
                button = new JIRA.QuickForm.FieldPicker.UnconfigurableButton(field);
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


JIRA.QuickForm.FieldPicker.UnconfigurableButton = AJS.Control.extend({

    init: function (descriptor) {
        this.descriptor = descriptor;
    },

    render: function () {
        this.$element = jQuery(JIRA.Templates.QuickForm.unconfigurablePickerButton({
                label: this.descriptor.label,
                isActive: true
            }));

        return this.$element;
    }
});

/**
 * A view class that renders a button that when clicked toggles the visibility of corresponding
 * @see JIRA.QuickForm.ConfigurableField.
 * @class Button
 */
JIRA.QuickForm.FieldPicker.ConfigurableButton = AJS.Control.extend({

    /**
     * @constructor
     * @param options
     * ... {field} JIRA.QuickForm.ConfigurableField
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
            label: this.field.getLabel(),
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