/**
 * A single select list that can be queried and suggestions selected via a dropdown. Suggestions are retrieved via AJAX or Statically.
 *
 * @constructor AJS.SingleSelect
 * @extends AJS.QueryableDropdownSelect
 */
AJS.SingleSelect = AJS.QueryableDropdownSelect.extend({

    /**
     * This constructor:
     * <ul>
     *  <li>Overrides default options with user options</li>
     *  <li>Inserts an input field before dropdown</li>
     *  <li>Displays selection in input field, if there is a selected option in the select field</li>
     * <ul>
     *
     * @param options
     */
    init: function (options) {

        var instance = this;

        if (this._setOptions(options) === this.INVALID) {
            return this.INVALID;
        }

        jQuery.extend(this, AJS.SelectHelper);

        var element = AJS.$(this.options.element);
        element.hide();

        // Used to retrieve and set options in our <select> element via a JSON interface
        this.model = new AJS.SelectModel({
            element: this.options.element,
            removeOnUnSelect: this.options.removeOnUnSelect
        });

        if (this.options.disabled) {
            this._createFurniture(true);
            return this;
        }

        this.model.$element.bind("reset", function () {
            var selectedDescriptor = instance.getSelectedDescriptor();
            if (selectedDescriptor) {
                instance.setSelection(instance.getSelectedDescriptor());
            }
        });

        this.options.id = element.attr("id") || element.attr("name");

        element.data("aui-ss", true);

        element.bind('set-selection-value', function (e, value) {
            instance._setDescriptorWithValue(value);
        });

        // Add the visual representation
        this._createFurniture();

        this.dropdownController = AJS.InlineLayer.create({
            alignment:AJS.LEFT,
            offsetTarget: this.$field,
            content: AJS.$(".aui-list", this.$container)
        });

        var listOptions = {
            serverDataGroupId: options.serverDataGroupId,
            containerSelector: AJS.$(".aui-list", this.$container),
            groupSelector: "ul.aui-list-section",
            matchingStrategy: this.options.matchingStrategy,
            maxInlineResultsDisplayed: this.options.maxInlineResultsDisplayed,
            matchItemText: this.options.matchItemText,
            selectionHandler: function (e) {
                var selectedSuggestion = this.getFocused(),
                        selectedDescriptor = selectedSuggestion.data("descriptor");

                instance.setSelection(selectedDescriptor);
                instance.$field.select();

                e.preventDefault();
                return false;
            }
        };
        this.listController = AJS.ListBuilder.newList(listOptions);

        this._assignEventsToFurniture();

        // If editValue is set, ignore any selected option
        if (this.options.editValue) {
            this._setEditingMode();
            this.$field.val(this.options.editValue);
        // display selected, if there is one.
        } else if (this.getSelectedDescriptor()) {
            this.setSelection(this.getSelectedDescriptor());
        // otherwise turn editing on
        } else {
            this._setEditingMode();
            if (this.options.inputText) { // inputText is really placeholder text
                this.$field.val(this.options.inputText);
            }
        }

        if (this.options.width) {
            this.setFieldWidth(this.options.width)
        }

        if (this.$overlabel) {
            this.$overlabel.overlabel(this.$field);
        }

        this.model.$element.trigger("initialized", [this]);

        return this;
    },


    /**
     * Sets field width
     *
     * @param {Number} width - field width
     */
    setFieldWidth: function (width) {

        this.$container.css({
            width: width,
            minWidth: width
        });

        this.$field.width(width);
    },

    /**
     * Returns the selected descriptor. Undefined if there is none.
     *
     * @return {AJS.ItemDescriptor}
     */
    getSelectedDescriptor: function () {
        return this.model.getDisplayableSelectedDescriptors()[0];
    },

    /**
     * Gets the value that has been configured to display to the user. Uses label by default.
     *
     * @param {AJS.ItemDescriptor} descriptor
     * @return {String}
     */
    getDisplayVal: function (descriptor) {
        return descriptor[this.options.itemAttrDisplayed || "label"]();
    },

    /**
     * Gets default options
     *
     * @method _getDefaultOptions
     * @protected
     * @return {Object}
     */
    _getDefaultOptions: function () {
        return AJS.$.extend(true, this._super(), {
            errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.error"),
            ajaxOptions: {
                minQueryLength: 1
            },
            revertOnInvalid: false,
            showDropdownButton: true
        });
    },

    /**
     * Appends furniture around specified dropdown element. This includes:
     *
     * <ul>
     *  <li>errorMessage - A container for error messages is created but not appended until needed</li>
     *  <li>selectedItemsWrapper - A wrapper for selected items</li>
     *  <li>selectedItemsContainer - A container for selected items</li>
     * </ul>
     *
     * @method _createFurniture
     * @protected
     */
    _createFurniture: function (disabled) {

        var id = this.model.$element.attr("id");

        this.$container = this._render("container", this.options.id);
        var containerClass = this.model.$element.data('container-class');
        if (containerClass) {
            this.$container.addClass(containerClass);
        }

        if (disabled) {
            var value = this.model.$element.val() && this.model.$element.val()[0];
            value = value || "";
            this.model.$element.replaceWith(this._render("disableSelectField", id, value));
        } else {
            this.$field = this._render("field", this.options.id).appendTo(this.$container);
            this.$container.append(this._render("suggestionsContainer", this.options.id));
            this.$container.insertBefore(this.model.$element);
            this.$dropDownIcon = this._render("dropdownAndLoadingIcon", this._hasDropdownButton()).appendTo(this.$container);
            this.$errorMessage = this._render("errorMessage");

            if (this.options.overlabel) {
                this.$overlabel = this._render("overlabel").insertBefore(this.$field);
            }
        }
    },

    /**
     * If there is a selection, search for everything, otherwise use the field value.
     *
     * @return {String}
     */
    getQueryVal: function () {
        if (this.$container.hasClass("aui-ss-editing")) {
            return this.$field.val();
        } else {
            return "";
        }
    },

    /**
     * Adds supplied suggestions to dropdown and &lt;select&gt; list
     *
     * @method _setSuggestions
     * @param {Object} data - JSON representing suggestions
     * @param {Object} context - JSON representing suggestion context, including:
     * - {String} groupId - a group in the model to set suggestions inside of. If blank, set for the entire model.
     * - {boolean} filter - true if the data should be filtered on any query entered
     */
    _setSuggestions: function (data, context) {
        if (data) {
            var modified = this.model.appendOptionsFromJSON(data);
            if (modified) {
                data = this.model.getUnSelectedDescriptors(context);
            }
            this._super(data, context);
            this.model.$element.trigger("suggestionsRefreshed", [this]);
        } else {
            this.hideSuggestions();
        }
    },

    /**
     * Sets to editing mode. Clearing the appearence of a selection.
     */
    _setEditingMode: function () {
        this.$container.addClass("aui-ss-editing")
                .removeClass("aui-ss-has-entity-icon");
        // Workaround for IE9 form element styling bug JRADEV-6299
        this.$field.css("paddingLeft");
    },

    _hasIcon: function () {
        var icon,
            selectedDescriptor = this.getSelectedDescriptor();
        if (selectedDescriptor) {
            icon = selectedDescriptor.icon();
            return icon && icon !== "none";
        }
    },

    /**
     * Sets to readonly mode. Displaying the appearence that something is selected.
     */
    _setReadOnlyMode: function () {

        this.$container.removeClass("aui-ss-editing");

        if (this._hasIcon()) {
            this.$container.addClass("aui-ss-has-entity-icon");
            // Workaround for IE9 form element styling bug JRADEV-6299
            if (jQuery.browser.msie && jQuery.browser.version > 8) {
                this.$container.append(this.$field.detach());
            }
        }
    },
    
    /**
     * Submits form
     *
     * @method submitForm
     */
    submitForm: function () {
        if (!this.suggestionsVisible) {
            this.handleFreeInput();
            AJS.$(this.$field[0].form).submit(); // submit on enter if field is empty
        }
    },

    /**
     * Allows a selection to made based on the value of an option. Useful for tests.
     *
     * @param value - internal value of an option item to select. If no matching options exists nothing happens.
     */
    selectValue: function (value) {
        this.listController.selectValue(value);
    },

    /**
     * Sets as selected in model and changes styling to demonstrate selection
     *
     * @param {AJS.ItemDescriptor} descriptor
     */
    setSelection: function (descriptor) {

        if (typeof descriptor === "string") {
            descriptor = new AJS.ItemDescriptor({
                value: descriptor,
                label: descriptor
            })
        }

        this.removeFreeInputVal();

        if (this.model.setSelected(descriptor)) {
            this.hideErrorMessage();
        }

        var fieldValue = descriptor.fieldText() || this.getDisplayVal(descriptor);
        this.$field.val(fieldValue);

        if (this._hasIcon()) {
            if (this.$entityIcon) {
                this.$entityIcon.remove();
            }

            this.$entityIcon = this._render("entityIcon", descriptor.icon()).appendTo(this.$container);
        }

        this._setReadOnlyMode();
        this.hideSuggestions();

        this.lastSelection = descriptor;
        this.model.$element.trigger("selected", [descriptor, this]);
    },

    /**
     * Clears the control - selection and field text.
     */
    clear: function () {
        this.$field.val('');
        this._handleCharacterInput(true);
        this.clearSelection();
    },

    /**
     * Clears selection and sets back to editing mode
     */
    clearSelection: function () {

        var instance = this;
        instance._setEditingMode();
        instance.model.setAllUnSelected();
        instance.model.$element.trigger("unselect", [this]);
    },

    /**
     * Removal of items in select list if we are replacing them from the server.
     *
     * @method _handleServerSuggestions
     * @override
     * @protected
     * @param {Array} data
     */
    _handleServerSuggestions: function (data) {
        this.cleanUpModel();
        this._super(data);
    },

    /**
     * If we are querying the server then the server will return the full result set to be displayed. We do not
     * want any linguring options in there.
     */
    cleanUpModel: function () {
        if (this.options.ajaxOptions.query) {
            this.model.clearUnSelected();
        }
    },

     /**
     * Handles editing of input value
     */
    onEdit: function (e) {

        if (e.key === "\r") {
            return;
        }

        this._super(e);

        if (this.getSelectedDescriptor()) {
            // prevent Internet explorer from redirecting browser
            if (this.$field.val().length === 0 && e.key === "Backspace") {
                e.preventDefault();
            }
            this.clearSelection();
        }

        this.model.$element.trigger("query");
    },

    /**
     * Handle the case where text remains unselected in the text field
     */
    handleFreeInput: function(value) {

        value = value || AJS.$.trim(this.$field.val());

        if (this.options.revertOnInvalid && !this.model.getDescriptor(value)) {
            this.setSelection(this.lastSelection);
        } else if (this.$container.hasClass("aui-ss-editing")) {
            if (this._setDescriptorWithValue(value)) {
                this.hideErrorMessage();
            } else if (!this.options.submitInputVal) {
                this.showErrorMessage(value);
            }
        }
    },

    _setDescriptorWithValue: function (value) {
        var descriptor = this.model.getDescriptor(value);
        if (descriptor) {
            this.setSelection(descriptor);
            return true;
        }
        return false;
    },

    _handleCharacterInput: function (ignoreBuffer, ignoreQueryLength) {
        this._super(ignoreBuffer, ignoreQueryLength);
        if (this.$container.hasClass("aui-ss-editing")) {
            this.updateFreeInputVal();
        }
    },

    _deactivate: function () {
        this.handleFreeInput();
        this.hideSuggestions();
    },

    keys: {
        "Return": function (e) {
            this.submitForm();
            e.preventDefault();
         },
        "Tab" : function () {
            this.acceptFocusedSuggestion();
        }
    },

    _events: {
        field: {
            focus: function () {
                var instance = this;
                window.setTimeout(function () {
                    if (instance.$field.is(":focus")) {
                        instance.$field.select();
                    }
                }, 0);
            },
            click: function () {
                // force show suggestion
                this._handleCharacterInput(true, this._hasDropdownButton());
            }
        }
    },

    _renders: {
        label: function (label, id) {
            return AJS.$("<label />").attr("for", id).text(label).addClass("overlabel");
        },
        errorMessage: function () {
            return AJS.$('<div class="error" />');
        },
        entityIcon: function (url) {
            return AJS.$('<div class="aui-ss-entity-icon" />').css("backgroundImage",  "url('" + url + "')");
        },
        field: function (idPrefix) {
            return AJS.$('<input id="' + idPrefix + '-field" class="text aui-ss-field ajs-dirty-warning-exempt" autocomplete="off" />');
        },
        disableSelectField: function (id, value) {
            return AJS.$("<input type='text' class='long-field' value='" + value + "' name='" + id + "' id='" + id + "' />");
        },
        container : function (idPrefix) {
            return AJS.$('<div class="aui-ss" id="' + idPrefix +'-single-select">');
        },
        suggestionsContainer : function (idPrefix) {
            return AJS.$('<div class="aui-list" id="' + idPrefix + '-suggestions" tabindex="-1"></div>');
        },
        dropdownAndLoadingIcon: function (showDropdown) {
            var $element = AJS.$('<span class="icon aui-ss-icon noloading"><span>More</span></span>');
            if  (showDropdown) {
                $element.addClass("drop-menu");
            }
            return $element;
        }
    }
});
