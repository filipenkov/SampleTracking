/**
 * @module Controls
 * @namespace AJS
 */

/**
 * Shorten long lists with an ellipsis
 *
 * <h4>Use </h4>
 *
 * <h5>Markup:</h5>
 *
 * <pre>
 * <div id="mylist">
 *      <a href='#'>a</a>
 *      <a href='#'>b</a>
 *      <a href='#'>c</a>
 *      <a href='#'>d</a>
 *      <a href='#'>e</a>
 *      <a href='#'>f</a>
 *      <a href='#'>g</a>
 *      <a href='#'>h</a>
 *      <a href='#'>i</a>
 *      <a href='#'>j</a>
 *      <a href='#'>k</a>
 * </div>
 * </pre>
 *
 * <h5>JavaScript</h5>
 *
 * <pre>
 * // no options
 * new AJS.Shortener("#mylist");
 *
 * // with options
 * new AJS.Shortener({
 *      element: "#myList",
 *      numRows: 2
 * });
 * </pre>
 *
 * @class Shortener
 * @requires AJS
 *
 */
AJS.Shortener = Class.extend({

    /**
     * Gets default options
     *
     * @method _getDefaultOptions
     * @private
     *
     */
    _getDefaultOptions: function () {
        return {
            items: "a, span",
            numRows: 1,
            shortenText: "hide",
            shortenOnInit: true,
            persist: true
        };
    },

    /**
     * Creates a shorten control
     *
     * @constructor
     * @param options
     *
     * <dl>
     *  <dt>{String, jQuery} items</dt>
     *      <dd>selector or jQuery collection specifing items</dd>
     *      <dd><strong>Default: <em>a</em></strong><dd>
     *  <dt>{Number} numRows</dt>
     *      <dd>Number of rows to display when shortened</dd>
     *      <dd><strong>Default: <em>1</em></strong></dd>
     *  <dt>{String} shortenText</dt>
     *      <dd>Text to display in link at the end of the list when expanded</dd>
     *      <dd><strong>Default: <em>hide</em></strong></dd>
     *  <dt>{String} shortenOnInit</dt>
     *      <dd>If true will shorten onload</dd>
     *      <dd><strong>Default: <em>true</em></strong></dd>
     * </dl>
     *
     */
    init: function (options) {

        var that = this,
            timer = null;

        if (typeof options === "string") {
            options = {element: options};
        }

        options = options || {};

        this.options = AJS.$.extend(this._getDefaultOptions(), options);
        this.$container = AJS.$(this.options.element);

        // requires no padding
        this.$container.css({
            padding: 0,
            display: "block"
        });

        if (this.options.persist) {
            AJS.Cookie.read(this.$container.attr("id"), 'hidden');
        }

        if (this._isShortenedOnLoad()) {
            this.shorten();
        } else {
            this.expand();
        }

        AJS.$(window).resize(function () {
            if (!that.expanded && timer === null) {
                // do this so the function is not hitting too hard in Safari and Chrome
                // and to save us from accidental Chrome multithreading
                that.timer = setTimeout(function() {
                    that.shorten();
                    that.timer = null;
                },100);
            }
        });
    },

    /**
     * Should list should be shortened on load. This is determined by cookie, if persist options is true, or "shortenOnInit"
     * option. Please not that if persist option is set to true, the list will not be shortened if use has expanded it previously
     * regardless of the shortenOnInit set to true.
     *
     * @method _isShortenedOnLoad
     * @private
     * @return boolean
     */
    _isShortenedOnLoad: function () {
        var cookieValue = AJS.Cookie.read(this.$container.attr("id"));
        if (cookieValue === "hidden") {
            return true;
        } else if (cookieValue === "shown") {
            return false;
        } else if (this.options.shortenOnInit) {
            return true;
        }
        return false;
    },

    _renders: {

        /**
         * Creates the jQuery object representing an ellipsis. The ellipsis appended to the shortened list of items. It
         * contains text representing how many items have been hidden. When clicked it reveals the full list.
         * The ellipsis has a class of <strong>ellipsis</strong> and styling should be controlled in css.
         *
         * @method _renders.ellipsis
         * @private
         *
         * @param {String} itemsHidden - number of items hidden
         * @return {jQuery} jQuery wrapped HTML element
         *
         */
        ellipsis: function (itemsHidden) {
            return AJS.$("<a href='#' class='ellipsis'>(" + (itemsHidden) + ")</a><br />");
        },

        /**
         * Creates the jQuery object representing the shorten tip. The shorten tip is appended to the expanded list of
         * items. When clicked it shortens the list to the user specified paramater <strong>numRows</strong> Ellipsis has
         * a calss of <strong>icon-hide</strong> and styling should be controlled in css.
         *
         * @method _renders.shortenTip
         * @private
         *
         * @param {String} removeText - number of items hidden
         * @return {jQuery} jQuery wrapped HTML element
         *
         */
        shortenTip: function (removeText) {
            return AJS.$("<a title='Hide' class='icon icon-hide' href='#'><span>" + removeText + "</span></a>");
        }
    },

    /**
     * Removes ellipsis and it's associated &lt;br&gt; tag from DOM
     *
     * @method _removeEllipsis
     * @private
     */
    _removeEllipsis: function () {
        if (this.$ellipsis) {
            this.$ellipsis.remove();
            this.$ellipsis = null;
        }
    },

    /**
     * Determines if the ellipsis has wrapped.
     *
     * @method _hasEllipsisWrapped
     * @private
     * @return {Boolean}
     *
     */
    _hasEllipsisWrapped: function () {
        return this.$ellipsis.attr("offsetTop") > this.$ellipsis.prev().attr("offsetTop");
    },

    /**
     * Inserts ellipsis into DOM
     *
     * @method _insertEllipsis
     * @private
     * @param {Number} tryInsertAfter - index of item to insert ellipsis after
     * @param {Number} itemsHidden - count of items hidden
     */
    _insertEllipsis: function (tryInsertAfter, itemsHidden) {

        var that = this,
            insertAfterCount = tryInsertAfter;

        this.$ellipsis = this._renders.ellipsis(itemsHidden).insertAfter(this.$items[insertAfterCount]);

        if (insertAfterCount !== 0 && this._hasEllipsisWrapped()) {
            insertAfterCount--;
            that._removeEllipsis();
            that.$ellipsis = that._renders.ellipsis(itemsHidden + 1).insertAfter(this.$items[insertAfterCount]);
        }

        this.$ellipsis.click(function (e) {
            e.preventDefault();
            that.expand();
        });
        return insertAfterCount;
    },

    /**
     * Contracts list to user specified number of rows, adding a link to shorten.
     *
     * @method shorten
     * @param {Boolean} moveToShortened - scroll element into view
     *
     */
    shorten: function (moveToShortened) {


        function hasWrapped () {
            // Why are we checking offsetLeft as well as offsetTop ... because IE7 lies!
            if(i < that.$items.length - 1) {
                return that.$items[i].offsetTop < that.$items[i+1].offsetTop
                        || that.$items[i].offsetLeft > that.$items[i+1].offsetLeft
                        || that.$items[i].realHeight > that.$items[i+1].realHeight + 5;
            } else {
                return that.$items[i].offsetTop > that.$items[i-1].offsetTop
                        || that.$items[i].offsetLeft < that.$items[i-1].offsetLeft
                        || that.$items[i].realHeight > that.$items[i-1].realHeight + 5;
            }
        }

        var availableRows = this.options.numRows,
            i = 0,
            rows = 0,
            containerheight = 0,
            ellipseIndex = 0,
            that = this;

        this._removeEllipsis();

        if (this.$shortenTip) {
            this.$shortenTip.remove();
        }

        this.$items = this.$container.children(this.options.items);

        this.$items.each(function(){
            if (this.offsetHeight !== 0) {
                this.realHeight = this.offsetHeight;
            } else {
                this.realHeight = AJS.$(this).children(that.options.items).attr("offsetHeight") || 0;
            }
        });

        if (this.$items.length < 2) {
            return;
        }

        this.$container.css({overflow: "hidden"});

        do {
            if (hasWrapped()) {
                rows++;

                if (rows === availableRows) {
                    if(i===0) {
                        ellipseIndex = that._insertEllipsis(0, this.$items.length - 1);
                    } else {
                        ellipseIndex = that._insertEllipsis(i-1, this.$items.length - i);
                    }
                    containerheight = (this.$items[ellipseIndex].offsetTop - that.$items[0].offsetTop);
                    containerheight += this.$items[ellipseIndex].realHeight;
                    if ((that.$items[ellipseIndex].offsetTop) < this.$ellipsis[0].offsetTop) {
                        containerheight += this.$ellipsis[0].offsetHeight + (this.$ellipsis[0].offsetTop - that.$items[ellipseIndex].offsetTop - this.$items[ellipseIndex].realHeight);
                    }
                    that.$container.height(containerheight+3);
                    break;
                } else if (i === 0) {
                    // handle if the first item is really long
                    availableRows = availableRows + 1;
                }
            }

            i++;
        } while(this.$items[i]);

        if (this.options.persist) {
            AJS.Cookie.save(this.$container.attr("id"), 'hidden');
        }

        // scrolls to position of shortened version if out of the viewport
        if (moveToShortened) {
            this.$container.scrollIntoView();
        }

        // Hack for IE8 - as shortening the field will leave the things below it hanging in the People group on the right hand side.
        if (jQuery.browser.msie  && jQuery.browser.version  >= 8 && jQuery.browser.version < 9) {
            AJS.$('body').addClass('reflow');
        }

        delete this.expanded;

    },

    /**
     * Expands list to full height, adding a link to shorten
     *
     * @method expand
     *
     */
    expand: function () {

        function canBeShortened () {
            return that.$items[0].offsetTop < that.$items[that.$items.length-1].offsetTop || (that.$items[0].realHeight *2) < that.$container[0].offsetHeight ;
        }

        var that = this;

        this.expanded = true;
        this._removeEllipsis();

        this.$items = AJS.$(this.$container.children(this.options.items));
        this.$items.each(function(){

            if (this.offsetHeight !== 0) {
                this.realHeight = this.offsetHeight;
            } else {
                this.realHeight = AJS.$(this).children(that.options.items)[0].offsetHeight;
            }
        });
        this.$container.css({
            height: '',
            overflow: ''
        });

        if (this.$items.length > 1 && canBeShortened()) {
            this.$shortenTip = this._renders.shortenTip(this.options.shortenText);
            this.$shortenTip.appendTo(this.$container).click(function (e) {
                that.shorten(true);
                e.preventDefault();
            });
        }

        if (this.options.persist) {
            AJS.Cookie.save(this.$container.attr("id"), 'shown');
        }

        // Hack for IE
        if (jQuery.browser.msie) {
            AJS.$('body').addClass('reflow');
        }

    }

});

/**
 *
 * jQuery plugin to shorten long lists with an ellipsis.
 *
 * Note: Delegates to AJS.Shortener
 *
 * <h4>Use </h4>
 *
 * // no options
 * AJS.$("#my-container").shorten();
 *
 * // options
 * AJS.$("#my-container").shorten({
 *      numRows: 5
 * });
 *
 * For full options @see AJS.Shortener
 *
 * @module jQuery
 * @param options
 */
jQuery.fn.shorten = function (options) {

    var res = [];
    options = options || {};

    this.each(function () {
        options.element = this;
        res.push(new AJS.Shortener(options));
    });

    return res;
};
