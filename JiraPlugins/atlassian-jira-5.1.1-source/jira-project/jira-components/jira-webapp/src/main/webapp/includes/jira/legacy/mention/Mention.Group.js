/**
 * @deprecated
 */
AJS.LegacyMentionGroup = AJS.Group.extend({
    keys: {
        "Up": function(e) {
            this.shiftFocus(-1);
            e.preventDefault();
        },
        "Down": function(e) {
            this.shiftFocus(1);
            e.preventDefault();
        },
        "Return": function(e) {
            if (this.items && this.items[this.index]) {
                this.items[this.index].trigger("accept");
                e.preventDefault();
            }
        }
    }
});