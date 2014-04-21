/**
 * A generic control for toggling a blocks state. For example expanded and collapsed.
 *
 * <h4>Use </h4>
 *
 * <h5>Markup:</h5>
 *
 * <pre>
 * <div id="must-be-unique" class="twixi-block expanded">
 *       <div class="twixi-wrap verbose">
 *           <a href="#" class="twixi">Contract</a>
 *           <div class="action-details">...</div>
 *       </div>
 *       <div class="twixi-wrap concise">
 *           <a href="#" class="twixi">Expand</a>
 *           <div class="action-details flooded">...</div>
 *       </div>
 * </div>
 * </pre>
 *
 * <h5>CSS:</h5>
 *
 * <pre>
 * .twixi-block.expanded .concise {display:none;}
 * .twixi-block.contracted .concise {display:block;}
 * .twixi-block.contracted .concise {display:block;}
 * .twixi-block.expanded .concise {display:none;}
 * </pre>
 *
 * <h5>JavaScript</h5>
 *
 * <pre>
 * JIRA.ToggleBlock();
 * </pre>
 *
 * @constructor JIRA.ToggleBlock
 */
JIRA.ToggleBlock = Class.extend({

    /**
     * Options substituted if user does not provide them to constructor
     *
     * @method getDefautOptions
     * @return {Object}
     */
    getDefautOptions: function() {
        return {
            blockSelector: ".twixi-block",
            triggerSelector: ".twixi",
            eventType: "click",
            collapsedClass: "collapsed",
            expandedClass: "expanded",
            cookieName: "jira.toggleblocks.cong.cookie",
            cookieCollectionName: "twixi-blocks",
            autoFocusTrigger: true
        };
    },

    /**
     * Gets collapsed ids from cookie, and applies collapsed class to element
     *
     * @method _updateTwixiBlockIdInCookie
     * @private
     * @return {Object} instance of self to support chaining
     */
    _collapseTwixiBlocksFromCookie: function () {

        var block,
            val = readFromConglomerateCookie(this.options.cookieName, this.options.cookieCollectionName, '');

        val = val.replace(/\./g, "\\.");
        if (/#\w+/.test(val)) {
            block = AJS.$(val);
            if (block.is(this.options.blockSelector)) {
                if (!this.isPermlink()) {
                    block.removeClass(this.options.expandedClass).addClass(this.options.collapsedClass);
                }
            }
        }
        return this;
    },

    /**
     * Adds the Block ID to the cookie if it's not there, removes it if it is there
     *
     * @method _updateTwixiBlockIdInCookie
     * @private
     * @param blockId
     * @return {Object} instance of self to support chaining
     */
    _updateTwixiBlockIdInCookie: function (blockId) {
        // JRADEV-1736
        // permlinks are always expanded, don't pollute preferences
        if (!this.isPermlink()) {
            // lets not pollute our cookie with an invalid id
            if (!/#\w+/.test(blockId)) {
                return this;
            }
            var val = readFromConglomerateCookie(this.options.cookieName, this.options.cookieCollectionName, ''),
                blockLength = (',' + val + ',').indexOf(',' + blockId + ',') + 1;
            if (blockLength) {
                // Remove the ID from the value to be stored in the cookie
                if (val.indexOf(',' + blockId) + 1) {
                    val = val.replace(',' + blockId, '');
                } else {
                    val = val.replace(blockId, '');
                }
            } else {
                // Append the ID to the value to be stored in the cookie
                val = val.length ? val + ',' + blockId : blockId;
            }
            // Save the value to the cookie
            saveToConglomerateCookie(this.options.cookieName, this.options.cookieCollectionName, val);
        }
        return this;
    },

    /**
     * Contracts specified block, and adds contracted state to cookie
     *
     * @method expand
     * @param {HTMLelement, jQuery} block - element to expand, must match specified <strong>blockSelector</strong> option
     * @return {Object} instance of self to support chaining
     */
    contract: function (block) {
        block = jQuery(block);
        if (block.is(this.options.blockSelector)) {
            block.removeClass(this.options.expandedClass).addClass(this.options.collapsedClass);
            if (this.options.persist !== false) {
                this._updateTwixiBlockIdInCookie('#'+ block.attr('id'));
            }
        }

        AJS.$(block).trigger("contractBlock");

        return this;
    },

    /**
     * Expands specified block, and removes contracted state to cookie
     *
     * @method expand
     * @param {HTMLElement | jQuery} block - element to expand, must match specified <strong>blockSelector</strong> option
     * @return {Object} instance of self to support chaining
     */
    expand: function (block) {
        block = jQuery(block);
        if (block.is(this.options.blockSelector)) {
            block.removeClass(this.options.collapsedClass).addClass(this.options.expandedClass);
            // save state to cookie
            if (this.options.persist !== false) {
                this._updateTwixiBlockIdInCookie('#'+ block.attr('id'));
            }
        }

        AJS.$(block).trigger("expandBlock");

        return this;
    },

    /**
     * Toggles expanded and collapsed class on the specified block (option blockSelector)
     *
     * @method toggle   
     * @param twikiBlockChild - child of block element to be toggled
     * @return {Object} instance of self to support chaining
     */
    toggle: function (twikiBlockChild) {
        var block = AJS.$(twikiBlockChild).closest(this.options.blockSelector);

        if (!block.hasClass(this.options.collapsedClass)){ // Switch to concise view
            this.contract(block);

        } else { // Switch to verbose view
            this.expand(block);
        }

        if (this.options.autoFocusTrigger) {
            block.find(this.options.triggerSelector + ':visible').focus(); // Set focus on the newly visible twixi as the previous one will be hidden
        }

        return this;
    },

    /**
     * Returns whether the page was reached via a permLink or not
     *
     * @since 4.2
     * @method isPermlink
     * @return {boolean} true if this is a permlink
     */
    isPermlink: function()
    {
        var queryString = jQuery.query.load(location.href);
        return (queryString.get("focusedCommentId") !== "" || queryString.get("focusedWorklogId") !== "");
    },

    /**
     * Triggers a toggle event from the specified element (triggerSelector) and event (eventType)
     *
     * @method addTrigger
     * @param triggerSelector
     * @param eventType
     * @return {Object} instance of self to support chaining
     */
    addTrigger: function (triggerSelector, eventType) {

        var thisInstance = this,
            $doc = AJS.$(document),
            lastMousedown = 0;

        if (triggerSelector) {

            eventType = eventType || "click";

            // Double-clicks alter the text selection, which we don't want to happen, so
            // the following prevents the browser from selecting text.
            if (eventType === "dblclick") {
                if (document.selection) {
                    // For IE, clear any text selections when the user double-clicks.
                    $doc.delegate(triggerSelector, "dblclick", function() {
                        document.selection.empty();
                    });
                } else {
                    // For non-IE, return false from the "mousedown" event when it's the second
                    // mousedown in a short time, i.e. just before a "dblclick" event is triggered.
                    $doc.delegate(triggerSelector, "mousedown", function() {
                        var now = new Date().getTime(),
                            allowSelection = now - lastMousedown > 750;
                        lastMousedown = now;
                        return allowSelection;
                    });
                }
            }

            $doc.delegate(triggerSelector, eventType, function () {
                thisInstance.toggle(this);
            });
        }

        return this;
    },

    /**
     * Adds a function to be called <strong>AFTER</strong> specified method has executed
     *
     * @method addCallback
     * @param methodName - Name of public/private method
     * @param callback - function to be executed
     */
    addCallback: function (methodName, callback) {
        jQuery.aop.after({target: this, method: methodName}, callback);
        return this;
    },

    /**
     *
     * @constructor
     * @param {Object} options
     *
     * <dl>
     *  <dt>{String} blockSelector</dt>
     *      <dd><strong>Default: <em>.twixi-block</em></strong><dd>
     *      <dd></dd>
     *  <dt>{String} triggerSelector</dt>
     *      <dd>Selector for element that executes toggle</dd>
     *      <dd><strong>Default: <em>.twixi</em></strong></dd>
     *  <dt>{String} eventType</dt>
     *      <dd>Event applied to option <strong>triggerSelector</strong>, which when occurs executes toggle</dd>
     *      <dd><strong>Default: <em>click</em></strong></dd>
     *  <dt>{String} collapsedClass</dt>
     *      <dd>Class name to be applied to element specified in option <strong>blockSelector</strong> when collapsed</dd>
     *      <dd><strong>Default: <em>collapsed</em></strong></dd>
     *  <dt>{String} expandedClass</dt>
     *      <dd>Class name to be applied to element specified in option <strong>blockSelector</strong> when expanded</dd>
     *      <dd><strong>Default: <em>expanded</em></strong></dd>
     *  <dt>{String} cookieName</dt>
     *      <dd>Actual cookie value will be stored in</dd>
     *      <dd><strong>Default: <em>jira.viewissue.cong.cookie</em></strong></dd>
     *  <dt>{String} cookieCollectionName</dt>
     *      <dd>Namespace inside of cookie</dd>
     *      <dd><strong>Default: <em>twixi-blocks</em></strong></dd>
     * </dl>
     */
    init: function (options) {

        var thisInstance = this;

        options = options || {};

        this.options = jQuery.extend(this.getDefautOptions(), options);

        AJS.$(document).delegate(this.options.triggerSelector, this.options.eventType, function (e) {
            if (!(thisInstance.options.originalTargetIgnoreSelector && jQuery(e.originalTarget).is(thisInstance.options.originalTargetIgnoreSelector))){
                thisInstance.toggle(this);
                e.preventDefault();
            }
        });
        
        if (this.options.persist !== false) {
            // need to wait until the elements are actually available to collapse them.
            jQuery(function () {
                thisInstance._collapseTwixiBlocksFromCookie();
            });
        }
    }
});
