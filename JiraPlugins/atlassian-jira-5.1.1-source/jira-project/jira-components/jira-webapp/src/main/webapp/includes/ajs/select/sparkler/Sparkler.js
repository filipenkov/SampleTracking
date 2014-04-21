/**
 * A multiselect list that can be queried and suggestions selected via a dropdown. Suggestions are retrieved via AJAX.
 *
 * @constructor AJS.MultiSelect
 * @extends AJS.QueryableDropdownSelect
 */
AJS.Sparkler = AJS.QueryableDropdownSelect.extend({

    /**
     * This constructor:
     * <ul>
     *  <li>Overrides default options with user options</li>
     *  <li>Inserts an input field before dropdown</li>
     *  <li>Adds items currently selected in the &lt;select&gt; as items sitting on the top of the textarea</li>
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

        this.options.element = AJS.$(this.options.element);

        AJS.$(this.options.element).hide();

        if (this.options.disabled) {
            this._createFurniture(true);
            return this;
        }

        this.model = new AJS.SelectModel({
            element: this.options.element,
            removeOnUnSelect: this.options.removeOnUnSelect
        });

        this.options.element.bind("updateOptions", function () {
            instance._setOptions(options);
        }).bind("selectOption", function (e, descriptor) {
            instance.addItem(descriptor);
        }).bind("removeOption", function (e, descriptor) {
            instance.removeItem(descriptor);
        });

        // Add the visual representation
        this._createFurniture();

        this.dropdownController = {
            show: jQuery.noop,
            setWidth: jQuery.noop,
            setPosition: jQuery.noop,
            hide: jQuery.noop
        };


        var $list = AJS.$(".aui-list", this.$container);

        this.listController = new AJS.List({
            containerSelector: $list,
            scrollContainer: $list,
            delegateTarget: this.$field,
            groupSelector: "ul.aui-list-section",
            matchingStrategy: this.options.matchingStrategy,
            maxInlineResultsDisplayed: this.options.maxInlineResultsDisplayed,
            renderers: {
                suggestion: this._renders.suggestionItem
            },
            selectionHandler: function (e) {
                instance._selectionHandler(this.getFocused(), e);
                return false;
            }
        });

        this._assignEventsToFurniture();

        if (this.options.width) {
            this.setFieldWidth(this.options.width)
        }

        if (this.options.inputText) {
            this.$field.val(this.options.inputText);
            this.updateFreeInputVal();
        }

        if (this.options.includeSelectAll) {

            var $listOperationsContainer = this._render("listOperationsContainer");
            var selectAllItemDescriptor = new AJS.ItemDescriptor({
                label: this.options.selectAllLabel
            });

            $listOperationsContainer.find("ul").append(this._render("suggestionItem", selectAllItemDescriptor));
            $listOperationsContainer.insertAfter(this.$field);

            var s = "#" + this.$container.attr("id") + " input:checkbox";
            // TODO: These live events aren't working. WHY??
            this._assignEvents("checkboxes", s);

            this.$allOption = $listOperationsContainer.find("input:checkbox").first();
        } else {
            this.$allOption = jQuery();
        }

        this.render();

        this.model.$element.trigger("initialized", [this]);

        return this;
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
            minRoomForText: 50,
            includeSelectAll: true,
            selectAllLabel: "All [TODO:i18n]", // TODO: Add JWAS key
            errorMessage: AJS.I18n.getText("jira.ajax.autocomplete.error"),
            ajaxOptions: {
                minQueryLength: 0
            },
            showDropdownButton: false
        });
    },

    /**
     * Appends furniture around specified dropdown element. This includes:
     *
     * errorMessage - A container for error messages is created but not appended until needed
     *
     * @method _createFurniture
     * @protected
     */
    _createFurniture: function (disabled) {
        var id = this.model.$element.attr("id");

        // remove placeholder if there is one. This placeholder, takes up the space that the multi-select control will
        // while the page is being loaded and the "real" control has not been inserted. i.e Stops the page jumping around.
        if (this.model.$element.prev().hasClass("ajs-multi-select-placeholder")) {
            this.model.$element.prev().remove();
        }

        if (disabled) {
            this.model.$element.replaceWith(this._render("disableSelectField", id));
        } else {
            this.$container = this._render("container", id);
            this.$field = this._render("field", id).appendTo(this.$container);
            this.$container.append(this._render("suggestionsContainer", id));
            this.$container.insertBefore(this.model.$element);
            this.$errorMessage = this._render("errorMessage", id);
        }
    },

    /**
     * Assigns events furniture
     *
     * @method _assignEventsToFurniture
     */
    _assignEventsToFurniture: function () {
        var instance = this;
        this._assignEvents("body", document);
        // if this control is created as the result of a keydown event then we do no want to catch keyup or keypress for a moment
        setTimeout(function() {
            instance._assignEvents("field", instance.$field);
            instance._assignEvents("keys", instance.$field);
        }, 15);
    },

    /**
     * Gets value for suggestion/option mirroring user input
     *
     * @method _getUserInputValue
     * @protected
     * @return {String}
     */
    _getUserInputValue: function () {
        return this.options.uppercaseUserEnteredOnSelect ? this.$field.val().toUpperCase(): this.$field.val();
    },

    /**
     * Adds the user's input as an option in the suggestions and the select model
     *
     * @method _handleUserInputOption
     */
    _handleUserInputOption: function () {

        var groupDescriptor;

        if (!this.hasUserInputtedOption() || this.$field.val().length === 0) {
            return;
        }

        groupDescriptor = new AJS.GroupDescriptor({
            type: "optgroup",
            label: "user inputted option",
            weight: 9999,
            showLabel: false,
            replace: true
        });

        groupDescriptor.addItem(new AJS.ItemDescriptor({
            value: this._getUserInputValue(),
            label: this.$field.val(),
            labelSuffix: " (" + this.options.userEnteredOptionsMsg + ")",
            title: this.$field.val(),
            allowDuplicate: false,
            noExactMatch: true          // this item doesn't count as an exact query match for selection purposes
        }));

        this.model.appendOptionsFromJSON([groupDescriptor]);
    },

    /**
     * Returns weather or not an option will be appended to the bottom of the suggestions, mirroring the user input.
     * The will be true if at invocation the userEnteredOptionMsg is set in the options object. The value of this property
     * will be displayed in brackets after the suggestion.
     *
     * @method hasUserInputtedOption
     * @return {Boolean}
     */
    hasUserInputtedOption: function () {
        return this.options.userEnteredOptionsMsg;
    },

    /**
     * Some special handling for user inputted option, and removal of item in select list if we are replacing them from
     * the server.
     *
     * @method _handleSuggestionResponse
     * @override
     * @protected
     * @param {Array} data
     */
    _handleServerSuggestions: function (data) {

        // If we are querying the server then the server will return the full result set to be displayed. We do not
        // want any linguring options in there.
        if (this.options.ajaxOptions.query) {
            this.model.clearUnSelected();
        }

        // we have to do this again as we clear all the unselected options above, which includes the user inputted option
        this._handleUserInputOption();
        this._super(data);
    },

    /**
     * Adds supplied suggestions to dropdown and &lt;select&gt; list
     *
     * @method _setSuggestions
     * @param {Object} data - JSON representing suggestions
     * @param {Object} context - JSON representing suggestion context, including group (if any) and filter flag
     */
    _setSuggestions: function (data, context) {
        if (data) {
            this.model.appendOptionsFromJSON(data);
            if (!this.hasInputtedValue) {
                var suggestions = [];
                suggestions.push(new AJS.GroupDescriptor({
                    items: this.model.getDisplayableSelectedDescriptors()
                }));
                suggestions.push(new AJS.GroupDescriptor({
                    items: this.model.getUnSelectedDescriptors()
                }));
                this._super(suggestions, context);
            } else {
                this._super(this.model.getAllDescriptors(), context);
            }

        } else {
            this.hideSuggestions();
        }
    },

    /**
     * Clears the control - selection(s) and field text.
     */
    clear: function () {
        this.$field.val('');
        this._handleCharacterInput(true);
        this.clearSelection();
    },

    /**
     * Clears selection(s) and sets back to editing mode.
     */
    clearSelection: function () {
        this.model.setAllUnSelected();
        this.model.$element.trigger("unselect", [this]);
    },

    /**
     * Unselects item in model
     *
     * @method removeItem
     * @param {Object} descriptor
     */
    removeItem: function (descriptor) {
        this.model.setUnSelected(descriptor);
        this.render();
        this.model.$element.trigger("unselect", [descriptor, this])
    },
    /**
     },
     * Adds items currently selected in the &lt;select&gt; as items sitting on the top of the textarea
     *
     * @method _restoreSelectedOptions
     */
    _restoreSelectedOptions: function () {
        var instance = this;

        // creates selected "button" style representation
        AJS.$.each(this.model.getDisplayableSelectedDescriptors(), function () {
            instance.addItem(this, true);
        });
    },

    /**
     * Handling of backspace in textarea.
     */
    _handleBackSpace: function () {
        var instance = this;
        this.$field.one("keyup", function() {
            instance._handleCharacterInput();
        });
    },

    /**
     * Handling of delete in textarea.
     */
    _handleDelete: function () {
        if (AJS.$.trim(this.$field.val()) !== "") {
            // delay until character is actually removed from textarea
            var instance = this;
            this.$field.one("keyup", function () {
                instance._handleCharacterInput();
            });
        }
    },

    /**
     * Handle text paste event.
     */
    _handlePaste: function() {
        // Remove whitespace such as newlines, tabs, etc.
        this.$field.val(AJS.$.trim(this.$field.val()).replace(/\s+/g, " "));
        this._handleCharacterInput();
    },

    /**
     * Adds selected suggestion to the selected items, and marks it as selected in the model.
     *
     * @method _addItem
     * @param {Object} descriptor - JSON describing suggestion/option
     */
    addItem: function(descriptor, initialize) {

        if (descriptor instanceof AJS.ItemDescriptor) {
            descriptor = AJS.copyObject(descriptor.allProperties(), false);
        }

        descriptor.value = AJS.$.trim(descriptor.value);
        descriptor.label = AJS.$.trim(descriptor[this.options.itemAttrDisplayed]) || descriptor.value;
        descriptor.title = AJS.$.trim(descriptor.title) || descriptor.label;

        descriptor = new AJS.ItemDescriptor(descriptor);

        this.model.setSelected(descriptor);
        this.render();

        if (!initialize) {
            this.model.$element.trigger("selected", [descriptor, this]);
        }
    },

    /**
     * Adds multiple items
     *
     * @method _addMultipleItems
     * @param {Array} items - Array of item descriptors. e.g {value: "The val", label: "Label to be displayed in suggestions"}
     * @param {Boolean} removeOnUnSelect - If set to true, if the item is removed from the control (unselected), the option
     * will also be deleted from the select model also. This means it will not appear in the suggestions dropdown.
     */
    _addMultipleItems: function(items, removeOnUnSelect) {

        var instance = this;

        AJS.$.each(items, function (i, descriptor) {
            if (removeOnUnSelect) {
                descriptor.removeOnUnSelect = true;
            }
            instance.addItem(descriptor);
        });
    },

    /**
     * @method _selectionHandler - Handle when a suggestion is accepted
     * @param selected
     * @param e
     */
    _selectionHandler: function (selected, e) {
        var instance = this;
        this.hasInputtedValue = true;
        selected.each(function () {
            var descriptor = AJS.$.data(this, "descriptor");
            if (!descriptor.selected()) {
                instance.addItem(descriptor);
            } else {
                instance.removeItem(descriptor);
            }
        });
        this.$field.val("").focus().scrollIntoView({ margin: 20 });
        this.hideSuggestions();
        this.hideErrorMessage();
        this.updateFreeInputVal();
        this.model.$element.trigger("change");

        e.preventDefault();
    },

    /**
     * @method isValidItem - Determines whether the given value represents a valid item
     * @param {String} itemValue
     * @return {Boolean}
     */
    isValidItem: function(itemValue) {
        var suggestedItemDescriptor = this.listController.getFocused().data("descriptor");
        if (!suggestedItemDescriptor) {
            return false;
        }
        itemValue = itemValue.toLowerCase();
        return itemValue === AJS.$.trim(suggestedItemDescriptor.label.toLowerCase()) ||
                itemValue === AJS.$.trim(suggestedItemDescriptor.value.toLowerCase());
    },

    /**
     * @method handleFreeInput
     */
    handleFreeInput: function() {
        var value = AJS.$.trim(this.$field.val()),
            descriptor;

        if (value) {
            descriptor = this.model.getDescriptor(value);
            if (descriptor) {
                this.addItem(descriptor);
                this.model.$element.trigger("change");
                this.$field.val("");
                this.hideErrorMessage();
            } else if (!this.options.submitInputVal) {
                this.showErrorMessage(value);
            }
        }
    },

    hideSuggestions: jQuery.noop,

    /**
     * Submits form
     * @method submitForm
     */
    submitForm: function () {
        if (this.$field.val().length === 0 && !this.suggestionsVisible) {
            AJS.$(this.$field[0].form).submit(); // submit on enter if field is empty
        }
    },

    _handleCharacterInput: function (ignoreBuffer, ignoreQueryLength) {
        this._super(ignoreBuffer, ignoreQueryLength);
        this.updateFreeInputVal();
    },

    _deactivate: function () {
        this.handleFreeInput();
        this.hideSuggestions();
    },

    render: function () {
        this._handleCharacterInput(true, true);
    },

    _selectAll: function() {
        this.model.setAllSelected();
        this.$allOption.prop({
            checked: true,
            indeterminate: false
        });
        this._getItemCheckboxes().prop("checked", true);
    },

    _unselectAll: function() {
        this.model.setAllUnSelected();
        this.$allOption.prop({
            checked: false,
            indeterminate: false
        });
        this._getItemCheckboxes().prop("checked", false);
    },

    _getItemCheckboxes: function() {
        return this.$container.find("input:checkbox").not(this.$allOption);
    },

    _updateAllOptionState: function() {
        var $checkboxes = this._getItemCheckboxes();
        var properties = {
            checked: false,
            indeterminate: false
        };
        if ($checkboxes.filter(":checked").length > 0) {
            if ($checkboxes.filter(":checked").length > 0) {
                // All items are checked.
                properties.checked = false;
            } else {
                // Some items are checked.
                properties.indeterminate = true;
            }
        }
        this.$allOption.prop(properties);
    },

    keys: {
        "Backspace": function () {
            this._handleBackSpace();
        },
        "Del": function () {
            this._handleDelete();
        },
        "Return": function (e) {
            this.submitForm();
            e.preventDefault();
        },
        "Tab" : function (e) {
            this.acceptFocusedSuggestion();
        }
    },

    _events: {
        body: {
            // handling for the case where control is in a tab, and as a result hidden.
            tabSelect: function () {
                if (this.$field.is(":visible")) {
                    this.updateItemsIndent();
                }
            },
            // Bulk operations don't yet use AUI forms.
            bulkTabSelect: function() {
                if (this.$field.is(":visible")) {
                    this.updateItemsIndent();
                }
            }
        },
        field: {
            paste: function() {
                // This must be delayed because the pasted text has not been added yet.
                setTimeout(AJS.$.proxy(this, "_handlePaste"), 0);
            },
            "aui:keydown aui:keypress": function(event) {
                this.hasInputtedValue = true;
            },
            click: function() {
                this.$field.focus();
            }
        },
        checkboxes: {
            click: function(event) {
                if (event.target === this.$allOption[0]) {
                    if (event.target.checked) {
                        this._selectAll();
                    } else {
                        this._unselectAll();
                    }
                } else {
                    this._updateAllOptionState();
                }
            }
        }
    },

    _renders: {
        errorMessage: function (idPrefix) {
            return AJS.$('<div class="error" />').attr('id', idPrefix + "-error");
        },
        field: function (idPrefix) {
            //  the wrap="off" attribute prevents text from growing under the labels. It doesn't prevent linebreaks
            return AJS.$('<textarea autocomplete="off" id="' + idPrefix + '-textarea" class="aui-field" wrap="off"></textarea>');
        },
        disableSelectField: function (id) {
            return AJS.$("<input type='text' class='long-field' name='" + id + "' id='" + id + "' />");
        },
        container : function (idPrefix) {
            return AJS.$('<div class="multi-select sparkler-select" id="' + idPrefix +'-multi-select">');
        },
        suggestionsContainer : function (idPrefix) {
            return AJS.$('<div class="aui-list" id="' + idPrefix + '-suggestions"></div>');
        },
        listOperationsContainer: function() {
            return AJS.$('<div class="aui-list sparkler-list-operations"><ul class="aui-list-section"></ul></div>');
        },
        suggestionItem: function(descriptor, replacementText) {

            //adding the label as a class for testing.
            var idSuffix = descriptor.fieldText() || descriptor.label();
            var itemId = AJS.escapeHTML(AJS.$.trim(idSuffix.toLowerCase()).replace(/[\s\.]+/g, "-")),
                    listElem = AJS.$('<li class="aui-list-item aui-list-item-li-' + itemId + '">'),
                    linkElem = AJS.$('<a />').addClass("aui-list-item-link aui-list-item-checked");

            if (descriptor.selected()) {
                listElem.addClass("aui-checked");
            }

            linkElem.attr("href", descriptor.href() || "#");

            if (descriptor.styleClass()) {
                linkElem.addClass(descriptor.styleClass());
            }

            var $label = jQuery("<span class='item-label' />");

            if (replacementText) {
                $label.html(replacementText);
            } else if (descriptor.html()) {
                $label.html(descriptor.html());
            } else {
                $label.text(descriptor.label());
            }

            if (descriptor.labelSuffix()) {
                var suffixSpan = AJS.$("<span class='aui-item-suffix' />").text(descriptor.labelSuffix());
                $label.append(suffixSpan);
            }

            var $checkbox = jQuery("<input type='checkbox' />");

            if (descriptor.selected()) {
                $checkbox.attr("checked", "checked");
            }

            linkElem.append($checkbox);

            if (descriptor.icon() && descriptor.icon() !== "none") {
                linkElem.append("<img src='" + descriptor.icon() + "' />");
            }

            linkElem.append($label);

            listElem.append(linkElem).data("descriptor", descriptor);

            return listElem;
        }
    }
});


//jQuery(function () {
//
//    new AJS.Sparkler({
//        itemAttrDisplayed: "label",
//        element: jQuery("#searcher-pid")
//    });
//})
