/**
 * Atlassian utils for use in qunit tests.
 * <p>
 * Depends on AJS.log, either the actual or mocked version.
 * TODO: Duplicated from selenium-test. We need to make this available in a common module.
 */
(function($) {
QUtil = {

    /**
     * Freezes the qunit testing until condition is met.
     *
     * @param options contain the following:
     *    condition condition to wait until true. Must be a function
     *    doAfterReady function to execute after condition is true. Must be a function.
     *    doAfterTimeout function to execute after a timeout
     *    maxWait maximum time to wait. default 500ms
     *    pollPeriod time between condition checks. default 50ms
     */
    waitForCondition : function(options) {
        options = jQuery.extend({
            maxWait: 500,
            pollPeriod: 50,
            doAfterTimeout: function() {
                ok(false, "waitForCondition timed out with no doAfterTimeout function to call");
            },
            doAfterReady: function() {},
            condition: function(){
		        ok(false, "no condition passed");
	            return true;  // don't wait for timeout (still runs doAfterReady)
	        }
        }, options);

        var intervalId,
            startTime = new Date();

        function intervalTrigger() {
            var ready = options.condition();
            var timeout = !ready && (((new Date()) - startTime) > options.maxWait);
            if (ready || timeout) {
                start();
                clearInterval(intervalId);
                ready && options.doAfterReady();
                timeout && options.doAfterTimeout();
            }
        }
        stop();
        intervalId = setInterval(intervalTrigger, options.pollPeriod);
    },

    /**
     * Waits until an element matching the selector is visible.
     * @param selector a jQuery selector
     * @param onSuccess function to run when the selector element is visible
     */
    waitUntilVisible: function (selector, onSuccess) {
        QUtil.waitForCondition({
            condition: function() {
                return !!$(selector + ':visible').length;
            },
            doAfterReady: function() {
                ok(true, 'Element with selector "'+ selector + '" appeared');
                onSuccess();
            },
            doAfterTimeout: function() {
                // Should not occur
                ok(false, 'Timed out waiting for the element with selector "'+ selector + '" to appear.');
                start();
            },
            maxWait: 5000
        });
    },

    /**
     * Cross-browser check to see if a color is transparent
     * @param color
     */
    isTransparent : function (color) {
        return color.match(/transparent|rgba\(0,\s*0,\s*0,\s*0\)/i);
    },

    /**
     * Converts a color into a 6 digit hex representation
     * @param color
     */
    convertToHex : function(color) {
        var toHex = function(num) {
            var hex = parseInt(num).toString(16);
            if(hex.length < 2) {
                hex = '0' + hex;
            }
            return hex;
        },
            hex = null;
        if(color.charAt(0) == '#') {
            // IE
            if(color.length == 4) {
                hex = color.charAt(1) + color.charAt(1)
                    + color.charAt(2) + color.charAt(2)
                    + color.charAt(3) + color.charAt(3);
            } else if(color.length == 7) {
                hex = color.substring(1);
            } else {
                hex = null;
            }
        } else {
            // Everything else
            var rgbRegex = /rgb\((\d+),\s*(\d+),\s*(\d+)\)/i
            var match = rgbRegex.exec(color);
            if(match && match.length == 4) {
                hex = toHex(match[1])
                    + toHex(match[2])
                    + toHex(match[3]);
            }
        }
        return hex && hex.toLowerCase();
    },

    /**
     * Compares the html of an element with the expected string
     * This is required to normalise the output since IE likes to
     * insert new lines and capitalise elements.
     * @param el - the element
     * @param expected - the expected html
     */
    htmlEquals : function (el, expected) {
        var $el = el.jquery ? el : $(el),
            parent = $el.parent();
        if (!parent.length)
            parent = $("<div>").append($el);

        equals(parent.html(), $("<div>").html($.trim(expected)).html());
    },

    /**
     * Tests if a string contains an expected substring.
     */
    contains: function (haystack, needle, message) {
        ok(haystack && needle && haystack.indexOf(needle) > -1, message);
    },

    getOuterHTML : function(node) {
    // if IE, Chrome take the internal method otherwise build one
    return node.outerHTML || (
        function(n){
          var div = document.createElement('div'), h;
          div.appendChild( n.cloneNode(true) );
          h = div.innerHTML;
          div = null;
          return h;
        })(node);
    }
};
}(AJS.$));
