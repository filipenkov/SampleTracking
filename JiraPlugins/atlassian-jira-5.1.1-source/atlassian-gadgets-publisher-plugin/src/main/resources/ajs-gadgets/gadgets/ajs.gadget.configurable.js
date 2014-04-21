/**
 * Standard Gadget object. This is constuctor is used when a gadget has previously been configured, but now locked
 * (read only).
 *
 * @class Configurable
 * @private
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
 *      <li>
 *
 *          <p><strong>config:</strong></li> Used by templater to construct the configuration form. The
 *          config parameter has a number of options, provided as a JSON object:</p>
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
 *              <li><Strong>{Function} descriptor</strong> - specifies the fields you would like to display for the
 *              user to set preferences. The structure of the form is described using a descriptor. The descriptor
 *              must be a function and return an object with valid form values. Valid form values are yet to be
 *              documented.</li>
 *          </ul>
 *      </li>
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
 *          <em>optional</em> <strong> {Boolean} enableReload </strong> - if true will add a list of reload
 *          intervals to the configuration form. When gadget loads timer will initiate, when interval is hit, view
 *          will be reloaded
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
var Configurable =  function (options) {

    var gadget = Configured(options);

    if(AJS.debug) {
        if (options.view.enableReload) {
            AJS.$(["refresh.never","refresh.minutes","refresh.hour","refresh.hours","refresh.description",
                "refresh.label"]).each(function(){
                if (gadget.getPrefs().getMsg("gadget.common." + this) === "") {
                    console.warn("il8n key missing:\"" + "gadget.common." + this + "\"");
                }
            });
        }
    }

    AJS.$.extend(gadget, {

        constructor: Configurable,

        /**
         * Object responsible for building configuration form & saving preferences
         *
         * @property configTemplate
         * @type Object
         * @private
         */
        configTemplate: AJS.gadgets.templater.Form ({

            // target of html injection
            target: AJS.$("<div id='config' />").appendTo(document.body),

            // proxy to user defined descriptor to ensure descriptor has gadget context
            // we can also append additional configuration options, not set by user, to config form.
            descriptor: function (args) {
                var descriptor = options.config.descriptor.call(gadget, args);
                if (options.view.enableReload) {
                    descriptor.fields.push({
                        userpref: "refresh",
                        label: gadget.getMsg("gadget.common.refresh.label"),
                        description: gadget.getMsg("gadget.common.refresh.description"),
                        selected: gadget.getPref("refresh"),
                        options: [
                            {value: "false", label: gadget.getMsg("gadget.common.refresh.never")},
                            {value: "15", label: AJS.format(gadget.getMsg("gadget.common.refresh.minutes"), 15)},
                            {value: "30", label: AJS.format(gadget.getMsg("gadget.common.refresh.minutes"), 30)},
                            {value: "60", label: AJS.format(gadget.getMsg("gadget.common.refresh.hour"), 1)},
                            {value: "120", label: AJS.format(gadget.getMsg("gadget.common.refresh.hours"), 2)}
                        ],
                        type: "select"
                    });
                }
                return descriptor;
            },

            // Loop through all the remote args if ajaxOptions is a function then ensure it is
            // called by the templater in the context of the gadget
            args: function() {
                var args = [];

                // we need to make requests to trigger oauth request, so if config has no args we are going to fire
                // requests for the view.

                if (AJS.$.isFunction(options.config.args)) {
                    options.config.args = options.config.args();
                }

                AJS.$(options.config.args).each(function() {
                    var that = this;
                    if (AJS.$.isFunction(this.ajaxOptions)) {
                        args.push({key: this.key, ajaxOptions: function (args) {
                            return that.ajaxOptions.call(gadget, args);
                        }});
                    } else {
                        args.push(this);
                    }
                });

                return args;
            }(),

            // Method called when the cancel button on the form is clicked
            cancel: function (e) {
                gadget.showView();
                e.preventDefault();
            },

            // Method called when form is submitted
            submit: function (e) {
                var form = AJS.$(this),
                save = function () {
                    var fieldValues = {};

                    // save form configuration to user prefs
                    AJS.$(form.serializeArray()).each(function(){
                        var fieldVal = fieldValues[this.name];
                        if (!fieldVal) {
                            fieldVal = this.value;
                        } else if (AJS.$.isArray(fieldVal)) {
                            fieldVal.push(this.value);
                        } else {
                            fieldVal = [fieldVal, this.value];
                        }
                        fieldValues[this.name] = fieldVal;
                    });

                    AJS.$(":checkbox:not(:checked)", form).each(function () {
                        if (!fieldValues[this.name] || fieldValues[this.name] == "") {
                            fieldValues[this.name] = "false";
                        }
                    });

                    for (var name in fieldValues) {
                        gadget.savePref(name, fieldValues[name]);
                    }

                    // hide form
                    gadget.showView(true);
                };
                if (form.attr("action") !== "") {
                    AJS.$.ajax({
                        type: "get",
                        url: form.attr("action"),
                        data: form.serialize(),
                        global: false, // we don't want the global error handler coming into play

                        // if the configuration passed server side validation
                        success: function () {
                            save();
                        },
                        error: function (response, settings) {
                            gadget.ajaxErrorHandler.execute(null, response, AJS.$.extend(settings, {isConfig: true}));
                        }
                    });
                } else {
                    save();
                }
                e.preventDefault();
            }
        }),


        /**
         * Gets config object, wrapper div for config form (jQuery Object)
         *
         * @method getConfig
         * @return {Object} jQuery object
         */
        getConfig: function () {
            return gadget.configTemplate.getTarget();
        }
    });

    /**
     * shows configuration
     *
     * @method showConfig
     */
    gadget.showConfig = function () {

         var restoreValuesFromHash = function () {

            var options, fields;

            /* If preselected form fields have been serialized to the end of the hash */
            if (/#/.test(window.location.href) && !/#rpctoken=[0-9]+$/.test(window.location.href)) {
                fields = window.location.href.replace(/.*?#/,"").split("&");
                AJS.$.each(fields, function () {
                    var name = this.split("=")[0], val = decodeURIComponent(this.split("=")[1]).replace(/\+/, " "), field = AJS.$("#config input[name=" + name + "]");
                    if (field.length > 0) {
                        if (field.is("select")) {
                            options = val.split(",");
                            AJS.$.each(options, function () {
                                AJS.$("option[value=" + this + "]", field).attr("selected", "selected");
                            });
                        } else if (field.is(":radio,:checkbox")) {
                            field.attr("checked", "checked");
                        } else {
                            field.val(val);
                        }
                    }
                });
                window.location.href = window.location.href.replace(/(#rpctoken=[0-9]+).*/,"$1");
            }
        };

        gadget.configTemplate.addCallback(function(){

            AG.sysMsg.clear(); // Clear all system messages

            AJS.$("body").addClass("config-active");
            gadget.configTemplate.getTarget().show();
            // Restores the values of a partly completed form. This happens in the case of an oauth approval, the
            // iframe is refreshed. Before we refresh though we save the value to the hash to restore, which we are doing now.
            restoreValuesFromHash();

            AJS.$("button", ".footer").hide();
            gadget.resize();

            //IE requires this to force a re-flow of the page elements that were dynamically inserted.
            if(AJS.$.browser.msie) {
                AJS.$("body").addClass("ie-reflow");
            }

            // used for performance testing
            if (gadget.isAtlassianContainer()) {
                window.top.AJS.$("body").trigger("gadget-rendering-finished");
            }
        });

        return function () {
            gadget.configTemplate.build();
        };
    }();

    /**
     * Hides configuration
     *
     * @method hideConfig
     */
    gadget.hideConfig = function () {
        AJS.$("body").removeClass("config-active");
        gadget.configTemplate.getTarget().hide();
    };

    /**
     * shows primary content
     *
     * @method showView
     * @param {Boolean} refresh - flag that specifies to reload content
     * @override
     */
    gadget.showView = function (refresh, resize) {
        if (refresh) {
            gadget.viewTemplate.build();
        } else {
            gadget.hideConfig();
            if (resize !== false) {
                gadget.resize();
            }
            AJS.$("button", ".footer").show();
        }
        gadget.addMenuItem("configure", gadget.getMsg("gadget.common.configure"), function (e) {
            gadget.showConfig();
            e.preventDefault();
        });
        if (gadget.addReloadButton) {
            gadget.addReloadButton();
        }
    };

    /**
     * Handles validation errors
     *
     * @method ajaxErrorHandler.handle500
     * @private
     */
    gadget.ajaxErrorHandler.handle400 = function (superHandle400) {

        var validationErrors,

        showFormErrors = function () {
            AJS.$(".error", gadget.configTemplate.getTarget()).hide();
            AJS.$(validationErrors).each(function(){
                var msg = gadget.getMsg(this.error, this.params);
                AJS.$(":input[name='" + this.field + "']")
                .parent()
                .find(".error")
                .text(msg)
                .show();
                AJS.$("#" + this.field + "-error").text(msg).show();
            });
            gadget.resize();
            validationErrors = null;
        };


        gadget.configTemplate.addCallback(function () {
            if (!gadget.getPrefs().getBool("isConfigured") || validationErrors) {
                AJS.$("input.cancel", gadget.configTemplate.getTarget()).hide();
            }
            if (validationErrors) {
                showFormErrors();
            }
            gadget.removeMenuItem("configure");
            gadget.removeMenuItem("reload");
        });

        return function (evt, data, settings) {


            validationErrors = data.errors;
            if (!settings.isConfig) {
                superHandle400.apply(gadget, arguments);
            } else {
                showFormErrors();
            }
        };

    }(gadget.ajaxErrorHandler.handle400);

    // we show a login or configure app link message here so should hide configuration
    jQuery(document).bind("ajax.anonymousAccessDenied", function () {
        gadget.hideConfig();
    });

    return gadget;
};