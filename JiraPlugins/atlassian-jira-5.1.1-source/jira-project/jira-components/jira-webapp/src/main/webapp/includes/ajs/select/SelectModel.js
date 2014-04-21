/**
 * Gives a JSON interface to a &lt;select list&gt;. Allowing you to add elements via JSON descriptors. It also
 * provides utility methods to retrieve collections of elements as JSON, for example selected options.
 *
 * @constructor AJS.SelectModel
 * @extends AJS.Control
 */
AJS.SelectModel = AJS.Control.extend({

    /**
     * Sets some defaults and parses all options, and option group into JSON. This json representation is bound
     * to it's corresponding DOM element, and can be accessed using jQuery([OPTION OR OPTGROUP HERE]).data("descriptor")
     *
     * @constructor
     * @param {jQuery | Selector | HTMLElement} options - <select> tag for the in dom represenation of model
     */
    init: function (options) {

        var instance = this;

        if (options.element) {
            options.element = AJS.$(options.element);
        } else {
            options.element = AJS.$(options);
        }

        this._setOptions(options);

        this.$element = this.options.element;

        this.type = this.$element.attr("multiple") ? "multiple" : "single";

        if (this.type === "single") {
            this.$element.attr("multiple", "multiple");
        }

        // provide a way for people to dynamically update/populate the <select> and have it be reflected in frother control
        this.$element.bind("reset", function () {
            instance._parseDescriptors();
        });

        // Get a json representation of our <select>
        this._parseDescriptors();
    },

    /**
     * Gets value of <option> that represents the free text in <input>
     * @return {*}
     * @private
     */
    _getFreeInputEl: function () {
        return this.$element.find(".free-input");
    },

    /**
     * Removes free input option
     */
    removeFreeInputVal: function () {
        this._getFreeInputEl().remove();
    },

    /**
     * Updates <option> that represents the free text in <input>
     * @param {String} val
     */
    updateFreeInput: function (val) {
        var $freeInput = this._getFreeInputEl();
        val = AJS.$.trim(val);
        if (val) {
            if (!$freeInput.length) {
                $freeInput = AJS.$("<option class='free-input' />").appendTo(this.$element);
            }
            $freeInput.attr({
                "value" : val,
                "selected" : "selected"
            });
        } else {
            $freeInput.remove();
        }
    },

    /**
     * Gets default options
     *
     * @method _getDefaultOptions
     * @private
     * @return {Object}
     */
    _getDefaultOptions: function () {
        return {};
    },

    /**
     * Returns all the selected values
     *
     * @return {Array}
     */
    getSelectedValues: function () {
        var selectedVals = [],
            selected = this.getDisplayableSelectedDescriptors();
        for (var i=0; i < selected.length; i++) {
            selectedVals.push(selected[i].value());
        }
        return selectedVals;
    },

    /**
     * Used to set selected state on option. In the case of a single select this will remove the selected property
     * from all other options.
     *
     * @method setSelected
     * @param {Object} descriptor - JSON object describing option. E.g. {label: "one", value: 1}
     */
    setSelected: function (descriptor) {

        var instance = this,
            selectedItem = false,
            changed = false,
            $toSelect = this.$element.find("option:not(.free-input)").filter(function() {
                // need to filter rather than using jQuery expression in find as value may container special characters.
                return this.value === descriptor.value();
            });

        // In the case of a single select we want to loop over all the <option>'s and remove selection so we are in a clean state.
        // Please note that our <select> is actually a <select multiple="multiple" />.
        if (this.type === "single") {

            // No need to update here
            if (this.getValue() === descriptor.value()) {
                return changed;
            }

             // As we are a single select we only want to select the first option, even though there may be multiple
            // <option>'s with the same value
            $toSelect = $toSelect.first();

            this.$element.find("option:not(.free-input):selected").each(function (i, option) {
                var currDescriptor = AJS.$(this).data("descriptor");
                if ($toSelect[0] !== option) {
                    instance.setUnSelected(currDescriptor);
                }
            });
        }

        $toSelect.each(function () {
            var $this = AJS.$(this);
            selectedItem = true;
            changed = !$this.is(':selected');          // if already selected, it hasn't really changed
            $this.attr("selected", "selected").data("descriptor").selected(true);
        });

        //if the option doesn't exist, create it! This may be useful for free text dropdowns like the labels
        if (!selectedItem) {
            descriptor.selected(true);
            var newOption = this._render("option", descriptor);
            newOption.attr("selected", "selected");
            this.$element.append(newOption);
            changed = true;
        }

        // We need to manually fire the change event as this doesn't fire when we programmatically change the value.
        if (changed) {
            this.$element.trigger('change', descriptor);
        }

        return changed;
    },


    /**
     * Sets all options to unselected
     *
     * @method setAllSelected
     * @private
     */
    setAllSelected: function () {
        var instance = this;
        AJS.$(this.getDisplayableUnSelectedDescriptors()).each(function () {
            instance.setSelected(this);
        });
    },


    /**
     * Sets all options to unselected
     *
     * @method setAllUnSelected
     * @private
     */
    setAllUnSelected: function () {
        var instance = this;
        AJS.$(this.getDisplayableSelectedDescriptors()).each(function () {
            instance.setUnSelected(this);
        });
    },


    /**
     * Used to set unselected an option. Note, this will unselect and option with the same value also.
     *
     * @method setUnSelected
     * @param {Object} descriptor - JSON object decribing option
     */
    setUnSelected: function (descriptor) {

        var instance = this;

        this.$element.find("option:not(.free-input):selected")
            .filter(function () {
                return AJS.$(this).attr("value") === descriptor.value();
            })
            .each(function () {
                var $this = AJS.$(this);
                if (instance.options.removeOnUnSelect || $this.data("descriptor").removeOnUnSelect()) {
                    $this.remove();
                } else {
                    $this.removeAttr("selected");
                    $this.data("descriptor").selected(false);
                }
            });
    },


    /**
     * Return the option with an identical value to our descriptor, if present in the provided context.
     *
     * @method _getOptionItem
     * @private
     * @param {Object} descriptor - JSON object decribing option
     * @param {jQuery | HTMLElement} ctx - Context to search for option. If not provided will search entire select
     * @return {jQuery}
     */
    _getOptionItem: function (descriptor, ctx) {

        ctx = ctx || this.$element;
        var value = descriptor.value().replace(/"/g, '\\"');

        return ctx.find('option:not(.free-input)[value="' + value + '"]');
    },


    /**
     * Return the optiongroup with an identical label to our descriptor, if present in the provided context.
     *
     * @method _getOptionGroup
     * @private
     * @param  {Object} descriptor - JSON object decribing optgroup
     * @return {jQuery}
     */
    _getOptionGroup: function (descriptor, ctx) {

        ctx = ctx || this.$element;
        return ctx.find("optgroup").filter(function () {
            return this.id === descriptor.id() || AJS.$(this).attr("label") === descriptor.label();
        });
    },


    /**
     * Removes corresponding option from select
     *
     * @method remove
     * @param {Object} descriptor - JSON object decribing option
     */
    remove: function (descriptor) {
        if(descriptor && descriptor.model()) {
            descriptor.model().remove();
        }
    },

    /**
     * Loops over all unselected descriptors searching for one that either matches the value or label of argument
     *
     * @method getDescriptor
     * @param {String} value - value or label to test against
     * @return {Object} descriptor
     */
    getDescriptor: function (value) {

        var returnDescriptor;

        value = AJS.$.trim(value.toLowerCase());

        AJS.$.each(this.getAllDescriptors(false), function (e, descriptor) {
            if (value === AJS.$.trim(descriptor.label().toLowerCase()) ||
               value === AJS.$.trim(descriptor.value().toLowerCase())) {
                returnDescriptor = descriptor;
                return false; //bail out of loop, we are done
            }
        });

        return returnDescriptor;
    },

    /**
     * Appends options to select  using an array of JSON descriptors. ONLY options that have not already been defined.
     *
     * <pre>
     * // structure
     * [
     *  {
     *      label: "History Search",
     *      type: "optgroup",
     *      weight: 0, // order in list (starts from 0)
     *      options: [
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-2 - test,
     *              type: "option"
     *          },
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-1 - I am a iest Issue,
     *              type: "option"
     *          }
     *      ]
     *  },
     *  {
     *      label: "Current Search",
     *      type: "optgroup"
     *      weight: 1, // order in list (starts from 0)
     *      options: [
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-2 - test,
     *              type: "option"
     *          },
     *          {
     *              icon: "/jira/images/icons/issue_subtask.gif",
     *              label: "HSP-1 - I am a iest Issue,
     *              type: "option"
     *          }
     *      ]
     *   }
     * ]
     * </pre>
     *
     * @method appendOptionsFromJSON
     * @param {Array} optionDescriptors - JSON tree describing options and optgroups to be appended
     *
     * @return true if the model is updated in any way
     */
    appendOptionsFromJSON: function (optionDescriptors) {

        var modified = false;

        for (var i = 0, len = optionDescriptors.length; i < len; i++) {

            var descriptor = optionDescriptors[i];
            if (descriptor instanceof AJS.GroupDescriptor) {

                // Ensure an <optgroup> exists to match this GroupDescriptor
                this._appendGroup(descriptor);
                modified = true;

            } else if (descriptor instanceof AJS.ItemDescriptor) {

                // Ensure an <option> exists to match this ItemDescriptor
                if (!this._getOptionItem(descriptor).length) {
                    this._render("option", descriptor).appendTo(this.$element);
                    modified = true;
                }
            }
        }

        return modified;
    },

    _appendGroup: function (descriptor) {

        var optgroup = this._getOptionGroup(descriptor);
        var container = this.$element;
        var replaceItems = descriptor.replace();

        // 1. Either clean up an existing <optgroup> or create one.
        if (optgroup.length) {
            if (replaceItems) {
                optgroup.find("option:not(:selected, .free-input)").remove();        // clears *unused* options, see NOTE 1 below
            }
        } else {
            optgroup = this._render("optgroup", descriptor);
        }
        optgroup.data("descriptor", descriptor);

        // 2. Update or create <option> items inside the group. If item scope for this group is not the default, items
        //    will only be added to this group if they don't exist in the specified scope.
        var items = descriptor.items();
        var itemScope = (descriptor.uniqueItemScope() == 'container') ? container : optgroup;
        for (var i = 0, len = items.length; i < len; i++) {

            var optDescriptor = items[i];
            var option = this._getOptionItem(optDescriptor, itemScope);
            if (!option.length) {
                optgroup.append(this._render("option", optDescriptor));
            } else if (!replaceItems) {
                // NOTE 1 - selected options will therefore not be updated if replaceItemsInGroup is true.
                option.data("descriptor", optDescriptor);
            }
        }

        // 3. Check to see if this Group has a weight and should go somewhere specific in the container.
        var target;
        var weight = descriptor.weight();
        if (typeof weight !== "undefined") {
            // This Group wants to go into the container at a particular index... see if something else
            // is in the way.
            target = container.children()[weight];
            if (target === optgroup[0]) {
                // This Group already exists in the container at the correct index - leave it there.
                return;
            }
        }

        // 4. Either put the new <optgroup> in its place or at the bottom of the container.
        if (target) {
            // There's another group or item where this Group wants to go - insert to bump it down.
            optgroup.insertBefore(target);
        } else {
            optgroup.appendTo(container);
        }
    },

    /**
     * Supplied with an option element, a JSON object describing it is bound to it using jQuery's data method
     *
     * @method _parseOption
     * @private
     * @param {jQuery | HTMLElement} optionElem - element to which a json descriptor will be bound
     * @return {Object} descriptor - JSON object decribing option
     */
    _parseOption: function (optionElem) {

        var descriptor,
            icon;

        optionElem = AJS.$(optionElem);

        if (this.options.removeNullOptions && this._hasNullValue(optionElem)) {
            optionElem.remove(); // thank you
            return null;
        }

        icon = optionElem.attr("data-icon");

        descriptor = new AJS.ItemDescriptor({
            styleClass: optionElem.prop("className"),
            value: optionElem.val(),

            // The text that will appear in the field when this item is selected. Also used for the item id.
            fieldText: optionElem.attr("data-field-text"),

            title: optionElem.attr("title"),

            // The text that will be displayed in the dropdown item
            label: optionElem.attr("data-field-label") || AJS.$.trim(optionElem.text()),

            icon: icon ? icon : optionElem.css("backgroundImage").replace(/url\(['"]?(.*?)['"]?\)/,"$1"), // we just store the url
            selected: optionElem.prop("selected"),
            model: optionElem
        });

        optionElem.data("descriptor", descriptor);

        return descriptor;
    },

    /**
     * Checks if given option element has 'null' value. As 'null' we understand any value lower than 0 that indicates
     * this option will be used to reset field value to null.
     *
     * @method _hasNullValue
     * @private
     * @param {jQuery | HTMLElement} optionElement - element to check
     * @return {boolean} true, if value of the element is lowert than 0, false otherwise
     */
    _hasNullValue: function(optionElement) {
        return optionElement.val() < 0;
    },

    /**
     * Builds then binds a JSON representation to optgroups and options
     *
     * @method _parseDescriptors
     * @private
     */
    _parseDescriptors: function () {

        var instance = this,
            items = this.$element.children();

        function parseOptGroup (optionGroup) {
            var properties = {
                label: optionGroup.attr("label"),
                footerText: optionGroup.attr("data-footer-text"),
                styleClass: optionGroup.prop("className"),
                model: optionGroup,
                items: retrieveAvailableOptions(optionGroup)
            };
            var weight = optionGroup.data('weight');
            if (weight) {
                properties.weight = +weight;
            }
            optionGroup.data("descriptor", new AJS.GroupDescriptor(properties));
        }

        function retrieveAvailableOptions (parent) {

            var availableOptionElems = AJS.$("option", parent),
                arr = [];

            AJS.$.each(availableOptionElems, function () {
                arr.push(instance._parseOption(this));
            });

            return arr;
        }

        items.each(function (i) {
            var item = items.eq(i);
            if (item.is("optgroup")) {
                parseOptGroup(item);
            } else if (item.is("option")) {
                instance._parseOption(item);
            }
        });
    },

    /**
     * Gets value
     *
     * @return {String, Array}0
     */
    getValue: function () {
        if (this.type === "single") {
            return this.$element.val() && this.$element.val()[0];
        } else {
            return this.$element.val();
        }
    },

    /**
     * Gets an array of JSON descriptors for selected options
     *
     * @method getDisplayableSelectedDescriptors
     * @return {Array} 
     */
    getDisplayableSelectedDescriptors: function () {
        var descriptors = [];
        this.$element.find("option:not(.free-input):selected").each(function () {
            descriptors.push(AJS.$.data(this, "descriptor"));
        });
        return descriptors;
    },

    /**
     * Gets an array of JSON descriptors for unselected options
     *
     * @method getDisplayableUnSelectedDescriptors
     * @return {Array}
     */
    getDisplayableUnSelectedDescriptors: function () {
        var descriptors = [];
        this.$element.find("option").not(":selected, .free-input").each(function () {
            descriptors.push(AJS.$.data(this, "descriptor"));
        });
        return descriptors;
    },

    /**
     * Gets an array of all descriptors, selected and unselected.
     *
     * @method getAllDescriptors
     * @param {boolean} showGroups - If set to false will not include group descriptors 
     *
     * @return {Array}
     */
    getAllDescriptors: function (showGroups) {

        var properties,
            descriptors = [];

        this.$element.children().each(function () {
            var descriptor,
                elem = AJS.$(this);
            if (elem.is("option")) {
                if (elem.data("descriptor")) {
                    descriptors.push(elem.data("descriptor"));
                }

            } else if (elem.is("optgroup")) {
                if (showGroups !== false) {

                    properties = AJS.copyObject(elem.data("descriptor").allProperties(), false);
                    properties.items = [];
                    descriptor = new AJS.GroupDescriptor(properties);
                    descriptors.push(descriptor);
                }
                elem.children("option").each(function () {
                    var elem = AJS.$(this);
                    var itemDescriptor = elem.data("descriptor");
                    if (itemDescriptor) {
                        if (showGroups !== false) {
                            descriptor.addItem(itemDescriptor);
                        } else {
                            descriptors.push(itemDescriptor);
                        }
                    }
                });
            }
        });
        
        return descriptors;
    },

    /**
     * Removes all unselected options
     *
     * @method clearUnSelected
     */
    clearUnSelected: function () {
        this.$element.find("option:not(:selected, .free-input)").remove();
    },

    /**
     * Gets an array of JSON descriptors for unselected options
     *
     * @method getUnSelectedDescriptors
     *
     * @param {Object} context - includes:
     * - {boolean} showGroups - If set to false will not include group descriptors
     * - {String} groupId - a group id to filter on - only return descriptors from this group
     *
     * @return {Array}
     */
    getUnSelectedDescriptors: function (context) {

        context = context || {};

        var descriptors = [],
            selectedValues = {},
            addedValues = {},
            ignoreGroups = context.ignoreGroups,
            groupId = context.groupId;

        // this function looks for options already selected by comparing values. NOTE: not case sensitive
        function isValid(descriptor) {
            var descriptorVal = descriptor.value().toLowerCase();
            if (!selectedValues[descriptorVal] && (!addedValues[descriptorVal] ||
                                                                    descriptor.allowDuplicate() !== false)) {
                addedValues[descriptorVal] = true;
                return true;
            }
            return false;
        }

        var selectedDescriptors = this.getDisplayableSelectedDescriptors();
        AJS.$.each(selectedDescriptors, function (i, descriptor) {
            selectedValues[descriptor.value().toLowerCase()] = true;
        });

        var selectChildren = this.$element.children();
        if (groupId) {
            selectChildren = selectChildren.filter('#' + groupId);
        }
        selectChildren.each(function () {
            var descriptor,
                properties,
                elem = AJS.$(this);
            if (elem.is("option") && !this.selected) {
                descriptor = AJS.$.data(this, "descriptor");
                if (isValid(descriptor)) {
                    descriptors.push(descriptor);
                }
            } else if (elem.is("optgroup")) {
                if (!ignoreGroups) {
                    properties = AJS.copyObject(elem.data("descriptor").allProperties(), false);
                    properties.items = [];
                    descriptor = new AJS.GroupDescriptor(properties);
                    descriptors.push(descriptor);
                }
                elem.find("option:not(.free-input)").each(function () {
                    if (this.selected) {
                        return;
                    }
                    var itemDescriptor = AJS.$.data(this, "descriptor");
                    if (isValid(itemDescriptor)) {
                        if (ignoreGroups) {
                            descriptors.push(itemDescriptor);
                        } else {
                            descriptor.addItem(itemDescriptor);
                        }
                    }
                });
            }
        });

        return descriptors;
    },

    _renders: {

        /**
         * Renders an option, built using descriptor.
         *
         * You call this method like this:
         *
         * <pre>
         * this.render("option", {...});
         * </pre>
         *
         * @method _renders.option
         * @private
         * @param {AJS.ItemDescriptor} descriptor
         * @return {jQuery}
         */
        option: function (descriptor) {

            var label = descriptor.label();
            var text = descriptor.fieldText() || label;
            var option  = new Option(text, descriptor.value());
            var $option = AJS.$(option);
            var iconUrl = descriptor.icon();

            option.title = descriptor.title();
            
            if (text != label) {
                // The displayed label might be more descriptive that the text entered in an input field.
                $option.data('field-label', label);
            }

            AJS.$.data(option, "descriptor", descriptor);

            descriptor.model($option);

            if (iconUrl) {
                $option.css("backgroundImage", "url(" + iconUrl + ")");
            }

            return $option;
        },

        /**
         * Renders an optgroup, built using descriptor.
         *
         * You call this method like this:
         *
         * <pre>
         * this.render("option", {...});
         * </pre>
         *
         * @method _renders.option
         * @param {AJS.GroupDescriptor} descriptor
         * @return {jQuery}
         */
        optgroup: function (descriptor) {

            var elem = AJS.$("<optgroup />")
                    .addClass(descriptor.styleClass())
                    .attr("label", descriptor.label());

            descriptor.model(elem);

            elem.data("descriptor", descriptor);

            if (descriptor.id()) {
                elem.attr("id", descriptor.id());
            }

            return elem;
        }
    }

});
