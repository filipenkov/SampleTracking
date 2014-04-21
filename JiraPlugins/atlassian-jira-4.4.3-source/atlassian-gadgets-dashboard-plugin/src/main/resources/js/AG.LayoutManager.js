/**
 * Manages Dashboard layout. This includes organising of gadgets and drag and drop functionality. Dashboard specific
 * methods, NOT gadget specific.
 *
 * @module dashboard
 * @class LayoutManager
 * @constructor
 * @namespace AG
 */

/*jslint bitwise: true, eqeqeq: true, immed: true, newcap: true, nomen: true, onevar: true, plusplus: true, regexp: true, undef: true, white: true, indent: 4 */
/*global AG, AJS, alert, console */

(function () {

    if (typeof Object.create !== 'function') {
        Object.create = function (o) {
            function F() {}
            F.prototype = o;
            return new F();
        };
    }

    var LayoutManager = {

        /**
         * Sends request to server to persist current layout, including gadget ordering and column layout.
         *
         * @method saveLayout
         * @private
         */
        saveLayout: function () {

            var that = this;

            function getData() {
                return AJS.$.extend({
                    layout: that.layout
                }, AG.Sortable.serialize());
            }

            AJS.$.ajax({

                type: "post",
                url: AG.param.get("layoutAction"),

                // Self executing function that firstly sets the layout type & method of storge (put). Secondly sets
                // gadget positioning in layout, by looping through, sequentially, all columns and their gadgets,
                // binding gadget id's to column id's.
                contentType: "application/json",
                data: JSON.stringify(getData()),

                beforeSend: function(xhr) {
                    xhr.setRequestHeader("X-HTTP-Method-Override", "PUT");
                },

                success: function () {
                    if(AJS.debug) {
                        console.log("AG.LayoutManager.saveLayout: Layout (" + that.layout + ") saved successfully");
                    }
                },

                // In the case of an error. This can be caused by a timeout (if specified), http error or parseError
                error: function (request) {
                    if (request.status == 403 || request.status == 401) {
                        alert(AG.param.get("dashboardErrorDashboardPermissions"));
                    }
                    else {
                        alert(AG.param.get("dashboardErrorCouldNotSave"));
                    }
                    if(AJS.debug) {
                        console.log("AG.LayoutManager.saveLayout: Request failed! Printing response object...");
                        console.log(request);
                    }
                }
            });
        },

        getColumn: function (idx) {

            var
            that = this,
            i    = parseInt(idx, 16);

            if (arguments.length) {
                return that.columns;
            } else if (!isNaN(i) || !that.columns[i]) {
                return that.columns.eq(i);
            }
            else if(AJS.debug) {
                console.error("AG.LayoutManager.getColumn: The column index you provided is invalid. " +
                        "Expected a number in the range of 0-" + that.columns.length - 1 + " but recieved " + idx);
            }
        },


        /**
         * Sets the type of layout.
         *
         * @method setLayout
         * @param {String} layout - valid layout code
         * @param {Boolean} save (optional) - Flag to presist layout by sending request to server.
         */
        setLayout: function (layout, save) {

            layout = AJS.$.trim(layout);

            // checking if the layout is not the same as current
            if (this.layout === layout) {
                if(AJS.debug) {
                    console.warn("AG.LayoutManager.setLayout: Ignoring! The layout supplied is the same as the current " +
                    "layout (" + layout + ")");
                }
                return;
            }

            // checking if valid layout
            if (AJS.$.inArray(layout, AG.LayoutManager.layouts) === -1) {
                if(AJS.debug) {
                    console.error("AG.LayoutManager.setLayout: Invalid layout! Was given " + layout + ", but expected " +
                                  "either of '" + AG.LayoutManager.layouts.toString() + "'");
                }
            }

            this.container.addClass("layout-" + layout.toLowerCase());

            // cannot set layout if it is not writable
            if (!this.isWritable()) {
                if(AJS.debug) {
                    console.log("AG.LayoutManager.setLayout: Can't manipulate layout. Layout is not writable");
                }
                return;
            }

            // toggle class that defines column visibility and width's
            if (this.layout) {
                this.container.removeClass("layout-" + this.layout.toLowerCase());
            }

            // move any gadgets in this layout, that are hidden due to layout changes, into the closest visible column.
            AJS.$.each(this.gadgets, function () {

                var
                layoutRep = this.getElement().layoutRep, /* Gadget representaion in column layout */
                prevColumn = layoutRep.parent().prev(); /* Column preceeding gadget's current column */

                if (!layoutRep.is(":visible")) {
                    if (prevColumn.is(":visible")) {
                        prevColumn.append(layoutRep);
                    } else {
                        prevColumn.prev().append(layoutRep);
                    }
                }
            });

            // Add "sortable" classes to columns. AG.Sortable uses these classes to find columns to apply sortable
            // functionality to.
            if (layout === "AB" || layout === "BA" || layout === "AA") {
                this.columns.eq(0).addClass("sortable");
                this.columns.eq(1).addClass("sortable");
                this.columns.eq(2).removeClass("sortable");
            } else if (layout === "A") {
                this.columns.eq(0).addClass("sortable");
                this.columns.eq(1).removeClass("sortable");
                this.columns.eq(2).removeClass("sortable");
            } else if (layout === "AAA") {
                this.columns.eq(0).addClass("sortable");
                this.columns.eq(1).addClass("sortable");
                this.columns.eq(2).addClass("sortable");
            }


            this.layout = layout;
            this.refresh();

            if (save !== false) {
                this.saveLayout();
            }
            else if(AJS.debug) {
                console.log("AG.LayoutManager.setLayout: Layout successfully set to '" + this.layout + "'");
            }
        },

        getGadgets: function () {
            return this.gadgets;
        },

        removeGadget: function (gadget) {
            var that = this;
            AJS.$.each(this.gadgets, function (idx) {
                if (this === gadget) {
                    that.gadgets.splice(idx, 1);
                    return false;
                }
            });
        },

        deactivate: function () {
            this.container.hide();
            AJS.$.each(this.gadgets, function () {
                if (this.getElement().layoutRep.css("display") === "list-item") {
                    this.getElement().hide();
                }
            });
        },

        activate: function () {
            this.container.show();
            AJS.$.each(this.gadgets, function () {
                this.getElement().show();
            });
            this.refresh();
        },

        restoreDefaultView: function () {

            var that = this;

            AJS.$(".operations li", AG.DashboardManager.getDashboard()).toggleClass("hidden");

            this.container.removeClass("maximized");

            jQuery.each(this.getGadgets(), function () {
                if (that.getPublicInstance() !== this) {
                    this.getElement().show();
                }
            });

            this.refresh();
        },

        refresh: function () {

            var that = this, isIE6OrBelow = !!(AJS.$.browser.msie && parseInt(jQuery.browser.version) <= 6);

            function appendEmptyMsgElem(col, idx) {
                var message = AG.param.get("dragHere",
                        "<a class='add-gadget-link' id='add-gadget-" + idx + "' href='#'>", "</a>");
                return AJS.$("<li class='empty-text'>" + message + "</li>").appendTo(col);
            }

            function isCanvasMode() {
                return !!AJS.$('.layout.maximized').size();
            }

            if (!this.initialized && !isCanvasMode()) {
                return;
            }

            if (!AG.DashboardManager.getDashboard().hasClass("dragging")) {

                this.columns.css(isIE6OrBelow ? "height" : "minHeight", "");

                this.columns.filter(":visible").each(function (idx) {

                    var column = AJS.$(this);

                    if (AJS.$("li:visible:not(.empty-text)", column).length === 0) {
                        column.addClass("empty");
                        if (that.isWritable()) {
                            if (!this.msgElem) {
                                this.msgElem = appendEmptyMsgElem(column, idx);
                            } else if (this.msgElem) {
                                this.msgElem.show();
                            }
                        }

                    } else if (column.hasClass("empty")) {
                        column.removeClass("empty");
                        if (this.msgElem) {
                            this.msgElem.hide();
                        }
                    }
                });
            }

            AJS.$("li.gadget", this.container).each(function () {
                this.getGadgetInstance().updatePosition();
            });

            if (!AG.DashboardManager.getDashboard().hasClass("dragging")) {
                this.columns.each(function () {
                    var column = AJS.$(this);
                    column.css(isIE6OrBelow ? "height" : "minHeight", column.parent().height());
                });
            }
        },

        /**
         * Adds the gadget to specified column. If the column is not specified then the gadget is added to the first column.
         *
         * @method addGadget
         * @param {AG.Gadget, Object} gadget - Gadget instance to add
         * @param {Number} column - Column in current layout (optionial)
         */
        addGadget: function (gadget, column) {

            var that = this,
                rpctoken;

            function hideGadget () {
                gadget.getElement().layoutRep.addClass("hidden");
                gadget.getElement().hide();
                gadget.getLayoutManager().refresh();
            }

            function showGadget () {
                delete gadget.hasBeenDropped;
                gadget.getElement().layoutRep.removeClass("hidden");
                gadget.getElement().show();
                gadget.getLayoutManager().refresh();
            }

            function validateAdd (numGadgets) {
                if (parseInt(numGadgets) >= AG.param.get("maxGadgets")) {
                    showGadget();
                    alert(AG.param.get("dashboardErrorTooManyGadgets"));
                } else {
                    gadget.move(that.options.resourceUrl);
                }
            }

            function appendToColumn () {

                if (!that.initialized) {
                    that.columns.eq(column).append(gadget.getElement().layoutRep);
                } else {
                    that.columns.eq(column).prepend(gadget.getElement().layoutRep);
                }
            }

            function isFromDifferentLayout () {
                return AJS.$.isFunction(gadget.getLayoutManager);
            }

            function ensureIframeDoesntCache () {
                AJS.$("iframe", gadget.getElement()).each(function() {
                    this.src = this.src;
                    this.contentWindow.location = this.src;
                });
            }

            if (!isFromDifferentLayout()) {
                column = column || gadget.column || 0;

                gadget = gadget.loaded ? gadget : AG.Gadget.getNullGadgetRepresentation(gadget);

                // create the rpctoken that will be used in rpc calls
                rpctoken = Math.round(Math.random() * 10000000);
                if (gadget.renderedGadgetUrl.indexOf("#rpctoken") == -1) {
                    gadget.renderedGadgetUrl += "#rpctoken=" + rpctoken;
                } else {
                    gadget.renderedGadgetUrl = gadget.renderedGadgetUrl.replace(/#rpctoken=\d*/, "#rpctoken=" + rpctoken);
                }

                // extend gadget descriptor with layout descriptor.
                gadget.layout = this.options;

                // constructs gadget object & methods.
                gadget = AG.Gadget(gadget);

                appendToColumn();

                AG.DashboardManager.getDashboard().contents.append(gadget.getElement());
                gadget.updatePosition();

                // setup the iframe to send/receive rpc calls
                gadgets.rpc.setAuthToken(gadget.getElement().find("iframe").attr("id"), rpctoken);

                ensureIframeDoesntCache();

                // adds to sortable control
                AG.Sortable.update();

            } else {
                hideGadget();
                AJS.$.get(this.options.resourceUrl + "/numGadgets", function (numGadgets) {
                    validateAdd(numGadgets);
                });
            }

            // note: only refreshes after all gadgets are appended.
            this.refresh();

            // store reference to instance
            this.gadgets.push(gadget);

            return gadget;
        },

        markReadOnlyLayout: function () {
            if (!this.isWritable()) {
                this.tab.addClass("inactive");
            }
        },
        onInit: function (callback) {
            if (!this.initialized) {
                this.onInit.callbacks = this.onInit.callbacks || [];
                this.onInit.callbacks.push(callback);
            } else {
                callback();
            }
        },
        unmarkReadOnlyLayout: function () {
            if (!this.isWritable() && this.tab.hasClass("inactive")) {
                this.tab.removeClass("inactive");
            }
        },

        isWritable: function () {
            return this.options.writable;
        },

        getPublicInstance: function () {

            var that = this;

            if (!this.publicInterface) {
                this.publicInterface = {
                    unmarkReadOnlyLayout: function () {
                        return that.unmarkReadOnlyLayout.apply(that, arguments);
                    },
                    restoreDefaultView: function () {
                        return that.restoreDefaultView.apply(that, arguments);
                    },
                    markReadOnlyLayout: function () {
                        return that.markReadOnlyLayout.apply(that, arguments);
                    },
                    isWritable: function () {
                        return that.isWritable.apply(that, arguments);
                    },
                    activate: function () {
                        return that.activate.apply(that, arguments);
                    },
                    deactivate: function () {
                        return that.deactivate.apply(that, arguments);
                    },
                    getGadgets: function () {
                        return that.getGadgets.apply(that, arguments);
                    },
                    getLayout: function () {
                        return that.layout;
                    },
                    getContainer: function () {
                        return that.container;
                    },
                    getColumn: function () {
                        return that.columns;
                    },
                    setLayout: function () {
                        return that.setLayout.apply(that, arguments);
                    },
                    addGadget: function () {
                        return that.addGadget.apply(that, arguments);
                    },
                    removeGadget: function () {
                        return that.removeGadget.apply(that, arguments);
                    },
                    refresh: function () {
                        return that.refresh.apply(that, arguments);
                    },
                    init: function () {
                        return that.init.apply(that, arguments);
                    },
                    saveLayout: function () {
                        return that.saveLayout.apply(that, arguments);
                    },
                    getId: function () {
                        return that.options.id;
                    },
                    onInit: function () {
                        return that.onInit.apply(that, arguments);
                    }
                };
            }

            return this.publicInterface;
        },



        init: function () {
            var that = this, canvasGadget;
            this.gadgets = [];
            this.options.gadgets = this.options.gadgets || [];


            function getCanvasGadgetRepresentation (gadgets) {
                var canvasGadget;
                 AJS.$.each(that.options.gadgets, function () {
                    if (AG.Gadget.isCanvasView(this.id)) {
                        canvasGadget = this;
                        return false;
                    }
                });
                return canvasGadget;
            }

            /**
             * Auto-adjust the number of characters to truncate in the tab label
             * to fit the capacity of the tab.
             * 
             * @param label The jQuery object of the tab label in a "span".
             * @param capacity The width of the tab avaiable for rendering a tab (pixel).
             */
            function fitTab(label, capacity) {
                var labelText = label.text();

                /*
                 * HtmlUnit doesn't seem to update the element width while executing this, which results in an infinite
                 * loop when running the integration tests against a page with tabs if we don't have a second condition
                 * that terminates the loop.  So, as a failsafe, don't let the label text go below three characters.
                 * (AG-882)
                 */
                while (label.width() >= capacity && labelText.length >= 3) {
                    labelText = labelText.slice(0, labelText.length - 1);
                    label.text(labelText + '...');
                }
            }

            function appendTab () {
                var labelSpan = AJS.$('<span />').text(that.options.title).attr("title", that.options.title),
                    labelStrong = AJS.$("<strong />").append(labelSpan),
                    capacity;

                that.tab = AJS.$("<li />");

                if (that.options.uri) {
                    that.tab.append(AJS.$("<a href='" + that.options.uri + "' />").append(labelStrong));
                } else {
                    that.tab.append(labelStrong);
                    that.tab.addClass("active");
                }

                that.tab.get(0).getLayoutInstance = function () {
                    return that.getPublicInstance();
                };

                that.tab.appendTo(AG.DashboardManager.getDashboard().tabContainer);

                capacity = AG.DashboardManager.getDashboard().tabContainer.innerWidth() -
                               parseInt(labelStrong.css('padding-left')) -
                               parseInt(labelStrong.css('padding-right'));
                fitTab(labelSpan, capacity);

                AJS.$("li:first",  AG.DashboardManager.getDashboard().tabContainer).addClass("first");

                if(that.isWritable() && that.options.active === false) {
                    AG.Sortable.addHotSpot(that.tab, function (gadget) {
                        that.addGadget(gadget);
                    });
                }
            }

            function appendColumns () {
                that.container = AJS.$("<div class='layout' />").appendTo(AG.DashboardManager.getDashboard().contents);
                that.columns = AJS.$("<ul />").addClass("column first")
                .add(AJS.$("<ul />").addClass("column second"))
                .add(AJS.$("<ul />").addClass("column third"))
                .appendTo(that.container);
            }

            function appendGadgets () {
                AJS.$.each(that.options.gadgets, function () {
                    that.addGadget(this);
                });
            }

            function setInitialized () {
                that.initialized = true;
                if (that.onInit.callbacks) {
                    AJS.$.each(that.onInit.callbacks, function () {
                        this();
                    });
                }
                that.refresh();
            }

            if (AG.DashboardManager.getDashboard().tabContainer) {
                appendTab();
            }

            if (this.options.active !== false) {
                appendColumns();

                this.setLayout(this.options.layout, false);

                canvasGadget = getCanvasGadgetRepresentation(this.options.gadgets);

                if (canvasGadget) {
                    canvasGadget = this.addGadget(canvasGadget);
                    canvasGadget.setView("canvas");
                    canvasGadget.updatePosition();

                    AJS.$.aop.after({target: canvasGadget, method: "setView"}, function () {
                        canvasGadget.remove();
                        that.removeGadget(canvasGadget);
                        appendGadgets();
                        setInitialized();
                    });
                } else {
                    appendGadgets();
                    setInitialized();
                }
            }
        }
    };

    AG.LayoutManager = function (options) {

        // Using prototype as there could be many gadgets on the page. This is most memory efficient.
        var layoutManager = Object.create(LayoutManager);


        layoutManager.options = options;

        // define public interface
        return layoutManager.getPublicInstance();

    };


    /**
     * @property layouts
     * @type Array
     * @static
     */
    AG.LayoutManager.layouts = ["A", "AA", "BA", "AB", "AAA"];

    /**
     * @method getLayoutAttrName
     * @static
     * @param layout
     */
    AG.LayoutManager.getLayoutAttrName = function (layout) {
        return "layout-" + layout.toLowerCase();
    };

}());



