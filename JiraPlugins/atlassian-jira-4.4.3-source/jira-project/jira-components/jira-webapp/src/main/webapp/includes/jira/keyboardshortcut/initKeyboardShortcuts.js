AJS.whenIType.fromJSON = function(json) {
    if (json) {
        // Blur field when ESC key is pressed.
        jQuery(document).bind("aui:keyup", function(event) {
            var $target = jQuery(event.target);
            if (event.key === "Esc" && $target.is(":input")) {
                $target.blur();
            }
        });

        jQuery.each(json, function() {
            // Flatten this.keys array.
            var keys = Function.prototype.call.apply(Array.prototype.concat, this.keys);
            var shortcut = keys.join("");
            if (keys.length < shortcut.length) {
                throw new Error("Shortcut sequence [" + keys.join(",") + "] contains invalid keys");
            }
            AJS.whenIType(shortcut)[this.op](this.param);
        });
    }
};

jQuery(function() {
    // AJS.keys is defined by the keyboard-shortcut plugin.
    if (AJS.keys) {
        AJS.whenIType.fromJSON(AJS.keys.shortcuts);
    }
});
