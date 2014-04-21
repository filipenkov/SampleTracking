/**
 *
 * @module Controls
 * @requires AJS, jQuery, jQuery.moveTo
 */

/**
 * Expands textareas upto a certain max-height, depending on the amount of content, on
 * calls to expandOnInput() and subsequent keypresses.
 *
 * Repeatedly calling expandOnInput() on the same DOM element is safe. 
 *
 * <pre>
 * <strong>Usage:</strong>
 * jQuery("textarea").expandOnInput();
 * </pre>
 *
 * @class expandOnInput
 * @constuctor expandOnInput
 * @namespace jQuery.fn
*/

(function () {
    var eventsToListenTo = "input keyup";

    jQuery.fn.expandOnInput = function(maxHeight) {
        var $textareas = this.filter('textarea');
        // Make sure we don't bind duplicate event handlers.
        $textareas.unbind(eventsToListenTo, setHeight).bind(eventsToListenTo, setHeight);

        //FF3.0 is especially precious when pasting into the stalker comment box. For some reason
        //it doesnt' resize on the paste rightaway.
        // Additionally, IE and FF don't scroll all the way to the bottom when a textarea got overflow
        //hidden so this scrolls to the bottom as well.  It's not perfect since it will not be right
        //if the user's pasting in the middle of some text. HTFU!!
        if(AJS.$.browser.mozilla || AJS.$.browser.msie) {
            $textareas.unbind("paste", triggerKeyup).bind("paste", triggerKeyup);
        }


        // TODO I don't think anything broadcasts this event.
        $textareas.unbind("refreshInputHeight").bind("refreshInputHeight", function () {
            setHeight.call(AJS.$(this).css("height", ""));
        });

        $textareas.data("expandOnInput_maxHeight", maxHeight);

        $textareas.each(function () {

            var $this = AJS.$(this);

            $this.each(function () {
                var $this = AJS.$(this);
                $this.data("hasFixedParent", $this.hasFixedParent());
            });

            // Respect initial heights for empty textareas.
            if (AJS.$(this).val() !== '') {
                setHeight.call(this);
            }
        });
        return this;
    };

    function triggerKeyup() {
        var $textarea = AJS.$(this), textarea = this;
        setTimeout(function() {
            $textarea.keyup();
            textarea.scrollTop = textarea.scrollHeight;
        }, 0);
    }

    function setHeight() {
        var $textarea = AJS.$(this),
            height    = parseInt($textarea.css("height"), 10) || $textarea.height(),
            padding   = $textarea.attr("clientHeight") - height;

        // Workaround for IE not giving an accurate value for scrollHeight.
        // http://www.atalasoft.com/cs/blogs/davidcilley/archive/2009/06/23/internet-explorer-textarea-scrollheight-bug.aspx
        this.scrollHeight;

        var maxHeight = parseInt($textarea.css("maxHeight"), 10) || $textarea.data("expandOnInput_maxHeight") || AJS.$(window).height() - 160,
            newHeight = Math.max(height, this.scrollHeight - padding);

        if (newHeight < maxHeight) {
            $textarea.css({
                "overflow": "hidden",
                "height": newHeight + "px"
            });
        } else {
            var cursorPosition = this.selectionStart;
            $textarea.css({
                "overflow-y": "auto",
                "height": maxHeight + "px"
            });
            if (AJS.$.browser.msie && AJS.$.browser.version <= 7) {
                //trigger a reflow otherwise the textarea may be covering stuff below it!
                setTimeout(function() {$textarea.css({"zoom":"1"})}, 0);
            }
            $textarea.unbind(eventsToListenTo, setHeight);
            $textarea.unbind("paste", triggerKeyup);
            if (this.selectionStart !== cursorPosition) {
                this.selectionStart = cursorPosition;
                this.selectionEnd   = cursorPosition;
            }
            newHeight = maxHeight;
        }

        if (!$textarea.data("hasFixedParent")) {
            var $window = AJS.$(window),
                scrollTop = $window.scrollTop(),
                minScrollTop = $textarea.offset().top + newHeight - $window.height() + 29;

            if (scrollTop < minScrollTop) {
                $window.scrollTop(minScrollTop);
            }
        }

        $textarea.trigger("stalkerHeightUpdated");
    }
})();
