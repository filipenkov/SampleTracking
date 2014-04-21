/**
 * @deprecated Use AJS.Group instead
 */
AJS.List = AJS.Control.extend({

    init: function (options) {
        options = options || {};
        if (options) {
            this.options = AJS.$.extend(true, this._getDefaultOptions(options), options);
        } else {
            this.options = this._getDefaultOptions(options);
        }

        var instance = this;

        this.containerSelector = AJS.$(this.options.$layerContent);
        this.disabled = true;
        this.reset();

        if (this.options.selectionHandler) {
            this.$container.delegate(this.options.itemSelector, "click", function (e) {
                instance.options.selectionHandler.call(instance, e);
            });
        }

        this.$container.delegate(this.options.itemSelector, "mouseover", function () {
            if (instance.motionDetector.moved && !instance.disabled) {
                instance.index = AJS.$.inArray(this, instance.$visibleItems);
                instance.focus();
            }
        });

        this._renders = jQuery.extend({}, this._renders, this.options.renderers);

    },

    _getDefaultOptions: function () {
        return {
            delegateTarget: document,
            matchingStrategy: "(^|.*?(\\s+|\\())({0})(.*)", // match start of words, including after '('
            containerSelector: ".aui-list",
            itemSelector: "li.aui-list-item"
        };
    },

    index: 0,

    /**
     * Focuses item element in list. If the mutliselectMode is set to true will toogle that elements focused state. 
     *
     * @method moveToNext
     */
    moveToNext: function () {
        if (this.index < this.maxIndex) {
            ++this.index; // increase the index
            this.focus(this.SCROLL_DOWN); // focus it
        } else if (this.$visibleItems.length > 1) {
            this.index = 0;
            this.focus(this.SCROLL_DOWN);
        }
        this.motionDetector.wait();
    },

    SCROLL_UP: -1,

    SCROLL_DOWN: 1,

    container: function (container) {
        if (container) {
            this.$container = AJS.$(container);
            this.containerSelector = container;
        } else {
            return this.$container;
        }
    },

    scrollContainer: function () {
        return this.options.scrollContainer || this.container().parent();
    },

    /**
     * Removes focus from all items in list
     *
     * @method unfocusAll
     */
    unfocusAll: function () {
        this.$visibleItems.removeClass("active");
    },

    /**
     * Focuses item element in list. If the mutliselectMode is set to true will toogle that elements focused state.
     *
     * @method moveToPrevious
     */
    moveToPrevious: function () {
        if (this.index > 0) {
            --this.index; // decrease the index
            this.focus(this.SCROLL_UP); // focus it

        } else if (this.$visibleItems.length > 0) {
            this.index = this.$visibleItems.length-1;
            this.focus(this.SCROLL_UP);
        }
        this.motionDetector.wait();
    },

    /**
     * Unfocus element at this.index
     *
     * @method focus
     * @param {Number} direction - If defined will scroll element into view
     */
    unfocus: function (direction) {
        if (direction !== undefined) {
            this.scrollTo(this.$visibleItems.eq(this.index), direction);
        }
        this.$visibleItems.eq(this.index).removeClass("active");
    },

    /**
     * Scrolls the container to ensure the element is visible
     *
     * @method scrollTo
     * @param {jQuery} $target - element to scroll to
     * @param {Number} direction - direction of the scroll
     */
    scrollTo: function ($target, direction) {

       var $scrollContainer = this.scrollContainer(),
            offsetTop = $target.offset().top - this.$container.offset().top;


        if ($target[0] === this.$visibleItems[0]) {
            $scrollContainer.scrollTop(0)
        } else if ($scrollContainer.scrollTop() + $scrollContainer.height() < offsetTop + $target.outerHeight() ||
            $scrollContainer.scrollTop() > offsetTop) {
            if (direction === -1) {
                $scrollContainer.scrollTop(offsetTop);
            } else if (direction === 1) {
                $scrollContainer.scrollTop(offsetTop + $target.outerHeight() - $scrollContainer.height());
            }
        }
    },

    /**
     * Focus element at this.index
     *
     * @method focus
     * @param {Number} direction - If defined will scroll element into view
     */
    focus: function (direction) {

        var $target = this.$visibleItems.eq(this.index);

        this.unfocusAll(); // remove previous focused element if there is one.

        if (direction !== undefined) {
            this.scrollTo($target, direction);
        }

        this.lastFocusedItemDescriptor = $target.data("descriptor");
        this.motionDetector.unbind();

        $target.addClass("active");

        // We do not want to scroll the page if we are in a dialog
        // AJS.dim.dim is a reference to the blanket added when dialog is visible.
        // I know! I know! bad coupling, but meh! This control is deprecated anyway.
        if (!AJS.dim.$dim) {
            $target.scrollIntoView({
                marginBottom: 50,
                callback: AJS.$.proxy(this.motionDetector, "wait")
            });
        } else {
            this.motionDetector.wait();
        }
    },

    motionDetector: new JIRA.Mouse.MotionDetector(),

    disable: function () {
        if (this.disabled) {
            return;
        }

        this._unassignEvents("delegateTarget", this.options.delegateTarget);

        this.disabled = true;
        this.lastFocusedItemDescriptor = null;
        this.motionDetector.unbind();
    },

    enable: function () {
        var instance = this;
        if (!instance.disabled) {
            return;
        }

        instance.motionDetector.wait();

        window.setTimeout(function () {
            instance._assignEvents("delegateTarget", instance.options.delegateTarget);
        }, 0);

        instance.disabled = false;
        this.scrollContainer().scrollTop(0);
    },

    getFocused: function () {
        return this.$visibleItems.filter(".active");
    },

    reset: function (index) {
        // RegExps cached to boost IE7 performance
        var noSuggestionsClassName = /(?:^|\s)no-suggestions(?!\S)/;
        var hiddenClassName = /(?:^|\s)hidden(?!\S)/;

        this.$container = AJS.$(this.options.containerSelector);
        this.items = AJS.$(this.options.itemSelector, this.$container).filter(function() { return !noSuggestionsClassName.test(this.className) });
        this.$visibleItems = this.items.filter(function() { return !hiddenClassName.test(this.className) });
        this.groups = AJS.$(this.options.groupSelector, this.$container);
        this.maxIndex = this.$visibleItems.length - 1;
        this.index = this.$visibleItems[index] ? index : 0;

        this.focus();
    },

    selectValue: function (value) {
        var matchedItem = this.$container.find('li a').filter(function () {
           return AJS.$(this).parent().data('descriptor').value() == value;
        });
        if (!matchedItem.length) {
            AJS.log("WARN: No List item found with Decriptor value '" + value + "'");
        }
        matchedItem.click();
    },

    _getLinkFromItem: function (item) {
        var link;

        item = AJS.$(item);
        if (item.is("a")) {
            link = item;
        } else {
            link = item.find("a");
        }

        if (!link.length) {
            throw new Error("AJS.List._getLinkFromItem: could not find a link node");
        } else {
            return link;
        }
    },

    _makeResultDiv: function (data, context) {

        var $result = AJS.$('<div>'),
            instance = this,
            ungrouped = [];

        function appendUngrouped() {
            if (ungrouped.length > 0) {
                $result.append(instance._generateUngroupedOptions(ungrouped, context));
                ungrouped = [];
            }
        }

        AJS.$.each(data, function (i, descriptor) {
            if (descriptor instanceof AJS.GroupDescriptor) {
                appendUngrouped();
                $result.append(instance._generateOptGroup(descriptor, context));
            } else if (this instanceof AJS.ItemDescriptor) {
                ungrouped.push(descriptor);
            }
        });

        appendUngrouped();

        return $result;
    },

    // context is used by overriding subclasses
    _addResultToContainer: function ($result, context) {
        if (!$result.children().length) {
            this.$container.html(this._render("noSuggestion"));
        } else {
            $result.find("ul:last").addClass("aui-last");
            this.$container.html($result.children());
        }
    },

    /**
     * Using the array of @see AJS.List.GroupDescriptor and @see AJS.List.ItemDescriptor matches items using the supplied query
     * argument. These items are then rendered. Note: Any previous items in the list a removed.
     *
     * @method generateListFromJSON
     * @param {Array} data
     * @param {String} query
     * @param {String} groupId - the id of a group to target with the generated elements
     */
    generateListFromJSON: function (data, context) {

        context = context || {};

        var query = context.query;

        this.suggestions = 0;
        this.exactMatchIndex = -1;
        this.lastFocusedIndex = -1;
        this.lastQuery = query;

        var $result = this._makeResultDiv(data, context);
        this._addResultToContainer($result, context);

        this.$container.hide();

        var $listItems = AJS.$("li > a", this.$container);
        $listItems.each(function(){
           var elem = AJS.$(this);
           elem.attr("title",elem.text());
        });
        $listItems.css({
            textOverflow:"ellipsis",
            overflow:"hidden"
        });

        this.$container.show();

        // Apply text-overflow after showing this.$container so that we know the list item width.
        $listItems.textOverflow('&#x2026;', false);

        var indexToHighlight = this.exactMatchIndex >= 0 ? this.exactMatchIndex : this.lastFocusedIndex;
        this.reset(indexToHighlight);
    },

    _generateOption: function (item, query, labelRegex) {
        var replacementText;

        // Only highlight query matches if html has NOT been specified - assume the back end knows what it's doing.
        if (!item.html() && labelRegex && labelRegex.test(item.label())) {
            replacementText = item.label().replace(labelRegex, function (_, prefix, spaceOrParenthesis, match, suffix) {
                var div = AJS.$("<div>");

                prefix && div.append(AJS.$("<span>").text(prefix));
                div.append(AJS.$("<em>").text(match));
                suffix && div.append(AJS.$("<span>").text(suffix));

                return div.html();
            });
        }

        if (this.exactMatchIndex < 0) {
            var itemValue = AJS.$.trim(item.label()).toLowerCase();
            if (!item.noExactMatch() && itemValue === AJS.$.trim(query).toLowerCase()) {
                this.exactMatchIndex = this.suggestions;
            } else if (this.lastFocusedIndex < 0 && this.lastFocusedItemDescriptor && itemValue === AJS.$.trim(this.lastFocusedItemDescriptor.label()).toLowerCase()) {
                this.lastFocusedIndex = this.suggestions;
            }
        }

        this.suggestions++;

        return this._render("suggestion", item, replacementText);
    },

    _filterOptions: function(options, regexEscapedQuery, labelRegex) {

        if (!regexEscapedQuery) return options;

        var filtered = [],
            keywordsRegex = new RegExp(AJS.format(".*{0}.*", regexEscapedQuery), "i");

        for (var i = 0, len = options.length; i < len; i++) {
            var item = options[i];

            if (labelRegex.test(item.label())) {
                filtered.push(item);

            } else if (item.keywords()) {
                // if we didn't match on the label, try to match on keywords
                // for each keyword that contains the query, add it to the item's suffix string
                // otherwise return null if we don't match on the label or on any keywords
                var matchedKeywords = [];
                var keywordString = "" + item.keywords(),
                    keywords = keywordString.split(",");

                for (var j = 0; j < keywords.length; j++) {
                    var keyword = keywords[j];
                    if (keywordsRegex.test(keyword)) {
                        matchedKeywords.push(keyword);
                    }
                }

                if (matchedKeywords.length) {
                    item.labelSuffix(" " + matchedKeywords.join(', '));
                    filtered.push(item);
                }
            }
        }

        return filtered;
    },

    _addOptionsToContainer: function(options, $container, query, labelRegex) {
        var instance = this,
            maxInlineResultsDisplayed = this.options.maxInlineResultsDisplayed,
            hasSuggestion = false;

        for (var i = 0, len = options.length; i < len; i++) {
            var option = options[i];
            if (!maxInlineResultsDisplayed || i < maxInlineResultsDisplayed || !query) {
                var $suggestion = instance._generateOption(option, query, labelRegex);
                if ($suggestion) {
                    hasSuggestion = true;
                    $container.append($suggestion);
                }
            } else {
                $container.append(instance._render("tooManySuggestions", options.length - i));
                break;
            }
        }

        return hasSuggestion;
    },

    _filterAndAddOptions: function (options, container, context) {

        context = context || {};
        var regexEscapedQuery, labelRegex, query = context.query;


        if (query) {

            regexEscapedQuery = RegExp.escape(query);
            labelRegex = new RegExp(AJS.format(this.options.matchingStrategy, regexEscapedQuery), "i");

            if (context.filter) {
                options = this._filterOptions(options, regexEscapedQuery, labelRegex);
            }
        }

        return this._addOptionsToContainer(options, container, query, labelRegex);
    },

    _generateUngroupedOptions: function (options, context) {
        var $container = this._render("ungroupedSuggestions");
        var optionsAdded = this._filterAndAddOptions(options, $container, context);
        if (optionsAdded) {
            return $container;
        }
    },

    _generateOptGroup: function (groupDescriptor, context) {

        var res = AJS.$(),
            optContainer = this._render("suggestionGroup", groupDescriptor),
            options = groupDescriptor.items();

        var optionsAdded = this._filterAndAddOptions(options, optContainer, context);
        if (!optionsAdded) {
            return;
        }

        if (groupDescriptor.label() && groupDescriptor.showLabel() !== false) {
            res = res.add(this._render("suggestionGroupHeading", groupDescriptor));
        }

        if (groupDescriptor.footerText()) {
            optContainer.append(this._render("suggestionsGroupFooter", groupDescriptor.footerText()));
        }

        res = res.add(optContainer);

        return res;
    },

    _events: {
        delegateTarget: {
            "aui:keydown aui:keypress": function (event) {
                this._handleKeyEvent(event);
            }
        }
    },

    _renders: {

        suggestion: function (descriptor, replacementText) {

            //adding the label as a class for testing.
            var idSuffix = descriptor.fieldText() || descriptor.label();
            var itemId = AJS.escapeHTML(AJS.$.trim(idSuffix.toLowerCase()).replace(/[\s\.]+/g, "-")),
                listElem = AJS.$('<li class="aui-list-item aui-list-item-li-' + itemId + '">'),
                linkElem = AJS.$('<a />').addClass("aui-list-item-link");

            if (descriptor.selected()) {
                listElem.addClass("aui-checked");
            }

            linkElem.attr("href", descriptor.href() || "#");

            if (descriptor.icon() && descriptor.icon() !== "none") {
                linkElem.addClass("aui-iconised-link").css({backgroundImage: "url(" + descriptor.icon() + ")"});
            }

            if (descriptor.styleClass()) {
                linkElem.addClass(descriptor.styleClass());
            }

            if (replacementText) {
                linkElem.html(replacementText);
            } else if (descriptor.html()) {
                linkElem.html(descriptor.html());
            } else {
                linkElem.text(descriptor.label());
            }

            if (descriptor.labelSuffix()) {
                var suffixSpan = AJS.$("<span class='aui-item-suffix' />").text(descriptor.labelSuffix());
                linkElem.append(suffixSpan);
            }

            listElem.append(linkElem).data("descriptor", descriptor);

            return listElem;
        },
        noSuggestion: function () {
            return AJS.$("<li class='no-suggestions'>" + AJS.I18n.getText("common.concepts.no.matches") + "</li>");
        },
        tooManySuggestions: function(suggestionCount) {
            return AJS.$("<li class='no-suggestions'>" + AJS.I18n.getText("common.concepts.too.many.matches", suggestionCount) + "</li>");
        },
        ungroupedSuggestions: function () {
            return AJS.$('<ul>');
        },
        suggestionGroup: function (descriptor) {
            return AJS.$("<ul class='aui-list-section' />").attr("id", descriptor.label().replace(/\s/g, "-").toLowerCase())
                    .addClass(descriptor.styleClass()).data("descriptor", descriptor);
        },
        suggestionGroupHeading: function (descriptor) {
            var elem = AJS.$("<h5 />").text(descriptor.label()).addClass(descriptor.styleClass()).data("descriptor", descriptor);

            if (descriptor.description()) {
                AJS.$("<span class='aui-section-description' />").text(" (" + descriptor.description() + ")").appendTo(elem);
            }

            return elem;
        },
        suggestionsGroupFooter: function (text) {
            return AJS.$("<li class='aui-list-section-footer' />").text(text);
        }
    },

    _acceptSuggestion: function (item) {

        if (!item instanceof AJS.$) {
            item = AJS.$(item);
        }

        var linkNode = this._getLinkFromItem(item);
        var event = new jQuery.Event("click");

        linkNode.trigger(event, [linkNode]);


        if (!event.isDefaultPrevented()) {
            window.location.href = linkNode.attr("href");
        }
    },

    _acceptUserInput: function($field) {
        // Call the blur event handler on this field, so that accepting user input goes through a different
        // code path to accepting suggestions.
        // TODO: Refactor this so that AJS.List doesn't need to know about is owner AJS.QueryableDropdownSelect.
        $field.triggerHandler("blur");
    },

    _handleSectionByKeyboard: function (e) {
        var $focusedItem = this.getFocused();
        var $field = AJS.$(e.target);

        if ($focusedItem.length === 0) {
            return;
        }

        if ($focusedItem.closest("#user-inputted-option").length > 0) {
            this._acceptUserInput($field);
            return;
        }

        // NOTE: See AJS.QueryableDropdownSelect.prototype._requestThenResetSuggestions for where this._latestQuery is set.
        if (this._latestQuery && AJS.$.trim($field.val()) !== this._latestQuery) {
            // Handle case where user input is inconsistent with suggestion text like a user-inputted-option.
            var inputWords = $field.val().toLowerCase().match(/\S+/g);
            if (inputWords) {
                var html = this.lastFocusedItemDescriptor && this.lastFocusedItemDescriptor.html();
                var $item = html ? AJS.$("<div>").html(html) : $focusedItem;
                var matches = [];
                $item.find("em,b").each(function() {
                    var $match = AJS.$(this),
                        nextText = AJS.$($match.attr('nextSibling')).text().toLowerCase().match(/^\S*/)[0];
                    AJS.$.each($match.text().toLowerCase().match(/\S+/g), function (i, match) {
                        matches.push(match + nextText);
                    });
                });

                for (var i = 0; i < inputWords.length; i++) {
                    var word = inputWords[i];
                    var n = word.length;
                    var hasMatch = false;
                    for (var j = 0; j < matches.length; j++) {
                        if (matches[j].slice(0, n) === word) {
                            hasMatch = true;
                            break;
                        }
                    }
                    if (!hasMatch) {
                        this._acceptUserInput($field);
                        return;
                    }
                }
            }
        }

        // If it's a genuine matching selection, defer to the selectionHandler if one exists.
        // The selection handler may choose to handle accepting a suggestion on its own, in which
        // case we don't need to do any further work.
        if (this.options.selectionHandler && !this.options.selectionHandler.call(this, e)) {
            return;
        }

        this._acceptSuggestion($focusedItem);
    },

    _isValidInput: function () {
        return !this.disabled && this.$container.is(":visible");
    },

    keys: {
        "Down": function (e) {
            this.moveToNext();
            e.preventDefault();
        },
        "Up": function (e) {
            this.moveToPrevious();
            e.preventDefault();
        },
        "Return": function (e) {
            this._handleSectionByKeyboard(e);
        }
    }

});

