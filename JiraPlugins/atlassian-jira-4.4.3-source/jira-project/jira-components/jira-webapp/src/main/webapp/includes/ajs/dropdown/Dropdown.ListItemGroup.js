/**
 * A list item group has key handling for shifting focus with the vertical arrow keys,
 * and accepting an item with the return key.
 *
 * @constructor AJS.Dropdown.ListItemGroup
 * @extends AJS.Group
 */
AJS.Dropdown.ListItemGroup = AJS.Group.extend({
    keys: {
        "Up": function(event) {
            this.shiftFocus(-1);
            event.preventDefault();
        },
        "Down": function(event) {
            this.shiftFocus(1);
            event.preventDefault();
        },
        "Return": function(event) {
            this.items[this.index].trigger("accept");
            event.preventDefault();
        }
    }
});
