/**
 * Standard Gadget object. Gadgets using this constructor are unconfigurable and simply render a view.
 *
 * @class Standard
 * @constructor
 * @protected
 * @param {Object} options
 *
 * <ul>
 *      <li><strong>baseUrl:</strong> String that will prefix any remote request url</li>
 *
 *      <li><p><strong>useOauth:</strong> Used to determine what authorisation headers (if any) are required for
 *      requests. Using the provided url, gadget will request resource without oauth headers, if data
 *      comes back in the response, all subsequent requests will not use oauth headers. If response does not
 *      return data, subsequent requests will use oauth.
 *      <a href="https://extranet.atlassian.com/display/JIRADEV/OAuth+and+Gagdets+with+anonymous+access">Full
 *      implementation details available here</a>.</p>
 *
 *      <p>Alternatively you can provide the string "always" which will force all requests to use oauth. Please
 *      note this removes the ability to provide data to anonymous users (@AnonymousAllowed)</p></li>
 *
 *
 *      <li>
 *
 *          <p><strong>view:</strong></li> Used by templater to construct primary content (view). The view has parameter
 *          has a number of options, provided as a JSON object:</p>
 *
 *          <ul>
 *              <li>
 *                  <p><strong>{Function, Array} args</strong> - Either an array of objects or function that returns one.
 *                  The objects in this array have two keys: </p>
 *                  <ol>
 *                      <li>{String} key - name of key that will be used to access the data from within the template.</li>
 *                      <li>{Object} ajaxOptions - set of request options to retrieve data from remote resource. Options
 *                      are <a href="http://docs.jquery.com/Ajax/jQuery.ajax#options">available here</a></li>
 *                  </ol>
 *              </li>
 *
 *              <li>
 *                  <p><strong> {Function, String} template </strong> - The function or microtemplate that will be
 *                  executed to render the view (primary content).</p>
 *
 *                  <p>In the case of a function being provided, it will be called with a single argument. This argument is
 *                  a map (object) of the request responses defined in the args array. (See param args above)</p>
 *
 *                  <p>In the case of a string being provided, a &lt;script&gt;, with an ID that matches the provided
 *                  string is located. This script must have a type set to "text/html" also. The innerHTML of this script
 *                  is then used to generate a (x)HTML string using
 *                  <a href="http://ejohn.org/blog/javascript-micro-templating/">Resig's Micro-Templating.</a></p>
 *              </li>
 *          </ul>
 *      </li>
 *
 *      <li>
 *          <em>optional</em> <strong> {Boolean} onResizeReload </strong> - if browser window is resized, view
 *          will be reloaded. This is useful for graphical elements, such as charts where the graphic can be resized
 *          relative to the window.
 *      </li>
 *      <li>
 *          <em>optional</em> <strong> {Boolean} onResizeAdjustHeight </strong> - if browser window is resized, gadget.resize()
 *          will be called to make sure that the gadget height will cope with line wraps that may have occured.
 *      </li>
 * 
 * </ul>
 *
 */
var Standard = function (options) {

    // extend abstract Gadget class
    var gadget = Gadget(options);
    AJS.$.extend(gadget, {

        constructor: Standard,

        getView: function () {
            var view = AJS.$("<div />").addClass("view").appendTo(gadget.getGadget());
            return function () {
                return view;
            };
        }(),

        getFooter: function () {
            var footer = AJS.$("<div />").addClass("footer").appendTo(gadget.getGadget());
            AJS.$("body").addClass("has-footer-options");
            this.getFooter = function () {
                return footer;
            };
            return footer;
        },

        containWidthOverflow: function () {
            var bodyWidth = AJS.$("body").outerWidth();
            gadget.getView().css("overflowX", "");
            gadget.getView().css("width", "");
            if (bodyWidth < parseInt(AJS.$(".view").attr("scrollWidth"))) {
                gadget.getView().width(bodyWidth).css("overflowX", "scroll");
                gadget.resize();
            } else if (gadget.getView().css("overflowX") === "scroll" &&
                        parseInt(gadget.getView().attr("scrollWidth")) === gadget.getView().width()) {
                gadget.getView().css("overflowX", "");
                gadget.getView().css("width", "");
            }
        },

        executeAfterLoading: function (invocation) {
            if (AJS.$("body").hasClass("loading")) {
                gadget.hideLoading.callbacks = gadget.hideLoading.callbacks || [];
                gadget.hideLoading.callbacks.push(invocation.proceed);
            } else {
                invocation.proceed();
            }
        },

        /**
         * Object responsible for displaying primary gadget content
         *
         * @property viewTemplate
         * @type Object
         * @private
         */
        viewTemplate: AJS.gadgets.templater.Custom ({
            template: function(args) {
                if (gadgets.views) {
                    gadget.setViewMode(gadgets.views.getCurrentView().getName());
                }
                options.view.template.call(gadget, args);
                gadget.showView(false, false);

                // used for performance testing
                if (gadget.isAtlassianContainer()) {
                    window.top.AJS.$("body").trigger("gadget-rendering-finished");
                }
            },
            cache: false,
            // Loop through all the remote args if ajaxOptions is a function then ensure it is
            // called by the templater in the context of the gadget
            args: function() {
                var args = [];
                AJS.$(options.view.args).each(function(){
                    if (AJS.$.isFunction(this.ajaxOptions)) {
                        var that = this;
                        args.push({
                            key: this.key,
                            ajaxOptions: function (args) {
                                return that.ajaxOptions.call(gadget, args);
                            }
                        });
                    } else {
                        args.push(this);
                    }
                });
                return args;
            }()
        })
    });


    AJS.$.aop.around({target: gadget.viewTemplate, method: "addCallback"},  gadget.executeAfterLoading);
    AJS.$.aop.around({target: gadget, method: "resize"}, function (invocation) {
        if (!gadget.resize.pending) {
            gadget.resize.pending = true;
            gadget.executeAfterLoading.apply(this, arguments);
        }
    });
    
    /**
     * shows primary content
     *
     * @method showView
     * @param {Boolean} refresh - flag that specifies to reload content
     */
    gadget.showView = function () {
        gadget.viewTemplate.addCallback(function () {
            //JRA-19081: Who knows why, but for some reason Safari needs a bit of a timeout before resizing.
            window.setTimeout(function() {gadget.resize();}, 150);
        });
        if (AJS.$.browser.msie) {
            gadget.viewTemplate.addCallback(gadget.containWidthOverflow);
        }
        return function (refresh, resize) {
            if (refresh) {
                gadget.viewTemplate.build();
            } else {
                gadget.getView().show();
                if (resize !== false) {
                    gadget.resize();
                }
            }
        };
    }();

    if (AJS.$.browser.msie) {
        AJS.$(window).resize(gadget.containWidthOverflow);
    }

    if (options.view.onResizeReload) {
        AJS.$(window).resize(function () {
            var width = gadgets.window.getViewportDimensions().width;
            return function () {
                if (gadget.getView().is(":visible")) {
                    var newWidth = gadgets.window.getViewportDimensions().width;
                    if (width > (newWidth + 50) || width < (newWidth - 50)) {
                        gadget.showView(true);
                        width = newWidth;
                    }
                }
            };
        }());
    }

    if (options.view.onResizeAdjustHeight) {
        AJS.$(window).resize(function () {
            var width = gadgets.window.getViewportDimensions().width;
            return function () {
                var newWidth = gadgets.window.getViewportDimensions().width;
                if (width > (newWidth + 50) || width < (newWidth - 50)) {
                    gadget.resize();
                    width = newWidth;
                }
            };
        }());
    }

    return gadget;

};