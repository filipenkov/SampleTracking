/**
 * A group manages focus for a list of items so that only one item has focus at a time.
 *
 * @constructor AJS.Group
 * @extends AJS.Control
 */
AJS.Group = AJS.Control.extend({

	init: function() {
		this.items = [];
		this.index = -1;
        this._assignEvents("instance", this);
	},

    /**
     * Add an item to this group.
     *
     * @method addItem
     * @param {AJS.Control} item
     */
	addItem: function(item) {
		this.items.push(item);
		this._assignEvents("item", item);
	},

    /**
     * Remove an item from this group.
     * Note: This does not remove the UI lozenge.
     *
     * @method _removeItem
     * @param {AJS.Control} item
     */
	_removeItem: function(item) {
        var index = AJS.$.inArray(item, this.items);
        if (index < 0) {
            throw new Error("AJS.Group: item [" + item + "] is not a member of this group");
        }
        item.trigger("blur");
        if (index < this.index) {
            this.index--;
        }
        this.items.splice(index, 1);
        this._unassignEvents("item", item);
	},

    /**
     * Remove an item from this group. It also removes the UI lozenge.
     *
     * @method removeItem
     * @param {AJS.Control} item
     */
    removeItem: function(item) {
        item.trigger("remove")
    },

    /**
     * Remove all items from this group. It also removes the UI lozenges.
     *
     * @method removeAllItems
     */
    removeAllItems: function() {
        while (this.items.length) {
            this.items[0].trigger("remove");
        }
    },

    /**
     * Move focus to a new item, relative to the currently focused item.
     *
     * @method shiftFocus
     * @param {Number} offset -- The position of the item to focus, relative to the position of the currently focused item.
     */
	shiftFocus: function(offset) {
        if (this.index === -1 && offset === 1) {
            offset = 0;
        }
		if (this.items.length > 0) {
            var i = (Math.max(0, this.index) + this.items.length + offset) % this.items.length;
			this.items[i].trigger("focus");
		}
	},

    /**
     * Assigns events so that (ie in the case of a dropdown, if no items are focused that key down will focus first time)
     * @method prepareForInput
     *
     */
    prepareForInput: function () {
        this._assignEvents("keys", document);
    },

	_events: {
        "instance": {
            "focus": function() {
                if (this.items.length === 0) {
                    return;
                }
                if (this.index < 0) {
                    this.items[0].trigger("focus");
                } else {
                    this._assignEvents("keys", document);
                }
            },
            "blur": function() {
                if (this.index >= 0) {
                    this.items[this.index].trigger("blur");
                } else {
                    this._unassignEvents("keys", document);
                }
            }
        },
		"keys": {
			"aui:keydown aui:keypress": function(event) {
				this._handleKeyEvent(event);
			}
		},
		"item": {
			"focus": function(event) {
                var index = this.index;
                this.index = AJS.$.inArray(event.target, this.items);
                if (index < 0) {
                    this.trigger("focus");
                } else if (index !== this.index) {
                    this.items[index].trigger("blur");
                }
			},
			"blur": function(event) {
				if (this.index === AJS.$.inArray(event.target, this.items)) {
					this.index = -1;
                    this.trigger("blur");
				}
			},
			"remove": function(event) {
                this._removeItem(event.target);
			}
		}
	},

	keys: {
		// Key handlers may be added by descendant classes.
	}
});
