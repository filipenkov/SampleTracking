/**
 * A list item is an item used in a list.
 *
 * @constructor AJS.Dropdown.ListItem
 * @extends AJS.Control
 */
AJS.Dropdown.ListItem = AJS.Control.extend({

    init: function(options) {
        this._setOptions(options);

        this.$element = AJS.$(this.options.element);
        this.hasFocus = false;

        this._assignEvents("instance", this);
        this._assignEvents("element", this.$element);
    },

    _getDefaultOptions: function() {
        return {
            element: null,
            autoScroll: true,
            focusClass: AJS.ACTIVE_CLASS
        };
    },

    _events: {
        "instance": {
            "focus": function(event) {
                this.hasFocus = true;
                this.$element.addClass(this.options.focusClass);

                if (!event.noscrolling) {
                    AJS.Dropdown.ListItem.MOTION_DETECTOR.unbind();
                    this.isWaitingForMove = true;
                    if (this.options.autoScroll) {
                        this.$element.scrollIntoView(AJS.Dropdown.ListItem.SCROLL_INTO_VIEW_OPTIONS);
                    }
                }
            },
            "blur": function() {
                this.hasFocus = false;
                this.$element.removeClass(this.options.focusClass);
            },
            "accept": function() {
                var event = new jQuery.Event("click");
                var $target = this.$element.is("a[href]") ? this.$element : this.$element.find("a[href]");

                $target.trigger(event);

                if (!event.isDefaultPrevented()) {
                    window.top.location = $target.attr("href");
                }
            }
        },
        "element": {
            "mousemove": function() {
                if (((this.isWaitingForMove && AJS.Dropdown.ListItem.MOTION_DETECTOR.moved) && !this.hasFocus) 
                        || !this.hasFocus) {
                    this.isWaitingForMove = false;
                    this.trigger({
                        type: "focus",
                        noscrolling: true
                    });
                }
            }
        }
    }
});

AJS.Dropdown.ListItem.MOTION_DETECTOR = new JIRA.Mouse.MotionDetector();

AJS.Dropdown.ListItem.SCROLL_INTO_VIEW_OPTIONS = {
    duration: 100,
    callback: function() {
        AJS.Dropdown.ListItem.MOTION_DETECTOR.wait();
    }
};
