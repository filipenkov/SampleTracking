/**
 * The group descriptor is used in @see AJS.QueryableDropdownSelect to define characteristics and display
 * of groups of items added to suggestions dropdown and in the case of AJS.QueryableDropdownSelect and
 * AJS.SelectModel also.
 *
 * @constructor AJS.GroupDescriptor
 * @extends AJS.Descriptor
 */
AJS.GroupDescriptor = AJS.Descriptor.extend({

    /**
     * Defines default properties
     *
     * @method _getDefaultOptions
     * @return {Object};
     */
    _getDefaultOptions: function () {
        return {
            showLabel: true,
            label: "",
            items: []
        };
    },

    /**
     * Gets styleClass, in the case of @see AJS.QueryableDropdownSelect these are the classNames that will be applied to the
     * &lt;div&gt; surrounding a group of suggestions.
     *
     * @method styleClass
     * @return {String} 
     */
    styleClass: function () {
        return this.properties.styleClass;
    },

    /**
     * Gets weight, in the case of @see AJS.QueryableDropdownSelect this defines the order in which the group is appended in
     * the &lt;optgroup&gt; and as a result displayed in the suggestions.
     *
     * @method weight
     * @return {Number}
     */
    weight: function () {
        return this.properties.weight;
    },

    /**
     * Gets label, in the case of @see AJS.QueryableDropdownSelect this is the heading that is displayed in the suggestions
     *
     * @method label
     * @return {String}
     */
    label: function () {
        return this.properties.label;
    },

    /**
     * Unselectable Li appended to bottom of list
     * @return {String}
     */
    footerText: function () {
        return this.properties.footerText;
    },

    /**
     * Determines if the label should be shown or not, in the case of @see AJS.QueryableDropdownSelect this is used when we have
     * a suggestion that mirrors that of the user input. It sits in a seperate group but we do not want a heading for it. 
     *
     * @method showLabel
     * @return {Boolean}
     */
    showLabel: function () {
        return this.properties.showLabel;
    },

    /**
     * Gets items, in the case of @see AJS.QueryableDropdownSelect and subclasses these are instances of @see AJS.ItemDescriptor.
     * These items are used to describe the elements built as &lt;option&gt;'s in @see AJS.SelectModel and suggestion
     * items built in @see AJS.List
     *
     * @method items
     * @return {AJS.ItemDescriptor[]}
     */
    items: function () {
        return this.properties.items;  
    },

    /**
     * Adds item to the items array.
     *
     * @method addItem
     * @param {AJS.ItemDescriptor} item
     */
    addItem: function (item) {
        this.properties.items.push(item);
    },

     /**
     * Gets a unique id
     *
     * @method addItem
     * @param {AJS.ItemDescriptor} item
     */
    id: function () {
        return this.properties.id;
    },


    /**
     * Sets model, in the
     *
     * @param {jQuery} $model
     */
    setModel: function ($model) {
        this.properties.model = $model;
    },


    replace: function () {
        return this.properties.replace;  
    },

    /**
     * Defines a scope within which items in this Group must be unique; allowed values are:
     *
     * - 'group':     (default) the item must be unique in this group
     * - 'container': the item must be unique in this Group *and* its container
     * - 'none':      the item does not need to be unique.
     *
     * The setting here may be overridden by the ItemDescriptor's 'allowDuplicate' property.
     */
    uniqueItemScope: function () {
        return this.properties.uniqueItemScope;
    },


    description: function () {
        return this.properties.description;  
    },

    /**
     * Gets or sets model, in the case of @see AJS.SelectModel gets jQuery wrapped &lt;optgroup&gt; element
     *
     * @method model
     *
     * @return {jQuery}
     */
    model: function ($model) {
        if ($model) {
            this.properties.model = $model;
        } else {
            return this.properties.model;
        }
    }
});
