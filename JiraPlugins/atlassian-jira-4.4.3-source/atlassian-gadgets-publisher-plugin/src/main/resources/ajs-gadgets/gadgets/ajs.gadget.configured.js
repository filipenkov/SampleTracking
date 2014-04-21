/**
 * Standard Gadget object. This is constuctor is used when a gadget has previously been configured, but now locked
 * (read only).
 *
 * @class Configured
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
var Configured = function (options) {

    var gadget = Standard(options);

    gadget.constructor = Configured;

    if(AJS.debug) {
        if (gadget.getPref("isConfigured") === "") {
            throw "@constructor Configured: \nGadget is spec'ed as being configured or configurable. Add "
            + "<UserPref name=\"isConfigured\" datatype=\"hidden\" default_value=\"false\" /> to <ModulePrefs> in your "
            + "gadget XML.";
        }
    }

    /* validation errors */
    gadget.ajaxErrorHandler.handle400 = function (evt, data) {

        /* stop double append */
        if (AJS.$("body").hasClass("config-unavailable")) {
            return;
        }

        var
        errorContainer = AJS.$("<div class='message info' />")
                .append("<p>" + gadget.getMsg("gadget.common.config.unavailable") + "</p>")
                .appendTo(gadget.getView()),

        errorList = AJS.$("<ul />")
                .appendTo(errorContainer);

        AJS.$("body").addClass("config-unavailable");

        AJS.$(data.errors).each(function(){
            AJS.$("<li />").append("<strong>" + gadget.getMsg(this.error, this.params) + "</strong>").appendTo(errorList);
        });

        gadget.resize();
    };

    if (options.view.enableReload) {

        if(AJS.debug) {
            if (gadget.getPref("refresh") === "") {
                throw "@constructor Configured: \n You have specified 'enableReload' for view. This feature requires you to "
                        + "add <UserPref name=\"refresh\" datatype=\"hidden\" default_value=\"false\" /> to <ModulePrefs> "
                        + "in your gadget XML.";
            }
        }

        if (gadget.getPref("refresh") !== "" && gadget.getPref("refresh") !== "false") {
            window.setInterval(function () {
                gadget.showView(true);
            }, 60000 * gadget.getPref("refresh"));
        }

        gadget.addReloadButton = function () {
            gadget.addMenuItem("reload", gadget.getMsg("gadget.common.reload"), function (e) {
                gadget.showView(true);
                e.preventDefault();
            });
        };

        gadget.viewTemplate.addCallback(gadget.addReloadButton);

    }
    return gadget;
};
