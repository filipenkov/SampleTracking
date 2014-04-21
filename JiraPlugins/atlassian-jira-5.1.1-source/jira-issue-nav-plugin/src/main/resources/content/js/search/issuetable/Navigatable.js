AJS.namespace("JIRA.Issues.Mixin.Navigatable");

/*
 * Calls `next()` on model when _J_ is pressed.
 * Calls `prev()` on model when _K_ is pressed.
 */
JIRA.Issues.Mixin.Navigatable = {

    initialize: function () {

        // ref to real renderer
        var oldRender = this.render;

        // We wrap the real render as we want to apply the shortcuts after we render
        this.render = function () {

            // perform real render and store return value
            var ret = oldRender.apply(this, arguments);

            // unbind any previous j/k shortcuts - we can only have one active at a time
            AJS.$(document).unbind("shortcut");

            // bind j shortcut
            AJS.whenIType("j").execute(function () {
                this.model.next();
            }.bind(this));

            // bind k shortcut
            AJS.whenIType("k").execute(function () {
                this.model.prev();
            }.bind(this));

            // return value from real renderer
            return ret;
        }
    }
};