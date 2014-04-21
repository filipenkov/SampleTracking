if (typeof jQuery != "undefined") {

    var AJS = (function () {
        var bindings = {click: {}},
            initFunctions = [],
            included = [],
            isInitialised = false;

        var res = {

            params: {},
            /**
            * Returns an HTMLElement reference.
            * @method $
            * @param {String | HTMLElement |Array} el Accepts a string to use as an ID for getting a DOM reference, an actual DOM reference, or an Array of IDs and/or HTMLElements.
            * @return {HTMLElement | Array} A DOM reference to an HTML element or an array of HTMLElements.
            */
            $: jQuery,

            log: function(obj) {
                if (typeof console != "undefined" && console.log)
                    console.log(obj);
            },

            /**
             * Prevent further handling of an event. Returns false, which you should use as the return value of your event handler:
             *     return AJS.stopEvent(e);
             * @param e jQuery event
             */
            stopEvent: function(e) {
                e.stopPropagation();
                return false; // required for JWebUnit pop-up links to work properly
            },
            include: function (url) {
                if (!this.contains(included, url)) {
                    included.push(url);
                    var s = document.createElement("script");
                    s.src = url;
                    this.$("body").append(s);
                }
            },
            /**
            * Shortcut function to toggle class name of an element.
            * @method toggleClassName
            * @param {String | HTMLElement} element The HTMLElement or an ID to toggle class name on.
            * @param {String} className The class name to remove or add.
            */
            toggleClassName: function (element, className) {
                if (!(element = this.$(element))) {
                    return;
                }
                element.toggleClass(className);
            },
            /**
             * Shortcut function adds or removes "hidden" classname to an element based on a passed boolean.
             * @method setVisible
             * @param {String | HTMLElement} element The HTMLElement or an ID to show or hide.
             * @param {boolean} show true to show, false to hide
             */
            setVisible: function (element, show) {
                if (!(element = this.$(element))) {
                    return;
                }
                var $ = this.$; // aliased for use inside function below
                $(element).each(function () {
                    var isHidden = $(this).hasClass("hidden");
                    if (isHidden && show) {
                        $(this).removeClass("hidden");
                    }
                    else if (!isHidden && !show) {
                        $(this).addClass("hidden");
                    }
                });
            },
            /**
             * Shortcut function adds or removes "current" classname to an element based on a passed boolean.
             * @param {String | HTMLElement} element The HTMLElement or an ID to show or hide.
             * @param {boolean} show true to add "current" class, false to remove
             */
            setCurrent: function (element, current) {
                if (!(element = this.$(element))) {
                    return;
                }
                if (current)
                    element.addClass("current");
                else
                    element.removeClass("current");
            },
            /**
             * Shortcut function to see if passed element is currently visible on screen.
             * @method isVisible
             * @param {String | HTMLElement} element The HTMLElement or an jQuery selector to check.
             */
            isVisible: function (element) {
                return !this.$(element).hasClass("hidden");
            },
            /**
            * Runs functions from list (@see toInit) and attach binded funtions (@see bind)
            * @method init
            */
            init: function () {
                var ajs = this;
                this.$(".parameters input").each(function () {
                    var value = this.value,
                        id = this.title || this.id;
                    if (ajs.$(this).hasClass("list")) {
                        if (ajs.params[id]) {
                            ajs.params[id].push(value);
                        } else {
                            ajs.params[id] = [value];
                        }
                    } else {
                        ajs.params[id] = (value.match(/^(tru|fals)e$/i) ? value.toLowerCase() == "true" : value);
                    }
                });
                isInitialised = true;
                AJS.initFunctions = initFunctions;
                for (var i = 0, ii = initFunctions.length; i < ii; i++) {
                    if (typeof initFunctions[i] == "function") {
                        initFunctions[i](AJS.$);
                    }
                }
            },
            /**
            * Adds functions to the list of methods to be run on initialisation. Wraps
            * error handling around the provided function so its failure won't prevent
            * other init functions running.
            * @method toInit
            * @param {Function} func Function to be call on initialisation.
            * @return AJS object.
            */
            toInit: function (func) {
                var ajs = this;
                this.$(function () {
                    try {
                        func.apply(this, arguments);
                    } catch(ex) {
                        ajs.log("Failed to run init function: " + ex);
                    }
                });
                return this;
            },

            /**
            * DEPRECATED instead use AJS.$(element).bind();
            * Binds given function to some object or set of objects as event handlers by class name or id.
            * @method bind
            * @param {String} reference Element or name of the element class. Put "#" in the beginning od the string to use it as id.
            * @param {String} handlerName (optional) Name of the event i.e. "click", "mouseover", etc.
            * @param {Function} func Function to be attached.
            * @return AJS object.
            */
            bind: function () {},

            /**
            * Finds the index of an element in the array.
            * @method indexOf
            * @param item Array element which will be searched.
            * @param fromIndex (optional) the index from which the item will be searched. Negative values will search from the
            * end of the array.
            * @return a zero based index of the element.
            */
            indexOf: function (array, item, fromIndex) {
                var length = array.length;
                if (fromIndex == null) {
                  fromIndex = 0;
                } else {
                    if (fromIndex < 0) {
                      fromIndex = Math.max(0, length + fromIndex);
                    }
                }
                for (var i = fromIndex; i < length; i++) {
                  if (array[i] === item) return i;
                }
                return -1;
            },
            /**
            * Looks for an element inside the array.
            * @method contains
            * @param item Array element which will be searched.
            * @return {Boolean} Is element in array.
            */
            contains: function (array, item) {
                return this.indexOf(array, item) > -1;
            },
            /**
            * Replaces tokens in a string with arguments, similar to Java's MessageFormat.
            * Tokens are in the form {0}, {1}, {2}, etc.
            * @method format
            * @param message the message to replace tokens in
            * @param arg (optional) replacement value for token {0}, with subsequent arguments being {1}, etc.
            * @return {String} the message with the tokens replaced
            * @usage AJS.format("This is a {0} test", "simple");
            */
            format: function (message) {
                var args = arguments;
                return message.replace(/\{(\d+)\}/g, function (str, i) {
                    var replacement = args[parseInt(i, 10) + 1];
                    return replacement != null ? replacement : str;
                });
            },
            /**
            * Includes firebug lite for debugging in IE. Especially in IE.
            * @method firebug
            * @usage Type in addressbar "javascript:alert(AJS.firebug());"
            */
            firebug: function () {
                var script = this.$(document.createElement("script"));
                script.attr("src", "http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js");
                this.$("head").append(script);
                (function () {
                    if (window.firebug) {
                        firebug.init();
                    } else {
                        setTimeout(arguments.callee, 0);
                    }
                })();
            },
            /**
             * Clones the element specified by the selector and removes the id attribute
             * @param selector a jQuery selector
             */
            clone : function(selector) {
                return AJS.$(selector).clone().removeAttr("id");
            },
            /**
            * Compare two strings in alphanumeric way
            * @method alphanum
            * @param {String} a first string to compare
            * @param {String} b second string to compare
            * @return {Number(-1|0|1)} -1 if a < b, 0 if a = b, 1 if a > b
            * @usage a.sort(AJS.alphanum)
            */
            alphanum: function (a, b) {
                var chunks = /(\d+|\D+)/g,
                    am = a.match(chunks),
                    bm = b.match(chunks),
                    len = Math.max(am.length, bm.length);
                for (var i = 0; i < len; i++) {
                    if (i == am.length) {
                        return -1;
                    }
                    if (i == bm.length) {
                        return 1;
                    }
                    var ad = parseInt(am[i], 10),
                        bd = parseInt(bm[i], 10);
                    if (ad == am[i] && bd == bm[i] && ad != bd) {
                        return (ad - bd) / Math.abs(ad - bd);
                    }
                    if ((ad != am[i] || bd != bm[i]) && am[i] != bm[i]) {
                        return am[i] < bm[i] ? -1 : 1;
                    }
                }
                return 0;
            },
            dim: function () {
                if (AJS.dim.dim) {
                    AJS.dim.dim.remove();
                    AJS.dim.dim = null;
                } else {
                    AJS.dim.dim = AJS("div").css({
                        width: "100%",
                        height: AJS.$(document).height(),
                        background: "#000",
                        opacity: .5,
                        position: "absolute",
                        top: 0,
                        left: 0
                    });
                    AJS.$("body").append(AJS.dim.dim);
                }
            },
            onTextResize: function (f) {
                if (typeof f == "function") {
                    if (AJS.onTextResize["on-text-resize"]) {
                        AJS.onTextResize["on-text-resize"].push(function (emsize) {
                            f(emsize);
                        });
                    } else {
                        var em = AJS("div");
                        em.css({
                            width: "1em",
                            height: "1em",
                            position: "absolute",
                            top: "-9999em",
                            left: "-9999em"
                        });
                        this.$("body").append(em);
                        em.size = em.width();
                        setInterval(function () {
                            if (em.size != em.width()) {
                                em.size = em.width();
                                for (var i = 0, ii = AJS.onTextResize["on-text-resize"].length; i < ii; i++) {
                                    AJS.onTextResize["on-text-resize"][i](em.size);
                                };
                            }
                        }, 0);
                        AJS.onTextResize.em = em;
                        AJS.onTextResize["on-text-resize"] = [function (emsize) {
                            f(emsize);
                        }];
                    }
                }
            },
            unbindTextResize: function (f) {
                for (var i = 0, ii = AJS.onTextResize["on-text-resize"].length; i < ii; i++) {
                    if (AJS.onTextResize["on-text-resize"][i] == f) {
                        return AJS.onTextResize["on-text-resize"].splice(i, 1);
                    }
                };
            },
            escape: function (string) {
                return escape(string).replace(/%u\w{4}/gi, function (w) {
                    return unescape(w);
                });
            },

            /**
             * Filters a list of entries by a passed search term.
             *
             * Options :
             *   - "keywordsField" - name of entry field containing keywords, default "keywords"
             *   - "ignoreForCamelCase" - ignore search case for camel case, e.g. CB matches Code Block *and* Code block
             *   - "matchBoundary" - match words only at boundary, e.g. link matches "linking" but not "hyperlinks"
             *   - "splitRegex" - regex to split search words, instead of on whitespace
             *
             * @param entries an index array of objects with a "keywords" property
             * @param search one or more words to search on, which may include camel-casing.
             * @param options - optional - specifiy to override default behaviour
             */
            filterBySearch : function(entries, search, options) {
                if (search == "") return [];   // search for nothing, get nothing - up to calling code to handle.

                var $ = this.$;
                var keywordsField = (options && options.keywordsField) || "keywords";
                var camelCaseFlags = (options && options.ignoreForCamelCase) ? "i" : "";
                var boundaryFlag  = (options && options.matchBoundary) ? "\\b" : "";
                var splitRegex = (options && options.splitRegex) || /\s+/;

                // each word in the input is considered a distinct filter that has to match a keyword in the record
                var filterWords = search.split(splitRegex);
                var filters = [];
                $.each(filterWords, function () {
                  var subfilters = [new RegExp(boundaryFlag + this, "i")]; // anchor on word boundaries
                  if (/^([A-Z][a-z]*){2,}$/.test(this)) { // split camel-case into separate words
                      var camelRegexStr = this.replace(/([A-Z][a-z]*)/g, "\\b$1[^,]*");
                      subfilters.push(new RegExp(camelRegexStr, camelCaseFlags));
                  }
                  filters.push(subfilters);
                });
                var result = [];
                $.each(entries, function () {
                    for (var i = 0; i < filters.length; i++) {
                        var somethingMatches = false;
                        for (var j = 0; j < filters[i].length; j++) {
                            if (filters[i][j].test(this[keywordsField])) {
                                somethingMatches = true;
                                break;
                            }
                        }
                        if (!somethingMatches) return;
                    }
                    result.push(this);
                });
                return result;
            }
        };
        if (typeof AJS != "undefined") {
            for (var i in AJS) {
                res[i] = AJS[i];
            }
        }
        /**
        * Creates DOM object
        * @method AJS
        * @param {String} element tag name
        * @return {jQuery object}
        * @usage var a = AJS("div");
        */
        var result = function () {
            var res = null;
            if (arguments.length && typeof arguments[0] == "string") {
                res = arguments.callee.$(document.createElement(arguments[0]));
                if (arguments.length == 2) {
                    res.html(arguments[1]);
                }
            }
            return res;
        };
        for (var i in res) {
            result[i] = res[i];
        }
        return result;
    })();

    AJS.$(function () {AJS.init();});
}