/**
 * A List that has 2 or more distinct Groups of options that can be updated and filtered independently.
 */
AJS.MultiList = AJS.List.extend({

    // If targetting a particular group in the dropdown, only update that group's contents.
    _addResultToContainer: function ($result, context) {

        var id = context.groupId + '-view',
            container = this.$container.find('#' + id);

        // We have 2 different groups and no-suggestions gets evaluated each time.
        // So we need to remove it each time also as it is very difficult to ensure when we are actually rendering the
        // last group. Groups are appended as a result of ajax requests and other ways...
        this.$container.find(".no-suggestions").remove();

        if (!container.length) {
            container = AJS.$('<div id="' + id + '"></div>');
            this.$container.append(container);
        }

        $result.find("ul:last").addClass("aui-last");

        container.html($result.children());

        if (!this.$container.find('.aui-list-item').length) {
            // If the server found nothing and there were no matched local suggestions already set in the list, there
            // are REALLY no results. Render ze message! Note that we can be confident that there are no results once
            // the server results are known because they'll always be added AFTER any local results.
            this.$container.html(this._render("noSuggestion"));
        }
    }
});

AJS.ListBuilder = {
    newList: function (listOptions) {
        if (listOptions.serverDataGroupId) {
            return new AJS.MultiList(listOptions);
        }

        return new AJS.List(listOptions);
    }
};
