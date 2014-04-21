

/**
 * Gadget implementation.
 *
 * @module dashboard
 * @class Gadget
 * @constructor
 * @namespace AG
 */

(function(){

    function FauxList (listContainer) {

        var that = this, ENTER_KEY = 13;

        this.container = AJS.$(listContainer);
        this.values = AJS.$("input.list", this.container).hide().val().split("|");
        this.originalValues = this.values.join("|").split("|"); // creating a separate array - ugly but it works.

        // Create fake form elements and add to container
        this.container.append("<input class=\"add-to-list text med js\" type=\"text\" value=\"\" />"
                + "<button class=\"submit-to-list js\">" + AG.param.get("add") + "</button>");

        // Add list items to list individually
        this.container.children(".submit-to-list").click(function(e) {
            that.addToList();
            e.preventDefault();
        });

        AJS.$("input.add-to-list", this.container).keydown(function(e) {
            if (e.keyCode == ENTER_KEY) {
                that.addToList();
                e.preventDefault();
            }
        });

        AJS.$(".description", this.container).appendTo(this.container);

        this.create(this.values);
    }

    FauxList.prototype = {


        reset: function () {
            this.create(this.originalValues);
        },

        create: function (values) {

            var ul, listItem, that = this;

            // If the list is empty, skip all of this.
            if (values) {

                // Strip empty values
                AJS.$.each(values, function() {
                    if (this == "") {
                        values.splice(values.indexOf(this), 1);
                    }
                });

                // Create UL to hold visible list
                ul = AJS.$("<ul class=\"faux-list\"></ul>");

                // Create list for any list items
                // Else, remove if none
                if (values.length > 0) {
                    for (var i = 0, ii = values.length; i < ii; i++) {

                        // Make LI element with delete functionality
                        listItem = AJS.$("<li class=\"listvalue-" + i + "\"><span>" + values[i] +
                                         "</span><a class=\"remove\" href=\"#remove-from-list\" title=\"" +
                                         AG.param.get("removeFromList") + "\">x</a></li>");

                        // Remove list items from list individually
                        AJS.$("a", listItem).click(function(e) {

                            // Grab the value to be removed from the array
                            var removeValue = AJS.$(this).parent("li").attr("class").split("-")[1];

                            // Find the index of the value and remove it
                            values.splice(removeValue, 1);

                            if (values.length > 0) {
                                that.create(values);
                            }
                            else {
                                AJS.$("input.list",     this.container).val("");
                            }

                            // Remove LI from visible list
                            listItem.remove();
                            e.preventDefault();
                        });

                        // Add new list items to ul.faux-list
                        ul.append(listItem);
                    }

                    // If no values existed, add ul.faux-list before the old input
                    // Else, replace old .faux-list with new version
                    if (AJS.$("ul.faux-list", this.container).length == 0) {
                        AJS.$("input.list", this.container).before(ul);
                    }
                    else {
                        AJS.$("ul.faux-list", this.container).replaceWith(ul);
                    }
                }
                else {
                    AJS.$("ul.faux-list", this.container).remove();
                }

                // Rewrite the old input value with current array
                AJS.$("input.list", this.container).val(values.join("|"));

                AG.DashboardManager.getLayout().refresh();

            }
        },

        addToList: function () {
            // Stripping pipes from our pipe-delimited input
            var newValue = AJS.$("input.add-to-list", this.container).val().replace(/\|+/g, '%7C');

            //Grab value from new input and add to array
            this.values.push(newValue);

            // Clear new input
            AJS.$("input.add-to-list", this.container).val("");

            // Remake the list
            this.create(this.values);
        }
    };

    var Gadget =  {

        /**
         * Draws gadget, preference form and furniture
         *
         * @method draw
         */
        draw: function (json) {

            var that = this;

            function createElement () {
                json.minimized = that.minimized;
                json.view = that.view;
                that.$ = AJS.$(AG.render("gadget", json));
                that.$.updateShadow = function () {
                    var attrs = {
                        width: that.$.width() + 32,
                        height: that.$.height() + 29
                    };

                    that.$.shadow.sides.css("height", attrs.height - 46);
                    that.$.shadow.bottom.css("width", attrs.width - 58);
                    that.$.shadow.css(attrs);
                };
            }

            // setHeightFromCookie must be called before rendering the gadget
            function setHeightFromCookie() {
                if (json) {
                    var height = AG.Cookie.read("gadget-" + json.id + "-fh", null);
                    if (!isNaN(parseInt(height))) {
                        json = AJS.$.extend(json, {height: height});
                    }
                }
            }

            function setElementShortcuts () {
                that.$.layoutRep = AJS.$("<li class='gadget' id='rep-" + json.id + "' />").height(that.$.height());
                that.$.shadow = AJS.$("div.shadow", that.$);
                that.$.shadow.bottom = AJS.$("div.b", that.$.shadow);
                that.$.shadow.sides = AJS.$("div.l", that.$.shadow).add(AJS.$("div.r", that.$.shadow));
                that.$.layoutRep.get(0).getGadgetInstance = that.$.get(0).getGadgetInstance = function () {
                    return that.getPublicInstance();
                };
            }

            function applyDropdownControls () {
                
                var
                ACTIVE_CLASS    = "dropdown-active",
                ddParent        = AJS.$("li.aui-dd-parent", that.$);
                
                ddParent.mousedown(function (e) {
                    e.stopPropagation();
                });

                that.$.dropdown = ddParent.dropDown("standard", {
                    selectionHandler: function (e) {
                        e.preventDefault();
                    },
                    item: "> li"
                })[0];

                that.$.dropdown.addCallback("show", function () {
                    that.$.addClass(ACTIVE_CLASS);
                    that.$.updateShadow(); // using shadow as shim.
                    AG.DashboardManager.showShims();
                });

                that.$.dropdown.addCallback("hide", function () {
                    that.$.removeClass(ACTIVE_CLASS);
                    // if we are not dragging
                    AG.DashboardManager.hideShims();
                });

                that.$.hover(function () {}, function () {
                    if (that.$.dropdown.$.is(":visible")) {
                        that.$.dropdown.hide();
                    }
                });
            }

            function applyGadgetHoverControls () {

                var HOVER_CLASS = "gadget-hover";

                that.$.hover(function() {
                    AJS.$(".gadget-container", that.$).addClass(HOVER_CLASS);
                }, function () {
                    AJS.$(".gadget-container", that.$).removeClass(HOVER_CLASS);
                });
            }

            function applyColorControls () {
                AJS.$(".gadget-colors a", that.$).click(function (e) {
                    that.setColor(this.parentNode.className);
                    e.preventDefault();
                });
            }

            function applyMinimizeControls () {

                var
                menuElem    = AJS.$("a.minimization, a.maximization", that.$),
                titleElem   = AJS.$(".dashboard-item-title", that.$),

                maxMinToggle = function (e) {

                    if (that.minimized) that.maximize();
                    else that.minimize();

                    AJS.$(this).one(e.type, function (e) {
                        if (that.minimized) that.maximize();
                        else that.minimize();
                        AJS.$(this).one(e.type, maxMinToggle);
                    });
                };

                titleElem.one("dblclick", maxMinToggle);
                menuElem.one("click", maxMinToggle);

                /* AJS.$.browser is deprecated, for the preferred feature detection. However feature detection cannot detect safari. */
                if (AJS.$.browser.safari) {
                    /* stops double click from selecting title text */
                    titleElem.get(0).onselectstart = function () {
                        return false;
                    };
                }

            }

            function applyFocusControls () {

                var column;

                AJS.$(".dashboard-item-header", that.$).mousedown(function (e) {
                        // Don't make draggable in Canvas mode unless there are multiple Dashboards.
                        if (!that.$.hasClass("maximized") || AG.DashboardManager.getDashboard().hasClass("v-tabs")) {
                          
                            if (!column) {
                                column = that.$.layoutRep.parent();
                            }

                             // hide dropdown if it is visible
                            that.$.dropdown.hide();

                            that.$.focusTimeout = setTimeout(function() {
                                that.$.updateShadow();
                                that.$.shadow.show();
                                delete that.$.focusTimeout;
                            }, 150);
                            that.$.layoutRep.trigger(e);
                        }

                    });
                    that.$.mouseup(function () {
                        if (that.$.focusTimeout) {
                            clearTimeout(that.$.focusTimeout);
                        } else if (that.$.layoutRep.is(":visible")) {
                            that.$.stop(true, true);
                            that.$.shadow.hide();
                        }
                    });
            }

            function applyUserPrefControls () {

                var
                fauxLists = [],
                prefForm = AJS.$(".userpref-form", that.$),
                prefsChanged = false,
                savePrefForm = function(success) {
                    var formArray = prefForm.serializeArray(),
                        submittedState = {};
                    AJS.$.each(formArray, function() {
                        submittedState[this.name] = this.value || '';
                    });

                    AJS.$(":checkbox:not(:checked)", prefForm).each(function () {
                        submittedState[this.name] = false;
                    });

                    AJS.$.ajax({
                        url: prefForm.attr("action"),
                        type: "POST",
                        contentType: "application/json",
                        data: JSON.stringify(submittedState),
                        beforeSend: function(xhr) {
                            xhr.setRequestHeader("X-HTTP-Method-Override", "PUT");
                        },
                        dataType: "text",
                        success: success
                    });
                };

                if (that.isEditable()) {
                    // Creates a neater form element for adding multiple values
                    AJS.$(".list-container", that.$).each(function() {
                        fauxLists.push(new FauxList(this));
                    });

                    AJS.$(".edit", that.$).click(function (e) {
                        if (!prefForm.is(":visible")) {
                            if (that.minimized) {
                                that.maximize();
                            }
                            prefForm.show();
                            AJS.$(".field-group :input:first", prefForm).focus();
                            that.getLayoutManager().refresh();
                        } else {
                            prefForm.hide();
                            that.getLayoutManager().refresh();
                        }
                        e.preventDefault();
                    });

                    prefForm.submit(function (e) {
                        savePrefForm(function () {
                            window.location.reload();
                        });

                        e.preventDefault();
                    });

                    AJS.$(":reset", prefForm).click(function () {
                        AJS.$.each(fauxLists, function () {
                            this.reset();
                        });
                        prefForm.hide();
                        that.getLayoutManager().refresh();
                    });
                }

                /*
                 * Event handler for a customed event "setUserPref".  This event
                 * is triggered inside gadgets-dashboard.js -> setUserPref().
                 * It acts like a buffer for all the setPref() calls.
                 * The results are flushed to the server using ajax later.
                 * arguments e is something like [name1, value1, name2, value2]
                 */
                AJS.$("iframe.gadget-iframe", that.$).bind("setUserPref", function (e) {

                    // update the preference form with the given name value pairs
                    for (var i = 1, j = arguments.length; i < j; i += 2) {
                        if (pref_name = arguments[i]) {
                            var pref_value = arguments[i+1];
                            var input = AJS.$(":input[name='up_" + pref_name + "']", prefForm);
                            // checkbox needs a default value of false
                            if (input.attr("type") == "checkbox") {
                                input.attr("checked", pref_value);
                            } else {
                                input.val(pref_value);
                            }
                        }
                    }
                    
                    // flag for whether a flush is scheduled
                    if(!prefsChanged) {
                        setTimeout(function () {
                            savePrefForm(function () {} );
                            prefsChanged = false;
                        }, 100);
                        prefsChanged = true;
                    }
                });


            }

            function applyDeleteControls () {
                AJS.$("a.delete", that.$).bind("click", function (e) {
                    that.destroy();
                    e.preventDefault();
                });
            }

            function applyViewToggleControls () {

                AJS.$("a.maximize", that.$).click(function (e) {
                    if (!AG.Gadget.isCanvasView(that.id)) {
                        that.getPublicInstance().setView("canvas");
                        e.preventDefault();
                    } else {
                        that.getPublicInstance().setView("default");
                        e.preventDefault();
                    }
                });
            }


            function applyErrorMessage () {
                AJS.$("iframe.gadget-iframe", that.$).load(function() {
                    AJS.$("#error-gadget-message", AJS.$(this).contents()).html(that.errorMessage);
                });
            }

            setHeightFromCookie();
            createElement();
            setElementShortcuts();
            applyDropdownControls();
            applyGadgetHoverControls();
            applyMinimizeControls();

            if (this.isMaximizable) {
                applyViewToggleControls();
            }

            if (!this.loaded) {
                applyErrorMessage();
            }

            if (this.getLayoutManager().isWritable()) {
                applyDeleteControls();
                applyFocusControls();
                applyColorControls();
                applyUserPrefControls();
            }
        },

        isEditable: function () {
            return !!(this.getLayoutManager().isWritable() && this.hasNonHiddenUserPrefs);   
        },

        setLayoutManager: function (layoutManager) {
            if (layoutManager) {
                this.layoutManager = layoutManager;
            } else {
                this.layoutManager = AG.DashboardManager.getLayout();
            }
        },

        /**
         *
         * Provides access to gadget's Layout manager. When the gadget
         * needs to know how it's owner is. Such as in the method destroy
         * where we need to also remove it from the layout.
         *
         * @method getLayoutManager
         */
        getLayoutManager: function () {
            if (!this.layoutManager) {
                this.setLayoutManager();
            }
            return this.layoutManager;
        },

        /**
         * Sets gadget chrome colour. Gets chrome and applies associated class and
         * sends preference back to server to persist.
         *
         * @method setColor
         */
        setColor: function (color) {

            var that = this;

            that.$.removeClass(that.color).addClass(color);
            that.color = color;
            AJS.$.ajax({
                type: "post",
                url: that.colorUrl,
                contentType: "application/json",
                data: JSON.stringify({color: color}),
                beforeSend: function(xhr) {
                    xhr.setRequestHeader("X-HTTP-Method-Override", "PUT");
                },
                error: function(request) {
                    if (request.status == 403 || request.status == 401) {
                        alert(AG.param.get("dashboardErrorDashboardPermissions"));
                    } else {
                        alert(AG.param.get("dashboardErrorCouldNotSave"));
                    }
                }
            });
        },
        
        updatePosition: function () {

            var
            gadgetCSSToUpdate,
            layoutCSSToUpdate,
            that = this;

            function isGadgetBeingDragged () {
                return that.$.hasClass("dragging");
            }

            function getCurrentGadgetCSS () {
                var
                LAYOUT_REP_OFFSET,
                dashboard = AG.DashboardManager.getDashboard().contents,
                DASHBOARD_OFFSET  = dashboard.offset();

                if (!getCurrentGadgetCSS.cache) {
                    LAYOUT_REP_OFFSET = that.$.layoutRep.offset();
                    getCurrentGadgetCSS.cache = {
						left: (LAYOUT_REP_OFFSET.left - DASHBOARD_OFFSET.left) / dashboard.width() * 100 + "%",
						top: LAYOUT_REP_OFFSET.top - DASHBOARD_OFFSET.top,
						width: that.$.layoutRep.width() / dashboard.width() * 100 + "%"
					};
                }
                return getCurrentGadgetCSS.cache;
            }

            function getCurrentLayoutRepCSS () {
                if (!getCurrentLayoutRepCSS.cache) {
                    getCurrentLayoutRepCSS.cache = {
						height: that.$.height()
                    };
                }
                return getCurrentLayoutRepCSS.cache;
            }

            function filterModifiedCSS (lastRecordedCSS, currentCSS) {
                if (lastRecordedCSS)  {
                    AJS.$.each(lastRecordedCSS, function(property){
						if (this === currentCSS[property]) {
							delete currentCSS[property];
						}
					});
                }
                return currentCSS;
            }

            if (!isGadgetBeingDragged()) {
                layoutCSSToUpdate = filterModifiedCSS(this.$.layoutRep.lastRecordedCSS, getCurrentLayoutRepCSS());
                this.$.layoutRep.css(layoutCSSToUpdate);
                this.$.layoutRep.lastRecordedCSS = layoutCSSToUpdate;

                gadgetCSSToUpdate = filterModifiedCSS(this.$.lastRecordedCSS, getCurrentGadgetCSS());
                this.$.css(gadgetCSSToUpdate);
                this.$.lastRecordedCSS = gadgetCSSToUpdate;

                if (this.$.hasClass("hidden")) {
                    this.$.removeClass("hidden");
                }
            }

        },

        /**
         * Minimises gadget. Hides everything but title bar.
         *
         * @method maximize
         */
        maximize: function () {
            var
            MIN_CLASS   = "minimization",
            MAX_CLASS   = "maximization",
            MIN_TEXT    = AG.param.get("minimize"),
            menuElem    = AJS.$("a.minimization, a.maximization", this.$);

            menuElem.removeClass(MAX_CLASS).addClass(MIN_CLASS).text(MIN_TEXT);

            // need to reset height to auto because sortable control sets an explicit pixel height
            this.$.css({height: "auto"});
            AJS.$(".dashboard-item-content", this.$).removeClass(MIN_CLASS);
            // updates positioning of gadgets & their layout references
            this.getLayoutManager().refresh();

            /* erase cookie */
            AG.Cookie.erase(this.COOKIE_MINIMIZE);
            this.minimized = false;
        },

        minimize: function () {

            var
            MIN_CLASS   = "minimization",
            MAX_CLASS   = "maximization",
            MAX_TEXT    = AG.param.get("expand"),
            menuElem    = AJS.$("a.minimization, a.maximization", this.$);

            menuElem.removeClass(MIN_CLASS).addClass(MAX_CLASS).text(MAX_TEXT);

            // need to reset height to auto because sortable control sets an explicit pixel height
            this.$.css({height: "auto"});
            AJS.$(".dashboard-item-content", this.$).addClass(MIN_CLASS);
            this.getLayoutManager().refresh();
            AG.Cookie.save(this.COOKIE_MINIMIZE, "true");
            this.minimized = true;
        },

        remove: function () {
            var that = this;
                that.$.layoutRep.remove();
                that.$.remove();
                that.getLayoutManager().removeGadget(that.getPublicInstance());
                that.getLayoutManager().refresh();
                 // remove from memory
                AJS.$.each(this, function (name, property) {
                    property = null;
                });
                AG.Cookie.erase("gadget-" + that.id + "-fh");
        },

        /**
         * Moves gadget from the dashboard it's currently on to a new dashboard specified
         * by the target resource URL
         */
        move: function (targetResourceUrl) {
            this.remove();

            AJS.$(AJS.$.ajax({
                type: "post",
                data: {id : this.id,
                       title : this.title,
                       titleUrl : this.titleUrl,
                       gadgetSpecUrl : this.gadgetSpecUrl,
                       height : this.$.height(),
                       width : this.$.width(),
                       color : this.color,
                       isMaximizable : this.isMaximizable,
                       userPrefs : this.userPrefs,
                       renderedGadgetUrl : this.renderedGadgetUrl,
                       colorUrl : this.colorUrl,
                       gadgetUrl : this.gadgetUrl,
                       hasNonHiddenUserPrefs : this.hasNonHiddenUserPrefs,
                       column : this.column,
                       loaded : this.loaded
                },
                contentType: "application/json",
                url: targetResourceUrl + "/gadget/" + this.id,
                beforeSend: function(xhr) {
                    xhr.setRequestHeader("X-HTTP-Method-Override", "PUT");
                }                
            })).throbber({target: AJS.$("#dash-throbber")});
        },

        /**
         * Removes gadget from dashboard and deletes object references
         *
         * @method destroy
         */
        destroy: function () {

            var that = this;

            if (confirm(AG.param.get("areYouSure") + " " + that.title + " " + AG.param.get("gadget"))) {

                AJS.$("#dash-throbber").addClass("loading");

                AJS.$.ajax({
                    type: "POST",
                    url: this.gadgetUrl,
                    beforeSend: function(xhr) {
                        xhr.setRequestHeader("X-HTTP-Method-Override", "DELETE");
                    },
                    complete: function () {
                        AJS.$("#dash-throbber").removeClass("loading");
                    },
                    success: function() {
                        that.$.fadeOut(function () {
                            that.remove();
                            if (that.view === "canvas") {
                                that.getLayoutManager().restoreDefaultView();
                            }
                        });
                    },
                    error: function(request) {
                        if (request.status == 403 || request.status == 401) {
                            alert(AG.param.get("dashboardErrorDashboardPermissions"));
                        }
                        else {
                            alert(AG.param.get("dashboardErrorCouldNotSave"));
                        }
                    }
                });
            }
        },

        /**
         * Sets the layout of the gadget to either canvas or dashboard. Does
         * so by delegating layout actions to LayoutManager.
         *
         * @method setView
         * @param {String} view - Accepts either "canvas" or "dashboard"
         */
        setView: function (view) {
            var
            MAXIMIZED_CLASS = "maximized",
            uri,
            that = this,
            anchor = this.title.replace(/\s/g,"-") + "/" + this.id,
            layoutManager = this.getLayoutManager(),
            rpctoken;


            function toDefaultViewHandler () {
                that.getPublicInstance().setView("default");
            }

            if (this.view === view) {
                return;
            }

            if (view === "canvas" || view === "default") {

                // use rendered url to get latest user prefs and a fresh security token
                AJS.$.ajax({
                    async: false,
                    type: "GET",
                    url: this.gadgetUrl,
                    dataType: "json",
                    success: function(rep){
                        // create the rpctoken that will be used in rpc calls
                        rpctoken = Math.round(Math.random() * 10000000);
                        uri = AJS.parseUri(rep.renderedGadgetUrl + "#rpctoken=" + rpctoken);
                    }
                });

                // setup the iframe to send/receive rpc calls
                gadgets.rpc.setAuthToken(AJS.$("iframe.gadget-iframe", this.$).attr("id"), rpctoken);

                if (view === "canvas") {

                    AJS.$(".operations li", AG.DashboardManager.getDashboard()).toggleClass("hidden");

                    AJS.$.extend(uri.queryKey, { view: "canvas" });
                    AJS.$("iframe.gadget-iframe", this.$).attr("src", uri.toString());

                    layoutManager.getContainer().addClass(MAXIMIZED_CLASS);
                    AJS.$(".gadget-container", this.$).addClass(MAXIMIZED_CLASS);
                    this.$.addClass(MAXIMIZED_CLASS);
                    AJS.$(".aui-icon",this.$).attr("title", AG.param.get("restoreFromCanvasMode"));
                    this.$.layoutRep.addClass(MAXIMIZED_CLASS);
                    this.$.layoutRep.parent().addClass(MAXIMIZED_CLASS);

                    // Not really sure about this, would prefer to add a class to the body tag. Problem is I can't as I
                    // do not want every gadget to be hidden, in the case of multiple tabs this would cause problems.
                    AJS.$.each(this.getLayoutManager().getGadgets(), function () {
                        if (that.getPublicInstance() !== this) {
                            this.getElement().hide();
                        }
                    });

                    AJS.$.extend(uri.queryKey, { view: "canvas" });
                    AJS.$("iframe.gadget-iframe", this.$).attr("src", uri.toString());

                    this.maximize();
                    // add bookmarking capabilities
                    window.location.href = window.location.href.replace(/#.*/, "") + "#" + anchor;
                    AJS.$(".minimize", AG.DashboardManager.getDashboard()).click(toDefaultViewHandler);
                    this.view = "canvas";

                } else {

                    AJS.$(".gadget-container", this.$).removeClass(MAXIMIZED_CLASS);
                    this.$.removeClass(MAXIMIZED_CLASS);
                    AJS.$(".aui-icon",this.$).attr("title", AG.param.get("maximize"));
                    this.$.layoutRep.removeClass(MAXIMIZED_CLASS);
                    this.$.layoutRep.parent().removeClass(MAXIMIZED_CLASS);

                    this.getLayoutManager().restoreDefaultView();

                    AJS.$.extend(uri.queryKey, { view: "default" });
                    AJS.$("iframe.gadget-iframe", this.$).attr("src", uri.toString());
                    this.getLayoutManager().refresh();
                    window.location.href = window.location.href.replace(anchor, "");
                    AJS.$("a.minimize", AG.DashboardManager.getDashboard()).unbind("click", toDefaultViewHandler);
                    this.view = "default";
                }

            }
            else if(AJS.debug) {
                console.warn("AG.Gadget.setView: Ignored! not a valid view. Was supplied '" + view + "' but expected "
                        + "either 'default' or 'canvas'");
            }
        },
    

        /**
         * Displays edit preferences form
         *
         * @method editPrefs
         */
        editPrefs: function () {},

        getPublicInstance: function () {

            var gadget = this;

            if (!this.publicInterface) {
                this.publicInterface = {
                    updatePosition: function () {
                        return gadget.updatePosition.apply(gadget, arguments);
                    },
                    getLayoutManager: function () {
                        return gadget.getLayoutManager.apply(gadget, arguments);
                    },
                    setLayoutManager: function () {
                        return gadget.setLayoutManager.apply(gadget, arguments);
                    },
                    getElement: function () {
                        return gadget.$;
                    },
                    move: function (targetUrl) {
                    	return gadget.move(targetUrl);
                    },
                    remove: function () {
                        return gadget.remove.apply(gadget, arguments);
                    },
                    getId: function () {
                        return gadget.id;
                    },
                    showShim: function () {
                        return gadget.showShim.apply(gadget, arguments);
                    },
                    hideShim: function () {
                        return gadget.hideShim.apply(gadget, arguments);
                    },
                    minimize: function () {
                        return gadget.minimize.apply(gadget, arguments);
                    },
                    maximize: function () {
                        return gadget.minimize.apply(gadget, arguments);
                    },
                    getSecurityToken: function() {
                    	return gadget.securityToken;
                    },
                    setSecurityToken: function(securityToken) {
                    	gadget.securityToken = securityToken;
                    }
                };

                if (this.isMaximizable) {
                    this.publicInterface.setView = function () {
                        return gadget.setView.apply(gadget,  arguments);
                    };
                }
            }

            return this.publicInterface;
        },

        init: function (options) {
            this.COOKIE_MINIMIZE = options.id + ":minimized";
            this.minimized = AG.Cookie.read(this.COOKIE_MINIMIZE) === "true";
            this.title = options.title;
            this.color = options.color;
            this.colorUrl = options.colorUrl;
            this.gadgetUrl = options.gadgetUrl;
            this.id = options.id;
            this.hasNonHiddenUserPrefs = options.hasNonHiddenUserPrefs;
            this.isMaximizable = options.isMaximizable;
            this.titleUrl = options.titleUrl;
            this.gadgetSpecUrl = options.gadgetSpecUrl;
            this.userPrefs = options.userPrefs;
            this.renderedGadgetUrl = options.renderedGadgetUrl;
            this.column = options.column;
            this.loaded = options.loaded;
            this.errorMessage = options.errorMessage;
            this.securityToken = AJS.parseUri(options.renderedGadgetUrl).queryKey["st"];
            this.draw(options);
        }
    };

    AG.Gadget = function (options) {

        // Using prototype as there could be many gadgets on the page. This is most memory efficient.
        var gadget = AJS.clone(Gadget);

        gadget.init(options);

        return gadget.getPublicInstance();
    };

    AG.Gadget.COLORS = [1, 2, 3, 4, 5, 6, 7, 8];

    AG.Gadget.getColorAttrName = function (color) {
        return "color" + color;
    };

    AG.Gadget.isCanvasView = function (gadgetId) {
        var uri = AJS.parseUri(window.location.href);
        return new RegExp(gadgetId).test(uri.anchor);
    };

    AG.Gadget.getNullGadgetRepresentation = function (errorGadget) {
        return AJS.$.extend(errorGadget, {
            title: AG.param.get("errorGadgetTitle"),
            renderedGadgetUrl : AG.param.get("errorGadgetUrl"),
            color: errorGadget.color || "color7"
        });
    };

})();
