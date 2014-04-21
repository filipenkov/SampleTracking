/**
 * Manages dashboard. Besides rendering the dashboard menu, this singleton is responsible for delegating actions to
 * layoutManagers.
 *
 * @module dashboard
 * @class DashboardManager
 * @constructor
 * @namespace AG
 */

/*global AJS, console, document*/
/*jslint bitwise: true, eqeqeq: true, immed: true, newcap: true, nomen: true, onevar: true, plusplus: true, regexp: true, undef: true, white: true, indent: 4 */

//DO NOT EDIT. The DebugTransformer depends on this!
AJS.debug=true;
//END DO NOT EDIT


AG = {}; // set namespace


AG.DashboardManager = (function () {

    var

    dashboard,

    /**
     * Data store of AG.LayoutMangers
     *
     * @property layouts
     * @private
     * @type {Array}
     */
    layouts = [],

    /**
     * Creates dashboard menu
     *
     * @method createMenu
     * @private
     */
    createMenu = function (json) {

        var // local variable declarations
        editButton, /* {jQuery} jQuery wrapped html element, the edit button */
        descriptor; /* {Object} contains dashboard menu properites. Used by templater to render html */

        // Gets attributes to assign event handlers
        descriptor = AG.render.getDescriptor("dashboardMenu", json);

        // Appends rendered html to dashboard. HTML produced by rendering engine, AG.render.
        dashboard.contents.append(AG.render(descriptor));

        // Gets the edit button using the id set in descriptor 'dashboardMenu'.
        editButton = AJS.$("#layout-changer");

        editButton.click(function (e) {
            // stop default link action (do not follow link)
            e.preventDefault();
            // opens edit layout dialog
            AG.DashboardManager.editLayout();
        });

        AJS.$("#dashboard-tools-dropdown").dropDown("standard", {
            trigger: ".aui-dd-link"
        });
    };

    return {


        /**
         * Gets dashboard container. The &lt;div&gt; that serves as the container for all other dashboard HTML.
         *
         * @method getDashboard
         * @return {jQuery} dashboard htmlElement
         */
        getDashboard: function () {
            return dashboard;
        },
        
        /**
         * Creates a new instance of AG.LayoutManger. On construction the Layout Manager will build the html for the
         * columns, and if descriptors provided, gadgets also. If gadget descriptors are provided, layout will be set to
         * active.
         *
         * <dl>
         *  <dt>Usage</dt>
         *  <dd>
         *      <pre>
         *
         *      // This will create a layout. It will set it to active because you have specified gadgets
         *      AG.DashboardManager.addLayout({
         *           title: "Dashboard for Scott Harwood",
         *           type: "layout-aaa",
         *           gadgets: [
         *               {
         *                   "height": "300",
         *                   "id":"1",
         *                   "title":"All Hidden Prefs test",
         *                   "gadgetSpecUrl":"http://gadgetspeclocation.com",
         *                   "color":"color4",
         *                   "isMaximizable":false,
         *                   "userPrefs":null,
         *                   "renderedGadgetUrl":"http://gadgetlocationrenderlocation.com"
         *               },
         *               {
         *                   "height": "300",
         *                   "id":"1",
         *                   "title":"All Hidden Prefs test",
         *                   "gadgetSpecUrl":"http://gadgetspeclocation.com",
         *                   "color":"color4",
         *                   "isMaximizable":false,
         *                   "userPrefs":null,
         *                   "renderedGadgetUrl":"http://gadgetlocationrenderlocation.com"
         *               }
         *           ]
         *       });
         *      </pre>
         *  </dd>
         *  <dd>
         *      <pre>
         *
         *      // This will create a layout. It will be inactive as you have specifed a url of which to retrieve gadget descriptors from, when selected.
         *      AG.DashboardManager.addLayout({
         *           title: "Dashboard for Scott Harwood",
         *           type: "layout-aaa",
         *           gadgets: "http://gadget-decriptor-url-for-this-layout/
         *      });
         *      </pre>
         *      </pre>
         *  </dd>
         * </dl>
         *
         * @method addLayout
         * @param {Object} descriptor - JSON describing layout <em>type</em>, <em>title</em>, & <em>gadgets</em>.
         * Gadgets can be an array of gadget descriptors or a url that can be requested to retrieve them.
         */
        addLayout: function (descriptor) {

            var // local variable declarations
              /* {AG.LayoutManager} Instance of AG.LayoutManager  */
            layout = AG.LayoutManager(descriptor);

                // add instance to collection so we can refer to it later
                layouts.push(layout);

                if (descriptor.active !== false) {
                    // make this layout (tab) the active one
                    this.setLayout(layout);
                }

                layout.init();
        },

        showShims: function () {
            if (!AJS.$("body").hasClass("propagation-blocker")) {
                AJS.$("body").addClass("propagation-blocker");
                this.getDashboard().shim.height(this.getDashboard().outerHeight());
            }
        },

        hideShims: function () {
            if (AJS.$("body").hasClass("propagation-blocker")) {
                AJS.$("body").removeClass("propagation-blocker");
            }

        },

        /**
         * Executes the diagnostics script, or retrieves the result if it has already been executed before.
         */
        doDiagnostics: function() {
            AJS.$.ajax({
                type: "post",
                url : AG.param.get("dashboardDiagnosticsUrl"),
                data: {
                    uri: document.location.href
                },
                error: function(request) {
                    if (request.status == 500) {
                        diagnosticsErrorDisplay(request);
                    }
                },
                success: function(data) {
                    // do not show warning
                    AJS.$("#diagnostic-warning").addClass("hidden");
                }
            });

            var diagnosticsErrorDisplay = function(request) {
                var diagnosticsWarningDiv = AJS.$("#diagnostic-warning");

                diagnosticsWarningDiv.html(request.responseText);

                var learnMoreText = "Click here to learn more";
                var diagnosticsContentDiv = AJS.$("#diagnostic-content", diagnosticsWarningDiv);
                var learnMoreLink = AJS.$("#learn-more-link", diagnosticsWarningDiv);
                var displayErrorLink = AJS.$("#display-error-link", diagnosticsWarningDiv);
                var stackTraceDiv = AJS.$("#error-stack-trace", diagnosticsWarningDiv);

                var closeWarning = function() {
                    diagnosticsWarningDiv.slideUp();
                    diagnosticsWarningDiv.addClass("hidden");
                    AJS.$.ajax({
                        type: "post",
                        url: AG.param.get("dashboardDiagnosticsUrl"),
                        data: {
                            method: "delete"
                        }
                    });
                };

                var setToggleDetails = function(link, detailDiv) {
                    link.click(function(){
                        if (link.text() == "Hide") {
                            link.text(learnMoreText);
                            detailDiv.slideUp();
                            detailDiv.addClass("hidden");
                        } else {
                            detailDiv.removeClass("hidden");
                            detailDiv.slideDown('slow');
                            link.text("Hide");
                        }
                    });
                };

                setToggleDetails(learnMoreLink, diagnosticsContentDiv);
                setToggleDetails(displayErrorLink, stackTraceDiv);

                AJS.$("#diagnostic-warning .close").click(function() {
                    closeWarning();
                });

                diagnosticsWarningDiv.removeClass("hidden");
            };

        },

        /**
         * Displays edit layout dialog. This function evolves. First time it is called will constuct the html and
         * show layout dialog. Subsequent times it will simply toggle it's visibility.
         *
         * @method editLayout
         */
        editLayout: function () {

            var // local variable declarations
            descriptor, /* {Object} contains edit layout dialog properites. Used by templater to render html */
            popup;      /* {AJS.popup} instance of AJS.popup */

            // get properties for layo  ut dialog
            descriptor = AG.render.getDescriptor("layoutDialog", {
                layoutType: AG.DashboardManager.getLayout().getLayout()
            });

            // create instance of AJS.popup. This does NOT show popup.
            popup = AJS.popup(507, 150, "layout-dialog");

            // Appends rendered html to popup element. HTML produced by rendering engine AG.render. Properties used by
            // rendering engine are gatherd from a call to AG.render.getDescriptor("layoutDialog").
            popup.element
                    .html(AG.render(descriptor))
                    .addClass(AG.LayoutManager.getLayoutAttrName(descriptor.layoutType)).addClass("aui-dialog");

            // adds close button & close behaviour
            AJS.$("#" + descriptor.closeId, popup.element).click(function (e) {

                popup.hide();

                // don't follow link
                e.preventDefault();
            });

            // Find all the layout representations in dialog. Assign a click handler to them that will change the
            // current layout.
            AJS.$.each(AG.LayoutManager.layouts, function () {
                var
                layout = this,
                layoutAttrName = AG.LayoutManager.getLayoutAttrName(layout);

                AJS.$("#" + layoutAttrName).click(function (e) {

                    // Find the layout we are going to affect.
                    var activeLayout = AG.DashboardManager.getLayout();
                    // Set the highlighted layout for this dialog. So next time we open the dialog, the correct one is
                    // highlighted.

                    popup.element
                            .removeClass(AG.LayoutManager.getLayoutAttrName(activeLayout.getLayout()))
                            .addClass(layoutAttrName);

                    // Finally display the selected layout.
                    activeLayout.setLayout(layout);
                    popup.hide();

                    AG.Sortable.update();
                    
                    // Don't follow link.
                    e.preventDefault();
                });
            });

            // Re-define this method (thankyou javascript), so that the next time we call editActive layout we do not
            // constuct the html everytime. All we do is toggle it's visibility with a call to the show method.
            this.editLayout = (function () {
                popup.show();
                AJS.$(document).keyup(function (e) {
                    if (e.keyCode === 27) {
                        popup.hide();
                        AJS.$(document).unbind("keyup", arguments.callee);
                        e.preventDefault();
                    }
                });
                return arguments.callee;
            }()); // call myself straight away. Don't worry! I will be restored by returning myself (arguments.callee)


        },


        /**
         * Gets active layout manager
         *
         * @method getLayout
         * @return {AG.LayoutManager}
         */
        getLayout: function () {
            return this.activeLayout;
        },

        markReadOnlyLayouts: function () {
            AJS.$.each(layouts, function () {
                if (!this.isWritable()) {
                    this.markReadOnlyLayout();
                }
            });
        },

        unmarkReadOnlyLayouts: function () {
            AJS.$.each(layouts, function () {
                if (!this.isWritable()) {
                    this.unmarkReadOnlyLayout();
                }
            });
        },

        /**
         * Sets active layout manager
         * @method setLayout
         * @param {AG.LayoutManager} layout
         */
        setLayout: function (layout) {
//            if (this.activeLayout) {
//                this.activeLayout.deactivate();
//                this.activeLayout.tab.removeClass("active");
//                layout.activate();
//            }
            // layout.tab.addClass("active");
            this.activeLayout = layout;
        },

        /**
         * Creates gadget using the provided <em>gadgetDesriptor</em> and appends it to the specified column of the
         * active layout/tab. If column is not specified, will be added as first gadget in the first column.
         *
         * <dl>
         *  <dt>Usage</dt>
         *  <dd>
         *      <pre>
         *      AG.DashboardManager.addGadget({
         *          "height": "300",
         *          "id":"1",
         *          "title":"All Hidden Prefs test",
         *          "gadgetSpecUrl":"http://gadgetspeclocation.com",
         *          "color":"color4",
         *          "isMaximizable":false,
         *          "userPrefs":null,
         *          "renderedGadgetUrl":"http://gadgetlocationrenderlocation.com"
         *       }, 1);
         *       </pre>
         * </dl>
         *
         *
         * @method addGadget
         * @param {Object} gadgetDescriptor - JSON with gadget properites
         * @param {Number} column - Column to append gadget to. (optional)
         */
        addGadget: function (gadget, column) {
            this.activeLayout.addGadget(gadget, column);
        },

        /**
         * Creates furniture & layouts, sets params & il8n strings.
         *
         * @method setup
         * @param options
         */
        setup: function (options) {
            var
            that = this,     /* {AG.DashboardManager} 'this' reference for inside of inner functions */
            securityTokenRefreshRate = AJS.parseUri(document.location.href).queryKey["__st_refresh"] || 1000*60*12;
            
            console.debug = console.debug || function() {};
            
            // add a point cut to the setHeight service so that it refreshes the positioning of our gadgets. They are
            // absolute positioned so a change of height affects their offset.
            AJS.$.aop.after({target: gadgets.IfrGadgetService.prototype, method: "setHeight"}, function () {
                that.getLayout().onInit(function () {
                    that.getLayout().refresh();
                });
            });

            // adds all the il8n and param strings to data store
            AG.param.set(options.params);
            
            AG.render.ready(function () {
                AJS.$(function () {

                    // creates shim that sits over dashboard to prevent propagation of events during actions like dragging
                    AG.Sortable.init();

                    dashboard = AJS.$("#dashboard");
                    dashboard.header = AJS.$("<div id='dashboard-header' />").appendTo(dashboard);

                    //if there's only one tab, hide the tabs.
                    if (options.layouts.length > 1) {
                        // This class name ties to draggability in Canvas mode. See applyFocusControls()
                        that.getDashboard().addClass("v-tabs");
                        dashboard.tabContainer = AJS.$("<ul class='vertical tabs' />").appendTo(dashboard);
                    }

                    dashboard.contents = AJS.$("<div id='dashboard-content' />").appendTo(dashboard);

                    if(options.menu.items && options.menu.items.length > 0) {
                        dashboard.menu = createMenu(options.menu);
                    }
                    dashboard.shim = AJS.$('<div class="dashboard-shim"> </div>').appendTo(dashboard.contents);


                    AJS.$.each(options.layouts, function () {
                        // creates layout instance and appends gadgets, if provided.
                        that.addLayout(this);
                    });

                    dashboard.removeClass("initializing");

                    function updateSecurityTokens() {
                        var gadgetTokenFrames = new Array(),
                            updateTokenParams = {};
                        console.debug("Updating all gadget security tokens");
                        
                        AJS.$.each(AG.DashboardManager.getLayout().getGadgets(), function(index) {
                            gadgetTokenFrames.push({
                                gadget: this,
                                iframeId: this.getElement().find("iframe.gadget-iframe").attr("id")
                            });
                            updateTokenParams["st." + index] = this.getSecurityToken();
                        });
                        if (!updateTokenParams["st.0"]) {
                            console.debug("No gadgets on dashboard, so there is no need to update security tokens.")
                            return;
                        }
                        AJS.$.ajax({
                            type: "POST",
                            url: AG.param.get("securityTokensUrl"),
                            data: updateTokenParams,
                            dataType: "json",
                            success: function(newSecurityTokens) {
                                AJS.$.each(gadgetTokenFrames, function(index) {
                                    this.gadget.setSecurityToken(newSecurityTokens["st." + index]);
                                    try {
                                        gadgets.rpc.call(this.iframeId, "update_security_token", null, this.gadget.getSecurityToken());
                                    } catch (e) {
                                        console.debug(
                                            "Unable to update the security token for gadget with iframe id " +
                                            this.iframeId + ".  This likely means that the gadget does not use the " +
                                            "'auth-refresh' feature.  If the gadget uses gadgets.io.makeRequest after its" +
                                            "initial startup, it is a good idea to use the 'auth-refresh' feature " +
                                            "by adding <Optional feature='auth-refresh' /> to your gadget's " +
                                            "<ModulePrefs> section.  Otherwise, the gadget's security token could expire" +
                                            " and subsequent calls to gadgets.io.makeRequest will fail.");
                                    }
                                });
                                console.debug("Updating security tokens complete.");
                            },
                            error: function(request, textStatus) {
                                if (request.status != 200) {
                                   console.debug(
                                       "Failed to get new security tokens. Response was had a status of '" +
                                       request.status + "' saying '" + request.statusText + "'");
                                } else {
                                    console.debug("There was an error processing the response. Error was '" +
                                        textStatus + "'");
                                }
                            }
                        });
                    };

                    console.debug("Security tokens will be refreshed every " + securityTokenRefreshRate + "ms");
                    window.setInterval(updateSecurityTokens, securityTokenRefreshRate);
                    that.doDiagnostics();
                });
            });

            AG.render.initialize();
        }
    };
}());


// JRA-19963: If a gadget's iframe size is updated then we need to refresh the layout. We need to do this because all the
// gadgets are absolutely positioned so the dashboard chrome height needs to be calculated and applied.

(function () {

    var buffer;

    jQuery(AG).bind("AG.iframeResize", function () {

        if (buffer) {
            clearTimeout(buffer);
        }
        
        buffer = window.setTimeout(function () {
            AG.DashboardManager.getLayout().refresh();
            buffer = null;
        }, 100);
    });

})();