if (typeof console == "undefined") {
    console = {
        messages: [],
        log: function (text) {
            this.messages.push(text);
        },
        show: function () {
            alert(this.messages.join("\n"));
            this.messages = [];
        }
    };
}
else {
    // Firebug console - show not required to do anything.
    console.show = function(){};
}
/**
 * Covers screen with semitransparent DIV
 * @method dim
 * @namespace AJS
*/
AJS.dim = function () {
    if (!AJS.dim.dim) {
        AJS.dim.dim = AJS("div").addClass("blanket");
        if (AJS.$.browser.msie) {
            AJS.dim.dim.css({width: "200%", height: Math.max(AJS.$(document).height(), AJS.$(window).height()) + "px"});
        }
        AJS.$("body").append(AJS.dim.dim).css("overflow", "hidden");
        AJS.$("html").css("overflow", "hidden");
    }
};
/**
 * Removes semitransparent DIV
 * @method undim
 * @namespace AJS
 * @see dim
*/
AJS.undim = function () {
    if (AJS.dim.dim) {
        AJS.dim.dim.remove();
        AJS.dim.dim = null;
        AJS.$("html, body").css("overflow", "");
        // Safari bug workaround
        if (AJS.$.browser.safari) {
            AJS.$("body").css({height: "200px"});
            setTimeout(function () {
                AJS.$("body").css({height: ""});
            }, 0);
        }
    }
};
/**
 * Creates a generic popup
 * @method poup
 * @namespace AJS
 * @param width {number} width of the popup
 * @param height {number} height of the popup
 * @param id {number} [optional] id of the popup
 * @return {object} popup object
*/
AJS.popup = function (width, height, id) {
    var shadow = AJS.$('<div class="shadow"><div class="tl"></div><div class="tr"></div><div class="l"></div><div class="r"></div><div class="bl"></div><div class="br"></div><div class="b"></div></div>');
    var popup = AJS("div").addClass("popup").css({
        margin: "-" + Math.round(height / 2) + "px 0 0 -" + Math.round(width / 2) + "px",
        width: width + "px",
        height: height + "px",
        background: "#fff"
    });
    if (id) {
        popup.attr("id", id);
    }
    AJS.$("body").append(shadow);
    shadow.css({
        margin: "-" + Math.round(height / 2) + "px 0 0 -" + Math.round(width / 2 + 16) + "px",
        width: width + 32 + "px",
        height: height + 29 + "px"
    });
    AJS.$(".b", shadow).css("width", width - 26 + "px");
    AJS.$(".l, .r", shadow).css("height", height - 18 + "px");
    AJS.$("body").append(popup);
    popup.hide();
    shadow.hide();

    /**
     * Popup object
     * @class Popup
     * @static
    */
    return {
        /**
         * Makes popup visible
         * @method show
        */
        show: function () {
            var show = function () {
                scrollDistance = document.documentElement.scrollTop || document.body.scrollTop;
                popup.show();
                shadow.show();
                AJS.dim();
            };
            show();
            if (popup.css("position") == "absolute") {
                // Internet Explorer case
                var scrollfix = function () {
                    scrollDistance = document.documentElement.scrollTop || document.body.scrollTop;
                    var marginTop = scrollDistance + (document.documentElement.clientHeight - height)/2;
                    popup.css("margin-top", marginTop);
                    shadow.css("margin-top", marginTop);
                };
                scrollfix();
                AJS.$(window).load(scrollfix);
                this.show = function () {
                    show();
                    scrollfix();
                };
            } else {
                this.show = show;
            }
        },
        /**
         * Makes popup invisible
         * @method hide
        */
        hide: function () {
            this.element.hide();
            shadow.hide();
            AJS.undim();
        },
        /**
         * jQuery object, representing popup DOM element
         * @property element
        */
        element: popup,
        /**
         * Removes popup elements from the DOM
         * @method remove
        */
        remove: function () {
            shadow.remove();
            popup.remove();
            this.element = null;
        }
    };
};



