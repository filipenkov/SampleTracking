/**
 * <p>Singleton that provides simple get and set mechanism for param strings. Has support for
 * <a href="http://java.sun.com/j2se/1.4.2/docs/api/java/text/MessageFormat.html">Java's MessageFormat</a> syle.</p>
 *
 * <p><strong>Note:</strong> this is a replacement for AJS.params because it is leaner and faster then getting strings
 * form html document. It should <strong>ONLY</strong> be used for <strong>SAFE STRINGS</strong></p>
 *
 * @module dashboard
 * @class param
 * @static
 * @namespace AG
 */

/*global console, AJS*/
/*jslint bitwise: true, eqeqeq: true, immed: true, newcap: true, nomen: true, onevar: true, plusplus: true, regexp: true, undef: true, white: true, indent: 4 */

if (typeof AG === "undefined") {
    var AG = {};
}
if (!console.warn) {
    console.warn = function () {};
}

AG.param = (function () {

    /**
     * Map containing param strings
     * @property strs
     * @private
     * @type Object
     */
    var strs = {};

    return {

        /**
         * Gets param string of provided key, and if necessary substitutes params using Javas MessageFormat
         *
         * <dl>
         *  <dt>Usage</dt>
         *  <dd>
         *      <pre>AG.param.get ("dashboard.add.message", "simple", "test");</pre>
         *  </dd>
         * </dl>
         *
         * @method get
         * @param {String} key of param string
         * @param {String} arg replacement value for token 0, with subsequent arguments being 1, etc.
         * @return {String} the param string with tokens replaced with supplied arguments
         */
        get: function (key) {
            var args = arguments;
            if (strs[key]) {
                if (arguments.length > 1) {
                    args[0] = strs[key];
                    return AJS.format.apply(this, args);
                } else {
                    return strs[key];
                }
            }

            else {
                if(AJS.debug) {
                    console.warn("param.get: Key '" + key + "' does not exist.");
                }
                return null;
            }
        },

        /**
         * Sets param strings in bulk or individually
         *
         * <dl>
         *  <dt>Usage</dt>
         *
         * <dd>
         *  <pre>
         *   // bulk
         *   AG.param.set ([
         *      {
         *          key: "help.question",
         *          str: "I need help with my dashboard jim"
         *      },
         *      {
         *          key: "help.response",
         *          str: "No worries, {0}
         *      }
         *   ]);
         * </pre>
         * </dd>
         *
         * <dd>
         * <pre>
         *   // individual
         *   AG.param.set({
         *      key: "help.question",
         *      str: "I need help with my dashboard Jim"
         *   });
         * </pre>
         * </dd>
         * </dl>
         *
         *
         * @method set
         * @param {Object, Array} param An object with keys <em>key</em> & <em>str</em> and their associated values,
         * or an array of those objects.
         */
        set: function (key, str) {
            var setParam = function () {
                var k = arguments[0];
                var v = arguments[1];
                if (typeof k === "string") {
                    strs[k] = v;
                }
                else if(AJS.debug) {
                    console.warn("param.set: Ignored param.set, key may be undefined. " +
                            "Printing value...");
                    console.log(k);
                }
            };
            if (arguments.length === 1 && typeof arguments[0] === "object") {
                AJS.$.each(arguments[0], function (key, str) {
                    setParam(key, str);
                });
            } else if (arguments.length === 2) {
                setParam(arguments[0], arguments[1]);
            }
            else if(AJS.debug) {
                console.warn("param.set: Expected arguments to be of length 1 or 2, however recieved a length of " + 
                             arguments.length + ". Printing value...");
                console.log(arguments);
            }
        },

        clear: function () {
            if(AJS.debug) {
              strs = {};
            }
        }
    };

}());