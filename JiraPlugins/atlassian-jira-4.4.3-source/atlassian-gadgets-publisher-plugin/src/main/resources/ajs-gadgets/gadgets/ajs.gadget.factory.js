/**
 * Standard Gadget object. This is constuctor is used when a gadget has previously been configured, but now locked
 * (read only).
 *
 * @class AJS.Gadget
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
 *
 *          <p><em>optional</em> <strong>{Object} config:</strong></li> Used by templater to construct the configuration form. The
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
AJS.Gadget = function (options) {

    var gadget, pubInterface,

    /**
     * Detects if the user is gadget owner and has permissions to configure. If an external container writable
     * param will be undefined and we default to allow permissions.
     *
     * @method isWriteable
     * @private
     * @return {Boolean} if user has permissions to configure.
     */
    isWriteable = function () {
        if (gadgets.views) {
            return gadgets.views.getParams().writable === undefined || gadgets.views.getParams().writable === "true";
        }
        else if(AJS.debug) {
            throw "@method isWriteable: \nExpected Object 'gadgets.views', however it was undefined. To get this object "
                    + "you need to include to <Require feature='views' /> to <ModulePrefs> in your gadget XML";
        }
    },

    getStandardInterface = function () {
        return {
            showMessage: gadget.showMessage,
            savePref: gadget.savePref,
            setViewMode: gadget.setViewMode,
            getViewMode: gadget.getViewMode,
            getBaseUrl: gadget.getBaseUrl, 
            getPrefs: gadget.getPrefs,
            getPref: gadget.getPref,
            getPrefArray: gadget.getPrefArray,
            getMsg: gadget.getMsg,
            getGadget: gadget.getGadget,
            resize: gadget.resize,
            showLoading: gadget.showLoading,
            hideLoading: gadget.hideLoading,
            createCookie: gadget.createCookie,
            readCookie: gadget.readCookie,
            eraseCookie: gadget.eraseCookie,
            isLocal: gadget.isLocal,
            addMenuItem: gadget.addMenuItem,
            removeMenuItem: gadget.removeMenuItem
        };
    },

    getConfiguredInterface = function () {
        return AJS.$.extend(getStandardInterface(), {
            getView: gadget.getView,
            showView: gadget.showView
        });
    },

    getConfigurableInterface = function () {
        return AJS.$.extend(getConfiguredInterface(), {
            showConfig: gadget.showConfig,
            getConfig: gadget.getConfig
        });
    };
    if (options.view && !options.config) {
        gadget = Standard(options);
        pubInterface = getStandardInterface();
        gadget.showView(true);
    } else if (options.view && options.config && !isWriteable()) {
		if(AJS.debug) {
            console.log("@constructor AJS.Gadget: \nConfiguration form has been excluded, user is not gadget owner and does "
                    + "NOT have write permission");
        }
		gadget = Configured(options);
        pubInterface = getConfiguredInterface();
        if (gadget.getPrefs().getBool("isConfigured")) {
            gadget.showView(true);
        }



    } else if (options.view && options.config && isWriteable()) {
        gadget = Configurable(options);
        pubInterface = getConfigurableInterface();
        if (!gadget.getPrefs().getBool("isConfigured")) {
            gadget.showConfig();
        } else {
            gadget.showView(true);
        }
    }

    return pubInterface;
};

// support legacy constuctor
AJS.gadget.Configurable = AJS.Gadget;
