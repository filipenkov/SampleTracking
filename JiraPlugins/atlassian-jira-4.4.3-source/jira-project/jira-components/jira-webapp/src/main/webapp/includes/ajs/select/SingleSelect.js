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

        AJS.$(this.options.element).hide();

        if (this.options.disabled) {
            this._createFurniture(true);
            return this;
        }

        // Used to retrieve and set options in our <select> element via a JSON interface
        this.model = new AJS.SelectModel({
            element: this.options.element,
            removeOnUnSelect: this.options.removeOnUnSelect
        });

        this.options.id = this.model.$element.attr("id");

        AJS.$(this.options.element).data("aui-ss", true);

        // Add the visual representation
        this._createFurniture();

        this.dropdownController = AJS.InlineLayer.create({
            alignment:AJS.LEFT,
            offsetTarget: this.$field,
            content: AJS.$(".aui-list", this.$container)
        });

        this.listController = new AJS.List({
            containerSelector: AJS.$(".aui-list", this.$container),
            groupSelector: "ul.aui-list-section",
            itemSelector: "li",
            matchingStrategy: this.options.matchingStrategy,
            selectionHandler: function (e) {

                var selectedSuggestion = this.getFocused(),
                    selectedDescriptor = selectedSuggestion.data("descriptor");

                instance.setSelection(selectedDescriptor);
                e.preventDefault();
                return false;
            }
        });

        this._assignEventsToFurniture();

        // display selected, if there is one.
        if (this.getSelectedDescriptor()) {
            this.setSelection(this.getSelectedDescriptor());

        // otherwise turn editing on
        } else {
            this._setEditingMode();

            if (this.options.inputText) {
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
        return this.model.getSelectedDescriptors()[0];
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


        this.$container = this._render("container", this.options.id);

        this.$field = this._render("field", this.options.id).appendTo(this.$container);
        this.$container.append(this._render("suggestionsContainer", this.options.id));
        this.$container.insertBefore(this.model.$element);
        this.$dropDownIcon = this._render("dropdownAndLoadingIcon", this._hasDropdownButton()).appendTo(this.$container);
        this.$errorMessage = this._render("errorMessage");

        if (this.options.overlabel) {
            this.$overlabel = this._render("overlabel").insertBefore(this.$field);
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
     */
    _setSuggestions: function (data) {
        if (data) {
            this.model.appendOptionsFromJSON(data);
            this._super(this.model.getUnSelectedDescriptors());
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
        return this.getSelectedDescriptor().icon() && this.getSelectedDescriptor().icon() !== "none";
    },

    /**
     * Sets to readonly mode. Displaying the appearence that something is selected.
     */
    _setReadOnlyMode: function () {

        this.$container.removeClass("aui-ss-editing");


        if (this._hasIcon()) {
            this.$container.addClass("aui-ss-has-entity-icon");

            // Workaround for IE9 form element styling bug JRADEV-6299
            this.$container.append(this.$field.detach());
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

        this.model.setSelected(descriptor);
        this.$field.val(this.getDisplayVal(descriptor));

        if (this._hasIcon()) {
            if (this.$entityIcon) {
                this.$entityIcon.remove();
            }

            this.$entityIcon = this._render("entityIcon", descriptor.icon()).appendTo(this.$container);
        }

        this._setReadOnlyMode();
        this.hideSuggestions();
        this.hideErrorMessage();

        this.$field.select();
        this.model.$element.trigger("selected", [descriptor, this]);
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
     * Handles editing of input value
     */
    onEdit: function (e) {

        var instance = this;

        if (e.key === "\r") {

            return;

        } else if (this.getSelectedDescriptor()) {

            // prevent Internet explorer from redirecting browser
            if (this.$field.val().length === 0 && e.key === "Backspace") {
                e.preventDefault();
            }
            this.clearSelection();
        }

        // delay until character is insterted into field
        this.$field.one("keyup", function () {
            instance._handleCharacterInput();
        });

        this.model.$element.trigger("query");
    },

    /**
     * Handle the case where text remains unselected in the text field
     */
    handleFreeInput: function() {

        var value = AJS.$.trim(this.$field.val()),
            descriptor;

        if (this.$container.hasClass("aui-ss-editing") && value) {
            descriptor = this.model.getDescriptor(value);
            if (descriptor) {
                this.setSelection(descriptor);
                this.model.$element.trigger("change");
            } else if (this.options.submitInputVal) {
                descriptor = new AJS.ItemDescriptor({
                    value: value,
                    label: value,
                    removeOnUnSelect: true
                });
                this.model.setSelected(descriptor);
            } else {
                this.showErrorMessage(value);
                return;
            }
        }
        this.hideErrorMessage();
    },

    /**
     * Unbinds events from field
     */
    disable: function () {
        this._unassignEvents("field", this.$field);
    },

    /**
     * Binds events to field
     */
    enable: function () {
        this._assignEvents("field", this.$field);
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
        "Tab" : function (e) {
            this.acceptFocusedSuggestion();
        }
    },

    _events: {
        field: {
            focus: function () {
                var instance = this;
                window.setTimeout(function () {
                    instance.$field.select();
                }, 0);
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
        disableSelectField: function (id) {
            return AJS.$("<input type='text' class='long-field' name='" + id + "' id='" + id + "' />");
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
