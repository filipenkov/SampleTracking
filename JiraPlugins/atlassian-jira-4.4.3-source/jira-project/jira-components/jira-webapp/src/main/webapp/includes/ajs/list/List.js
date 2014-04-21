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
                instance.unfocusAll();
                instance.index = AJS.$.inArray(this, instance.$visibleItems);
                instance.focus();
            }
        });
    },

    _getDefaultOptions: function () {
        return {
            matchingStrategy: "(^|.*?\\s+)({0})(.*)", // match start of words
            containerSelector: ".aui-list",
            itemSelector: "li"
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
            this.unfocusAll(); // remove focus from all incase multiselect mode was used previously
            ++this.index; // increase the index
            this.focus(this.SCROLL_DOWN); // focus it
        } else if (this.$visibleItems.length > 1) {
            this.unfocusAll();
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
        return this.container().parent();
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
            this.unfocusAll(); // remove focus from all incase multiselect mode was used previously
            --this.index; // decrease the index
            this.focus(this.SCROLL_UP); // focus it

        } else if (this.$visibleItems.length > 0) {
            this.unfocusAll();
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

        if (direction !== undefined) {
            this.scrollTo($target, direction);
        }

        this.lastFocusedItemDescriptor = $target.data("descriptor");
        this.motionDetector.unbind();

        $target.addClass("active");

        // We do not want to scroll the page if we are in a dialog
        // AJS.dim.dim is a reference to the blanket added when dialog is visible.
        // I know! I know! bad coupling, but meh! This control is deprecated anyway.
        if (!AJS.dim.dim) {
            $target.scrollIntoView({
                duration: 100,
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

        this._unassignEvents("document", document);

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
            instance._assignEvents("document", document);
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

    /**
     * Using the array of @see AJS.List.GroupDescriptor and @see AJS.List.ItemDescriptor matches items using the supplied query
     * argument. These items are then rendered. Note: Any previous items in the list a removed.
     *
     * @method generateListFromJSON
     * @param {Array} data
     * @param {String} query
     */
    generateListFromJSON: function (data, query) {

        var event,
            $result = AJS.$('<div>'),
            instance = this,
            ungrouped = [],
            $listItems;

        this.suggestions = 0;
        this.exactMatchIndex = -1;
        this.lastFocusedIndex = -1;
        this.lastQuery = query;

        AJS.$.each(data, function (i, descriptor) {
            if (descriptor instanceof AJS.GroupDescriptor) {
                if (ungrouped.length > 0) {
                    $result.append(instance._generateUngroupedOptions(ungrouped, query));
                    ungrouped = [];
                }
                $result.append(instance._generateOptGroup(descriptor, query));
            } else if (this instanceof AJS.ItemDescriptor) {
                ungrouped.push(descriptor);
            }
        });

        if (ungrouped.length > 0) {
            $result.append(this._generateUngroupedOptions(ungrouped, query));
        }

        if ($result.children().length === 0) {
            this.$container.html(this._render("noSuggestion"));
        } else {

            $result.find("ul:last").addClass("aui-last");

            this.$container.html($result.children());
        }

        this.$container.hide();
        $listItems = AJS.$("li > a", this.$container);
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

        AJS.trigger("contentChange", this.$container);

        this.reset(this.exactMatchIndex >= 0 ? this.exactMatchIndex : this.lastFocusedIndex);
    },

    _generateOption: function (item, query) {
        var replacementText;

        if (query && item.labelSuffix() === "") {
            replacementText = item.label().replace(this.regex, function (_, prefix, match, suffix) {
                return AJS.$("<div>")
                    .append(AJS.$("<span>").text(prefix))
                    .append(AJS.$("<em>").text(match))
                    .append(AJS.$("<span>").text(suffix))
                    .html();
            });
        }

        if (this.exactMatchIndex < 0) {
            var itemValue = AJS.$.trim(item.label()).toLowerCase();
            if (itemValue === AJS.$.trim(query).toLowerCase()) {
                this.exactMatchIndex = this.suggestions;
            } else if (this.lastFocusedIndex < 0 && this.lastFocusedItemDescriptor && itemValue === AJS.$.trim(this.lastFocusedItemDescriptor.label()).toLowerCase()) {
                this.lastFocusedIndex = this.suggestions;
            }
        }

        this.suggestions++;

        return this._render("suggestion", item, replacementText);
    },

    _filterUngroupedOptions: function(options) {
        var instance = this,
            filtered = [],
            keywordsRegex = (this.regexEscapedQuery) ? new RegExp(AJS.format(".*{0}.*", this.regexEscapedQuery), "i") : null;

        AJS.$.each(options, function(i, item) {
                item.labelSuffix("");

                if (instance.regex && !instance.regex.test(item.label())) {

                    // if we didn't match on the label, try to match on keywords
                    // for each keyword that contains the query, add it to the item's suffix string
                    // otherwise return null if we don't match on the label or on any keywords

                    var matchedKeywords = "";

                    if (item.keywords()) {
                        var keywordString = "" + item.keywords(),
                            keywords = keywordString.split(",");

                        for (var i = 0; i < keywords.length; i++) {
                            var keyword = keywords[i];
                            if (keywordsRegex.test(keyword)) {
                                if (matchedKeywords != "") {
                                    matchedKeywords += ", ";
                                }
                                matchedKeywords += keyword;
                            }
                        }
                    }

                    if (matchedKeywords != "") {
                        item.labelSuffix(" " + matchedKeywords);
                        filtered.push(item);
                    }
                } else {
                    filtered.push(item);
                }
        });

        return filtered;
    },

    _setRegexFromQuery: function(query) {
        this.regexEscapedQuery = (query) ? RegExp.escape(query) : null;
        this.regex = (query) ? new RegExp(AJS.format(this.options.matchingStrategy, this.regexEscapedQuery), "i") : null;
    },

    _addOptionsToContainer: function(options, $container, query) {
        var instance = this,
            maxInlineResultsDisplayed = this.options.maxInlineResultsDisplayed,
            hasSuggestion = false;
        AJS.$.each(options, function(i, option) {
            if (!maxInlineResultsDisplayed || i < maxInlineResultsDisplayed || !query) {
                var $suggestion = instance._generateOption(option, query);
                if ($suggestion) {
                    hasSuggestion = true;
                    $container.append($suggestion);
                }
            } else {
                $container.append(instance._render("tooManySuggestions", options.length - i));
                return false;
            }
        });

        return hasSuggestion;
    },

    _generateUngroupedOptions: function (options, query) {
        var instance = this,
            $container = this._render("ungroupedSuggestions");

        this._setRegexFromQuery(query);

        if (query) {
            options = this._filterUngroupedOptions(options);
        }

        var hasSuggestion = this._addOptionsToContainer(options, $container, query);

        if (hasSuggestion) {
            return $container;
        }
    },

    _generateOptGroup: function (groupDescriptor, query) {

        var res = AJS.$(),
            optContainer = this._render("suggestionGroup", groupDescriptor),
            options = groupDescriptor.items();

        this._setRegexFromQuery(query);

        if (query) {
            options = this._filterUngroupedOptions(options);
        }

        var hasSuggestion = this._addOptionsToContainer(options, optContainer, query);

        if (!hasSuggestion) {
            return;
        }

        if (groupDescriptor.label() && groupDescriptor.showLabel() !== false) {
            res = res.add(this._render("suggestionGroupHeading", groupDescriptor));
        }

        res = res.add(optContainer);

        return res;
    },

    _events: {
        document: {
            "aui:keydown aui:keypress": function (event) {
                this._handleKeyEvent(event);
            }
        }
    },

    _renders: {

        suggestion: function (descriptor, replacementText) {

            //adding the label as a class for testing.
            var itemId = AJS.escapeHTML(AJS.$.trim(descriptor.label().toLowerCase()).replace(/[\s\.]+/g, "-")),
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

            if (descriptor.html()) {
                linkElem.html(descriptor.html());
            } else if (!replacementText) {
                linkElem.text(descriptor.label());
            } else {
                linkElem.html(replacementText);
            }

            if (descriptor.labelSuffix()) {
                var suffixSpan = AJS.$("<span class='aui-item-suffix' />").text(descriptor.labelSuffix())
                
                if (suffixSpan) {
                    suffixSpan.appendTo(linkElem);
                }
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
        if (this._latestQuery && $field.val() !== this._latestQuery) {
            // Handle case where user input is inconsistent with suggestion text like a user-inputted-option.
            var inputWords = $field.val().toLowerCase().match(/\S+/g);
            if (inputWords) {
                var html = this.lastFocusedItemDescriptor && this.lastFocusedItemDescriptor.html();
                var $item = html ? AJS.$("<div>").html(html) : $focusedItem;
                var matches = AJS.$.map($item.find("em,b"), function($match) {
                    $match = AJS.$($match);
                    return ($match.text() + AJS.$($match.attr("nextSibling")).text().match(/^\S*/)[0]).toLowerCase();
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