// Usage:
// var popup = new AJS.Dialog(860, 530);
// popup.addHeader("Insert Macro");
// popup.addPanel("All", "<p></p>");
// popup.addButton("Next", function (dialog) {dialog.nextPage();});
// popup.addButton("Cancel", function (dialog) {dialog.hide();});
// popup.addPage();
// popup.page[1].addButton("Cancel", function (dialog) {dialog.hide();});
// somebutton.click(function () {popup.show();});


// Scoping function
(function () {
    /**
     * @class Button
     * @constructor Button
     * @param page {number} page id
     * @param label {string} button label
     * @param onclick {function} [optional] click event handler
     * @param className {string} [optional] class name
     * @private
    */
    function Button(page, label, onclick, className) {
        if (!page.buttonpanel) {
            page.buttonpanel = AJS("div").addClass("dialog-button-panel");
            page.element.append(page.buttonpanel);
        }
        this.page = page;
        this.onclick = onclick;
        this._onclick = function () {
            onclick.call(this, page.dialog, page);
        };
        this.item = AJS("button", label);
        if (className) {
            this.item.addClass(className);
        }
        if (typeof onclick == "function") {
            this.item.click(this._onclick);
        }
        page.buttonpanel.append(this.item);
        this.id = page.button.length;
        page.button[this.id] = this;
    }
    function itemMove (leftOrRight, target) {
        var dir = leftOrRight == "left"? -1 : 1;
        return function (step) {
            var dtarget = this.page[target];
            if (this.id != ((dir == 1) ? dtarget.length - 1 : 0)) {
                dir *= (step || 1);
                dtarget[this.id + dir].item[(dir < 0 ? "before" : "after")](this.item);
                dtarget.splice(this.id, 1);
                dtarget.splice(this.id + dir, 0, this);
                for (var i = 0, ii = dtarget.length; i < ii; i++) {
                    if (target == "panel" && this.page.curtab == dtarget[i].id) {
                        this.page.curtab = i;
                    }
                    dtarget[i].id = i;
                }
            }
            return this;
        };
    };
    function itemRemove(target) {
        return function () {
            this.page[target].splice(this.id, 1);
            for (var i = 0, ii = this.page[target].length; i < ii; i++) {
                this.page[target][i].id = i;
            }
            this.item.remove();
        };
    }
    /**
     * Moves item left in the hierarchy
     * @method moveUp
     * @method moveLeft
     * @param step {number} how many items to move, default is 1
     * @return {object} button
    */
    Button.prototype.moveUp = Button.prototype.moveLeft = itemMove("left", "button");
    /**
     * Moves item right in the hierarchy
     * @method moveDown
     * @method moveRight
     * @param step {number} how many items to move, default is 1
     * @return {object} button
    */
    Button.prototype.moveDown = Button.prototype.moveRight = itemMove("right", "button");
    /**
     * Removes item
     * @method remove
    */
    Button.prototype.remove = itemRemove("button");

    /**
     * Getter and setter for label
     * @method label
     * @param label {string} [optional] label of the button
     * @return {string} label, if nothing is passed in
     * @return {object} jQuery button object, if label is passed in
    */
    Button.prototype.html = function (label) {
        return this.item.html(label);
    };
    /**
     * Getter and setter of onclick event handler
     * @method onclick
     * @param onclick {function} [optional] new event handler, that is going to replace the old one
     * @return {function} existing event handler if new one is undefined
    */
    Button.prototype.onclick = function (onclick) {
        if (typeof onclick == "undefined") {
            return this.onclick;
        } else {
            this.item.unbind("click", this._onclick);
            this._onclick = function () {
                onclick.call(this, page.dialog, page);
            };
            if (typeof onclick == "function") {
                this.item.click(this._onclick);
            }
        }
    };

    /**
     * Class for panels
     * @class Panel
     * @constructor
     * @param page {number} page id
     * @param title {string} panel title
     * @param reference {string} or {object} jQuery object or selector for the contents of the Panel
     * @param className {string} [optional] HTML class name
     * @private
    */
    var Panel = function (page, title, reference, className) {
        if (!(reference instanceof AJS.$)) {
            reference = AJS.$(reference);
        }
        this.dialog = page.dialog;
        this.page = page;
        this.id = page.panel.length;
        this.button = AJS("button").html(title);
        this.item = AJS("li").append(this.button);
        this.body = AJS("div").append(reference).addClass("dialog-panel-body").css("height", page.dialog.height + "px");
        this.padding = 10;
        if (className) {
            this.body.addClass(className);
        }
        var i = page.panel.length,
            tab = this;
        page.menu.append(this.item);
        page.body.append(this.body);
        page.panel[i] = this;
        var onclick = function () {
            var cur;
            if (page.curtab + 1) {
                cur = page.panel[page.curtab];
                cur.body.hide();
                cur.item.removeClass("selected");
                (typeof cur.onblur == "function") && cur.onblur();
            }
            page.curtab = tab.id;
            tab.body.show();
            tab.item.addClass("selected");
            (typeof tab.onselect == "function") && tab.onselect();
            (typeof page.ontabchange == "function") && page.ontabchange(tab, cur);
        };
        if (!this.button.click) {
            AJS.log("atlassian-dialog:Panel:constructor - this.button.click falsy");
            this.button.onclick = onclick;
        }
        else {
            this.button.click(onclick);
        }
        onclick();
        if (i == 0) {
            page.menu.css("display", "none"); // don't use jQuery hide()
        } else {
            page.menu.show();
        }
    };
    /**
     * Selects current panel
     * @method select
    */
    Panel.prototype.select = function () {
        this.button.click();
    };
    /**
     * Moves item left in the hierarchy
     * @method moveUp
     * @method moveLeft
     * @param step {number} how many items to move, default is 1
     * @return {object} panel
    */
    Panel.prototype.moveUp = Panel.prototype.moveLeft = itemMove("left", "panel");
    /**
     * Moves item right in the hierarchy
     * @method moveDown
     * @method moveRight
     * @param step {number} how many items to move, default is 1
     * @return {object} panel
    */
    Panel.prototype.moveDown = Panel.prototype.moveRight = itemMove("right", "panel");
    /**
     * Removes item
     * @method remove
    */
    Panel.prototype.remove = itemRemove("panel");
    /**
     * Getter and setter of inner HTML of the panel
     * @method html
     * @param html {string} HTML source to set up
     * @return {object} panel
     * @return {string} current HTML source
    */
    Panel.prototype.html = function (html) {
        if (html) {
            this.body.html(html);
            return this;
        } else {
            return this.body.html();
        }
    };
    /**
     * Default padding is 10 px. This method gives you ability to overwrite default value. Use it with caution.
     * @method setPadding
     * @param padding {number} padding in pixels
     * @return {object} panel
    */
    Panel.prototype.setPadding = function (padding) {
        if (!isNaN(+padding)) {
            this.body.css("padding", +padding);
            this.padding = +padding;
            this.page.recalcSize();
        }
        return this;
    };


    /**
     * Class for pages
     * @class Page
     * @constructor
     * @param dialog {object} dialog object
     * @param className {string} [optional] HTML class name
     * @private
    */
    var Page = function (dialog, className) {
        this.dialog = dialog;
        this.id = dialog.page.length;
        this.element = AJS("div").addClass("dialog-components");
        this.body = AJS("div").addClass("page-body");
        this.menu = AJS("ul").addClass("page-menu").css("height", dialog.height + "px");
        this.body.append(this.menu);
        this.curtab;
        this.panel = [];
        this.button = [];
        if (className) {
            this.body.addClass(className);
        }
        dialog.popup.element.append(this.element.append(this.menu).append(this.body));
        dialog.page[dialog.page.length] = this;
    };
    /**
     * Size updater for contents of the page. For internal use
     * @method recalcSize
    */
    Page.prototype.recalcSize = function () {
        var headerHeight = this.header ? 43 : 0;
        var panelHeight = this.buttonpanel ? 43 : 0;
        for (var i = this.panel.length; i--;) {
            this.panel[i].body.css("height", this.dialog.height - headerHeight - panelHeight - this.panel[i].padding * 2 + "px");
        }
    };
    /**
     * Method for adding new panel to the page
     * @method addPanel
     * @param title {string} panel title
     * @param reference {string} or {object} jQuery object or selector for the contents of the Panel
     * @param className {string} [optional] HTML class name
     * @return {object} the page
    */
    Page.prototype.addPanel = function (title, reference, className) {
        new Panel(this, title, reference, className);
        return this;
    };
    /**
     * Method for adding header to the page
     * @method addHeader
     * @param title {string} panel title
     * @param className {string} [optional] HTML class name
     * @return {object} the page
    */
    Page.prototype.addHeader = function (title, className) {
        if (this.header) {
            this.header.remove();
        }
        this.header = AJS("h2").html(title);
        className && this.header.addClass(className);
        this.element.prepend(this.header);
        this.recalcSize();
        return this;
    };
    /**
     * Method for adding new button to the page
     * @method addButton
     * @param label {string} button label
     * @param onclick {function} [optional] click event handler
     * @param className {string} [optional] class name
     * @return {object} the page
    */
    Page.prototype.addButton = function (label, onclick, className) {
        new Button(this, label, onclick, className);
        this.recalcSize();
        return this;
    };
    /**
     * Selects corresponding panel
     * @method gotoPanel
     * @param panel {object} panel object
     * @param panel {number} id of the panel
    */
    Page.prototype.gotoPanel = function (panel) {
        this.panel[panel.id || panel].select();
    };
    /**
     * Returns current panel on the page
     * @method getCurrentPanel
     * @return panel {object} the panel
    */
    Page.prototype.getCurrentPanel = function () {
        return this.panel[this.curtab];
    };
    /**
     * Hides the page
     * @method hide
    */
    Page.prototype.hide = function () {
        this.element.hide();
    };
    /**
     * Shows the page, if it was hidden
     * @method show
    */
    Page.prototype.show = function () {
        this.element.show();
    };
    /**
     * Removes the page
     * @method remove
    */
    Page.prototype.remove = function () {
        this.element.remove();
    };



    /**
     * Class for dialog
     * @class Dialog
     * @namespace AJS
     * @constructor
     * @param width {number} dialog width in pixels
     * @param height {number} dialog height in pixels
     * @param id {number} [optional] dialog id
     * @private
    */
    AJS.Dialog = function (width, height, id) {
        this.height = height || 480;
        this.width = width || 640;
        this.id = id;
        this.popup = AJS.popup(this.width, this.height, this.id);

        this.popup.element.addClass("dialog");
        this.page = [];
        this.curpage = 0;
        new Page(this);
    };
    /**
     * Method for adding header to the current page
     * @method addHeader
     * @param title {string} panel title
     * @param className {string} [optional] HTML class name
     * @return {object} the dialog
    */
    AJS.Dialog.prototype.addHeader = function (title, className) {
        this.page[this.curpage].addHeader(title, className);
        return this;
    };
    /**
     * Method for adding new button to the current page
     * @method addButton
     * @param label {string} button label
     * @param onclick {function} [optional] click event handler
     * @param className {string} [optional] class name
     * @return {object} the dialog
    */
    AJS.Dialog.prototype.addButton = function (label, onclick, className) {
        this.page[this.curpage].addButton(label, onclick, className);
        return this;
    };
    /**
     * Method for adding new panel to the current page
     * @method addPanel
     * @param title {string} panel title
     * @param reference {string} or {object} jQuery object or selector for the contents of the Panel
     * @param className {string} [optional] HTML class name
     * @return {object} the dialog
    */
    AJS.Dialog.prototype.addPanel = function (title, reference, className) {
        this.page[this.curpage].addPanel(title, reference, className);
        return this;
    };
    /**
     * Method for adding new page
     * @method addPage
     * @param className {string} [optional] HTML class name
     * @return {object} the dialog
    */
    AJS.Dialog.prototype.addPage = function (className) {
        new Page(this, className);
        this.page[this.curpage].hide();
        this.curpage = this.page.length - 1;
        return this;
    };
    /**
     * Making next page in hierarchy visible and active
     * @method nextPage
     * @return {object} the dialog
    */
    AJS.Dialog.prototype.nextPage = function () {
        this.page[this.curpage++].hide();
        if (this.curpage >= this.page.length) {
            this.curpage = 0;
        }
        this.page[this.curpage].show();
        return this;
    };
    /**
     * Making previous page in hierarchy visible and active
     * @method prevPage
     * @return {object} the dialog
    */
    AJS.Dialog.prototype.prevPage = function () {
        this.page[this.curpage--].hide();
        if (this.curpage < 0) {
            this.curpage = this.page.length - 1;
        }
        this.page[this.curpage].show();
        return this;
    };
    /**
     * Making specified page visible and active
     * @method gotoPage
     * @param num {number} page id
     * @return {object} the dialog
    */
    AJS.Dialog.prototype.gotoPage = function (num) {
        this.page[this.curpage].hide();
        this.curpage = num;
        if (this.curpage < 0) {
            this.curpage = this.page.length - 1;
        } else if (this.curpage >= this.page.length) {
            this.curpage = 0;
        }
        this.page[this.curpage].show();
        return this;
    };
    /**
     * Returns specified panel at the current page
     * @method getPanel
     * @param pageorpanelId {number} page id or panel id
     * @param panelId {number} panel id
     * @return {object} the panel
    */
    AJS.Dialog.prototype.getPanel = function (pageorpanelId, panelId) {
        var pageid = (panelId == null) ? this.curpage : pageorpanelId;
        if (panelId == null) {
            panelId = pageorpanelId;
        }
        return this.page[pageid].panel[panelId];
    };
    /**
     * Returns specified page
     * @method getPage
     * @param pageid {number} page id
     * @return {object} the page
    */
    AJS.Dialog.prototype.getPage = function (pageid) {
        return this.page[pageid];
    };
    /**
     * Returns current panel at the current page
     * @method getCurrentPanel
     * @return {object} the panel
    */
    AJS.Dialog.prototype.getCurrentPanel = function () {
        return this.page[this.curpage].getCurrentPanel();
    };

    /**
     * Selects corresponding panel
     * @method gotoPanel
     * @param pageorpanel {object} panel object or page object
     * @param panel {object} panel object
     * @param panel {number} id of the panel
    */
    AJS.Dialog.prototype.gotoPanel = function (pageorpanel, panel) {
        if (panel != null) {
            var pageid = pageorpanel.id || pageorpanel;
            this.gotoPage(pageid);
        }
        this.page[this.curpage].gotoPanel(typeof panel == "undefined" ? pageorpanel : panel);
    };

    /**
     * Shows the dialog, if it is not visible
     * @method hide
    */
    AJS.Dialog.prototype.show = function () {
        this.popup.show();
        return this;
    };
    /**
     * Hides the dialog, if it was visible
     * @method hide
    */
    AJS.Dialog.prototype.hide = function () {
        this.popup.hide();
        return this;
    };
    /**
     * Removes the dialog
     * @method remove
    */
    AJS.Dialog.prototype.remove = function () {
        this.popup.hide();
        this.popup.remove();
    };
    /**
     * Gets set of items depending on query
     * @method get
     * @param query {string} query to search for panels, pages, headers or buttons
    */
    AJS.Dialog.prototype.get = function (query) {
        var coll = [],
            dialog = this;
        (query + "").replace(/(?:,|^)\s*(?:(page|panel|button|header)(?:#([^ ]*)|:(\d+))?|#([^ ]*))(?:\s+(?:(page|panel|button|header)(?:#([^ ]*)|:(\d+))?|#([^ ]*)))?\s*(?=,|$)/ig, function (all, name, title, id, justtitle, name2, title2, id2, justtitle2) {
            name = name && name.toLowerCase();
            var pages = [];
            if (name == "page" && dialog.page[id]) {
                pages.push(dialog.page[id]);
                name = name2;
                name = name && name.toLowerCase();
                title = title2;
                id = id2;
                justtitle = justtitle2;
            } else {
                pages = dialog.page;
            }
            if (name || justtitle) {
                for (var i = pages.length; i--;) {
                    if (justtitle || (name == "panel" && (title || (!title && id == null)))) {
                        for (var j = pages[i].panel.length; j--;) {
                            if (pages[i].panel[j].button.html() == justtitle || pages[i].panel[j].button.html() == title || (name == "panel" && !title && id == null)) {
                                coll.push(pages[i].panel[j]);
                            }
                        }
                    }
                    if (justtitle || (name == "button" && (title || (!title && id == null)))) {
                        for (var j = pages[i].button.length; j--;) {
                            if (pages[i].button[j].item.html() == justtitle || pages[i].button[j].item.html() == title || (name == "button" && !title && id == null)) {
                                coll.push(pages[i].button[j]);
                            }
                        }
                    }
                    if (pages[i][name] && pages[i][name][id]) {
                        coll.push(pages[i][name][id]);
                    }
                    if (name == "header" && pages[i].header) {
                        coll.push(pages[i].header);
                    }
                }
            } else {
                coll = coll.concat(pages);
            }
        });
        var res = {
            length: coll.length
        };
        for (var i = coll.length; i--;) {
            res[i] = coll[i];
            for (var method in coll[i]) {
                if (!(method in res)) {
                    (function (m) {
                        res[m] = function () {
                            for (var j = this.length; j--;) {
                                if (typeof this[j][m] == "function") {
                                    this[j][m].apply(this[j], arguments);
                                }
                            }
                        };
                    })(method);
                }
            }
        }
        return res;
    };
})();


/*global AJS, document, setTimeout */

AJS.dropDown = function (obj, options) {

    var dd = null,
        result = [],
        $doc = AJS.$(document),
        isAdditionalProperty = function (name) {
            return !((name == "href") || (name == "name") || (name == "className") || (name == "icon"));
        };

    options = options || {};


    if (obj && obj.jquery) { // if AJS.$
        dd = obj;

    } else if (typeof obj == "string") { // if AJS.$ selector
        dd = AJS.$(obj);
    } else if (obj && obj.constructor == Array) { // if JSON
        dd = AJS("ul").attr("class", (options.isVisibleByDefault ? "hidden" : "") + "ajs-drop-down");
        for (var i = 0, ii = obj.length; i < ii; i++) {
            var ol = AJS("ol");
            for (var j = 0, jj = obj[i].length; j < jj; j++) {
                var li = AJS("li");
                if (obj[i][j].href) {
                    // any additional attributes (beyond those expected) on the JSON objects will be added as
                    // i elements with a class name matching their attribute name
                    var additionalVarsText = "";
                    for (var additionalVar in obj[i][j]) {
                        if (isAdditionalProperty(additionalVar) && !obj[i][j][additionalVar]) {
                            additionalVarsText = additionalVarsText + "<i class='" + additionalVar + "'>" + obj[i][j][additionalVar] + "</i>";
                        }
                    }

                    li.append(AJS("a")
                        .html("<span>" + obj[i][j].name + additionalVarsText + "</span>")
                        .attr({href:  obj[i][j].href})
                        .addClass(obj[i][j].className));
                } else {
                    li.html(obj[i][j].html).addClass(obj[i][j].className);
                }
                if (obj[i][j].icon) {
                    li.prepend(AJS("img").attr("src", obj[i][j].icon));
                }
                ol.append(li);
            }
            if (i == ii - 1) {
                ol.addClass("last");
            }
            dd.append(ol);
        }
        AJS.$("body").append(dd);
    } else {
        throw new Error("AJS.dropDown function was called with illegal parameter. Should be AJS.$ object, AJS.$ selector or array.");
    }





    var movefocus = function (e) {
        if (!AJS.dropDown.current) {
            return true;
        }
        var c = e.which,
            cdd = AJS.dropDown.current.$[0],
            focus = (typeof cdd.focused == "number" ? cdd.focused : -1);
			AJS.dropDown.current.cleanFocus();
       		cdd.focused = focus;
        switch (c) {
			case 40: {
				cdd.focused++;
				break;
			}
			case 9:
			case 39: {
				return false;
			}
			case 37: {
				return false;
			}
			case 38:{
				cdd.focused--;
				break;
			}
			case 27:{
				AJS.dropDown.current.hide("escape");
				return false;
			}
			case 13:{
				options.selectionHandler.call(AJS.dropDown.current, e, AJS.$(AJS.dropDown.current.links[cdd.focused]));
				return false;
			}
			default:{
				if (AJS.dropDown.current.links.length) {
					AJS.$(AJS.dropDown.current.links[cdd.focused]).addClass("active");
				}
				return true;
			}
		}
        if (cdd.focused < 0) {
            cdd.focused = AJS.dropDown.current.links.length - 1;
        }
        if (cdd.focused > AJS.dropDown.current.links.length - 1) {
            cdd.focused = 0;
        }
        if (AJS.dropDown.current.links.length) {
			AJS.$(AJS.dropDown.current.links[cdd.focused]).addClass("active");
        }
        e.stopPropagation();
        e.preventDefault();
        return false;
    };
    var hider = function (e) {
        if (!((e && e.which && (e.which == 3)) || (e && e.button && (e.button == 2)) || false)) { // right click check
            if (AJS.dropDown.current) {
                AJS.dropDown.current.hide("click");
            }
        }
    };
    var active = function (i) {
        return function () {
            if (!AJS.dropDown.current) {
                return;
            }
            AJS.dropDown.current.cleanFocus();
            this.originalClass = this.className;
			AJS.$(this).addClass("active");
            AJS.dropDown.current.$[0].focused = i;
        };
    };
    dd.each(function () {
        var cdd = this, $cdd = AJS.$(this), res;
        var methods = {
			reset: function () {
				res = AJS.$.extend(res || {}, {
					$: $cdd,
	                links: AJS.$(options.item, cdd),
					cleanFocus: function () {
		                if (cdd.focused + 1 && res.links.length) {
							AJS.$(res.links[cdd.focused]).removeClass("active");
		                }
		                cdd.focused = -1;
		            }
				});
		        res.links.each(function (i) {
					AJS.$(this).hover(active(i), res.cleanFocus);
					AJS.$(this).click(function (e) {
						if (AJS.dropDown.current) {
							options.selectionHandler.call(AJS.dropDown.current, e, AJS.$(this));
						}
					});
				});
				return arguments.callee;
			}(),
            appear: function (dir) {
                if (dir) {
                    $cdd.removeClass("hidden");
                } else {
                    $cdd.addClass("hidden");
                }
            },
            fade: function (dir) {
                if (dir) {
                    $cdd.fadeIn("fast");
                } else {
                    $cdd.fadeOut("fast");
                }
            },
            scroll: function (dir) {
                if (dir) {
                    $cdd.slideDown("fast");
                } else {
                    $cdd.slideUp("fast");
                }
            }
        };

        /**
		 * Uses Aspect Oriented Programming (AOP) to insert callback <em>after</em> the
		 * specified method has returned @see AJS.$.aop
		 * @method addCallback
		 * @param {String} methodName - Name of a public method
		 * @param {Function} callback - Function to be executed
		 * @return {Array} weaved aspect
		 */
		res.addCallback = function (method, callback) {
			return AJS.$.aop.after({target: this, method: method}, callback);
		};

		res.reset = methods.reset();

        res.show = function (method) {
            hider();
            AJS.dropDown.current = this;
            this.method = method || this.method || "appear";
            methods[this.method](true);
            this.timer = setTimeout(function () {
                $doc.click(hider);
            }, 0);
            $doc.keydown(movefocus);
			if (options.firstSelected && this.links[0]) {
				active(0).call(this.links[0]);
			}
            AJS.$(cdd.offsetParent).css({zIndex: 2000});
        };
        res.hide = function (causer) {
            this.method = this.method || "appear";
            AJS.$($cdd.get(0).offsetParent).css({zIndex: ""});
            this.cleanFocus();
            methods[this.method](false);
            $doc.unbind("click", hider).unbind("keydown", movefocus);
            AJS.dropDown.current = null;
            return causer;
        };
		res.addCallback("reset", function () {
			if (options.firstSelected && this.links[0]) {
				active(0).call(this.links[0]);
			}
		});

		if (!AJS.dropDown.iframes) {
		    AJS.dropDown.iframes = [];
		}
		AJS.dropDown.createShims = function () {
            AJS.$("iframe").each(function (idx) {
               var iframe = this;
                if (!iframe.shim) {
                    iframe.shim = AJS.$("<div />")
                                                  .addClass("shim hidden")
                                                  .appendTo("body");
                    AJS.dropDown.iframes.push(iframe);
                }
            });
		    return arguments.callee;
	    }();

	    res.addCallback("show", function() {
            AJS.$(AJS.dropDown.iframes).each(function(){
                var $this = AJS.$(this);
                if ($this.is(":visible")) {
                    var offset = $this.offset();
                    offset.height = $this.height();
                    offset.width = $this.width();
                    this.shim.css({
                        left: offset.left + "px",
                        top: offset.top + "px",
                        height: offset.height + "px",
                        width: offset.width + "px"
                    }).removeClass("hidden");
                }
            });
        });
        res.addCallback("hide", function () {
            AJS.$(AJS.dropDown.iframes).each(function(){
                this.shim.addClass("hidden");
            });
        });

        // shadow
        (function () {
            var refreshShadow = function () {

                if (this.$.is(":visible")) {
                    if (!this.shadow) {
                        this.shadow = AJS.$('<div class="aui-shadow"><div class="tl"></div><div class="tr"></div><div class="l"></div><div class="r"></div><div class="bl"></div><div class="br"></div><div class="b"></div></div>').insertBefore(this.$);
                    }
                    if (parseInt(this.$.outerWidth(), 10) > 14) {
                        this.shadow.css({
                            display: "block",
                            top: this.$.css("top"),
                            right: "-7px",
                            width: this.$.outerWidth() + 14 + "px",
                            height: this.$.outerHeight() + 14 + "px"
                        })
                        .find(".b").css("width", this.$.outerWidth() - 14 + "px");
                        this.shadow.find(".l, .r").css("height", this.$.outerHeight() - 8 + "px");
                    }
                }
            };
            res.addCallback("reset", refreshShadow);
            res.addCallback("show", refreshShadow);
            res.addCallback("hide", function () {
                if (this.shadow) {
                    this.shadow.css({display: "none"});
                }
            });
        })();
        result.push(res);
    });
    return result;
};

// for each item in the drop down get the value of the named additional property. If there is no
// property with the specified name then null will be returned.
AJS.dropDown.getAdditionalPropertyValue = function (item, name) {
    var spaceNameElement = AJS.$("i." + name, item);
    if (spaceNameElement.length === 0) {
        return null;
    } else {
        return spaceNameElement.text();
    }
};

// remove all additional properties
AJS.dropDown.removeAllAdditionalProperties = function (item) {
    AJS.$("i", item).remove();
};

 /**
  * Base dropdown control. Enables you to identify triggers that when clicked, display dropdown.
  *
  * @class Standard
  * @contructor
  * @namespace AJS.dropDown
  * @param {Object} options
  * @return {Object
  */
 AJS.dropDown.Standard = function (usroptions) {

    var res = [], dropdownParents, options = {
        selector: ".aui-dd-parent",
		dropDown: ".aui-dropdown",
		trigger: ".aui-dd-trigger",
		item: "li:has(a)",
        activeClass: "active",
        selectionHandler: function (e, selected) {
            if (selected) {
                if (selected.get(0).nodeName.toLowerCase() !== "a") {
                    window.location = selected.find("a").attr("href");
                } else {
                    window.location = selected.attr("href");
                }
            }
        }
	};

     // extend defaults with user options
    AJS.$.extend(options, usroptions);

      // handling for jQuery collections
    if (this instanceof AJS.$) {
        dropdownParents = this;
    // handling for selectors
    } else {
        dropdownParents = AJS.$(options.selector);
    }

    // a series of checks to ensure we are dealing with valid dropdowns
    dropdownParents = dropdownParents
            .not(".dd-allocated")
            .filter(":has(" + options.dropDown + ")")
            .filter(":has(" + options.trigger + ")");

    dropdownParents.each(function () {
        var
        $parent = AJS.$(this),
        $dropdown = AJS.$(options.dropDown, this),
        $trigger = AJS.$(options.trigger, this),
        ddcontrol = AJS.dropDown($dropdown, options)[0];

        // extend to control to have any additional properties/methods
        AJS.$.extend(ddcontrol, {trigger: $trigger});

        // flag it to prevent additional dd controls being applied
        $parent.addClass("dd-allocated");

        //hide dropdown if not already hidden
        $dropdown.addClass("hidden");


        $trigger.click(function (e) {
            if (ddcontrol != AJS.dropDown.current) {
                $dropdown.css({top: $trigger.outerHeight()});
                ddcontrol.show();
                e.stopPropagation();
            }
            e.preventDefault();
        });

        ddcontrol.addCallback("show", function () {
           $parent.addClass("active");
        });

        ddcontrol.addCallback("hide", function () {
           $parent.removeClass("active");
        });

        // respect access keys
        if ($trigger.attr("accesskey")) {
            AJS.$(document).keypress(function (e) {
                if (e.ctrlKey && String.fromCharCode(e.charCode) === $trigger.attr("accesskey")) {
                    if (ddcontrol != AJS.dropDown.current) {
                        ddcontrol.show();
                        e.preventDefault();
                        e.stopPropagation();
                    }
                }
            });
        }

        // add control to the response
        res.push(ddcontrol);

    });
    return res;
};


/**
 * A NewStandard dropdown, however, with the ability to populate its content's via ajax.
 *
 * @class Ajax
 * @contructor
 * @namespace AJS.dropDown
 * @param {Object} options
 * @return {Object} dropDown instance
 */
AJS.dropDown.Ajax = function (usroptions) {

    var dropdowns, options = {cache: true};

     // extend defaults with user options
    AJS.$.extend(options, usroptions || {});

    // we call with "this" in case we are called in the context of a jQuery collection
    dropdowns = AJS.dropDown.Standard.call(this, options);

    AJS.$(dropdowns).each(function () {

        var ddcontrol = this;

        AJS.$.extend(ddcontrol, {
            getAjaxOptions: function (opts) {
                var success = function (response) {
                    if (options.formatResults) {
                        response = options.formatResults(response);
                    }
                    if (options.cache) {
                        ddcontrol.cache.set(ddcontrol.getAjaxOptions(), response);
                    }
                    ddcontrol.refreshSuccess(response);
                };
                if (options.ajaxOptions) {


                    if (AJS.$.isFunction(options.ajaxOptions)) {
                        return AJS.$.extend(options.ajaxOptions.call(ddcontrol), {success: success});
                    } else {
                        return AJS.$.extend(options.ajaxOptions, {success: success});
                    }
                }
                return AJS.$.extend(opts, {success: success});
            },
            refreshSuccess: function (response) {
                this.$.html(response);
            },
            cache: function () {
                var c = {};
                return {
                    get: function (ajaxOptions) {
                        var data = ajaxOptions.data || "";
                        return c[(ajaxOptions.url + data).replace(/[\?\&]/gi,"")];
                    },
                    set: function (ajaxOptions, responseData) {
                        var data = ajaxOptions.data || "";
                        c[(ajaxOptions.url + data).replace(/[\?\&]/gi,"")] = responseData;
                    },
                    reset: function () {
                        c = {};
                    }
                };
            }(),
            show: function (superMethod) {
                return function (opts) {
                    if (options.cache && !!ddcontrol.cache.get(ddcontrol.getAjaxOptions())) {
                        ddcontrol.refreshSuccess(ddcontrol.cache.get(ddcontrol.getAjaxOptions()));
                        superMethod.call(ddcontrol);
                    } else {
                        AJS.$(AJS.$.ajax(ddcontrol.getAjaxOptions())).throbber({target: ddcontrol.$,
                            end: function () {
                                ddcontrol.reset();
                            }
                        });
                        superMethod.call(ddcontrol);
                        ddcontrol.shadow.hide();
                    }
                };
            }(ddcontrol.show),
            resetCache: function () {
                ddcontrol.cache.reset();
            }
        });
        ddcontrol.addCallback("refreshSuccess", function () {
			ddcontrol.reset();
		});
    });
    return dropdowns;
};


AJS.$.fn.dropDown = function (type, options) {
    type = (type || "Standard").replace(/^([a-z])/, function (match) {
        return match.toUpperCase();
    });
    return AJS.dropDown[type].call(this, options);
};


(function($) {
    /**
     * Creates a new inline dialog
     *
     * @param items jQuery object - the items that trigger the display of this popup when the user mouses over.
     * @param identifier A unique identifier for this popup. This should be unique across all popups on the page and a valid CSS class.
     * @param url The URL to retrieve popup contents.
     * @param initCallback
     * @param options Custom options to change default behaviour. See AJS.InlineDialog.opts for default values and valid options.
     *
     * @return jQuery object - the popup that was created
     */
    AJS.InlineDialog = function(items, identifier, url, options) {
        var opts = $.extend(false, AJS.InlineDialog.opts, options);
        var hideDelayTimer;
        var showTimer;
        var beingShown = false;
        var shouldShow = false;
        var contentLoaded = false;
        var mousePosition;
        var targetPosition;
        $(opts.container).append($('<div id="inline-dialog-' + identifier + '" class="aui-inline-dialog"><div class="contents"></div><div id="arrow-' + identifier + '" class="arrow"></div></div>'));
        var popup = $("#inline-dialog-" + identifier);
        var arrow = $("#arrow-" + identifier);
        var contents = popup.find(".contents");

//        AJS.log(opts);

        contents.css("width", opts.width + "px");
        contents.mouseover(function(e) {
            clearTimeout(hideDelayTimer);
            popup.unbind("mouseover");
            //e.stopPropagation();
        }).mouseout(function() {
            hidePopup();
        });

        var showPopup = function() {
            if (popup.is(":visible")) {
                return;
            }
            showTimer = setTimeout(function() {
                if (!contentLoaded || !shouldShow) {
                    return;
                }
                $(items).addClass("active");
                beingShown = true;
                // retrieve the position of the click target. The offsets might be different for different types of targets and therefore
                // either have to be customisable or we will have to be smarter about calculating the padding and elements around it

                var posx = targetPosition.target.offset().left + opts.offsetX;
                var posy = targetPosition.target.offset().top + targetPosition.target.height() + opts.offsetY;

                var diff = $(window).width() - (posx + opts.width + 30);
                if (diff<0) {
                    popup.css({
                        right: "20px",
                        left: "auto"
                    });
                    arrow.css({
                        left: -diff + (targetPosition.target.width() / 2) + "px",
                        right: "auto"
                    });
                } else {
                    popup.css({
                        left: posx + "px",
                        right: "auto"
                    });

                    arrow.css({
                        left: targetPosition.target.width() / 2 + "px",
                        right: "auto"
                    });
                }

                var bottomOfViewablePage = (window.pageYOffset || document.documentElement.scrollTop) + $(window).height();
                if ((posy + popup.height()) > bottomOfViewablePage) {
                    posy = bottomOfViewablePage - popup.height() - 5;
                    popup.mouseover(function() {
                        clearTimeout(hideDelayTimer);
                    }).mouseout(function() {
                        hidePopup();
                    });
                }
                popup.css({
                    top: posy + "px"
                });

                var shadow = $("#inline-dialog-shadow").appendTo(popup).show();
                // reset position of popup box
                popup.fadeIn(opts.fadeTime, function() {
                    // once the animation is complete, set the tracker variables
                    // beingShown = false; // is this necessary? Maybe only the shouldShow will have to be reset?
                });

                shadow.css({
                    width: contents.outerWidth() + 32 + "px",
                    height: contents.outerHeight() + 25 + "px"
                });
                $(".b", shadow).css("width", contents.outerWidth() - 26 + "px");
                $(".l, .r", shadow).css("height", contents.outerHeight() - 21 + "px");
            }, opts.showDelay);
        };

        var hidePopup = function(delay) {
            shouldShow = false;
            // only exectute the below if the popup is currently being shown
            if (beingShown) {
                delay = (delay == null) ? opts.hideDelay : delay;
                clearTimeout(hideDelayTimer);
                clearTimeout(showTimer);
                // store the timer so that it can be cleared in the mouseover if required
                hideDelayTimer = setTimeout(function() {
                    $(items).removeClass("active");
                    popup.fadeOut(opts.fadeTime, function() { opts.hideCallback.call(popup[0].popup); });
                    beingShown = false;
                    shouldShow = false;
                    if (!opts.cacheContent) {
                        //if not caching the content, then reset the
                        //flags to false so as to reload the content
                        //on next mouse hover.
                        contentLoaded = false;
                        contentLoading = false;
                    }
                }, delay);
            }
        };

        // the trigger is the jquery element that is triggering the popup (i.e., the element that the mousemove event is bound to)
        var initPopup = function(e,trigger) {
            $(".aui-inline-dialog").each(function() {
                if (typeof this.popup != "undefined")
                    this.popup.hide();
            });

            mousePosition = { x: e.pageX, y: e.pageY };
            var targetOffset = $(e.target).offset();
            targetPosition = {target: $(e.target)};

            if (!beingShown) {
                clearTimeout(showTimer);
            }
            shouldShow = true;
            var doShowPopup = function() {
                contentLoaded = true;
                opts.initCallback.call({
                    popup: popup,
                    hide: function () {hidePopup(0);},
                    id: identifier,
                    show: function () {showPopup();}
                });
                showPopup();
            };

            // lazy load popup contents
            if (!contentLoading) {
                contentLoading = true;
                if ($.isFunction(url)) {
                    // If the passed in URL is a function, execute it. Otherwise simply load the content.
                    url(contents, trigger, doShowPopup);
                } else {
                    contents.load(url, function() {
                        contentLoaded = true;
                        opts.initCallback.call({
                            popup: popup,
                            hide: function () {hidePopup(0);},
                            id: identifier,
                            show: function () {showPopup();}
                        });
                        showPopup();
                    });
                }
            }
            // stops the hide event if we move from the trigger to the popup element
            clearTimeout(hideDelayTimer);
            // don't trigger the animation again if we're being shown
            if (!beingShown) {
                showPopup();
            }
            return false;
        };

        popup[0].popup = {popup: popup, hide: function () {
            hidePopup(0);
        }, id: identifier, show: function () {
            showPopup();
        }};

        var contentLoading = false;
        if (opts.onHover) {
            $(items).mousemove(function(e) {
                initPopup(e,this);
            }).mouseout(function() {
                hidePopup();
            });
        } else {
            $(items).click(function(e) {
                initPopup(e,this);
                return false;
            }).mouseout(function() {
                hidePopup();
            });
        }

        contents.click(function(e) {
            e.stopPropagation();
        });

        $("body").click(function() {
            hidePopup(0);
        });

        return popup;
    };

    AJS.InlineDialog.opts = {
        onHover: false,
        fadeTime: 100,
        hideDelay: 10000,
        showDelay: 0,
        width: 300,
        offsetX: 0,
        offsetY: 10,
        container: "body",
        cacheContent : true,
        hideCallback: function(){}, // if defined, this method will be exected after the popup has been faded out.
        initCallback: function(){} // A function called after the popup contents are loaded. `this` will be the popup jQuery object, and the first argument is the popup identifier.
    };

    AJS.toInit(function() {
        $("body").append($('<div id="inline-dialog-shadow"><div class="tl"></div><div class="tr"></div><div class="l"></div><div class="r"></div><div class="bl"></div><div class="br"></div><div class="b"></div></div>'));
        $("#inline-dialog-shadow").hide();
    });
})(jQuery);
